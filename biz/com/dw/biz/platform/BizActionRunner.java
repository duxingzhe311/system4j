package com.dw.biz.platform;

import com.dw.biz.AbstractBizRenderCtrl;
import com.dw.biz.BizActionResult;
import com.dw.biz.BizManager;
import com.dw.biz.BizTransaction;
import com.dw.biz.IBizEnv;
import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.system.gdb.datax.DataXClass;
import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.logger.ILogger;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.user.UserProfile;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.UUID;

public abstract class BizActionRunner
{
  Writer outputWriter = null;

  protected BizTransaction transaction = null;

  protected UserProfile userProfile = null;

  protected AbstractBizRenderCtrl renderCtrl = null;

  protected BizTransaction bizTransaction = null;

  protected String bizItemPath = null;

  protected BizManager bizMgr = null;

  protected IBizEnv bizEnv = null;

  protected XmlData inputXmlData = null;

  protected XmlData outputXmlData = new XmlData();

  protected HashMap<String, Object> inputObjMap = null;

  protected HashMap<String, Object> outputObjMap = new HashMap();

  protected XmlData ctrlXmlData = null;

  protected DataXManager dataxMgr = null;
  protected ILogger log;
  boolean bLock = false;

  protected long addDataX(String dataxbase, String dataxclass, XmlData xd)
    throws Exception
  {
    if (xd == null) {
      return -1L;
    }
    DataXClass dxc = this.dataxMgr.getDataXClass(dataxbase, dataxclass);
    if (dxc == null) {
      throw new Exception("cannot get DataXClass=" + dataxbase + "." + 
        dataxclass);
    }

    long nid = dxc.addNewXmlData(-1L, xd);
    return nid;
  }

  protected void updateDataX(String dataxbase, String dataxclass, long xdid, XmlData xd)
    throws Exception
  {
    if (xd == null) {
      return;
    }
    DataXClass dxc = this.dataxMgr.getDataXClass(dataxbase, dataxclass);
    if (dxc == null) {
      throw new Exception("cannot get DataXClass=" + dataxbase + "." + 
        dataxclass);
    }

    dxc.saveXmlData(xdid, -1L, xd);
  }

  protected void deleteDataX(String dataxbase, String dataxclass, long xdid)
    throws Exception
  {
    DataXClass dxc = this.dataxMgr.getDataXClass(dataxbase, dataxclass);
    if (dxc == null) {
      throw new Exception("cannot get DataXClass=" + dataxbase + "." + 
        dataxclass);
    }
    dxc.saveXmlData(xdid, -1L, null);
  }

  protected String calLinkOutputUrl(String oper, XmlData outputxd)
  {
    return "";
  }

  protected void includeView(String viewpath, XmlData inputxd)
    throws Exception
  {
  }

  protected void includeView(String viewpath, String viewid, XmlData inputxd)
    throws Exception
  {
    viewpath = calAbsPath(viewpath);

    BizViewRunner bvr = this.bizMgr.RT_getBizViewRunner(viewid, viewpath);
    bvr.renderMeAjaxInclude(this.outputWriter);
  }

  protected void includeViewCell(String name, String viewpath, XmlData inputval, Hashtable<String, String> output2method)
    throws Exception
  {
    if (!viewpath.startsWith("/"))
    {
      int p = this.bizItemPath.lastIndexOf('/');
      viewpath = this.bizItemPath.substring(0, p + 1) + viewpath;
    }

    XmlData ctrlxd = new XmlData();
    ctrlxd.setParamValue("cell_val_path", "/" + name);
    XmlData inputxd = new XmlData();
  }

  protected void includeAjaxView(String viewpath, XmlData ctrlxd, XmlData inputxd)
    throws Exception
  {
    if (this.renderCtrl == null) {
      return;
    }
    viewpath = calAbsPath(viewpath);
    String uid = UUID.randomUUID().toString().replaceAll("-", "");
    this.renderCtrl.renderIncludeAjaxView(uid, this.outputWriter, viewpath, ctrlxd, 
      inputxd);
  }

  protected XmlData callAction(String actionpath, XmlData inputxd)
    throws Exception
  {
    if (!actionpath.startsWith("/"))
    {
      int p = this.bizItemPath.lastIndexOf('/');
      actionpath = this.bizItemPath.substring(0, p + 1) + actionpath;
    }

    BizActionResult bar = this.bizMgr.RT_doBizAction(this.userProfile, actionpath, null, inputxd);

    return bar.getResultData();
  }

  public String calAbsPath(String path)
  {
    if (!path.startsWith("/"))
    {
      int p = this.bizItemPath.lastIndexOf('/');
      path = this.bizItemPath.substring(0, p + 1) + path;
    }

    return path;
  }

  public String renderCellViewOutputUrl(String outputname, XmlData xd)
    throws Exception
  {
    return null;
  }

  public String renderOpenDlgUrl(String viewpath, XmlData ctrlxd, XmlData inputxd, String ss)
    throws Exception
  {
    return null;
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

  public void runIt() throws Exception
  {
    render(this.outputWriter);
  }

  public abstract void render(Writer paramWriter)
    throws Exception;

  public abstract int getWriteLineNumber();

  public void prepareRunner(UserProfile up, AbstractBizRenderCtrl rc, PrintWriter output, BizManager bm, IBizEnv env, String biz_itempath, BizTransaction bt, XmlData ctrlxd, XmlData inputxd, HashMap<String, Object> inputom)
  {
    this.renderCtrl = rc;

    this.outputWriter = output;

    this.bizMgr = bm;
    this.userProfile = up;
    this.dataxMgr = bm.getDataXManager();
    this.bizEnv = env;
    this.bizItemPath = biz_itempath;
    this.bizTransaction = bt;
    this.ctrlXmlData = ctrlxd;
    this.inputXmlData = inputxd;
    this.inputObjMap = inputom;
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
    return null;
  }

  public XmlDataStruct getRunnerOutputXmlDataStruct()
  {
    return null;
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

  public void setLog(ILogger log)
  {
    this.log = log;
  }
}
