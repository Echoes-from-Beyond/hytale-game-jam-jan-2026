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

  public BuildingInteractUI(PlayerRef playerRef) {
    super(
        playerRef,
        CustomPageLifetime.CanDismissOrCloseThroughInteraction,
        BuildingInteractUIData.CODEC);
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    commandBuilder.append("Building_Interact.ui");
    String selector = "#TestGroup";
    commandBuilder.set(selector + " #TestTitle.Text", "are we kweeback?");

    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        selector + " #Ignore",
        EventData.of(BuildingInteractUIData.BUILDING_NAME, "placeholder"),
        false);
  }

  @Override
  public void handleDataEvent(
      Ref<EntityStore> ref, Store<EntityStore> store, BuildingInteractUIData data) {}

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
