package com.dw.ext.notify;

import com.dw.system.gdb.GDB;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class NotifyManager
{
  private static NotifyManager ins = null;

  private static Object locker = new Object();

  public static NotifyManager getInstance()
  {
    if (ins != null) {
      return ins;
    }
    synchronized (locker)
    {
      if (ins != null) {
        return ins;
      }
      ins = new NotifyManager();
      return ins;
    }
  }

  public NotifyItem sendNotify(String soruser, String taruser, String title, String cont, NotifyItem.State st)
    throws Exception
  {
    NotifyItem ni = new NotifyItem(soruser, taruser, title, cont, st);
    GDB.getInstance().addXORMObjWithNewId(ni);
    return ni;
  }

  public NotifyItem sendNotify(String soruser, String taruser, String title, String cont, NotifyItem.State st, Date timeout)
    throws Exception
  {
    NotifyItem ni = new NotifyItem(soruser, taruser, title, cont, st);
    ni.setTimeOutDate(timeout);
    GDB.getInstance().addXORMObjWithNewId(ni);
    return ni;
  }

  public List<NotifyItem> listNotifyByTarUser(String taruser, NotifyItem.State[] sts, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@TarUserName", taruser);
    if ((sts == null) || (sts.length <= 0)) {
      throw new IllegalArgumentException("no state info input!");
    }
    StringBuilder sb = new StringBuilder();
    sb.append(sts[0].getValue());
    for (int i = 1; i < sts.length; i++)
    {
      sb.append(',').append(sts[i].getValue());
    }
    ht.put("@CurDT", new Date());
    ht.put("$States", sb.toString());
    return GDB.getInstance().accessDBPageAsXORMObjList(
      "Notify.ListNotifyItem", ht, 
      NotifyItem.class, 
      pageidx, pagesize);
  }

  public NotifyItem getNotifyItemById(long notifyid, String taruser)
    throws Exception
  {
    NotifyItem ni = (NotifyItem)GDB.getInstance().getXORMObjByPkId(NotifyItem.class, Long.valueOf(notifyid));
    if (ni.getTarUserName().equals(taruser)) {
      return ni;
    }
    return null;
  }

  public void updateNotifyItemState(long notifyid, String taruser, NotifyItem.State st)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@NotifyId", Long.valueOf(notifyid));
    ht.put("@TarUserName", taruser);
    ht.put("@State", Integer.valueOf(st.getValue()));

    GDB.getInstance().accessDB("Notify.UpdateNotifyItemState", ht);
  }

  public void setReadNotifyItem(long notifyid, String taruser, NotifyItem.State st)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    ht.put("@NotifyId", Long.valueOf(notifyid));
    ht.put("@TarUserName", taruser);
    ht.put("@State", Integer.valueOf(st.getValue()));
    ht.put("@ReadDate", new Date());

    GDB.getInstance().accessDB("Notify.SetReadNotifyItemState", ht);
  }
}
