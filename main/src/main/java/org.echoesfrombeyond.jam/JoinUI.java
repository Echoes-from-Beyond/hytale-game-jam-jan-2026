package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

public class JoinUI extends CustomUIPage {
  public JoinUI(@NotNull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
  }

  @Override
  public void build(
          @NotNull Ref<EntityStore> ref,
          @NotNull UICommandBuilder commandBuilder,
          @NotNull UIEventBuilder eventBuilder,
          @NotNull Store<EntityStore> store) {
    commandBuilder.append("Join_Popup.ui");
  }
}
