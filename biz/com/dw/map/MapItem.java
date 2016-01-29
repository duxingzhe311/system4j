package com.dw.map;

import com.dw.system.Convert;
import com.dw.system.xmldata.XmlData;
import java.util.HashMap;

public abstract class MapItem extends MapId
{
  IMapDynItemRender dynRender = null;

  public MapItem()
  {
  }

  public MapItem(String id)
  {
    super(id);
  }

  public IMapDynItemRender getDynRender()
  {
    return this.dynRender;
  }

  public void setDynRender(IMapDynItemRender v)
  {
    this.dynRender = v;
  }

  public void setDynRenderByName(String dynn)
  {
    this.dynRender = DynRenderHelper.getRender(dynn);
  }

  public abstract boolean hitRect(MapRect paramMapRect);

  public abstract String getTypeStr();

  public abstract MapItem createNewIns();

  public abstract HashMap<RuleGrid, MapItemInRuleGrid> splitByRuleGrid(long paramLong);

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("mid", this.id);
    xd.setParamValue("t", getTypeStr());

    if (this.extInfo != null)
    {
      xd.setSubDataSingle("ext", this.extInfo);
    }

    if (this.dynRender != null) {
      xd.setSubDataSingle("dyn_render", this.dynRender.toXmlData());
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.id = xd.getParamValueStr("mid");
    if (Convert.isNullOrEmpty(this.id)) {
      throw new IllegalArgumentException("no mid found in MapItem xmldata");
    }
    this.extInfo = xd.getSubDataSingle("ext");

    XmlData dynr = xd.getSubDataSingle("dyn_render");
    if (dynr != null)
    {
      this.dynRender = IMapDynItemRender.parseFromXD(dynr);
    }
  }
}
