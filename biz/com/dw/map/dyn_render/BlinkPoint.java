package com.dw.map.dyn_render;

import com.dw.map.DrawPoint;
import com.dw.map.IMapDynItemRender;
import com.dw.map.MapId;
import com.dw.map.MapItemPoint;
import com.dw.map.MapWindow;
import com.dw.system.xmldata.XmlData;
import java.awt.Color;
import java.awt.Graphics;

public class BlinkPoint extends IMapDynItemRender
{
  int blinkSwitchTickN = 20;

  Color blinkColor = Color.red;

  int blinkPointSize = 20;

  public int getBlinkSwitchTickNum()
  {
    return this.blinkSwitchTickN;
  }

  public void setBlinkSwitchTickNum(int v)
  {
    if (v <= 0) {
      return;
    }
    this.blinkSwitchTickN = v;
  }

  public Color getBlinkColor()
  {
    return this.blinkColor;
  }

  public void setBlinkColor(Color c)
  {
    this.blinkColor = c;
  }

  public int getBlinkPointSize()
  {
    return this.blinkPointSize;
  }

  public void setBlinkPointSize(int v)
  {
    this.blinkPointSize = v;
  }

  public String getDynName()
  {
    return "blink";
  }

  public IMapDynItemRender createNewIns()
  {
    return new BlinkPoint();
  }

  protected int onDynTick(MapWindow mapwin, int tick, Graphics g, MapId mi)
  {
    if ((mi instanceof MapItemPoint))
    {
      return doMapItemPointBlink(mapwin, tick, g, (MapItemPoint)mi);
    }
    return -1;
  }

  int doMapItemPointBlink(MapWindow mapwin, int tick, Graphics g, MapItemPoint mip)
  {
    if (tick >= this.blinkSwitchTickN)
    {
      if (tick >= this.blinkSwitchTickN * 2) {
        return 0;
      }
      return -1;
    }

    DrawPoint dp = mapwin.calDrawPoint(mip.getMapPoint());
    if (dp == null) {
      return -1;
    }

    int r = this.blinkPointSize / 2;
    g.setColor(this.blinkColor);

    g.drawOval(dp.x - r, dp.y - r, 2 * r, 2 * r);
    return -1;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("switch_tn", Integer.valueOf(this.blinkSwitchTickN));

    xd.setParamValue("c_r", Integer.valueOf(this.blinkColor.getRed()));
    xd.setParamValue("c_g", Integer.valueOf(this.blinkColor.getGreen()));
    xd.setParamValue("c_b", Integer.valueOf(this.blinkColor.getBlue()));

    xd.setParamValue("blink_size", Integer.valueOf(this.blinkPointSize));
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    super.fromXmlData(xd);

    this.blinkSwitchTickN = xd.getParamValueInt32("switch_tn", 20);

    int r = xd.getParamValueInt32("c_r", -1);
    int g = xd.getParamValueInt32("c_g", -1);
    int b = xd.getParamValueInt32("c_b", -1);
    if ((r > 0) && (g > 0) && (b > 0))
      this.blinkColor = new Color(r, g, b);
    else
      this.blinkColor = Color.red;
    this.blinkPointSize = xd.getParamValueInt32("blink_size", 20);
  }
}
