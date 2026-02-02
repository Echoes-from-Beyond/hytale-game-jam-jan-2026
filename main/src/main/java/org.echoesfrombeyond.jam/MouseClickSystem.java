package org.echoesfrombeyond.jam;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
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
          Vector3i clickLocation = event.pos;
          var ref = chunk.getReferenceTo(i);

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

            Vector3i minBound =
                new Vector3i(
                    clickLocation.x + prefabBuffer.getMinX(),
                    clickLocation.y + prefabBuffer.getMinY(),
                    clickLocation.z + prefabBuffer.getMinZ());
            Vector3i maxBound =
                new Vector3i(
                    clickLocation.x + prefabBuffer.getMaxX(),
                    clickLocation.y + prefabBuffer.getMaxY(),
                    clickLocation.z + prefabBuffer.getMaxZ());

            boolean conflict = false;

            for (JamSave.Building build : save.buildings) {
              var building = new Bounds3i(build.min.clone(), build.max.clone());
              var candidate = new Bounds3i(minBound.clone(), maxBound.clone());

              building.min.setY(0);
              building.max.setY(1);

              candidate.min.setY(0);
              candidate.max.setY(1);

              if (building.intersects(candidate)) {
                conflict = true;
                break;
              }
            }

            if (conflict) {
              // TODO: warn the player about conflict
              System.out.println("Conflict with building placement");
              return;
            }

            PrefabUtil.paste(
                prefabBuffer, world, clickLocation, Rotation.None, true, new FastRandom(), buffer);

            var res =
                Arrays.stream(JamSave.BuildingType.values())
                    .filter(bt -> bt.name().equalsIgnoreCase(target.name()))
                    .findFirst();
            if (res.isEmpty()) return;

            JamSave.Building building = new JamSave.Building();
            building.type = res.get();
            building.min = minBound;
            building.max = maxBound;

            save.buildings.add(building);

            store.removeComponent(ref, Plugin.getPlaceType());
            return;
          }

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

          if (clickedBuilding == null) {
            return;
          }

          // TODO: interact with buildings otherwise
          System.out.println("I clicked on a building!");
          OpenBuildingInteractUI.openBuildingPage(buffer, ref);
        });
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(Player.getComponentType(), PlayerRef.getComponentType());
  }
}
