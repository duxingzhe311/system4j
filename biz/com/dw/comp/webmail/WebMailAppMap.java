package com.dw.comp.webmail;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;

@XORMClass(table_name="webmail_appmap")
public class WebMailAppMap
{

  @XORMProperty(name="MapId", has_col=true, is_pk=true, is_auto=true)
  long mapId = -1L;

  @XORMProperty(name="MailId", has_col=true, has_fk=true, fk_table="webmail", fk_column="MailId")
  long mailId = -1L;

  @XORMProperty(name="MailApp", has_col=true, order_num=145, max_len=20)
  String mailApp = null;

  @XORMProperty(name="MailAppRef", has_col=true, order_num=150, max_len=50)
  String mailAppRef = null;

  public WebMailAppMap(long mid, String mailapp, String mailappref)
  {
    this.mailId = mid;
    this.mailApp = mailapp;
    this.mailAppRef = mailappref;
  }

  public WebMailAppMap()
  {
  }

  public long getMailId()
  {
    return this.mailId;
  }

  public long getMapId() {
    return this.mapId;
  }
}
