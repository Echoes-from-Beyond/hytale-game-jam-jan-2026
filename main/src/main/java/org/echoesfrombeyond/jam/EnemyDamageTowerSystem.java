package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class EnemyDamageTowerSystem extends EntityTickingSystem<EntityStore> {
  // in case of stuck entities
  public static final float MAX_LIFETIME = 60;
  public static final int DAMAGE_TOWER_RANGE = 5;

  @Override
  public void tick(
      float v,
      int i,
      ArchetypeChunk<EntityStore> archetypeChunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var enemy = archetypeChunk.getComponent(i, Plugin.getEnemyType());
    var transform = archetypeChunk.getComponent(i, TransformComponent.getComponentType());
    assert enemy != null && transform != null;

    var pos = transform.getPosition().clone();

    var world = store.getExternalData().getWorld();
    var ref = archetypeChunk.getReferenceTo(i);

    enemy.timeSpentAlive += v;

    world.execute(
        () -> {
          var player = enemy.player;

          if (player == null) {
            if (ref.isValid())
              DamageSystems.executeDamage(
                  ref, store, new Damage(Damage.NULL_SOURCE, new DamageCause("OutOfWorld"), 1000F));
            return;
          }

          var stage = store.getComponent(enemy.player, Plugin.getStageType());
          if (stage == null || !stage.isBattle) {
            if (ref.isValid())
              DamageSystems.executeDamage(
                  ref, store, new Damage(Damage.NULL_SOURCE, new DamageCause("OutOfWorld"), 1000F));
            return;
          }

          if (pos.distanceSquaredTo(Plugin.RADIO_LOC) < DAMAGE_TOWER_RANGE * DAMAGE_TOWER_RANGE) {
            var jam = world.getChunkStore().getStore().getResource(Plugin.getJamType());
            if (jam.towerHealth > 0) {
              jam.towerHealth--;
              world.sendMessage(Message.raw("Your tower is taking damage!"));
              if (player.isValid()) store.invoke(player, new HudUpdateSystem.Event());
            }
          }
        });

    if (pos.distanceSquaredTo(Plugin.RADIO_LOC) < DAMAGE_TOWER_RANGE * DAMAGE_TOWER_RANGE
        || enemy.timeSpentAlive > MAX_LIFETIME) {
      DamageSystems.executeDamage(
          i,
          archetypeChunk,
          commandBuffer,
          new Damage(Damage.NULL_SOURCE, new DamageCause("OutOfWorld"), 1000F));
    }
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(
        Plugin.getEnemyType(),
        TransformComponent.getComponentType(),
        Query.not(DeathComponent.getComponentType()));
  }
}
