package com.dw.biz.workitem;

import com.dw.biz.BizParticipant.AssignmentStyle;
import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.xmldata.XmlData;
import java.io.UnsupportedEncodingException;
import java.util.Date;

@XORMClass(table_name="biz_workitem")
public class BizWorkItem
{

  @XORMProperty(name="WorkItemId", has_col=true, is_pk=true, is_auto=true)
  long id = -1L;

  @XORMProperty(name="Title", has_col=true, max_len=100, auto_truncate=true, order_num=20)
  String title = null;

  @XORMProperty(name="CreationDate", has_col=true, has_idx=true, order_num=35)
  Date creationDate = new Date();

  @XORMProperty(name="EndDate", has_col=true, order_num=37)
  Date endDate = null;

  State state = State.Normal;

  @XORMProperty(name="CreationUser", has_col=true, max_len=15, default_str_val="", has_idx=true, order_num=40)
  String creationUser = null;

  @XORMProperty(name="AssignedUser", has_col=true, max_len=15, default_str_val="", has_idx=true, order_num=50)
  String assignedUser = null;

  @XORMProperty(name="AssignedChk", has_col=true, default_str_val="0", order_num=51)
  boolean assignedChecked = false;

  @XORMProperty(name="FinishedChk", has_col=true, default_str_val="0", order_num=53)
  boolean finishedChecked = false;

  BizParticipant.AssignmentStyle assignStyle = BizParticipant.AssignmentStyle.auto;

  @XORMProperty(name="ViewPath", has_col=true, max_len=100, default_str_val="", has_idx=true, order_num=60)
  String bizViewPath = null;

  @XORMProperty(name="RelatedIdx", has_col=true, max_len=25, default_str_val="", has_idx=true, order_num=70)
  String relatedIdx = null;

  @XORMProperty(name="FinishActionPath", max_len=25, has_col=true, order_num=80)
  String finishBizActionPath = null;

  @XORMProperty(name="CostSecond", has_col=true, order_num=90)
  long costSecond = -1L;

  @XORMProperty(name="FlowInsId", has_col=true, has_idx=true, max_len=20, default_str_val="-1", order_num=100)
  long flowInsId = -1L;

  @XORMProperty(name="FlowPath", is_transient=true, max_len=20, default_str_val="", order_num=110)
  transient String flowPath = null;

  @XORMProperty(name="FlowNodeInsId", has_col=true, max_len=20, default_str_val="", order_num=110)
  String flowNodeId = null;

  @XORMProperty(name="Priority", has_col=true, default_str_val="0", order_num=120)
  int priority = 0;

  @XORMProperty(name="LimitDate", has_col=true, order_num=130)
  Date limitDate = null;

  @XORMProperty(name="ReplaceUsers", has_col=true, max_len=100, order_num=130)
  String replaceUsers = null;

  XmlData dataCont = null;

  @XORMProperty(name="State", has_col=true, default_str_val="0", order_num=30)
  private int get_State()
  {
    return this.state.getValue();
  }

  private void set_State(int st) {
    this.state = State.valueOf(st);
  }

  @XORMProperty(name="AssignStyle", has_col=true, default_str_val="0", order_num=55)
  int get_AssignStyle()
  {
    return this.assignStyle.getValue();
  }

  void set_AssignStyle(int i)
  {
    this.assignStyle = BizParticipant.AssignmentStyle.valueOf(i);
  }

  @XORMProperty(name="DataCont", has_col=true, order_num=140)
  private byte[] get_DataCont()
    throws UnsupportedEncodingException
  {
    if (this.dataCont == null) {
      return null;
    }
    return this.dataCont.toBytesWithUTF8();
  }

  private void set_DataCont(byte[] cont) throws Exception
  {
    if ((cont == null) || (cont.length <= 0))
    {
      this.dataCont = null;
      return;
    }

    this.dataCont = XmlData.parseFromByteArrayUTF8(cont);
  }

  public BizWorkItem()
  {
  }

  public BizWorkItem(String title, String assign_user, String biz_viewpath, String relatedidx, String finish_actpath, long flow_insid, String flow_nodeid, XmlData data_cont)
  {
    this.title = title;
    this.assignedUser = assign_user;
    this.bizViewPath = biz_viewpath;
    this.relatedIdx = relatedidx;
    this.finishBizActionPath = finish_actpath;
    if (flow_insid <= 0L) {
      flow_insid = -1L;
    }
    if (flow_insid > 0L)
    {
      if ((flow_nodeid == null) || (flow_nodeid.equals("")))
        throw new IllegalArgumentException("flow node workitem must has flow node id!");
      this.flowInsId = flow_insid;
      this.flowNodeId = flow_nodeid;
    }

    this.dataCont = data_cont;
  }

  private long get_WorkItemId()
  {
    return this.id;
  }

