package com.dw.comp.webmail;

import com.dw.comp.mail.WBMailManager;
import com.dw.comp.mail.config.PostOffice;
import com.dw.comp.mail.config.WBMailConfiguration;
import com.dw.comp.webmail.impl.IMapMailServer;
import com.dw.comp.webmail.impl.InnerJamesServer;
import com.dw.comp.webmail.impl.Pop3MailServer;
import com.dw.system.gdb.GdbException;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.user.sso.ModuleUserAccount;
import com.dw.user.sso.SSOManager;
import java.util.Date;
import java.util.HashSet;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;

public abstract class AbstractMailServer
{
  static ILogger log = LoggerManager.getLogger(AbstractMailServer.class);

  static AbstractMailServer instance = null;

  public static AbstractMailServer getInstance()
  {
    if (instance != null) {
      return instance;
    }
    try
    {
      if (WebMailManager.isInnerMailServer())
      {
        instance = new InnerJamesServer();
        return instance;
      }

      WBMailConfiguration wmc = WBMailManager.getInstance().getConfiguration();
      PostOffice po = wmc.getDefaultPostOffice();
      if (po != null)
      {
        if ("imap".equalsIgnoreCase(po.getProtocol()))
        {
          instance = new IMapMailServer();
        }
        else if ("pop3".equalsIgnoreCase(po.getProtocol()))
        {
          instance = new Pop3MailServer();
        }
      }

      return instance;
    }
    catch (Exception ee)
    {
      log.error(ee);
    }return null;
  }

  public ModuleUserAccount getMailAccountInfo(String username)
    throws Exception
  {
    return SSOManager.getInstance().getModuleUserAccount(
      "mail", username);
  }

  public abstract String getMailServerHost();

  public abstract void sendMail(String paramString, WebMailItem paramWebMailItem)
    throws Exception;

  public abstract void sendSystemMail(WebMailItem paramWebMailItem)
    throws Exception;

  int recvNewMail(String username, Date startd, Date endd, boolean is_sentbox, StringBuilder failedreson)
    throws Exception
  {
    return recvNewMailDo(username, startd, endd, is_sentbox, failedreson);
  }

  protected abstract int recvNewMailDo(String paramString, Date paramDate1, Date paramDate2, boolean paramBoolean, StringBuilder paramStringBuilder)
    throws Exception;

  public abstract void delMailFromServerById(String paramString, HashSet<String> paramHashSet)
    throws Exception;

  protected boolean onRecvedNewMail(String username, String uid, MimeMessage m, WebMailItem.State st)
    throws GdbException, Exception
  {
    WebMailManager wmm = WebMailManager.getInstance();

    if (wmm.checkMailExistedByMsgUid(username, uid)) {
      return false;
    }

    MimeMessage msg = new MimeMessage(m);
    WebMailItem wmi = new WebMailItem(username, uid);
    wmi.fromMimeMsg(msg);

    if (st != null)
    {
      wmi.setMailState(st);
    }
    else
    {
      wmi.setMailState(WebMailItem.State.recved_new);

      Flags fs = msg.getFlags();
      if (fs != null)
      {
        if (fs.contains(Flags.Flag.DELETED))
          wmi.setMailState(WebMailItem.State.delete);
        else if (fs.contains(Flags.Flag.RECENT))
          wmi.setMailState(WebMailItem.State.recved_new);
        else if (fs.contains(Flags.Flag.SEEN)) {
          wmi.setMailState(WebMailItem.State.recved_seen);
        }
      }
    }
    wmm.saveMail(wmi);
    return true;
  }

  public static class RecvInfo
  {
    int totalMsgCount = -1;

    int recvedMsgCount = -1;
  }
}
