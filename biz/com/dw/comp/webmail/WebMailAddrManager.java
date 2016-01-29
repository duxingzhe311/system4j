package com.dw.comp.webmail;

import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class WebMailAddrManager
{
  static WebMailAddrManager ins = null;

  static ILogger log = LoggerManager.getLogger(WebMailAddrManager.class);

  private HashSet<String> ignoreDomain = new HashSet();

  public static WebMailAddrManager getInstance()
  {
    if (ins != null) {
      return ins;
    }
    synchronized (log)
    {
      if (ins != null) {
        return ins;
      }
      ins = new WebMailAddrManager();
      return ins;
    }
  }

  public void setupMailByStr(String mailaddrs)
    throws Exception
  {
    if (mailaddrs == null) {
      return;
    }
    mailaddrs = mailaddrs.trim();
    if ("".equals(mailaddrs)) {
      return;
    }
    ArrayList wmas = 
      WebMailItem.transStringTOMailAddrs(mailaddrs);
    if ((wmas == null) || (wmas.size() <= 0)) {
      return;
    }
    for (WebMailAddr wma : wmas)
    {
      WebMailAddrItem wmai = WebMailAddrItem.createByAddr(wma);
      String md = wmai.getMailDomain();
      if (!this.ignoreDomain.contains(md))
      {
        setupMailAddrItem(wmai);
      }
    }
  }

  public void setupMailAddrItem(WebMailAddrItem wmai) throws Exception
  {
    List mais = GDB.getInstance()
      .listXORMAsObjList(
      WebMailAddrItem.class, 
      "MailUserName='" + wmai.getMailUserName() + 
      "' and MailDomain='" + wmai.getMailDomain() + 
      "'", null, 0, -1);

    if ((mais == null) || (mais.size() <= 0))
    {
      GDB.getInstance().addXORMObjWithNewId(wmai);
      return;
    }

    WebMailAddrItem oldm = (WebMailAddrItem)mais.get(0);
    String oldmp = oldm.getMailPerson();
    String newmp = wmai.getMailPerson();
    if ((Convert.isNotNullEmpty(oldmp)) || (Convert.isNullOrEmpty(newmp)))
    {
      GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
        Long.valueOf(oldm.getAutoId()), WebMailAddrItem.class, 
        new String[] { "OccurNum", "LastOccurDate" }, 
        new Object[] { Integer.valueOf(oldm.getOccurNum() + 1), new Date() });
      return;
    }

    GDB.getInstance().updateXORMObjToDBWithHasColNameValues(
      Long.valueOf(oldm.getAutoId()), WebMailAddrItem.class, 
      new String[] { "OccurNum", "MailPerson", "LastOccurDate" }, 
      new Object[] { Integer.valueOf(oldm.getOccurNum() + 1), newmp, new Date() });
  }

  public List<WebMailAddr> searchMailAddr(String searchtxt, int pageidx, int pagesize)
    throws Exception
  {
    Hashtable ht = new Hashtable();
    if (Convert.isNotNullEmpty(searchtxt)) {
      ht.put("$SearchTxt", searchtxt);
    }
    List mais = GDB.getInstance()
      .accessDBPageAsXORMObjList("AppMail.SearchMailAddrItem", ht, 
      WebMailAddrItem.class, pageidx, pagesize);
    if ((mais == null) || (mais.size() <= 0)) {
      return null;
    }
    ArrayList rets = new ArrayList(mais.size());
    for (WebMailAddrItem ai : mais)
      rets.add(ai.toWebMailAddr());
    return rets;
  }
}
