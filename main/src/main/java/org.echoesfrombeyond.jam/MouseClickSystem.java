package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.MouseButtonType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MouseClickSystem extends EntityEventSystem<EntityStore, MouseClickEvent> {
  public MouseClickSystem() {
    super(MouseClickEvent.class);
  }

  @Override
  public void handle(
      int i,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> buffer,
      MouseClickEvent event) {
    if (event.type != MouseButtonType.Left) return;

    var player = chunk.getComponent(i, Player.getComponentType());
    var playerRef = chunk.getComponent(i, PlayerRef.getComponentType());
    assert player != null && playerRef != null;

    var ref = playerRef.getReference();
    if (ref == null) return;
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.of(Player.getComponentType(), PlayerRef.getComponentType());
  }
}
