package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

public class JoinUI extends CustomUIPage {
  public JoinUI(@NotNull PlayerRef playerRef) {
    super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
  }

  @Override
  public void build(
      @NotNull Ref<EntityStore> ref,
      @NotNull UICommandBuilder commandBuilder,
      @NotNull UIEventBuilder eventBuilder,
      @NotNull Store<EntityStore> store) {
    String foreword =
        """
        The fallen magitech metropolis of Crucible looms over you.
        Your first foray into the city ended with your entire squad decimated by its
        fiery automatons, somehow still functional after centuries of neglect.
        Regroup on the outskirts and fend off its overactive defense systems...
        maybe that radio tower will help if you manage to rewire it?
        """;

    // sigh, we need noesis so bad
    String joinText =
        """
        Scrapvengers is a simple tech demo of a colony simulator made in Hytale.
        Manage your resources and defend the heart of your base (the radio tower)
        against intruders. Repair the tower to win; neglect it too much and the
        consequences are obvious.
        This mod is played almost entirely with your mouse. Use the menus in your
        hotbar to place buildings and to advance the day (i.e. gather your
        resources). Be careful, because your enemies strike in meantime.

        NOTE: in case enemies somehow get stuck, they will automatically die after
        60 seconds, to avoid softlocking.

        Avoid loading another mod alongside this one. Scrapvengers is meant to be
        standalone - it changes enough of the game that things are bound to go
        wrong otherwise.
        We hope you enjoy our dumb little experiment :)
        - Cobrinthine & Vegetal

        (Most menus can be closed with ESC)\
        """;
    commandBuilder.append("Join_Popup.ui");
    commandBuilder.set("#Foreword.Text", foreword);
    commandBuilder.set("#JoinText.Text", joinText);
  }
}
