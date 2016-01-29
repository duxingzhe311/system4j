package com.dw.ext.timelimit;

import com.dw.system.gdb.xorm.XORMProperty;
import java.util.ArrayList;
import java.util.Date;

public class TLInAppItem
{

  @XORMProperty(name="AppName", has_col=true)
  String appName = null;

  @XORMProperty(name="AppId", has_col=true)
  String appId = null;

  @XORMProperty(name="RecentLD", has_col=true)
  Date recentLimitDate = null;

  @XORMProperty(name="TLItemNum", has_col=true)
  int tlItemNum = -1;

  String appTitle = null;

  public TLInAppItem()
  {
  }

  public TLInAppItem(String appname, String appid, ArrayList<TLItem> tlis) {
  }

  public String getAppName() {
    return this.appName;
  }

  public String getAppId()
  {
    return this.appId;
  }

  public String getAppTitle()
  {
    return this.appTitle;
  }

  public Date getRecentLimitDate()
  {
    return this.recentLimitDate;
  }

  public int getTLItemNum()
  {
    return this.tlItemNum;
  }
}
