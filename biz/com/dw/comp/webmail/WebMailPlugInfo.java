package com.dw.comp.webmail;

import com.dw.biz.BizActionResult;
import com.dw.biz.BizManager;
import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.util.HashMap;

public class WebMailPlugInfo
{
  public static final String MAIL_PLUG_NAME = "name";
  public static final String MAIL_PLUG_LIST_BY_REFS = "show.mailplug.list.by.refs.view";
  public static final String MAIL_PLUG_CHECK_MAIL_RELATED = "check.mail.related";
  public static final String MAIL_PLUG_SET_OR_UNSET_MAIL_RELATED = "set_or_unset.mail.related";
  public static final String MAIL_PLUG_GET_MAIL_RELATED_REFS = "get.mail.related.refs";
  public static final String MAIL_PLUG_SET_MAIL_RELATED_RIGHT_CHECK = "set.mail.related.right.check";
  public static final String MAIL_PLUG_GET_USER_CAN_RELATED_REFMAP = "get.user.can.related.ref_map";
  public static final String MAIL_PLUG_CHECK_USER_CAN_OPEN_MAIL = "check.user.can.open.mail";
  String appName = null;

  HashMap<String, String> appPlugMap = new HashMap();

  public WebMailPlugInfo(String appn, HashMap<String, String> hm)
  {
    this.appName = appn;
    this.appPlugMap.putAll(hm);

    calPath("show.mailplug.list.by.refs.view");
    calPath("check.mail.related");
    calPath("set_or_unset.mail.related");
    calPath("get.mail.related.refs");
    calPath("set.mail.related.right.check");
    calPath("get.user.can.related.ref_map");
    calPath("check.user.can.open.mail");
  }

  private void calPath(String n)
  {
    String s = (String)this.appPlugMap.get(n);
    if (Convert.isNullOrEmpty(s)) {
      return;
    }
    this.appPlugMap.put(n, AppWebConfig.transAbsPath(this.appName, s));
  }

  public String getAppName()
  {
    return this.appName;
  }

  public String getMailPlugName()
  {
    return (String)this.appPlugMap.get("name");
  }

  public String getListByRefsViewPath()
  {
    return (String)this.appPlugMap.get("show.mailplug.list.by.refs.view");
  }

  public String getCheckMailRelatedAction()
  {
    return (String)this.appPlugMap.get("check.mail.related");
  }

  public String getGetMailRlatedRefsAction()
  {
    return (String)this.appPlugMap.get("get.mail.related.refs");
  }

  public String getSetOrUnsetMailRelatedAction()
  {
    return (String)this.appPlugMap.get("set_or_unset.mail.related");
  }

  public String getSetMailRelatedRightCheckAction()
  {
    return (String)this.appPlugMap.get("set.mail.related.right.check");
  }

  public String getGetUserCanRlatedRefMapAction()
  {
    return (String)this.appPlugMap.get("get.user.can.related.ref_map");
  }

  public String getCheckUserCanOpenMailAction()
  {
    return (String)this.appPlugMap.get("check.user.can.open.mail");
  }

  boolean checkUserCanRead(UserProfile up, WebMailItem wmi)
    throws Exception
  {
    if (up == null) {
      return false;
    }
    String actp = getCheckUserCanOpenMailAction();
    if (Convert.isNullOrEmpty(actp)) {
      return false;
    }
    XmlData inputxd = new XmlData();
    inputxd.setParamValue("mail_id", wmi.getMailIdWithInnerBox());
    BizActionResult bar = BizManager.getInstance().RT_doBizAction(up, actp, null, inputxd);
    if (bar == null)
      return false;
    XmlData rxd = bar.getResultData();
    if (rxd == null)
      return false;
    return rxd.getParamValueBool("res", false).booleanValue();
  }
}
