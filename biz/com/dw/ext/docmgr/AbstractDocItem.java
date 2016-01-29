package com.dw.ext.docmgr;

import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMProperty;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AbstractDocItem
{

  @XORMProperty(name="DocId", has_col=true, is_pk=true, is_auto=true)
  long docId = -1L;

  @XORMProperty(name="DocFileName", max_len=100, has_col=true, has_idx=true, nullable=false)
  String docFileName = null;

  @XORMProperty(name="CreationTime", has_col=true, has_idx=true)
  Date creationTime = new Date();

  State state = State.Normal;

  @XORMProperty(name="LockUserName", max_len=50, has_col=true, has_idx=true)
  String lockUserName = null;

  transient AbstractDocFileItem lastDocFileItem = null;

  transient AbstractDocManager docMgr = null;

  List<AbstractDocFileItem> fileItems = null;

  @XORMProperty(name="State", has_col=true, has_idx=true)
  int get_State()
  {
    return this.state.getValue();
  }

  void set_State(int v)
  {
    this.state = State.valueOf(v);
  }

  public AbstractDocItem()
  {
  }

  public AbstractDocItem(String filename)
  {
    this.docFileName = filename;
  }

  public long getDocId()
  {
    return this.docId;
  }

  public String getDocFileName()
  {
    return this.docFileName;
  }

  public void setDocFileName(String dfn)
  {
    if (Convert.isNullOrEmpty(dfn)) {
      throw new IllegalArgumentException("doc file name cannot be null or empty!");
    }
    this.docFileName = dfn;
  }

  public State getState()
  {
    return this.state;
  }

  public void setState(State s)
  {
    this.state = s;
  }

  public String getStateInfo()
  {
    if (this.state == State.Normal)
      return "";
    if (this.state == State.Locked)
      return "�? + this.lockUserName + "锁定";
    if (this.state == State.Freezed)
      return "冻结";
    if (this.state == State.Delete) {
      return "被删�?;
    }
    return "";
  }

  public String getLockUserName()
  {
    return this.lockUserName;
  }

  public AbstractDocFileItem getLastDocFileItem()
  {
    return this.lastDocFileItem;
  }

  public String getLastFileExt()
  {
    if (this.lastDocFileItem == null)
      return "";
    return this.lastDocFileItem.getFileExt();
  }

  public long getLastFileLen()
  {
    if (this.lastDocFileItem == null)
      return -1L;
    return this.lastDocFileItem.getFileLen();
  }

  public String getLastFileLenStr()
  {
    if (this.lastDocFileItem == null) {
      return "";
    }
    long l = this.lastDocFileItem.getFileLen();
    if (l < 1024L) {
      return l;
    }
    return l / 1024L + (l % 1024L > 0L ? 1 : 0) + "K";
  }

  public String getLastFileVersion()
  {
    if (this.lastDocFileItem == null) {
      return "";
    }
    return this.lastDocFileItem.getVersion();
  }

  public String getLastFileUserName()
  {
    if (this.lastDocFileItem == null) {
      return "";
    }
    return this.lastDocFileItem.getUserName();
  }

  public int getLastFileUserVer()
  {
    if (this.lastDocFileItem == null)
      return -1;
    DocVer dv = this.lastDocFileItem.getVer();
    return dv.getUserVer();
  }

  public String getLastFileShowVer()
  {
    if (this.lastDocFileItem == null) {
      return "";
    }
    return DocVer.transToShowStr(this.lastDocFileItem.getVersion());
  }

  public String getLastFileModifyDateDesc()
  {
    if (this.lastDocFileItem == null) {
      return "";
    }
    return Convert.toYMDHM(this.lastDocFileItem.getModifyDate());
  }

  public String getLastFileModifyDateStrShort()
  {
    if (this.lastDocFileItem == null) {
      return "";
    }
    return Convert.toShortYMD(this.lastDocFileItem.getModifyDate());
  }

  public List<AbstractDocFileItem> getFileItems()
    throws Exception
  {
    if (this.fileItems != null) {
      return this.fileItems;
    }
    synchronized (this)
    {
      this.fileItems = this.docMgr.listDocFileItem(this.docId);
      return this.fileItems;
    }
  }

  public AbstractDocFileItem getExistedVerFile(Date dt, long filelen)
    throws Exception
  {
    for (AbstractDocFileItem dfi : getFileItems())
    {
      if (dfi.getFileLen() == filelen)
      {
        Calendar dfc = Calendar.getInstance();
        dfc.setTime(dfi.getModifyDate());
        Calendar dtc = Calendar.getInstance();
        dtc.setTime(dt);

        if (dfc.get(1) == dtc.get(1))
        {
          if (dfc.get(2) == dtc.get(2))
          {
            if (dfc.get(5) == dtc.get(5))
            {
              if (dfc.get(11) == dtc.get(11))
              {
                if (dfc.get(12) == dtc.get(12))
                {
                  if (dfc.get(13) == dtc.get(13))
                  {
                    return dfi;
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  public HashMap<String, String> toJspTagMap()
  {
    return null;
  }

  public static enum State
  {
    Delete(-1), 
    Normal(0), 
    Locked(1), 
    Freezed(2), 

    NewMsg(101), 
    OldMsg(102), 
    ImpMsg(103);

    private final int val;

    private State(int v)
    {
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
      case -1:
        return Delete;
      case 0:
        return Normal;
      case 1:
        return Locked;
      case 2:
        return Freezed;
      }
      throw new IllegalArgumentException("unknow state value=" + v);
    }
  }
}
