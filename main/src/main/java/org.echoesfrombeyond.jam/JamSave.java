package org.echoesfrombeyond.jam;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jspecify.annotations.NullMarked;

/** Data that should be persistent across restarts */
@NullMarked
public class JamSave implements Resource<ChunkStore> {
  public enum BuildingType {
    None(""),

    // should use CamelCase not SHOUTY_SNAKE_CASE as per hytale convention
    CommandTent("command_tent"),
    Farm("farm"),
    Housing("housing"),
    RadioTower("radio_tower"),
    ScavengerPort("scavenger_port"),
    Turret("turret"),
    Well("well");

    public final String prefabAsset;

    BuildingType(String prefabAsset) {
      this.prefabAsset = prefabAsset;
    }

    public String getPrefabAsset() {
      return prefabAsset;
    }
  }

  public static class Building {
    public BuildingType type;
    public Vector3i min;
    public Vector3i max;

    // for turrets
    public float fireDelay;

    public Building() {
      this.type = BuildingType.Farm;
      this.min = new Vector3i(0, 0, 0);
      this.max = new Vector3i(0, 0, 0);

      this.fireDelay = TurretFireSystem.TURRET_FIRE_RATE;
    }

    public Building(Building other) {
      this.type = other.type;
      this.min = other.min.clone();
      this.max = other.max.clone();
      this.fireDelay = other.fireDelay;
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
          .append(
              new KeyedCodec<>("FireDelay", Codec.FLOAT),
              (self, value) -> self.fireDelay = value,
              (self) -> self.fireDelay)
          .add()
          .build();

  public static final Codec<List<Building>> BUILDING_LIST_CODEC =
      new ListCodec<>(BUILDING_CODEC, CopyOnWriteArrayList::new, false);

  public static final BuilderCodec<JamSave> CODEC =
      BuilderCodec.builder(JamSave.class, JamSave::new)
          .append(
              new KeyedCodec<>("Day", Codec.INTEGER),
              (self, value) -> self.day = value,
              (self) -> self.day)
          .add()
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
              new KeyedCodec<>("Buildings", BUILDING_LIST_CODEC),
              (self, value) -> self.buildings = value,
              (self) -> self.buildings)
          .add()
          .build();

  public int day;
  public int towerHealth;
  public int colonists;
  public int food;
  public int water;
  public int scrap;
  public List<Building> buildings;

  public JamSave() {
    this.day = 1;
    this.towerHealth = 10;
    this.buildings = new CopyOnWriteArrayList<>();
  }

  public JamSave(JamSave other) {
    this.day = other.day;
    this.towerHealth = other.towerHealth;
    this.colonists = other.colonists;
    this.food = other.food;
    this.water = other.water;
    this.scrap = other.scrap;
    this.buildings = new ArrayList<>(other.buildings.size());

    for (int i = 0; i < other.buildings.size(); i++)
      this.buildings.add(new Building(other.buildings.get(i)));
  }

  @Override
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  public Resource<ChunkStore> clone() {
    return new JamSave(this);
  }
}