  private void set_WorkItemId(long id)
  {
    this.id = id;
  }

  private String get_Title()
  {
    return this.title;
  }

  private void set_Title(String t)
  {
    this.title = t;
  }

  private String get_BizViewPath()
  {
    return this.bizViewPath;
  }

  private void set_BizViewPath(String p)
  {
    this.bizViewPath = p;
  }

  private String get_RelatedIdx()
  {
    return this.relatedIdx;
  }

  private void set_RelatedIdx(String ridx)
  {
    this.relatedIdx = ridx;
  }

  private String get_FinishActionPath()
  {
    return this.finishBizActionPath;
  }

  private void set_FinishActionPath(String p)
  {
    this.finishBizActionPath = p;
  }

  private String get_AssignedUser()
  {
    return this.assignedUser;
  }

  private void set_AssignedUser(String au)
  {
    this.assignedUser = au;
  }

  private long get_CostSecond()
  {
    return this.costSecond;
  }

  private void set_CostSecond(long cs)
  {
    this.costSecond = cs;
  }

  private int get_Priority()
  {
    return this.priority;
  }

  private void set_Priority(int pri)
  {
    this.priority = pri;
  }

  private Date get_LimitDate()
  {
    return this.limitDate;
  }

  private void set_LimitDate(Date lt)
  {
    this.limitDate = lt;
  }

  private long get_FlowInsId()
  {
    return this.flowInsId;
  }

  private void set_FlowInsId(long fid)
  {
    this.flowInsId = fid;
  }

  private String get_FlowNodeInsId()
  {
    return this.flowNodeId;
  }

  private void set_FlowNodeInsId(String fnid)
  {
    this.flowNodeId = fnid;
  }

  public long getWorkItemId()
  {
    return this.id;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getCreationUser()
  {
    return this.creationUser;
  }

  public String getAssignedUser()
  {
    return this.assignedUser;
  }

  public Date getCreationDate()
  {
    return this.creationDate;
  }

  public BizParticipant.AssignmentStyle getAssignmentStyle()
  {
    return this.assignStyle;
  }

  public String getBizViewPath()
  {
    return this.bizViewPath;
  }

  public String getRelatedIdx()
  {
    return this.relatedIdx;
  }

  public String getFinishBizActionPath()
  {
    return this.finishBizActionPath;
  }

  public Date getEndDate()
  {
    return this.endDate;
  }

  public State getState()
  {
    return this.state;
  }

  public boolean isAssignChecked()
  {
    return this.assignedChecked;
  }

  public boolean isFinishChecked()
  {
    return this.finishedChecked;
  }

  public boolean isClose()
  {
    return this.state != State.Normal;
  }

  public long getCostSecond()
  {
    return this.costSecond;
  }

  public String getCostHour()
  {
    double d = this.costSecond;
    return d / 3600.0D;
  }

  public int getPriority()
  {
    return this.priority;
  }

  public String getPriorityStr()
  {
    if (this.priority <= 0) {
      return "Normal";
    }
    if (this.priority == 1) {
      return "Middle";
    }
    if (this.priority > 1) {
      return "High";
    }
    return "Normal";
  }

  public Date getLimitDate()
  {
    return this.limitDate;
  }

  public String getLimitDateStr()
  {
    if (this.limitDate == null)
      return "";
    return Convert.toYMDHM(this.limitDate);
  }

  public long getDelayMinutes()
  {
    if (this.limitDate == null) {
      return -1L;
    }
    long delayms = System.currentTimeMillis() - this.limitDate.getTime();
    if (delayms <= 0L) {
      return -1L;
    }
    return delayms / 60000L;
  }

  public long getFlowInsId()
  {
    return this.flowInsId;
  }

  public String getFlowPath()
  {
    return this.flowPath;
  }

  public String getFlowNodeId() {
    return this.flowNodeId;
  }

  public String getReplaceUsers()
  {
    return this.replaceUsers;
  }

  public void setReplaceUsers(String repu)
  {
    this.replaceUsers = repu;
  }

  public boolean canUserReplaceDo(String usern)
  {
    if (this.state != State.Normal) {
      return false;
    }

    if (Convert.isNullOrEmpty(this.replaceUsers)) {
      return false;
    }
    return this.replaceUsers.indexOf("|" + usern + "|") >= 0;
  }

  public XmlData getDataCont()
  {
    return this.dataCont;
  }

  void setDataCont(XmlData xd)
  {
    this.dataCont = xd;
  }

  public static enum State
  {
    Normal(0), 
    Finished(1), 
    Canceled(2), 
    Abort(3);

    private final int val;

    private State(int v) {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public static State valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return Normal;
      case 1:
        return Finished;
      case 2:
        return Canceled;
      case 3:
        return Abort;
      }
      return Normal;
    }
  }
}
