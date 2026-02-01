package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.PrefabUtil;
import java.util.Arrays;
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

          var placement = chunk.getComponent(i, Plugin.getPlaceType());
          JamSave save = world.getChunkStore().getStore().getResource(Plugin.getJamType());

          if (placement != null) {
            JamSave.BuildingType target = placement.building;
            var prefabBuffer =
                PrefabBufferUtil.getCached(
                    AssetModule.get()
                        .getAssetPack("org.echoesfrombeyond:Scrapvengers")
                        .getRoot()
                        .resolve("Server")
                        .resolve("Prefabs")
                        .resolve(target.getPrefabAsset()));

            PrefabUtil.paste(
                prefabBuffer, world, event.pos, Rotation.None, true, new FastRandom(), buffer);

            var res =
                Arrays.stream(JamSave.BuildingType.values())
                    .filter(bt -> bt.name().equalsIgnoreCase(target.name()))
                    .findFirst();
            if (res.isEmpty()) return;

            JamSave.Building building = new JamSave.Building();
            building.type = res.get();
            building.min =
                new Vector3i(
                    event.pos.x + prefabBuffer.getMinX(),
                    event.pos.y + prefabBuffer.getMinY(),
                    event.pos.z + prefabBuffer.getMinZ());
            building.max =
                new Vector3i(
                    event.pos.x + prefabBuffer.getMaxX(),
                    event.pos.y + prefabBuffer.getMaxY(),
                    event.pos.z + prefabBuffer.getMaxZ());

            save.buildings.add(building);
            return;
          }

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
          // TODO: interact with buildings otherwise

          if (clickedBuilding == null) {
            return;
          }

          System.out.println("I clicked on a building!");
        });
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(Player.getComponentType(), PlayerRef.getComponentType());
  }
}
