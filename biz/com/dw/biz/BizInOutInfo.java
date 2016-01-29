package com.dw.biz;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.ArrayList;
import java.util.List;

@XRmi(reg_name="biz_inout_info")
public class BizInOutInfo
  implements IXmlDataable
{
  XmlDataStruct input = null;
  ArrayList<BizOutput> outputs = null;

  public BizInOutInfo() {
  }

  public BizInOutInfo(XmlDataStruct input, List<BizOutput> outputs) {
    this.input = input;
    if (outputs != null)
    {
      this.outputs = new ArrayList(outputs.size());
      this.outputs.addAll(outputs);
    }
  }

  public XmlDataStruct getInputInfo()
  {
    return this.input;
  }

  public ArrayList<BizOutput> getOutputInfo()
  {
    return this.outputs;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    if (this.input != null)
      xd.setSubDataSingle("in", this.input.toXmlData());
    if (this.outputs != null)
    {
      List xds = xd.getOrCreateSubDataArray("out");
      for (BizOutput bo : this.outputs)
      {
        xds.add(bo.toXmlData());
      }
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    XmlData inxd = xd.getSubDataSingle("in");
    if (inxd != null)
    {
      this.input = new XmlDataStruct();
      this.input.fromXmlData(inxd);
    }

    List outxds = xd.getSubDataArray("out");
    if (outxds != null)
    {
      this.outputs = new ArrayList(outxds.size());
      for (XmlData outxd : outxds)
      {
        BizOutput bo = new BizOutput();
        bo.fromXmlData(outxd);
        this.outputs.add(bo);
      }
    }
  }
}
