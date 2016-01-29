package com.dw.biz.flow_ins;

import com.dw.system.gdb.GDB;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

@XORMClass(table_name="biz_flow_ins")
public class BizFlowInsContext
{
  public static final String PROP_NODE_INS_MAP = "NodeInsMap";
  public static final String PROP_DATA_FIELD_DATA = "DataFieldData";

  @XORMProperty(name="FlowInsId", has_col=true, is_pk=true, is_auto=true)
  private long insId = -1L;

  @XORMProperty(name="FlowPath", has_col=true, has_idx=true, max_len=50, order_num=5)
  private String flowPath = null;

  @XORMProperty(name="Title", has_col=true, max_len=200, order_num=10)
  String title = null;

  @XORMProperty(name="CreationUser", has_col=true, has_idx=true, max_len=20, order_num=11)
  private String creationUser = null;

  @XORMProperty(name="CreationDate", has_col=true, order_num=20)
  private Date creationDate = new Date();

  @XORMProperty(name="StartDate", has_col=true, order_num=30)
  private Date startDate = new Date();

  @XORMProperty(name="CloseDate", has_col=true, order_num=40)
  private Date closeDate = null;

  private BizFlowInsState state = BizFlowInsState.open_notStarted;

  @XORMProperty(name="InputParam")
  XmlData inputParam = null;

  @XORMProperty(name="OutputParam")
  XmlData outputParam = null;

  @XORMProperty(name="DataFieldData", has_col=true)
  private XmlData dataFieldData = new XmlData();

  private NodeId2InsMap nodeId2InsMap = new NodeId2InsMap();

  transient boolean bDirty = false;

  private transient int _scan_id = 1;

  @XORMProperty(name="InsState", has_col=true, order_num=50)
  private int get_InsState()
  {
    return this.state.getValue();
  }

  private void set_InsState(int v) {
    this.state = BizFlowInsState.valueOf(v);
  }

  @XORMProperty(name="NodeInsMap", has_col=true)
  private XmlData get_NodeId2InsMap()
  {
    return this.nodeId2InsMap.toXmlData();
  }

  private void set_NodeId2InsMap(XmlData xd) {
    this.nodeId2InsMap = new NodeId2InsMap();
    this.nodeId2InsMap.fromXmlData(xd);
  }

  public BizFlowInsContext()
  {
  }

  public BizFlowInsContext(BizFlowInsContext cxt)
  {
    this.insId = cxt.insId;

    this.flowPath = cxt.flowPath;
    this.title = cxt.title;
    this.creationUser = cxt.creationUser;

    this.creationDate = new Date(cxt.creationDate.getTime());
    this.startDate = new Date(cxt.startDate.getTime());
    this.closeDate = cxt.closeDate;
    this.state = cxt.state;

    this.inputParam = cxt.inputParam;
    if (this.inputParam != null) {
      this.inputParam = this.inputParam.copyMe();
    }
    this.outputParam = cxt.outputParam;
    if (this.outputParam != null) {
      this.outputParam = this.outputParam.copyMe();
    }
    this.dataFieldData = cxt.dataFieldData.copyMe();
    this.nodeId2InsMap = new NodeId2InsMap(cxt.nodeId2InsMap);
  }

  public BizFlowInsContext(UserProfile up, String flowpath, String title, XmlData inputparam)
  {
    if (up != null) {
      this.creationUser = up.getUserName();
    }
    this.flowPath = flowpath;

    this.title = title;
    this.inputParam = inputparam;
    if (inputparam != null) {
      this.dataFieldData.combineAppend(inputparam);
    }
    this.bDirty = true;
  }

  void increaseScanId()
  {
    this._scan_id += 1;
  }

  public long getInsId()
  {
    return this.insId;
  }

  public String getFlowPath()
  {
    return this.flowPath;
  }

  public Date getCreationDate()
  {
    return this.creationDate;
  }

  public String getCreationUserName()
  {
    return this.creationUser;
  }

  public Date getStartDate()
  {
    return this.startDate;
  }

  public BizFlowInsState getInsState()
  {
    return this.state;
  }

  public void setInsState(BizFlowInsState st)
  {
    this.state = st;
  }

  public Date getCloseDate()
  {
    return this.closeDate;
  }

  public String getInsTitle()
  {
    if (this.title == null)
      return "";
    return this.title;
  }

