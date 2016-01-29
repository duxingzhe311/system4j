package com.dw.ext.timelimit;

import com.dw.biz.BizActionResult;
import com.dw.biz.BizManager;
import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GDBTransInThread;
import com.dw.system.gdb.GdbException;
import com.dw.system.gdb.IDBSelectObjCallback;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;

public class TLManager
{
  private static Object locker = new Object();

  private static TLManager instance = null;

  private static ILogger log = LoggerManager.getLogger(TLManager.class);

  HashMap<String, TLPlugs> appName2TLP = new HashMap();

  IDBSelectObjCallback<TLItem> remindTLItemStCB = new IDBSelectObjCallback()
  {
    public boolean onFindObj(int rowidx, TLItem o)
    {
      if (o == null) {
        return true;
      }
      TLPlugs.TLPlug ttp = TLManager.this.getPlug(o.getAppName(), o.getPlugType());
      String actp = ttp.getRemindActionPath();
      if (Convert.isNullOrEmpty(actp)) {
        return true;
      }
      try
      {
        XmlData xd = XORMUtil.extractXmlDataFromObj(o);
        BizManager.getInstance().RT_doBizAction(null, actp, null, xd);
        return true;
      }
      catch (Exception ee)
      {
        if (TLManager.log.isErrorEnabled())
          TLManager.log.equals(ee);
      }
      return true;
    }
  };

