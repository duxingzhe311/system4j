package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailException;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;

public class WBMailFolder
{
  public static final int TYPE_MAILBOX = 1;
  public static final int TYPE_FOLDER = 2;
  public static final int TYPE_MIXED = 3;
  public static final int TYPE_MESSAGE_CONTAINER = 4;
  public static final int TYPE_FOLDER_CONTAINER = 5;
  public static final int TYPE_ALL = 10;
  private static ILogger log = LoggerManager.getLogger(WBMailFolder.class);
  protected WBMailStore m_Store;
  protected Folder m_Folder;
  protected WBMailFolderList m_Subfolders;
  protected WBMailMsgInfoList m_MessageInfoList;
  protected String m_Name;
  protected String m_Path;
  protected int m_Type;
  Type folderType = Type.normal;
  protected WBMailMsgDisplay m_ActualMessage;
  protected boolean m_OnlineCounting;
  protected FetchProfile m_DraftProfile;

  protected WBMailFolder(Folder f)
  {
    this.m_Folder = f;
  }

  protected WBMailFolder(Folder f, WBMailStore store)
  {
    this.m_Folder = f;
    this.m_Store = store;
  }

  public Type getFolderType()
  {
    return this.folderType;
  }

  public String getName()
  {
    return this.m_Name;
  }

  private void setName(String name)
  {
    this.m_Name = name;
  }

  public String getPath()
  {
    return this.m_Path;
  }

  private void setPath(String path)
  {
    this.m_Path = path;
  }

  public int getType()
  {
    return this.m_Type;
  }

  private void setType(int type)
  {
    this.m_Type = type;
  }

  public boolean isType(int type)
  {
    return this.m_Type == type;
  }

  public boolean isSubscribed()
  {
    return this.m_Folder.isSubscribed();
  }

  public void setSubscribed(boolean b) throws WBMailException
  {
    try
    {
      this.m_Folder.setSubscribed(b);
    }
    catch (Exception ex)
    {
      log.error(ex);
      throw new WBMailException("folder.subscription.failed");
    }
  }

  public Folder getFolder()
  {
    return this.m_Folder;
  }

  public int getNewMessageCount()
  {
    try
    {
      return this.m_Folder.getNewMessageCount();
    }
    catch (MessagingException ex) {
    }
    return -1;
  }

  public boolean hasNewMessages()
  {
    return getNewMessageCount() > 0;
  }

  public int getUnreadMessageCount()
  {
    try
    {
      return this.m_Folder.getUnreadMessageCount();
    }
    catch (MessagingException ex) {
    }
    return -1;
  }

  public boolean hasUnreadMessages()
  {
    return getUnreadMessageCount() > 0;
  }

  public boolean isEmpty()
  {
    return !hasMessages();
  }

  public boolean isOnlineCounting()
  {
    return this.m_OnlineCounting;
  }

  public void setOnlineCounting(boolean b)
  {
    this.m_OnlineCounting = b;
  }

  public void addIfSubfolder(WBMailFolder folder)
  {
    if (isSubfolder(getPath(), folder.getPath()))
    {
      this.m_Subfolders.addFolderToList(folder);
    }
  }

  public void removeIfSubfolder(String path)
  {
    if (isSubfolder(getPath(), path))
    {
      this.m_Subfolders.removeFolderFromList(path);
    }
  }

  public void removeIfSubfolder(String[] folders)
  {
    for (int i = 0; i < folders.length; i++)
    {
      removeIfSubfolder(folders[i]);
    }
  }

  public boolean isSubfolder(String folder, String possiblesubfolder)
  {
    return possiblesubfolder.startsWith(folder);
  }

  public WBMailFolder[] listSubfolders(int type)
  {
    return this.m_Subfolders.createFolderArray(this.m_Subfolders
      .sublist(type, 
      true));
  }

  public WBMailFolder[] listSubfolders()
  {
    return this.m_Subfolders.createFolderArray(this.m_Subfolders
      .sublist(10, true));
  }

  public boolean hasSubfolders()
  {
    if ((this.m_Subfolders != null) && (this.m_Subfolders.size() > 0))
    {
      return true;
    }

    return false;
  }

  public boolean hasMessages()
  {
    if ((!isType(2)) && (getMessageCount() > 0))
    {
      return true;
    }

    return false;
  }

  public int getMessageCount()
  {
    if (isOnlineCounting())
    {
      try
      {
        return this.m_Folder.getMessageCount();
      }
      catch (MessagingException mex)
      {
        log.error(mex);

        return 0;
      }

    }

    if (this.m_MessageInfoList != null)
    {
      return this.m_MessageInfoList.size();
    }

    return 0;
  }

  public WBMailMsgDisplay getActualMessage()
  {
    return this.m_ActualMessage;
  }

  public Message getJavaMessage(int num)
    throws WBMailException
  {
    try
    {
      return this.m_Folder.getMessage(num);
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.folder.getmessage.failed", true)
        .setException(mex);
    }
  }

