package org.echoesfrombeyond.jam;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SimHud extends CustomUIHud {
  public SimHud(PlayerRef playerRef) {
    super(playerRef);
  }

  @Override
  protected void build(UICommandBuilder builder) {
    builder.append("Sim_Hud.ui");
  }
}
