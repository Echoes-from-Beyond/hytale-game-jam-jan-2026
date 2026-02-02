package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GameStageComponent implements Component<EntityStore> {
  public int day;

  /** whether we're in the combat phase */
  public boolean isBattle;

  public GameStageComponent() {}

  public GameStageComponent(GameStageComponent other) {
    this.day = other.day;
    this.isBattle = other.isBattle;
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Component<EntityStore> clone() {
    return new GameStageComponent(this);
  }
}