  public Folder getJavaFolder()
  {
    return this.m_Folder;
  }

  public int[] getReadMessages()
    throws WBMailException
  {
    int[] readmsg = new int[0];
    try
    {
      this.m_Folder.open(1);
      Message[] messages = this.m_Folder.getMessages();
      ArrayList readlist = new ArrayList(messages.length);

      for (int i = 0; i < messages.length; i++)
      {
        if (messages[i].isSet(Flags.Flag.SEEN))
        {
          readlist.add(new Integer(messages[i].getMessageNumber()));
        }
      }

      readmsg = new int[readlist.size()];
      int c = 0;
      for (Iterator iter = readlist.iterator(); iter.hasNext(); c++)
      {
        readmsg[c] = ((Integer)(Integer)iter.next()).intValue();
      }
    }
    catch (MessagingException ex)
    {
      throw new WBMailException("jwma.folder.readmessages");
    }
    finally
    {
      try
      {
        this.m_Folder.close(false);
      }
      catch (MessagingException localMessagingException1)
      {
      }
    }

    return readmsg;
  }

  public int getNextMessageNumber()
  {
    return this.m_MessageInfoList.getNextMessageNumber(
      this.m_ActualMessage.getMessageNumber());
  }

  public int getPreviousMessageNumber()
  {
    return this.m_MessageInfoList.getPreviousMessageNumber(
      this.m_ActualMessage.getMessageNumber());
  }

  public boolean checkMessageExistence(int number)
  {
    return (number >= 1) && (number <= getMessageCount());
  }

  public WBMailMsgDisplay getMsg(int num)
    throws WBMailException
  {
    try
    {
      try
      {
        this.m_Folder.open(2);

        Message msg = getJavaMessage(num);
        WBMailMsgDisplay message = WBMailMsgDisplay.createWBMailMsg(msg);

        if (!msg.isSet(Flags.Flag.SEEN))
        {
          msg.setFlag(Flags.Flag.SEEN, true);
        }

        this.m_Folder.close(false);

        this.m_ActualMessage = message;

        return message;
      }
      catch (MessagingException mex)
      {
        throw new WBMailException("jwma.folder.jwmamessage", true)
          .setException(mex);
      }
    }
    finally
    {
      try
      {
        if (this.m_Folder.isOpen())
        {
          this.m_Folder.close(false);
        }
      }
      catch (MessagingException localMessagingException2)
      {
      }
    }
  }

  public WBMailMsgCompose getDraftMsg(int num)
    throws WBMailException
  {
    try
    {
      try
      {
        this.m_Folder.open(2);

        Message[] msg = { getJavaMessage(num) };
        if (this.m_DraftProfile == null)
        {
          this.m_DraftProfile = new FetchProfile();
          this.m_DraftProfile.add(FetchProfile.Item.ENVELOPE);

          this.m_DraftProfile.add(FetchProfile.Item.FLAGS);

          this.m_DraftProfile.add(FetchProfile.Item.CONTENT_INFO);
        }

        this.m_Folder.fetch(msg, this.m_DraftProfile);

        return null;
      }
      catch (MessagingException ex)
      {
        throw new WBMailException("jwma.folder.draftmessage", true)
          .setException(ex);
      }
    }
    finally
    {
      try
      {
        if (this.m_Folder.isOpen())
        {
          this.m_Folder.close(false);
        }
      }
      catch (MessagingException localMessagingException2)
      {
      }
    }
  }

  private static WBMailMsgDisplay createDraft(Message msg)
    throws WBMailException
  {
    WBMailMsgDisplay message = WBMailMsgDisplay.createWBMailMsg((MimeMessage)msg);

    return message;
  }

  public WBMailMsgInfoList getMessageInfoList()
  {
    return this.m_MessageInfoList;
  }

  public int deleteActualMessage()
    throws WBMailException
  {
    int nextmsgnum = getNextMessageNumber() - 1;

    deleteMessage(this.m_ActualMessage.getMessageNumber());

    return nextmsgnum;
  }

  public void deleteMessage(int number)
    throws WBMailException
  {
    int[] nums = { number };
    deleteMessages(nums);
  }

  public void deleteAllMessages()
    throws WBMailException
  {
    int[] msgnumbers = new int[getMessageCount()];
    for (int i = 0; i < getMessageCount(); i++)
    {
      msgnumbers[i] = (i + 1);
    }
    deleteMessages(msgnumbers);
  }

  public void deleteMessages(int[] numbers)
    throws WBMailException
  {
    if ((numbers == null) || (numbers.length == 0))
    {
      return;
    }

    try
    {
      this.m_Folder.open(2);
      Message[] msgs = this.m_Folder.getMessages(numbers);
      if (msgs.length != 0)
      {
        if (!this.m_Path.equals(this.m_Store.getTrashFolder().getFullName()))
        {
          this.m_Folder.copyMessages(msgs, this.m_Store.getTrashFolder());
        }

        this.m_Folder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
      }

      this.m_Folder.close(true);

      this.m_MessageInfoList.remove(numbers);
      this.m_MessageInfoList.removeDeleted();

      this.m_MessageInfoList.renumber();
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.folder.deletemessage.failed", true)
        .setException(mex);
    }
    finally
    {
      try
      {
        if (this.m_Folder.isOpen())
        {
          this.m_Folder.close(false);
        }
      }
      catch (MessagingException localMessagingException1)
      {
      }
    }
  }

