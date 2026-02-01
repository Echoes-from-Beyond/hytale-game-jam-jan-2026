package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MouseClickSystem extends EntityEventSystem<EntityStore, MouseClickEvent> {
  public MouseClickSystem() {
    super(MouseClickEvent.class);
  }

  @Override
  public void handle(
      int i,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> buffer,
      MouseClickEvent event) {

    var world = buffer.getExternalData().getWorld();
    world.execute(
        () -> {
          if (event.type == MouseButtonType.Right) {
            // dismiss the current selection with rclick
            buffer.tryRemoveComponent(chunk.getReferenceTo(i), Plugin.getPlaceType());
            return;
          }

          JamSave save = world.getChunkStore().getStore().getResource(Plugin.getJamType());
          Vector3i clickLocation = event.pos;
          JamSave.Building clickedBuilding = null;
          for (JamSave.Building build : save.buildings) {
            if (clickLocation.x >= build.min.x
                && clickLocation.z >= build.min.z
                && clickLocation.x <= build.max.x
                && clickLocation.z <= build.max.z) {
              clickedBuilding = build;
              break;
            }
          }
        });
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(Player.getComponentType(), PlayerRef.getComponentType());
  }
}
