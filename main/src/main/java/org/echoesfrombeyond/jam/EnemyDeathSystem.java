package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class EnemyDeathSystem extends DeathSystems.OnDeathSystem {
  private static final List<String> DEATH_MESSAGES =
      List.of(
          "Beep boop.",
          "01010011 01001101 01001000",
          "Whirrrr. Buzz.",
          "Target *tk* identified.",
          "Buzz. Sustained critical damage.",
          "System error detected. Beep.");

  private static final Random RNG = new Random();

  @Override
  public void onComponentAdded(
      Ref<EntityStore> ref,
      DeathComponent deathComponent,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> commandBuffer) {
    var enemy = commandBuffer.getComponent(ref, Plugin.getEnemyType());
    assert enemy != null;

    var world = store.getExternalData().getWorld();
    world.execute(
        () -> {
          var playerRef = enemy.player;
          if (playerRef == null || !playerRef.isValid()) return;

          var stage =
              world.getEntityStore().getStore().getComponent(playerRef, Plugin.getStageType());
          if (stage == null || !stage.isBattle) return;

          stage.killed++;
          if (RNG.nextInt(10) != 9) {
            return;
          }
          world.sendMessage(Message.raw(DEATH_MESSAGES.get(RNG.nextInt(DEATH_MESSAGES.size()))));
        });
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(Plugin.getEnemyType());
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(new SystemDependency<>(Order.AFTER, EnemyDamageTowerSystem.class));
  }
}
