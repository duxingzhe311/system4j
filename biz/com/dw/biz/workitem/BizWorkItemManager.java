package com.dw.biz.workitem;

import com.dw.biz.BizFlow;
import com.dw.biz.BizParticipant.AssignmentStyle;
import com.dw.biz.BizView.Controller;
import com.dw.biz.flow_ins.BizFlowIns;
import com.dw.biz.flow_ins.BizFlowInsManager;
import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.system.gdb.DBResult;
import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GdbException;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class BizWorkItemManager
{
  private static BizWorkItemManager bwiMgr = new BizWorkItemManager();

  HashMap<String, BizWorkItemPlugInfo> app2plug = null;

  public static BizWorkItemManager getInstance()
  {
    return bwiMgr;
  }

  public BizWorkItemPlugInfo getWorkItemPlugInfoByAppName(String appn)
  {
    HashMap n2p = getWorkItemPlugInfo();
    if (n2p == null) {
      return null;
    }
    return (BizWorkItemPlugInfo)n2p.get(appn);
  }

  public BizWorkItemPlugInfo[] getAllWorkItemPlugInfos()
  {
    HashMap n2wmpi = getWorkItemPlugInfo();
    if (n2wmpi == null) {
      return null;
    }
    BizWorkItemPlugInfo[] rets = new BizWorkItemPlugInfo[n2wmpi.size()];
    n2wmpi.values().toArray(rets);
    return rets;
  }

  public HashMap<String, BizWorkItemPlugInfo> getWorkItemPlugInfo()
  {
    if (this.app2plug != null) {
      return this.app2plug;
    }
    try
    {
      HashMap tmp = new HashMap();
      AppInfo[] ais = CompManager.getInstance().getAllAppInfo();
      for (AppInfo ai : ais)
      {
        String fn = ai.getRealPath() + "/workitem.plug.xml";
        File f = new File(fn);
        if (f.exists())
        {
          BizWorkItemPlugInfo bwipi = BizWorkItemPlugInfo.getBizWorkItemPlugInfoByFile(ai.getContextName(), f);

          tmp.put(bwipi.getAppName(), bwipi);
        }
      }
      this.app2plug = tmp;
      return tmp;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  public BizWorkItem getWorkItemById(long wiid)
    throws GdbException, Exception
  {
    return (BizWorkItem)GDB.getInstance().getXORMObjByPkId(BizWorkItem.class, Long.valueOf(wiid));
  }

  public BizWorkItemList getWorkItemsByAssignedUser(String username, BizWorkItem.State st, String viewpath, String relatedidx, int pageidx, int pagesize)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@AssignedUser", username);
    if (st != null)
      ht.put("@State", Integer.valueOf(st.getValue()));
    if (viewpath != null)
      ht.put("@ViewPath", viewpath);
    if (relatedidx != null) {
      ht.put("@RelatedIdx", relatedidx);
    }
    DBResult dbr = GDB.getInstance().accessDBPage(
      "WorkItem.GetWorkItemsByAssignedUser", ht, pageidx, pagesize);
    List us = (List)dbr.transTable2XORMObjList(0, 
      BizWorkItem.class);
    int tc = dbr.getResultFirstTable().getTotalCount();
    return new BizWorkItemList(us, pageidx, pagesize, tc);
  }

  public BizWorkItemList getWorkItemsByViewPath(String viewpath, String relatedidx, String assigneduser, BizWorkItem.State st, int pageidx, int pagesize)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();

    ht.put("@ViewPath", viewpath);
    if (st != null)
      ht.put("@State", Integer.valueOf(st.getValue()));
    if (relatedidx != null)
      ht.put("@RelatedIdx", relatedidx);
    if (assigneduser != null) {
      ht.put("@AssignedUser", assigneduser);
    }
    DBResult dbr = GDB.getInstance().accessDBPage(
      "WorkItem.GetWorkItemsByViewPath", ht, pageidx, pagesize);
    List us = (List)dbr.transTable2XORMObjList(0, 
      BizWorkItem.class);
    int tc = dbr.getResultFirstTable().getTotalCount();
    return new BizWorkItemList(us, pageidx, pagesize, tc);
  }

  public BizWorkItemList getWorkItemsByFlowInsId(long flowinsid)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();

    ht.put("@FlowInsId", Long.valueOf(flowinsid));

    DBResult dbr = GDB.getInstance().accessDBPage(
      "WorkItem.GetWorkItemsByFlowInsId", ht, 0, -1);
    List us = (List)dbr.transTable2XORMObjList(0, 
      BizWorkItem.class);
    int tc = dbr.getResultFirstTable().getTotalCount();
    return new BizWorkItemList(us, 0, -1, tc);
  }

  public List<BizWorkItem> getUnassignedWorkItemsByFlowIns(long flow_ins_id, String create_user, BizParticipant.AssignmentStyle as0)
    throws Exception
  {
    Hashtable ht = new Hashtable();

    ht.put("@CreationUser", create_user);
    ht.put("@FlowInsId", Long.valueOf(flow_ins_id));
    ht.put("@AssignStyle", Integer.valueOf(as0.getValue()));

    return GDB.getInstance().accessDBPageAsXORMObjList(
      "WorkItem.GetUnsignedWorkItemsByFlowIns", ht, BizWorkItem.class, 0, -1);
  }

  public List<BizWorkItem> getUnassignedFirstAcceptWorkItems(String username)
    throws Exception
  {
    Hashtable ht = new Hashtable();

    return GDB.getInstance().accessDBPageAsXORMObjList(
      "WorkItem.GetUnsignedWorkItemsByFlowIns", ht, BizWorkItem.class, 0, -1);
  }

  public BizWorkItem createWorkItem(String creation_user, BizParticipant.AssignmentStyle as0, String title, String viewpath, String relatedidx, String finishactpath, String assigneduser, Date limitdate, List<String> replace_users, long flowins_id, String flownode_id, XmlData cont)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    if (creation_user != null) {
      ht.put("@CreationUser", creation_user);
    }
    ht.put("@Title", title);
    ht.put("@CreationDate", new Date());
    ht.put("@ViewPath", viewpath);
    if (relatedidx != null)
      ht.put("@RelatedIdx", relatedidx);
    if (finishactpath != null)
      ht.put("@FinishActionPath", finishactpath);
    if (assigneduser != null) {
      ht.put("@AssignedUser", assigneduser);
    }
    if (as0 != null) {
      ht.put("@AssignStyle", Integer.valueOf(as0.getValue()));
    }
    if (limitdate != null) {
      ht.put("@LimitDate", limitdate);
    }
    if ((replace_users != null) && (replace_users.size() > 0))
    {
      StringBuilder sb = new StringBuilder();
      sb.append("|");
      for (String repu : replace_users)
      {
        sb.append(repu).append("|");
      }
      ht.put("@ReplaceUsers", sb.toString());
    }

    if (flowins_id <= 0L)
      flowins_id = -1L;
    if (flowins_id > 0L)
    {
      if ((flownode_id == null) || (flownode_id.equals("")))
        throw new IllegalArgumentException(
          "flow node workitem must has flow node id!");
    }
    ht.put("@FlowInsId", Long.valueOf(flowins_id));
    if (flownode_id != null)
      ht.put("@FlowNodeInsId", flownode_id);
    if (cont != null)
      ht.put("@DataCont", cont.toBytesWithUTF8());
    DBResult dbr = GDB.getInstance().accessDB(
      "WorkItem.CreateWorkItemWithLog", ht);

    DataTable dt = dbr.getResultTable(1);
    Number n = (Number)dt.getFirstColumnOfFirstRow();

    BizWorkItem bwi = new BizWorkItem(title, assigneduser, 
      viewpath, relatedidx, finishactpath, 
      flowins_id, flownode_id, cont);

    bwi.id = n.longValue();
    return bwi;
  }

  public BizWorkItem createWorkItemByUser(String creation_user, BizParticipant.AssignmentStyle as0, String title, String viewpath, String relatedidx, String finishact_path, String assigneduser, Date limitdate, XmlData cont)
    throws GdbException, Exception
  {
    if ((creation_user == null) || (creation_user.equals(""))) {
      throw new IllegalArgumentException("Creation User Cannot be null!");
    }
    return createWorkItem(creation_user, as0, title, viewpath, relatedidx, finishact_path, 
      assigneduser, limitdate, null, -1L, 
      null, cont);
  }

  public BizWorkItem createWorkItemByUser(BizWorkItem bwi)
    throws GdbException, Exception
  {
    return createWorkItemByUser(bwi.getCreationUser(), bwi.getAssignmentStyle(), bwi.getTitle(), 
      bwi.getBizViewPath(), bwi.getRelatedIdx(), bwi.getFinishBizActionPath(), bwi.getAssignedUser(), bwi.getLimitDate(), bwi.getDataCont());
  }

  public void setCheckAssigned(long wiid)
    throws Exception
  {
    GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(wiid), BizWorkItem.class, new String[] { "AssignedChk" }, new Object[] { Boolean.valueOf(true) });
  }

  public BizWorkItem createWorkItemByFlow(String creator, BizParticipant.AssignmentStyle as0, String title, String viewpath, String finishactpath, String assigneduser, long flowins_id, String flownode_id, Date limitdt, List<String> replace_usrs, XmlData cont)
    throws GdbException, Exception
  {
    if (flowins_id <= 0L)
      throw new IllegalArgumentException("must has flow ins id!");
    if ((flownode_id == null) || (flownode_id.equals(""))) {
      throw new IllegalArgumentException("must has flow node id!");
    }
    return createWorkItem(creator, as0, title, viewpath, null, finishactpath, assigneduser, limitdt, replace_usrs, flowins_id, 
      flownode_id, cont);
  }

  public boolean finishWorkItem(BizWorkItem bwi, UserProfile up, XmlData cont, long costsecond, HashMap<String, String> np2username, String sel_transid)
    throws Exception
  {
    boolean bres = finishWorkItemDB(bwi.getWorkItemId(), up.getUserName(), cont, costsecond);
    if (!bres) {
      return false;
    }
    long flow_ins_id = bwi.getFlowInsId();
    String flow_node_id = bwi.getFlowNodeId();
    BizView.Controller ctrl = null;
    BizFlowIns bfi = null;
    BizFlow bf = null;
    if (flow_ins_id > 0L)
    {
      bfi = BizFlowInsManager.getInstance().getFlowIns(flow_ins_id);
      if (bfi == null)
      {
        throw new Exception("cannot find flow instance with id=" + flow_ins_id);
      }

      bfi.setUnassignedManualNodePerformerUser(np2username, false);

      bfi.runItWithNodeOut(flow_node_id, sel_transid);
    }

    return true;
  }

  private boolean finishWorkItemDB(long workitemid, String oper_user, XmlData cont, long costsecond)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@WorkItemId", Long.valueOf(workitemid));

    if (oper_user != null)
      ht.put("@OperUser", oper_user);
    if (cont != null)
      ht.put("@DataCont", cont.toBytesWithUTF8());
    if (costsecond <= 0L)
      costsecond = -1L;
    ht.put("@CostSecond", Long.valueOf(costsecond));

    DBResult dbr = GDB.getInstance().accessDB("WorkItem.FinishWorkItemWithLog", ht);
    Number n = dbr.getResultFirstColOfFirstRowNumber();
    if (n == null) {
      return false;
    }
    if (n.intValue() <= 0) {
      return false;
    }

    return true;
  }

  public void setCheckFinished(long wiid)
    throws Exception
  {
    GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(wiid), BizWorkItem.class, new String[] { "FinishedChk" }, new Object[] { Boolean.valueOf(true) });
  }

  public void cancelWorkItem(long workitemid, String oper_user, String desc)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@WorkItemId", Long.valueOf(workitemid));
    ht.put("@OperUser", oper_user);

    if (desc != null)
      ht.put("@CancelDesc", desc);
    GDB.getInstance().accessDB("WorkItem.CancelWorkItemWithLog", ht);
  }

  public void abortWorkItemByFlowIns(long flow_ins_id)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@FlowInsId", Long.valueOf(flow_ins_id));
    ht.put("@LogDate", new Date());
    GDB.getInstance().accessDB("WorkItem.AbortWorkItemWithByFlowIns", ht);
  }

  public boolean assignWorkItem(String oper_user, long workitemid, String assign_user, String desc)
    throws GdbException, Exception
  {
    if ((assign_user == null) || (assign_user.equals(""))) {
      throw new IllegalArgumentException("assign user cannot be null or empty!");
    }
    Hashtable ht = new Hashtable();
    ht.put("@WorkItemId", Long.valueOf(workitemid));
    ht.put("@OperUser", oper_user);
    ht.put("@AssignUser", assign_user);
    if (desc != null) {
      ht.put("@LogDesc", desc);
    }
    DBResult dbr = GDB.getInstance().accessDB("WorkItem.AssignWorkItemWithLog", ht);
    return ((Number)dbr.getResultFirstColumnOfFirstRow()).intValue() == 1;
  }

  public boolean acquireWorkItem(String oper_user, long workitemid, String desc)
    throws GdbException, Exception
  {
    if ((oper_user == null) || (oper_user.equals(""))) {
      throw new IllegalArgumentException("oper user cannot be null or empty!");
    }
    Hashtable ht = new Hashtable();
    ht.put("@WorkItemId", Long.valueOf(workitemid));
    ht.put("@OperUser", oper_user);
    ht.put("@AssignUser", oper_user);
    if (desc != null) {
      ht.put("@LogDesc", desc);
    }
    DBResult dbr = GDB.getInstance().accessDB("WorkItem.AssignWorkItemWithLog", ht);
    return ((Number)dbr.getResultFirstColumnOfFirstRow()).intValue() == 1;
  }

  public BizWorkItemList listICanReplaceWorkitems(String usern, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();

    ht.put("@State", BizWorkItem.State.Normal);
    ht.put("@UserName", usern);
    ht.put("$UserNameStr", usern);
    ht.put("@CurDT", new Date());
    DBResult dbr = GDB.getInstance().accessDBPage(
      "WorkItem.GetWorkItemsByCanReplace", ht, pageidx, pagesize);
    List us = (List)dbr.transTable2XORMObjList(0, 
      BizWorkItem.class);
    int tc = dbr.getResultFirstTable().getTotalCount();
    return new BizWorkItemList(us, pageidx, pagesize, tc);
  }

  public boolean directWorkItem(String oper_user, long workitemid, String target_user, String desc)
    throws GdbException, Exception
  {
    if ((target_user == null) || (target_user.equals(""))) {
      throw new IllegalArgumentException("assign user cannot be null or empty!");
    }
    Hashtable ht = new Hashtable();
    ht.put("@WorkItemId", Long.valueOf(workitemid));
    ht.put("@OperUser", oper_user);
    ht.put("@TargetUser", target_user);
    if (desc != null) {
      ht.put("@LogDesc", desc);
    }
    DBResult dbr = GDB.getInstance().accessDB("WorkItem.DirectWorkItemWithLog", ht);
    return ((Number)dbr.getResultFirstColumnOfFirstRow()).intValue() == 1;
  }

  public List<BizWorkItemLog> getWorkItemLogs(long workitemid)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@WorkItemId", Long.valueOf(workitemid));

    return GDB.getInstance().accessDBAsObjList(
      "WorkItemLog.GetLogByWorkItemId", ht, BizWorkItemLog.class);
  }

  public BizWorkItemList getUserWorkItemListByLog(String user, BizWorkItemLog.LogType lt, BizWorkItem.State st, int pageidx, int pagesize)
  {
    return null;
  }
}
