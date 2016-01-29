package com.dw.map;

import com.dw.system.Convert;
import com.dw.system.xmldata.XmlData;
import java.awt.Graphics;

public abstract class IMapDynItemRender
{
  int curTickVal = 0;

  public static IMapDynItemRender parseFromXD(XmlData xd)
  {
    String dynn = xd.getParamValueStr("dyn_name");
    if (Convert.isNullOrEmpty(dynn)) {
      return null;
    }
    IMapDynItemRender r = DynRenderHelper.getRender(dynn);
    if (r == null) {
      return null;
    }
    r = r.createNewIns();
    r.fromXmlData(xd);
    return r;
  }

  public void tickRender(MapWindow mapwin, Graphics g, MapId mi)
  {
    int r = onDynTick(mapwin, this.curTickVal, g, mi);
    if (r >= 0)
      this.curTickVal = r;
    else
      this.curTickVal += 1;
  }

  public abstract String getDynName();

  public abstract IMapDynItemRender createNewIns();

  protected abstract int onDynTick(MapWindow paramMapWindow, int paramInt, Graphics paramGraphics, MapId paramMapId);

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("dyn_name", getDynName());
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
  }
}
