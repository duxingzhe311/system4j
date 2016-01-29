package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailException;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.sun.mail.imap.IMAPMessage;
import java.util.Date;
import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

public class WBMailMsgInfo
{
  private static ILogger log = LoggerManager.getLogger(WBMailMsgInfo.class);

  private String m_msgId = null;
  private Integer m_Number;
  private boolean m_Read;
  private boolean m_Answered;
  private boolean m_Recent;
  private boolean m_Deleted;
  private boolean m_Draft;
  private boolean m_Received;
  private boolean m_Singlepart;
  private Date m_ReceivedDate;
  private Date m_SentDate;
  private String m_From;
  private String m_To;
  private String m_Subject;
  private int m_Size;
  private transient Message javaMsg = null;

  public static WBMailMsgInfo createMsgInfo(Message msg)
    throws WBMailException
  {
    WBMailMsgInfo messageinfo = null;
    try
    {
      messageinfo = new WBMailMsgInfo(msg, msg.getMessageNumber());

      if ((msg instanceof IMAPMessage))
      {
        messageinfo.m_msgId = ((IMAPMessage)msg).getMessageID();
      }
      else
      {
        messageinfo.m_msgId = null;
      }

      messageinfo.setNew(msg.isSet(Flags.Flag.RECENT));
      messageinfo.setRead(msg.isSet(Flags.Flag.SEEN));
      messageinfo.setAnswered(msg.isSet(Flags.Flag.ANSWERED));
      messageinfo.setDeleted(msg.isSet(Flags.Flag.DELETED));
      messageinfo.setDraft(msg.isSet(Flags.Flag.DRAFT));

      messageinfo.setReceived(msg.getHeader("Received") != null);

      Address[] fradds = msg.getFrom();
      if ((fradds != null) && (fradds.length > 0)) {
        messageinfo.setFrom(prepareString(InternetAddress.toString(fradds)));
      }
      Address[] rec_adds = msg.getRecipients(Message.RecipientType.TO);
      if ((rec_adds != null) && (rec_adds.length > 0)) {
        messageinfo.setTo(prepareString(InternetAddress.toString(rec_adds)));
      }

      messageinfo.setReceivedDate(msg.getReceivedDate());
      messageinfo.setSentDate(msg.getSentDate());

      String sub = msg.getSubject();

      messageinfo.setSubject(prepareString(sub));

      messageinfo.setSize(msg.getSize());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      throw new WBMailException("jwma.messageinfo.failedcreation");
    }
    return messageinfo;
  }

  private WBMailMsgInfo(Message m, int number)
  {
    this.javaMsg = m;
    setMessageNumber(number);
  }

  private WBMailMsgInfo(Message m, int number, String msgid)
  {
    this.javaMsg = m;
    setMessageNumber(number);
    this.m_msgId = msgid;
  }

  public Message getJavaMessage()
  {
    return this.javaMsg;
  }

  public int getMessageNumber()
  {
    return this.m_Number.intValue();
  }

  public Integer getNumberForSort()
  {
    return this.m_Number;
  }

  public String getMessageId()
  {
    return this.m_msgId;
  }

  public void setMessageNumber(int num)
  {
    this.m_Number = new Integer(num);
  }

  public boolean isRead()
  {
    return this.m_Read;
  }

  private void setRead(boolean b)
  {
    this.m_Read = b;
  }

  public boolean isDraft()
  {
    return this.m_Draft;
  }

  private void setDraft(boolean b)
  {
    this.m_Draft = b;
  }

  public boolean isAnswered()
  {
    return this.m_Answered;
  }

  private void setAnswered(boolean b)
  {
    this.m_Answered = b;
  }

  public boolean isDeleted()
  {
    return this.m_Deleted;
  }

  private void setDeleted(boolean b)
  {
    this.m_Deleted = b;
  }

  public boolean isNew()
  {
    return this.m_Recent;
  }

  private void setNew(boolean b)
  {
    this.m_Recent = b;
  }

  public boolean isReceived()
  {
    return this.m_Received;
  }

  private void setReceived(boolean b)
  {
    this.m_Received = b;
  }

  public boolean isSent()
  {
    return !this.m_Received;
  }

  public Date getDate()
  {
    if ((isReceived()) && (this.m_ReceivedDate != null))
    {
      return this.m_ReceivedDate;
    }
    if ((isSent()) && (this.m_SentDate != null))
    {
      return this.m_SentDate;
    }

    return new Date();
  }

  public Date getReceivedDate()
  {
    return this.m_ReceivedDate;
  }

  private void setReceivedDate(Date d)
  {
    this.m_ReceivedDate = d;
  }

  public Date getSentDate()
  {
    return this.m_SentDate;
  }

  private void setSentDate(Date d)
  {
    this.m_SentDate = d;
  }

  public String getWho()
  {
    if (isReceived())
    {
      return getFrom();
    }

    return "<i>" + getTo() + "</i>";
  }

  public String getFrom()
  {
    return this.m_From;
  }

  private void setFrom(String from)
  {
    this.m_From = from;
  }

  public String getTo()
  {
    return this.m_To;
  }

  private void setTo(String to)
  {
    this.m_To = to;
  }

  public String getSubject()
  {
    return this.m_Subject;
  }

  private void setSubject(String subject)
  {
    if (subject == null)
    {
      this.m_Subject = "";
    }
    else
    {
      this.m_Subject = subject;
    }
  }

  public boolean isSinglepart()
  {
    return this.m_Singlepart;
  }

  public boolean isMultipart()
  {
    return !this.m_Singlepart;
  }

  private void setSinglepart(boolean b)
  {
    this.m_Singlepart = b;
  }

  public int getSize()
  {
    return this.m_Size;
  }

  private void setSize(int bytes)
  {
    this.m_Size = bytes;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("msgnum=").append(getMessageNumber()).append(" msgid=").append(getMessageId()).append(" from=").append(this.m_From).append(" to=")
      .append(this.m_To)
      .append("\n\tsubject=").append(this.m_Subject)
      .append("\n\tsize=").append(getSize() / 1024).append("K")
      .append("\n\t deleted=").append(isDeleted());
    return sb.toString();
  }

  private static String prepareString(String str)
    throws Exception
  {
    if (str == null)
    {
      return "";
    }

    return str;
  }
}
