package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GameStageComponent implements Component<EntityStore> {
  /** whether we're in the combat phase */
  public boolean isBattle;

  public boolean won;
  public float battleTime;
  public int spawned;
  public int killed;

  public GameStageComponent() {}

  public GameStageComponent(GameStageComponent other) {
    this.isBattle = other.isBattle;
    this.won = other.won;
    this.battleTime = other.battleTime;
    this.spawned = other.spawned;
    this.killed = other.killed;
  }

  public void reset() {
    isBattle = false;
    won = false;
    battleTime = 0;
    spawned = 0;
    killed = 0;
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Component<EntityStore> clone() {
    return new GameStageComponent(this);
  }
}
