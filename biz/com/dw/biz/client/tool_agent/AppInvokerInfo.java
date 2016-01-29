package com.dw.biz.client.tool_agent;

import com.dw.biz.api.Parameter;
import com.dw.biz.api.WFElement;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.ArrayList;
import java.util.List;

@XRmi(reg_name="app_invoker_info")
public class AppInvokerInfo
  implements IXmlDataable, WFElement
{
  String appId = null;
  String appName = null;
  String appDesc = null;
  List<Parameter> inFormalParams = null;
  List<Parameter> outFormalParams = null;
  List<Parameter> inoutFormalParams = null;

  private transient WFElement parentWFE = null;

  public AppInvokerInfo()
  {
  }

  public AppInvokerInfo(String app_id, String app_name, String app_desc, List<Parameter> inps, List<Parameter> outps, List<Parameter> inoutps)
  {
    if ((app_id == null) || (app_id.equals(""))) {
      throw new IllegalArgumentException("app id cannot be null or empty!");
    }
    this.appId = app_id;
    this.appName = app_name;
    this.appDesc = app_desc;

    this.inFormalParams = inps;
    this.outFormalParams = outps;
    this.inoutFormalParams = inoutps;
  }

  public AppInvokerInfo(IAppInvoker ai)
  {
    this.appId = ai.getAppId();
    this.appName = ai.getAppName();
    this.appDesc = ai.getAppDesc();

    this.inFormalParams = ai.getInFormalParameters();
    this.outFormalParams = ai.getOutFormalParameters();
    this.inoutFormalParams = ai.getInOutFormalParameters();
  }

  public String getId()
  {
    return this.appId;
  }

  public void setParentWFElement(WFElement pwfe)
  {
    this.parentWFE = pwfe;
  }

  public String getAppId()
  {
    return this.appId;
  }

  public String getAppName()
  {
    return this.appName;
  }

  public String getAppDesc()
  {
    return this.appDesc;
  }

  public String toString()
  {
    return this.appName + "[" + this.appId + "]";
  }

  public List<Parameter> getInFormalParameters()
  {
    return this.inFormalParams;
  }

  public List<Parameter> getOutFormalParameters()
  {
    return this.outFormalParams;
  }

  public List<Parameter> getInOutFormalParameters()
  {
    return this.inoutFormalParams;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    xd.setParamValue("app_id", this.appId);
    if (this.appName != null)
      xd.setParamValue("app_name", this.appName);
    if (this.appDesc != null) {
      xd.setParamValue("app_desc", this.appDesc);
    }
    if (this.inFormalParams != null)
    {
      List sxds = xd.getOrCreateSubDataArray("in_formal_param");
      for (Parameter p : this.inFormalParams)
      {
        sxds.add(p.toXmlData());
      }
    }

    if (this.outFormalParams != null)
    {
      List sxds = xd.getOrCreateSubDataArray("out_formal_param");
      for (Parameter p : this.outFormalParams)
      {
        sxds.add(p.toXmlData());
      }
    }

    if (this.inoutFormalParams != null)
    {
      List sxds = xd.getOrCreateSubDataArray("inout_formal_param");
      for (Parameter p : this.inoutFormalParams)
      {
        sxds.add(p.toXmlData());
      }
    }
    return xd;
  }

  public void loadXmlData(XmlData xd)
  {
    this.appId = xd.getParamValueStr("app_id");
    this.appName = xd.getParamValueStr("app_name");
    this.appDesc = xd.getParamValueStr("app_desc");

    List sxd = xd.getSubDataArray("in_formal_param");
    if (sxd != null)
    {
      this.inFormalParams = new ArrayList(sxd.size());
      for (XmlData tmpxd : sxd)
      {
        Parameter tmpp = new Parameter();
        tmpp.loadXmlData(tmpxd);
        this.inFormalParams.add(tmpp);
      }
    }
    sxd = xd.getSubDataArray("out_formal_param");
    if (sxd != null)
    {
      this.outFormalParams = new ArrayList(sxd.size());
      for (XmlData tmpxd : sxd)
      {
        Parameter tmpp = new Parameter();
        tmpp.loadXmlData(tmpxd);
        this.outFormalParams.add(tmpp);
      }
    }

    sxd = xd.getSubDataArray("inout_formal_param");
    if (sxd != null)
    {
      this.inoutFormalParams = new ArrayList(sxd.size());
      for (XmlData tmpxd : sxd)
      {
        Parameter tmpp = new Parameter();
        tmpp.loadXmlData(tmpxd);
        this.inoutFormalParams.add(tmpp);
      }
    }
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
