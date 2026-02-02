package org.echoesfrombeyond.jam.data;

public class UpgradeRequirement {
  public String resourceType;
  public int amount;

  // match resourceType from here if you want to assign one
  // EXTREMELY hacky but no time
  public static String[] resourceTypes = {"scrap", "water", "food"};

  public UpgradeRequirement() {
    this.resourceType = "scrap";
    this.amount = 10;
  }

  public UpgradeRequirement(String resourceType, int amount) {
    this.resourceType = resourceType;
    this.amount = amount;
  }
}
