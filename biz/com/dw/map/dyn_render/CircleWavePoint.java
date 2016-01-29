package com.dw.map.dyn_render;

import com.dw.map.DrawPoint;
import com.dw.map.IMapDynItemRender;
import com.dw.map.MapId;
import com.dw.map.MapItemPoint;
import com.dw.map.MapWindow;
import com.dw.system.xmldata.XmlData;
import java.awt.Color;
import java.awt.Graphics;

public class CircleWavePoint extends IMapDynItemRender
{
  int circleNum = 20;

  Color circleColor = Color.red;

  int maxCircleSize = 20;

  public String getDynName()
  {
    return "circle_wave";
  }

  public IMapDynItemRender createNewIns()
  {
    return new CircleWavePoint();
  }

  protected int onDynTick(MapWindow mapwin, int tick, Graphics g, MapId mi)
  {
    if ((mi instanceof MapItemPoint))
    {
      return doMapItemPointCircleWave(mapwin, tick, g, (MapItemPoint)mi);
    }
    return -1;
  }

  int doMapItemPointCircleWave(MapWindow mapwin, int tick, Graphics g, MapItemPoint mip)
  {
    if (this.circleNum < tick) {
      return 0;
    }

    DrawPoint dp = mapwin.calDrawPoint(mip.getMapPoint());
    if (dp == null) {
      return -1;
    }

    g.setColor(this.circleColor);
    int r = tick * this.circleNum / this.maxCircleSize;

    g.drawOval(dp.x - r, dp.y - r, 2 * r, 2 * r);
    return -1;
  }

  public XmlData toXmlData()
  {
    XmlData xd = super.toXmlData();
    xd.setParamValue("circle_num", Integer.valueOf(this.circleNum));

    xd.setParamValue("c_r", Integer.valueOf(this.circleColor.getRed()));
    xd.setParamValue("c_g", Integer.valueOf(this.circleColor.getGreen()));
    xd.setParamValue("c_b", Integer.valueOf(this.circleColor.getBlue()));

    xd.setParamValue("circle_size", Integer.valueOf(this.maxCircleSize));
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    super.fromXmlData(xd);

    this.circleNum = xd.getParamValueInt32("circle_num", 20);

    this.maxCircleSize = xd.getParamValueInt32("circle_size", 20);
  }
}
