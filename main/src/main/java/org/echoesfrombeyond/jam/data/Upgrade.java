package org.echoesfrombeyond.jam.data;

import java.util.ArrayList;
import java.util.List;

public class Upgrade {
  public int level;
  public List<UpgradeRequirement> requirements;

  public Upgrade() {
    this.level = 1;
    this.requirements = new ArrayList<>();
  }

  public Upgrade(int level, List<UpgradeRequirement> requirements) {
    this.level = level;
    this.requirements = requirements;
  }
}
