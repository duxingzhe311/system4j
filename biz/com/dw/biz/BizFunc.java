package com.dw.biz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;

@XRmi(reg_name="biz_func")
public class BizFunc extends BizNodeObj
  implements IXmlDataable
{
  String title = null;

  String desc = null;

  boolean bNeedInsRunner = false;

  String startNodeId = null;

  String endNodeId = null;

  HashSet<INode> funcNodes = new HashSet();

  Hashtable<String, NodePath> fromId2Path = new Hashtable();

  String userRightRule = null;

  public NodePath createPathByPathStr(String pathstr)
  {
    int p = pathstr.indexOf('-');
    if (p <= 0) {
      return null;
    }
    String fromstr = pathstr.substring(0, p);
    String tarpath = pathstr.substring(p + 1);
    p = fromstr.indexOf('+');
    if (p <= 0) {
      return null;
    }
    String frompath = fromstr.substring(0, p);
    String fromoutput = fromstr.substring(p + 1);
    if (BizManager.isBizViewPath(frompath))
    {
      if (BizManager.isBizViewPath(tarpath))
      {
        return new ViewToViewPath(frompath, fromoutput, tarpath);
      }
      if (BizManager.isBizActionPath(tarpath))
      {
        return new ViewToActionPath(frompath, fromoutput, tarpath);
      }

      return null;
    }

    if (BizManager.isBizActionPath(frompath))
    {
      if (BizManager.isBizViewPath(tarpath))
      {
        return new ActionToViewPath(frompath, fromoutput, tarpath);
      }
      if (BizManager.isBizActionPath(tarpath))
      {
        return new ActionToActionPath(frompath, fromoutput, tarpath);
      }

      return null;
    }

    return null;
  }

  public NodePath createPathByFromTo(String frompath, String fromoutput, String tarpath)
  {
    if (BizManager.isBizViewPath(frompath))
    {
      if (BizManager.isBizViewPath(tarpath))
      {
        return new ViewToViewPath(frompath, fromoutput, tarpath);
      }
      if (BizManager.isBizActionPath(tarpath))
      {
        return new ViewToActionPath(frompath, fromoutput, tarpath);
      }

      return null;
    }

    if (BizManager.isBizActionPath(frompath))
    {
      if (BizManager.isBizViewPath(tarpath))
      {
        return new ActionToViewPath(frompath, fromoutput, tarpath);
      }
      if (BizManager.isBizActionPath(tarpath))
      {
        return new ActionToActionPath(frompath, fromoutput, tarpath);
      }

      return null;
    }

    return null;
  }

  public NodePath createPathByXmlData(XmlData xd)
  {
    String frompath = xd.getParamValueStr("from_path");
    if ((frompath == null) || (frompath.equals(""))) {
      return null;
    }
    String fromoutput = xd.getParamValueStr("from_output");
    if ((fromoutput == null) || (fromoutput.equals(""))) {
      return null;
    }
    String tarpath = xd.getParamValueStr("tar_path");
    if ((tarpath == null) || (tarpath.equals("")))
      return null;
    NodePath np = createPathByFromTo(frompath, fromoutput, tarpath);
    np.setTitle(xd.getParamValueStr("title"));
    return np;
  }

  public BizFunc()
  {
  }

  public BizFunc(String title, String desc, String startnodeid, String endnodeid, INode[] nodes, NodePath[] paths, String rightrule)
  {
    this.title = title;
    this.desc = desc;

    this.startNodeId = startnodeid;
    this.endNodeId = endnodeid;

    if (nodes != null) {
      for (INode n : nodes)
        this.funcNodes.add(n);
    }
    if (paths != null)
    {
      for (NodePath np : paths) {
        this.fromId2Path.put(np.getFromStr(), np);
      }
    }
    this.userRightRule = rightrule;
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

  public String getRightRule()
  {
    return this.userRightRule;
  }

  public void setRightRule(String rr)
  {
    this.userRightRule = rr;
  }

  public boolean canRunInBizEnv()
  {
    return false;
  }

  public void setStartNode(INode n)
  {
    this.startNodeId = n.getNodeId();
  }

  public String getStartNodeId()
  {
    return this.startNodeId;
  }

  public INode getStartNode()
  {
    if ((this.startNodeId == null) || (this.startNodeId.equals(""))) {
      return null;
    }
    for (INode n : this.funcNodes)
    {
      if (n.getNodeId().equals(this.startNodeId))
      {
        return n;
      }
    }
    return null;
  }

  public XmlDataStruct getInputXmlDataStruct() throws Exception
  {
    INode sn = getStartNode();
    if (sn == null) {
      return null;
    }
    if ((sn instanceof NodeView))
    {
      NodeView nv = (NodeView)sn;
      return nv.getBizView().getInOutXmlDataStruct();
    }
    if ((sn instanceof NodeAction))
    {
      NodeAction na = (NodeAction)sn;
      return na.getBizAction().getInputDataStruct();
    }

    return null;
  }

  public String getEndNodeId()
  {
    return this.endNodeId;
  }

  public void setEndNode(INode n)
  {
    if (hasOutputNode(n))
      throw new IllegalArgumentException(
        "node which has output node cannot be end node");
    this.endNodeId = n.getNodeId();
  }

  public INode getEndNode()
  {
    if ((this.endNodeId == null) || (this.endNodeId.equals(""))) {
      return null;
    }
    for (INode n : this.funcNodes)
    {
      if (n.getNodeId().equals(this.endNodeId))
      {
        if (!hasOutputNode(n)) {
          return n;
        }
        return null;
      }
    }
    return null;
  }

  public INode setNode(int x, int y, BizView bv)
  {
    NodeView tmpnv = new NodeView(bv, x, y);
    this.funcNodes.add(tmpnv);
    return tmpnv;
  }

  public INode setNode(int x, int y, BizAction ba)
  {
    NodeAction tmpnv = new NodeAction(ba, x, y);
    this.funcNodes.add(tmpnv);
    return tmpnv;
  }

  public void removeNode(INode n)
  {
    ArrayList toberm = new ArrayList();
    Iterator localIterator = this.fromId2Path.values().iterator();

    while (localIterator.hasNext())
    {
      NodePath np = (NodePath)localIterator.next();

      if ((np.getFromNode().equals(n)) || (np.getTarNode().equals(n)))
      {
        toberm.add(np);
      }
    }
    NodePath np;
    for (localIterator = toberm.iterator(); localIterator.hasNext(); this.fromId2Path.remove(np.getFromStr())) np = (NodePath)localIterator.next(); if (!this.funcNodes.remove(n));
  }

  public INode[] getAllNodes()
  {
    INode[] rets = new INode[this.funcNodes.size()];
    this.funcNodes.toArray(rets);
    return rets;
  }

  public INode getNode(String func_nodeid)
  {
    for (INode n : this.funcNodes)
    {
      if (n.getNodeId().equals(func_nodeid)) {
        return n;
      }
    }
    return null;
  }

  public NodeView getNode(BizView bv)
  {
    String tmpid = bv.getBizPath().toString();
    for (INode n : this.funcNodes)
    {
      if (n.getNodeId().equals(tmpid))
        return (NodeView)n;
    }
    return null;
  }

  public NodeAction getNode(BizAction ba)
  {
    String tmpid = ba.getBizPath().toString();
    for (INode n : this.funcNodes)
    {
      if (n.getNodeId().equals(tmpid))
        return (NodeAction)n;
    }
    return null;
  }

  public NodeView getNodeViewByViewPath(String bizviewpath)
  {
    for (INode n : this.funcNodes)
    {
      if ((n instanceof NodeView))
      {
        NodeView nv = (NodeView)n;
        if (nv.viewPath.equals(bizviewpath))
          return nv;
      }
    }
    return null;
  }

  public void setFuncNode(BizView bv, int x, int y)
  {
    NodeView nv = new NodeView(bv, x, y);
    this.funcNodes.add(nv);
  }

  public void setFuncNode(BizAction ba, int x, int y)
  {
    NodeAction na = new NodeAction(ba, x, y);
    this.funcNodes.add(na);
  }

  public void unsetFuncNode(BizView bv)
  {
    INode n = new NodeView(bv, -1, -1);
    this.funcNodes.remove(n);
  }

  public void unsetFuncNode(BizAction ba)
  {
    INode n = new NodeAction(ba, -1, -1);
    this.funcNodes.remove(n);
  }

  public void unsetNodePath(NodePath np)
  {
    this.fromId2Path.remove(np.getFromStr());
  }

  public boolean setNodePath(BizOutput oper, INode tarn) throws Exception
  {
    BizNodeObj frombn = oper.getBelongToBizNodeObj();

    XmlDataStruct fromxds = oper.getOutputDataStruct();

    if ((frombn instanceof BizView))
    {
      BizView frombv = (BizView)frombn;
      XmlDataStruct tarxds = null;
      if ((tarn instanceof NodeAction))
      {
        NodeAction tarna = (NodeAction)tarn;
        BizAction tarba = tarna.getBizAction();
        if (tarba == null) {
          return false;
        }
        tarxds = tarba.getInputDataStruct();

        if (!fromxds.checkFitFor(tarxds, null)) {
          return false;
        }
        ViewToActionPath vta = new ViewToActionPath(frombv, 
          oper.getName(), tarba);
        this.fromId2Path.put(vta.getFromStr(), vta);
        return true;
      }
      if ((tarn instanceof NodeView))
      {
        NodeView tarnv = (NodeView)tarn;
        BizView tarbv = tarnv.getBizView();
        if (tarbv == null) {
          return false;
        }
        if (tarnv.getNodeId().equals(frombv.getBizPath().toString())) {
          return false;
        }
        tarxds = tarbv.getInOutXmlDataStruct();

        if (!fromxds.checkFitFor(tarxds, null)) {
          return false;
        }
        ViewToViewPath vta = new ViewToViewPath(frombv, oper.getName(), 
          tarbv);
        this.fromId2Path.put(vta.getFromStr(), vta);
        return true;
      }
    }
    else if ((frombn instanceof BizAction))
    {
      BizAction fromba = (BizAction)frombn;
      XmlDataStruct tarxds = null;
      if ((tarn instanceof NodeAction))
      {
        NodeAction tarna = (NodeAction)tarn;
        BizAction tarba = tarna.getBizAction();
        if (tarba == null) {
          return false;
        }
        if (tarna.getNodeId().equals(fromba.getBizPath().toString())) {
          return false;
        }
        tarxds = tarba.getInputDataStruct();

        if (!fromxds.checkFitFor(tarxds, null)) {
          return false;
        }
        ActionToActionPath vta = new ActionToActionPath(fromba, 
          oper.getName(), tarba);
        this.fromId2Path.put(vta.getFromStr(), vta);
        return true;
      }
      if ((tarn instanceof NodeView))
      {
        NodeView tarnv = (NodeView)tarn;
        BizView tarbv = tarnv.getBizView();
        if (tarbv == null) {
          return false;
        }
        tarxds = tarbv.getInOutXmlDataStruct();

        if (!fromxds.checkFitFor(tarxds, null)) {
          return false;
        }
        ActionToViewPath vta = new ActionToViewPath(fromba, 
          oper.getName(), tarbv);
        this.fromId2Path.put(vta.getFromStr(), vta);
        return true;
      }
    }
    return false;
  }

  public NodePath getNodePathByOper(BizOutput op)
  {
    BizNodeObj bv = op.getBelongToBizNodeObj();
    for (NodePath np : this.fromId2Path.values())
    {
      if ((np instanceof ViewToActionPath))
      {
        ViewToActionPath vtap = (ViewToActionPath)np;
        if ((vtap.getFromNodePath().equals(bv.getBizPath().toString())) && 
          (vtap.getFromOutputName().equals(op.getName())))
          return vtap;
      }
      else if ((np instanceof ViewToViewPath))
      {
        ViewToViewPath vtvp = (ViewToViewPath)np;
        if ((vtvp.getFromNodePath().equals(bv.getBizPath().toString())) && 
          (vtvp.getFromOutputName().equals(op.getName())))
          return vtvp;
      }
      else if ((np instanceof ActionToActionPath))
      {
        ActionToActionPath vtap = (ActionToActionPath)np;
        if ((vtap.getFromNodePath().equals(bv.getBizPath().toString())) && 
          (vtap.getFromOutputName().equals(op.getName())))
          return vtap;
      }
      else if ((np instanceof ActionToViewPath))
      {
        ActionToViewPath vtvp = (ActionToViewPath)np;
        if ((vtvp.getFromNodePath().equals(bv.getBizPath().toString())) && 
          (vtvp.getFromOutputName().equals(op.getName()))) {
          return vtvp;
        }
      }
    }
    return null;
  }

  public boolean unsetNodePathByOper(BizOutput op)
  {
    NodePath np = getNodePathByOper(op);
    if (np == null) {
      return false;
    }
    np = (NodePath)this.fromId2Path.remove(np.getFromStr());
    return np != null;
  }

  public List<INode> getCanPathToNodes(BizOutput op)
  {
    ArrayList ns = new ArrayList();

    for (INode n : this.funcNodes)
    {
      if ((n instanceof NodeView))
      {
        NodeView nv = (NodeView)n;
        try
        {
          BizView bv = nv.getBizView();
          if (nv.getNodeId().equals(
            op.getBelongToBizNodeObj().getBizPath().toString())) {
            continue;
          }
          StringBuffer tmpsb = new StringBuffer();
          if (!op.getOutputDataStruct().checkFitFor(
            bv.getInOutXmlDataStruct(), tmpsb)) continue;
          ns.add(nv);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      else if ((n instanceof NodeAction))
      {
        NodeAction na = (NodeAction)n;
        try
        {
          BizAction ba = na.getBizAction();

          StringBuffer tmpsb = new StringBuffer();
          if (op.getOutputDataStruct().checkFitFor(
            ba.getInputDataStruct(), tmpsb))
            ns.add(na);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }

    return ns;
  }

  public boolean checkCanPath(BizOutput bvo, INode tarn)
  {
    try
    {
      BizNodeObj frombn = bvo.getBelongToBizNodeObj();
      XmlDataStruct outxds = bvo.getOutputDataStruct();

      if ((tarn instanceof NodeView))
      {
        NodeView dnbv = (NodeView)tarn;
        BizView bv = dnbv.getBizView();

        if ((bv != null) && 
          (!frombn.getBizPath().toString().equals(
          dnbv.getNodeId())))
        {
          return outxds.checkFitFor(bv.getInOutXmlDataStruct(), null);
        }
      }
      else if ((tarn instanceof NodeAction))
      {
        NodeAction dnba = (NodeAction)tarn;

        if ((frombn instanceof BizAction))
        {
          NodeAction tmpna = getNode((BizAction)frombn);
          if (hasAutoRoute(dnba, tmpna)) {
            return false;
          }
        }
        BizAction tarba = dnba.getBizAction();

        if (tarba != null)
        {
          return outxds.checkFitFor(tarba.getInputDataStruct(), null);
        }
      }

      return false;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return false;
  }

  public boolean hasPath(INode from, INode to)
  {
    for (NodePath np : this.fromId2Path.values())
    {
      INode fn = np.getFromNode();
      if (fn.equals(from))
      {
        INode tn = np.getTarNode();
        if (tn.equals(to))
        {
          return true;
        }
      }
    }
    return false;
  }

  public boolean hasAutoRoute(NodeAction fromna, NodeAction tona)
  {
    if (fromna.equals(tona)) {
      return false;
    }
    NodeAction fna = fromna;

    HashSet<INode> ons = getOutputNodeList(fromna);
    for (INode tmpn : ons)
    {
      if ((tmpn instanceof NodeAction))
      {
        if (tona.equals(tmpn)) {
          return true;
        }
        return hasAutoRoute((NodeAction)tmpn, tona);
      }

    }

    return false;
  }

  public HashSet<INode> getInputNodeList(INode n)
  {
    HashSet ns = new HashSet();
    for (INode tmpn : this.funcNodes)
    {
      if (!tmpn.equals(n))
      {
        if (hasPath(tmpn, n))
          ns.add(tmpn); 
      }
    }
    return ns;
  }

  public boolean hasInputNode(INode n)
  {
    HashSet hs = getInputNodeList(n);
    return hs.size() > 0;
  }

  public HashSet<INode> getOutputNodeList(INode n)
  {
    HashSet ns = new HashSet();
    for (INode tmpn : this.funcNodes)
    {
      if (!tmpn.equals(n))
      {
        if (hasPath(n, tmpn))
          ns.add(tmpn); 
      }
    }
    return ns;
  }

  public List<NodePath> getOutputNodePath(INode n)
    throws Exception
  {
    if (n == null) {
      return null;
    }
    BizOutput[] bos = (BizOutput[])null;
    if ((n instanceof NodeView))
    {
      NodeView nv = (NodeView)n;
      BizView bv = nv.getBizView();
      if (bv == null) {
        return null;
      }
      bos = bv.getOutputs();
    }
    else if ((n instanceof NodeAction))
    {
      NodeAction nv = (NodeAction)n;
      BizAction bv = nv.getBizAction();
      if (bv == null) {
        return null;
      }
      bos = bv.getOutputs();
    }

    if (bos == null) {
      return null;
    }
    ArrayList nps = new ArrayList();
    for (BizOutput bo : bos)
    {
      NodePath np = getNodePathByFrom(n, bo.getName());
      if (np != null)
      {
        nps.add(np);
      }
    }
    return nps;
  }

  private NodePath getNodePathByFrom(INode n, String oper)
  {
    String fid = n.getNodeId() + "+" + oper;
    return (NodePath)this.fromId2Path.get(fid);
  }

  public INode getOutputNode(INode n, String oper)
  {
    NodePath np = getNodePathByFrom(n, oper);
    if (np == null) {
      return null;
    }
    return np.getTarNode();
  }

  public boolean hasOutputNode(INode n)
  {
    HashSet hs = getOutputNodeList(n);
    return hs.size() > 0;
  }

  public NodePath[] getAllPaths()
  {
    NodePath[] rets = new NodePath[this.fromId2Path.size()];
    this.fromId2Path.values().toArray(rets);
    return rets;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    if (this.title != null)
      xd.setParamValue("title", this.title);
    if (this.desc != null) {
      xd.setParamValue("desc", this.desc);
    }
    if (this.startNodeId != null)
      xd.setParamValue("start_nodeid", this.startNodeId);
    if (this.endNodeId != null) {
      xd.setParamValue("end_nodeid", this.endNodeId);
    }
    if (this.userRightRule != null) {
      xd.setParamValue("right_rule", this.userRightRule);
    }
    List nxds = xd.getOrCreateSubDataArray("nodes");
    XmlData xd0;
    for (INode n : this.funcNodes)
    {
      if ((n instanceof NodeView))
      {
        xd0 = new XmlData();
        NodeView nv = (NodeView)n;
        xd0.setParamValue("t", "nv");
        xd0.setParamValue("node_id", nv.getNodeId());
        xd0.setParamValue("x", Integer.valueOf(nv.getX()));
        xd0.setParamValue("y", Integer.valueOf(nv.getY()));
        if (nv.title != null)
          xd0.setParamValue("title", nv.title);
        nxds.add(xd0);
      }
      else if ((n instanceof NodeAction))
      {
        xd0 = new XmlData();
        NodeAction na = (NodeAction)n;
        xd0.setParamValue("t", "na");
        xd0.setParamValue("node_id", na.getNodeId());
        xd0.setParamValue("x", Integer.valueOf(na.getX()));
        xd0.setParamValue("y", Integer.valueOf(na.getY()));
        if (na.title != null)
          xd0.setParamValue("title", na.title);
        nxds.add(xd0);
      }

    }

    List npxds = xd.getOrCreateSubDataArray("node_paths");
    for (NodePath np : this.fromId2Path.values())
    {
      npxds.add(np.toXmlData());
    }

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.title = xd.getParamValueStr("title");
    this.desc = xd.getParamValueStr("desc");

    this.startNodeId = xd.getParamValueStr("start_nodeid");
    this.endNodeId = xd.getParamValueStr("end_nodeid");

    this.userRightRule = xd.getParamValueStr("right_rule");
    if (this.userRightRule != null) {
      this.userRightRule = this.userRightRule.trim();
    }
    List<XmlData> nxds = xd.getSubDataArray("nodes");
    String tstr;
    if (nxds != null)
    {
      for (XmlData xd0 : nxds)
      {
        tstr = xd0.getParamValueStr("t");
        if ("nv".equals(tstr))
        {
          String vfid = xd0.getParamValueStr("node_id");
          if ((vfid != null) && (!vfid.equals("")))
          {
            int x = xd0.getParamValueInt32("x", -1);
            int y = xd0.getParamValueInt32("y", -1);

            NodeView nv = new NodeView(vfid, x, y);
            String t = xd0.getParamValueStr("title");
            if (t != null)
              nv.setNodeTitle(t);
            this.funcNodes.add(nv);
          }
        } else if ("na".equals(tstr))
        {
          String vaid = xd0.getParamValueStr("node_id");
          if ((vaid != null) && (!vaid.equals("")))
          {
            int x = xd0.getParamValueInt32("x", -1);
            int y = xd0.getParamValueInt32("y", -1);
            NodeAction na = new NodeAction(vaid, x, y);
            String t = xd0.getParamValueStr("title");
            if (t != null)
              na.setNodeTitle(t);
            this.funcNodes.add(na);
          }

        }

      }

    }

    List<XmlData> npxds = xd.getSubDataArray("node_paths");
    if (npxds != null)
    {
    Iterator<XmlData> tstr2 = npxds.iterator();

      while (tstr2.hasNext())
      {
        XmlData npxd = tstr2.next();
        try
        {
          NodePath np = createPathByXmlData(npxd);
          if (np != null)
          {
            if ((np.getFromNode() != null) && (np.getTarNode() != null))
              this.fromId2Path.put(np.getFromStr(), np);
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static abstract interface INode
  {
    public abstract int getX();

    public abstract void setX(int paramInt);

    public abstract int getY();

    public abstract void setY(int paramInt);

    public abstract String getNodeId();

    public abstract String getNodeTitle()
      throws Exception;

    public abstract void setNodeTitle(String paramString);
  }

  public class NodeView
    implements BizFunc.INode
  {
    String viewPath = null;

    int x = -1;

    int y = -1;

    String title = null;

    XmlData ctrlData = null;

    NodeView(BizView bv, int x, int y)
    {
      this.viewPath = bv.getBizPath().toString();
      this.x = x;
      this.y = y;
    }

    NodeView(String viewpath, int x, int y)
    {
      this.viewPath = viewpath;
      this.x = x;
      this.y = y;
    }

    public BizView getBizView() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
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

    public String getNodeTitle()
    {
      if ((this.title != null) && (!this.title.equals(""))) {
        return this.title;
      }
      return this.viewPath;
    }

    public String getNodeShowTitle()
    {
      String tmps = this.title;
      if (tmps == null) {
        tmps = "";
      }
      return tmps + '-' + this.viewPath;
    }

    public void setNodeTitle(String t)
    {
      this.title = t;
    }

    public XmlData getCtrlXmlData()
    {
      return this.ctrlData;
    }

    public void setCtrlXmlData(XmlData xd)
    {
      this.ctrlData = xd;
    }

    public int hashCode()
    {
      return this.viewPath.hashCode();
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof NodeView)) {
        return false;
      }
      return this.viewPath.equals(((NodeView)o).viewPath);
    }

    public String getViewPath()
    {
      return this.viewPath;
    }

    public String getNodeId()
    {
      return this.viewPath;
    }

    public int getX()
    {
      return this.x;
    }

    public void setX(int x)
    {
      this.x = x;
    }

    public int getY()
    {
      return this.y;
    }

    public void setY(int y)
    {
      this.y = y;
    }
  }

  public class NodeAction implements BizFunc.INode
  {
    String actionPath = null;

    int x = -1;

    int y = -1;

    String title = null;

    NodeAction(BizAction ba, int x, int y)
    {
      this.actionPath = ba.getBizPath().toString();
      this.x = x;
      this.y = y;
    }

    NodeAction(String actpath, int x, int y)
    {
      this.actionPath = actpath;
      this.x = x;
      this.y = y;
    }

    public BizAction getBizAction() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
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

    public String getNodeTitle()
    {
      if ((this.title != null) && (!this.title.equals(""))) {
        return this.title;
      }
      return this.actionPath;
    }

    public void setNodeTitle(String t)
    {
      this.title = t;
    }

    public int hashCode()
    {
      return this.actionPath.hashCode();
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof NodeAction)) {
        return false;
      }
      return this.actionPath.equals(((NodeAction)o).actionPath);
    }

    public String getActionPath()
    {
      return this.actionPath;
    }

    public String getNodeId()
    {
      return this.actionPath;
    }

    public int getX()
    {
      return this.x;
    }

    public void setX(int x)
    {
      this.x = x;
    }

    public int getY()
    {
      return this.y;
    }

    public void setY(int y)
    {
      this.y = y;
    }
  }

  public abstract class NodePath
    implements IXmlDataable
  {
    String title = null;

    String fromNodePath = null;

    String fromOutputName = null;

    String tarNodePath = null;

    String fromStr = null;

    String pathStr = null;

    protected void setFromToStr(String fromnodeid, String fromoutput, String tarnodeid)
    {
      this.fromNodePath = fromnodeid;
      this.fromOutputName = fromoutput;

      this.tarNodePath = tarnodeid;

      this.fromStr = (this.fromNodePath + "+" + this.fromOutputName);
      this.pathStr = (this.fromStr + "-" + this.tarNodePath);
    }

    protected NodePath()
    {
    }

    public String getTitle()
    {
      if ((this.title != null) && (!this.title.equals(""))) {
        return this.title;
      }
      return this.fromOutputName;
    }

    public void setTitle(String t)
    {
      this.title = t;
    }

    public BizFunc.INode getFromNode()
    {
      for (BizFunc.INode n : BizFunc.this.funcNodes)
      {
        if (n.getNodeId().equals(this.fromNodePath)) {
          return n;
        }
      }
      return null;
    }

    public abstract BizOutput getFromOutput() throws Exception;

    public BizFunc.INode getTarNode()
    {
      for (BizFunc.INode n : BizFunc.this.funcNodes)
      {
        if (n.getNodeId().equals(this.tarNodePath)) {
          return n;
        }
      }
      return null;
    }

    public String getFromNodePath()
    {
      return this.fromNodePath;
    }

    public String getFromOutputName()
    {
      return this.fromOutputName;
    }

    public String getTarNodePath()
    {
      return this.tarNodePath;
    }

    public String getPathStr()
    {
      return this.pathStr;
    }

    public String getFromStr()
    {
      return this.fromStr;
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof NodePath)) {
        return false;
      }
      return this.pathStr.equals(((NodePath)o).pathStr);
    }

    public int hashCode()
    {
      return this.pathStr.hashCode();
    }

    public String toString()
    {
      return this.pathStr;
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      if (this.title != null)
        xd.setParamValue("title", this.title);
      if (this.fromNodePath != null) {
        xd.setParamValue("from_path", this.fromNodePath);
      }
      if (this.fromOutputName != null) {
        xd.setParamValue("from_output", this.fromOutputName);
      }
      if (this.tarNodePath != null)
        xd.setParamValue("tar_path", this.tarNodePath);
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      throw new RuntimeException("not support");
    }
  }

  public class ViewToActionPath extends BizFunc.NodePath
  {
    public ViewToActionPath(BizView frombv, String oper, BizAction tarba)
    {
      super();

      BizOutput op = frombv.getOutput(oper);
      if (op == null) {
        throw new IllegalArgumentException(
          "cannot find oper with name=" + oper + 
          " in BizView with name=" + frombv.getName());
      }
      setFromToStr(frombv.getBizPath().toString(), oper, 
        tarba.getBizPath().toString());
    }
    public ViewToActionPath(String frompath, String fromoutput, String tarpath) {
      super();

      if (!BizManager.isBizViewPath(frompath)) {
        throw new IllegalArgumentException("invalid view from path");
      }
      if (!BizManager.isBizActionPath(tarpath)) {
        throw new IllegalArgumentException("invalid action tar path");
      }
      setFromToStr(frompath, fromoutput, tarpath);
    }

    public BizView getFromBizView() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getFromNodePath());
      if (bn == null) {
        return null;
      }
      return (BizView)bn.getBizObj();
    }

    public BizOutput getFromOutput() throws Exception
    {
      BizView bv = getFromBizView();
      if (bv == null) {
        return null;
      }
      return bv.getOutput(getFromOutputName());
    }

    public BizAction getTarBizAction() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getTarNodePath());
      if (bn == null) {
        return null;
      }
      return (BizAction)bn.getBizObj();
    }
  }

  public class ViewToViewPath extends BizFunc.NodePath
  {
    public ViewToViewPath(BizView frombv, String oper, BizView tarbv)
    {
      super();

      BizOutput op = frombv.getOutput(oper);
      if (op == null) {
        throw new IllegalArgumentException(
          "cannot find oper with name=" + oper + 
          " in BizView with name=" + frombv.getName());
      }
      setFromToStr(frombv.getBizPath().toString(), oper, 
        tarbv.getBizPath().toString());
    }
    public ViewToViewPath(String frompath, String output, String tarpath) {
      super();

      if (!BizManager.isBizViewPath(frompath)) {
        throw new IllegalArgumentException("invalid view from id");
      }
      if (!BizManager.isBizViewPath(tarpath)) {
        throw new IllegalArgumentException("invalid view tar id");
      }
      setFromToStr(frompath, output, tarpath);
    }

    public BizView getFromBizView() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getFromNodePath());
      if (bn == null) {
        return null;
      }
      return (BizView)bn.getBizObj();
    }

    public BizOutput getFromOutput() throws Exception
    {
      BizView bv = getFromBizView();
      if (bv == null) {
        return null;
      }
      return bv.getOutput(getFromOutputName());
    }

    public BizView getTarBizView() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getTarNodePath());
      if (bn == null) {
        return null;
      }
      return (BizView)bn.getBizObj();
    }
  }

  public class ActionToViewPath extends BizFunc.NodePath
  {
    public ActionToViewPath(BizAction fromba, String oper, BizView tarbv)
    {
      super();

      BizOutput op = fromba.getOutput(oper);
      if (op == null)
        throw new IllegalArgumentException(
          "cannot find oper with name=" + oper + 
          " in BizAction with name=" + fromba.getName());
      setFromToStr(fromba.getBizPath().toString(), oper, 
        tarbv.getBizPath().toString());
    }
    public ActionToViewPath(String fromstr, String output, String tarstr) {
      super();

      if (!BizManager.isBizActionPath(fromstr)) {
        throw new IllegalArgumentException("invalid action from id");
      }
      if (!BizManager.isBizViewPath(tarstr)) {
        throw new IllegalArgumentException("invalid view tar id");
      }
      setFromToStr(fromstr, output, tarstr);
    }

    public BizAction getFromBizAction() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getFromNodePath());
      if (bn == null) {
        return null;
      }
      return (BizAction)bn.getBizObj();
    }

    public BizOutput getFromOutput() throws Exception
    {
      BizAction ba = getFromBizAction();
      if (ba == null) {
        return null;
      }
      return ba.getOutput(getFromOutputName());
    }

    public BizView getTarBizView() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getTarNodePath());
      if (bn == null) {
        return null;
      }
      return (BizView)bn.getBizObj();
    }
  }

  public class ActionToActionPath extends BizFunc.NodePath
  {
    public ActionToActionPath(BizAction fromba, String oper, BizAction tarba)
    {
      super();

      BizOutput op = fromba.getOutput(oper);
      if (op == null)
        throw new IllegalArgumentException(
          "cannot find oper with name=" + oper + 
          " in BizAction with name=" + fromba.getName());
      setFromToStr(fromba.getBizPath().toString(), oper, 
        tarba.getBizPath().toString());
    }
    public ActionToActionPath(String fromstr, String output, String tarstr) {
      super();

      if (!BizManager.isBizActionPath(fromstr)) {
        throw new IllegalArgumentException("invalid action from id");
      }
      if (!BizManager.isBizActionPath(tarstr)) {
        throw new IllegalArgumentException("invalid action tar id");
      }
      setFromToStr(fromstr, output, tarstr);
    }

    public BizAction getFromBizAction() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getFromNodePath());
      if (bn == null) {
        return null;
      }
      return (BizAction)bn.getBizObj();
    }

    public BizOutput getFromOutput() throws Exception
    {
      BizAction ba = getFromBizAction();
      if (ba == null) {
        return null;
      }
      return ba.getOutput(getFromOutputName());
    }

    public BizAction getTarBizAction() throws Exception
    {
      BizNode bn = BizFunc.this.getBizContainer().getBizNodeByPath(
        getTarNodePath());
      if (bn == null) {
        return null;
      }
      return (BizAction)bn.getBizObj();
    }
  }
}
