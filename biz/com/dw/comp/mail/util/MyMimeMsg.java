package com.dw.comp.mail.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

public class MyMimeMsg extends MimeMessage
{
  public MyMimeMsg(InputStream is)
    throws MessagingException
  {
    super(null, is);
  }

  protected void parse(InputStream is)
    throws MessagingException
  {
    super.parse(is);
  }

  protected InternetHeaders createInternetHeaders(InputStream is)
    throws MessagingException
  {
    return new MyInternetHeader(is);
  }

  public static void main(String[] args)
    throws Exception
  {
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream("E:/working_pro/NetMon/data/popdata/2005/3/3/1109814415_1.pop_2.eml");
      long t1 = System.currentTimeMillis();
      MyMimeMsg mmm = new MyMimeMsg(fis);
      long t2 = System.currentTimeMillis();
      System.out.println("cost==" + (t2 - t1));

      System.out.println("encoding==" + mmm.getEncoding());

      System.out.println("send date==" + mmm.getSentDate() + "  " + 
        mmm.getSentDate().getTime());

      Address[] addrs = mmm.getFrom();
      if (addrs != null)
      {
        for (int i = 0; i < addrs.length; i++)
        {
          System.out.println("from===" + addrs[i].toString() + 
            " type==" + addrs[i].getType());
        }
      }
      addrs = mmm.getRecipients(MimeMessage.RecipientType.TO);
      if (addrs != null)
      {
        for (int i = 0; i < addrs.length; i++)
        {
          System.out.println("to===" + addrs[i].toString() + 
            " type==" + addrs[i].getType());
        }
      }

      addrs = mmm.getRecipients(MimeMessage.RecipientType.CC);
      if (addrs != null)
      {
        for (int i = 0; i < addrs.length; i++)
        {
          System.out.println("cc to===" + addrs[i].toString() + 
            " type==" + addrs[i].getType());
        }
      }

      addrs = mmm.getRecipients(MimeMessage.RecipientType.BCC);
      if (addrs != null)
      {
        for (int i = 0; i < addrs.length; i++)
        {
          System.out.println("bcc to===" + addrs[i].toString() + 
            " type==" + addrs[i].getType());
        }
      }

      System.out.println("subject===" + mmm.getSubject());
      System.out.println("content type==" + mmm.getContentType());

      Object o = mmm.getContent();
      System.out.println("content class==" + o.getClass().getName());
      if ((o instanceof String))
      {
        System.out.println(o.toString());
      }
      else if ((o instanceof MimeMultipart))
      {
        MimeMultipart mm = (MimeMultipart)o;
        int c = mm.getCount();
        for (int i = 0; i < c; i++)
        {
          BodyPart bp = mm.getBodyPart(i);
          System.out.println(i + "cn=" + bp.getClass().getName() + 
            " - content type=" + bp.getContentType());
          System.out.println("Content cn==" + 
            bp.getContent().getClass().getName());
          System.out.println("\n" + bp.getContent());
          MimeBodyPart mbp = (MimeBodyPart)bp;
          System.out.println("filename=" + mbp.getFileName());
          InputStream is = mbp.getInputStream();
          mbp.getFileName();
        }

      }

    }
    finally
    {
      if (fis != null)
      {
        fis.close();
      }
    }
  }
}
