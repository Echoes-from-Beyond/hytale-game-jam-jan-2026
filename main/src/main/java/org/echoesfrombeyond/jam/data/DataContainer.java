package org.echoesfrombeyond.jam.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.echoesfrombeyond.jam.JamSave;

// no time to add and test deserialization so this is all hardcoded
public class DataContainer {
  public JamSave.BuildingType buildingType;
  public List<Upgrade> upgrades;
  public String resourceGenerated;
  public String description;

  public DataContainer(
      JamSave.BuildingType buildingType,
      List<Upgrade> upgrades,
      String resourceGenerated,
      String description) {
    this.buildingType = buildingType;
    this.upgrades = upgrades;
    this.resourceGenerated = resourceGenerated;
    // for building interact UIs in a sec
    this.description = description;
  }

  // look, this is quite possibly the worst thing I've ever made
  // I guess we're not that kweeback after all :pensive:
  // TODO: balancing
  public static DataContainer[] allUpgrades = {
    // ignore the comment that was here before, same effect was achieved by forcing everything into
    // the same string
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
        UpgradeRequirement.resourceTypes[0],
        """
        According to some old maps,
        a security outpost used to
        be here. This broken tower
        is all that remains.

        After you moved in, the
        automatons have been trying
        their hardest to destroy it.
        If you fixed it, would you
        be able to shut them all
        off?
        """),
    new DataContainer(
        JamSave.BuildingType.CommandTent,
        Arrays.asList(
            // tents are neither bought nor upgraded but just in case
            new Upgrade()),
        UpgradeRequirement.resourceTypes[0],
        """
        A haphazard tent you set up
        from some fabric you had
        lying around. This is where
        you order everyone else
        around from.
        """),
    new DataContainer(
        JamSave.BuildingType.Farm,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 3),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 3)),
                1,
                1),
            new Upgrade(
                2,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 10),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 10)),
                2,
                1)),
        UpgradeRequirement.resourceTypes[2],
        """
        Most crops don't grow well
        in Crucible's polluted
        soil. Out of the native
        flora, it took your squad
        a lot of trial and error
        to figure out what is
        safe to eat, and what
        only sends you on a trip
        to the latrine.
        """),
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
                1)),
        UpgradeRequirement.resourceTypes[1],
        """
        Though the surface is nigh
        uninhabitable, clean water
        reserves still exist deep
        in the rock under Crucible's
        foundations.

        It does take a bit of effort
        to drill down to them,
        however.
        """),
    new DataContainer(
        JamSave.BuildingType.ScavengerPort,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 5),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 6)),
                3,
                1),
            new Upgrade(
                2,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 13),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 14)),
                2,
                1)),
        UpgradeRequirement.resourceTypes[0],
        """
        Although they are nowhere
        near as well-preserved as
        what can supposedly be found
        in the heart of Crucible,
        bits of ancient tech still
        dot the devastated
        countryside of the once-great
        metropolis.

        It's a matter of sending someone
        out to get it. And praying that
        the automatons don't find them.
        """),
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
        UpgradeRequirement.resourceTypes[0],
        """
        After the fall of Crucible, the
        rest of the world decided to
        either worship magic or tech
        - not a blend of both - under
        the assumption that this
        'unnatural' combination was
        what led to the disaster.

        As such, magitech turrets have
        been lost to time. But the
        construction of the standard one
        remains as common knowledge, for
        even devout wizards respect
        big guns.
        """),
    new DataContainer(
        JamSave.BuildingType.Housing,
        Arrays.asList(
            new Upgrade(
                1,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 3),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[2], 5)),
                0,
                0),
            new Upgrade(
                2,
                Arrays.asList(
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[0], 10),
                    new UpgradeRequirement(UpgradeRequirement.resourceTypes[1], 4)),
                0,
                0)),
        UpgradeRequirement.resourceTypes[0],
        """
        Crucible is crawling with
        scavengers and urban explorers,
        all eager to plunder its many,
        alleged treasures. Even if you
        manage to sway them to your
        personal mission, you still
        need to offer them a place to
        stay.
        """)
  };

  public static ArrayList<DataContainer> placeableBuildings() {
    ArrayList<DataContainer> builds = new ArrayList<>(Arrays.asList(DataContainer.allUpgrades));

    builds.removeIf(
        b ->
            b.buildingType == JamSave.BuildingType.RadioTower
                || b.buildingType == JamSave.BuildingType.CommandTent
                || b.buildingType == JamSave.BuildingType.None);

    return builds;
  }
}
