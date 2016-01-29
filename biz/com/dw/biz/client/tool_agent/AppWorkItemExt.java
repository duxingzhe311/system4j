package com.dw.biz.client.tool_agent;

import com.dw.biz.api.IExtable;
import com.dw.system.xmldata.XmlData;

public class AppWorkItemExt
  implements IExtable
{
  public String getExtType()
  {
    return "app";
  }

  public XmlData toXmlData()
  {
    return null;
  }

  public void fromXmlData(XmlData xd)
  {
  }
}
