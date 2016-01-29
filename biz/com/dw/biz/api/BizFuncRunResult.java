package com.dw.biz.api;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;

@XRmi(reg_name="biz_func_run_result")
public class BizFuncRunResult
  implements IXmlDataable
{
  ResultType resultType = ResultType.run_show_view;

  String funcPath = null;

  String bizViewPath = null;

  String viewCont = null;

  String viewContType = "html";

  int errorCode = -1;

  String errorInfo = null;

  public static BizFuncRunResult createErrorResult(String funcpath, int errorid, String errorinfo)
  {
    if ((funcpath == null) || (funcpath.equals(""))) {
      throw new IllegalArgumentException("func path cannot be null!");
    }
    BizFuncRunResult r = new BizFuncRunResult();
    r.resultType = ResultType.run_error;
    r.funcPath = funcpath;
    r.errorCode = errorid;
    r.errorInfo = errorinfo;
    return r;
  }

  public static BizFuncRunResult createShowBizViewResult(String funcpath, String viewpath, String viewcont)
  {
    if ((funcpath == null) || (funcpath.equals(""))) {
      throw new IllegalArgumentException("func path cannot be null!");
    }
    if ((viewpath == null) || (viewpath.equals(""))) {
      throw new IllegalArgumentException("biz view id cannot be null or empty");
    }
    BizFuncRunResult r = new BizFuncRunResult();
    r.resultType = ResultType.run_show_view;
    r.funcPath = funcpath;
    r.bizViewPath = viewpath;
    r.viewCont = viewcont;

    return r;
  }

  public static BizFuncRunResult createEndResult(String funcpath, String viewcont, String viewconttype)
  {
    if ((funcpath == null) || (funcpath.equals(""))) {
      throw new IllegalArgumentException("func path cannot be null!");
    }
    BizFuncRunResult r = new BizFuncRunResult();
    r.resultType = ResultType.run_end;
    r.funcPath = funcpath;
    r.viewCont = viewcont;
    r.viewContType = viewconttype;

    return r;
  }

  public ResultType getResultType()
  {
    return this.resultType;
  }

  public String getBizFuncPath()
  {
    return this.funcPath;
  }

  public String getBizViewPath()
  {
    return this.bizViewPath;
  }

  public String getViewCont()
  {
    return this.viewCont;
  }

  public String getViewContType()
  {
    return this.viewContType;
  }

  public int getErrorCode()
  {
    return this.errorCode;
  }

  public String getErrorInfo()
  {
    return this.errorInfo;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("type", this.resultType.toString());
    xd.setParamValue("biz_func_path", this.funcPath);
    if (this.bizViewPath != null)
      xd.setParamValue("biz_view_path", this.bizViewPath);
    if (this.viewCont != null)
      xd.setParamValue("view_cont", this.viewCont);
    if (this.viewContType != null) {
      xd.setParamValue("view_cont_type", this.viewContType.toString());
    }
    xd.setParamValue("error_code", Integer.valueOf(this.errorCode));
    if (this.errorInfo != null) {
      xd.setParamValue("error_info", this.errorInfo);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    String strrt = xd.getParamValueStr("type");
    this.resultType = ResultType.valueOf(strrt);

    this.funcPath = xd.getParamValueStr("biz_func_path");
    this.bizViewPath = xd.getParamValueStr("biz_view_path");
    this.viewCont = xd.getParamValueStr("view_cont");
    this.viewContType = xd.getParamValueStr("view_cont_type");

    this.errorCode = xd.getParamValueInt32("error_code", -1);
    this.errorInfo = xd.getParamValueStr("error_info");
  }

  public static enum ResultType
  {
    run_show_view, 
    run_end, 
    run_error;
  }
}
