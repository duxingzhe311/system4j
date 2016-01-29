package com.dw.biz.api;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlVal;

public class Parameter
  implements IXmlDataable, WFElement
{
  public static final int MODE_IN = 0;
  public static final int MODE_OUT = 1;
  public static final int MODE_INOUT = 2;
  private String id = null;
  private int mode = 0;

  private XmlVal val = null;

  private transient WFElement parentWFE = null;

  public Parameter()
  {
  }

  public Parameter(String id, int mode) {
    this.id = id;
    this.mode = mode;
  }

  public Parameter(String id, int mode, XmlVal v)
  {
    this.id = id;
    this.mode = mode;
    this.val = v;
  }

  public void setParentWFElement(WFElement pwfe)
  {
    this.parentWFE = pwfe;
  }

  public String getId()
  {
    return this.id;
  }

  public int getMode()
  {
    return this.mode;
  }

  public XmlVal getValue()
  {
    return this.val;
  }

  public void setValue(XmlVal o)
  {
    this.val = o;
  }

  public String toString()
  {
    StringBuffer tmpsb = new StringBuffer();

    tmpsb.append("[");
    if (this.mode == 1)
    {
      tmpsb.append("OUT");
    }
    else if (this.mode == 2)
    {
      tmpsb.append("INOUT");
    }
    else
    {
      tmpsb.append("IN");
    }
    tmpsb.append("]").append(this.id);

    return tmpsb.toString();
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    xd.setParamValue("id", this.id);
    xd.setParamValue("mode", Integer.valueOf(this.mode));
    if (this.val != null) {
      xd.setParamValue("val", this.val);
    }
    return xd;
  }

  public void loadXmlData(XmlData xd)
  {
    this.id = xd.getParamValueStr("id");
    this.mode = xd.getParamValueInt32("mode", 0);
    this.val = xd.getParamXmlVal("val");
  }

  public void fromXmlData(XmlData xd)
  {
    loadXmlData(xd);
  }

  public WFElement getParentWFElement()
  {
    return this.parentWFE;
  }
}
