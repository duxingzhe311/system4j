package com.dw.comp.webmail;

import com.dw.system.gdb.GDB;
import java.util.HashMap;

public class WebMailUserProfileManager
{
  private static WebMailUserProfileManager wmupMgr = null;

  private static Object locker = new Object();

  HashMap<String, WebMailUserProfile> name2up = new HashMap();

  public static WebMailUserProfileManager getInstance()
  {
    if (wmupMgr != null) {
      return wmupMgr;
    }
    synchronized (locker)
    {
      if (wmupMgr != null) {
        return wmupMgr;
      }
      wmupMgr = new WebMailUserProfileManager();
      return wmupMgr;
    }
  }

  public WebMailUserProfile getMailUserProfile(String username)
    throws Exception
  {
    WebMailUserProfile wmup = (WebMailUserProfile)this.name2up.get(username);
    if (wmup != null) {
      return wmup;
    }
    wmup = (WebMailUserProfile)GDB.getInstance().getXORMObjByUniqueColValue(WebMailUserProfile.class, "UserName", username, false);
    if (wmup != null) {
      this.name2up.put(username, wmup);
    }
    return wmup;
  }

  public String getMailSign(String username) throws Exception
  {
    WebMailUserProfile wmup = getMailUserProfile(username);
    if (wmup == null) {
      return null;
    }
    return wmup.getMailSign();
  }

  public void setMailUserSign(String username, String sign)
    throws Exception
  {
    WebMailUserProfile wmup = getMailUserProfile(username);
    if (wmup == null)
    {
      wmup = new WebMailUserProfile(username, sign, false, false);

      GDB.getInstance().addXORMObjWithNewId(wmup);

      this.name2up.put(username, wmup);

      return;
    }

    wmup.mailSign = sign;
    GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(wmup.getAutoId()), WebMailUserProfile.class, 
      new String[] { "MailSign" }, new Object[] { sign });
  }
}
