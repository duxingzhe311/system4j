package com.dw.comp.webmail;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;

@XORMClass(table_name="webmail_userprofile")
public class WebMailUserProfile
{

  @XORMProperty(name="AutoId", has_col=true, is_pk=true, is_auto=true)
  long autoId = -1L;

  @XORMProperty(name="UserName", has_col=true, has_idx=true, is_unique_idx=true, max_len=50, order_num=20)
  String userName = null;

  @XORMProperty(name="MailSign", has_col=true, max_len=1000, order_num=40)
  String mailSign = null;

  @XORMProperty(name="MailSignReplayAllow", has_col=true, order_num=60)
  boolean mailSignReplayAllow = false;

  @XORMProperty(name="MailSignForwardAllow", has_col=true, order_num=80)
  boolean mailSignForwardAllow = false;

  public WebMailUserProfile()
  {
  }

  public WebMailUserProfile(String usern, String sign, boolean reply_sign, boolean forward_sign)
  {
    this.userName = usern;
    this.mailSign = sign;
    this.mailSignReplayAllow = reply_sign;
    this.mailSignForwardAllow = forward_sign;
  }

  public long getAutoId()
  {
    return this.autoId;
  }

  public String getUserName()
  {
    return this.userName;
  }

  public String getMailSign()
  {
    return this.mailSign;
  }

  public boolean isSignReplyAllow()
  {
    return this.mailSignReplayAllow;
  }

  public boolean isSignForwardAllow()
  {
    return this.mailSignForwardAllow;
  }
}
