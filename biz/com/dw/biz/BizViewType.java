package com.dw.biz;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.user.UserProfile;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BizViewType
{
  public abstract String getTypeName();

  public abstract String getTypeTitle();

  public abstract void runBizView(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, UserProfile paramUserProfile, String paramString, BizManager paramBizManager, BizTransaction paramBizTransaction, BizView paramBizView, XmlData paramXmlData1, XmlData paramXmlData2, PrintWriter paramPrintWriter)
    throws Exception;

  public abstract BizInOutInfo getInOutInfoByStrCont(String paramString);

  public XmlDataStruct getCtrlXmlDataStruct(String strcont)
  {
    return null;
  }
}
