package com.dw.ext.timelimit;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import java.util.Calendar;
import java.util.Date;

@XORMClass(table_name="ext_time_limit")
public class TLItem
{

  @XORMProperty(name="TLId", has_col=true, is_pk=true, is_auto=true)
  long tlId = -1L;

  @XORMProperty(name="AppName", has_col=true, max_len=20, has_idx=true, order_num=5)
  String appName = null;

  @XORMProperty(name="PlugType", has_col=true, max_len=20, has_idx=true, order_num=6)
  String plugType = null;

  @XORMProperty(name="AppId", has_col=true, max_len=20, has_idx=true, order_num=7)
  String appId = null;

  @XORMProperty(name="AppTag", has_col=true, max_len=20, has_idx=true, order_num=8)
  String appTag = null;

  @XORMProperty(name="Owner", has_col=true, max_len=20, has_idx=true, order_num=10)
  String owner = null;

  @XORMProperty(name="Assistant", has_col=true, max_len=20, has_idx=true, order_num=15)
  String assistant = null;

  @XORMProperty(name="Monitor", has_col=true, max_len=20, has_idx=true, order_num=20)
  String monitor = null;

  @XORMProperty(name="Title", has_col=true, max_len=100, order_num=30)
  String title = null;

  @XORMProperty(name="LimitDate", has_col=true, max_len=100, order_num=40)
  Date limitDate = null;

  @XORMProperty(name="BeforeRemindDay", has_col=true, max_len=100, order_num=41)
  int beforeRemindDay = -1;

  State timeState = State.Normal;

  AfterEffect preAfterEffect = AfterEffect.Broken_Normal;

  @XORMProperty(name="Description", has_col=true, max_len=400, order_num=75)
  String desc = null;

  AfterEffect realAfterEffect = AfterEffect.Normal;

  @XORMProperty(name="IsFoundBroken", has_col=true, order_num=82)
  boolean bFoundBroken = false;

  @XORMProperty(name="ApplyFinishDate", has_col=true, order_num=110)
  Date applyFinishDate = null;

  @XORMProperty(name="ConfirmFinishDate", has_col=true, order_num=120)
  Date confirmFinishDate = null;

  @XORMProperty(name="FinishDesc", has_col=true, max_len=300, order_num=130)
  String finishDesc = null;

  @XORMProperty(name="FinishCostSec", has_col=true, max_len=300, order_num=132)
  long finishCostSecond = -1L;

  @XORMProperty(name="IsApplyDelay", has_col=true, default_str_val="0", order_num=135)
  boolean bApplyDelay = false;

  @XORMProperty(name="ApplyDelayDate", has_col=true, order_num=137)
  Date applyDelayDate = null;

  @XORMProperty(name="DelayDesc", has_col=true, max_len=300, order_num=140)
  String delayDesc = null;

  @XORMProperty(name="IsApplyCancel", has_col=true, default_str_val="0", order_num=150)
  boolean bApplyCancel = false;

  @XORMProperty(name="CancelDesc", has_col=true, max_len=300, order_num=160)
  String cancelDesc = null;

