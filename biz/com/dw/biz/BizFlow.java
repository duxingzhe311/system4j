package com.dw.biz;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dw.system.Convert;
import com.dw.system.gdb.datax.DataXClass;
import com.dw.system.gdb.datax.DataXItem;
import com.dw.system.gdb.datax.IDataXContainer;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataMember;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlVal;
import com.dw.system.xmldata.XmlValDef;
import com.dw.system.xmldata.join.JoinBoard;
import com.dw.system.xmldata.join.JoinInterface;
import com.dw.system.xmldata.join.JoinType;
import com.dw.system.xmldata.join.JoinUnit;
import com.dw.system.xmldata.xrmi.XRmi;
import com.dw.user.right.RightRule;

@XRmi(reg_name="biz_flow")
public class BizFlow extends BizNodeObj
  implements IXmlDataable, IBizHasInnerView
{
  String title = null;

  String desc = null;

  Hashtable<String, Node> id2node = new Hashtable();

  Hashtable<String, Transition> id2transition = new Hashtable();

  DataFieldContainer dataFieldContainer = new DataFieldContainer();

  String flowInsTitleTemp = null;

  transient String[] flowInsTitleTempArray = null;

  public static boolean checkNodePathMidAcross(NodePath p1, NodePath p2)
  {
    ArrayList<ActNode> mns1 = p1.getMidNodes();
    if ((mns1 == null) || (mns1.size() <= 0)) {
      return false;
    }
    ArrayList<ActNode> mns2 = p2.getMidNodes();
    if ((mns2 == null) || (mns2.size() <= 0)) {
      return false;
    }
    for (ActNode n : mns1)
    {
      if (mns2.contains(n)) {
        return true;
      }
    }
    return false;
  }

  public Node constructNodeFromXmlData(XmlData xd)
  {
    NodeType nt = NodeType.valueOf(xd.getParamValueStr("node_type"));
    if (nt == NodeType.biz_action)
    {
      NodeAction na = new NodeAction();
      na.fromXmlData(xd);
      return na;
    }
    if (nt == NodeType.biz_view)
    {
      NodeView na = new NodeView();
      na.fromXmlData(xd);
      return na;
    }
    if (nt == NodeType.router)
    {
      NodeRouter na = new NodeRouter();
      na.fromXmlData(xd);
      return na;
    }
    if (nt == NodeType.biz_flow)
    {
      NodeSubFlow na = new NodeSubFlow();
      na.fromXmlData(xd);
      return na;
    }
    if (nt == NodeType.start)
    {
      NodeStart ns = new NodeStart();
      ns.fromXmlData(xd);
      return ns;
    }
    if (nt == NodeType.end)
    {
      NodeEnd ne = new NodeEnd();
      ne.fromXmlData(xd);
      return ne;
    }
    if (nt == NodeType.performer)
    {
      NodePerformer np = new NodePerformer();
      np.fromXmlData(xd);
      return np;
    }
    if (nt == NodeType.flow_ctrl_view)
    {
      FlowCtrlViewNode fcvn = new FlowCtrlViewNode();
      fcvn.fromXmlData(xd);
      return fcvn;
    }

    return null;
  }

  DataField constructDataFieldFromXmlData(XmlData xd)
  {
    DataFieldStyle dfs = DataFieldStyle.valueOf(xd
      .getParamValueStr("style"));
    if (dfs == DataFieldStyle.xmlval)
    {
      DataFieldXmlVal df = new DataFieldXmlVal();
      df.fromXmlData(xd);
      return df;
    }
    if (dfs == DataFieldStyle.xmldata)
    {
      DataFieldXmlData df = new DataFieldXmlData();
      df.fromXmlData(xd);
      return df;
    }
    if (dfs == DataFieldStyle.datax)
    {
      DataFieldDataX df = new DataFieldDataX();
      df.fromXmlData(xd);
      return df;
    }

    return null;
  }

  public void exportOutDataToDataField(ActNode n)
    throws Exception
  {
    XmlDataStruct xds = null;
    if ((n instanceof NodeView))
    {
      xds = n.getNodeInOutDataStruct();
    }
    else if ((n instanceof NodeAction))
    {
      xds = ((NodeAction)n).getOutDataStruct();
    }

    if (xds != null)
    {
      for (XmlDataMember m : xds.getSubXmlDataMembers())
      {
        DataFieldXmlVal df = new DataFieldXmlVal(m.getName());
        df.setXmlValDataField(m.getXmlValDef());
        getDataFieldContainer().setDataField(df);
      }
    }
  }

  public BizFlow()
  {
  }

  public BizFlow(String title, String desc)
  {
    this.title = title;
    this.desc = desc;
  }

  public BizView getInnerViewById(String id) throws Exception
  {
    Node n = getNode(id);
    if (n == null) {
      return null;
    }
    if ((n instanceof NodeStart))
    {
      NodeStart ns = (NodeStart)n;
      return ns.getBizView();
    }

    if ((n instanceof NodeView))
    {
      NodeView nv = (NodeView)n;
      if (nv.isInnerView()) {
        return nv.getBizView();
      }
      return null;
    }

    return null;
  }

  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String t)
  {
    this.title = t;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public void setDesc(String d)
  {
    this.desc = d;
  }

  public String getFlowInsTitleTemp()
  {
    return this.flowInsTitleTemp;
  }

  public void setFlowInsTitleTemp(String tt)
  {
    this.flowInsTitleTemp = tt;

    this.flowInsTitleTempArray = null;
  }

  public String[] getFlowInsTitleTempArray()
  {
    if (this.flowInsTitleTempArray != null) {
      return this.flowInsTitleTempArray;
    }
    if (Convert.isNullOrEmpty(this.flowInsTitleTemp))
    {
      this.flowInsTitleTempArray = new String[0];
      return this.flowInsTitleTempArray;
    }

    ArrayList rets = new ArrayList();
    String left_str = this.flowInsTitleTemp;
    int p = -1;
    int q = -1;
    while (((p = left_str.indexOf("[@")) >= 0) && 
      ((q = left_str.indexOf(']', p + 1)) > p + 1))
    {
      if (p > 0)
      {
        rets.add(left_str.substring(0, p));
      }

      String tmps = left_str.substring(p + 2, q).trim();
      if (Convert.isNotNullEmpty(tmps)) {
        rets.add("@" + tmps);
      }
      left_str = left_str.substring(q + 1);
    }

    if (Convert.isNotNullEmpty(left_str)) {
      rets.add(left_str);
    }
    String[] ss = new String[rets.size()];
    rets.toArray(ss);
    this.flowInsTitleTempArray = ss;
    return this.flowInsTitleTempArray;
  }

  public void setStartNode(int x, int y)
  {
    NodeStart ns = getStartNode();
    if (ns == null)
    {
      ns = new NodeStart(UUID.randomUUID().toString(), x, y);
      this.id2node.put(ns.getNodeId(), ns);
    }

    ns.setX(x);
    ns.setY(y);
  }

  public NodeStart getStartNode()
  {
    for (Node n : this.id2node.values())
    {
      if ((n instanceof NodeStart))
        return (NodeStart)n;
    }
    return null;
  }

  public XmlDataStruct getInputXmlDataStruct()
    throws Exception
  {
    return null;
  }

  public NodeEnd getEndNode()
  {
    for (Node n : this.id2node.values())
    {
      if ((n instanceof NodeEnd))
        return (NodeEnd)n;
    }
    return null;
  }

  public void setEndNode(int x, int y)
  {
    NodeEnd en = getEndNode();
    if (en == null)
    {
      en = new NodeEnd(UUID.randomUUID().toString(), x, y);
      this.id2node.put(en.getNodeId(), en);
    }

    en.setX(x);
    en.setY(y);
  }

  public ActNode setNode(int x, int y, BizView bv)
  {
    String nid = UUID.randomUUID().toString();
    NodeView tmpnv = new NodeView(bv, nid, x, y);
    this.id2node.put(tmpnv.getNodeId(), tmpnv);
    return tmpnv;
  }

  public FlowCtrlViewNode setFlowCtrlViewNode(int x, int y, BizView bv)
  {
    String nid = UUID.randomUUID().toString();
    FlowCtrlViewNode tmpnv = new FlowCtrlViewNode(bv, nid, x, y);
    this.id2node.put(tmpnv.getNodeId(), tmpnv);
    return tmpnv;
  }

  public ActNode setNode(int x, int y, BizAction ba)
  {
    String nid = UUID.randomUUID().toString();
    NodeAction tmpnv = new NodeAction(ba, nid, x, y);
    this.id2node.put(tmpnv.getNodeId(), tmpnv);
    return tmpnv;
  }

  public ActNode setNode(int x, int y, BizFlow bf)
  {
    String nid = UUID.randomUUID().toString();
    NodeSubFlow tmpnv = new NodeSubFlow(bf, nid, x, y);
    this.id2node.put(tmpnv.getNodeId(), tmpnv);
    return tmpnv;
  }

  public NodeRouter setRouterNode(String name, int x, int y)
  {
    String nid = UUID.randomUUID().toString();
    NodeRouter tmpnv = new NodeRouter(nid, name, x, y);
    this.id2node.put(tmpnv.getNodeId(), tmpnv);
    return tmpnv;
  }

  public NodePerformer setNodePerformer(String name, int x, int y)
    throws Exception
  {
    DataField df = this.dataFieldContainer.getDataField(name);
    if (df != null) {
      throw new Exception("Data Field contains name=" + name);
    }
    NodePerformer np = getNodePerformer(name);
    if (np != null) {
      throw new Exception("Node Performer with name=" + name + " is already existed!");
    }

    this.dataFieldContainer.setDataField(name, DataFieldStyle.xmlval);

    String nid = UUID.randomUUID().toString();
    NodePerformer tmpnv = new NodePerformer(nid, name, x, y);
    this.id2node.put(tmpnv.getNodeId(), tmpnv);

    return tmpnv;
  }

  public NodeView setInnerView(String name, int x, int y)
    throws Exception
  {
    String nid = UUID.randomUUID().toString().replace("-", "");
    BizView bv = new BizView(nid, this, "");
    NodeView tmpnv = new NodeView(bv, nid, x, y);
    this.id2node.put(tmpnv.getNodeId(), tmpnv);
    return tmpnv;
  }

  public void removeNode(Node n)
  {
    this.id2node.remove(n.getNodeId());

    if ((n instanceof NodePerformer))
    {
      this.dataFieldContainer.unsetDataField(n.getNodeName());
    }
  }

  public Node[] getAllNodes()
  {
    Node[] rets = new Node[this.id2node.size()];
    this.id2node.values().toArray(rets);
    return rets;
  }

  public ActNode[] getAllActNodes()
  {
    ArrayList ans = new ArrayList();
    for (Node n : this.id2node.values())
    {
      if ((n instanceof ActNode))
        ans.add((ActNode)n);
    }
    ActNode[] rets = new ActNode[ans.size()];
    ans.toArray(rets);
    return rets;
  }

  public ArrayList<NodeAction> getOnEventNodeActions(EventType et)
  {
    if (et == null) {
      return null;
    }
    ArrayList ret = new ArrayList();
    for (ActNode an : getAllActNodes())
    {
      if ((an instanceof NodeAction))
      {
        NodeAction na = (NodeAction)an;
        if (na.getOnEvent() == et)
          ret.add(na);
      }
    }
    return ret;
  }

  public ArrayList<NodePerformer> getAllPerformerNodes()
  {
    ArrayList ans = new ArrayList();
    for (Node n : this.id2node.values())
    {
      if ((n instanceof NodePerformer)) {
        ans.add((NodePerformer)n);
      }
    }
    Convert.sort(ans);

    return ans;
  }

  public NodePerformer getNodePerformer(String name)
  {
    for (NodePerformer np : getAllPerformerNodes())
    {
      if (np.getNodeName().equals(name))
        return np;
    }
    return null;
  }

  public NodePerformer getNodePerformerByNodeViewId(String nodeid)
  {
    Node n = getNode(nodeid);
    if (n == null) {
      return null;
    }
    if ((n instanceof NodeView))
    {
      NodeView nv = (NodeView)n;
      return nv.getRelatedNodePerformer();
    }

    return null;
  }

  public NodePerformer getRelatedNodePerformerByNodeViewId(String nodeid)
  {
    Node n = getNode(nodeid);
    if (n == null) {
      return null;
    }
    if ((n instanceof NodeView))
    {
      NodeView nv = (NodeView)n;
      return nv.getRelatedNodePerformer();
    }

    return null;
  }

  public Node getNode(String nodeid)
  {
    return (Node)this.id2node.get(nodeid);
  }

  public Transition getTransition(ActNode fromn, ActNode ton)
  {
    for (Transition t : this.id2transition.values())
    {
      if ((t.getFromNode() == fromn) && (t.getToNode() == ton))
        return t;
    }
    return null;
  }

  public Transition addTransition(Point pos) throws Exception
  {
    String nid = UUID.randomUUID().toString();
    Transition t = new Transition(nid, pos);
    this.id2transition.put(t.getTransitionId(), t);
    return t;
  }

  public void unsetTransition(Transition t)
  {
    this.id2transition.remove(t.getTransitionId());
  }

  public ArrayList<ActNode> getPrevNodeList(ActNode n)
  {
    ArrayList<ActNode> ns = new ArrayList<ActNode>();
    ArrayList<Transition> ts = n.getInTransitions();
    for (Transition t : ts)
    {
      ActNode fn = t.getFromNode();
      if (fn != null)
        ns.add(fn);
    }
    return ns;
  }

  public ArrayList<NodeTransitionWrapper> getPrevNodeTransListWithIgnore(ActNode n, ActNode ignoren)
  {
    ArrayList<NodeTransitionWrapper> rets = new ArrayList<NodeTransitionWrapper>();
    ArrayList<Transition> ts = n.getInTransitions();
    for (Transition t : ts)
    {
      ActNode fn = t.getFromNode();
      if ((fn == null) || (!fn.equals(ignoren)))
      {
        NodeTransitionWrapper ntw = new NodeTransitionWrapper(fn, t);
        rets.add(ntw);
      }
    }
    return rets;
  }

  public boolean hasPrevNode(ActNode n)
  {
    ArrayList<ActNode> pns = getPrevNodeList(n);
    return pns.size() > 0;
  }

  public ArrayList<ActNode> getNextNodeList(ActNode n)
  {
    ArrayList<ActNode> ns = new ArrayList<ActNode>();
    ArrayList<Transition> ts = n.getOutTransitions();
    for (Transition t : ts)
    {
      ActNode fn = t.getToNode();
      if (fn != null)
        ns.add(fn);
    }
    return ns;
  }

  public ArrayList<NodeTransitionWrapper> getNextNodeTransListWithIgnore(ActNode n, ActNode ignoren)
  {
    ArrayList<NodeTransitionWrapper> rets = new ArrayList<NodeTransitionWrapper>();
    ArrayList<Transition> ts = n.getOutTransitions();
    for (Transition t : ts)
    {
      ActNode fn = t.getToNode();
      if ((fn == null) || (!fn.equals(ignoren)))
      {
        NodeTransitionWrapper ntw = new NodeTransitionWrapper(fn, t);
        rets.add(ntw);
      }
    }
    return rets;
  }

  public boolean hasNextNode(ActNode n)
  {
    return getNextNodeList(n).size() > 0;
  }

  public Transition[] getAllTransitions()
  {
    Transition[] rets = new Transition[this.id2transition.size()];
    this.id2transition.values().toArray(rets);
    return rets;
  }

  public ArrayList<NodePath> getAllNodePath(ActNode fromn, ActNode ton)
  {
    if (fromn.equals(ton)) {
      return null;
    }
    ArrayList rets = new ArrayList();
    HashSet ignorens = new HashSet();
    ignorens.add(fromn);
    ignorens.add(ton);

    ArrayList curmidns = new ArrayList();
    getAllNodePathAsNodeList(fromn, ton, rets, 
      ignorens, 
      curmidns, 
      fromn);

    return rets;
  }

  private void getAllNodePathAsNodeList(ActNode startn, ActNode endn, ArrayList<NodePath> nps, HashSet<ActNode> ignorens, ArrayList<ActNode> cur_mid_ns, ActNode fromn)
  {
    if (fromn == null)
    {
      return;
    }

    ignorens.add(fromn);

    if (fromn.equals(endn))
    {
      return;
    }

    ArrayList<ActNode> nextns = getNextNodeList(fromn);
    if ((nextns == null) || (nextns.size() <= 0)) {
      return;
    }
    for (ActNode nextn : nextns)
    {
      if (nextn.equals(endn))
      {
        NodePath nnp = new NodePath(startn, endn, cur_mid_ns);
        nps.add(nnp);
      }
      else if (!ignorens.contains(nextn))
      {
        ArrayList nmidns = new ArrayList(
          cur_mid_ns.size() + 1);
        nmidns.addAll(cur_mid_ns);
        if (!fromn.equals(startn))
          nmidns.add(fromn);
        HashSet nignorens = new HashSet();
        nignorens.addAll(ignorens);
        getAllNodePathAsNodeList(startn, endn, nps, nignorens, 
          nmidns, 
          nextn);
      }
    }
  }

  public boolean checkCloseInWithIgnoreTransition(HashSet<Transition> ignore_trans, ActNode fromn, ActNode ton)
  {
    if (fromn.equals(ton)) {
      return true;
    }
    HashSet trans = new HashSet();
    HashSet nodes = new HashSet();
    return getAllCloseInNodesAndTransitions(ignore_trans, fromn, ton, 
      trans, nodes, fromn);
  }

  private boolean getAllCloseInNodesAndTransitions(HashSet<Transition> ignore_trans, ActNode fromn, ActNode ton, HashSet<Transition> trans, HashSet<ActNode> nodes, ActNode curnode)
  {
    nodes.add(curnode);

    ArrayList<Transition> ints = curnode.getInTransitions();
    if ((ints == null) || (ints.size() <= 0))
    {
      if (!fromn.equals(curnode))
        return false;
    }
    ArrayList<Transition> outts = curnode.getOutTransitions();
    if ((outts == null) || (outts.size() <= 0))
    {
      if (!ton.equals(curnode)) {
        return false;
      }
    }
    for (Transition t : ints)
    {
      if (!ignore_trans.contains(t))
      {
        if (!trans.contains(t))
        {
          trans.add(t);

          ActNode tmpn = t.getFromNode();
          if (tmpn == null) {
            return false;
          }
          if (!nodes.contains(tmpn))
          {
            boolean bv = getAllCloseInNodesAndTransitions(ignore_trans, fromn, 
              ton, trans, nodes, tmpn);
            if (!bv)
              return false; 
          }
        }
      }
    }
    for (Transition t : outts)
    {
      if (!ignore_trans.contains(t))
      {
        if (!trans.contains(t))
        {
          trans.add(t);

          ActNode tmpn = t.getToNode();
          if (tmpn == null) {
            return false;
          }
          if (!nodes.contains(tmpn))
          {
            boolean bv = getAllCloseInNodesAndTransitions(ignore_trans, fromn, 
              ton, trans, nodes, tmpn);
            if (!bv)
              return false; 
          }
        }
      }
    }
    return true;
  }

  public boolean checkCanSetSplitJoinAnd(ActNode fromn, ActNode ton)
  {
    if (fromn.equals(ton))
    {
      return false;
    }

    ArrayList outts = fromn.getOutTransitions();
    int outts_num = outts.size();
    if (outts_num <= 1)
    {
      return false;
    }

    ArrayList tmpits = ton.getInTransitions();
    if (tmpits.size() != outts_num)
    {
      return false;
    }

    ArrayList bknps = getAllNodePath(ton, fromn);
    if (bknps.size() > 0) {
      return false;
    }

    ArrayList<NodeTransitionWrapper> nexts = getNextNodeTransListWithIgnore(
      fromn, ton);

    for (NodeTransitionWrapper ntw : nexts)
    {
      if (ntw.getNode() == null) {
        return false;
      }
    }
    ArrayList<NodeTransitionWrapper> prevs = getPrevNodeTransListWithIgnore(
      ton, fromn);
    for (NodeTransitionWrapper ntw : prevs)
    {
      if (ntw.getNode() == null) {
        return false;
      }
    }
    if (nexts.size() != prevs.size()) {
      return false;
    }

    while (nexts.size() > 0)
    {
      NodeTransitionWrapper nntw = (NodeTransitionWrapper)nexts.remove(nexts.size() - 1);
      for (NodeTransitionWrapper pntw : prevs)
      {
        HashSet ignts = new HashSet();
        ignts.add(nntw.getTransition());
        ignts.add(pntw.getTransition());
        if (checkCloseInWithIgnoreTransition(ignts, nntw.getNode(), 
          pntw.getNode()))
        {
          prevs.remove(pntw);
          break;
        }
      }

    }

    return (prevs.size() == 0) && (nexts.size() == 0);
  }

  public DataFieldContainer getDataFieldContainer()
  {
    return this.dataFieldContainer;
  }

  public void cleanFlow()
  {
    for (Node n : this.id2node.values())
    {
      if ((n instanceof ActNode))
        ((ActNode)n).cleanNode();
    }
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    if (this.title != null)
      xd.setParamValue("title", this.title);
    if (this.desc != null) {
      xd.setParamValue("desc", this.desc);
    }

    List nxds = xd.getOrCreateSubDataArray("nodes");
    for (Node n : this.id2node.values())
    {
      nxds.add(n.toXmlData());
    }

    List txds = xd.getOrCreateSubDataArray("transitions");
    for (Transition t : this.id2transition.values())
    {
      txds.add(t.toXmlData());
    }

    xd.setSubDataSingle("datafield_container", 
      this.dataFieldContainer.toXmlData());

    if (this.flowInsTitleTemp != null) {
      xd.setParamValue("ins_title_temp", this.flowInsTitleTemp);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.title = xd.getParamValueStr("title");
    this.desc = xd.getParamValueStr("desc");

    List<XmlData> nxds = xd.getSubDataArray("nodes");
    Node n;
    if (nxds != null)
    {
      for (XmlData tmpxd : nxds)
      {
        n = constructNodeFromXmlData(tmpxd);
        if (n != null)
        {
          this.id2node.put(n.getNodeId(), n);
        }
      }
    }

    List<XmlData> txds = xd.getSubDataArray("transitions");
    if (txds != null)
    {
      for (XmlData tmpxd : txds)
      {
        Transition t = new Transition();
        t.fromXmlData(tmpxd);
        this.id2transition.put(t.getTransitionId(), t);
      }

    }

    XmlData dfcxd = xd.getSubDataSingle("datafield_container");
    if (dfcxd != null) {
      this.dataFieldContainer.fromXmlData(dfcxd);
    }

    this.flowInsTitleTemp = xd.getParamValueStr("ins_title_temp");
  }

  public ArrayList<JoinUnit> getAllJoinUnits()
  {
    ArrayList jus = new ArrayList();
    DataField[] dfs = this.dataFieldContainer.getAllDataFields();
    if (dfs != null)
    {
      for (DataField df : dfs)
      {
        if ((df instanceof JoinUnit)) {
          jus.add((JoinUnit)df);
        }
      }
    }
    return jus;
  }

  public JoinUnit getJoinUnit(String name)
  {
    for (JoinUnit ju : getAllJoinUnits())
    {
      if (ju.getJoinUnitName().equals(name)) {
        return ju;
      }
    }
    return null;
  }

  public static enum AndXor
  {
    NULL, 
    AND, 
    XOR;
  }

  static enum NodeType
  {
    start, end, biz_view, biz_action, router, biz_flow, flow_ctrl_view, performer;
  }

  public static class NodePath
  {
    BizFlow.ActNode fromNode = null;

    BizFlow.ActNode toNode = null;

    ArrayList<BizFlow.ActNode> midNodes = null;

    public NodePath(BizFlow.ActNode fn, BizFlow.ActNode tn, List<BizFlow.ActNode> midn)
    {
      this.fromNode = fn;
      this.toNode = tn;
      if ((midn != null) && (midn.size() > 0))
      {
        this.midNodes = new ArrayList(midn.size());
        this.midNodes.addAll(midn);
      }
    }

    public BizFlow.ActNode getFromNode()
    {
      return this.fromNode;
    }

    public BizFlow.ActNode getToNode()
    {
      return this.toNode;
    }

    public ArrayList<BizFlow.ActNode> getMidNodes()
    {
      return this.midNodes;
    }

    public int getNodeNum()
    {
      if (this.midNodes == null) {
        return 2;
      }
      return 2 + this.midNodes.size();
    }
  }

  public static abstract interface ITitleable
  {
    public abstract String getTitle();

    public abstract void setTitle(String paramString);
  }

  public static abstract interface IViewCtrlable
  {
    public abstract List<String> getViewCellNames()
      throws Exception;

    public abstract BizView.Controller getViewCtrl();

    public abstract void setViewCtrl(BizView.Controller paramController);
  }

  public abstract class Node
    implements IXmlDataable, BizFlow.ITitleable
  {
    public static final int SIZE = 15;
    String id = null;

    String title = null;

    Point pos = new Point(-1, -1);

    BizInOutParam inputParam = new BizInOutParam();

    BizInOutParam outputParam = new BizInOutParam();

    HashMap<String, String> outputDataField2Exp = new HashMap();

    HashMap<String, String> paramDataField2InputMap = new HashMap();

    HashMap<String, String> paramOutput2DataFieldMap = new HashMap();

    protected Node()
    {
    }

    public Node(String nid, int x, int y)
    {
      this.id = nid;
      this.pos.x = x;
      this.pos.y = y;
    }

    public BizFlow getBelongFlow()
    {
      return BizFlow.this;
    }

    public final int getX()
    {
      return this.pos.x;
    }

    public final void setX(int x)
    {
      this.pos.x = x;
    }

    public final int getY()
    {
      return this.pos.y;
    }

    public final void setY(int y)
    {
      this.pos.y = y;
    }

    public String getTitle()
    {
      if (this.title == null)
        return "";
      return this.title;
    }

    public void setTitle(String t)
    {
      this.title = t;
    }

    public Rectangle getDrawingRect()
    {
      return new Rectangle(getX(), getY(), 20, 19);
    }

    public final Point getPoint()
    {
      return this.pos;
    }

    public final Point getCenterPoint()
    {
      int d = 10;
      return new Point(getX() + d, getY() + d);
    }

    public final int hashCode()
    {
      return getNodeId().hashCode();
    }

    public final boolean equals(Object o)
    {
      if (!(o instanceof Node)) {
        return false;
      }
      return getNodeId().equals(((Node)o).getNodeId());
    }

    public final String getNodeId()
    {
      return this.id;
    }

    public abstract String getNodeName();

    public abstract BizFlow.NodeType getNodeType();

    public BizInOutParam getInputParam()
    {
      return this.inputParam;
    }

    public void setInputParam(BizInOutParam ps)
    {
      this.inputParam = ps;
    }

    public BizInOutParam getOutputParam()
    {
      return this.outputParam;
    }

    public void setOutputParam(BizInOutParam ps)
    {
      this.outputParam = ps;
    }

    public final void setParamDataField2InputMap(HashMap<String, String> m)
    {
      this.paramDataField2InputMap.clear();
      if (m != null)
        for (Map.Entry n2n : m.entrySet())
        {
          this.paramDataField2InputMap.put((String)n2n.getKey(), (String)n2n.getValue());
        }
    }

    public final HashMap<String, String> getParamDataField2InputMap()
    {
      return this.paramDataField2InputMap;
    }

    public final void setParamOutput2DataFieldMap(HashMap<String, String> m)
    {
      this.paramOutput2DataFieldMap.clear();
      if (m != null)
        for (Map.Entry<String, String> n2n : m.entrySet())
        {
          this.paramOutput2DataFieldMap.put((String)n2n.getKey(), (String)n2n.getValue());
        }
    }

    public final HashMap<String, String> getParamOutput2DataFieldMap()
    {
      return this.paramOutput2DataFieldMap;
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("id", this.id);
      xd.setParamValue("node_type", getNodeType().toString());
      if (this.title != null)
        xd.setParamValue("node_title", this.title);
      xd.setParamValue("x", Integer.valueOf(this.pos.x));
      xd.setParamValue("y", Integer.valueOf(this.pos.y));

      if (this.inputParam != null) {
        xd.setSubDataSingle("input_param", this.inputParam.toXmlData());
      }

      if (this.outputParam != null)
        xd.setSubDataSingle("output_param", this.outputParam.toXmlData());
      Iterator localIterator;
      if (this.paramDataField2InputMap != null)
      {
        XmlData tmpxd = xd.getOrCreateSubDataSingle("datafield_input");

        localIterator = this.paramDataField2InputMap
          .entrySet().iterator();

        while (localIterator.hasNext()) {
          Map.Entry df2inp = (Map.Entry)localIterator.next();

          tmpxd.setParamValue((String)df2inp.getKey(), df2inp.getValue());
        }
      }
      if (this.paramOutput2DataFieldMap != null)
      {
        XmlData tmpxd = xd.getOrCreateSubDataSingle("output_datafield");

        localIterator = this.paramOutput2DataFieldMap
          .entrySet().iterator();

        while (localIterator.hasNext()) {
          Map.Entry df2inp = (Map.Entry)localIterator.next();

          tmpxd.setParamValue((String)df2inp.getKey(), df2inp.getValue());
        }
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      this.id = xd.getParamValueStr("id");
      this.title = xd.getParamValueStr("node_title");
      this.pos.x = xd.getParamValueInt32("x", 0);
      this.pos.y = xd.getParamValueInt32("y", 0);

      XmlData inputxd = xd.getSubDataSingle("input_param");
      if (inputxd != null) {
        this.inputParam.fromXmlData(inputxd);
      }
      XmlData outputxd = xd.getSubDataSingle("output_param");
      if (outputxd != null) {
        this.outputParam.fromXmlData(outputxd);
      }
      XmlData tmpxd = xd.getSubDataSingle("datafield_input");
      if (tmpxd != null)
      {
        for (String ns : tmpxd.getParamNames())
        {
          this.paramDataField2InputMap.put(ns, tmpxd.getParamValueStr(ns));
        }
      }

      tmpxd = xd.getSubDataSingle("output_datafield");
      if (tmpxd != null)
      {
        for (String ns : tmpxd.getParamNames())
        {
          this.paramOutput2DataFieldMap
            .put(ns, tmpxd.getParamValueStr(ns));
        }
      }
    }
  }

  public abstract class ActNode extends BizFlow.Node
  {
    BizFlow.AndXor join = BizFlow.AndXor.NULL;

    BizFlow.AndXor split = BizFlow.AndXor.NULL;

    private transient int flowOrderNum = -1;

    protected ActNode()
    {
      super();
    }

    public ActNode(String nid, int x, int y)
    {
      super(nid, x, y);
    }

    public final BizFlow.NodePerformer getRelatedNodePerformer()
    {
      List<BizFlow.NodePerformer> nps = BizFlow.this.getAllPerformerNodes();
      for (BizFlow.NodePerformer np : nps)
      {
        if (np.hasRelation(this))
          return np;
      }
      return null;
    }

    public final ArrayList<BizFlow.NodePerformer> getManualAssignNodePerformers()
    {
      ArrayList rets = new ArrayList();

      List<BizFlow.NodePerformer> nps = BizFlow.this.getAllPerformerNodes();
      for (BizFlow.NodePerformer np : nps)
      {
        if (np.hasManualAssign(this))
          rets.add(np);
      }
      return rets;
    }

    public final RightRule getNodePerformerRightRule()
    {
      BizFlow.NodePerformer np = getRelatedNodePerformer();
      if (np == null) {
        return null;
      }
      BizParticipant bp = np.getParticipant();
      if (bp == null) {
        return null;
      }
      return bp.getContAsRightRule();
    }

    public int getFlowOrderNum()
    {
      if (this.flowOrderNum >= 0) {
        return this.flowOrderNum;
      }
      if ((this instanceof BizFlow.NodeStart))
      {
        this.flowOrderNum = 0;
        return this.flowOrderNum;
      }

      ArrayList<BizFlow.NodePath> nps = BizFlow.this.getAllNodePath(BizFlow.this.getStartNode(), this);
      if ((nps == null) || (nps.size() <= 0))
      {
        this.flowOrderNum = 0;
        return this.flowOrderNum;
      }

      int l = -1;
      for (BizFlow.NodePath np : nps)
      {
        int nn = np.getNodeNum() - 1;
        if ((nn < l) || (l < 0))
          l = nn;
      }
      this.flowOrderNum = l;
      return this.flowOrderNum;
    }

    public final Point getInPoint()
    {
      Rectangle r = getDrawingRect();
      return new Point(r.x, r.y + r.height / 2);
    }

    public final Point getOutPoint()
    {
      Rectangle r = getDrawingRect();
      return new Point(r.x + r.width, r.y + r.height / 2);
    }

    public abstract XmlDataStruct getNodeInOutDataStruct()
      throws Exception;

    public abstract boolean isManualNode();

    public BizFlow.AndXor getJoin()
    {
      return this.join;
    }

    public boolean checkCanSetSplitAnd()
    {
      return getMapJoinAndNode() != null;
    }

    public ActNode getMapJoinAndNode()
    {
      for (BizFlow.Node tmpn : BizFlow.this.id2node.values())
      {
        if (!tmpn.equals(this))
        {
          if ((tmpn instanceof ActNode))
          {
            if (BizFlow.this.checkCanSetSplitJoinAnd(this, (ActNode)tmpn))
              return (ActNode)tmpn; 
          }
        }
      }
      return null;
    }

    public boolean checkCanSetJoinAnd()
    {
      return getMapSplitAndNode() != null;
    }

    public ActNode getMapSplitAndNode()
    {
      for (BizFlow.Node tmpn : BizFlow.this.id2node.values())
      {
        if (!tmpn.equals(this))
        {
          if ((tmpn instanceof ActNode))
          {
            if (BizFlow.this.checkCanSetSplitJoinAnd((ActNode)tmpn, this))
              return (ActNode)tmpn; 
          }
        }
      }
      return null;
    }

    public BizFlow.AndXor getSplit()
    {
      return this.split;
    }

    public boolean setSplit(BizFlow.AndXor ax)
    {
      if (ax == BizFlow.AndXor.AND)
      {
        ActNode mn = getMapJoinAndNode();
        if (mn == null) {
          return false;
        }

        mn.join = BizFlow.AndXor.AND;
        this.split = BizFlow.AndXor.AND;
        return true;
      }

      this.split = ax;
      ActNode mn = getMapJoinAndNode();
      if (mn != null) {
        mn.join = ax;
      }
      return true;
    }

    public ArrayList<BizFlow.Transition> getInTransitions()
    {
      ArrayList rets = new ArrayList();

      for (BizFlow.Transition t : BizFlow.this.id2transition.values())
      {
        if (t.getToNode() == this) {
          rets.add(t);
        }
      }
      return rets;
    }

    public ArrayList<BizFlow.Transition> getOutTransitions()
    {
      ArrayList rets = new ArrayList();

      for (BizFlow.Transition t : BizFlow.this.id2transition.values())
      {
        if (t.getFromNode() == this) {
          rets.add(t);
        }
      }
      return rets;
    }

    public void cleanNode()
    {
      if (this.join == BizFlow.AndXor.AND)
      {
        ActNode spn = getMapSplitAndNode();
        if ((spn == null) || (spn.split != BizFlow.AndXor.AND)) {
          this.join = BizFlow.AndXor.NULL;
        }
      }
      if (this.split == BizFlow.AndXor.AND)
      {
        ActNode jan = getMapJoinAndNode();
        if ((jan == null) || (jan.join != BizFlow.AndXor.AND))
          this.split = BizFlow.AndXor.NULL;
      }
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();

      xd.setParamValue("join", this.join.toString());
      xd.setParamValue("split", this.split.toString());
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);

      String strj = xd.getParamValueStr("join");
      if ((strj != null) && (!strj.equals(""))) {
        this.join = BizFlow.AndXor.valueOf(strj);
      }
      String strs = xd.getParamValueStr("split");
      if ((strs != null) && (!strs.equals("")))
        this.split = BizFlow.AndXor.valueOf(strs);
    }
  }

  public class NodePerformer extends BizFlow.Node
    implements Comparable<NodePerformer>
  {
    String name = null;

    BizParticipant participant = null;

    ArrayList<String> relatedNodeIds = new ArrayList();

    ArrayList<String> manualAssignNodeIds = new ArrayList();

    NodePerformer() { super(); }


    NodePerformer(String nid, String name, int x, int y)
    {
      super(nid, x, y);

      this.name = name;
    }

    public String getNodeName()
    {
      return this.name;
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.performer;
    }

    public BizParticipant.Type getParticipantType()
    {
      return BizParticipant.Type.none;
    }

    public BizParticipant getParticipant()
    {
      return this.participant;
    }

    public void setParticipant(BizParticipant bp)
    {
      if (bp == null) {
        bp = new BizParticipant(getParticipantType());
      }
      this.participant = bp;
    }

    public boolean hasRelation(BizFlow.Node n)
    {
      return this.relatedNodeIds.contains(n.getNodeId());
    }

    public List<BizFlow.Node> getCanRelatedNodes()
    {
      ArrayList rets = new ArrayList();
      BizFlow.ActNode[] ans = BizFlow.this.getAllActNodes();
      for (BizFlow.ActNode an : ans)
      {
        if (checkCanRelatedNode(an))
          rets.add(an);
      }
      return rets;
    }

    public List<BizFlow.Node> getRelatedNodes()
    {
      ArrayList rets = new ArrayList();
      for (String tmpid : this.relatedNodeIds)
      {
        BizFlow.Node n = BizFlow.this.getNode(tmpid);
        if (n != null)
          rets.add(n);
      }
      return rets;
    }

    public boolean isFlowInsStarter()
    {
      for (BizFlow.Node n : getRelatedNodes())
      {
        if ((n instanceof BizFlow.NodeStart)) {
          return true;
        }
      }
      return false;
    }

    public int getFlowOrderNum()
    {
      List<BizFlow.Node> ns = getRelatedNodes();
      if ((ns == null) || (ns.size() <= 0)) {
        return 0;
      }
      int l = -1;
      for (BizFlow.Node n : ns)
      {
        if ((n instanceof BizFlow.ActNode))
        {
          BizFlow.ActNode an = (BizFlow.ActNode)n;
          int nn = an.getFlowOrderNum();
          if ((l < 0) || (l < nn))
          {
            l = nn;
          }
        }
      }
      return l;
    }

    public boolean checkCanRelatedNode(BizFlow.Node n)
    {
      if ((n instanceof BizFlow.NodeStart))
      {
        BizFlow.NodeStart ns = (BizFlow.NodeStart)n;
        if (ns.isInnerView()) {
          return true;
        }
        BizNode bn = ns.getBizNode();
        if (bn == null) {
          return false;
        }
        try
        {
          BizNodeObj bno = bn.getBizObj();
          if (bno == null) {
            return false;
          }
          if ((bno instanceof BizView)) 
          return false;
        }
        catch (Exception e)
        {
          return false;
        }

      }
      else if (!(n instanceof BizFlow.NodeView)) {
        return false;
      }

      List<NodePerformer> nps = BizFlow.this.getAllPerformerNodes();
      for (NodePerformer np : nps)
      {
        if (np.hasRelation(n)) {
          return false;
        }
      }
      return true;
    }

    public void setRelatedNode(BizFlow.Node an)
    {
      if (!checkCanRelatedNode(an)) {
        throw new RuntimeException("cannot set relation to node:" + 
          an.getTitle() + "[" + an.getNodeName() + "]");
      }
      this.relatedNodeIds.add(an.getNodeId());
    }

    public void unsetRelatedNode(BizFlow.Node an)
    {
      this.relatedNodeIds.remove(an.getNodeId());
    }

    public boolean hasManualAssign(BizFlow.Node n)
    {
      return this.manualAssignNodeIds.contains(n.getNodeId());
    }

    public List<BizFlow.Node> getCanManualAssignNodes()
    {
      ArrayList rets = new ArrayList();
      BizFlow.ActNode[] ans = BizFlow.this.getAllActNodes();
      for (BizFlow.ActNode an : ans)
      {
        if (checkCanManualAssignNode(an))
          rets.add(an);
      }
      return rets;
    }

    public List<BizFlow.Node> getManualAssignNodes()
    {
      ArrayList rets = new ArrayList();
      for (String tmpid : this.manualAssignNodeIds)
      {
        BizFlow.Node n = BizFlow.this.getNode(tmpid);
        if (n != null)
          rets.add(n);
      }
      return rets;
    }

    public boolean checkCanManualAssignNode(BizFlow.Node n)
    {
      if (!(n instanceof BizFlow.ActNode)) {
        return false;
      }
      ArrayList<BizFlow.ActNode> n_nexts = BizFlow.this.getNextNodeList((BizFlow.ActNode)n);
      if ((n_nexts == null) || (n_nexts.size() <= 0)) {
        return false;
      }
      List<BizFlow.Node> rns = getRelatedNodes();

      if ((rns == null) || (rns.size() <= 0)) {
        return false;
      }

      boolean b_is_next = false;
      for (BizFlow.Node tmpn : rns)
      {
        if (tmpn.equals(n)) {
          return false;
        }

        for (BizFlow.ActNode nnn : n_nexts)
        {
          if (nnn.equals(tmpn))
          {
            b_is_next = true;
            break;
          }
        }

        if (b_is_next)
        {
          break;
        }
        if (((n instanceof BizFlow.NodeView)) && ((tmpn instanceof BizFlow.NodeView)))
        {
          BizFlow.NodeView tmprnv = (BizFlow.NodeView)tmpn;
          BizFlow.NodeView thisnv = (BizFlow.NodeView)n;

          if (tmprnv.checkIsOnlyPrevNodeView(thisnv))
          {
            b_is_next = true;
            break;
          }
        }
      }

      if (!b_is_next) {
        return false;
      }

      if ((n instanceof BizFlow.NodeStart))
      {
        BizFlow.NodeStart ns = (BizFlow.NodeStart)n;
        if (ns.isInnerView())
          return true;
        BizNode bn = ns.getBizNode();
        if (bn == null) {
          return false;
        }
        try
        {
          BizNodeObj bno = bn.getBizObj();
          if (bno == null) {
            return false;
          }
          if ((bno instanceof BizView))
          return false;
        }
        catch (Exception e)
        {
          return false;
        }
      }
      else if (!(n instanceof BizFlow.NodeView)) {
        return false;
      }

      label287: if (hasManualAssign(n)) {
        return false;
      }
      return true;
    }

    public void setManualAssignNode(BizFlow.Node an)
    {
      if (!checkCanManualAssignNode(an)) {
        throw new RuntimeException("cannot set manual assign to node:" + 
          an.getTitle() + "[" + an.getNodeName() + "]");
      }
      this.manualAssignNodeIds.add(an.getNodeId());
    }

    public void unsetManualAssignNode(BizFlow.Node an)
    {
      this.manualAssignNodeIds.remove(an.getNodeId());
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      if (this.name != null) {
        xd.setParamValue("performer_name", this.name);
      }
      if (this.participant != null) {
        xd.setSubDataSingle("participant", this.participant.toXmlData());
      }

      if ((this.relatedNodeIds != null) && (this.relatedNodeIds.size() > 0)) {
        xd.setParamValues("related_nids", this.relatedNodeIds);
      }
      if ((this.manualAssignNodeIds != null) && (this.manualAssignNodeIds.size() > 0)) {
        xd.setParamValues("manual_assign_nids", this.manualAssignNodeIds);
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      this.name = xd.getParamValueStr("performer_name");

      if (this.participant == null)
        this.participant = new BizParticipant(getParticipantType());
      XmlData tmpxd = xd.getSubDataSingle("participant");
      if (tmpxd != null) {
        this.participant.fromXmlData(tmpxd);
      }

      String[] tmps = xd.getParamValuesStr("related_nids");
      if (tmps != null)
      {
        for (String s : tmps) {
          this.relatedNodeIds.add(s);
        }
      }
      tmps = xd.getParamValuesStr("manual_assign_nids");
      if (tmps != null)
      {
        for (String s : tmps)
          this.manualAssignNodeIds.add(s);
      }
    }

    public int compareTo(NodePerformer o)
    {
      return getFlowOrderNum() - o.getFlowOrderNum();
    }
  }

  public static abstract interface INodeSingleParticipant
  {
    public abstract BizParticipant getParticipant();

    public abstract void setParticipant(BizParticipant paramBizParticipant);
  }

  public class NodeView extends BizFlow.ActNode
    implements BizFlow.IViewCtrlable
  {
    String viewPath = null;

    String viewContTxt = null;

    boolean bInnerView = false;

    long limitDurMs = -1L;

    BizView.Controller viewCtrl = new BizView.Controller();

    public XmlDataStruct getNodeInOutDataStruct()
      throws Exception
    {
      BizView bv = getBizView();
      if (bv == null) {
        return null;
      }
      return bv.getFormInputDataStruct(this.viewCtrl);
    }

    NodeView()
    {
      super();
    }

    public boolean isManualNode()
    {
      return true;
    }

    NodeView(BizView bv, String id, int x, int y)
    {
      super(id, x, y);

      if (bv.isInnerView())
      {
        this.bInnerView = true;
        this.viewPath = "";
      }
      else
      {
        this.viewPath = bv.getBizPath().toString();
      }
    }

    NodeView(String viewpath, String id, int x, int y)
    {
      super(id, x, y);

      this.viewPath = viewpath;
    }

    public BizView getBizView() throws Exception
    {
      if (this.bInnerView)
      {
        return new BizView(getNodeId(), BizFlow.this, this.viewContTxt);
      }

      BizNode bn = BizFlow.this.getBizContainer().getBizNodeByPath(
        this.viewPath);
      if (bn == null) {
        return null;
      }
      try
      {
        return (BizView)bn.getBizObj();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }return null;
    }

    public boolean isInnerView()
    {
      return this.bInnerView;
    }

    public void setInnerView(boolean binner)
    {
      this.bInnerView = binner;
    }

    public String getViewInnerTxt()
    {
      return this.viewContTxt;
    }

    public void setViewInnerTxt(String cont)
    {
      this.viewContTxt = cont;
    }

    public long getLimitDurMS()
    {
      return this.limitDurMs;
    }

    public void setLimitDurMS(long dms)
    {
      this.limitDurMs = dms;
    }

    public String getViewPath()
    {
      return this.viewPath;
    }

    public String getNodeName()
    {
      return this.viewPath;
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.biz_view;
    }

    public List<String> getViewCellNames()
      throws Exception
    {
      BizView bv = getBizView();
      return bv.getViewCellNames();
    }

    public BizView.Controller getViewCtrl()
    {
      return this.viewCtrl;
    }

    public void setViewCtrl(BizView.Controller fc)
    {
      this.viewCtrl = fc;
    }

    public boolean checkIsOnlyPrevNodeView(NodeView nv)
    {
      ArrayList ints = getInTransitions();
      if (ints == null) {
        return false;
      }
      ArrayList nps = BizFlow.this.getAllNodePath(nv, this);
      if ((nps == null) || (nps.size() == 0) || (nps.size() > 1)) {
        return false;
      }
      BizFlow.NodePath np = (BizFlow.NodePath)nps.get(0);
      ArrayList<BizFlow.ActNode> midns = np.getMidNodes();
      if (midns == null) {
        return true;
      }
      int i = midns.size();
      if (i == 0) {
        return true;
      }

      for (BizFlow.ActNode man : midns)
      {
        if (man.isManualNode()) {
          return false;
        }

        ArrayList ins = man.getInTransitions();
        if ((ins == null) || (ins.size() == 0) || (ins.size() > 1)) {
          return false;
        }
        ArrayList outs = man.getOutTransitions();
        if ((outs == null) || (outs.size() == 0) || (outs.size() > 1)) {
          return false;
        }
      }
      return true;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      xd.setParamValue("view_path", this.viewPath);
      xd.setParamValue("is_inner_view", Boolean.valueOf(this.bInnerView));
      xd.setParamValue("inner_view_txt", this.viewContTxt);
      xd.setParamValue("limit_dur_ms", Long.valueOf(this.limitDurMs));

      if (this.viewCtrl != null)
      {
        xd.setSubDataSingle("view_ctrl", this.viewCtrl.toXmlData());
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      this.viewPath = xd.getParamValueStr("view_path");
      this.bInnerView = xd.getParamValueBool("is_inner_view", false).booleanValue();
      this.viewContTxt = xd.getParamValueStr("inner_view_txt");
      this.limitDurMs = xd.getParamValueInt64("limit_dur_ms", -1L);
      XmlData tmpxd = xd.getSubDataSingle("view_ctrl");
      if (tmpxd != null)
      {
        this.viewCtrl = new BizView.Controller();
        this.viewCtrl.fromXmlData(tmpxd);
      }
    }
  }

  public class FlowCtrlViewNode extends BizFlow.ActNode
  {
    String viewPath = null;

    BizView.Controller formCtrl = new BizView.Controller();

    FlowCtrlViewNode() { super(); }


    FlowCtrlViewNode(BizView bv, String id, int x, int y)
    {
      super(id, x, y);

      this.viewPath = bv.getBizPath().toString();
    }

    FlowCtrlViewNode(String viewpath, String id, int x, int y)
    {
      super(id, x, y);

      this.viewPath = viewpath;
    }

    public boolean isManualNode()
    {
      return true;
    }

    public BizView getBizView() throws Exception
    {
      BizNode bn = BizFlow.this.getBizContainer().getBizNodeByPath(
        this.viewPath);
      if (bn == null) {
        return null;
      }
      try
      {
        return (BizView)bn.getBizObj();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }return null;
    }

    public String getViewPath()
    {
      return this.viewPath;
    }

    public String getNodeName()
    {
      return this.viewPath;
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.flow_ctrl_view;
    }

    public XmlDataStruct getNodeInOutDataStruct()
      throws Exception
    {
      BizView bv = getBizView();
      if (bv == null) {
        return null;
      }
      return bv.getFormInputDataStruct(this.formCtrl);
    }

    public BizView.Controller getFormViewCtrl()
    {
      return this.formCtrl;
    }

    public void setFromViewCtrl(BizView.Controller fc)
    {
      this.formCtrl = fc;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      xd.setParamValue("view_path", this.viewPath);

      if (this.formCtrl != null)
      {
        xd.setSubDataSingle("form_ctrl", this.formCtrl.toXmlData());
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      this.viewPath = xd.getParamValueStr("view_path");
      XmlData tmpxd = xd.getSubDataSingle("form_ctrl");
      if (tmpxd != null)
      {
        this.formCtrl = new BizView.Controller();
        this.formCtrl.fromXmlData(tmpxd);
      }
    }
  }

  public static enum EventType
  {
    flow_ins_start(1, "Start", "流程启动"), 
    flow_ins_end(2, "End", "流程结束"), 
    flow_ins_abort(3, "Abort", "流程取消"), 
    flow_ins_suspend(4, "Suspend", "流程挂起"), 
    flow_ins_resume(5, "Resume", "流程恢复"), 
    flow_ins_forword(6, "Forword", "流程进展");

    final int val;
    final String titleEn;
    final String titleCn;

    private EventType(int i, String ten, String tcn) { this.val = i;
      this.titleEn = ten;
      this.titleCn = tcn; }

    public int getIntValue()
    {
      return this.val;
    }

    public String getTitleCn()
    {
      return this.titleCn;
    }

    public String getTitleEn()
    {
      return this.titleEn;
    }

    public static EventType valueOfInt(int i)
    {
      switch (i)
      {
      case 1:
        return flow_ins_start;
      case 2:
        return flow_ins_end;
      case 3:
        return flow_ins_abort;
      case 4:
        return flow_ins_suspend;
      case 5:
        return flow_ins_resume;
      case 6:
        return flow_ins_forword;
      }
      return null;
    }
  }

  public class NodeAction extends BizFlow.ActNode
  {
    BizFlow.EventType onEvent = null;
    String actionPath = null;

    NodeAction() { super(); }


    NodeAction(BizAction ba, String id, int x, int y)
    {
      super(id, x, y);

      this.actionPath = ba.getBizPath().toString();
    }

    NodeAction(String actpath, String id, int x, int y)
    {
      super(id, x, y);

      this.actionPath = actpath;
    }

    public BizFlow.EventType getOnEvent()
    {
      return this.onEvent;
    }

    public void setOnEvent(BizFlow.EventType et)
    {
      this.onEvent = et;
    }

    public boolean isManualNode()
    {
      return false;
    }

    public XmlDataStruct getNodeInOutDataStruct()
      throws Exception
    {
      BizAction ba = getBizAction();
      if (ba == null)
        return null;
      BizOutput[] bos = ba.getOutputs();
      if ((bos == null) || (bos.length <= 0))
        return null;
      return bos[0].getOutputDataStruct();
    }

    public XmlDataStruct getInDataStruct()
      throws Exception
    {
      BizAction ba = getBizAction();
      if (ba == null)
        return null;
      return ba.getInputDataStruct();
    }

    public XmlDataStruct getOutDataStruct() throws Exception
    {
      BizAction ba = getBizAction();
      if (ba == null)
        return null;
      return ba.getOutputDataStruct();
    }

    public BizAction getBizAction() throws Exception
    {
      BizNode bn = BizFlow.this.getBizContainer().getBizNodeByPath(
        this.actionPath);
      if (bn == null) {
        return null;
      }
      try
      {
        return (BizAction)bn.getBizObj();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }return null;
    }

    public String getActionPath()
    {
      return this.actionPath;
    }

    public String getNodeName()
    {
      return this.actionPath;
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.biz_action;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      xd.setParamValue("action_path", this.actionPath);
      if (this.onEvent != null)
        xd.setParamValue("on_event", Integer.valueOf(this.onEvent.getIntValue()));
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      this.actionPath = xd.getParamValueStr("action_path");
      this.onEvent = BizFlow.EventType.valueOfInt(xd.getParamValueInt32("on_event", -1));
    }
  }

  public class NodeRouter extends BizFlow.ActNode
  {
    String name = null;

    NodeRouter() { super(); }


    NodeRouter(String nid, String name, int x, int y)
    {
      super(nid, x, y);

      this.name = name;
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.router;
    }

    public String getNodeName()
    {
      return this.name;
    }

    public boolean isManualNode()
    {
      return false;
    }

    public XmlDataStruct getNodeInOutDataStruct()
      throws Exception
    {
      return null;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      xd.setParamValue("router_name", this.name);
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      this.name = xd.getParamValueStr("router_name");
    }
  }

  public class NodeStart extends BizFlow.ActNode
    implements BizFlow.IViewCtrlable
  {
    String bizNodePath = null;

    BizView.Controller viewCtrl = null;

    boolean bInnerView = false;

    String innerViewTxt = null;

    XmlDataStruct flowInputXDS = new XmlDataStruct();

    NodeStart() { super(); }


    NodeStart(String nid, int x, int y)
    {
      super(nid, x, y);
    }

    public XmlDataStruct getNodeInOutDataStruct()
      throws Exception
    {
      BizNode bn = getBizNode();
      if (bn != null)
      {
        Object bizobj = bn.getBizObj();
        if ((bizobj instanceof BizView))
        {
          BizView bv = (BizView)bizobj;
          BizOutput[] bos = bv.getOutputs();
          if ((bos == null) || (bos.length <= 0))
            return null;
          return bos[0].getOutputDataStruct();
        }
        if ((bizobj instanceof BizAction))
        {
          BizAction bv = (BizAction)bizobj;
          BizOutput[] bos = bv.getOutputs();
          if ((bos == null) || (bos.length <= 0))
            return null;
          return bos[0].getOutputDataStruct();
        }

        return null;
      }

      return getFlowInputXDS();
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.start;
    }

    public void setFlowInputXDS(XmlDataStruct fip)
    {
      if (getBizNode() != null) {
        throw new RuntimeException(
          "biz node start cannot set input xds");
      }
      this.flowInputXDS = fip;
      if (this.flowInputXDS == null)
        this.flowInputXDS = new XmlDataStruct();
    }

    public XmlDataStruct getFlowInputXDS()
    {
      if (getBizNode() != null) {
        return null;
      }
      return this.flowInputXDS;
    }

    public BizNode getBizNode()
    {
      if (this.bizNodePath == null)
        return null;
      if (this.bizNodePath.equals("")) {
        return null;
      }
      try
      {
        return BizFlow.this.getBizContainer().getBizNodeByPath(
          this.bizNodePath);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }return null;
    }

    public boolean isManualNode()
    {
      BizNode bn = getBizNode();
      try
      {
        BizNodeObj bno = bn.getBizObj();
        return bno instanceof BizView;
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }

    public BizView getBizView()
      throws Exception
    {
      if (this.bInnerView)
      {
        return new BizView(this.id, BizFlow.this, this.innerViewTxt);
      }

      BizNode bn = getBizNode();
      if (bn == null) {
        return null;
      }
      BizNodeObj bno = bn.getBizObj();
      if (bno == null) {
        return null;
      }
      if ((bno instanceof BizView)) {
        return (BizView)bno;
      }
      return null;
    }

    public boolean isInnerView()
    {
      return this.bInnerView;
    }

    public void setInnerView(boolean biv)
    {
      this.bInnerView = biv;
    }

    public String getInnerViewTxt()
    {
      return this.innerViewTxt;
    }

    public void setInnerViewTxt(String vt)
    {
      this.innerViewTxt = vt;
    }

    public void setBizNodePath(String bizpath)
    {
      this.bizNodePath = bizpath;
      this.bInnerView = false;
    }

    public void setBizNode(BizNode bn)
      throws Exception
    {
      if (bn == null) {
        return;
      }
      Object bizobj = bn.getBizObj();

      if (!(bizobj instanceof BizView))
      {
        if (!(bizobj instanceof BizAction))
        {
          throw new Exception("cannot set biz node as start :" + 
            bn.getBizPath().toString());
        }
      }
      this.bizNodePath = bn.getBizPath().toString();

      this.bInnerView = false;
    }

    public String getNodeName()
    {
      if (this.bInnerView)
      {
        return "Start:Inner View";
      }

      if (this.bizNodePath == null) {
        return "Start";
      }
      return "Start:" + this.bizNodePath;
    }

    public List<String> getViewCellNames()
      throws Exception
    {
      BizView bv = getBizView();
      if (bv == null)
        return null;
      return bv.getViewCellNames();
    }

    public BizView.Controller getViewCtrl()
    {
      return this.viewCtrl;
    }

    public void setViewCtrl(BizView.Controller fc)
    {
      this.viewCtrl = fc;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();

      if (this.flowInputXDS != null) {
        xd.setSubDataSingle("flow_input_xds", this.flowInputXDS.toXmlData());
      }
      if (this.bizNodePath != null) {
        xd.setParamValue("biz_nodepath", this.bizNodePath);
      }
      if (this.viewCtrl != null) {
        xd.setSubDataSingle("view_ctrl", this.viewCtrl.toXmlData());
      }
      xd.setParamValue("is_inner_view", Boolean.valueOf(this.bInnerView));
      if (this.innerViewTxt != null) {
        xd.setParamValue("inner_view_txt", this.innerViewTxt);
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);

      XmlData tmpxd = xd.getSubDataSingle("flow_input_xds");
      if (tmpxd != null) {
        this.flowInputXDS.fromXmlData(tmpxd);
      }
      this.bizNodePath = xd.getParamValueStr("biz_nodepath");

      tmpxd = xd.getSubDataSingle("view_ctrl");
      if (tmpxd != null)
      {
        this.viewCtrl = new BizView.Controller();
        this.viewCtrl.fromXmlData(tmpxd);
      }

      this.bInnerView = xd.getParamValueBool("is_inner_view", false).booleanValue();
      this.innerViewTxt = xd.getParamValueStr("inner_view_txt");
    }
  }

  public class NodeEnd extends BizFlow.ActNode
  {
    XmlDataStruct flowOutputXDS = new XmlDataStruct();

    NodeEnd() { super(); }


    NodeEnd(String nid, int x, int y)
    {
      super(nid, x, y);
    }

    public boolean isManualNode()
    {
      return false;
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.end;
    }

    public XmlDataStruct getNodeInOutDataStruct() throws Exception
    {
      return this.flowOutputXDS;
    }

    public String getNodeName()
    {
      return "End";
    }

    public void setFlowOutputXDS(XmlDataStruct fip)
    {
      this.flowOutputXDS = fip;
      if (this.flowOutputXDS == null)
        this.flowOutputXDS = new XmlDataStruct();
    }

    public XmlDataStruct getFlowOutputXDS()
    {
      return this.flowOutputXDS;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();

      if (this.flowOutputXDS != null) {
        xd.setSubDataSingle("flow_output_xds", 
          this.flowOutputXDS.toXmlData());
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);

      XmlData tmpxd = xd.getSubDataSingle("flow_output_xds");
      if (tmpxd != null)
        this.flowOutputXDS.fromXmlData(tmpxd);
    }
  }

  public class NodeSubFlow extends BizFlow.ActNode
  {
    String flowPath = null;

    NodeSubFlow() { super(); }


    NodeSubFlow(BizFlow bf, String id, int x, int y)
    {
      super(id, x, y);

      this.flowPath = bf.getBizPath().toString();
    }

    NodeSubFlow(String bfpath, String id, int x, int y)
    {
      super(id, x, y);

      this.flowPath = bfpath;
    }

    public XmlDataStruct getNodeInOutDataStruct()
      throws Exception
    {
      BizFlow bf = getBizFlow();
      if (bf == null) {
        return null;
      }
      BizFlow.NodeEnd ne = bf.getEndNode();
      if (ne == null) {
        return null;
      }
      return ne.getFlowOutputXDS();
    }

    public BizFlow getBizFlow() throws Exception
    {
      BizNode bn = BizFlow.this.getBizContainer().getBizNodeByPath(
        this.flowPath);
      if (bn == null) {
        return null;
      }
      try
      {
        return (BizFlow)bn.getBizObj();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }return null;
    }

    public String getFlowPath()
    {
      return this.flowPath;
    }

    public String getNodeName()
    {
      return this.flowPath;
    }

    public boolean isManualNode()
    {
      return false;
    }

    public BizFlow.NodeType getNodeType()
    {
      return BizFlow.NodeType.biz_flow;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      xd.setParamValue("flow_path", this.flowPath);
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      this.flowPath = xd.getParamValueStr("flow_path");
    }
  }

  public static class IdPoint
    implements IXmlDataable
  {
    String id = null;

    Point pos = new Point(-1, -1);

    public IdPoint()
    {
    }

    public IdPoint(int x, int y)
    {
      this.id = UUID.randomUUID().toString();
      this.pos.x = x;
      this.pos.y = y;
    }

    public IdPoint(Point p)
    {
      this(p.x, p.y);
    }

    public IdPoint(String id, int x, int y)
    {
      this.id = id;
      this.pos.x = x;
      this.pos.y = y;
    }

    public String getId()
    {
      return this.id;
    }

    public int getX()
    {
      return this.pos.x;
    }

    public void setX(int x)
    {
      this.pos.x = x;
    }

    public int getY()
    {
      return this.pos.y;
    }

    public void setY(int y)
    {
      this.pos.y = y;
    }

    public void setLocation(int x, int y)
    {
      this.pos.x = x;
      this.pos.y = y;
    }

    public Point getLocation()
    {
      return new Point(this.pos);
    }

    public void move(int cx, int cy)
    {
      this.pos.x += cx;
      this.pos.y += cy;
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof IdPoint)) {
        return false;
      }
      return this.id.equals(((IdPoint)o).id);
    }

    public int hashCode()
    {
      return this.id.hashCode();
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("id", this.id);
      xd.setParamValue("x", Integer.valueOf(this.pos.x));
      xd.setParamValue("y", Integer.valueOf(this.pos.y));
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      this.id = xd.getParamValueStr("id");
      this.pos.x = xd.getParamValueInt32("x", -1);
      this.pos.y = xd.getParamValueInt32("y", -1);
    }
  }

  public class Transition
    implements IXmlDataable, BizFlow.ITitleable
  {
    String id = null;

    String title = null;

    String promptInfo = null;

    String fromNodeId = null;

    String toNodeId = null;

    BizFlow.IdPoint startP = new BizFlow.IdPoint();

    BizFlow.IdPoint endP = new BizFlow.IdPoint();

    ArrayList<BizFlow.IdPoint> midPs = new ArrayList();

    BizCondition bizCond = new BizCondition();

    Transition()
    {
    }

    public Transition(String id, Point pp)
    {
      this.id = id;
      this.startP = new BizFlow.IdPoint(pp.x - 30, pp.y - 30);
      this.endP = new BizFlow.IdPoint(pp.x + 30, pp.y + 30);
    }

    public String getTitle()
    {
      return this.title;
    }

    public void setTitle(String t)
    {
      this.title = t;
    }

    public String getPromptInfo()
    {
      return this.promptInfo;
    }

    public void setPromptInfo(String pi)
    {
      this.promptInfo = pi;
    }

    public BizFlow.ActNode getFromNode()
    {
      if ((this.fromNodeId == null) || (this.fromNodeId.equals(""))) {
        return null;
      }
      return (BizFlow.ActNode)BizFlow.this.id2node.get(this.fromNodeId);
    }

    public void setFromNode(BizFlow.ActNode n) throws Exception
    {
      if (n == null)
      {
        BizFlow.ActNode fn = getFromNode();
        if (fn != null)
        {
          Point op = fn.getOutPoint();

          this.startP.setLocation(op.x + 20, op.y);
        }
        this.fromNodeId = null;
        return;
      }

      if ((n instanceof BizFlow.NodeEnd))
      {
        throw new Exception("cannot from end node!");
      }

      BizFlow.ActNode ton = getToNode();
      if (ton != null)
      {
        Transition oldt = BizFlow.this.getTransition(n, ton);
        if (oldt != null)
          throw new Exception("the transition from=" + 
            n.getNodeName() + " to=" + ton.getNodeName() + 
            " is existed!");
      }
      this.fromNodeId = n.getNodeId();
    }

    public BizFlow.IdPoint getStartPoint()
    {
      BizFlow.ActNode n = getFromNode();
      if (n != null)
      {
        Point sp = n.getOutPoint();
        this.startP.setLocation(sp.x, sp.y);
      }

      return this.startP;
    }

    public boolean isStartPoint(BizFlow.IdPoint idp)
    {
      return this.startP.equals(idp);
    }

    public List<BizFlow.ActNode> getOutNodesInSinglePath(boolean b_end_manual)
    {
      ArrayList rets = new ArrayList();
      Transition tmpt = this;
      while (tmpt != null)
      {
        BizFlow.ActNode an = tmpt.getToNode();
        if (an == null) {
          break;
        }
        rets.add(an);
        if ((b_end_manual) && (an.isManualNode())) {
          break;
        }
        ArrayList ts = an.getOutTransitions();
        if ((ts == null) || (ts.size() == 0)) {
          break;
        }
        if (ts.size() > 1) {
          break;
        }
        tmpt = (Transition)ts.get(0);
      }

      return rets;
    }

    public BizFlow.ActNode getToNode()
    {
      if ((this.toNodeId == null) || (this.toNodeId.equals(""))) {
        return null;
      }
      return (BizFlow.ActNode)BizFlow.this.id2node.get(this.toNodeId);
    }

    public void setToNode(BizFlow.ActNode n) throws Exception
    {
      if (n == null)
      {
        BizFlow.ActNode fn = getToNode();
        if (fn != null)
        {
          Point op = fn.getOutPoint();

          this.endP.setLocation(op.x - 50, op.y);
        }
        this.toNodeId = null;
        return;
      }

      if ((n instanceof BizFlow.NodeStart)) {
        throw new Exception("cannot to start node!");
      }
      BizFlow.ActNode fromn = getFromNode();
      if (fromn != null)
      {
        Transition oldt = BizFlow.this.getTransition(fromn, n);
        if (oldt != null)
          throw new Exception("the transition from=" + 
            fromn.getNodeName() + " to=" + n.getNodeName() + 
            " is existed!");
      }
      this.toNodeId = n.getNodeId();
    }

    public BizFlow.IdPoint getEndPoint()
    {
      BizFlow.ActNode n = getToNode();
      if (n != null)
      {
        Point ep = n.getInPoint();
        this.endP.setLocation(ep.x, ep.y);
      }

      return this.endP;
    }

    public boolean isEndPoint(BizFlow.IdPoint idp)
    {
      return this.endP.equals(idp);
    }

    public String getFromNodeId()
    {
      return this.fromNodeId;
    }

    public String getToNodeId()
    {
      return this.toNodeId;
    }

    public List<BizFlow.IdPoint> getMidPoints()
    {
      return this.midPs;
    }

    public void setMidPoint(int idx, Point p)
    {
      if ((idx < 0) || (idx > this.midPs.size())) {
        throw new IllegalArgumentException("invalid mid point idx");
      }
      if (this.midPs.contains(p)) {
        return;
      }
      this.midPs.add(idx, new BizFlow.IdPoint(p));
    }

    public int getCanSetMidPointIdx(BizFlow.IdPoint beforep)
    {
      if (beforep.equals(this.startP)) {
        return -1;
      }
      if (beforep.equals(this.endP)) {
        return this.midPs.size();
      }
      return this.midPs.indexOf(beforep);
    }

    public int getMidPointIdx(BizFlow.IdPoint p)
    {
      return this.midPs.indexOf(p);
    }

    public void unsetMidPoint(BizFlow.IdPoint p)
    {
      if (!this.midPs.contains(p)) {
        return;
      }
      this.midPs.remove(p);
    }

    public String getTransitionId()
    {
      return this.id;
    }

    public ArrayList<BizFlow.IdPoint> getAllPoints()
    {
      ArrayList allps = new ArrayList();
      allps.add(this.startP);
      allps.addAll(this.midPs);
      allps.add(this.endP);
      return allps;
    }

    public Point getCenterPoint()
    {
      if ((this.midPs == null) || (this.midPs.size() <= 0)) {
        return new Point((this.startP.getX() + this.endP.getX()) / 2, (
          this.startP.getY() + this.endP.getY()) / 2);
      }
      BizFlow.IdPoint p = (BizFlow.IdPoint)this.midPs.get(0);
      int x = 0;
      int y = 0;

      for (BizFlow.IdPoint idp : this.midPs)
      {
        x += idp.getX();
        y += idp.getY();
      }

      int s = this.midPs.size();
      return new Point(x / s, y / s);
    }

    public Point getPathCenterPoint()
    {
      int s = this.midPs.size();
      if (s % 2 == 1)
      {
        return ((BizFlow.IdPoint)this.midPs.get(s / 2)).getLocation();
      }

      BizFlow.IdPoint sp = this.startP;
      BizFlow.IdPoint ep = this.endP;

      if (s > 0)
      {
        sp = (BizFlow.IdPoint)this.midPs.get(s / 2 - 1);
        ep = (BizFlow.IdPoint)this.midPs.get(s / 2);
      }

      return new Point((sp.getX() + ep.getX()) / 2, 
        (sp.getY() + 
        ep.getY()) / 2);
    }

    public Point getTransitionPos()
    {
      Point retp = new Point(this.startP.getLocation());
      for (BizFlow.IdPoint p : this.midPs)
      {
        retp.x += p.getX();
        retp.y += p.getY();
      }
      retp.x += this.endP.getX();
      retp.y += this.endP.getY();

      int pnum = 2 + this.midPs.size();
      retp.x /= pnum;
      retp.y /= pnum;

      return retp;
    }

    public void setTransitionPos(Point p)
    {
      Point oldp = getTransitionPos();
      int cx = p.x - oldp.x;
      int cy = p.y - oldp.y;

      if ((cx == 0) && (cy == 0)) {
        return;
      }
      this.startP.move(cx, cy);
      this.endP.move(cx, cy);
      for (BizFlow.IdPoint mp : this.midPs)
      {
        mp.move(cx, cy);
      }
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof Transition)) {
        return false;
      }
      return this.id.equals(((Transition)o).id);
    }

    public int hashCode()
    {
      return this.id.hashCode();
    }

    public BizCondition getCondition()
    {
      return this.bizCond;
    }

    public void setCondition(BizCondition bc)
    {
      if (bc == null)
        bc = new BizCondition();
      this.bizCond = bc;
    }

    public String toString()
    {
      return this.id;
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("id", this.id);
      if (this.title != null)
        xd.setParamValue("title", this.title);
      if (this.promptInfo != null) {
        xd.setParamValue("prompt_info", this.promptInfo);
      }
      if (this.fromNodeId != null)
        xd.setParamValue("from_node_id", this.fromNodeId);
      if (this.toNodeId != null) {
        xd.setParamValue("to_node_id", this.toNodeId);
      }
      xd.setSubDataSingle("cond", this.bizCond.toXmlData());

      xd.setSubDataSingle("start_p", this.startP.toXmlData());
      xd.setSubDataSingle("end_p", this.endP.toXmlData());

      List midxds = xd.getOrCreateSubDataArray("mid_ps");
      for (BizFlow.IdPoint mp : this.midPs)
      {
        midxds.add(mp.toXmlData());
      }

      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      this.id = xd.getParamValueStr("id");
      this.title = xd.getParamValueStr("title");
      this.promptInfo = xd.getParamValueStr("prompt_info");
      this.fromNodeId = xd.getParamValueStr("from_node_id");
      this.toNodeId = xd.getParamValueStr("to_node_id");

      XmlData tmpxd0 = xd.getSubDataSingle("cond");

      if (tmpxd0 != null)
      {
        this.bizCond.fromXmlData(tmpxd0);
      }

      XmlData sxd = xd.getSubDataSingle("start_p");
      this.startP = new BizFlow.IdPoint();
      this.startP.fromXmlData(sxd);

      XmlData exd = xd.getSubDataSingle("end_p");
      this.endP.fromXmlData(exd);

      List<XmlData> midxds = xd.getSubDataArray("mid_ps");
      for (XmlData tmpxd : midxds)
      {
        BizFlow.IdPoint tmpidp = new BizFlow.IdPoint();
        tmpidp.fromXmlData(tmpxd);
        this.midPs.add(tmpidp);
      }
    }
  }

  public static class NodeTransitionWrapper
  {
    BizFlow.ActNode node = null;

    BizFlow.Transition trans = null;

    public NodeTransitionWrapper(BizFlow.ActNode n, BizFlow.Transition t)
    {
      this.node = n;
      this.trans = t;
    }

    public BizFlow.ActNode getNode()
    {
      return this.node;
    }

    public BizFlow.Transition getTransition()
    {
      return this.trans;
    }
  }

  public static enum DataFieldStyle
  {
    xmlval, xmldata, datax;
  }

  public static enum DataFieldInOut
  {
    inner, 
    param_in, 
    param_out, 
    param_inout;
  }

  public abstract class DataField
    implements IXmlDataable
  {
    BizFlow.DataFieldInOut dfInOut = BizFlow.DataFieldInOut.inner;

    String name = null;

    DataField() {
    }
    public DataField(String n) { this.name = n; }


    public String getName()
    {
      return this.name;
    }

    public BizFlow.DataFieldInOut getInOutInfo()
    {
      return this.dfInOut;
    }

    public abstract BizFlow.DataFieldStyle getDataFieldStyle();

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("name", this.name);
      xd.setParamValue("style", getDataFieldStyle().toString());
      xd.setParamValue("in_out", this.dfInOut.toString());
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      this.name = xd.getParamValueStr("name");
      String tmps = xd.getParamValueStr("in_out");
      if ((tmps != null) && (!tmps.equals("")))
        this.dfInOut = BizFlow.DataFieldInOut.valueOf(tmps);
    }
  }

  public abstract class DataFieldJU extends BizFlow.DataField implements JoinUnit
  {
    DataFieldJU() {
      super();
    }

    public DataFieldJU(String n)
    {
      super(n);
    }

    public String getJoinUnitName()
    {
      return "df_" + getName();
    }

    public abstract XmlDataStruct getDataFieldStruct();

    public JoinInterface getJoinInterface(String name)
    {
      ArrayList<JoinInterface> jis = getAllJoinInterfaces();
      if (jis == null) {
        return null;
      }
      for (JoinInterface ji : jis)
      {
        if (ji.getJoinInterfaceName().equals(name))
          return ji;
      }
      return null;
    }
  }

  public class DataFieldXmlVal extends BizFlow.DataField
  {
    XmlValDef xmlValDef = new XmlValDef();

    boolean hasIdx = false;

    DataFieldXmlVal() { super(); }


    public DataFieldXmlVal(String name)
    {
      super(name);

      setXmlValDataField("string", -1, false);
    }

    public DataFieldXmlVal(String name, String xmlvt, boolean barray)
    {
      super(name);

      setXmlValDataField(xmlvt, -1, barray);
    }

    public XmlValDef getXmlValDef()
    {
      return this.xmlValDef;
    }

    public boolean isHasIdx()
    {
      return this.hasIdx;
    }

    public void setHasIdx(boolean bhi)
    {
      this.hasIdx = bhi;
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof DataFieldXmlVal)) {
        return false;
      }
      DataFieldXmlVal dfxv = (DataFieldXmlVal)o;
      if (!this.xmlValDef.equals(dfxv.xmlValDef)) {
        return false;
      }
      if (this.hasIdx != dfxv.hasIdx) {
        return false;
      }
      return true;
    }

    public boolean equalsIgnoreIdx(Object o)
    {
      if (!(o instanceof DataFieldXmlVal)) {
        return false;
      }
      DataFieldXmlVal dfxv = (DataFieldXmlVal)o;
      if (!this.xmlValDef.equals(dfxv.xmlValDef)) {
        return false;
      }
      return true;
    }

    public BizFlow.DataFieldStyle getDataFieldStyle()
    {
      return BizFlow.DataFieldStyle.xmlval;
    }

    public XmlDataStruct getDataFieldStruct()
    {
      return null;
    }

    public void setXmlValDataField(String xmlvt, int maxlen, boolean barray)
    {
      if (!XmlVal.isXmlValType(xmlvt)) {
        throw new IllegalArgumentException("not xml val type=" + xmlvt);
      }
      setXmlValDataField(new XmlValDef(xmlvt, barray, true, maxlen));
    }

    public void setXmlValDataField(XmlValDef xvd)
    {
      if (xvd == null)
        throw new IllegalArgumentException("XmlValDef cannot be null!");
      this.xmlValDef = xvd;
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      xd.setSubDataSingle("xmlval_def", this.xmlValDef.toXmlData());
      xd.setParamValue("xmlval_def_idx", Boolean.valueOf(this.hasIdx));

      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      XmlData tmpxd = xd.getSubDataSingle("xmlval_def");
      if (tmpxd != null) {
        this.xmlValDef.fromXmlData(tmpxd);
      }
      this.hasIdx = xd.getParamValueBool("xmlval_def_idx", false).booleanValue();
    }
  }

  public class DataFieldXmlData extends BizFlow.DataFieldJU
  {
    XmlDataStruct dataStruct = new XmlDataStruct();

    DataFieldXmlData() { super(); }


    public DataFieldXmlData(String name)
    {
      super(name);
    }

    public DataFieldXmlData(String name, XmlDataStruct xds)
    {
      super(name);
      setXmlDataStruct(xds);
    }

    public BizFlow.DataFieldStyle getDataFieldStyle()
    {
      return BizFlow.DataFieldStyle.xmldata;
    }

    public XmlDataStruct getDataFieldStruct()
    {
      return this.dataStruct;
    }

    public void setXmlDataStruct(XmlDataStruct xds)
    {
      this.dataStruct = xds;
      if (this.dataStruct == null)
        this.dataStruct = new XmlDataStruct();
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      if (this.dataStruct != null) {
        xd.setSubDataSingle("data_struct", this.dataStruct.toXmlData());
      }
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      XmlData tmpxd = xd.getSubDataSingle("data_struct");
      if (tmpxd != null)
        this.dataStruct.fromXmlData(tmpxd);
    }

    public ArrayList<JoinInterface> getAllJoinInterfaces()
    {
      ArrayList jis = new ArrayList();
      jis.add(new JoinInterface(JoinType.out, "get", this.dataStruct));
      jis.add(new JoinInterface(JoinType.in, "set", this.dataStruct));
      jis.add(new JoinInterface(JoinType.in, "update", this.dataStruct));
      return jis;
    }
  }

  public class DataFieldDataX extends BizFlow.DataFieldJU
  {
    String dataxBase = null;

    String dataxClass = null;

    DataFieldDataX() { super(); }


    public DataFieldDataX(String name)
    {
      super(name);
    }

    public DataFieldDataX(String name, String dataxbase, String dataxclass)
    {
      super(name);

      setDataXInfo(dataxbase, dataxclass);
    }

    public String getDataXBase()
    {
      return this.dataxBase;
    }

    public String getDataXClass()
    {
      return this.dataxClass;
    }

    public void setDataXInfo(String dataxb, String dataxc)
    {
      this.dataxBase = dataxb;
      this.dataxClass = dataxc;
    }

    public BizFlow.DataFieldStyle getDataFieldStyle()
    {
      return BizFlow.DataFieldStyle.datax;
    }

    public XmlDataStruct getDataFieldStruct()
    {
      if ((this.dataxBase == null) || (this.dataxBase.equals(""))) {
        return null;
      }
      if ((this.dataxClass == null) || (this.dataxClass.equals(""))) {
        return null;
      }
      IDataXContainer dxc = BizFlow.this.getBizContainer()
        .getDataXContainer();

      DataXClass dxcc = dxc.getDataXClass(this.dataxBase, this.dataxClass);
      if (dxcc == null)
        return null;
      return dxcc.getDataStruct();
    }

    public XmlData toXmlData()
    {
      XmlData xd = super.toXmlData();
      if (this.dataxBase != null)
        xd.setParamValue("datax_base", this.dataxBase);
      if (this.dataxClass != null)
        xd.setParamValue("datax_class", this.dataxClass);
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      super.fromXmlData(xd);
      this.dataxBase = xd.getParamValueStr("datax_base");
      this.dataxClass = xd.getParamValueStr("datax_class");
    }

    public ArrayList<JoinInterface> getAllJoinInterfaces()
    {
      XmlDataStruct xds = getDataFieldStruct();
      if (xds == null) {
        return null;
      }
      XmlDataStruct dxi_xds = DataXItem.calItemStruct(xds);
      ArrayList jits = new ArrayList();
      jits.add(new JoinInterface(JoinType.out, "get", dxi_xds));
      jits.add(new JoinInterface(JoinType.out, "get_xmldata", xds));
      jits.add(new JoinInterface(JoinType.in, "set_xmldata", xds));
      XmlDataStruct up_xds = xds.copyMeWithAllNullable();
      jits.add(new JoinInterface(JoinType.in, "update_xmldata", up_xds));

      XmlDataStruct set_xd_id_xds = new XmlDataStruct();
      xds.setXmlDataMember("xd_id", "int64");
      jits
        .add(new JoinInterface(JoinType.in, "set_xd_id", 
        set_xd_id_xds));
      return jits;
    }
  }

  public class DataFieldContainer extends JoinBoard
    implements IXmlDataable
  {
    Point pos = new Point(10, 10);

    Hashtable<String, BizFlow.DataField> name2dataField = new Hashtable();

    public DataFieldContainer()
    {
    }

    public DataFieldContainer(int x, int y)
    {
      this.pos.setLocation(x, y);
    }

    public Point getPos()
    {
      return new Point(this.pos);
    }

    public void setPos(int x, int y)
    {
      this.pos.x = x;
      this.pos.y = y;
    }

    public BizFlow.DataField[] getAllDataFields()
    {
      BizFlow.DataField[] rets = new BizFlow.DataField[this.name2dataField.size()];
      this.name2dataField.values().toArray(rets);
      return rets;
    }

    public BizFlow.DataField getDataField(String name)
    {
      return (BizFlow.DataField)this.name2dataField.get(name);
    }

    void setDataField(BizFlow.DataFieldXmlVal df)
    {
      String n = df.getName();
      BizFlow.DataField df0 = getDataField(n);
      if (df.equalsIgnoreIdx(df0)) {
        return;
      }
      this.name2dataField.put(n, df);
    }

    public void setDataField(String name, BizFlow.DataFieldStyle dfs)
    {
      BizFlow.DataField df = getDataField(name);
      if (df != null) {
        throw new RuntimeException("DataField with name=" + name + 
          " is already existed!");
      }
      StringBuffer fr = new StringBuffer();
      if (!XmlDataStruct.checkName(name, fr)) {
        throw new IllegalArgumentException(fr.toString());
      }
      if (dfs == BizFlow.DataFieldStyle.xmlval)
      {
        df = new BizFlow.DataFieldXmlVal(BizFlow.this, name);
      }
      else if (dfs == BizFlow.DataFieldStyle.xmldata)
      {
        df = new BizFlow.DataFieldXmlData(BizFlow.this, name);
      }
      else if (dfs == BizFlow.DataFieldStyle.datax)
      {
        df = new BizFlow.DataFieldDataX(BizFlow.this, name);
      }
      else
      {
        throw new RuntimeException("not support data field style!");
      }

      this.name2dataField.put(name, df);
    }

    public void unsetDataField(String name)
    {
      this.name2dataField.remove(name);
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("pos_x", Integer.valueOf(this.pos.x));
      xd.setParamValue("pos_y", Integer.valueOf(this.pos.y));

      List dfxds = xd.getOrCreateSubDataArray("datafields");
      for (BizFlow.DataField df : this.name2dataField.values())
      {
        dfxds.add(df.toXmlData());
      }

      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      this.pos.x = xd.getParamValueInt32("pos_x", 10);
      this.pos.y = xd.getParamValueInt32("pos_y", 10);

      List<XmlData> dfxds = xd.getSubDataArray("datafields");
      if (dfxds != null)
      {
        for (XmlData tmpxd : dfxds)
        {
          BizFlow.DataField df = BizFlow.this.constructDataFieldFromXmlData(tmpxd);
          this.name2dataField.put(df.getName(), df);
        }
      }
    }

    public ArrayList<JoinUnit> getAllJoinUnits()
    {
      ArrayList jus = new ArrayList();
      for (BizFlow.DataField tmpdf : this.name2dataField.values())
      {
        if ((tmpdf instanceof JoinUnit)) {
          jus.add((JoinUnit)tmpdf);
        }
      }
      return jus;
    }

    public JoinUnit getJoinUnit(String name)
    {
      BizFlow.DataField df = getDataField(name);
      if (df == null) {
        return null;
      }
      if ((df instanceof JoinUnit))
        return (JoinUnit)df;
      return null;
    }
  }
}
