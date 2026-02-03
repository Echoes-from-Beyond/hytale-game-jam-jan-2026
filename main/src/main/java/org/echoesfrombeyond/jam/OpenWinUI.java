package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class OpenWinUI {
  public static void openWinPopup(Ref<EntityStore> ref, PlayerRef playerRef) {

    var player = ref.getStore().getComponent(ref, Player.getComponentType());

    if (player == null
        || playerRef == null
        || ref.getStore().getComponent(ref, Plugin.getPlaceType()) != null) return;

    player.getPageManager().openCustomPage(ref, ref.getStore(), new WinUI(playerRef));
  }
}
