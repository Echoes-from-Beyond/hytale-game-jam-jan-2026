package org.echoesfrombeyond.jam.data;

import java.util.ArrayList;
import java.util.List;

public class Upgrade {
  public int level;
  public List<UpgradeRequirement> requirements;
  public int resourcesGeneratedPerColonist;

  // omit this if we remove colonists
  public int maxColonists;

  public Upgrade() {
    this.level = 1;
    this.requirements = new ArrayList<>();
    this.resourcesGeneratedPerColonist = 0;
    this.maxColonists = 0;
  }

  public Upgrade(
      int level, List<UpgradeRequirement> requirements, int resourcesGenerated, int maxColonists) {
    this.level = level;
    this.requirements = requirements;
    this.resourcesGeneratedPerColonist = resourcesGenerated;
    this.maxColonists = maxColonists;
  }
}
