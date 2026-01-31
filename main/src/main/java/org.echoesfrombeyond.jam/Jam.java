package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Jam implements Resource<ChunkStore> {
  public static final BuilderCodec<Jam> CODEC =
      BuilderCodec.builder(Jam.class, Jam::new)
          .append(
              new KeyedCodec<>("ClickCounter", Codec.INTEGER),
              (self, value) -> self.clickCounter = value,
              (self) -> self.clickCounter)
          .add()
          .build();

  public int clickCounter;

  public Jam() {}

  public Jam(Jam other) {
    this.clickCounter = other.clickCounter;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Resource<ChunkStore> clone() {
    return new Jam(this);
  }
}
