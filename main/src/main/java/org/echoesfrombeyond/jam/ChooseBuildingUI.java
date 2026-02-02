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
import java.util.Arrays;
import org.echoesfrombeyond.jam.data.DataContainer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ChooseBuildingUI
    extends InteractiveCustomUIPage<ChooseBuildingUI.ChooseBuildingUIData> {
  private final ArrayList<DataContainer> BUILDINGS = DataContainer.placeableBuildings();

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

    for (int i = 0; i < BUILDINGS.size(); i++) {
      // why are string templates not released yet smh oracle
      String select = "#BuildingGroup[" + i + "]";
      DataContainer targetBuild = BUILDINGS.get(i);
      JamSave.BuildingType build = targetBuild.buildingType;

      // why can't you generate a list in a single UI file lmao
      commandBuilder.append("#BuildingGroup", "Choose_Building_Fragment.ui");
      commandBuilder.set(select + " #BuildingName.Text", build.name());

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          select + " #BuildingSelector",
          EventData.of(ChooseBuildingUIData.BUILDING_NAME, build.name())
              // ONLY the first level can be found in the shop
              .append(
                  ChooseBuildingUIData.RESOURCE_TYPE,
                  targetBuild.upgrades.getFirst().requirements.getFirst().resourceType)
              .append(
                  ChooseBuildingUIData.RESOURCE_AMOUNT,
                  String.valueOf(targetBuild.upgrades.getFirst().requirements.getFirst().amount)),
          false);
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, ChooseBuildingUI.ChooseBuildingUIData data) {
    var playerRefRef = this.playerRef.getReference();
    Player player =
        playerRefRef != null ? store.getComponent(playerRefRef, Player.getComponentType()) : null;

    if (player == null || store.getComponent(ref, Plugin.getPlaceType()) != null) {
      return;
    }

    player.getPageManager().setPage(ref, store, Page.None);

    var res =
        Arrays.stream(JamSave.BuildingType.values())
            .filter(bt -> bt.name().equalsIgnoreCase(data.getBuildingName()))
            .findFirst();
    if (res.isEmpty()) return;

    var world = store.getExternalData().getWorld();
    world.execute(
        () -> {
          var preview = new PlacePreviewComponent();
          preview.building = res.get();
          preview.resourceType = data.getResourceType();
          preview.amountSpent = data.getResourceAmount();
          store.addComponent(ref, Plugin.getPlaceType(), preview);
        });
  }

  public static class ChooseBuildingUIData {
    // BarterPage uses String instead of an integer so I'm just going to assume that this is a
    // limitation with the client
    // doesn't this use JSONs? those support numbers lol
    static final String BUILDING_NAME = "BuildingName";
    static final String RESOURCE_TYPE = "ResourceType";
    static final String RESOURCE_AMOUNT = "ResourceAmount";

    public static final BuilderCodec<ChooseBuildingUIData> CODEC =
        BuilderCodec.builder(ChooseBuildingUIData.class, ChooseBuildingUIData::new)
            .append(
                new KeyedCodec<>(BUILDING_NAME, BuilderCodec.STRING),
                (data, s) -> data.buildingName = s,
                (data) -> data.buildingName)
            .add()
            .append(
                new KeyedCodec<>(RESOURCE_TYPE, BuilderCodec.STRING),
                (data, s) -> data.resourceType = s,
                (data) -> data.resourceType)
            .add()
            .append(
                new KeyedCodec<>(RESOURCE_AMOUNT, BuilderCodec.STRING),
                (data, s) -> data.resourceAmount = Integer.parseInt(s),
                (data) -> String.valueOf(data.resourceAmount))
            .add()
            .build();

    private String buildingName = "";
    private String resourceType = "";
    private int resourceAmount;

    public String getBuildingName() {
      return buildingName;
    }

    public String getResourceType() {
      return resourceType;
    }

    public int getResourceAmount() {
      return resourceAmount;
    }
  }
}
