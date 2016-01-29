package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailException;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.web_ui.WebUtil;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class WBMailMsgDisplay
  implements WBMailMsg
{
  private static ILogger log = LoggerManager.getLogger(WBMailMsgDisplay.class);
  private Message m_Message;
  private boolean m_Received;
  private String m_FullHeader;
  private int m_Number;
  private Date m_SentDate;
  private Date m_ReceivedDate;
  private String m_From;
  private String m_ReplyTo;
  private String m_To;
  private String m_Subject;
  private boolean m_Singlepart;
  private String m_CC;
  private String m_BCC;
  private String txtBody = null;

  private String htmlBody = null;

  private HashMap<Integer, String> appendPartNum2FileName = null;

  private static String getAddressesAsString(Address[] addr)
    throws Exception
  {
    if ((addr != null) && (addr.length > 0))
    {
      return MimeUtility.decodeText(InternetAddress.toString(addr));
    }

    return "";
  }

  public static WBMailMsgDisplay createWBMailMsg(Message msg)
    throws WBMailException
  {
    WBMailMsgDisplay message = null;
    try
    {
      message = new WBMailMsgDisplay(msg, msg.getMessageNumber());

      StringBuffer fullhead = new StringBuffer();
      for (Enumeration en = ((MimeMessage)msg).getAllHeaderLines(); en
        .hasMoreElements(); )
      {
        fullhead.append((String)en.nextElement()).append("\n");
      }

      message.setFullHeader(fullhead.toString());

      message.setFrom(getAddressesAsString(msg.getFrom()));
      message.setReplyTo(getAddressesAsString(msg.getReplyTo()));

      message.setTo(getAddressesAsString(
        msg.getRecipients(Message.RecipientType.TO)));
      message.setCCTo(getAddressesAsString(
        msg.getRecipients(Message.RecipientType.CC)));
      message.setBCCTo(getAddressesAsString(
        msg.getRecipients(Message.RecipientType.BCC)));

      message.setReceived(msg.getHeader("Received") != null);

      message.setReceivedDate(msg.getReceivedDate());
      message.setSentDate(msg.getSentDate());

      message.m_Subject = msg.getSubject();

      message.setSinglepart(!msg.isMimeType("multipart/*"));

      message.prepareDisplayBody();
      return message;
    }
    catch (Exception mex)
    {
      mex.printStackTrace();
      throw new WBMailException("jwma.displaymessage.failedcreation", 
        true).setException(mex);
    }
  }

  protected WBMailMsgDisplay(Message msg, int number)
  {
    this.m_Message = msg;
    this.m_Number = number;
  }

  private void prepareDisplayBody()
    throws Exception
  {
    Object o = this.m_Message.getContent();

    if ((o instanceof String))
    {
      if (this.m_Message.isMimeType("text/html"))
        this.htmlBody = ((String)o);
      else {
        this.txtBody = ((String)o);
      }
      return;
    }

    if ((o instanceof MimeMultipart))
    {
      MimeMultipart mm = (MimeMultipart)o;
      int c = mm.getCount();
      Vector fns = new Vector();
      for (int i = 0; i < c; i++)
      {
        BodyPart bp = mm.getBodyPart(i);
        String fn = bp.getFileName();
        if ((fn != null) && (!fn.equals("")))
        {
          if (this.appendPartNum2FileName == null) {
            this.appendPartNum2FileName = new HashMap();
          }
          fn = MimeUtility.decodeText(fn);
          this.appendPartNum2FileName.put(Integer.valueOf(i), fn);
        }
        else
        {
          Object oo = bp.getContent();
          if ((oo instanceof String))
          {
            if (bp.isMimeType("text/html"))
              this.htmlBody = ((String)oo);
            else
              this.txtBody = ((String)oo);
          }
          else if ((oo instanceof MimeMultipart))
          {
            MimeMultipart tmpmm = (MimeMultipart)oo;
            int bcount = tmpmm.getCount();
            for (int j = 0; j < bcount; j++)
            {
              BodyPart tmpbp = tmpmm.getBodyPart(j);
              if (tmpbp.isMimeType("text/html"))
              {
                this.htmlBody = tmpbp.getContent().toString();
              }
              else
              {
                this.txtBody = tmpbp.getContent().toString();
              }
            }
          }
        }
      }
    }
  }

  public Message getMessage() {
    return this.m_Message;
  }

  public String getFullHeader()
  {
    return this.m_FullHeader;
  }

  private void setFullHeader(String header)
  {
    this.m_FullHeader = header;
  }

  public int getMessageNumber()
  {
    return this.m_Number;
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

  public String getFrom()
  {
    return this.m_From;
  }

  private void setFrom(String from)
  {
    this.m_From = from;
  }

  public String getReplyTo()
  {
    return this.m_ReplyTo;
  }

  public void setReplyTo(String replyto)
  {
    this.m_ReplyTo = replyto;
  }

  public String getTo()
  {
    return this.m_To;
  }

  private void setTo(String to)
  {
    this.m_To = to;
  }

  public String getCCTo()
  {
    return this.m_CC;
  }

  private void setCCTo(String cc)
  {
    this.m_CC = cc;
  }

  public String getBCCTo()
  {
    return this.m_BCC;
  }

  private void setBCCTo(String bcc)
  {
    this.m_BCC = bcc;
  }

  public String getSubject()
  {
    return this.m_Subject;
  }

  private void setSubject(String subject)
  {
    try
    {
      if (subject == null)
      {
        this.m_Subject = "";
      }
      else
      {
        this.m_Subject = MimeUtility.decodeText(subject);
      }
    }
    catch (Exception ex)
    {
      this.m_Subject = "";
    }
  }

  public boolean isSinglepart()
  {
    return this.m_Singlepart;
  }

  private void setSinglepart(boolean b)
  {
    this.m_Singlepart = b;
  }

  public boolean isMultipart()
  {
    return !this.m_Singlepart;
  }

  public String getTxtBody()
  {
    return this.txtBody;
  }

  public String getHtmlBody()
  {
    return this.htmlBody;
  }

  public String getBodyForWebPage()
  {
    if ((this.htmlBody != null) && (!this.htmlBody.equals(""))) {
      return this.htmlBody;
    }
    if ((this.txtBody == null) || (this.txtBody.equals(""))) {
      return "";
    }

    return WebUtil.plainToHtml(this.txtBody);
  }

  public HashMap<Integer, String> getAppendFileInfo()
  {
    return this.appendPartNum2FileName;
  }

  public boolean hasAppend()
  {
    if (this.appendPartNum2FileName == null)
      return false;
    return this.appendPartNum2FileName.size() > 0;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("\nReceived: ").append(this.m_Received);
    sb.append("\nMsgNumber: ").append(this.m_Number);

    if (this.m_SentDate != null)
      sb.append("\nSentDate: ").append(this.m_SentDate.toString());
    if (this.m_ReceivedDate != null)
      sb.append("\nReceivedDate: ").append(this.m_ReceivedDate.toString());
    if (this.m_From != null)
      sb.append("\nFrom: ").append(this.m_From);
    if (this.m_ReplyTo != null)
      sb.append("\nReplyTo: ").append(this.m_ReplyTo);
    if (this.m_To != null)
      sb.append("\nTo: ").append(this.m_To);
    if (this.m_CC != null)
      sb.append("\nCC: ").append(this.m_CC);
    if (this.m_BCC != null)
      sb.append("\nBCC: ").append(this.m_BCC);
    if (this.m_Subject != null)
      sb.append("\nSubject: ").append(this.m_Subject);
    sb.append("\nIsSinglepart: ").append(this.m_Singlepart);
    if (this.txtBody != null)
      sb.append("\ntext body=").append(this.txtBody);
    if (this.htmlBody != null)
      sb.append("\nhtml body=").append(this.htmlBody);
    if (hasAppend())
    {
      sb.append("\nfile append:");
      for (Map.Entry me : this.appendPartNum2FileName.entrySet())
      {
        sb.append(" ").append(me.getKey()).append("=").append((String)me.getValue());
      }
    }

    return sb.toString();
  }
}
