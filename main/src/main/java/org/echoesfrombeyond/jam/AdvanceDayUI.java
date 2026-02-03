package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import org.echoesfrombeyond.jam.data.DataContainer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AdvanceDayUI extends InteractiveCustomUIPage<AdvanceDayUI.AdvanceDayData> {
  public AdvanceDayUI(PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, AdvanceDayData.CODEC);
  }

  @Override
  public void build(
      Ref<EntityStore> ref,
      UICommandBuilder commandBuilder,
      UIEventBuilder eventBuilder,
      Store<EntityStore> store) {
    commandBuilder.append("Advance_Day.ui");
    String groupSelect = "#Container";

    commandBuilder.set(groupSelect + " #AdvanceTitle.Text", "Advance day?");

    // les FREAKING go
    commandBuilder.set(groupSelect + " #ButtonTitle.Text", "LET'S GO");

    var jam =
        store
            .getExternalData()
            .getWorld()
            .getChunkStore()
            .getStore()
            .getResource(Plugin.getJamType());

    if (jam.buildings.stream()
        .noneMatch(building -> building.type == JamSave.BuildingType.Turret)) {
      commandBuilder.set(
          groupSelect + " #WarningDiv #WarningMessage.Text",
          "You have not built any turrets!\nYour radio tower is undefended!");
    } else if (jam.buildings.stream().anyMatch(building -> building.type.needsColonist)
        && jam.buildings.stream()
            .noneMatch(building -> building.type.needsColonist && building.hasColonist())) {
      commandBuilder.set(
          groupSelect + " #WarningDiv #WarningMessage.Text",
          "You have not assigned any colonists!\nAdvancing the day won't produce resources.");
    }

    // fyi you can't use anything other than string because of this method
    eventBuilder.addEventBinding(
        CustomUIEventBindingType.Activating,
        groupSelect + " #AdvanceButton",
        EventData.of(AdvanceDayData.CLICKED_FIELD, String.valueOf(true)),
        false);
  }

  @Override
  public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, AdvanceDayData data) {
    System.out.println("Day advanced");

    var playerRefRef = this.playerRef.getReference();
    Player player =
        playerRefRef != null ? store.getComponent(playerRefRef, Player.getComponentType()) : null;

    if (player == null || store.getComponent(ref, Plugin.getPlaceType()) != null) {
      return;
    }

    player.getPageManager().setPage(ref, store, Page.None);

    var world = store.getExternalData().getWorld();
    world.execute(
        () -> {
          JamSave save = world.getChunkStore().getStore().getResource(Plugin.getJamType());
          var gs = store.getComponent(ref, Plugin.getStageType());
          if (gs != null) gs.isBattle = true;

          ArrayList<DataContainer> placeables = DataContainer.placeableBuildings();
          outerLoop:
          for (JamSave.Building b : save.buildings) {
            for (DataContainer p : placeables) {
              if (p.buildingType != b.type) {
                continue;
              }

              switch (p.resourceGenerated) {
                case ("scrap"):
                  save.scrap +=
                      p.upgrades.getFirst().resourcesGeneratedPerColonist * b.resourceMul();
                  break;
                case ("water"):
                  save.water +=
                      p.upgrades.getFirst().resourcesGeneratedPerColonist * b.resourceMul();
                  break;
                case ("food"):
                  save.food +=
                      p.upgrades.getFirst().resourcesGeneratedPerColonist * b.resourceMul();
                  break;
                default:
                  continue outerLoop;
              }
            }
          }
          var assignedColonists =
              (int) save.buildings.stream().filter(JamSave.Building::hasColonist).count();
          var idleColonists = save.colonists;

          var totalColonists = assignedColonists + idleColonists;

          save.food -= totalColonists;
          save.water -= totalColonists;

          var deficit = Math.min(save.food, save.water);
          if (deficit < 0) {
            var colonistsToKill = Math.abs(deficit);

            player.sendMessage(
                Message.raw(
                    "You lost "
                        + Math.min(colonistsToKill, totalColonists)
                        + " colonist(s) to lack of "
                        + (save.food < save.water ? "food" : "water")
                        + "! Build wells to produce water and farms to produce food."));

            save.colonists = Math.max(save.colonists - colonistsToKill, 0);
            player.sendMessage(
                Message.raw(
                    " -- Lost " + Math.min(colonistsToKill, idleColonists) + " idle colonist(s)"));

            if (colonistsToKill >= idleColonists) {
              var colonistsToKillAtWork = colonistsToKill - idleColonists;

              if (assignedColonists - colonistsToKillAtWork <= 0) {
                player.sendMessage(Message.raw("You lost all of your colonists!"));
                save.towerHealth = 0;
              } else {
                int removed = 0;

                for (var building : save.buildings) {
                  if (building.hasColonist()) {
                    building.removeColonist();
                    if (++removed >= colonistsToKillAtWork) break;
                  }
                }

                player.sendMessage(Message.raw(" -- Lost " + removed + " assigned colonists"));
              }
            }
          }

          if (save.food < 0) save.food = 0;
          if (save.water < 0) save.water = 0;

          if (ref.isValid()) store.invoke(ref, new HudUpdateSystem.Event());
        });
  }

  public static class AdvanceDayData {
    static final String CLICKED_FIELD = "Clicked";

    public static final BuilderCodec<AdvanceDayData> CODEC =
        BuilderCodec.builder(AdvanceDayData.class, AdvanceDayData::new)
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
