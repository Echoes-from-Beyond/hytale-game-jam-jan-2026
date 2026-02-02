package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BattleSystem extends EntityTickingSystem<EntityStore> {
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
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(
        Player.getComponentType(), PlayerRef.getComponentType(), Plugin.getStageType());
  }
}
