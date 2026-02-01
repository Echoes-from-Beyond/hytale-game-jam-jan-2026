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
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ChooseBuildingUI
    extends InteractiveCustomUIPage<ChooseBuildingUI.ChooseBuildingUIData> {
  private final ArrayList<JamSave.BuildingType> BUILDINGS = addPermittedBuildings();

  private ArrayList<JamSave.BuildingType> addPermittedBuildings() {
    ArrayList<JamSave.BuildingType> builds =
        new ArrayList<>(Arrays.asList(JamSave.BuildingType.values()));
    builds.remove(JamSave.BuildingType.RadioTower);
    builds.remove(JamSave.BuildingType.CommandTent);
    builds.remove(JamSave.BuildingType.None);

    return builds;
  }

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
      JamSave.BuildingType build = BUILDINGS.get(i);

      // why can't you generate a list in a single UI file lmao
      commandBuilder.append("#BuildingGroup", "Choose_Building_Fragment.ui");
      commandBuilder.set(select + " #BuildingName.Text", build.name());

      eventBuilder.addEventBinding(
          CustomUIEventBindingType.Activating,
          select + " #BuildingSelector",
          EventData.of(ChooseBuildingUIData.BUILDING_NAME, build.name()),
          false);
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, ChooseBuildingUI.ChooseBuildingUIData data) {
    System.out.println("You clicked on " + data.getBuildingName());

    var playerRefRef = this.playerRef.getReference();
    Player player =
        playerRefRef != null ? store.getComponent(playerRefRef, Player.getComponentType()) : null;

    if (player == null) {
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
          store.addComponent(ref, Plugin.getPlaceType(), preview);
        });
  }

  public static class ChooseBuildingUIData {
    // BarterPage uses String instead of an integer so I'm just going to assume that this is a
    // limitation with the client
    // doesn't this use JSONs? those support numbers lol
    static final String BUILDING_NAME = "BuildingName";

    public static final BuilderCodec<ChooseBuildingUIData> CODEC =
        BuilderCodec.builder(ChooseBuildingUIData.class, ChooseBuildingUIData::new)
            .append(
                new KeyedCodec<>(BUILDING_NAME, BuilderCodec.STRING),
                (data, s) -> data.buildingName = s,
                (data) -> data.buildingName)
            .add()
            .build();

    private String buildingName = "";

    public String getBuildingName() {
      return buildingName;
    }
  }
}
