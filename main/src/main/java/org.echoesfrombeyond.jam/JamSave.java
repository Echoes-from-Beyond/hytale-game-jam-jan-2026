package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.jspecify.annotations.NullMarked;

/** Data that should be persistent across restarts */
@NullMarked
public class JamSave implements Resource<ChunkStore> {
  public enum BuildingType {
    None(""),

    // should use CamelCase not SHOUTY_SNAKE_CASE as per hytale convention
    RadioTower("radio_tower");

    public final String prefabAsset;

    BuildingType(String prefabAsset) {
      this.prefabAsset = prefabAsset;
    }
  }

  public static class Building {
    public BuildingType type;
    public Vector3i min;
    public Vector3i max;

    public Building() {
      this.type = BuildingType.RadioTower;
      this.min = new Vector3i(0, 0, 0);
      this.max = new Vector3i(0, 0, 0);
    }

    public Building(Building other) {
      this.type = other.type;
      this.min = other.min.clone();
      this.max = other.max.clone();
    }
  }

  public static final EnumCodec<BuildingType> BUILDING_TYPE_CODEC =
      new EnumCodec<>(BuildingType.class);

  public static final BuilderCodec<Building> BUILDING_CODEC =
      BuilderCodec.builder(Building.class, Building::new)
          .append(
              new KeyedCodec<>("BuildingType", BUILDING_TYPE_CODEC),
              (self, value) -> self.type = value,
              (self) -> self.type)
          .add()
          .append(
              new KeyedCodec<>("Min", Vector3i.CODEC),
              (self, value) -> self.min = value,
              (self) -> self.min)
          .add()
          .append(
              new KeyedCodec<>("Max", Vector3i.CODEC),
              (self, value) -> self.max = value,
              (self) -> self.max)
          .add()
          .build();

  public static final ArrayCodec<Building> BUILDING_ARRAY_CODEC =
      new ArrayCodec<>(BUILDING_CODEC, Building[]::new);

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
          .append(
              new KeyedCodec<>("Buildings", BUILDING_ARRAY_CODEC),
              (self, value) -> self.buildings = value,
              (self) -> self.buildings)
          .add()
          .build();

  public int colonists;
  public int food;
  public int water;
  public int scrap;
  public Building[] buildings;

  public JamSave() {
    this.buildings = new Building[0];
  }

  public JamSave(JamSave other) {
    this.colonists = other.colonists;
    this.food = other.food;
    this.water = other.water;
    this.scrap = other.scrap;
    this.buildings = new Building[other.buildings.length];

    for (int i = 0; i < buildings.length; i++) {
      this.buildings[i] = new Building(other.buildings[i]);
    }
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Resource<ChunkStore> clone() {
    return new JamSave(this);
  }
}
