package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jspecify.annotations.NullMarked;

/** Data that should be persistent across restarts */
@NullMarked
public class JamSave implements Resource<ChunkStore> {
  public static final BuilderCodec<JamSave> CODEC =
      BuilderCodec.builder(JamSave.class, JamSave::new)
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

  public JamSave() {}

  public JamSave(JamSave other) {
    this.colonists = other.colonists;
    this.food = other.food;
    this.water = other.water;
    this.scrap = other.scrap;
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Resource<ChunkStore> clone() {
    return new JamSave(this);
  }
}
