package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class EnemyComponent implements Component<EntityStore> {
  public @Nullable Ref<EntityStore> player;
  public float timeSpentAlive;

  public EnemyComponent() {}

  public EnemyComponent(EnemyComponent other) {
    this.player = other.player;
    this.timeSpentAlive = other.timeSpentAlive;
  }

  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  public Component<EntityStore> clone() {
    return new EnemyComponent(this);
  }
}
