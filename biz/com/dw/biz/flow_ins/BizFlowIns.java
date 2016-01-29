package com.dw.biz.flow_ins;

import com.dw.biz.BizActionResult;
import com.dw.biz.BizCondition;
import com.dw.biz.BizCondition.CondType;
import com.dw.biz.BizFlow;
import com.dw.biz.BizFlow.ActNode;
import com.dw.biz.BizFlow.AndXor;
import com.dw.biz.BizFlow.DataField;
import com.dw.biz.BizFlow.DataFieldContainer;
import com.dw.biz.BizFlow.EventType;
import com.dw.biz.BizFlow.Node;
import com.dw.biz.BizFlow.NodeAction;
import com.dw.biz.BizFlow.NodeEnd;
import com.dw.biz.BizFlow.NodePerformer;
import com.dw.biz.BizFlow.NodeRouter;
import com.dw.biz.BizFlow.NodeStart;
import com.dw.biz.BizFlow.NodeSubFlow;
import com.dw.biz.BizFlow.NodeView;
import com.dw.biz.BizFlow.Transition;
import com.dw.biz.BizInOutParam;
import com.dw.biz.BizInOutParam.ParamStyle;
import com.dw.biz.BizManager;
import com.dw.biz.BizParticipant;
import com.dw.biz.BizParticipant.AssignmentStyle;
import com.dw.biz.BizView;
import com.dw.biz.workitem.BizWorkItem;
import com.dw.biz.workitem.BizWorkItem.State;
import com.dw.biz.workitem.BizWorkItemManager;
import com.dw.system.Convert;
import com.dw.system.IExpPropProvider;
import com.dw.system.codedom.BoolExp;
import com.dw.system.codedom.BoolExpRunner;
import com.dw.system.codedom.RunContext;
import com.dw.system.codedom.ValWrapper;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import com.dw.user.right.RightManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BizFlowIns
  implements IExpPropProvider
{
  RunContext transRC = new RunContext()
  {
    public ValWrapper getValueWrapper(String var_name)
    {
      ValWrapper vw = super.getValueWrapper(var_name);
      if (vw != null) {
        return vw;
      }
      if (var_name.startsWith("@")) {
        return new ValWrapper(BizFlowIns.this.insContext.getXmlDataFieldVal(var_name.substring(1)));
      }
      return new ValWrapper(null);
    }
  };

  transient BizFlowInsManager flowInsMgr = null;
  transient BizManager bizMgr = null;

  String bizFlowPath = null;

  BizFlowInsContext insContext = null;

  transient TransitionRunner transitionRunner = new TransitionRunner();

  BizFlowIns()
  {
  }

  public BizFlowIns(BizFlowInsManager bfimgr, BizFlowInsContext cxt) throws Exception
  {
    this.flowInsMgr = bfimgr;
    this.bizMgr = bfimgr.getBizManager();
    this.bizFlowPath = cxt.getFlowPath();

    this.insContext = cxt;
    init();

    resetTitleByInsTitleTemp();
  }

  public BizFlow getBizFlow()
  {
    try
    {
      return this.bizMgr.getBizFlowByPath(this.bizFlowPath);
    }
    catch (Exception ee) {
    }
    return null;
  }

  public BizFlowInsContext getInsContext()
  {
    return this.insContext;
  }

  void init()
  {
    BizFlow.DataField[] dfs = getBizFlow().getDataFieldContainer().getAllDataFields();
  }

  public void setContext(BizFlowInsContext insc)
  {
    this.insContext = insc;
  }

  void resetTitleByInsTitleTemp()
  {
    String[] fitt = getBizFlow().getFlowInsTitleTempArray();
    if ((fitt == null) || (fitt.length <= 0)) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    for (String s : fitt)
    {
      if (s.charAt(0) == '@')
      {
        Object o = this.insContext.getDataFieldVal(s.substring(1));
        if (o != null)
        {
          sb.append(o.toString());
        }
      }
      else {
        sb.append(s);
      }
    }

    this.insContext.title = sb.toString();
  }

  public Object getPropValue(String propname)
  {
    if ("id".equalsIgnoreCase(propname))
      return Long.valueOf(this.insContext.getInsId());
    if ("title".equalsIgnoreCase(propname))
      return this.insContext.getInsTitle();
    return null;
  }

  public void setPropValue(String propname, Object v)
  {
  }

  public BizFlowInsState getState()
  {
    return this.insContext.getInsState();
  }

  public void startIt(String sel_transid)
    throws Exception
  {
    if (this.insContext.getInsState() != BizFlowInsState.open_notStarted) {
      throw new Exception("invalid state=" + this.insContext.getInsState());
    }
    this.insContext.setInsState(BizFlowInsState.open_running);

    BizFlow.NodeStart ns = getBizFlow().getStartNode();

    BizFlowInsContext.NodeId2InsItem niii = this.insContext.getNodeId2InsMap().getNodeInsItemByNodeId(ns.getNodeId());
    niii.setSelectTransId(sel_transid);

    niii.increateInsCount();

    niii.setRunning(true);

    runIt();
  }

  public void runIt()
    throws Exception
  {
    runItWithNodeOut(null, null);
  }

  public synchronized void runItWithNodeOut(String nodeid, String sel_transid)
    throws Exception
  {
    if (Convert.isNotNullEmpty(nodeid))
    {
      BizFlowInsContext.NodeId2InsItem niii = this.insContext.getNodeId2InsMap().getNodeInsItemByNodeId(nodeid);
      niii.setSelectTransId(sel_transid);
    }

    if (this.insContext.getInsState() != BizFlowInsState.open_running) {
      throw new RuntimeException("flow instance must in state=" + BizFlowInsState.open_running.toString());
    }

    this.insContext.increaseScanId();

    BizFlowInsContext.NodeId2InsItem nid = null;
    while (true)
    {
      nid = this.insContext.getNextCurRunningNodeIns();
      if (nid == null) {
        break;
      }
      runNode(nid);

      if (this.insContext.bDirty)
      {
        resetTitleByInsTitleTemp();

        this.insContext.saveContext();
      }
    }
  }

  public String getNodePerformerAssignedUser(BizFlow.NodePerformer np)
  {
    return (String)this.insContext.getDataFieldVal(np.getNodeName());
  }

  public void setNodePerformerAssignedUser(BizFlow.NodePerformer np, String username)
  {
    this.insContext.setDataFieldVal(np.getNodeName(), username);
  }

  public String calNodePerformerUserName(BizFlow.NodePerformer np)
    throws Exception
  {
    String un = getNodePerformerAssignedUser(np);
    if (Convert.isNotNullEmpty(un)) {
      return un;
    }
    BizParticipant bp = np.getParticipant();
    BizParticipant.AssignmentStyle as = bp.getAssignmentStyle();
    RightManager rmgr = RightManager.getDefaultIns();
    String[] runs = bp.getRelatedUserNames(rmgr);

    if (as == BizParticipant.AssignmentStyle.auto)
    {
      if (runs != null)
      {
        if (runs.length > 0)
          un = runs[0];
      }
    }
    else if (as == BizParticipant.AssignmentStyle.first_accept);
    return un;
  }

  private boolean runEventActionNode(BizFlow.NodeAction na)
    throws Exception
  {
    XmlData xd = readNodeInFromDF(na);
    String ap = na.getActionPath();
    BizActionResult bar = this.bizMgr.RT_doBizAction(null, ap, null, xd);
    XmlData outputxd = bar.getResultData();

    if (outputxd != null)
    {
      return true;
    }

    return false;
  }

  private void runNode(BizFlowInsContext.NodeId2InsItem nid)
    throws Exception
  {
    BizFlow.Node n = getBizFlow().getNode(nid.getNodeId());
    if (n == null) {
      return;
    }
    if ((n instanceof BizFlow.NodeRouter))
    {
      nid.setRunning(false);

      this.insContext.bDirty = true;

      runNodeOutTransition(nid, (BizFlow.NodeRouter)n);
    } else {
      if ((n instanceof BizFlow.NodeView))
      {
        BizFlow.NodeView nv = (BizFlow.NodeView)n;
        BizView bv = nv.getBizView();
        BizWorkItemManager bwi_mgr = BizWorkItemManager.getInstance();

        if (nid.getCurInsId() > 0L)
        {
          BizWorkItem bwi = bwi_mgr.getWorkItemById(nid.getCurInsId());

          if (bwi == null)
          {
            throw new Exception("no biz act ins found with id=" + nid.getCurInsId());
          }

          if (bwi.getState() == BizWorkItem.State.Finished)
          {
            XmlData outputxd = bwi.getDataCont();

            if (outputxd != null) {
              writeNodeOutToDF(outputxd, nv);
            }
            nid.setRunning(false);

            this.insContext.bDirty = true;

            runNodeOutTransition(nid, (BizFlow.NodeView)n);
            return;
          }

          return;
        }

        BizFlow.NodePerformer np = nv.getRelatedNodePerformer();
        if (np == null) {
          throw new RuntimeException("no performer for Node View=" + nv.getTitle());
        }
        BizParticipant bp = np.getParticipant();
        String un = calNodePerformerUserName(np);

        String pre_view_node_user = null;
        ArrayList ts = nv.getInTransitions();
        if ((ts != null) && (ts.size() == 1))
        {
          BizFlow.ActNode an = ((BizFlow.Transition)ts.get(0)).getFromNode();
          if ((an instanceof BizFlow.NodeView))
          {
            BizFlow.NodeView annv = (BizFlow.NodeView)an;
            BizFlow.NodePerformer nper = annv.getRelatedNodePerformer();
            if (nper != null)
            {
              pre_view_node_user = (String)this.insContext.getXmlDataFieldVal(nper.getNodeName());
            }
          }
        }
        if (un != null) un.equals("");

        Date limitdt = null;
        List replace_us = null;
        if (nv.getLimitDurMS() > 0L)
        {
          limitdt = new Date(System.currentTimeMillis() + nv.getLimitDurMS());
          RightManager rmgr = RightManager.getDefaultIns();
          String[] runs = bp.getRelatedUserNames(rmgr);
          if (runs != null)
          {
            replace_us = new ArrayList();
            for (String tmpun : runs)
            {
              if (!tmpun.equals(un))
              {
                replace_us.add(tmpun);
              }
            }
          }
        }
        XmlData xd = readNodeInFromDF(nv);

        BizWorkItem bwi = bwi_mgr.createWorkItemByFlow(pre_view_node_user, bp.getAssignmentStyle(), getBizFlow().getTitle() + "-" + nv.getTitle() + ":" + this.insContext.getInsTitle(), bv.getBizPathStr(), null, 
          un, this.insContext.getInsId(), nv.getNodeId(), 
          limitdt, replace_us, xd);

        nid.setCurInsId(bwi.getWorkItemId());

        this.insContext.bDirty = true;

        return;
      }

      if ((n instanceof BizFlow.NodeAction))
      {
        BizFlow.NodeAction na = (BizFlow.NodeAction)n;
        XmlData xd = readNodeInFromDF(na);
        String ap = na.getActionPath();
        BizActionResult bar = this.bizMgr.RT_doBizAction(null, ap, null, xd);
        XmlData outputxd = bar.getResultData();

        if (outputxd != null) {
          writeNodeOutToDF(outputxd, na);
        }
        nid.setRunning(false);

        this.insContext.bDirty = true;

        runNodeOutTransition(nid, (BizFlow.NodeAction)n);
        return;
      }
      if ((n instanceof BizFlow.NodeStart))
      {
        BizFlow.NodeStart ns = (BizFlow.NodeStart)n;

        XmlData tmpxd = this.insContext.getInputParam();
        if (tmpxd != null) {
          writeNodeOutToDF(tmpxd, ns);
        }
        nid.setRunning(false);

        doEvent(BizFlow.EventType.flow_ins_start);

        this.insContext.bDirty = true;

        runNodeOutTransition(nid, (BizFlow.NodeStart)n);
        return;
      }
      if ((n instanceof BizFlow.NodeEnd))
      {
        BizFlow.NodeEnd ne = (BizFlow.NodeEnd)n;

        XmlData xd = readNodeInFromDF(ne);
        this.insContext.outputParam = xd;
        this.insContext.setInsState(BizFlowInsState.closed_completed);

        this.insContext.bDirty = true;

        doEvent(BizFlow.EventType.flow_ins_end);

        BizFlowInsManager.getInstance().fireFlowInsClosed(this);
        return;
      }
      if ((n instanceof BizFlow.NodeSubFlow))
      {
        BizFlow.NodeSubFlow nsf = (BizFlow.NodeSubFlow)n;
        if (nid.getCurInsId() > 0L)
        {
          BizFlowIns bfi = BizFlowInsManager.getInstance().getFlowIns(nid.getCurInsId());
          if (bfi == null)
          {
            throw new Exception("no biz flow ins found with id=" + nid.getCurInsId());
          }

          if (this.insContext.getInsState().isClose())
          {
            XmlData outputxd = bfi.insContext.getOutputParam();

            if (outputxd != null) {
              writeNodeOutToDF(outputxd, nsf);
            }
            nid.setRunning(false);

            this.insContext.bDirty = true;

            runNodeOutTransition(nid, (BizFlow.NodeSubFlow)n);
            return;
          }

          return;
        }

        XmlData xd = readNodeInFromDF(nsf);
        BizFlowIns bfi = BizFlowInsManager.getInstance().createFlowIns(null, nsf.getFlowPath(), xd, null, null);

        nid.setCurInsId(bfi.insContext.getInsId());

        this.insContext.bDirty = true;

        return;
      }

      throw new RuntimeException("unknow node type!");
    }
  }

  private XmlData readNodeInFromDF(BizFlow.Node n)
    throws Exception
  {
    XmlData xddf = this.insContext.getDataField();

    BizInOutParam inputp = n.getInputParam();
    if (inputp == null)
    {
      return xddf;
    }

    BizInOutParam.ParamStyle ps = inputp.getParamStyle();
    if ((ps == null) || (ps == BizInOutParam.ParamStyle.All))
    {
      return xddf;
    }

    if (ps == BizInOutParam.ParamStyle.Null) {
      return null;
    }
    if (ps == BizInOutParam.ParamStyle.Script)
    {
      NodeInputRunCxt rc = new NodeInputRunCxt(this.insContext.getInsRTXmlData(), xddf);
      inputp.runScript(rc);
      return rc.getInputXmlData();
    }

    return xddf;
  }

  private void writeNodeOutToDF(XmlData xd, BizFlow.Node n)
    throws Exception
  {
    BizInOutParam outputp = n.getOutputParam();
    if (outputp == null)
    {
      this.insContext.writeToDataField(xd);
      return;
    }

    BizInOutParam.ParamStyle ps = outputp.getParamStyle();
    if ((ps == null) || (ps == BizInOutParam.ParamStyle.All))
    {
      this.insContext.writeToDataField(xd);
      return;
    }

    if (ps == BizInOutParam.ParamStyle.Null) {
      return;
    }
    if (ps == BizInOutParam.ParamStyle.Script)
    {
      NodeOutputRunCxt rc = new NodeOutputRunCxt(xd);
      outputp.runScript(rc);
      XmlData tmpxd = rc.getUpdateToDFXmlData();
      if (tmpxd != null)
        this.insContext.writeToDataField(tmpxd);
    }
  }

  private void runNodeOutTransition(BizFlowInsContext.NodeId2InsItem nid, BizFlow.ActNode n)
    throws Exception
  {
    ArrayList ts = n.getOutTransitions();
    if ((ts == null) || (ts.size() <= 0))
      throw new RuntimeException("not end node has no out transitions");
    ArrayList tarfts;
    if (ts.size() == 1)
    {
      BizFlow.ActNode tn = ((BizFlow.Transition)ts.get(0)).getToNode();
      if (tn == null) {
        throw new RuntimeException("transition has no to node!");
      }
      tarfts = tn.getInTransitions();
      int insc = nid.getInsCount();
      if ((tarfts.size() > 1) && (tn.getJoin() == BizFlow.AndXor.AND))
      {
        for (BizFlow.Transition tmpt : tarfts)
        {
          String tmpfnid = tmpt.getFromNodeId();
          BizFlowInsContext.NodeId2InsItem niii = this.insContext.getNodeId2InsMap().getNodeInsItemByNodeId(tmpfnid);
          if (niii.isRunning())
          {
            return;
          }

          if (niii.getInsCount() < insc)
          {
            return;
          }
        }

      }

      BizFlowInsContext.NodeId2InsItem tarnid = this.insContext.getNodeId2InsMap().getNodeInsItemByNodeId(tn.getNodeId());
      tarnid.increateInsCount();
      tarnid.setRunning(true);

      this.insContext.bDirty = true;
    }
    else if (n.getSplit() == BizFlow.AndXor.AND)
    {
      for (BizFlow.Transition t : ts)
      {
        String tnid = t.getToNodeId();
        if ((tnid == null) || (tnid.equals(""))) {
          throw new RuntimeException("transition has no to node!");
        }
        BizFlowInsContext.NodeId2InsItem tarnid = this.insContext.getNodeId2InsMap().getNodeInsItemByNodeId(tnid);
        tarnid.increateInsCount();
        tarnid.setRunning(true);

        this.insContext.bDirty = true;
      }
    }
    else
    {
      BizFlow.Transition defaultt = null;
      BizFlow.Transition select_t = null;
      String seltransid = nid.getSelectTransId();
      if (Convert.isNotNullEmpty(seltransid))
      {
        for (BizFlow.Transition t : ts)
        {
          if (t.getTransitionId().equals(seltransid))
          {
            select_t = t;
            break;
          }
        }
      }

      if (select_t == null)
      {
        for (BizFlow.Transition t : ts)
        {
          BizCondition bc = t.getCondition();
          BizCondition.CondType ct = bc.getCondType();
          if (ct == BizCondition.CondType.Normal)
          {
            BoolExp be = bc.getConditionBoolExp();
            if (be != null)
            {
              if (be.checkWithContext(this.transRC))
              {
                select_t = t;
                break;
              }

            }
            else if (t.getTransitionId().equals(seltransid))
            {
              select_t = t;
              nid.setSelectTransId(null);
              break;
            }

          }
          else if (ct == BizCondition.CondType.NormalDefault)
          {
            defaultt = t;
          }
        }
      }

      if (select_t == null)
      {
        if (defaultt == null)
          throw new RuntimeException(
            "no default transition found!");
        select_t = defaultt;
      }

      String tnid = select_t.getToNodeId();
      if ((tnid == null) || (tnid.equals(""))) {
        throw new RuntimeException("transition has no to node!");
      }
      BizFlowInsContext.NodeId2InsItem tarnid = this.insContext.getNodeId2InsMap().getNodeInsItemByNodeId(tnid);
      tarnid.increateInsCount();
      tarnid.setRunning(true);

      this.insContext.bDirty = true;

      return;
    }
  }

  public static ArrayList<BizFlow.ActNode> checkNodeFinishOutNextNodes(XmlData cxt_data, BizFlow.ActNode n)
    throws Exception
  {
    ArrayList ts = n.getOutTransitions();
    if ((ts == null) || (ts.size() <= 0)) {
      return null;
    }
    ArrayList rets = new ArrayList();
    if (ts.size() == 1)
    {
      BizFlow.ActNode tn = ((BizFlow.Transition)ts.get(0)).getToNode();
      if (tn == null) {
        throw new RuntimeException("transition has no to node!");
      }

      rets.add(tn);
      return rets;
    }

    if (n.getSplit() == BizFlow.AndXor.AND)
    {
      for (BizFlow.Transition t : ts)
      {
        BizFlow.ActNode tn = t.getToNode();
        if ((tn == null) || (tn.equals("")))
          throw new RuntimeException("transition has no to node!");
        rets.add(tn);
      }

      return rets;
    }

    BizFlow.Transition defaultt = null;
    BizFlow.Transition select_t = null;

    XmlDataRunContext rc = new XmlDataRunContext(cxt_data);
    for (BizFlow.Transition t : ts)
    {
      BizCondition bc = t.getCondition();
      BizCondition.CondType ct = bc.getCondType();
      if (ct == BizCondition.CondType.Normal)
      {
        BoolExp be = bc.getConditionBoolExp();
        if (be.checkWithContext(rc))
        {
          select_t = t;
          break;
        }
      }
      else if (ct == BizCondition.CondType.NormalDefault)
      {
        defaultt = t;
      }
    }

    if (select_t == null)
    {
      if (defaultt == null)
        throw new RuntimeException(
          "no default transition found!");
      select_t = defaultt;
    }

    BizFlow.ActNode tnid = select_t.getToNode();
    if ((tnid == null) || (tnid.equals("")))
      throw new RuntimeException("transition has no to node!");
    rets.add(tnid);
    return rets;
  }

  public static List<BizFlow.Transition> checkNeedManualSelectTransition(String flowpath, String nodeid, XmlData cxt_data)
    throws Exception
  {
    BizFlow bf = BizManager.getInstance().getBizFlowByPath(flowpath);
    if (bf == null) {
      throw new Exception("no biz flow found=" + flowpath);
    }
    BizFlow.ActNode n = (BizFlow.ActNode)bf.getNode(nodeid);
    if (n == null) {
      throw new Exception("no biz node found=" + nodeid);
    }
    BizFlow.AndXor ax = n.getSplit();
    if (ax == BizFlow.AndXor.AND) {
      return null;
    }
    ArrayList ts = n.getOutTransitions();
    if ((ts == null) || (ts.size() <= 1)) {
      return null;
    }
    ArrayList rets = new ArrayList();
    XmlDataRunContext rc = new XmlDataRunContext(cxt_data);
    BizFlow.Transition t_default = null;
    ArrayList t_fits = new ArrayList();
    for (BizFlow.Transition t : ts)
    {
      BizCondition bc = t.getCondition();
      if (bc != null)
      {
        if (bc.getCondType() == BizCondition.CondType.NormalDefault)
        {
          t_default = t;
        }
        else if (bc.getCondType() == BizCondition.CondType.Normal)
        {
          BoolExp be = bc.getConditionBoolExp();
          if (be == null)
          {
            rets.add(t);
          }
          else if (be.checkWithContext(rc))
          {
            t_fits.add(t);
          }
        }
      }
    }
    if (rets.size() > 0)
    {
      rets.addAll(t_fits);
      if (t_default != null)
        rets.add(t_default);
      return rets;
    }

    if (t_fits.size() > 1)
    {
      if (t_default != null)
        t_fits.add(t_default);
      return t_fits;
    }

    return null;
  }

  public static ArrayList<BizFlow.ActNode> checkNodeFinishOutRunNodes(boolean endwith_manualnode, XmlData cxt_data, BizFlow.ActNode n, String sel_transid)
    throws Exception
  {
    ArrayList ts = n.getOutTransitions();
    if ((ts == null) || (ts.size() <= 0)) {
      return null;
    }
    ArrayList rets = new ArrayList();
    if (ts.size() == 1)
    {
      List tns = ((BizFlow.Transition)ts.get(0)).getOutNodesInSinglePath(endwith_manualnode);
      if ((tns == null) || (tns.size() == 0))
      {
        return rets;
      }

      rets.addAll(tns);
      return rets;
    }

    if (n.getSplit() == BizFlow.AndXor.AND)
    {
      for (BizFlow.Transition t : ts)
      {
        List tns = t.getOutNodesInSinglePath(endwith_manualnode);
        if ((tns != null) && (tns.size() > 0))
        {
          rets.addAll(tns);
        }
      }
      return rets;
    }

    BizFlow.Transition defaultt = null;
    BizFlow.Transition select_t = null;

    XmlDataRunContext rc = new XmlDataRunContext(cxt_data);

    if (Convert.isNotNullEmpty(sel_transid))
    {
      for (BizFlow.Transition t : ts)
      {
        if (t.getTransitionId().equals(sel_transid))
        {
          select_t = t;
          break;
        }
      }
    }

    if (select_t == null)
    {
      for (BizFlow.Transition t : ts)
      {
        BizCondition bc = t.getCondition();
        BizCondition.CondType ct = bc.getCondType();
        if (ct == BizCondition.CondType.Normal)
        {
          BoolExp be = bc.getConditionBoolExp();
          if (be != null)
          {
            if (be.checkWithContext(rc))
            {
              select_t = t;
              break;
            }

          }
          else if (t.getTransitionId().equals(sel_transid))
          {
            select_t = t;
            break;
          }

        }
        else if (ct == BizCondition.CondType.NormalDefault)
        {
          defaultt = t;
        }
      }
    }

    if (select_t == null)
    {
      if (defaultt == null)
        throw new RuntimeException(
          "no default transition found!");
      select_t = defaultt;
    }

    List tnids = select_t.getOutNodesInSinglePath(endwith_manualnode);
    if ((tnids != null) && (tnids.size() > 0))
      rets.addAll(tnids);
    return rets;
  }

  public static ArrayList<BizFlow.NodePerformer> checkManualNodePerformersByNode(BizFlow.ActNode node, UserProfile up, XmlData inputxd, String sel_transid)
    throws Exception
  {
    ArrayList nextns = checkNodeFinishOutRunNodes(true, inputxd, node, sel_transid);
    if ((nextns == null) || (nextns.size() <= 0)) {
      return null;
    }
    BizFlow bf = node.getBelongFlow();

    ArrayList nps = bf.getAllPerformerNodes();
    if ((nps == null) || (nps.size() <= 0)) {
      return nps;
    }
    ArrayList rets = new ArrayList();

    for (BizFlow.NodePerformer np : nps)
    {
      String npnn = np.getNodeName();
      if (inputxd != null)
      {
        Object tmpvv = inputxd.getParamValue(npnn);
        if ((tmpvv != null) && (!tmpvv.toString().equals("")));
      }
      else {
        boolean has_next_related = false;
        for (BizFlow.ActNode nn : nextns)
        {
          if (np.hasRelation(nn))
          {
            has_next_related = true;
            break;
          }
        }

        if (has_next_related)
        {
          rets.add(np);
        }
      }
    }
    return rets;
  }

  public static ArrayList<BizFlow.NodePerformer> checkManualNodePerformersByNode0(BizFlow.ActNode node, UserProfile up, XmlData inputxd, String sel_transid)
    throws Exception
  {
    ArrayList nextns = checkNodeFinishOutRunNodes(false, inputxd, node, sel_transid);
    if ((nextns == null) || (nextns.size() <= 0)) {
      return null;
    }

    ArrayList nps = node.getManualAssignNodePerformers();
    if ((nps == null) || (nps.size() <= 0)) {
      return nps;
    }
    ArrayList rets = new ArrayList();

    for (BizFlow.NodePerformer np : nps)
    {
      boolean has_next_related = false;
      for (BizFlow.ActNode nn : nextns)
      {
        if (np.hasRelation(nn))
        {
          has_next_related = true;
          break;
        }
      }

      if (has_next_related)
      {
        rets.add(np);
      }
    }
    return rets;
  }

  public void suspendIt()
  {
  }

  public void resumeIt()
  {
  }

  public void abortIt()
  {
  }

  public boolean checkUserCanAbortFlowIns(UserProfile up)
  {
    BizFlow bf = getBizFlow();
    List nps = bf.getAllPerformerNodes();
    if ((nps == null) || (nps.size() <= 0)) {
      return false;
    }
    if (up.isAdministrator()) {
      return true;
    }
    for (BizFlow.NodePerformer np : nps)
    {
      BizParticipant bp = np.getParticipant();
      if (bp != null)
      {
        if (bp.canAbortFlow())
        {
          String nn = np.getNodeName();
          if (!Convert.isNullOrEmpty(nn))
          {
            Object dfv = this.insContext.getDataFieldVal(nn);
            if (up.getUserName().equals(dfv))
              return true; 
          }
        }
      }
    }
    return false;
  }

  void doEvent(BizFlow.EventType et) throws Exception
  {
    BizFlow bf = getBizFlow();
    ArrayList nas = bf.getOnEventNodeActions(et);
    if (nas != null)
    {
      for (BizFlow.NodeAction na : nas)
        runEventActionNode(na);
    }
  }

  public void terminateIt()
  {
  }

  public ArrayList<BizFlow.NodePerformer> getUnassignedManualNodePerformersByNodeId(String nodeid)
  {
    BizFlow.Node n = getBizFlow().getNode(nodeid);
    if (n == null)
    {
      return null;
    }

    if (!(n instanceof BizFlow.ActNode))
    {
      return null;
    }

    BizFlow.ActNode actn = (BizFlow.ActNode)n;
    List nps = actn.getManualAssignNodePerformers();
    if ((nps == null) || (nps.size() <= 0)) {
      return null;
    }
    ArrayList rets = new ArrayList();

    for (BizFlow.NodePerformer np : nps)
    {
      String un = getNodePerformerAssignedUser(np);
      if (!Convert.isNotNullEmpty(un))
      {
        rets.add(np);
      }
    }
    return rets;
  }

  public void setUnassignedManualNodePerformerUser(HashMap<String, String> np_name2username, boolean b_save_cxt)
    throws Exception
  {
    if ((np_name2username == null) || (np_name2username.size() <= 0)) {
      return;
    }

    List nps = getBizFlow().getAllPerformerNodes();
    if ((nps == null) || (nps.size() <= 0)) {
      return;
    }
    boolean bdirty = false;
    for (BizFlow.NodePerformer np : nps)
    {
      String un = getNodePerformerAssignedUser(np);
      if (!Convert.isNotNullEmpty(un))
      {
        un = (String)np_name2username.get(np.getNodeName());
        if (!Convert.isNullOrEmpty(un))
        {
          setNodePerformerAssignedUser(np, un);
          bdirty = true;
        }
      }
    }
    if ((b_save_cxt) && (bdirty))
    {
      this.insContext.saveContext();
    }
  }

  class TransitionRunner extends BoolExpRunner
  {
    TransitionRunner()
    {
    }

    public ValWrapper getRunnerVarValue(String var_name)
    {
      if ("cur_flow_ins".equalsIgnoreCase(var_name)) {
        return new ValWrapper(BizFlowIns.this);
      }
      return null;
    }

    public ValWrapper getRunnerPropValue(String propname)
    {
      return new ValWrapper(BizFlowIns.this.insContext.getXmlDataFieldVal(propname));
    }

    public boolean checkTransition(BizFlow.Transition t) throws Exception
    {
      BizCondition bc = t.getCondition();
      if (bc == null) {
        return false;
      }
      BizCondition.CondType ct = bc.getCondType();
      if (ct == BizCondition.CondType.Normal)
      {
        return runExp(bc.getConditionContent());
      }

      if (ct == BizCondition.CondType.Exception)
      {
        return runExp(bc.getConditionContent());
      }

      throw new Exception("BizCondition with type=" + ct.toString() + 
        " need not check!");
    }
  }

  static class XmlDataRunContext extends RunContext
  {
    private XmlData xd = null;

    public XmlDataRunContext(XmlData xd)
    {
      this.xd = xd;
    }

    public ValWrapper getValueWrapper(String var_name)
    {
      ValWrapper vw = super.getValueWrapper(var_name);
      if (vw != null) {
        return vw;
      }
      if ((var_name.startsWith("@")) && (this.xd != null)) {
        return new ValWrapper(this.xd.getParamValue(var_name.substring(1)));
      }
      return new ValWrapper(null);
    }
  }

  static class NodeInputRunCxt extends RunContext
  {
    XmlData dataFieldXD = null;
    ValWrapper dataFieldVW = null;

    XmlData inputXD = null;
    ValWrapper inputVW = null;

    ValWrapper insVW = null;

    public NodeInputRunCxt(XmlData ins_xd, XmlData datafield)
    {
      this.dataFieldXD = datafield.copyMe();
      this.dataFieldVW = new ValWrapper(this.dataFieldXD);

      this.inputXD = new XmlData();
      this.inputVW = new ValWrapper(this.inputXD);

      this.insVW = new ValWrapper(ins_xd);
    }

    public XmlData getInputXmlData()
    {
      return this.inputXD;
    }

    public ValWrapper getValueWrapper(String var_name)
    {
      if (("@datafield".equalsIgnoreCase(var_name)) || 
        ("@df".equalsIgnoreCase(var_name))) {
        return this.dataFieldVW;
      }
      if (("@input".equalsIgnoreCase(var_name)) || 
        ("@inputs".equalsIgnoreCase(var_name)) || 
        ("@i".equalsIgnoreCase(var_name))) {
        return this.inputVW;
      }
      if (("@instance".equalsIgnoreCase(var_name)) || 
        ("@ins".equalsIgnoreCase(var_name))) {
        return this.insVW;
      }
      return super.getValueWrapper(var_name);
    }
  }

  static class NodeOutputRunCxt extends RunContext
  {
    XmlData outputXD = null;
    ValWrapper outputVW = null;

    XmlData updateDFXD = null;
    ValWrapper updateDFVW = null;

    public NodeOutputRunCxt(XmlData outputxd)
    {
      this.outputXD = outputxd.copyMe();
      this.outputVW = new ValWrapper(this.outputXD);

      this.updateDFXD = new XmlData();
      this.updateDFVW = new ValWrapper(this.updateDFXD);
    }

    public XmlData getUpdateToDFXmlData()
    {
      return this.updateDFXD;
    }

    public ValWrapper getValueWrapper(String var_name)
    {
      if (("@datafield".equalsIgnoreCase(var_name)) || 
        ("@df".equalsIgnoreCase(var_name))) {
        return this.updateDFVW;
      }
      if (("@output".equalsIgnoreCase(var_name)) || 
        ("@outputs".equalsIgnoreCase(var_name)) || 
        ("@o".equalsIgnoreCase(var_name))) {
        return this.outputVW;
      }
      return super.getValueWrapper(var_name);
    }
  }
}
