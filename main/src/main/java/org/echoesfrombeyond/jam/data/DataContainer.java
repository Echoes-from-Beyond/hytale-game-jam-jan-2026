package org.echoesfrombeyond.jam.data;

import java.util.Arrays;
import java.util.List;
import org.echoesfrombeyond.jam.JamSave;

// no time to add and test deserialization so this is all hardcoded
public class DataContainer {
  public JamSave.BuildingType buildingType;
  public List<Upgrade> upgrades;
  public String resourceGenerated;

  public DataContainer(
      JamSave.BuildingType buildingType, List<Upgrade> upgrades, String resourceGenerated) {
    this.buildingType = buildingType;
    this.upgrades = upgrades;
    this.resourceGenerated = resourceGenerated;
  }

  // look, this is quite possibly the worst thing I've ever made
  // I guess we're not that kweeback after all :pensive:
  // TODO: balancing
  public static DataContainer[] allUpgrades = {
    // btw the event API for interactions is so bad that we can only have one resource requirement
    // there doesn't look to be a way to request an array of data from the player in a button
    // there IS potentially an ugly workaround for this (field_N where N is up to a fixed maximum of
    // requirements) but idk if we should bother
    new DataContainer(
        JamSave.BuildingType.RadioTower,
        Arrays.asList(
            // radio can't be bought, but have a default lvl 1 just in case
            new Upgrade(),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 100)),
                0,
                0)),
        UpgradeRequirement.resourceTypes[0]),
    new DataContainer(
        JamSave.BuildingType.CommandTent,
        Arrays.asList(
            // tents are neither bought nor upgraded but just in case
            new Upgrade()),
        UpgradeRequirement.resourceTypes[0]),
    new DataContainer(
        JamSave.BuildingType.Farm,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 3)),
                1,
                0),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 10)),
                2,
                2)),
        UpgradeRequirement.resourceTypes[2]),
    new DataContainer(
        JamSave.BuildingType.Well,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 6)),
                2,
                1),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 12)),
                2,
                4)),
        UpgradeRequirement.resourceTypes[1]),
    new DataContainer(
        JamSave.BuildingType.ScavengerPort,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 5)),
                3,
                1),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 13)),
                2,
                3)),
        UpgradeRequirement.resourceTypes[0]),
    new DataContainer(
        JamSave.BuildingType.Turret,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 7)),
                0,
                0),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 13)),
                0,
                0)),
        UpgradeRequirement.resourceTypes[0]),
    new DataContainer(
        JamSave.BuildingType.Housing,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 10)),
                0,
                0),
            new Upgrade(
                2,
                Arrays.asList(new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 10)),
                0,
                0)),
        UpgradeRequirement.resourceTypes[0])
  };
}
