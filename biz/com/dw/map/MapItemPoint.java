package com.dw.map;

import com.dw.system.xmldata.XmlData;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

public class MapItemPoint extends MapItem
{
  MapPoint point = null;

  String title = null;

  boolean bAreaMark = false;

  MapItemPoint()
  {
  }

  public MapItemPoint(long x, int y)
  {
    this.point = new MapPoint(x, y);
  }

  public MapItemPoint(MapPoint mp)
  {
    this.point = new MapPoint(mp.x, mp.y);
  }

  public MapItemPoint(double lng, double lat)
  {
    this.point = new MapPoint(lng, lat);
  }

  public MapPoint getMapPoint()
  {
    return this.point;
  }

  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String t)
  {
    this.title = t;
  }

  public boolean isAreaMark()
  {
    return this.bAreaMark;
  }

  public boolean hitRect(MapRect mr)
  {
    return mr.containsPoint(this.point);
  }

  public String getTypeStr()
  {
    return "pt";
  }

  public MapItem createNewIns()
  {
    return new MapItemPoint();
  }

  public MapRect getBounds()
  {
    return new MapRect(this.point.x, this.point.y, 0L, 0L);
  }

  public boolean canSelectedByDrawPos(Graphics g, MapWindow mapwin, int x, int y)
  {
    DrawPoint dp = mapwin.calDrawPoint(this.point);
    if (dp == null) {
      return false;
    }
    if (Math.abs(dp.x - x) > 2)
    {
      return false;
    }

    if (Math.abs(dp.y - y) > 2)
    {
      return false;
    }

    return true;
  }

  public void render(Graphics g, MapWindow mapwin, Color c)
  {
    DrawPoint dp = mapwin.calDrawPoint(this.point);
    if (dp == null) {
      return;
    }

    g.setColor(c);

    g.fillOval(dp.x - 2, dp.y - 2, 4, 4);
  }

  public HashMap<RuleGrid, MapItemInRuleGrid> splitByRuleGrid(long base_rule)
  {
    RuleGrid rg = RuleGrid.calBelongRuleGrid(base_rule, this.point);
    ArrayList mis = new ArrayList();
    mis.add(this);
    MapItemInRuleGrid r = new MapItemInRuleGrid(rg, mis);

    HashMap ret = new HashMap();

    ret.put(rg, r);
    return ret;
  }

  public XmlData toXmlData()
  {
    XmlData xd = super.toXmlData();

    xd.setParamValue("x", Long.valueOf(this.point.x));
    xd.setParamValue("y", Long.valueOf(this.point.y));

    if (this.title != null) {
      xd.setParamValue("title", this.title);
    }
    if (this.bAreaMark) {
      xd.setParamValue("area_mark", Boolean.valueOf(true));
    }

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    super.fromXmlData(xd);

    long x = xd.getParamValueInt64("x", 0L);
    long y = xd.getParamValueInt64("y", 0L);
    this.point = new MapPoint(x, y);

    this.title = xd.getParamValueStr("title");
    this.bAreaMark = xd.getParamValueBool("area_mark", false).booleanValue();
  }
}
