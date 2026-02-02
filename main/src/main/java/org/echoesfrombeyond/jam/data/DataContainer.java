package org.echoesfrombeyond.jam.data;

import java.util.Arrays;
import java.util.List;
import org.echoesfrombeyond.jam.JamSave;

// no time to add and test deserialization so this is all hardcoded
public class DataContainer {
  public JamSave.BuildingType buildingType;
  public List<Upgrade> upgrades;

  public DataContainer(JamSave.BuildingType buildingType, List<Upgrade> upgrades) {
    this.buildingType = buildingType;
    this.upgrades = upgrades;
  }

  // look, this is quite possibly the worst thing I've ever made
  // I guess we're not that kweeback after all :pensive:
  // TODO: balancing
  public static DataContainer[] allUpgrades = {
    new DataContainer(
        JamSave.BuildingType.RadioTower,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    // level 1 is never encountered (radio tower is not in shop) but just in case
                    new UpgradeRequirement())),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 100))))),
    new DataContainer(
        JamSave.BuildingType.CommandTent,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    // tents are neither bought nor upgraded but just in case
                    new UpgradeRequirement())))),
    new DataContainer(
        JamSave.BuildingType.Farm,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 3),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 3))),
            new Upgrade(
                2,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 6),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 10))))),
    new DataContainer(
        JamSave.BuildingType.Well,
        Arrays.asList(
            new Upgrade(
                1, Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 6))),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 12))))),
    new DataContainer(
        JamSave.BuildingType.ScavengerPort,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 5),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 6))),
            new Upgrade(
                2,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 13),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 14))))),
    new DataContainer(
        JamSave.BuildingType.Turret,
        Arrays.asList(
            new Upgrade(
                1, Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 7))),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 13))))),
    new DataContainer(
        JamSave.BuildingType.Housing,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 3),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 10))),
            new Upgrade(
                2,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 10),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 4)))))
  };
}
