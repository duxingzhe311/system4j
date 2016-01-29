package com.dw.comp.webmail.inner;

import com.dw.comp.webmail.WebMailItem;
import com.dw.comp.webmail.WebMailItem.Level;
import com.dw.comp.webmail.WebMailItem.State;
import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.web_ui.WebRes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;

public class JamesInboxMailItem
{

  @XORMProperty(name="mail_id", has_col=true)
  long mailId = -1L;

  @XORMProperty(name="message_name", has_col=true)
  String msgId = null;

  @XORMProperty(name="repository_name", has_col=true)
  String userName = null;

  @XORMProperty(name="message_state", has_col=true)
  String msgState = null;

  @XORMProperty(name="error_message", has_col=true)
  String errorMsg = null;

  @XORMProperty(name="sender", has_col=true)
  String sender = null;

  @XORMProperty(name="recipients", has_col=true)
  String recipients = null;

  @XORMProperty(name="remote_host", has_col=true)
  String remoteHost = null;

  @XORMProperty(name="remote_addr", has_col=true)
  String remoteAddr = null;

  @XORMProperty(name="message_body", has_col=true)
  byte[] messageBody = null;

  @XORMProperty(name="message_attributes", has_col=true)
  byte[] messageAttributes = null;

  @XORMProperty(name="last_updated", has_col=true)
  Date lastUpdated = null;

  @XORMProperty(name="state", has_col=true)
  int state = 0;

  @XORMProperty(name="title", has_col=true)
  String title = null;

  @XORMProperty(name="mail_size", has_col=true)
  int mailSize = -1;

  @XORMProperty(name="mail_attach_num", has_col=true)
  int mailAttachNum = -1;

  @XORMProperty(name="mail_level", has_col=true)
  int mailLvl = 1;

  public long getMailId()
  {
    return this.mailId;
  }

  public String getMsgId()
  {
    return this.msgId;
  }

  public String getUserName()
  {
    return this.userName;
  }

  public String getMsgBodyFileName()
  {
    if (Convert.isNullOrEmpty(this.msgId)) {
      return null;
    }
    return Convert.encodeKeyToFileName(this.msgId) + ".Repository.FileStreamStore";
  }

  public String getMsgState()
  {
    return this.msgState;
  }

  public String getErrorMsg()
  {
    return this.errorMsg;
  }

  public String getSender()
  {
    return this.sender;
  }

  public String getRecipients()
  {
    return this.recipients;
  }

  public String getRemoteHost()
  {
    return this.remoteHost;
  }

  public String getRemoteAddr()
  {
    return this.remoteAddr;
  }

  public byte[] getMsgHead()
  {
    return this.messageBody;
  }

  public byte[] getMsgArributes()
  {
    return this.messageAttributes;
  }

  public Date getLastUpdated()
  {
    return this.lastUpdated;
  }

  public int getState()
  {
    return this.state;
  }

  public String getTitle()
  {
    return this.title;
  }

  public int getMailSize()
  {
    return this.mailSize;
  }

  public int getAttachNum()
  {
    return this.mailAttachNum;
  }

  public int getMailLevel()
  {
    return this.mailLvl;
  }

  public WebMailItem toWebMailItem() throws Exception
  {
    return toWebMailItem(false);
  }

  public static File transMsgId2File(String msgid)
  {
    String msgbodyfp = AppConfig.getDataDirBase() + 
      "inner_mail/var/dbmail/" + 
      Convert.encodeKeyToFileName(msgid) + 
      ".Repository.FileStreamStore";
    return new File(msgbodyfp);
  }

  public WebMailItem toWebMailItem(boolean bdetail) throws Exception
  {
    WebMailItem.State st = WebMailItem.State.recved_seen;
    if (this.state == 0) {
      st = WebMailItem.State.recved_new;
    }
    WebMailItem.Level lvl = WebMailItem.Level.valueOf(this.mailLvl);
    WebMailItem wmi = new WebMailItem(this.userName, this.mailId, this.msgId, this.sender, this.recipients, this.title, this.lastUpdated, 
      st, this.mailSize, this.mailAttachNum, lvl, this.messageBody);

    wmi.setIsInnerMailInbox(true);
    if (!bdetail) {
      return wmi;
    }
    if (this.messageBody == null) {
      return wmi;
    }

    File f = transMsgId2File(this.msgId);

    if (!f.exists()) {
      return wmi;
    }
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(f);
      JamesHeadBodyInputStream hbin = new JamesHeadBodyInputStream(this.messageBody, fis);
      wmi.fromMimeMsgInputStream(hbin);
    }
    finally
    {
      if (fis != null)
        fis.close();
    }
    return wmi;
  }

  public void renderEmlFileOut(HttpServletResponse resp)
    throws IOException
  {
    File f = transMsgId2File(this.msgId);

    if (!f.exists()) {
      return;
    }
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(f);
      JamesHeadBodyInputStream hbin = new JamesHeadBodyInputStream(this.messageBody, fis);
      if (Convert.isNullOrTrimEmpty(this.title))
        this.title = ("download_" + this.msgId);
      WebRes.renderFile(resp, this.title + ".eml", hbin);
    }
    finally
    {
      if (fis != null)
        fis.close();
    }
  }
}