  public int moveActualMessage(String destfolder)
    throws WBMailException
  {
    int nextmsgnum = getNextMessageNumber() - 1;

    moveMessage(this.m_ActualMessage.getMessageNumber(), destfolder);

    return nextmsgnum;
  }

  public void moveMessage(int number, String destfolder)
    throws WBMailException
  {
    int[] nums = { number };
    moveMessages(nums, destfolder);
  }

  public void moveMessages(int[] numbers, String destfolder)
    throws WBMailException
  {
    if ((numbers == null) || (numbers.length == 0) || (destfolder == null) || 
      (destfolder.length() == 0))
    {
      return;
    }

    try
    {
      if (!this.m_Store.checkFolderExistence(destfolder))
      {
        throw new WBMailException(
          "jwma.folder.movemessage.destination.missing", true);
      }
      Folder dest = this.m_Store.getFolder(destfolder);

      if (dest.getType() == 2)
      {
        throw new WBMailException(
          "jwma.folder.movemessage.destination.foul", true);
      }

      this.m_Folder.open(2);

      Message[] msgs = this.m_Folder.getMessages(numbers);
      if (msgs.length != 0)
      {
        this.m_Folder.copyMessages(msgs, dest);
        this.m_Folder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
      }
      this.m_Folder.close(true);

      this.m_MessageInfoList.remove(numbers);
      this.m_MessageInfoList.removeDeleted();
      this.m_MessageInfoList.renumber();
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.folder.movemessage.failed", true)
        .setException(mex);
    }
    finally
    {
      try
      {
        if (this.m_Folder.isOpen())
        {
          this.m_Folder.close(false);
        }
      }
      catch (MessagingException localMessagingException1)
      {
      }
    }
  }

  public void writeMessagePart(Part part, OutputStream out)
    throws IOException, WBMailException
  {
    try
    {
      this.m_Folder.open(1);

      InputStream in = part.getInputStream();
      int i;
      while ((i = in.read()) != -1)
      {
        int i;
        out.write(i);
      }
      out.flush();
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("message.displaypart.failed")
        .setException(mex);
    }
    finally
    {
      try
      {
        if (this.m_Folder.isOpen())
        {
          this.m_Folder.close(false);
        }
      }
      catch (MessagingException ex)
      {
        log.error(ex);
      }
    }
  }

  public boolean equals(Object o)
  {
    if ((o instanceof WBMailFolder))
    {
      return getPath().equals(((WBMailFolder)o).getPath());
    }

    return false;
  }

  public void prepare()
    throws WBMailException
  {
    try
    {
      this.m_Name = this.m_Folder.getName();
      this.m_Path = this.m_Folder.getFullName();
      this.m_Type = this.m_Folder.getType();

      this.m_Subfolders = WBMailFolderList.createSubfolderList(this.m_Folder);
      if ((isType(1)) || 
        (isType(3)))
      {
        long st = System.currentTimeMillis();

        this.m_MessageInfoList = 
          WBMailMsgInfoList.createMsgInfoList(this.m_Folder);

        System.out.println("in perpare create msg list cost==" + (System.currentTimeMillis() - st));
      }
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.folder.failedcreation", true)
        .setException(mex);
    }
  }

  public void update(WBMailStore store)
    throws WBMailException
  {
    this.m_Store = store;
    prepare();
  }

  public static WBMailFolder createWBMailFolderImpl(WBMailStore store, Folder f)
    throws WBMailException
  {
    WBMailFolder folder = new WBMailFolder(f, store);
    long st = System.currentTimeMillis();
    folder.prepare();
    long et = System.currentTimeMillis();
    System.out.println("prepare folder cost==" + (et - st));
    return folder;
  }

  public static WBMailFolder createWBMailFolderImpl(WBMailStore store, String fullname)
    throws WBMailException
  {
    WBMailFolder folder = new WBMailFolder(store.getFolder(fullname), store);
    folder.prepare();
    return folder;
  }

  public static WBMailFolder createLight(Folder folder)
    throws WBMailException
  {
    try
    {
      WBMailFolder WBMailFolder = new WBMailFolder(folder);

      WBMailFolder.setName(folder.getName());
      WBMailFolder.setPath(folder.getFullName());
      WBMailFolder.setType(folder.getType());
      return WBMailFolder;
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.folder.failedcreation", true)
        .setException(mex);
    }
  }

  public static enum Type
  {
    normal, 
    inbox, 
    trash, 
    draft, 
    sent;
  }
}
