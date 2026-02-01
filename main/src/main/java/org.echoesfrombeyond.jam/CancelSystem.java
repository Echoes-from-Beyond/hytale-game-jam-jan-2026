package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class CancelSystem<T extends CancellableEcsEvent> extends EntityEventSystem<EntityStore, T> {
  public CancelSystem(Class<T> cls) {
    super(cls);
  }

  @Override
  public void handle(
      int i,
      ArchetypeChunk<EntityStore> chunk,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> buffer,
      T event) {
    event.setCancelled(true);
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Archetype.empty();
  }
}
