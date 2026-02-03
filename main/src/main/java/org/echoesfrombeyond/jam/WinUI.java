package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WinUI extends InteractiveCustomUIPage<WinUI.WinData> {
  public WinUI(PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CantClose, WinData.CODEC);
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    commandBuilder.append("Win.ui");
    String groupSelect = "#Container";

    commandBuilder.set(groupSelect + " #WinTitle.Text", "You Win!");
  }

  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, WinData data) {
    // do something here
  }

  public static class WinData {
    static final String CLICKED_FIELD = "Clicked";

    public static final BuilderCodec<WinData> CODEC =
        BuilderCodec.builder(WinData.class, WinData::new)
            .append(
                new KeyedCodec<>(CLICKED_FIELD, BuilderCodec.STRING),
                (data, s) -> data.clicked = s,
                (data) -> data.clicked)
            .add()
            .build();

    private String clicked = "false";

    public boolean getClicked() {
      return Boolean.getBoolean(clicked);
    }
  }
}
