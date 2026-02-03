package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.echoesfrombeyond.jam.data.DataContainer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BuildingInteractUI
    extends InteractiveCustomUIPage<BuildingInteractUI.BuildingInteractUIData> {
  // I made this interactive in case we have time to do building upgrades

  private final JamSave.Building building;
  private final DataContainer[] buildingData = DataContainer.allUpgrades;
  @Nullable private DataContainer matchingData = null;

  public BuildingInteractUI(PlayerRef playerRef, JamSave.Building building) {
    super(
        playerRef,
        CustomPageLifetime.CanDismissOrCloseThroughInteraction,
        BuildingInteractUIData.CODEC);

    this.building = building;
    for (DataContainer d : buildingData) {
      if (d.buildingType == building.type) {
        matchingData = d;
        break;
      }
    }
  }

  @Override
  public void onDismiss(Ref<EntityStore> ref, Store<EntityStore> store) {
    var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    if (playerRef == null) return;

    Plugin.setDefaultCameraPosition(playerRef);
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

    if (matchingData != null) {
      commandBuilder.set("#BuildingLore.Text", matchingData.description);

      if (matchingData.buildingType == JamSave.BuildingType.RadioTower) {
        var radioUpgrade = matchingData.upgrades.get(1).requirements.getFirst();
        commandBuilder.set("#Ingr.Text", radioUpgrade.resourceType + ": " + radioUpgrade.amount);
        commandBuilder.set("#Upgr.Text", "UPGRADE\n100 SCRAP");
      } else {
        commandBuilder.set("#UpgradeButton.Visible", false);
      }
    }

    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector + " #UpgradeButton",
        EventData.of(BuildingInteractUIData.CLICKED_COLONIST, "false")
            .append(BuildingInteractUIData.CLICKED_UPGRADE, "true"),
        false);

    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector + " #AssignColonist",
        EventData.of(BuildingInteractUIData.CLICKED_COLONIST, "true")
            .append(BuildingInteractUIData.CLICKED_UPGRADE, "false"),
        false);
  }

  private void setFromBuilding(JamSave.Building building, UICommandBuilder commandBuilder) {
    String selector = "#TestGroup";
    commandBuilder.set(selector + " #TestTitle.Text", building.type.prettyName);

    if (!building.type.needsColonist) {
      commandBuilder.set("#AssignColonist.Disabled", true);
      commandBuilder.set("#AssignColonist #Lab.Text", "Assign Colonist");
    } else if (building.hasColonist()) {
      commandBuilder.set("#AssignColonist #Lab.Text", "Remove Colonist");
    } else {
      commandBuilder.set("#AssignColonist #Lab.Text", "Assign Colonist");
    }
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, BuildingInteractUIData data) {
    if (!building.type.needsColonist && building.type != JamSave.BuildingType.RadioTower) return;

    var world = store.getExternalData().getWorld();
    world.execute(
        () -> {
          var jam = world.getChunkStore().getStore().getResource(Plugin.getJamType());

          if (Boolean.parseBoolean(data.clickedUpgrade)) {
            if (matchingData == null
                || matchingData.buildingType != JamSave.BuildingType.RadioTower) {
              return;
            }

            if (jam.scrap >= matchingData.upgrades.get(1).requirements.getFirst().amount) {
              if (!ref.isValid()) return;

              var gs = store.getComponent(ref, Plugin.getStageType());
              if (gs == null) return;

              gs.won = true;
            }
            return;
          }

          if (building.type == JamSave.BuildingType.RadioTower) return;

          if (building.hasColonist()) {
            building.removeColonist();
            jam.colonists++;
          } else {
            if (jam.colonists <= 0) {
              if (ref.isValid()) {
                var pr = store.getComponent(ref, PlayerRef.getComponentType());
                if (pr != null)
                  pr.sendMessage(
                      Message.raw(
                          "You have no more colonists! Build housing to gain more, or reallocate"
                              + " some from buildings they're already assigned to."));
              }

              return;
            }

            building.assignColonist();
            jam.colonists--;
          }

          UICommandBuilder builder = new UICommandBuilder();
          setFromBuilding(building, builder);

          this.sendUpdate(builder);
          if (ref.isValid()) {
            world.getEntityStore().getStore().invoke(ref, new HudUpdateSystem.Event());
          }
        });
  }

  public static class BuildingInteractUIData {
    static final String CLICKED_UPGRADE = "ClickedUpgrade";
    static final String CLICKED_COLONIST = "ClickedColonist";

    public static final BuilderCodec<BuildingInteractUIData> CODEC =
        BuilderCodec.builder(BuildingInteractUIData.class, BuildingInteractUIData::new)
            .append(
                new KeyedCodec<>(CLICKED_UPGRADE, BuilderCodec.STRING),
                (data, s) -> data.clickedUpgrade = s,
                (data) -> data.clickedUpgrade)
            .add()
            .append(
                new KeyedCodec<>(CLICKED_COLONIST, BuilderCodec.STRING),
                (data, s) -> data.clickedColonist = s,
                (data) -> data.clickedColonist)
            .add()
            .build();

    private String clickedUpgrade = "false";
    private String clickedColonist = "false";

    public String getClickedUpgrade() {
      return clickedUpgrade;
    }

    public String getClickedColonist() {
      return clickedColonist;
    }
  }
}
