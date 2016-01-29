package com.dw.biz.client.biz;

import com.dw.biz.api.BizFuncRunResult;
import com.dw.biz.client.WFClient;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XmlDataAndMethodCaller;

public class BizFuncClient
{
  public static BizFuncRunResult runBizFunc(String username, WFClient wfc, String funcpath, XmlData inputxd)
    throws Exception
  {
    XmlData xd = 
      XmlDataAndMethodCaller.packClientMethodParams(new Object[] { funcpath, inputxd });

    XmlData retxd = wfc.sendLoginUserXmlCmd(username, -1, "run_biz_func", xd);

    return (BizFuncRunResult)
      XmlDataAndMethodCaller.unpackObjFromXmlData(retxd);
  }

  public static BizFuncRunResult runBizFuncBizViewSubmit(String username, WFClient wfc, String funcpath, String bizviewpath, String oper, XmlData submitxd)
    throws Exception
  {
    XmlData xd = 
      XmlDataAndMethodCaller.packClientMethodParams(new Object[] { funcpath, 
      bizviewpath, oper, submitxd });

    XmlData retxd = wfc.sendLoginUserXmlCmd(username, -1, "run_biz_func_view_submit", xd);

    return (BizFuncRunResult)
      XmlDataAndMethodCaller.unpackObjFromXmlData(retxd);
  }

  public static String[] getBizFuncNameByUser(WFClient wfc, String username)
    throws Exception
  {
    XmlData xd = 
      XmlDataAndMethodCaller.packClientMethodParams(new Object[] { username });

    XmlData retxd = wfc.sendXmlCmd("get_biz_func_by_user", xd);

    Object[] objs = (Object[])
      XmlDataAndMethodCaller.unpackObjFromXmlData(retxd);

    if (objs == null) {
      return null;
    }
    String[] rets = new String[objs.length];
    System.arraycopy(objs, 0, rets, 0, rets.length);
    return rets;
  }
}
