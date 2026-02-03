package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TurretFireSystem extends EntityTickingSystem<EntityStore> {
  public static final int TURRET_RANGE = 10;
  public static final float TURRET_FIRE_RATE = 1.5F;

  @Override
  public void tick(
      float v,
      int i,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var stage = archetypeChunk.getComponent(i, Plugin.getStageType());
    assert stage != null;

    if (!stage.isBattle) return;

    var world = store.getExternalData().getWorld();
    var query =
        Query.and(
            Plugin.getEnemyType(),
            TransformComponent.getComponentType(),
            Query.not(DeathComponent.getComponentType()));

    var soundIndex = SoundEvent.getAssetMap().getIndex("SFX_Gun_Fire");
    var player = archetypeChunk.getReferenceTo(i);

    world.execute(
        () -> {
          var jam = world.getChunkStore().getStore().getResource(Plugin.getJamType());

          var turrets =
              jam.buildings.stream()
                  .filter(building -> building.type == JamSave.BuildingType.Turret)
                  .map(turret -> Map.entry(turret.min.clone().add(turret.max).scale(0.5), turret))
                  .toList();

          for (var turret : turrets) {
            if (turret.getValue().fireDelay < TURRET_FIRE_RATE) turret.getValue().fireDelay += v;
          }

          store.forEachChunk(
              query,
              (chunk, buffer) -> {
                int size = chunk.size();
                for (int j = 0; j < size; j++) {
                  var transform = chunk.getComponent(j, TransformComponent.getComponentType());
                  assert transform != null;

                  for (var building : turrets) {
                    var val = building.getValue();

                    if (transform.getPosition().distanceSquaredTo(building.getKey())
                            <= TURRET_RANGE * TURRET_RANGE
                        && val.fireDelay >= TURRET_FIRE_RATE) {
                      val.fireDelay = 0;

                      if (player.isValid())
                        SoundUtil.playSoundEvent3dToPlayer(
                            player,
                            soundIndex,
                            SoundCategory.SFX,
                            building.getKey().x,
                            building.getKey().y,
                            building.getKey().z,
                            100f,
                            1f,
                            store);

                      DamageSystems.executeDamage(
                          j,
                          chunk,
                          buffer,
                          new Damage(Damage.NULL_SOURCE, new DamageCause("Projectile"), 5F));
                    }
                  }
                }
              });
        });
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(Plugin.getStageType());
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(
        new SystemDependency<>(Order.AFTER, EnemyDamageTowerSystem.class),
        new SystemDependency<>(Order.AFTER, EnemyDeathSystem.class));
  }
}
