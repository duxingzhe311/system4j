package com.dw.biz;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.HashMap;

@XRmi(reg_name="biz_action_result")
public class BizActionResult
  implements IXmlDataable
{
  String resultStr = null;
  XmlData resultXD = null;
  HashMap<String, Object> resultObjMap = null;

  public BizActionResult()
  {
  }

  public BizActionResult(String res_str, XmlData xd) {
    this.resultStr = res_str;
    this.resultXD = xd;
  }

  public BizActionResult(String res_str, XmlData xd, HashMap<String, Object> res_objm)
  {
    this.resultStr = res_str;
    this.resultXD = xd;
    this.resultObjMap = res_objm;
  }

  public String getResultStr()
  {
    return this.resultStr;
  }

  public XmlData getResultData()
  {
    return this.resultXD;
  }

  public HashMap<String, Object> getResultObjMap()
  {
    return this.resultObjMap;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    if (this.resultStr != null) {
      xd.setParamValue("result", this.resultStr);
    }
    if (this.resultXD != null) {
      xd.setSubDataSingle("data", this.resultXD);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.resultStr = xd.getParamValueStr("result");
    this.resultXD = xd.getSubDataSingle("data");
  }
}
