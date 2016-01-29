package com.dw.biz.platform;

import com.dw.biz.BizActionResult;
import com.dw.biz.BizEvent;
import com.dw.biz.BizManager;
import com.dw.biz.BizOutput;
import com.dw.biz.BizView;
import com.dw.biz.BizView.Controller;
import com.dw.biz.BizView.EventMapper;
import com.dw.biz.BizViewCell.CtrlType;
import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.logger.ILogger;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlVal;
import com.dw.system.xmldata.obj.XmlDataObjHelper;
import com.dw.user.UserProfile;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BizViewRunner
{
  String viewId = null;

  Writer outputWriter = null;

  Hashtable<String, BizView.EventMapper> event2Refresh = new Hashtable();

  Hashtable<String, BizView.EventMapper> event2Output = new Hashtable();

  protected UserProfile userProfile = null;

  protected BizView.Controller cellCtrl = null;

  protected BizView bizView = null;

  protected BizManager bizMgr = null;

  protected XmlData inputXmlData = null;

  protected XmlData outputXmlData = new XmlData();

  protected HttpServletRequest request = null;

  protected HttpServletResponse response = null;

  protected HashMap<String, Object> inputObjMap = null;

  protected HashMap<String, Object> outputObjMap = new HashMap();

  protected DataXManager dataxMgr = null;

  private BizViewRunner parentRunner = null;
  protected ILogger log;
  boolean bLock = false;

  public BizViewRunner getParentRunner()
  {
    return this.parentRunner;
  }

  protected BizViewCellRunner[] getViewCells()
  {
    return null;
  }

  protected BizViewRunner[] getIncludeViews()
  {
    return null;
  }

  public BizViewRunner getIncludeViewById(String id)
  {
    BizViewRunner[] bvrs = getIncludeViews();
    if (bvrs == null) {
      return null;
    }
    for (BizViewRunner bvr : bvrs)
    {
      if (bvr.getViewId().equals(id)) {
        return bvr;
      }
    }
    return null;
  }

  public final void setViewId(String vid)
  {
    this.viewId = vid;
  }

  public final String getViewId()
  {
    return this.viewId;
  }

  public final void setCellCtrl(BizView.Controller fc)
  {
    this.cellCtrl = fc;
    if (this.cellCtrl == null) {
      return;
    }
    BizViewCellRunner[] bvcrs = getViewCells();
    if (bvcrs == null) {
      return;
    }
    for (BizViewCellRunner bvcr : bvcrs)
    {
      String n = bvcr.getCellName();
      BizViewCell.CtrlType ct = fc.getCellCtrlType(n);
      if (ct != BizViewCell.CtrlType.write)
      {
        if (ct == BizViewCell.CtrlType.write_need)
        {
          bvcr.setNullable(false);
        }
        else if (ct == BizViewCell.CtrlType.write_no)
        {
          bvcr.setWriteNo(true);
        }
        else if (ct == BizViewCell.CtrlType.hidden)
        {
          bvcr.setVisiable(false);
        }
        else if (ct == BizViewCell.CtrlType.ignore)
        {
          bvcr.setIgnore(true);
        }
        else if (ct == BizViewCell.CtrlType.read)
        {
          bvcr.setReadonly(true);
        }
      }
    }
  }

  public final BizView.Controller getCellCtrl() {
    return this.cellCtrl;
  }

  public BizViewRunner includeView(HttpServletRequest req, HttpServletResponse resp, String viewpath, XmlData inputxd)
    throws Exception
  {
    return includeView(req, resp, getViewId(), viewpath, inputxd);
  }

  private BizViewRunner includeView(HttpServletRequest req, HttpServletResponse resp, String viewid, String viewpath, XmlData inputxd)
    throws Exception
  {
    viewpath = calAbsPath(viewpath);

    BizViewRunner bvr = this.bizMgr.RT_getBizViewRunner(viewid, viewpath);
    bvr.prepareRunner(req, resp, this.userProfile, this.outputWriter, this.bizMgr, inputxd);
    bvr.runIt();
    return bvr;
  }

  public void includeAjaxView(String viewpath, XmlData inputxd)
    throws Exception
  {
    String uid = UUID.randomUUID().toString().replaceAll("-", "");
    includeAjaxView(null, null, viewpath, uid, inputxd);
  }

  public void includeAjaxView(HttpServletRequest req, HttpServletResponse resp, String viewid, String viewpath, XmlData inputxd)
    throws Exception
  {
    viewpath = calAbsPath(viewpath);

    BizViewRunner bvr = this.bizMgr.RT_getBizViewRunner(viewid, viewpath);
    bvr.prepareRunner(req, resp, this.userProfile, this.outputWriter, this.bizMgr, inputxd);
    bvr.renderMeAjaxInclude(this.outputWriter);
  }

  protected XmlData callAction(String actionpath, XmlData inputxd)
    throws Exception
  {
    if (!actionpath.startsWith("/"))
    {
      int p = getViewPath().lastIndexOf('/');
      actionpath = getViewPath().substring(0, p + 1) + actionpath;
    }

    BizActionResult bar = this.bizMgr.RT_doBizAction(this.userProfile, actionpath, null, inputxd);

    return bar.getResultData();
  }

  public String calAbsPath(String path)
  {
    if (!path.startsWith("/"))
    {
      int p = getViewPath().lastIndexOf('/');
      path = getViewPath().substring(0, p + 1) + path;
    }

    return path;
  }

  public void setEventRefresh(BizView.EventMapper er)
  {
    this.event2Refresh.put(er.getEventName(), er);
  }

  public void setEventOutput(BizView.EventMapper er)
  {
    this.event2Output.put(er.getEventName(), er);
  }

  public BizView.EventMapper[] getAllEventRefreshers()
  {
    BizView.EventMapper[] rets = new BizView.EventMapper[this.event2Refresh.size()];
    this.event2Refresh.values().toArray(rets);
    return rets;
  }

  public BizView.EventMapper[] getAllEventOutputs()
  {
    BizView.EventMapper[] rets = new BizView.EventMapper[this.event2Refresh.size()];
    this.event2Output.values().toArray(rets);
    return rets;
  }

  public boolean canHasAjaxEvent(String eventname)
  {
    if (this.event2Refresh != null)
    {
      if (this.event2Refresh.containsKey(eventname)) {
        return true;
      }
    }
    if (this.event2Output != null)
    {
      if (this.event2Output.containsKey(eventname)) {
        return true;
      }
    }
    return false;
  }

  public String getRootViewPath()
  {
    if (this.parentRunner == null) {
      return getViewPath();
    }
    return this.parentRunner.getRootViewPath();
  }

  public String calculateAjaxPostEventUrl(String eventname, XmlData eventxd, String otherp)
    throws Exception
  {
    if (canHasAjaxEvent(eventname)) {
      return "javascript:void";
    }
    BizViewRunner tmpbvr = this;
    if (this.parentRunner != null) {
      tmpbvr = this.parentRunner;
    }
    StringBuffer tmpsb = new StringBuffer();
    tmpsb.append("javascript:biz_view_event_post('")
      .append(getViewId())
      .append("','")
      .append(eventname)
      .append("','");

    if (eventxd != null)
    {
      Properties ps = eventxd.toProp();

      for (Enumeration en = ps.propertyNames(); en.hasMoreElements(); )
      {
        String pn = (String)en.nextElement();
        String pv = ps.getProperty(pn);
        tmpsb.append("&dx_").append(pn).append("=").append(pv);
      }
    }

    if (otherp != null) {
      tmpsb.append("&").append(otherp);
    }
    tmpsb.append("')");
    return tmpsb.toString();
  }

  public String calculateAjaxPostSelfUrl(XmlData post_xd) throws Exception
  {
    return calculateAjaxPostSelfUrl(post_xd, null);
  }

  public String calculateAjaxPostSelfUrl(XmlData post_xd, String app_param) throws Exception
  {
    StringBuffer tmpsb = new StringBuffer();
    tmpsb.append("javascript:ajaxPostSelfToServer('")
      .append(getViewId()).append("','").append(getViewPath()).append("','");

    if (post_xd != null)
    {
      Properties ps = post_xd.toProp();

      for (Enumeration en = ps.propertyNames(); en.hasMoreElements(); )
      {
        String pn = (String)en.nextElement();
        String pv = ps.getProperty(pn);
        tmpsb.append("&dx_").append(pn).append("=").append(pv);
      }
    }

    if (app_param != null)
    {
      tmpsb.append("&").append(app_param);
    }

    tmpsb.append("')");
    return tmpsb.toString();
  }

  public final void fireOutput(String outputn, XmlData xd)
  {
  }

  public void lock()
  {
    this.bLock = true;
  }

  public boolean isLock()
  {
    return this.bLock;
  }

  public void unlock()
  {
    this.bLock = false;
  }

  public String getRootViewId()
  {
    if (this.parentRunner == null) {
      return getViewId();
    }
    return this.parentRunner.getRootViewId();
  }

  public void setRenderReadOnly()
  {
    BizViewCellRunner[] vcrs = getViewCells();
    if (vcrs == null) {
      return;
    }
    for (BizViewCellRunner vcr : vcrs)
    {
      vcr.setReadonly(true);
    }
  }

  public void runIt()
    throws Exception
  {
    setViewId(getRootViewId());

    prepareRenderView(false);

    render(this.outputWriter);
  }

  public void runItAjax()
    throws Exception
  {
    prepareRenderView(true);

    renderMeAjaxInclude(this.outputWriter);
  }

  public final void renderMeAjaxInclude(Writer w)
    throws Exception
  {
    w.write("<div><div id='");
    w.write(getViewId());
    w.write("'><div id='" + getViewId() + "_ctrl'>");
    for (BizView.EventMapper bver : this.event2Refresh.values())
    {
      w.write("<input type=\"hidden\" id=\"event_refresh_" + 
        getViewId() + "\" event_name=\"" + 
        bver.getEventName() + "\" tar_view_ids=\"" + 
        bver.getRelatedViewIdStr() + "\" map_method=\"" + 
        bver.getMapMethod() + "\"/>");
    }

    for (BizView.EventMapper bver : this.event2Output.values())
    {
      w.write("<input type=\"hidden\" id=\"event_output_" + getViewId() + 
        "\" event_name=\"" + bver.getEventName() + 
        "\" output_name=\"" + bver.getOutputName() + 
        "\" rel_view_ids=\"" + bver.getRelatedViewIdStr() + 
        "\" map_method=\"" + bver.getMapMethod() + "\"/>");
    }

    w.write("<input type=\"hidden\" id=\"view_path_" + getViewId() + "\" view_path=\"" + getViewPath() + "\"/>");
    w.write("</div>");
    w.write("<div id='" + getViewId() + "_content'>");
    w.write("</div>");
    w.write("</div></div><script>");

    w.write("includeAjaxView('");
    w.write(getViewId());
    w.write("','");
    w.write(getViewPath());
    w.write("','");
    w.write(toViewStateXmlData().toHexString());
    w.write("');");
    w.write("</script>");
  }

  public abstract void render(Writer paramWriter)
    throws Exception;

  public abstract int getWriteLineNumber();

  public abstract String getViewPath();

  public void prepareRunner(HttpServletRequest req, HttpServletResponse resp, UserProfile up, Writer output, BizManager bm, XmlData inputxd)
    throws Exception
  {
    this.request = req;
    this.response = resp;
    this.userProfile = up;
    this.inputXmlData = inputxd;
    if (this.viewId == null) {
      this.viewId = UUID.randomUUID().toString().replaceAll("-", "");
    }

    BizViewCellRunner[] bvcrs = getViewCells();
    XmlVal xv;
    if (bvcrs != null)
    {
      for (BizViewCellRunner r : bvcrs)
      {
        r.belongToViewRunner = this;

        r.prepareRunner(up, output, bm, null, null, null);
        if (inputxd != null)
        {
          xv = inputxd.getParamXmlVal(r.getCellName());
          if (xv != null) {
            r.setStrValue(xv.getStrVal());
          }
        }
      }
    }
    BizViewRunner[] bvrs = getIncludeViews();
    if (bvrs != null)
    {
      for (BizViewRunner r : bvrs)
      {
        r.parentRunner = this;

        r.prepareRunner(req, resp, up, output, bm, inputxd);
      }
    }

    this.outputWriter = output;

    this.bizMgr = bm;
    this.dataxMgr = bm.getDataXManager();

    this.bizView = bm.getBizViewByPath(getViewPath());
  }

  protected void prepareRenderView(boolean render_ajax)
    throws Exception
  {
  }

  public XmlData getOutputXmlData()
  {
    return this.outputXmlData;
  }

  public HashMap<String, Object> getOutputObjMap()
  {
    return this.outputObjMap;
  }

  public XmlDataStruct getRunnerInputXmlDataStruct()
  {
    return XmlDataObjHelper.extractXDSFromClass(getClass());
  }

  public String getPortalEditUrl(XmlData inputxd)
  {
    return null;
  }

  public boolean checkPortalEditRight(HttpServletRequest req, UserProfile up, StringBuilder failsb)
  {
    return true;
  }

  public BizEvent[] getRunnerEvents()
  {
    return null;
  }

  public BizOutput[] getRunnerOutputs()
  {
    return null;
  }

  public void setLog(ILogger log)
  {
    this.log = log;
  }

  public XmlData toViewStateXmlData()
    throws Exception
  {
    XmlData xd = XmlDataObjHelper.extractXmlDataFromObj(this);

    return xd;
  }

  public void fromViewStateXmlData(XmlData xd)
    throws Exception
  {
    this.inputXmlData = xd;
    if (this.inputXmlData != null)
    {
      XmlDataObjHelper.injectXmlDataToObj(this, this.inputXmlData);
    }
  }

  public void bindViewXmlData(XmlData xd) throws Exception
  {
    if (xd == null) {
      return;
    }
    fromViewStateXmlData(xd);
  }

  public void bindViewData(Object obj)
    throws Exception
  {
    if (obj == null) {
      return;
    }
    XmlData xd = XORMUtil.extractXmlDataFromObj(obj);
    bindViewXmlData(xd);
  }

  public final String getModuleName()
  {
    ClassLoader cl = getClass().getClassLoader();
    if ((cl instanceof BizClassLoader))
      cl = cl.getParent();
    AppInfo ai = CompManager.getInstance().getAppInfo(cl);
    if (ai == null) {
      return null;
    }
    return ai.getContextName();
  }
}
