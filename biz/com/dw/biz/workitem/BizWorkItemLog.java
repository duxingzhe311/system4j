package com.dw.biz.workitem;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import java.util.Date;

@XORMClass(table_name="biz_workitem_log")
public class BizWorkItemLog
{

  @XORMProperty(name="LogId", has_col=true, is_pk=true, is_auto=true)
  long logId = -1L;

  @XORMProperty(name="WorkItemId", has_col=true, nullable=false, has_idx=true, has_fk=true, fk_table="biz_workitem", fk_column="WorkItemId", order_num=10)
  long workItemId = -1L;

  @XORMProperty(name="UserName", has_col=true, max_len=15, has_idx=true, default_str_val="", order_num=20)
  String userName = null;

  @XORMProperty(name="LogDate", has_col=true, order_num=30)
  Date logDate = null;

  LogType logType = null;

  @XORMProperty(name="LogDesc", has_col=true, max_len=200, order_num=50)
  String logDesc = null;

  @XORMProperty(name="LogType", has_col=true, default_str_val="0", order_num=40)
  private int get_LogType()
  {
    return this.logType.getValue();
  }

  private void set_LogType(int lt)
  {
    this.logType = LogType.valueOf(lt);
  }

  public BizWorkItemLog()
  {
  }

  public BizWorkItemLog(long workitemid, String usern, LogType lt, String desc)
  {
    if (lt == null) {
      throw new IllegalArgumentException("log type cannot be null!");
    }
    this.workItemId = workitemid;
    this.userName = usern;
    this.logDate = new Date();
    this.logType = lt;
    this.logDesc = desc;
  }

  private long get_LogId()
  {
    return this.logId;
  }

  private void set_LogId(long lid)
  {
    this.logId = lid;
  }

  private long get_WorkItemId()
  {
    return this.workItemId;
  }

  private void set_WorkItemId(long wiid)
  {
    this.workItemId = wiid;
  }

  private String get_UserName()
  {
    return this.userName;
  }

  private void set_UserName(String un)
  {
    this.userName = un;
  }

  private Date get_LogDate()
  {
    return this.logDate;
  }

  private void set_LogDate(Date ld)
  {
    this.logDate = ld;
  }

  private String get_LogDesc()
  {
    return this.logDesc;
  }

  private void set_LogDesc(String ld)
  {
    this.logDesc = ld;
  }

  public long getLogId()
  {
    return this.logId;
  }

  public long getWorkItemId()
  {
    return this.workItemId;
  }

  public String getUserName()
  {
    return this.userName;
  }

  public Date getLogDate()
  {
    return this.logDate;
  }

  public LogType getLogType()
  {
    return this.logType;
  }

  public String getLogDesc()
  {
    return this.logDesc;
  }

  public static enum LogType
  {
    Create(0), 
    Acquire(1), 
    Finish(2), 
    Assign(3), 
    Cancel(4), 
    Direct(5), 

    Abort(6);

    private final int val;

    private LogType(int v) {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public static LogType valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return Create;
      case 1:
        return Acquire;
      case 2:
        return Finish;
      case 3:
        return Assign;
      case 4:
        return Cancel;
      case 5:
        return Direct;
      case 6:
        return Abort;
      }
      return Create;
    }
  }
}
