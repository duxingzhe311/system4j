package com.dw.comp.mail.model;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class WBMailBodyCompose
{
  private transient Message mailMsg = null;

  private String txtBody = null;

  private String htmlBody = null;

  private HashMap<Integer, String> appendPartNum2FileName = null;

  public static WBMailBodyCompose createDisplayBody(Message msg)
    throws Exception
  {
    WBMailBodyCompose rets = new WBMailBodyCompose(msg);

    Object o = msg.getContent();

    if ((o instanceof String))
    {
      if (msg.isMimeType("text/html"))
        rets.htmlBody = ((String)o);
      else {
        rets.txtBody = ((String)o);
      }
      return rets;
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
          if (rets.appendPartNum2FileName == null) {
            rets.appendPartNum2FileName = new HashMap();
          }
          fn = MimeUtility.decodeText(fn);
          rets.appendPartNum2FileName.put(Integer.valueOf(i), fn);
        }
        else
        {
          Object oo = bp.getContent();
          if ((oo instanceof String))
          {
            if (bp.isMimeType("text/html"))
              rets.htmlBody = ((String)oo);
            else
              rets.txtBody = ((String)oo);
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
                rets.htmlBody = tmpbp.getContent().toString();
              }
              else
              {
                rets.txtBody = tmpbp.getContent().toString();
              }
            }
          }
        }
      }
    }
    return rets;
  }

  public WBMailBodyCompose(Message msg)
  {
    this.mailMsg = msg;
  }

  public String getTxtBody()
  {
    return this.txtBody;
  }

  public void setTxtBody(String txt)
  {
    this.txtBody = txt;
  }

  public String getHtmlBody()
  {
    return this.htmlBody;
  }

  public void setHtmlBody(String htmlstr)
  {
    this.htmlBody = htmlstr;
  }

  public HashMap<Integer, String> getAppendFileInfo()
  {
    return this.appendPartNum2FileName;
  }

  public void setAppendFileInfo(HashMap<Integer, String> pn2fn)
  {
    this.appendPartNum2FileName = pn2fn;
  }

  public boolean hasAppend()
  {
    if (this.appendPartNum2FileName == null)
      return false;
    return this.appendPartNum2FileName.size() > 0;
  }

  public String toString()
  {
    StringBuilder tmpsb = new StringBuilder();

    if (this.txtBody != null)
      tmpsb.append("\ntext body=").append(this.txtBody);
    if (this.htmlBody != null)
      tmpsb.append("\nhtml body=").append(this.htmlBody);
    if (hasAppend())
    {
      tmpsb.append("\nfile append:");
      for (Map.Entry me : this.appendPartNum2FileName.entrySet())
      {
        tmpsb.append(me.getKey()).append("=").append((String)me.getValue());
      }
    }
    return tmpsb.toString();
  }
}
