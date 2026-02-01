package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RemovePreviewSystem extends RefChangeSystem<EntityStore, PlacePreviewComponent> {
  @Override
  public ComponentType<EntityStore, PlacePreviewComponent> componentType() {
    return Plugin.getPlaceType();
  }

  @Override
  public void onComponentAdded(
      Ref<EntityStore> var1,
      PlacePreviewComponent var2,
      Store<EntityStore> var3,
      CommandBuffer<EntityStore> var4) {}

  @Override
  public void onComponentSet(
      Ref<EntityStore> var1,
      @Nullable PlacePreviewComponent var2,
      PlacePreviewComponent var3,
      Store<EntityStore> var4,
      CommandBuffer<EntityStore> var5) {}

  @Override
  public void onComponentRemoved(
      Ref<EntityStore> ref,
      PlacePreviewComponent preview,
      Store<EntityStore> store,
      CommandBuffer<EntityStore> buffer) {
    var world = store.getExternalData().getWorld();
    var min = preview.oldMin == null ? null : preview.oldMin.clone();
    var max = preview.oldMax == null ? null : preview.oldMax.clone();
    if (min == null || max == null) return;

    world.execute(() -> PlacePreviewSystem.remove(world, min, max));
  }

  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(Plugin.getPlaceType());
  }

  @Override
  public Set<Dependency<EntityStore>> getDependencies() {
    return Set.of(new SystemDependency<>(Order.AFTER, PlacePreviewSystem.class));
  }
}
