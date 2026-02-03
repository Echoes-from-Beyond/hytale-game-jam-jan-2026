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
public class BuildingInteractUI
    extends InteractiveCustomUIPage<BuildingInteractUI.BuildingInteractUIData> {
  // I made this interactive in case we have time to do building upgrades

  private final JamSave.Building building;

  public BuildingInteractUI(PlayerRef playerRef, JamSave.Building building) {
    super(
        playerRef,
        CustomPageLifetime.CanDismissOrCloseThroughInteraction,
        BuildingInteractUIData.CODEC);

    this.building = building;
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    commandBuilder.append("Building_Interact.ui");

    String selector = "#TestGroup";
    setFromBuilding(building, commandBuilder);

    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector + " #AssignColonist",
        EventData.of(BuildingInteractUIData.BUILDING_NAME, "ignored"),
        false);
  }

  private void setFromBuilding(JamSave.Building building, UICommandBuilder commandBuilder) {
    String selector = "#TestGroup";
    commandBuilder.set(selector + " #TestTitle.Text", building.type.prettyName);

    if (!building.type.needsColonist) {
      commandBuilder.set(selector + " #AssignColonist.Visible", false);
    } else if (building.hasColonist()) {
      commandBuilder.set(selector + " #AssignColonist #Lab.Text", "Remove Colonist");
    } else {
      commandBuilder.set(selector + " #AssignColonist #Lab.Text", "Assign Colonist");
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, BuildingInteractUIData data) {
    if (!building.type.needsColonist) return;

    var world = store.getExternalData().getWorld();
    world.execute(
        () -> {
          if (building.hasColonist()) building.removeColonist();
          else building.assignColonist();

          UICommandBuilder builder = new UICommandBuilder();
          setFromBuilding(building, builder);

          this.sendUpdate(builder);
        });
  }

  public static class BuildingInteractUIData {
    // placeholder
    static final String BUILDING_NAME = "BuildingName";

    public static final BuilderCodec<BuildingInteractUIData> CODEC =
        BuilderCodec.builder(BuildingInteractUIData.class, BuildingInteractUIData::new)
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