  XmlData getInsRTXmlData()
  {
    XmlData tmpxd = new XmlData();
    tmpxd.setParamValue("id", Long.valueOf(getInsId()));
    String t = getInsTitle();
    if (t != null) {
      tmpxd.setParamValue("title", t);
    }
    tmpxd.setParamValue("start_date", getStartDate());
    tmpxd.setParamValue("flow_path", getFlowPath());
    return tmpxd;
  }

  public XmlData getInputParam()
  {
    return this.inputParam;
  }

  public XmlData getOutputParam()
  {
    return this.outputParam;
  }

  public XmlData getDataField()
  {
    return this.dataFieldData;
  }

  public Object getDataFieldVal(String df_name)
  {
    return this.dataFieldData.getParamValue(df_name);
  }

  public void setDataFieldVal(String df_name, Object v)
  {
    if (v == null)
    {
      this.dataFieldData.removeParam(df_name);
      return;
    }

    this.dataFieldData.setParamValue(df_name, v);
  }

  public void writeToDataField(XmlData xd)
  {
    this.dataFieldData.combineAppend(xd);
  }

  public Object getXmlDataFieldVal(String df_name)
  {
    return this.dataFieldData.getParamValue(df_name);
  }

  public void setXmlDataFieldVal(String df_name, XmlData xd)
  {
    if (xd == null)
    {
      this.dataFieldData.removeSubData(df_name);
      return;
    }

    this.dataFieldData.setSubDataSingle(df_name, xd);
  }

  public void checkCanRun()
  {
    throw new RuntimeException("instance is not in open running state");
  }

  public NodeId2InsMap getNodeId2InsMap()
  {
    return this.nodeId2InsMap;
  }

  public NodeId2InsItem getNextCurRunningNodeIns()
  {
    return this.nodeId2InsMap.getNextCurRunningNodeIns(this._scan_id);
  }

  public ArrayList<NodeId2InsItem> getAllCurRunningNodeIns()
  {
    return NodeId2InsMap.access$1(this.nodeId2InsMap, this._scan_id);
  }

  public void saveNodeId2InsMap()
    throws Exception
  {
    GDB.getInstance().updateXORMObjToDBWithHasColNames(Long.valueOf(this.insId), this, new String[] { "NodeInsMap" });
  }

  public void saveContext()
    throws Exception
  {
    if (this.insId <= 0L)
      GDB.getInstance().addXORMObjWithNewId(this);
    else {
      GDB.getInstance().updateXORMObjToDB(Long.valueOf(this.insId), this);
    }

    StringBuilder sb = new StringBuilder();
    BizFlowInsManager.getInstance().updateCxtIdx(this, true, sb);

    this.bDirty = false;
  }

  public static BizFlowInsContext loadContext(long cxtid) throws Exception
  {
    BizFlowInsContext bfi = (BizFlowInsContext)GDB.getInstance().getXORMObjByPkId(BizFlowInsContext.class, Long.valueOf(cxtid));
    bfi.bDirty = false;
    return bfi;
  }

