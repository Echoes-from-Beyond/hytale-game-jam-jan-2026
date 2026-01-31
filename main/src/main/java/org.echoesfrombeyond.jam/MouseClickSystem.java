package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MouseClickSystem extends EntityEventSystem<ChunkStore, MouseClickEvent> {
  public MouseClickSystem() {
    super(MouseClickEvent.class);
  }

  @Override
  public void handle(
      int i,
      ArchetypeChunk<ChunkStore> archetypeChunk,
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> commandBuffer,
      MouseClickEvent mouseClickEvent) {
    var jam = archetypeChunk.getComponent(i, Plugin.getJamType());
    assert jam != null;

    System.out.println(jam.clickCounter++);
  }

  @Override
  public Query<ChunkStore> getQuery() {
    return Archetype.of(Plugin.getJamType());
  }
}
