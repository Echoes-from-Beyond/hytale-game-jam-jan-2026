package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MouseClickSystem extends WorldEventSystem<ChunkStore, MouseClickEvent> {
  public MouseClickSystem() {
    super(MouseClickEvent.class);
  }

  @Override
  public void handle(
      Store<ChunkStore> store,
      CommandBuffer<ChunkStore> commandBuffer,
      MouseClickEvent mouseClickEvent) {
    var jam = commandBuffer.getResource(Plugin.getJamType());

    System.out.println(jam.clickCounter++);
  }
}
