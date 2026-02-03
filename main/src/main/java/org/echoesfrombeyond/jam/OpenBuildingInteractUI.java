package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class OpenBuildingInteractUI {
  // this might be a stupid way of doing this
  // but we only want the interaction to succeed in specific circumstances...
  public static void openBuildingPage(
      @Nullable CommandBuffer<EntityStore> buffer,
      Ref<EntityStore> ref,
      JamSave.Building building) {
    if (buffer == null) return;

    var player = buffer.getComponent(ref, Player.getComponentType());
    var playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());
    var gs = buffer.getComponent(ref, Plugin.getStageType());

    if (player == null
        || playerRef == null
        || buffer.getComponent(ref, Plugin.getPlaceType()) != null
        || (gs != null && gs.isBattle)) return;

    var midpoint = building.min.clone().add(building.max).scale(0.5);
    Plugin.setBuildingFocusCameraPosition(playerRef, midpoint);

    player
        .getPageManager()
        .openCustomPage(ref, ref.getStore(), new BuildingInteractUI(playerRef, building));
  }
}