  @XORMProperty(name="StartRemindDate", has_col=true, order_num=42)
  private Date get_StartRemindDate()
  {
    if (this.beforeRemindDay <= 0) {
      return null;
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(this.limitDate);
    cal.add(5, -this.beforeRemindDay);
    return cal.getTime();
  }

  private void set_StartRemindDate(Date d)
  {
  }

  @XORMProperty(name="State", has_col=true, default_str_val="0", order_num=60)
  private int get_State()
  {
    return this.timeState.getValue();
  }

  private void set_State(int v) {
    this.timeState = State.valueOf(v);
  }

  @XORMProperty(name="PreAfterEffect", has_col=true, default_str_val="0", order_num=70)
  private int get_PreAfterEffect()
  {
    return this.preAfterEffect.getValue();
  }

  private void set_PreAfterEffect(int v) {
    this.preAfterEffect = AfterEffect.valueOf(v);
  }

  @XORMProperty(name="RealAfterEffect", has_col=true, default_str_val="0", order_num=80)
  private int get_RealAfterEffect()
  {
    return this.realAfterEffect.getValue();
  }

  private void set_RealAfterEffect(int v) {
    this.realAfterEffect = AfterEffect.valueOf(v);
  }

  public TLItem()
  {
  }

  public TLItem(String appname, String plugtype, String appid, String owner, String assistant, String monitor, String title, String desc, Date tl, int before_remind_day, AfterEffect pre)
  {
    if (before_remind_day < 0) {
      throw new IllegalArgumentException("before remind day cannot less 0");
    }
    this.appName = appname;
    this.plugType = plugtype;
    this.appId = appid;
    this.owner = owner;
    this.assistant = assistant;
    this.monitor = monitor;
    this.title = title;
    this.desc = desc;
    this.limitDate = tl;

    this.beforeRemindDay = before_remind_day;

    this.preAfterEffect = pre;
  }

  public TLItem(String appname, String plugtype, String appid, String apptag, String owner, String assistant, String monitor, String title, String desc, Date tl, int before_remind_day, AfterEffect pre)
  {
    this(appname, plugtype, appid, 
      owner, assistant, monitor, 
      title, desc, tl, before_remind_day, 
      pre);
    this.appTag = apptag;
  }

  public long getTLId()
  {
    return this.tlId;
  }

  public String getAppName()
  {
    return this.appName;
  }

  public String getPlugType()
  {
    return this.plugType;
  }

  public String getAppId()
  {
    return this.appId;
  }

  public void setAppId(String appid)
  {
    this.appId = appid;
  }

  public String getAppTag()
  {
    return this.appTag;
  }

  public TLPlugs.TLPlug getRelatedPlug()
  {
    return TLManager.getInstance().getPlug(this.appName, this.plugType);
  }

  public String getOwner()
  {
    return this.owner;
  }

  public void setOwner(String owner)
  {
    this.owner = owner;
  }

  public String getAssistant()
  {
    return this.assistant;
  }

  public void setAssistant(String assis)
  {
    this.assistant = assis;
  }

  public String getMonitor()
  {
    return this.monitor;
  }

  public void setMonitor(String m)
  {
    this.monitor = m;
  }

  public String getTitle()
  {
    return this.title;
  }

  public Date getLimitDate()
  {
    return this.limitDate;
  }

  public int getBeforeRemindDay()
  {
    return this.beforeRemindDay;
  }

  public State getState()
  {
    return this.timeState;
  }

  public AfterEffect getPreAfterEffect()
  {
    return this.preAfterEffect;
  }

  public AfterEffect getRealAfterEffect()
  {
    return this.realAfterEffect;
  }

  public Date getApplyFinishDate()
  {
    return this.applyFinishDate;
  }

  public Date getCheckFinishDate()
  {
    return this.confirmFinishDate;
  }

  public String getFinishDesc()
  {
    return this.finishDesc;
  }

  public long getFinishCostSecond()
  {
    return this.finishCostSecond;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public boolean isFoundBroken()
  {
    return this.bFoundBroken;
  }

  public boolean isApplyDelay()
  {
    return this.bApplyDelay;
  }

  public Date getApplyDelayDate()
  {
    return this.applyDelayDate;
  }

  public String getDelayDesc()
  {
    return this.delayDesc;
  }

  public boolean isApplyCancel()
  {
    return this.bApplyCancel;
  }

  public String getCancelDesc()
  {
    return this.cancelDesc;
  }

  public static enum State
  {
    Normal(0), 
    Cancel(1), 
    ApplyFinish(2), 
    Finish(3);

    private final int val;

    private State(int v)
    {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public String getTitle()
    {
      switch (this.val)
      {
      case 0:
        return "活动";
      case 1:
        return "取消";
      case 2:
        return "申请完成";
      case 3:
        return "完成";
      }
      return "";
    }

    public static State valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return Normal;
      case 1:
        return Cancel;
      case 2:
        return ApplyFinish;
      case 3:
        return Finish;
      }
      throw null;
    }
  }

  public static enum AfterEffect
  {
    Normal(0), 
    Broken_Ignore(1), 
    Broken_Normal(2), 
    Broken_Serious(3), 
    Broken_VerySerious(4);

    private final int val;

    private AfterEffect(int v)
    {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public static AfterEffect valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return Normal;
      case 1:
        return Broken_Ignore;
      case 2:
        return Broken_Normal;
      case 3:
        return Broken_Serious;
      case 4:
        return Broken_VerySerious;
      }
      throw new IllegalArgumentException("unknow state value=" + v);
    }
  }
}
