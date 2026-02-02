package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.Random;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class EnemyDeathSystem extends DeathSystems.OnDeathSystem {
  private static final List<String> DEATH_MESSAGES =
      List.of(
          "AAAAAAA!",
          "ouch...",
          "i just wanted to destroy your buildings...",
          "MY FRIENDS WILL AVENGE ME",
          "it's so kweeover",
          "i just... wanted to taste poutine... one last time...");

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

          var cause = deathComponent.getDeathCause();
          if (cause != null && !cause.getId().equals("OutOfWorld"))
            world.sendMessage(Message.raw(DEATH_MESSAGES.get(RNG.nextInt(DEATH_MESSAGES.size()))));
          stage.killed++;
        });
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(Plugin.getEnemyType());
  }
}
