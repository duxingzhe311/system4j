package com.dw.biz;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;

@XRmi(reg_name="biz_event")
public class BizEvent
  implements IXmlDataable
{
  String name = null;

  XmlDataStruct eventDataStruct = null;

  boolean bCtrllable = false;

  public BizEvent()
  {
    this.eventDataStruct = new XmlDataStruct();
  }

  public BizEvent(String n, XmlDataStruct xds)
  {
    this(n, xds, false);
  }

  public BizEvent(String n, XmlDataStruct xds, boolean bctrl)
  {
    if ((n == null) || (n.equals(""))) {
      throw new IllegalArgumentException("oper name cannot be null or empty");
    }
    if (xds == null) {
      throw new IllegalArgumentException("event xml data struct cannot be null");
    }
    this.name = n;
    this.eventDataStruct = xds;
    this.bCtrllable = bctrl;
  }

  public String getName()
  {
    return this.name;
  }

  public XmlDataStruct getEventDataStruct()
  {
    return this.eventDataStruct;
  }

  public void setEventDataStruct(XmlDataStruct evxds)
  {
    this.eventDataStruct = evxds;
  }

  public boolean isCtrllable()
  {
    return this.bCtrllable;
  }

  public void setCtrllable(boolean b)
  {
    this.bCtrllable = b;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("name", this.name);
    xd.setParamValue("is_ctrl", Boolean.valueOf(this.bCtrllable));
    xd.setSubDataSingle("event_xds", this.eventDataStruct.toXmlData());
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.name = xd.getParamValueStr("name");
    this.bCtrllable = xd.getParamValueBool("is_ctrl", false).booleanValue();
    XmlData tmpxd = xd.getSubDataSingle("event_xds");
    this.eventDataStruct = new XmlDataStruct();
    this.eventDataStruct.fromXmlData(tmpxd);
  }
}
