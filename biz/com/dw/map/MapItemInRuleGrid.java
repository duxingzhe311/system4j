package com.dw.map;

import java.util.ArrayList;
import java.util.List;

public class MapItemInRuleGrid
{
  RuleGrid ruleGrid = null;

  ArrayList<MapItem> mapItems = null;

  public MapItemInRuleGrid()
  {
  }

  public MapItemInRuleGrid(RuleGrid rg, List<MapItem> mis) {
    this.ruleGrid = rg;
    this.mapItems = new ArrayList();
    this.mapItems.addAll(mis);
  }

  public RuleGrid getRuleGrid()
  {
    return this.ruleGrid;
  }

  public List<MapItem> getMapItems()
  {
    return this.mapItems;
  }

  public void addMapItem(MapItem mi)
  {
    if (this.mapItems == null) {
      this.mapItems = new ArrayList();
    }
    this.mapItems.add(mi);
  }
}
