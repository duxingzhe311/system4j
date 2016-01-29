package com.dw.comp.webmail;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import java.util.Date;

@XORMClass(table_name="webmail_addr")
public class WebMailAddrItem
{

  @XORMProperty(name="AutoId", has_col=true, is_pk=true, is_auto=true)
  long autoId = -1L;

  @XORMProperty(name="MailPerson", has_col=true, has_idx=true, default_str_val="''", max_len=50, auto_truncate=true, order_num=10)
  String mailPerson = null;

  @XORMProperty(name="MailUserName", has_col=true, has_idx=true, max_len=50, order_num=20)
  String mailUserName = null;

  @XORMProperty(name="MailDomain", has_col=true, has_idx=true, max_len=50, order_num=20)
  String mailDomain = null;

  @XORMProperty(name="OccurNum", has_col=true, order_num=30)
  int occurNum = 0;

  @XORMProperty(name="LastOccurDate", has_col=true, order_num=35)
  Date lastOccurDate = null;

  @XORMProperty(name="IsGarbage", has_col=true, order_num=40)
  boolean isGarbage = false;

  public static WebMailAddrItem createByAddr(WebMailAddr wma)
  {
    WebMailAddrItem ret = new WebMailAddrItem();
    ret.mailPerson = wma.getName();
    if (ret.mailPerson == null) {
      ret.mailPerson = "";
    }
    String em = wma.getEmail();
    int p = em.indexOf('@');
    if (p <= 0) {
      return null;
    }
    ret.mailUserName = em.substring(0, p);
    ret.mailDomain = em.substring(p + 1).toLowerCase();
    ret.occurNum = 1;
    ret.lastOccurDate = new Date();
    return ret;
  }

  public long getAutoId()
  {
    return this.autoId;
  }

  public String getMailPerson()
  {
    if (this.mailPerson == null)
      return "";
    return this.mailPerson;
  }

  public String getMailUserName()
  {
    return this.mailUserName;
  }

  public String getMailDomain()
  {
    return this.mailDomain;
  }

  public int getOccurNum()
  {
    return this.occurNum;
  }

  public Date getLastOccurDate()
  {
    return this.lastOccurDate;
  }

  public boolean isGarbage()
  {
    return this.isGarbage;
  }

  public WebMailAddr toWebMailAddr()
  {
    return new WebMailAddr(this.mailPerson, this.mailUserName + "@" + this.mailDomain);
  }
}
