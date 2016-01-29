package com.dw.comp.webmail.inner;

import com.dw.system.Convert;
import com.dw.system.gdb.DBResult;
import com.dw.system.gdb.GDB;
import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class JamesInboxManager
{
  static Object locker = new Object();
  static JamesInboxManager ins = null;

  public static JamesInboxManager getInstance()
  {
    if (ins != null) {
      return ins;
    }
    synchronized (locker)
    {
      if (ins != null) {
        return ins;
      }
      ins = new JamesInboxManager();
      return ins;
    }
  }

  public JamesInboxMailItem getById(int domainid, String usern, long mid)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    if (usern != null)
    {
      if (domainid <= 0)
        ht.put("@UserName", usern);
      else {
        ht.put("@UserName", usern + "@" + domainid);
      }
    }
    ht.put("@MailId", Long.valueOf(mid));
    return (JamesInboxMailItem)GDB.getInstance().accessDBAsXORMObj("AppMail_Inner.GetById", ht, JamesInboxMailItem.class);
  }

  public List<JamesInboxMailItem> listByUser(int domainid, String usern, int[] states, Date startd, Date endd, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    if (domainid <= 0)
      ht.put("@UserName", usern);
    else {
      ht.put("@UserName", usern + "@" + domainid);
    }
    if (startd != null)
      ht.put("@StartD", startd);
    if (endd != null) {
      ht.put("@EndD", endd);
    }
    if ((states != null) && (states.length > 0))
    {
      StringBuilder tmpsb = new StringBuilder();
      tmpsb.append(states[0]);
      for (int k = 1; k < states.length; k++)
      {
        tmpsb.append(',').append(states[k]);
      }
      ht.put("$StateStr", tmpsb.toString());
    }

    return GDB.getInstance().accessDBPageAsXORMObjList("AppMail_Inner.ListByUser", ht, JamesInboxMailItem.class, pageidx, pagesize);
  }

  public void updateStateById(int domain, String username, long mid, int st)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@state", Integer.valueOf(st));
    if (Convert.isNotNullEmpty(username))
    {
      if (domain <= 0)
        ht.put("@UserName", username);
      else
        ht.put("@UserName", username + "@" + domain);
    }
    ht.put("@MailId", Long.valueOf(mid));
    ht.put("@UpDate", new Date());

    GDB.getInstance().accessDB("AppMail_Inner.UpdateSt", ht);
  }

  public void delToTrash(int domain, String username, long mid)
    throws Exception
  {
    updateStateById(domain, username, mid, -1);
  }

  public void recoverDeletedMail(int domain, String username, long[] mids)
    throws Exception
  {
    if ((mids == null) || (mids.length <= 0)) {
      return;
    }
    StringBuilder midstr = new StringBuilder();
    midstr.append(mids[0]);
    for (int i = 1; i < mids.length; i++)
    {
      midstr.append(',').append(mids[i]);
    }

    Hashtable ht = new Hashtable();
    ht.put("@state", Integer.valueOf(1));
    if (Convert.isNotNullEmpty(username))
    {
      if (domain <= 0)
        ht.put("@UserName", username);
      else
        ht.put("@UserName", username + "@" + domain);
    }
    ht.put("$MailIdsStr", midstr.toString());

    GDB.getInstance().accessDB("AppMail_Inner.RecoverSt", ht);
  }

  public void deleteById(int domain, String username, long mid) throws Exception
  {
    JamesInboxMailItem jmi = getById(0, null, mid);
    if (jmi == null) {
      return;
    }
    Hashtable ht = new Hashtable();
    if (Convert.isNotNullEmpty(username))
    {
      if (domain <= 0)
        ht.put("@UserName", username);
      else {
        ht.put("@UserName", username + "@" + domain);
      }
    }
    ht.put("@MailId", Long.valueOf(mid));
    DBResult dbr = GDB.getInstance().accessDB("AppMail_Inner.DelById", ht);
    if (dbr.getLastRowsAffected() == 1)
    {
      File f = JamesInboxMailItem.transMsgId2File(jmi.getMsgId());
      if (f.exists())
        f.delete();
    }
  }
}
