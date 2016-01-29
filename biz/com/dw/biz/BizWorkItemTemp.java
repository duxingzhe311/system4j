package com.dw.biz;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;

@XRmi(reg_name="biz_workitem_temp")
public class BizWorkItemTemp extends BizNodeObj
{
  String title = null;

  String desc = null;

  XmlDataStruct inputXDS = new XmlDataStruct();

  XmlDataStruct outputXDS = new XmlDataStruct();

  String readOnlyBizViewPath = null;

  String modifyBizNodePath = null;

  String finishBizActionPath = null;

  String checkBizActionPath = null;

  String userRightRule = null;

  transient boolean bValid = false;

  public BizWorkItemTemp()
  {
  }

  public BizWorkItemTemp(String title, String desc, String rightrule, XmlDataStruct inputxds, XmlDataStruct outputxds, String readonly_bizview, String modify_biznode, String finish_bizaction, String check_bizaction)
  {
    this.title = title;
    this.desc = desc;

    this.userRightRule = rightrule;

    this.inputXDS = inputxds;
    this.outputXDS = outputxds;
    this.readOnlyBizViewPath = readonly_bizview;
    this.modifyBizNodePath = modify_biznode;
    this.finishBizActionPath = finish_bizaction;
    this.checkBizActionPath = check_bizaction;
  }

  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String t)
  {
    this.title = t;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public void setDesc(String d)
  {
    this.desc = d;
  }

  public String getRightRule()
  {
    return this.userRightRule;
  }

  public void setRightRule(String rr)
  {
    this.userRightRule = rr;
  }

  public XmlDataStruct getInputDataStruct()
  {
    return this.inputXDS;
  }

  public void setInputDataStruct(XmlDataStruct xds)
  {
    if (xds == null)
      this.inputXDS = new XmlDataStruct();
    else
      this.inputXDS = xds;
  }

  public XmlDataStruct getOutputDataStruct()
  {
    return this.outputXDS;
  }

  public void setOutputDataStruct(XmlDataStruct xds)
  {
    if (xds == null)
      this.outputXDS = new XmlDataStruct();
    else
      this.outputXDS = xds;
  }

  public String getReadOnlyBizViewPath()
  {
    return this.readOnlyBizViewPath;
  }

  public void setReadOnlyBizViewPath(String vp)
  {
    this.readOnlyBizViewPath = vp;
  }

  public String getModifyBizNodePath()
  {
    return this.modifyBizNodePath;
  }

  public void setModifyBizNodePath(String p)
  {
    this.modifyBizNodePath = p;
  }

  public String getFinishBizActionPath()
  {
    return this.finishBizActionPath;
  }

  public void setFinishBizActionPath(String p)
  {
    this.finishBizActionPath = p;
  }

  public String getCheckBizActionPath()
  {
    return this.checkBizActionPath;
  }

  public void setCheckBizActionPath(String p)
  {
    this.checkBizActionPath = p;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    if (this.title != null)
      xd.setParamValue("title", this.title);
    if (this.desc != null) {
      xd.setParamValue("desc", this.desc);
    }
    if (this.userRightRule != null) {
      xd.setParamValue("right_rule", this.userRightRule);
    }
    if (this.inputXDS != null)
    {
      xd.setSubDataSingle("input_xds", 
        this.inputXDS.toXmlData());
    }

    if (this.outputXDS != null)
    {
      xd.setSubDataSingle("output_xds", 
        this.outputXDS.toXmlData());
    }

    xd.setParamValue("readonly_bizview", this.readOnlyBizViewPath);
    xd.setParamValue("modify_biznode", this.modifyBizNodePath);
    xd.setParamValue("finish_bizaction", this.finishBizActionPath);
    xd.setParamValue("check_bizaction", this.checkBizActionPath);

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.title = xd.getParamValueStr("title");
    this.desc = xd.getParamValueStr("desc");

    this.userRightRule = xd.getParamValueStr("right_rule");
    if (this.userRightRule != null) {
      this.userRightRule = this.userRightRule.trim();
    }
    XmlData tmpxd = xd.getSubDataSingle("input_xds");
    if (tmpxd != null)
    {
      this.inputXDS = new XmlDataStruct();
      this.inputXDS.fromXmlData(tmpxd);
    }

    tmpxd = xd.getSubDataSingle("output_xds");
    if (tmpxd != null)
    {
      this.outputXDS = new XmlDataStruct();
      this.outputXDS.fromXmlData(tmpxd);
    }

    this.readOnlyBizViewPath = xd.getParamValueStr("readonly_bizview");
    this.modifyBizNodePath = xd.getParamValueStr("modify_biznode");
    this.finishBizActionPath = xd.getParamValueStr("finish_bizaction");
    this.checkBizActionPath = xd.getParamValueStr("check_bizaction");
  }
}
