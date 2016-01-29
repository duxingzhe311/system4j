package com.dw.biz.flow_ins;

import com.dw.biz.BizFlow;
import com.dw.biz.BizFlow.DataField;
import com.dw.biz.BizFlow.DataFieldContainer;
import com.dw.biz.BizFlow.DataFieldXmlVal;
import com.dw.biz.BizFlow.EventType;
import com.dw.biz.BizFlow.NodePerformer;
import com.dw.biz.BizFlow.NodeStart;
import com.dw.biz.BizManager;
import com.dw.biz.workitem.BizWorkItem;
import com.dw.biz.workitem.BizWorkItemManager;
import com.dw.system.Convert;
import com.dw.system.cache.Cacher;
import com.dw.system.gdb.DBResult;
import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GDBMulti;
import com.dw.system.gdb.GdbException;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class BizFlowInsManager
{
  private static BizFlowInsManager bizfMgr = new BizFlowInsManager();

  private BizManager bizMgr = BizManager.getInstance();

  private transient Cacher cache = Cacher.getCacher(BizFlowInsManager.class.getCanonicalName());

  public static BizFlowInsManager getInstance()
  {
    return bizfMgr;
  }

  private BizFlowInsManager()
  {
    this.cache.setMaxBufferLength(100);
  }

  public BizManager getBizManager()
  {
    return this.bizMgr;
  }

  public ArrayList<BizFlow.NodePerformer> checkManualNodePerformersByStartNode(BizFlow bf, UserProfile up, XmlData inputxd, String sel_transid)
    throws Exception
  {
    BizFlow.NodeStart ns = bf.getStartNode();
    if (ns == null)
    {
      return null;
    }
    return BizFlowIns.checkManualNodePerformersByNode(ns, up, inputxd, sel_transid);
  }

  public BizFlowIns createFlowIns(UserProfile up, String flowpath, XmlData paramxd, HashMap<String, String> nodeperformer2user, String sel_transid)
    throws Exception
  {
    BizFlow bf = BizManager.getInstance().getBizFlowByPath(flowpath);
    if (bf == null) {
      throw new Exception("Cannot find BizFlow with path=" + flowpath);
    }
    return createFlowIns(up, bf, paramxd, nodeperformer2user, sel_transid);
  }

  public BizFlowIns createFlowIns(UserProfile up, BizFlow bf, XmlData paramxd, HashMap<String, String> nodeperformer2user, String sel_transid)
    throws Exception
  {
    BizFlow.NodeStart ns = bf.getStartNode();
    BizFlow.NodePerformer np = ns.getRelatedNodePerformer();

    BizFlowInsContext cxt = new BizFlowInsContext(up, bf.getBizPathStr(), "", paramxd);
    if (np != null)
    {
      cxt.setDataFieldVal(np.getNodeName(), up.getUserName());
    }
    if (nodeperformer2user != null)
    {
      List nps = checkManualNodePerformersByStartNode(bf, up, paramxd, sel_transid);
      for (BizFlow.NodePerformer tmpnp : nps)
      {
        String username = (String)nodeperformer2user.get(tmpnp.getNodeName());
        if (!Convert.isNullOrEmpty(username))
        {
          cxt.setDataFieldVal(tmpnp.getNodeName(), username);
        }
      }
    }
    BizFlowIns bfi = new BizFlowIns(this, cxt);

    cxt.saveContext();

    return bfi;
  }

  public void abortFlowIns(long insid)
    throws GdbException, Exception
  {
    BizFlowIns bfi = getFlowIns(insid);
    if (bfi == null) {
      return;
    }
    changeFlowInsState(insid, BizFlowInsState.closed_aborted);
    BizWorkItemManager.getInstance().abortWorkItemByFlowIns(insid);

    bfi.doEvent(BizFlow.EventType.flow_ins_abort);

    this.cache.remove(Long.valueOf(insid));
  }

  private void changeFlowInsState(long insid, BizFlowInsState st)
    throws GdbException, Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@FlowInsId", Long.valueOf(insid));
    ht.put("@InsState", Integer.valueOf(st.getValue()));
    GDB.getInstance().accessDB("BizFlowIns.ChangeFlowInsSt", ht);
  }

  public void deleteFlowIns(long insid)
  {
  }

  public BizFlowIns getFlowIns(long insid)
    throws Exception
  {
    BizFlowIns bfi = (BizFlowIns)this.cache.get(Long.valueOf(insid));
    if (bfi != null) {
      return bfi;
    }
    BizFlowInsContext bfic = BizFlowInsContext.loadContext(insid);
    if (bfic == null) {
      return null;
    }
    bfi = new BizFlowIns(this, bfic);

    this.cache.cache(Long.valueOf(insid), bfi);
    return bfi;
  }

  public BizFlowInsList listFlowIns(String flowpath, BizFlowInsState st, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    if ((flowpath != null) && (!flowpath.equals("")))
      ht.put("@FlowPath", flowpath);
    if (st != null) {
      ht.put("@InsState", Integer.valueOf(st.getValue()));
    }

    DBResult dbr = GDB.getInstance().accessDBPage("BizFlowIns.ListFlowIns", ht, pageidx * pagesize, pagesize);
    DataTable dt = dbr.getResultTable(0);
    List ls = (List)dbr.transTable2XORMObjList(0, BizFlowInsContext.class);
    return new BizFlowInsList(pageidx, pagesize, dt.getTotalCount(), ls);
  }

  public BizFlowInsList listFlowInsByRelatedUser(String username, BizFlowInsState st, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@UserName", username);
    if (st != null) {
      ht.put("@InsState", Integer.valueOf(st.getValue()));
    }
    DBResult dbr = GDB.getInstance().accessDBPage("BizFlowIns.ListUserRelatedInsCxt", ht, pageidx * pagesize, pagesize);
    DataTable dt = dbr.getResultTable(0);
    List ls = (List)dbr.transTable2XORMObjList(0, BizFlowInsContext.class);
    return new BizFlowInsList(pageidx, pagesize, dt.getTotalCount(), ls);
  }

  public BizFlowInsList listFlowInsByCreatedUser(String username, BizFlowInsState st, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@UserName", username);
    if (st != null) {
      ht.put("@InsState", Integer.valueOf(st.getValue()));
    }
    DBResult dbr = GDB.getInstance().accessDBPage("BizFlowIns.ListUserCreatedInsCxt", ht, pageidx * pagesize, pagesize);
    DataTable dt = dbr.getResultTable(0);
    List ls = (List)dbr.transTable2XORMObjList(0, BizFlowInsContext.class);
    return new BizFlowInsList(pageidx, pagesize, dt.getTotalCount(), ls);
  }

  public BizActIns createActivityIns(long flowins_id, XmlData inputxd)
  {
    return null;
  }

  public BizActIns getActIns(long insid)
  {
    return null;
  }

  void fireFlowInsClosed(BizFlowIns bfi)
  {
    BizFlowInsState bfis = bfi.insContext.getInsState();
    if (bfis != BizFlowInsState.closed_completed)
    {
      if (bfis != BizFlowInsState.closed_aborted)
      {
        throw new RuntimeException("flow is no close state!");
      }
    }
  }

  void fireActInsClosed(BizActIns bai)
  {
  }

  public void onWorkItemClosed(BizWorkItem bwi)
  {
  }

  public BizFlowInsList listFlowInsByPathAndIdx(String flowpath, String dfname, String dfstrv)
    throws Exception
  {
    return listFlowInsByPathAndIdx(flowpath, dfname, dfstrv, BizFlowInsState.getOpenStates());
  }

  public BizFlowInsList listFlowInsByPathAndIdx(String flowpath, String dfname, String dfstrv, BizFlowInsState[] sts)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@FlowPath", flowpath);
    ht.put("@DfName", dfname);
    ht.put("@DfStrVal", dfstrv);

    if ((sts != null) && (sts.length > 0))
    {
      StringBuilder sb = new StringBuilder();
      sb.append(sts[0].getValue());
      for (int k = 1; k < sts.length; k++)
        sb.append(',').append(sts[k].getValue());
      ht.put("$InsStateStr", sb.toString());
    }

    DBResult dbr = GDB.getInstance().accessDBPage("BizFlowIns.ListInsCxtByPathAndIdx", ht, 0, -1);
    DataTable dt = dbr.getResultTable(0);
    List ls = (List)dbr.transTable2XORMObjList(0, BizFlowInsContext.class);
    return new BizFlowInsList(0, -1, dt.getTotalCount(), ls);
  }

  public boolean updateCxtIdx(long flowinsid, StringBuilder failedreson)
    throws Exception
  {
    BizFlowInsContext cxt = BizFlowInsContext.loadContext(flowinsid);
    if (cxt == null) {
      return false;
    }
    return updateCxtIdx(cxt, false, failedreson);
  }

  boolean updateCxtIdx(BizFlowInsContext cxt, boolean checkchg, StringBuilder failedreson)
    throws Exception
  {
    String fp = cxt.getFlowPath();
    BizFlow bf = getBizManager().getBizFlowByPath(fp);
    if (bf == null)
    {
      failedreson.append("no flow found with path=" + fp);
      return false;
    }

    GDBMulti gdbm = new GDBMulti();
    Hashtable ht = new Hashtable();
    long insid = cxt.getInsId();
    ht.put("@InsId", Long.valueOf(insid));
    gdbm.addAccessDB("BizFlowIns.DelIdxByInsId", ht);

    HashMap newd = new HashMap();

    XmlData dfdata = cxt.getDataField();
    List nps = bf.getAllPerformerNodes();
    BizFlow.DataField[] dfs = bf.getDataFieldContainer().getAllDataFields();
    Object ov;
    if (((dfs != null) || (nps != null)) && (dfdata != null))
    {
      ArrayList dfns = new ArrayList();
      if (dfs != null)
      {
        for (BizFlow.DataField df : dfs)
        {
          if ((df instanceof BizFlow.DataFieldXmlVal))
          {
            BizFlow.DataFieldXmlVal dfxv = (BizFlow.DataFieldXmlVal)df;
            if (dfxv.isHasIdx())
              dfns.add(df.getName());
          }
        }
      }
      if (nps != null)
      {
        for (BizFlow.NodePerformer np : nps)
        {
          String nn = np.getNodeName();
          if (!Convert.isNullOrEmpty(nn))
          {
            if (!dfns.contains(nn)) {
              dfns.add(nn);
            }
          }
        }
      }

      for (String dfn : dfns)
      {
        ov = dfdata.getParamValue(dfn);
        String dfv = null;
        if (ov != null) {
          dfv = ov.toString();
        }

        if (dfv != null)
        {
          newd.put(dfn, dfv);

          BizFlowInsDfIdxItem ii = new BizFlowInsDfIdxItem(fp, insid, dfn, dfv);
          gdbm.addAddXORMObj(ii);
        }
      }
    }

    if (checkchg)
    {
      HashMap oldidxv = new HashMap();
      List olddd = getIdxByFlowInsId(insid);
      if (olddd != null)
      {
        for (ov = olddd.iterator(); ((Iterator)ov).hasNext(); ) { BizFlowInsDfIdxItem idx = (BizFlowInsDfIdxItem)((Iterator)ov).next();

          oldidxv.put(idx.dfName, idx.dfStrVal);
        }
      }

      if (!checkIdxDataChange(oldidxv, newd)) {
        return true;
      }
    }

    GDB.getInstance().accessDBMulti(gdbm);

    return true;
  }

  private boolean checkIdxDataChange(HashMap<String, String> oldd, HashMap<String, String> newd)
  {
    if (oldd.size() != newd.size()) {
      return true;
    }
    for (Map.Entry n2v : oldd.entrySet())
    {
      String k = (String)n2v.getKey();
      if (!((String)n2v.getValue()).equals(newd.get(k))) {
        return true;
      }
    }
    return false;
  }

  public List<BizFlowInsDfIdxItem> getIdxByFlowInsId(long insid)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@InsId", Long.valueOf(insid));
    return GDB.getInstance().accessDBPageAsXORMObjList("BizFlowIns.GetIdxByInsId", ht, BizFlowInsDfIdxItem.class, 0, -1);
  }
}
