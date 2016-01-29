package com.dw.comp.webmail.impl;

import com.dw.comp.mail.WBMailManager;
import com.dw.comp.mail.model.WBMailSession;
import com.dw.comp.mail.model.WBMailStore;
import com.dw.comp.webmail.AbstractMailServer;
import com.dw.comp.webmail.WebMailItem;
import com.sun.mail.pop3.POP3Folder;
import java.util.Date;
import java.util.HashSet;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

public class Pop3MailServer extends AbstractMailServer
{
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

  public void delMailFromServerById(String username, HashSet<String> msgids)
  {
  }

  public int recvNewMailDo(String username, Date startd, Date endd, boolean is_sent, StringBuilder failedreson)
    throws Exception
  {
    POP3Folder f = null;
    try
    {
      WBMailStore wbms = WBMailManager.getInstance().getUserMailStore(username, failedreson);
      if (wbms == null)
      {
        return -1;
      }

      long st0 = System.currentTimeMillis();

      f = (POP3Folder)wbms.getJavaFolder("INBOX");

      if (!f.isOpen())
      {
        WBMailStore wbms;
        f.open(1);
      }

      FlagTerm fts = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
      Message[] ms = f.search(fts);
      if (ms == null) {
        return 0;
      }
      f.close(true);

      f.open(2);
      for (Message m : ms)
      {
        Message[] ms;
        FlagTerm fts;
        long st0;
        WBMailStore wbms;
        FetchProfile fp = new FetchProfile();

        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(FetchProfile.Item.CONTENT_INFO);
        Message[] ms0 = { m };
        f.fetch(ms0, fp);

        Message msg = ms0[0];
        String uid = f.getUID(msg);

        onRecvedNewMail(username, uid, (MimeMessage)msg, null);

        if (uid == null) {
          msg.setFlag(Flags.Flag.DELETED, true);
        }
      }
      return 0;
    }
    finally
    {
      if (f.isOpen())
        f.close(true);
    }
  }
}