  IDBSelectObjCallback<TLItem> checkTLItemStCB = new IDBSelectObjCallback()
  {
    public boolean onFindObj(int rowidx, TLItem o)
    {
      if (o == null) {
        return true;
      }
      TLPlugs.TLPlug p = o.getRelatedPlug();
      String chkp = p.getCheckActionPath();
      if (Convert.isNullOrEmpty(chkp)) {
        return true;
      }
      try
      {
        XmlData xd = XORMUtil.extractXmlDataFromObj(o);
        BizActionResult bar = BizManager.getInstance().RT_doBizAction(null, chkp, null, xd);
        if (bar == null) {
          return true;
        }
        xd = bar.getResultData();
        if (xd == null) {
          return true;
        }
        int st = xd.getParamValueInt32("State", -1);
        if (st < 0) {
          return true;
        }
        TLItem.State nst = TLItem.State.valueOf(st);
        if (nst == null) {
          return true;
        }
        if (o.getState() == nst) {
          return true;
        }

        GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
          Long.valueOf(o.getTLId()), TLItem.class, 
          new String[] { "State" }, 
          new Object[] { Integer.valueOf(nst.getValue()) });
        return true;
      }
      catch (Exception ee)
      {
        if (TLManager.log.isErrorEnabled())
          TLManager.log.equals(ee);
      }
      return true;
    }
  };

  public static TLManager getInstance()
  {
    if (instance != null) {
      return instance;
    }
    synchronized (locker)
    {
      if (instance != null) {
        return instance;
      }
      instance = new TLManager();
      return instance;
    }
  }

  private TLManager()
  {
    for (AppWebConfig awc : AppWebConfig.getModuleWebConfigAll())
    {
      Element ele = awc.getConfElement("timelimit_plugs");
      if (ele != null)
      {
        TLPlugs ps = new TLPlugs(awc.getModuleName(), ele);
        this.appName2TLP.put(ps.getAppName(), ps);
      }
    }
  }

  public TLPlugs getPlugsByApp(String appn) {
    return (TLPlugs)this.appName2TLP.get(appn);
  }

  public TLPlugs[] getAllPlugs()
  {
    TLPlugs[] rets = new TLPlugs[this.appName2TLP.size()];
    this.appName2TLP.values().toArray(rets);
    return rets;
  }

  public TLPlugs.TLPlug getPlug(String appn, String type)
  {
    if (appn == null) {
      return null;
    }
    TLPlugs ps = (TLPlugs)this.appName2TLP.get(appn);
    if (ps == null) {
      return null;
    }
    if (Convert.isNullOrEmpty(type)) {
      return ps.defaultPlug;
    }
    return ps.getPlugByType(type);
  }

  public TLItem getTimeLimitById(long lpid)
    throws GdbException, Exception
  {
    return (TLItem)GDB.getInstance().getXORMObjByPkId(TLItem.class, Long.valueOf(lpid));
  }

  public List<TLItem> listTimeLimitByUser(String username, TLItem.State st)
    throws Exception
  {
    return GDB.getInstance().listXORMAsObjList(TLItem.class, 
      "Owner='" + username + "' and State=" + st.getValue(), 
      "LimitDate", 0, -1);
  }

  public List<TLInAppItem> listTLInAppByUser(String user, TLItem.State[] st)
    throws Exception
  {
    if (st == null)
      throw new IllegalArgumentException("no state input!");
    Hashtable ht = new Hashtable();
    ht.put("@User", user);
    String ststr = st[0].getValue();
    for (int i = 1; i < st.length; i++)
      ststr = ststr + "," + st[i].getValue();
    ht.put("$States", ststr);
    List rets = GDB.getInstance().accessDBPageAsXORMObjList(
      "TimeLimit.ListUserAppSummary", ht, TLInAppItem.class, 0, -1);

    if ((rets == null) || (rets.size() <= 0)) {
      return rets;
    }
    setTitles(rets);
    return rets;
  }

  public List<TLInAppItem> listTLInAppByMonitor(String monitor, boolean bmgr, TLItem.State[] st)
    throws Exception
  {
    if (st == null)
      throw new IllegalArgumentException("no state input!");
    Hashtable ht = new Hashtable();
    ht.put("@Monitor", monitor);
    int mgr = bmgr ? 1 : 0;
    ht.put("@IsMgr", Integer.valueOf(mgr));
    String ststr = st[0].getValue();
    for (int i = 1; i < st.length; i++)
      ststr = ststr + "," + st[i].getValue();
    ht.put("$States", ststr);
    List rets = GDB.getInstance().accessDBPageAsXORMObjList(
      "TimeLimit.ListMonitorAppSummary", ht, TLInAppItem.class, 0, -1);

    if ((rets == null) || (rets.size() <= 0)) {
      return rets;
    }
    setTitles(rets);
    return rets;
  }

  private void setTitles(List<TLInAppItem> rets) throws Exception
  {
    HashMap appn2ids = new HashMap();
    HashMap appid2item = new HashMap();
    for (TLInAppItem tli : rets)
    {
      String appn = tli.getAppName();
      ArrayList ids = (ArrayList)appn2ids.get(appn);
      if (ids == null)
      {
        ids = new ArrayList();
        appn2ids.put(appn, ids);
      }
      String appid = tli.getAppId();
      ids.add(appid);

      appid2item.put(appn + "_" + appid, tli);
    }

    for (Map.Entry n2ids : appn2ids.entrySet())
    {
      String appn = (String)n2ids.getKey();
      TLPlugs tlps = getPlugsByApp(appn);
      if (tlps != null)
      {
        String actp = tlps.getAppGetTitleAction();
        if (!Convert.isNullOrEmpty(actp))
        {
          XmlData xd = new XmlData();
          xd.setParamValues("AppIds", (List)n2ids.getValue());
          BizActionResult bar = BizManager.getInstance().RT_doBizAction(null, actp, null, xd);
          if (bar != null)
          {
            XmlData oxd = bar.getResultData();
            if (oxd != null)
            {
              String[] outids = oxd.getParamNames();
              if (outids != null)
              {
                for (String oid : outids)
                {
                  TLInAppItem tli = (TLInAppItem)appid2item.get(appn + "_" + oid);
                  if (tli != null)
                  {
                    tli.appTitle = oxd.getParamValueStr(oid);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  public List<TLItem> listTimeLimitByMonitor(String username, boolean ismgr, TLItem.State[] st) throws Exception {
    if (st == null)
      throw new IllegalArgumentException("no state input!");
    Hashtable ht = new Hashtable();
    ht.put("@Monitor", username);
    int mgr = ismgr ? 1 : 0;
    ht.put("@IsMgr", Integer.valueOf(mgr));
    String ststr = st[0].getValue();
    for (int i = 1; i < st.length; i++)
      ststr = ststr + "," + st[i].getValue();
    ht.put("$States", ststr);
    return GDB.getInstance().accessDBPageAsXORMObjList(
      "TimeLimit.ListTimeLimitByMonitor", ht, TLItem.class, 0, -1);
  }

  public List<TLItem> listTimeLimitByAppId(String appname, String appid)
    throws Exception
  {
    return GDB.getInstance().listXORMAsObjList(TLItem.class, 
      "AppName='" + appname + "' and AppId='" + appid + "'", 
      "LimitDate", 0, -1);
  }

  public List<TLItem> listTimeLimitByUserAppId(String appname, String appid, String usern, TLItem.State[] st)
    throws Exception
  {
    if (st == null)
      throw new IllegalArgumentException("no state input!");
    Hashtable ht = new Hashtable();
    ht.put("@AppName", appname);
    ht.put("@AppId", appid);
    ht.put("@User", usern);
    String ststr = st[0].getValue();
    for (int i = 1; i < st.length; i++)
      ststr = ststr + "," + st[i].getValue();
    ht.put("$States", ststr);
    return GDB.getInstance().accessDBPageAsXORMObjList(
      "TimeLimit.ListTimeLimitByUserAppId", ht, TLItem.class, 0, -1);
  }

  public List<TLItem> listTimeLimitByMonitorAppId(String appname, String appid, String monitor, boolean ismgr, TLItem.State[] st)
    throws Exception
  {
    if (st == null)
      throw new IllegalArgumentException("no state input!");
    Hashtable ht = new Hashtable();
    ht.put("@AppName", appname);
    ht.put("@AppId", appid);
    int mgr = ismgr ? 1 : 0;
    ht.put("@IsMgr", Integer.valueOf(mgr));
    ht.put("@Monitor", monitor);
    String ststr = st[0].getValue();
    for (int i = 1; i < st.length; i++)
      ststr = ststr + "," + st[i].getValue();
    ht.put("$States", ststr);
    return GDB.getInstance().accessDBPageAsXORMObjList(
      "TimeLimit.ListTimeLimitByMonitorAppId", ht, TLItem.class, 0, -1);
  }

  public List<TLItem> listTimeLimitByAppTag(String appname, String plugtype, String appid, String apptag)
    throws Exception
  {
    Hashtable ht = new Hashtable();

    ht.put("@AppName", appname);
    if (Convert.isNotNullEmpty(plugtype))
      ht.put("@PlugType", plugtype);
    ht.put("@AppTag", apptag);
    ht.put("@AppId", appid);

    return GDB.getInstance().accessDBPageAsXORMObjList(
      "TimeLimit.ListTimeLimitByTag", ht, TLItem.class, 0, -1);
  }

  public boolean checkExistedByAppTag(String appname, String plugtype, String appid, String apptag)
    throws Exception
  {
    List lis = listTimeLimitByAppTag(
      appname, plugtype, appid, apptag);
    if ((lis == null) || (lis.size() <= 0)) {
      return false;
    }
    return true;
  }

  public long addTimeLimit(TLItem ctlp)
    throws Exception
  {
    return ((Long)GDB.getInstance().addXORMObjWithNewId(ctlp)).longValue();
  }

  public long copyAddTimeLimit(String title, Date timelimit, TLItem tobecopy)
    throws Exception
  {
    if (Convert.isNullOrEmpty(title)) {
      title = tobecopy.title;
    }
    TLItem ntl = new TLItem(tobecopy.appName, tobecopy.plugType, tobecopy.appId, 
      tobecopy.owner, tobecopy.assistant, tobecopy.monitor, 
      title, tobecopy.desc, 
      timelimit, 
      tobecopy.beforeRemindDay, 
      tobecopy.preAfterEffect);

    return addTimeLimit(ntl);
  }

  public void deleteTimeLimitById(long lpid)
    throws GdbException, Exception
  {
    GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(lpid), TLItem.class);
  }

  public boolean changeTimeLimitMonitor(long tlid, String monitor)
    throws GdbException, Exception
  {
    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(tlid), TLItem.class, new String[] { "Monitor" }, new Object[] { monitor });
  }

  public boolean changeTimeLimitOwner(long tlid, String owner, String assistant)
    throws Exception
  {
    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "Owner", "Assistant" }, 
      new Object[] { owner, assistant });
  }

  public boolean changeTimeLimitInfo(long tlid, String title, String desc, Date tl, TLItem.AfterEffect pre)
    throws Exception
  {
    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "Title", "LimitDate", "Description", "PreAfterEffect" }, 
      new Object[] { title, tl, desc, Integer.valueOf(pre.getValue()) });
  }

  public boolean changeTimeLimitRealAffect(long tlid, TLItem.AfterEffect realae)
    throws Exception
  {
    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "RealAfterEffect" }, 
      new Object[] { Integer.valueOf(realae.getValue()) });
  }

  public boolean applyFinishTimeLimitPoint(long tlid, String finishdesc, long finishcost, StringBuilder failedreson)
    throws GdbException, Exception
  {
    Date date = new Date();
    TLItem tlp = getTimeLimitById(tlid);
    if (tlp == null) {
      throw new Exception("no time limit point found with id=" + tlid);
    }
    if (tlp.getState() != TLItem.State.Normal) {
      throw new Exception("time limit point is not normal!");
    }

    Date fd = new Date();
    boolean bres = GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "ApplyFinishDate", "FinishDesc", "FinishCostSec", "State" }, 
      new Object[] { fd, finishdesc, Long.valueOf(finishcost), Integer.valueOf(TLItem.State.ApplyFinish.getValue()) });

    if (!bres) {
      return false;
    }
    return bres;
  }

  public boolean confirmFinishTimeLimitPoint(long tlid, boolean isfinish)
    throws GdbException, Exception
  {
    Date date = new Date();
    TLItem tlp = getTimeLimitById(tlid);
    if (tlp == null) {
      throw new Exception("no time limit point found with id=" + tlid);
    }
    TLItem.State st = tlp.getState();
    if ((st != TLItem.State.Normal) && (st != TLItem.State.ApplyFinish)) {
      throw new Exception("time limit point is not normal!");
    }

    if (isfinish)
    {
      try
      {
        GDBTransInThread.create();

        Date fd = new Date();
        boolean bres = GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
          Long.valueOf(tlid), TLItem.class, 
          new String[] { "ConfirmFinishDate", "State" }, 
          new Object[] { fd, Integer.valueOf(TLItem.State.Finish.getValue()) });

        if (!bres) {
          return false;
        }
        TLPlugs.TLPlug ttp = getPlug(tlp.getAppName(), tlp.getPlugType());

        String fbap = ttp.finishAction;
        if (Convert.isNotNullEmpty(fbap))
        {
          tlp.applyFinishDate = fd;
          tlp.timeState = TLItem.State.Finish;

          XmlData xd = XORMUtil.extractXmlDataFromObj(tlp);
          BizManager.getInstance().RT_doBizAction(null, fbap, null, xd);
        }

        GDBTransInThread.commit();
        return bres;
      }
      finally
      {
        GDBTransInThread.rollback();
      }

    }

    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "State" }, 
      new Object[] { Integer.valueOf(TLItem.State.Normal.getValue()) });
  }

  public boolean applyDelayTimeLimitPoint(long tlid, Date d, String applydesc, StringBuilder failedreson)
    throws GdbException, Exception
  {
    TLItem tlp = getTimeLimitById(tlid);
    if (tlp == null) {
      throw new Exception("no time limit point found with id=" + tlid);
    }
    if (tlp.getState() != TLItem.State.Normal) {
      throw new Exception("time limit point is not normal!");
    }
    boolean bres = GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "IsApplyDelay", "ApplyDelayDate", "DelayDesc" }, 
      new Object[] { Integer.valueOf(1), d, applydesc });

    if (!bres) {
      return false;
    }
    return bres;
  }

  public boolean confirmDelayTimeLimitPoint(long tlid, boolean ballow)
    throws GdbException, Exception
  {
    Date date = new Date();
    TLItem tlp = getTimeLimitById(tlid);
    if (tlp == null) {
      throw new Exception("no time limit point found with id=" + tlid);
    }
    if (tlp.getState() != TLItem.State.Normal) {
      throw new Exception("time limit point is not normal!");
    }

    if (ballow)
    {
      return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
        Long.valueOf(tlid), TLItem.class, 
        new String[] { "IsApplyDelay", "LimitDate", "ApplyDelayDate" }, 
        new Object[] { Integer.valueOf(0), tlp.applyDelayDate });
    }

    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "IsApplyDelay", "ApplyDelayDate" }, 
      new Object[] { Integer.valueOf(0) });
  }

  public boolean applyCancelTimeLimitPoint(long tlid, String applydesc, StringBuilder failedreson)
    throws GdbException, Exception
  {
    TLItem tlp = getTimeLimitById(tlid);
    if (tlp == null) {
      throw new Exception("no time limit point found with id=" + tlid);
    }
    if (tlp.getState() != TLItem.State.Normal) {
      throw new Exception("time limit point is not normal!");
    }
    boolean bres = GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "IsApplyCancel", "CancelDesc" }, 
      new Object[] { Integer.valueOf(1), applydesc });

    if (!bres) {
      return false;
    }
    return bres;
  }

  public boolean confirmCancelTimeLimitPoint(long tlid, boolean ballow)
    throws GdbException, Exception
  {
    Date date = new Date();
    TLItem tlp = getTimeLimitById(tlid);
    if (tlp == null) {
      throw new Exception("no time limit point found with id=" + tlid);
    }
    if (tlp.getState() != TLItem.State.Normal) {
      throw new Exception("time limit point is not normal!");
    }

    if (ballow)
    {
      return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
        Long.valueOf(tlid), TLItem.class, 
        new String[] { "IsApplyCancel", "State" }, 
        new Object[] { Integer.valueOf(0), Integer.valueOf(TLItem.State.Cancel.getValue()) });
    }

    return GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(tlid), TLItem.class, 
      new String[] { "IsApplyDelay" }, 
      new Object[] { Integer.valueOf(0) });
  }

  public List<TLItem> listNeedRemindTimeLimit()
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@NowDate", new Date());
    ht.put("@State", Integer.valueOf(TLItem.State.Normal.getValue()));

    return GDB.getInstance().accessDBPageAsXORMObjList("TimeLimit.ListRemindTimeLimit", ht, TLItem.class, 0, -1);
  }

  public List<TLItem> listNotFoundBrokenTimeLimit()
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@NowDate", new Date());
    ht.put("@State", Integer.valueOf(TLItem.State.Normal.getValue()));
    ht.put("@IsFound", Integer.valueOf(0));

    return GDB.getInstance().accessDBPageAsXORMObjList("TimeLimit.ListBrokenTimeLimit", ht, TLItem.class, 0, -1);
  }

  public void checkRemindTimeLimit()
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@NowDate", new Date());
    ht.put("@State", Integer.valueOf(TLItem.State.Normal.getValue()));

    GDB.getInstance().accessDBWithXORMObjSelectCallback("TimeLimit.ListRemindTimeLimit", ht, TLItem.class, 0, -1, this.remindTLItemStCB);
  }

  public void checkNotFoundBrokenTimeLimit()
    throws Exception
  {
    List dtli = listNotFoundBrokenTimeLimit();
    label198: for (TLItem tli : dtli)
    {
      try {
        try {
          GDBTransInThread.create();

          boolean bres = GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
            Long.valueOf(tli.getTLId()), TLItem.class, 
            new String[] { "RealAfterEffect", "IsFoundBroken" }, 
            new Object[] { Integer.valueOf(tli.getPreAfterEffect().getValue()), Integer.valueOf(1) });

          if (!bres)
          {
            GDBTransInThread.rollback();

            continue;
          }
          TLPlugs.TLPlug ttp = getPlug(tli.getAppName(), tli.getPlugType());
          String actp = ttp.brokenAction;
          if (Convert.isNotNullEmpty(actp))
          {
            tli.realAfterEffect = tli.getPreAfterEffect();
            tli.bFoundBroken = true;
            XmlData xd = XORMUtil.extractXmlDataFromObj(tli);
            BizManager.getInstance().RT_doBizAction(null, actp, null, xd);
          }

        }
        catch (Exception eee)
        {
          if (!log.isErrorEnabled()) break label198; 
        }log.error(eee);
      }
      finally
      {
        GDBTransInThread.rollback(); } GDBTransInThread.rollback();
    }
  }

  public void checkAppChangedTimeLimit()
  {
    String ststr = TLItem.State.Normal.getValue() + "," + TLItem.State.ApplyFinish.getValue();

    for (TLPlugs ps : getAllPlugs())
    {
      TLPlugs.TLPlug[] pps = ps.getAllPlug();
      if ((pps != null) && (pps.length > 0))
      {
        String appn = ps.getAppName();
        String appts = "";
        for (TLPlugs.TLPlug p : pps)
        {
          String str1 = p.getCheckActionPath();
        }

        try
        {
          Hashtable ht = new Hashtable();
          ht.put("@AppName", appn);
          ht.put("$States", ststr);
          GDB.getInstance().accessDBWithXORMObjSelectCallback(
            "TimeLimit.ListTimeLimitByAppAndState", ht, TLItem.class, 0, -1, this.checkTLItemStCB);
        }
        catch (Exception ee)
        {
          if (log.isErrorEnabled())
            log.error(ee);
        }
      }
    }
  }
}
