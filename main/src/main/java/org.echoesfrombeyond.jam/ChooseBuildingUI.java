package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ChooseBuildingUI
    extends InteractiveCustomUIPage<ChooseBuildingUI.ChooseBuildingUIData> {
  private final JamSave.BuildingType[] BUILDINGS = JamSave.BuildingType.values();

  public ChooseBuildingUI(PlayerRef playerRef) {
    super(
        playerRef,
        CustomPageLifetime.CanDismissOrCloseThroughInteraction,
        ChooseBuildingUIData.CODEC);
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    commandBuilder.append("Choose_Building.ui");

    for (int i = 0; i < BUILDINGS.length; i++) {
      // why are string templates not released yet smh oracle
      String select = "#BuildingGroup[" + i + "]";

      // why can't you generate a list in a single UI file lmao
      commandBuilder.append("#BuildingGroup", "Choose_Building_Fragment.ui");
      commandBuilder.set(select + " #BuildingName.Text", BUILDINGS[i].name());

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          select + " #BuildingSelector",
          EventData.of("BuildingIndex", String.valueOf(i)),
          false);
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, ChooseBuildingUI.ChooseBuildingUIData data) {
    System.out.println("You clicked on " + data.getBuildingIndex());

    var playerRefRef = this.playerRef.getReference();
    Player player = playerRefRef != null ? store.getComponent(playerRefRef, Player.getComponentType()) : null;

    if(player == null) {
        return;
    }

    player.getPageManager().setPage(ref, store, Page.None);
  }

  public static class ChooseBuildingUIData {
    // BarterPage uses String instead of an integer so I'm just going to assume that this is a
    // limitation with the client
    // doesn't this use JSONs? those support numbers lol
    static final String BUILDING_INDEX = "BuildingIndex";

    public static final BuilderCodec<ChooseBuildingUIData> CODEC =
        BuilderCodec.builder(ChooseBuildingUIData.class, ChooseBuildingUIData::new)
            .append(
                new KeyedCodec<>("BuildingIndex", BuilderCodec.STRING),
                (data, s) -> data.buildingIndex = Integer.parseInt((String) s),
                (data) -> String.valueOf(data.buildingIndex))
            .add()
            .build();

    private int buildingIndex;

    public int getBuildingIndex() {
      return buildingIndex;
    }
  }
}
