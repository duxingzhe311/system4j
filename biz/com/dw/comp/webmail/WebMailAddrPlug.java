package com.dw.comp.webmail;

import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import org.w3c.dom.Element;

public class WebMailAddrPlug
{
  public static final String ATTRN_NAME = "name";
  public static final String ATTRN_TITLE = "title";
  public static final String ATTRN_ACTION = "action";
  String appName = null;

  String name = null;

  String title = null;

  String action = null;

  public static WebMailAddrPlug createIns(String appn, Element ele)
  {
    if (Convert.isNullOrEmpty(appn)) {
      return null;
    }
    WebMailAddrPlug ret = new WebMailAddrPlug();
    ret.appName = appn;
    ret.name = ele.getAttribute("name");
    ret.title = ele.getAttribute("title");
    ret.action = ele.getAttribute("action");

    ret.action = AppWebConfig.transAbsPath(appn, ret.action);

    return ret;
  }

  public String getAppName()
  {
    return this.appName;
  }

  public String getName()
  {
    return this.name;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getAction()
  {
    return this.action;
  }
}
