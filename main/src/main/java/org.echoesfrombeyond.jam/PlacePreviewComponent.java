package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PlacePreviewComponent implements Component<EntityStore> {
  public boolean dirty;

  /** The building that will be placed. */
  public JamSave.BuildingType building;

  /** Ignore these, they're only used by the preview system. */
  public @Nullable Vector3i oldMin;
  public @Nullable Vector3i oldMax;

  /** Block the cursor is hovering over. */
  public Vector3i cursorHoverPos;

  public PlacePreviewComponent() {
    this.building = JamSave.BuildingType.None;
    this.cursorHoverPos = new Vector3i(0, 0, 0);
  }

  public PlacePreviewComponent(PlacePreviewComponent other) {
    this.dirty = other.dirty;
    this.building = other.building;
    this.oldMin = other.oldMin == null ? null : other.oldMin.clone();
    this.oldMax = other.oldMax == null ? null : other.oldMax.clone();
    this.cursorHoverPos = other.cursorHoverPos.clone();
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Component<EntityStore> clone() {
    return new PlacePreviewComponent(this);
  }
}
