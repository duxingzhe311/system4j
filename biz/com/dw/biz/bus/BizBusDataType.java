package com.dw.biz.bus;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;

public class BizBusDataType
  implements IXmlDataable
{
  String dataTypeName = null;
  XmlDataStruct dataStruct = new XmlDataStruct();

  public BizBusDataType()
  {
  }

  public BizBusDataType(String dtn, XmlDataStruct xds)
  {
    this.dataTypeName = dtn;
    this.dataStruct = xds;
  }

  public String getTypeName()
  {
    return this.dataTypeName;
  }

  public XmlDataStruct getDataStruct()
  {
    return this.dataStruct;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("name", this.dataTypeName);
    xd.setParamValue("data_struct", this.dataStruct.toXmlData());
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.dataTypeName = xd.getParamValueStr("name");
    XmlData tmpxd = xd.getSubDataSingle("data_struct");
    if (tmpxd != null)
      this.dataStruct.fromXmlData(tmpxd);
  }
}
