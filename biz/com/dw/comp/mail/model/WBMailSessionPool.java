package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailManager;
import com.dw.comp.mail.config.FolderMap;
import com.dw.comp.mail.config.MailTransportAgent;
import com.dw.comp.mail.config.PostOffice;
import com.dw.comp.mail.config.WBMailConfiguration;
import com.dw.system.Convert;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.user.sso.ModuleUserAccount;
import com.dw.user.sso.SSOManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class WBMailSessionPool
{
  public static final int MAX_ACCESS_COUNT = 5;
  private PostOffice postOffice = null;
  private FolderMap folderMap = null;

  static ILogger logger = LoggerManager.getLogger(WBMailSessionPool.class);

  private HashMap<String, Object> user2locker = new HashMap();
  private Hashtable<String, WBMailSession> key2Session = new Hashtable();

  public WBMailSessionPool(PostOffice po, FolderMap fm)
  {
    this.postOffice = po;
    this.folderMap = fm;
  }

  private synchronized Object getUserLocker(String username)
  {
    Object o = this.user2locker.get(username);
    if (o == null)
    {
      o = new Object();
      this.user2locker.put(username, o);
    }
    return o;
  }

  public List<WBMailSession> getAllConnectedSession()
  {
    Object[] objs = this.key2Session.values().toArray();
    ArrayList rets = new ArrayList();
    for (Object o : objs)
    {
      if ((o instanceof WBMailSession))
        rets.add((WBMailSession)o);
    }
    return rets;
  }

  public void closeSessionByUserName(String username)
  {
    Object locker = getUserLocker(username);
    synchronized (locker)
    {
      WBMailSession ms = (WBMailSession)this.key2Session.get(username);
      if (ms != null)
      {
        if (!ms.isClose()) {
          ms.end();
        }
        this.key2Session.remove(username);
      }
    }
  }

  public WBMailSession acquireSession(String username, StringBuilder failedreson)
    throws Exception
  {
    Object locker = getUserLocker(username);
    synchronized (locker)
    {
      WBMailSession ms = (WBMailSession)this.key2Session.get(username);
      if (ms != null)
      {
        if (!ms.isClose())
        {
          if (ms.increaseAccessCount() < 5)
            return ms;
        }
        else
        {
          ms.endMailSession();
          this.key2Session.remove(username);
        }
      }

      SSOManager ssom = SSOManager.getInstance();
      ModuleUserAccount mua = ssom.getModuleUserAccount("mail", username);
      if (mua == null)
      {
        failedreson.append("no module user account info");
        return null;
      }

      try
      {
        ms = new WBMailSession(this.postOffice, this.folderMap, mua.getLoginName(), mua.getPasswordInfo());
        this.key2Session.put(username, ms);
        return ms;
      }
      catch (Exception ee)
      {
        if (logger.isErrorEnabled())
          logger.error(ee);
        failedreson.append("session conn error");
        return null;
      }
    }
  }

  public WBMailSession createSystemSession()
    throws Exception
  {
    MailTransportAgent mta = WBMailManager.getInstance().getConfiguration().getMTA();
    String sa = mta.getSystemAccount();
    if (Convert.isNullOrEmpty(sa)) {
      throw new RuntimeException("cannot find system mail transport account!");
    }
    String psw = mta.getSystemPsw();
    return new WBMailSession(this.postOffice, this.folderMap, sa, psw);
  }
}
