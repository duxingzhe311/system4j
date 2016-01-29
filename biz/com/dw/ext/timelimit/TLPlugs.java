package com.dw.ext.timelimit;

import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import java.util.Collection;
import java.util.HashMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class TLPlugs
{
  public static final String ATTRN_ADD_OR_EDIT = "add_or_edit_url";
  public static final String ATTRN_SHOW_VIEW = "show_view";
  public static final String ATTRN_CHECK_ACTION = "check_action";
  public static final String ATTRN_FINISH_ACTION = "finish_action";
  public static final String ATTRN_FINISH_CHK_ACTION = "finish_check_action";
  public static final String ATTRN_DELAY_ACTION = "delay_action";
  public static final String ATTRN_BROKEN_ACTION = "broken_action";
  public static final String ATTRN_REMIND_ACTION = "remind_action";
  public static final String ATTRN_GET_APP_TITLE_ACTION = "get_app_title_action";
  String appName = null;

  String addOrEditUrl = null;

  String defShowView = null;
  String defCheckAction = null;
  String defBrokenAction = null;
  String defFinishAction = null;
  String defFinishChkAction = null;
  String defDelayAction = null;
  String defRemindAction = null;

  String appGetTitleAction = null;

  TLPlug defaultPlug = new TLPlug(null);

  HashMap<String, TLPlug> type2Plug = new HashMap();

  private static String getAttrVINEle(Element ele, String attrn, String defv)
  {
    Attr a = ele.getAttributeNode(attrn);
    if (a == null) {
      return defv;
    }
    return a.getValue();
  }

  private static String getAbsPathInEleAttr(String appn, Element ele, String attrn, String defv)
  {
    String p = getAttrVINEle(ele, attrn, defv);
    return AppWebConfig.transAbsPath(appn, p);
  }

  TLPlugs(String appn, Element ele)
  {
    this.appName = appn;

    this.addOrEditUrl = getAbsPathInEleAttr(appn, ele, "add_or_edit_url", null);
    this.defaultPlug.showView = (this.defShowView = getAbsPathInEleAttr(appn, ele, "show_view", null));
    this.defaultPlug.checkAction = (this.defCheckAction = getAbsPathInEleAttr(appn, ele, "check_action", null));
    this.defaultPlug.brokenAction = (this.defBrokenAction = getAbsPathInEleAttr(appn, ele, "broken_action", null));
    this.defaultPlug.finishAction = (this.defFinishAction = getAbsPathInEleAttr(appn, ele, "finish_action", null));
    this.defaultPlug.finishChkAction = (this.defFinishChkAction = getAbsPathInEleAttr(appn, ele, "finish_check_action", null));
    this.defaultPlug.delayAction = (this.defDelayAction = getAbsPathInEleAttr(appn, ele, "delay_action", null));
    this.defaultPlug.remindAction = (this.defRemindAction = getAbsPathInEleAttr(appn, ele, "remind_action", null));

    this.appGetTitleAction = getAbsPathInEleAttr(appn, ele, "get_app_title_action", null);

    Element[] eles = Convert.getSubChildElement(ele, "timelimit_plug");
    if (eles == null) {
      return;
    }
    for (Element tmpe : eles)
    {
      TLPlug t = new TLPlug(appn, tmpe, null);
      this.type2Plug.put(t.type, t);
    }
  }

  public String getAppName()
  {
    return this.appName;
  }

  public String getAddOrEditUrl()
  {
    return this.addOrEditUrl;
  }

  public String getAppGetTitleAction()
  {
    return this.appGetTitleAction;
  }

  public TLPlug[] getAllPlug()
  {
    TLPlug[] rets = new TLPlug[this.type2Plug.size()];
    this.type2Plug.values().toArray(rets);
    return rets;
  }

  public TLPlug getPlugByType(String t)
  {
    return (TLPlug)this.type2Plug.get(t);
  }

  public class TLPlug
  {
    String type = null;
    String desc = null;

    String showView = null;

    String checkAction = null;

    String brokenAction = null;

    String finishAction = null;

    String finishChkAction = null;

    String delayAction = null;

    String remindAction = null;

    private TLPlug()
    {
    }

    private TLPlug(String appn, Element ele) {
      this.type = ele.getAttribute("type");
      this.desc = ele.getAttribute("desc");

      this.showView = TLPlugs.getAbsPathInEleAttr(appn, ele, "show_view", TLPlugs.this.defShowView);
      this.checkAction = TLPlugs.getAbsPathInEleAttr(appn, ele, "check_action", TLPlugs.this.defCheckAction);
      this.brokenAction = TLPlugs.getAbsPathInEleAttr(appn, ele, "broken_action", TLPlugs.this.defBrokenAction);
      this.finishAction = TLPlugs.getAbsPathInEleAttr(appn, ele, "finish_action", TLPlugs.this.defFinishAction);
      this.finishChkAction = TLPlugs.getAbsPathInEleAttr(appn, ele, "finish_check_action", TLPlugs.this.defFinishChkAction);
      this.delayAction = TLPlugs.getAbsPathInEleAttr(appn, ele, "delay_action", TLPlugs.this.defDelayAction);
      this.remindAction = TLPlugs.getAbsPathInEleAttr(appn, ele, "remind_action", TLPlugs.this.defRemindAction);
    }

    public String getTypeName()
    {
      return this.type;
    }

    public String getDesc()
    {
      return this.desc;
    }

    public String getShowViewPath()
    {
      return this.showView;
    }

    public String getCheckActionPath()
    {
      return this.checkAction;
    }

    public String getBrokenActionPath()
    {
      return this.brokenAction;
    }

    public String getFinishActionPath()
    {
      return this.finishAction;
    }

    public String getFinishChkActionPath()
    {
      return this.finishChkAction;
    }

    public String getRemindActionPath()
    {
      return this.remindAction;
    }

    public String getDelayActionPath()
    {
      return this.delayAction;
    }
  }
}
