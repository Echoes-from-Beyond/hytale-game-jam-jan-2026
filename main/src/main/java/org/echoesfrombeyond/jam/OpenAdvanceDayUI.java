package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class OpenAdvanceDayUI extends SimpleInstantInteraction {
  public static final BuilderCodec<OpenAdvanceDayUI> CODEC =
      BuilderCodec.builder(
              OpenAdvanceDayUI.class, OpenAdvanceDayUI::new, SimpleInstantInteraction.CODEC)
          .build();

  @Override
  protected void firstRun(
      InteractionType type, InteractionContext context, CooldownHandler handler) {
    var buffer = context.getCommandBuffer();
    if (buffer == null) return;

    var ref = context.getEntity();
    var player = buffer.getComponent(ref, Player.getComponentType());
    var playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());
    var gs = buffer.getComponent(ref, Plugin.getStageType());
    if (player == null
        || playerRef == null
        || buffer.getComponent(ref, Plugin.getPlaceType()) != null
        || (gs != null && gs.isBattle)) return;

    player.getPageManager().openCustomPage(ref, ref.getStore(), new AdvanceDayUI(playerRef));
  }
}
