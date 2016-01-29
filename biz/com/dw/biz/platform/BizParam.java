package com.dw.biz.platform;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.Enumeration;
import java.util.Properties;

@XRmi(reg_name="biz_param")
public class BizParam
  implements IXmlDataable
{
  Properties params = new Properties();

  public BizParam()
  {
  }

  public BizParam(Properties p) {
    this.params = p;
  }

  public void setParamSet(BizParam bp)
  {
    for (Enumeration en = bp.params.propertyNames(); en.hasMoreElements(); )
    {
      String n = (String)en.nextElement();
      String v = bp.params.getProperty(n);
      this.params.setProperty(n, v);
    }
  }

  public Enumeration getParameterNames()
  {
    return this.params.propertyNames();
  }

  public String getParameter(String name)
  {
    return this.params.getProperty(name);
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    for (Enumeration en = this.params.propertyNames(); en.hasMoreElements(); )
    {
      String pn = (String)en.nextElement();
      String v = this.params.getProperty(pn);
      xd.setParamValue(pn, v);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    String[] tmps = xd.getParamNames();
    if (tmps != null)
    {
      for (String pn : tmps)
      {
        this.params.setProperty(pn, xd.getParamValueStr(pn));
      }
    }
  }
}
