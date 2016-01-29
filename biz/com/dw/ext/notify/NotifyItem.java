package com.dw.ext.notify;

import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import java.util.Date;

@XORMClass(table_name="ext_notify")
public class NotifyItem
{

  @XORMProperty(name="AutoId", has_col=true, is_pk=true, is_auto=true)
  long autoId = -1L;

  @XORMProperty(name="SorUserName", max_len=50, has_col=true, has_idx=true, order_num=10)
  String sorUserName = null;

  @XORMProperty(name="TarUserName", max_len=50, has_col=true, has_idx=true, nullable=false, order_num=20)
  String tarUserName = null;

  State state = State.NewUnknow;

  @XORMProperty(name="SentDate", has_col=true, order_num=40)
  Date sentDate = null;

  @XORMProperty(name="ReadDate", has_col=true, order_num=50)
  Date readDate = null;

  @XORMProperty(name="TimeOutDate", has_col=true, order_num=70)
  Date timeOutDate = null;

  @XORMProperty(name="Title", max_len=100, has_col=true, auto_truncate=true, nullable=false, order_num=60)
  String title = null;

  @XORMProperty(name="Cont", max_len=1000, read_on_demand=true, auto_truncate=true, has_col=true, order_num=70)
  String cont = null;

  @XORMProperty(name="State", has_col=true, order_num=30)
  private int get_State()
  {
    if (this.state == null)
      return State.NewUnknow.getValue();
    return this.state.getValue();
  }

  private void set_State(int st) {
    this.state = State.valueOf(st);
  }

  public NotifyItem()
  {
  }

  public NotifyItem(String soruser, String taruser, String title, String cont, State st)
  {
    if (Convert.isNullOrEmpty(taruser)) {
      throw new IllegalArgumentException("nofity target user cannot null or empty");
    }
    this.sorUserName = soruser;
    this.tarUserName = taruser;

    this.sentDate = new Date();

    if (Convert.isNullOrEmpty(title)) {
      throw new IllegalArgumentException("nofity title cannot null or empty");
    }
    this.title = title;

    this.cont = cont;
    this.state = st;
  }

  public long getAutoId()
  {
    return this.autoId;
  }

  public String getSorUserName()
  {
    return this.sorUserName;
  }

  public String getTarUserName()
  {
    return this.tarUserName;
  }

  public Date getSentDate()
  {
    return this.sentDate;
  }

  public Date getTimeOutDate()
  {
    return this.timeOutDate;
  }

  public void setTimeOutDate(Date d)
  {
    this.timeOutDate = d;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getCont()
  {
    return this.cont;
  }

  public State getState()
  {
    return this.state;
  }

  public static enum State
  {
    NewUnknow(0), 
    NewKeep(1), 
    NewNotKeep(2), 

    ReadKeep(10), 
    ReadNotKeep(11);

    private final int val;

    private State(int v)
    {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public static State[] getNewStates()
    {
      return new State[] { NewUnknow, NewKeep, NewNotKeep };
    }

    public static State[] getReadStates()
    {
      return new State[] { ReadKeep, ReadNotKeep };
    }

    public static State valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return NewUnknow;
      case 1:
        return NewKeep;
      case 2:
        return NewNotKeep;
      case 10:
        return ReadKeep;
      case 11:
        return ReadNotKeep;
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
      case 9: } return NewUnknow;
    }
  }
}
