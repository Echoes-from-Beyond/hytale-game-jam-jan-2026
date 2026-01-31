package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class JamComponent implements Component<ChunkStore> {
  public static final BuilderCodec<JamComponent> CODEC =
      BuilderCodec.builder(JamComponent.class, JamComponent::new).build();

  public JamComponent() {}

  public JamComponent(JamComponent other) {}

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public @Nullable Component<ChunkStore> clone() {
    return new JamComponent(this);
  }
}
