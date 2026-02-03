package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LoseUI extends InteractiveCustomUIPage<LoseUI.LoseData> {
  public LoseUI(PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, LoseData.CODEC);
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    commandBuilder.append("Lose.ui");
    String groupSelect = "#Container";

    commandBuilder.set(groupSelect + " #LoseTitle.Text", "L + ratio");

    commandBuilder.set(groupSelect + " #ButtonTitle.Text", ":(");

    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        groupSelect + " #LoseButton",
        EventData.of(LoseData.CLICKED_FIELD, String.valueOf(true)),
        false);
  }

  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, LoseData data) {
    // do something here
  }

  public static class LoseData {
    static final String CLICKED_FIELD = "Clicked";

    public static final BuilderCodec<LoseData> CODEC =
        BuilderCodec.builder(LoseData.class, LoseData::new)
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
