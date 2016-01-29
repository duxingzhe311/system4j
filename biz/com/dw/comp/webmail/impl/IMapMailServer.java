package com.dw.comp.webmail.impl;

import com.dw.comp.mail.WBMailManager;
import com.dw.comp.mail.config.FolderMap;
import com.dw.comp.mail.config.PostOffice;
import com.dw.comp.mail.model.WBMailSession;
import com.dw.comp.mail.model.WBMailStore;
import com.dw.comp.webmail.AbstractMailServer;
import com.dw.comp.webmail.WebMailItem;
import com.dw.comp.webmail.WebMailItem.State;
import com.dw.system.Convert;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;

public class IMapMailServer extends AbstractMailServer
{
  static int MAIL_MAX_SIZE = 104857600;

  static ILogger log = LoggerManager.getLogger(IMapMailServer.class);
  transient PostOffice postOffice = null;
  transient FolderMap fm = new FolderMap();

  public String getMailServerHost()
  {
    return "192.168.0.4";
  }

  public void sendMail(String usern, WebMailItem wmi) throws Exception
  {
    StringBuilder sb = new StringBuilder();
    WBMailSession s = WBMailManager.getInstance().getUserMailSession(usern, sb);
    if (s == null) {
      throw new Exception("get user mail session error:" + sb.toString());
    }
    s.sendMessage(wmi.toMimeMsg());
  }

  public void sendSystemMail(WebMailItem wmi) throws Exception
  {
    WBMailSession s = WBMailManager.getInstance().getSystemMailSession();
    if (s == null) {
      throw new Exception("no session found!");
    }
    s.sendMessage(wmi.toMimeMsg());
  }

  public void delMailFromServerById(String username, HashSet<String> msgids) throws Exception
  {
    if ((msgids == null) || (msgids.size() <= 0)) {
      return;
    }
    Folder f = null;
    StringBuilder failedreson = new StringBuilder();
    try
    {
      WBMailStore wbms = null;
      try
      {
        wbms = WBMailManager.getInstance().getUserMailStore(username, failedreson);
        if (wbms == null)
          return;
      }
      catch (Exception localException2)
      {
      }
      WBMailStore wbms;
      f = wbms.getJavaFolder("INBOX");

      if (!f.isOpen())
      {
        f.open(2);
      }

      Message[] ms = f.getMessages();
      FetchProfile fp = new FetchProfile();

      fp.add(FetchProfile.Item.ENVELOPE);
      fp.add(FetchProfile.Item.FLAGS);

      f.fetch(ms, fp);

      if ((ms == null) || (ms.length <= 0)) {
        return;
      }
      for (Message m : ms)
      {
        FetchProfile fp;
        Message[] ms;
        WBMailStore wbms;
        MimeMessage mm = (MimeMessage)m;
        try
        {
          String mid = mm.getMessageID();

          if (!Convert.isNullOrEmpty(mid))
          {
            if (msgids.contains(mid))
            {
              if (log.isDebugEnabled())
              {
                log.debug("to be del mail id===" + mid);
              }
              m.setFlag(Flags.Flag.DELETED, true);
            }
          }
        }
        catch (Exception ee) {
          if (log.isDebugEnabled()) {
            log.error(ee);
          }
        }
      }
      f.close(true);
      f = null;
    }
    finally
    {
      try
      {
        if ((f != null) && (f.isOpen()))
          f.close(true);
      }
      catch (Exception localException4)
      {
      }
    }
    try
    {
      if ((f != null) && (f.isOpen()))
        f.close(true);
    }
    catch (Exception localException5)
    {
    }
  }

  public int recvNewMailDo(String username, Date startd, Date endd, boolean is_sent_box, StringBuilder failedreson)
    throws Exception
  {
    WebMailItem.State st = null;
    if (is_sent_box) {
      st = WebMailItem.State.sent;
    }
    Folder f = null;
    try
    {
      WBMailStore wbms = null;
      try
      {
        wbms = WBMailManager.getInstance().getUserMailStore(username, failedreson);
        if (wbms == null)
        {
          return -1;
        }
      }
      catch (Exception localException2)
      {
      }
      WBMailStore wbms;
      if (is_sent_box)
      {
        f = wbms.getSentMailFolder();
        if (f == null)
          return -1;
      }
      else
      {
        WBMailStore wbms;
        f = wbms.getJavaFolder("INBOX");
      }

      if (!f.isOpen())
      {
        f.open(1);
      }

      long st0 = System.currentTimeMillis();

      Message[] ms = f.getMessages();

      FetchProfile fp = new FetchProfile();

      fp.add(FetchProfile.Item.ENVELOPE);
      fp.add(FetchProfile.Item.FLAGS);

      f.fetch(ms, fp);
      long et0 = System.currentTimeMillis();

      if (ms == null) {
        return 0;
      }

      int c = 0;
      int error_c = 0;
      long lst = System.currentTimeMillis();
      long et0;
      Message[] ms;
      long st0;
      if (log.isDebugEnabled())
      {
        FetchProfile fp;
        WBMailStore wbms;
        log.debug(" recv msg inbox user=[" + username + "] num=" + ms.length);
      }
      int recv_count = 0;
      for (Message m : ms)
      {
        MimeMessage mm = (MimeMessage)m;

        Date rd = mm.getReceivedDate();

        int size = m.getSize();

        if (size > MAIL_MAX_SIZE)
        {
          if (log.isDebugEnabled()) {
            log.debug("too big>>>>>>>" + mm.getSubject() + " recv data=" + Convert.toFullYMDHMS(rd) + " size=" + size + " msguid=" + mm.getMessageID());
          }
        }
        else
        {
          try
          {
            if (log.isDebugEnabled())
            {
              if ("alice.ou".equals(username))
              {
                String tmpss = mm.getSubject();
                Date tmpddd = Convert.toCalendar("2008-07-15").getTime();

                if ((rd != null) && (rd.getTime() > tmpddd.getTime())) {
                  log.debug(">>>>>>>" + tmpss + " recved date==" + Convert.toFullYMDHMS(rd));
                }

              }

            }

            if ((startd != null) && (startd.getTime() > rd.getTime()))
            {
              c++;
            }
            else if ((endd != null) && (endd.getTime() < rd.getTime()))
            {
              c++;
            }
            else
            {
              if (!f.isOpen())
              {
                f.open(1);
              }

              mm.getMessageID();

              if (onRecvedNewMail(username, mm.getMessageID(), mm, st))
              {
                recv_count++;
              }

              c++;
            }
          }
          catch (Exception ee) {
            error_c++;
            if (log.isWarnEnabled()) {
              log.warn("recv user=" + username + "'s mail error:", ee);
            }
          }

        }

      }

      long lcost = System.currentTimeMillis() - lst;
      if (log.isDebugEnabled())
      {
        log.debug(">>>>>>>>>>>>>>>>>>total msgnum =[" + ms.length + "] recved count=[" + c + "] recved error=[" + error_c + "]head cost=" + (et0 - st0) + " loop msg cost=" + lcost);
      }

      return recv_count;
    }
    finally
    {
      try
      {
        if ((f != null) && (f.isOpen()))
          f.close(false);
      }
      catch (Exception localException6)
      {
      }
    }
  }
}
