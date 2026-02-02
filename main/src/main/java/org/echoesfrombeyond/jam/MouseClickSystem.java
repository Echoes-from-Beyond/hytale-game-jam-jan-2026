package org.echoesfrombeyond.jam;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.server.core.Message;
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
  public static int BUILDING_PLACE_GAP = 3;

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
    var ref = chunk.getReferenceTo(i);
    var world = buffer.getExternalData().getWorld();

    world.execute(
        () -> {
          if (!ref.isValid()) return;

          if (event.type == MouseButtonType.Right) {
            // dismiss the current selection with rclick
            buffer.tryRemoveComponent(ref, Plugin.getPlaceType());
            return;
          }

          var entityStore = world.getEntityStore().getStore();
          var placement = entityStore.getComponent(ref, Plugin.getPlaceType());
          var playerRef = entityStore.getComponent(ref, PlayerRef.getComponentType());

          JamSave save = world.getChunkStore().getStore().getResource(Plugin.getJamType());
          Vector3i clickLocation = event.pos;

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
              var building =
                  new Bounds3i(
                      build
                          .min
                          .clone()
                          .subtract(BUILDING_PLACE_GAP, BUILDING_PLACE_GAP, BUILDING_PLACE_GAP),
                      build
                          .max
                          .clone()
                          .add(BUILDING_PLACE_GAP, BUILDING_PLACE_GAP, BUILDING_PLACE_GAP));
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
              if (playerRef != null)
                playerRef.sendMessage(Message.raw("Too close to another building!"));
              return;
            }

            // these should be equal length; if not, there is a bug either with setting defaults for
            // data classes
            // or how the event is registered for choosing buildings
            String[] splitTypes = placement.resourceTypes.split(",");
            String[] splitAmounts = placement.amountsSpent.split(",");

            // TODO: feedback to player in case of lacking resources
            for (int j = 0; j < splitTypes.length; j++) {
              int value = Integer.parseInt(splitAmounts[j]);
              switch (splitTypes[j]) {
                case "scrap":
                  if (save.scrap < value) {
                    if (playerRef != null) playerRef.sendMessage(Message.raw("Not enough scrap!"));
                    return;
                  }

                  save.scrap -= value;
                  break;
                case "food":
                  if (save.food < value) {
                    if (playerRef != null) playerRef.sendMessage(Message.raw("Not enough food!"));
                    return;
                  }

                  save.food -= value;
                  break;
                case "water":
                  if (save.water < value) {
                    if (playerRef != null) playerRef.sendMessage(Message.raw("Not enough water!"));
                    return;
                  }

                  save.water -= value;
                  break;
                default:
                  // just do nothing for badly configured previews
                  return;
              }
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

            entityStore.removeComponent(ref, Plugin.getPlaceType());
            entityStore.invoke(ref, new HudUpdateSystem.Event());
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
          OpenBuildingInteractUI.openBuildingPage(buffer, ref);
        });
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(Player.getComponentType(), PlayerRef.getComponentType());
  }
}
