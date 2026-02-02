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
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TurretFireSystem extends EntityTickingSystem<EntityStore> {
  public static final int TURRET_RANGE = 10;
  public static final float TURRET_FIRE_RATE = 5;

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
    var query = Query.and(Plugin.getEnemyType(), TransformComponent.getComponentType(), Query.not(DeathComponent.getComponentType()));

    world.execute(
        () -> {
          var jam = world.getChunkStore().getStore().getResource(Plugin.getJamType());

          var turrets =
              jam.buildings.stream()
                  .filter(building -> building.type == JamSave.BuildingType.Turret)
                  .map(turret -> Map.entry(turret.min.clone().add(turret.max).scale(0.5), turret))
                  .toList();

          store.forEachChunk(
              query,
              (chunk, buffer) -> {
                int size = chunk.size();
                for (int j = 0; j < size; j++) {
                  var transform = chunk.getComponent(j, TransformComponent.getComponentType());
                  assert transform != null;

                  for (var building : turrets) {
                    var val = building.getValue();

                    if (val.fireDelay < TURRET_FIRE_RATE) val.fireDelay += v;
                    else if (transform.getPosition().distanceSquaredTo(building.getKey())
                        <= TURRET_RANGE * TURRET_RANGE) {
                      val.fireDelay = 0;

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
        new SystemDependency<>(Order.AFTER, EnemyDeathSystem.class),
        new SystemDependency<>(Order.AFTER, BattleSystem.class));
  }
}