  public static class NodeId2InsMap
    implements IXmlDataable
  {
    private Hashtable<String, BizFlowInsContext.NodeId2InsItem> nodeId2Ins = new Hashtable();

    public NodeId2InsMap()
    {
    }

    public NodeId2InsMap(NodeId2InsMap niim) {
      if (niim == null) {
        return;
      }
      for (Map.Entry nid2ins : niim.nodeId2Ins.entrySet())
      {
        this.nodeId2Ins.put((String)nid2ins.getKey(), new BizFlowInsContext.NodeId2InsItem((BizFlowInsContext.NodeId2InsItem)nid2ins.getValue()));
      }
    }

    public XmlData toXmlData()
    {
      XmlData tmpxd = new XmlData();
      for (Map.Entry n2nii : this.nodeId2Ins.entrySet())
      {
        tmpxd.setSubDataSingle((String)n2nii.getKey(), ((BizFlowInsContext.NodeId2InsItem)n2nii.getValue()).toXmlData());
      }
      return tmpxd;
    }

    public void fromXmlData(XmlData xd)
    {
      String[] sns = xd.getSubDataNames();
      if (sns == null) {
        return;
      }
      for (String sn : sns)
      {
        XmlData tmpxd = xd.getSubDataSingle(sn);
        BizFlowInsContext.NodeId2InsItem niii = new BizFlowInsContext.NodeId2InsItem();
        niii.fromXmlData(tmpxd);
        this.nodeId2Ins.put(sn, niii);
      }
    }

    private BizFlowInsContext.NodeId2InsItem getNextCurRunningNodeIns(int scanid)
    {
      if (this.nodeId2Ins.size() <= 0) {
        return null;
      }
      for (BizFlowInsContext.NodeId2InsItem n2i : this.nodeId2Ins.values())
      {
        if (n2i.checkRunningWithScan(scanid)) {
          return n2i;
        }
      }
      return null;
    }

    private ArrayList<BizFlowInsContext.NodeId2InsItem> getCurRunningNodeIns(int scanid)
    {
      if (this.nodeId2Ins.size() <= 0) {
        return null;
      }
      ArrayList rets = new ArrayList();
      for (BizFlowInsContext.NodeId2InsItem n2i : this.nodeId2Ins.values())
      {
        if (n2i.checkRunningWithScan(scanid)) {
          rets.add(n2i);
        }
      }
      return rets;
    }

    public BizFlowInsContext.NodeId2InsItem getNodeInsItemByNodeId(String nodeid)
    {
      BizFlowInsContext.NodeId2InsItem ret = (BizFlowInsContext.NodeId2InsItem)this.nodeId2Ins.get(nodeid);
      if (ret == null)
      {
        ret = new BizFlowInsContext.NodeId2InsItem(nodeid);
        this.nodeId2Ins.put(nodeid, ret);
      }
      return ret;
    }

    public boolean isRunningNodeId(String nid)
    {
      BizFlowInsContext.NodeId2InsItem ret = (BizFlowInsContext.NodeId2InsItem)this.nodeId2Ins.get(nid);
      if (ret == null) {
        return false;
      }
      return ret.isRunning();
    }
  }

  public static class NodeId2InsItem
    implements IXmlDataable
  {
    String nodeId = null;

    boolean bRunning = false;

    ArrayList<Long> relatedInsIds = new ArrayList();

    String selTransId = null;

    transient int _scanId = 0;

    public NodeId2InsItem()
    {
    }

    public NodeId2InsItem(NodeId2InsItem niii) {
      if (niii == null) {
        return;
      }
      this.nodeId = niii.nodeId;
      this.bRunning = niii.bRunning;
      this.relatedInsIds = new ArrayList(niii.relatedInsIds);
    }

    public NodeId2InsItem(String nodeid)
    {
      this.nodeId = nodeid;
    }

    public String getNodeId()
    {
      return this.nodeId;
    }

    public String getSelectTransId()
    {
      return this.selTransId;
    }

    public void setSelectTransId(String tid)
    {
      this.selTransId = tid;
    }

    public void increateInsCount()
    {
      this.relatedInsIds.add(Long.valueOf(-1L));
      setCurInsId(-1L);
    }

    public int getInsCount()
    {
      return this.relatedInsIds.size();
    }

    public boolean isRunning()
    {
      return this.bRunning;
    }

    public void setRunning(boolean b)
    {
      this.bRunning = b;
    }

    public boolean checkRunningWithScan(int scanid)
    {
      if (!this.bRunning) {
        return false;
      }
      boolean b = scanid > this._scanId;
      this._scanId = scanid;
      return b;
    }

    public long getInsId(int c)
    {
      return ((Long)this.relatedInsIds.get(c)).longValue();
    }

    public List<Long> getRelatedIds()
    {
      return this.relatedInsIds;
    }

    public long getCurInsId()
    {
      if (this.relatedInsIds.size() <= 0) {
        throw new RuntimeException("instance count<=0");
      }
      return ((Long)this.relatedInsIds.get(this.relatedInsIds.size() - 1)).longValue();
    }

    public void setCurInsId(long insid)
    {
      this.relatedInsIds.set(this.relatedInsIds.size() - 1, Long.valueOf(insid));
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("node_id", this.nodeId);

      xd.setParamValue("is_running", Boolean.valueOf(this.bRunning));
      if (this.selTransId != null) {
        xd.setParamValue("sel_trans_id", this.selTransId);
      }
      if (this.relatedInsIds != null)
        xd.setParamValues("ins_ids", this.relatedInsIds);
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      this.nodeId = xd.getParamValueStr("node_id");

      this.bRunning = xd.getParamValueBool("is_running", false).booleanValue();

      this.selTransId = xd.getParamValueStr("sel_trans_id");

      Long[] ids = xd.getParamValuesInt64("ins_ids");
      if (ids != null)
      {
        for (Long id : ids)
          this.relatedInsIds.add(id);
      }
    }
  }
}
