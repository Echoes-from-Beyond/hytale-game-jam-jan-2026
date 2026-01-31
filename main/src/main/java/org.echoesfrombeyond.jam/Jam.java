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
              new KeyedCodec<>("Colonists", Codec.INTEGER),
              (self, value) -> self.colonists = value,
              (self) -> self.colonists)
          .add()
          .append(
              new KeyedCodec<>("Food", Codec.INTEGER),
              (self, value) -> self.food = value,
              (self) -> self.food)
          .add()
          .append(
              new KeyedCodec<>("Water", Codec.INTEGER),
              (self, value) -> self.water = value,
              (self) -> self.water)
          .add()
          .append(
              new KeyedCodec<>("Scrap", Codec.INTEGER),
              (self, value) -> self.scrap = value,
              (self) -> self.scrap)
          .add()
          .build();

  public int colonists;
  public int food;
  public int water;
  public int scrap;

  public Jam() {}

  public Jam(Jam other) {
    this.colonists = other.colonists;
    this.food = other.food;
    this.water = other.water;
    this.scrap = other.scrap;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Resource<ChunkStore> clone() {
    return new Jam(this);
  }
}
