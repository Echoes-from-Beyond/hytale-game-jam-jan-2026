package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class OpenBuildingInteractUI {
  // this might be a stupid way of doing this
  // but we only want the interaction to succeed in specific circumstances...
  public static void openBuildingPage(
      CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref, JamSave.Building building) {
    if (buffer == null) return;

    var player = buffer.getComponent(ref, Player.getComponentType());
    var playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());

    if (player == null
        || playerRef == null
        || buffer.getComponent(ref, Plugin.getPlaceType()) != null) return;

    player
        .getPageManager()
        .openCustomPage(ref, ref.getStore(), new BuildingInteractUI(playerRef, building));
  }
}
