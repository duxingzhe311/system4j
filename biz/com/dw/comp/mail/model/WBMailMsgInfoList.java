package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailException;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

public class WBMailMsgInfoList
{
  private static ILogger log = LoggerManager.getLogger(WBMailMsgInfoList.class);
  protected List m_MessageInfos;
  protected boolean m_HasDeleted = false;

  protected int m_LastSortCriteria = 8;

  public int size()
  {
    return this.m_MessageInfos.size();
  }

  public WBMailMsgInfo[] listMessageInfos()
  {
    WBMailMsgInfo[] list = new WBMailMsgInfo[this.m_MessageInfos.size()];
    return (WBMailMsgInfo[])this.m_MessageInfos.toArray(list);
  }

  public void sort(int criteria)
  {
    sort(criteria, true);
  }

  private void sort(int criteria, boolean remember)
  {
    Comparator comp = null;
    try
    {
      comp = com.dw.comp.mail.util.MessageSortingUtil.CRITERIA_COMPARATOR[criteria];
    }
    catch (IndexOutOfBoundsException ex)
    {
      return;
    }

    Collections.sort(this.m_MessageInfos, comp);

    if (remember)
    {
      this.m_LastSortCriteria = criteria;
    }
  }

  public int getLastSortCriteria()
  {
    return this.m_LastSortCriteria;
  }

  public int getNextMessageNumber(int msgnum)
  {
    int listindex = getListIndex(msgnum);
    if ((listindex == -1) || (listindex == this.m_MessageInfos.size() - 1))
    {
      return -1;
    }

    return ((WBMailMsgInfo)this.m_MessageInfos.get(listindex + 1))
      .getMessageNumber();
  }

  public int getPreviousMessageNumber(int msgnum)
  {
    int listindex = getListIndex(msgnum);
    if ((listindex == -1) || (listindex == 0))
    {
      return -1;
    }

    return ((WBMailMsgInfo)this.m_MessageInfos.get(listindex - 1))
      .getMessageNumber();
  }

  public int getListIndex(int msgnum)
  {
    for (Iterator iter = iterator(); iter.hasNext(); )
    {
      WBMailMsgInfo msginfo = (WBMailMsgInfo)iter.next();
      if (msginfo.getMessageNumber() == msgnum)
      {
        return this.m_MessageInfos.indexOf(msginfo);
      }
    }
    return -1;
  }

  public Iterator iterator()
  {
    return this.m_MessageInfos.iterator();
  }

  public void remove(int[] msgsnums)
  {
    int n;
    for (Iterator iter = iterator(); iter.hasNext(); 
      n < msgsnums.length)
    {
      int num = ((WBMailMsgInfo)iter.next()).getMessageNumber();
      n = 0; continue;

      if (num == msgsnums[n])
      {
        iter.remove();
      }
      n++;
    }
  }

  public void removeDeleted()
  {
    if (this.m_HasDeleted)
    {
      WBMailMsgInfo msg = null;
      for (Iterator iter = iterator(); iter.hasNext(); )
      {
        msg = (WBMailMsgInfo)iter.next();
        if (msg.isDeleted())
        {
          iter.remove();
        }
      }
      this.m_HasDeleted = false;
    }
  }

  public void renumber()
  {
    sort(8, false);

    int i = 1;
    for (Iterator iter = iterator(); iter.hasNext(); i++)
    {
      ((WBMailMsgInfo)iter.next()).setMessageNumber(i);
    }

    sort(this.m_LastSortCriteria, false);
  }

  private void buildMessageInfoList(Message[] messages)
    throws WBMailException
  {
    this.m_MessageInfos = new ArrayList(messages.length);
    WBMailMsgInfo msginfo = null;
    for (int i = 0; i < messages.length; i++)
    {
      try
      {
        msginfo = WBMailMsgInfo.createMsgInfo(messages[i]);

        if ((msginfo.isDeleted()) && (!this.m_HasDeleted))
        {
          this.m_HasDeleted = true;
        }
        this.m_MessageInfos.add(msginfo);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }

  public static WBMailMsgInfoList createMsgInfoList(Message[] messages)
    throws WBMailException
  {
    WBMailMsgInfoList msglist = new WBMailMsgInfoList();

    msglist.buildMessageInfoList(messages);
    return msglist;
  }

  public static WBMailMsgInfoList createMsgInfoList(Folder f)
    throws WBMailException
  {
    try
    {
      try
      {
        long st = System.currentTimeMillis();
        if (!f.isOpen())
        {
          f.open(1);
        }
        long op_t = System.currentTimeMillis();
        System.out.println("open folder cost==" + (op_t - st));
        Message[] msgs = f.getMessages();

        long gm_t = System.currentTimeMillis();
        System.out.println("open getMessages cost==" + (gm_t - op_t));

        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        f.fetch(msgs, fp);

        long ft_t = System.currentTimeMillis();
        System.out.println("open fetch msg cost==" + (ft_t - gm_t));

        WBMailMsgInfoList ml = createMsgInfoList(msgs);
        long ml_t = System.currentTimeMillis();
        System.out.println("create msg list cost==" + (ml_t - ft_t));
        return ml;
      }
      catch (MessagingException mex)
      {
        throw new WBMailException("jwma.messagelist.failedcreation");
      }
    }
    finally
    {
      try
      {
        if (f.isOpen())
        {
          f.close(false);
        }
      }
      catch (MessagingException localMessagingException2)
      {
      }
    }
  }
}
