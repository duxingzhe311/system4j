package com.dw.comp.webmail;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import java.util.Date;

@XORMClass(table_name="webmail_userlog")
public class WebMailUserLog
{

  @XORMProperty(name="AutoId", has_col=true, is_pk=true, is_auto=true)
  long autoId = -1L;

  @XORMProperty(name="UserName", has_col=true, has_idx=true, is_unique_idx=true, max_len=50, order_num=20)
  String userName = null;

  @XORMProperty(name="LastRecvDate", has_col=true, order_num=30)
  Date lastRecvDate = null;

  public WebMailUserLog()
  {
  }

  public WebMailUserLog(String username, Date last_rd)
  {
    this.userName = username;
    this.lastRecvDate = last_rd;
  }

  public long getAutoId()
  {
    return this.autoId;
  }

  public String getUserName()
  {
    return this.userName;
  }

  public Date getLastRecvDate()
  {
    return this.lastRecvDate;
  }
}
