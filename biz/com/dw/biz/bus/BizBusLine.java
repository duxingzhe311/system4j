package com.dw.biz.bus;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import java.util.ArrayList;
import java.util.List;

public class BizBusLine
  implements IXmlDataable
{
  String lineName = null;
  ArrayList<BizBusDataType> dataTypes = new ArrayList();

  public BizBusLine()
  {
  }

  public BizBusLine(String name, List<BizBusDataType> bbdts) {
    this.lineName = name;
    this.dataTypes.addAll(bbdts);
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("name", this.lineName);
    List xds = xd.getOrCreateSubDataArray("data_types");
    for (BizBusDataType bbdt : this.dataTypes)
    {
      xds.add(bbdt.toXmlData());
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.lineName = xd.getParamValueStr("name");
    List xds = xd.getSubDataArray("data_types");
    if (xds != null)
    {
      for (XmlData tmpxd : xds)
      {
        BizBusDataType bbdt = new BizBusDataType();
        bbdt.fromXmlData(tmpxd);
        this.dataTypes.add(bbdt);
      }
    }
  }
}
