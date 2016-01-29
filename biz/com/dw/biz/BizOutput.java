package com.dw.biz;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;

@XRmi(reg_name="biz_output")
public class BizOutput
  implements IXmlDataable
{
  String name = null;

  XmlDataStruct outputDataStruct = null;

  boolean bCanCtrl = false;

  transient BizNodeObj belongTo = null;

  public BizOutput()
  {
    this.outputDataStruct = new XmlDataStruct();
  }

  public BizOutput(String n, XmlDataStruct xds)
  {
    if ((n == null) || (n.equals(""))) {
      throw new IllegalArgumentException("oper name cannot be null or empty");
    }
    if (xds == null) {
      throw new IllegalArgumentException("output xml data struct cannot be null");
    }
    this.name = n;
    this.outputDataStruct = xds;
  }

  public BizNodeObj getBelongToBizNodeObj()
  {
    return this.belongTo;
  }

  public String getName()
  {
    return this.name;
  }

  public XmlDataStruct getOutputDataStruct()
  {
    return this.outputDataStruct;
  }

  public void setOutputDataStruct(XmlDataStruct outputxds)
  {
    this.outputDataStruct = outputxds;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("name", this.name);
    xd.setSubDataSingle("ouput_xds", this.outputDataStruct.toXmlData());
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.name = xd.getParamValueStr("name");
    XmlData tmpxd = xd.getSubDataSingle("ouput_xds");
    this.outputDataStruct = new XmlDataStruct();
    this.outputDataStruct.fromXmlData(tmpxd);
  }
}
