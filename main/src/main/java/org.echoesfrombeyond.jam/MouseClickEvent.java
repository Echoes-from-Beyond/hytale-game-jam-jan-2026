package org.echoesfrombeyond.jam;

import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.MouseButtonType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MouseClickEvent extends EcsEvent {
  public final Vector3i pos;
  public final MouseButtonType type;

  public MouseClickEvent(Vector3i pos, MouseButtonType type) {
    this.pos = pos;
    this.type = type;
  }
}
