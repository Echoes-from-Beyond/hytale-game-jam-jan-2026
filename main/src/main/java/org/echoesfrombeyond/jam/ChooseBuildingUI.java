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
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.echoesfrombeyond.jam.data.DataContainer;
import org.echoesfrombeyond.jam.data.UpgradeRequirement;
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
      commandBuilder.set(select + " #BuildingName.Text", build.prettyName);

      List<UpgradeRequirement> ur = targetBuild.upgrades.getFirst().requirements;
      Pair<String, String> compactedUr = compactRequirements(ur);

      for (int j = 0; j < ur.size(); j++) {
        String selectInner = select + " #Requirement[" + j + "]";
        commandBuilder.append(select + " #Requirement", "Choose_Building_Requirement.ui");
        commandBuilder.set(
            selectInner + " #RequirementLabel.Text",
            ur.get(j).resourceType + ": " + ur.get(j).amount);
      }

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          select + " #BuildingSelector",
          EventData.of(ChooseBuildingUIData.BUILDING_NAME, build.name())
              // ONLY the first level can be found in the shop
              .append(ChooseBuildingUIData.RESOURCE_TYPES, compactedUr.first())
              .append(ChooseBuildingUIData.RESOURCE_AMOUNTS, compactedUr.second()),
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
          preview.resourceTypes = data.getResourceTypes();
          preview.amountsSpent = data.getResourceAmounts();
          store.addComponent(ref, Plugin.getPlaceType(), preview);
        });
  }

  private Pair<String, String> compactRequirements(List<UpgradeRequirement> reqs) {
    StringBuilder comps = new StringBuilder();
    StringBuilder amts = new StringBuilder();
    for (UpgradeRequirement req : reqs) {
      comps.append(req.resourceType).append(",");
      amts.append(req.amount).append(",");
    }

    return Pair.of(comps.toString(), amts.toString());
  }

  public static class ChooseBuildingUIData {
    // BarterPage uses String instead of an integer so I'm just going to assume that this is a
    // limitation with the client
    // doesn't this use JSONs? those support numbers lol
    static final String BUILDING_NAME = "BuildingName";
    static final String RESOURCE_TYPES = "ResourceType";
    static final String RESOURCE_AMOUNTS = "ResourceAmount";

    public static final BuilderCodec<ChooseBuildingUIData> CODEC =
        BuilderCodec.builder(ChooseBuildingUIData.class, ChooseBuildingUIData::new)
            .append(
                new KeyedCodec<>(BUILDING_NAME, BuilderCodec.STRING),
                (data, s) -> data.buildingName = s,
                (data) -> data.buildingName)
            .add()
            .append(
                new KeyedCodec<>(RESOURCE_TYPES, BuilderCodec.STRING),
                (data, s) -> data.resourceTypes = s,
                (data) -> data.resourceTypes)
            .add()
            .append(
                new KeyedCodec<>(RESOURCE_AMOUNTS, BuilderCodec.STRING),
                (data, s) -> data.resourceAmounts = s,
                (data) -> data.resourceAmounts)
            .add()
            .build();

    private String buildingName = "";
    private String resourceTypes = "";
    private String resourceAmounts = "";

    public String getBuildingName() {
      return buildingName;
    }

    public String getResourceTypes() {
      return resourceTypes;
    }

    public String getResourceAmounts() {
      return resourceAmounts;
    }
  }
}
