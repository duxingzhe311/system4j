package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailException;
import com.dw.comp.mail.config.FolderMap;
import com.dw.comp.mail.config.PostOffice;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

public class WBMailStore
{
  private static ILogger log = LoggerManager.getLogger(WBMailStore.class);
  public static final boolean c_SubscribedOnly = true;
  private PostOffice m_PostOffice;
  private WBMailSession m_Session;
  private Store m_Store;
  private Folder m_RootFolder;
  private WBMailFolder m_TrashFolder;
  private Folder m_SentMailFolder;
  private Folder m_DraftFolder;
  private WBMailFolder m_WBMailRootFolder;
  private WBMailFolder m_ActualFolder;
  private WBMailFolder m_InboxFolder;
  private char m_FolderSeparator;
  private WBMailFolderList m_Folders;
  private FolderMap folderMap = null;

  private WBMailStore(WBMailSession session, Store mstore, FolderMap fm)
  {
    this.m_Store = mstore;
    this.m_Session = session;
    this.m_PostOffice = session.getPostOffice();
    this.folderMap = fm;
  }

  public WBMailFolder getActualFolder()
  {
    return this.m_ActualFolder;
  }

  private void setActualFolder(WBMailFolder f)
  {
    this.m_ActualFolder = f;
  }

  public Folder getRootFolder() throws MessagingException
  {
    return this.m_Store.getDefaultFolder();
  }

  public List<Folder> listAllFolders()
    throws MessagingException
  {
    Folder rf = getRootFolder();
    ArrayList rets = new ArrayList();
    listFolder(rf, rets);
    return rets;
  }

  private void listFolder(Folder f, ArrayList<Folder> fs) throws MessagingException
  {
    fs.add(f);

    Folder[] sfs = f.list();
    if ((sfs == null) || (sfs.length == 0)) {
      return;
    }
    for (Folder tmpf : sfs)
    {
      listFolder(tmpf, fs);
    }
  }

  public WBMailFolder getInboxInfo()
  {
    return this.m_InboxFolder;
  }

  public Folder getJavaInboxFolder()
  {
    return null;
  }

  public WBMailFolder getTrashInfo()
    throws WBMailException
  {
    return this.m_TrashFolder;
  }

  public Folder getTrashFolder()
  {
    return this.m_TrashFolder.getFolder();
  }

  public void setTrashFolder()
    throws WBMailException
  {
    try
    {
      String path = this.folderMap.getTrashFolderName();

      Folder trash = getFolder(path);
      if (!trash.exists())
      {
        if (!trash.create(1))
        {
          throw new WBMailException("create trash folder failed!");
        }

        log.debug("setTrashFolder(): Created trash folder=" + path);
      }

      trash.setSubscribed(true);
      this.m_TrashFolder = WBMailFolder.createLight(trash);

      this.m_TrashFolder.setOnlineCounting(true);
    }
    catch (MessagingException ex)
    {
      throw new WBMailException(ex.getMessage()).setException(ex);
    }
  }

  public Folder getDraftFolder()
  {
    return this.m_DraftFolder;
  }

  protected void setDraftFolder(Folder f)
  {
    this.m_DraftFolder = f;
  }

  public void setDraftFolder()
    throws WBMailException
  {
    try
    {
      String path = this.folderMap.getDraftFolderName();
      this.m_DraftFolder = getFolder(path);
      if (!this.m_DraftFolder.exists())
      {
        if (!this.m_DraftFolder.create(3))
        {
          throw new WBMailException("create draft folder failed!");
        }

        log.debug("setDraftFolder(): Created draft folder=" + path);
      }

      this.m_DraftFolder.setSubscribed(true);
    }
    catch (MessagingException ex)
    {
      throw new WBMailException(ex.getMessage()).setException(ex);
    }
  }

  public Folder getSentMailFolder()
    throws MessagingException
  {
    String sent_box_name = "sent";
    if (this.folderMap != null)
    {
      sent_box_name = this.folderMap.getSentFolderName();
      if ((sent_box_name == null) || (sent_box_name.equals("")))
        sent_box_name = "sent";
    }
    return this.m_Store.getFolder(sent_box_name);
  }

  protected void setSentMailFolder(Folder f)
  {
    this.m_SentMailFolder = f;
  }

  public void setSentMailFolder()
    throws WBMailException
  {
    try
    {
      String path = this.folderMap.getSentFolderName();

      this.m_SentMailFolder = getFolder(path);
      if (!this.m_SentMailFolder.exists())
      {
        boolean success = this.m_SentMailFolder
          .create(3);
        log.debug("setSentMailFolder(): Created sent-mail folder=" + 
          path + ":" + success);
      }
      else
      {
        log.debug("setSentMailFolder(): Set sent-mail folder=" + path);
      }
      this.m_SentMailFolder.setSubscribed(true);
    }
    catch (MessagingException ex)
    {
      throw new WBMailException(ex.getMessage()).setException(ex);
    }
  }

  public void archiveSentMail(Message message)
    throws WBMailException
  {
    Folder archive = null;
    try
    {
      message.setSentDate(new Date());

      archive = getSentMailFolder();

      archive.open(2);

      Message[] tosave = { message };

      archive.appendMessages(tosave);

      archive.close(false);
    }
    catch (MessagingException mex)
    {
      try
      {
        if (archive.isOpen())
        {
          archive.close(false);
        }
      }
      catch (Exception localException)
      {
      }
    }
  }

  private WBMailFolder createWBMailFolder(String fullname)
    throws WBMailException
  {
    return WBMailFolder.createWBMailFolderImpl(this, getFolder(fullname));
  }

  public Folder getFolder(String fullname)
    throws WBMailException
  {
    try
    {
      if (fullname.length() != 0)
      {
        return this.m_Store.getFolder(fullname);
      }

      return this.m_Store.getDefaultFolder();
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.store.getfolder", true)
        .setException(mex);
    }
  }

  public Folder getJavaFolder(String fullname) throws MessagingException
  {
    return this.m_Store.getFolder(fullname);
  }

  public WBMailFolder getWBMailFolder(String fullname) throws WBMailException
  {
    try
    {
      Folder f = this.m_Store.getFolder(fullname);
      if (f == null) {
        return null;
      }
      if (!f.exists()) {
        return null;
      }
      return WBMailFolder.createWBMailFolderImpl(this, f);
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.store.getfolder", true)
        .setException(mex);
    }
  }

  public void createFolder(String fullname, int type)
    throws WBMailException
  {
    try
    {
      if (fullname.indexOf(getFolderSeparator()) == -1)
      {
        if (this.m_ActualFolder.getPath().length() > 0)
        {
          fullname = this.m_ActualFolder.getPath() + getFolderSeparator() + 
            fullname;
        }
      }
      Folder newfolder = getFolder(fullname);
      if (newfolder.exists())
      {
        throw new WBMailException("jwma.store.createfolder.exists", 
          true);
      }

      newfolder.create(type);

      newfolder.setSubscribed(true);

      WBMailFolder folder = WBMailFolder.createLight(newfolder);
      this.m_Folders.addFolderToList(folder);
      this.m_ActualFolder.addIfSubfolder(folder);
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.store.createfolder.failed", true)
        .setException(mex);
    }
  }

  public void deleteFolders(String[] folders)
    throws WBMailException
  {
    for (int i = 0; i < folders.length; i++)
    {
      deleteFolder(folders[i]);
    }
  }

  public void deleteFolder(String fullname)
    throws WBMailException
  {
    try
    {
      Folder delfolder = getFolder(fullname);

      if (isSpecialFolder(fullname))
      {
        throw new WBMailException(
          "jwma.store.deletefolder.systemfolder", true);
      }

      delfolder.setSubscribed(false);
      delfolder.delete(true);

      this.m_Folders.removeFolderFromList(fullname);
      this.m_ActualFolder.removeIfSubfolder(fullname);
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.store.deletefolder.failed", true)
        .setException(mex);
    }
  }

  public void moveFolder(String foldername, String destfolder)
    throws WBMailException
  {
    String[] folders = { foldername };
    moveFolders(folders, destfolder);
  }

  public void moveFolders(String[] foldernames, String destfolder)
    throws WBMailException
  {
    try
    {
      if (!checkFolderExistence(destfolder))
      {
        throw new WBMailException(
          "jwma.store.movefolder.destination.missing", true);
      }

      Folder dest = getFolder(destfolder);
      if ((dest.getType() != 10) && (dest.getType() != 2))
      {
        throw new WBMailException(
          "jwma.store.movefolder.destination.foul", true);
      }

      List folders = getFolders(foldernames);

      for (Iterator iter = folders.iterator(); iter.hasNext(); )
      {
        Folder oldfolder = (Folder)iter.next();
        Folder newfolder = getFolder(destfolder + getFolderSeparator() + 
          oldfolder.getName());
        if (newfolder.exists())
        {
          throw new WBMailException(
            "jwma.store.movefolder.destination.exists", true);
        }

        if (!newfolder.getFullName().regionMatches(0, 
          oldfolder.getFullName(), 0, 
          oldfolder.getFullName().length()))
        {
          if (oldfolder.isSubscribed())
          {
            oldfolder.setSubscribed(false);
          }
          oldfolder.renameTo(newfolder);
          newfolder.setSubscribed(true);
        }
        else
        {
          throw new WBMailException(
            "jwma.store.movefolder.destination.subfolder", true);
        }
      }

      this.m_Folders.rebuild();
      this.m_ActualFolder.removeIfSubfolder(foldernames);
    }
    catch (MessagingException mex)
    {
      throw new WBMailException("jwma.store.movefolder.failed", true)
        .setException(mex);
    }
  }

  public void updateFolderSubscription(String[] foldernames, boolean subscribe)
    throws WBMailException
  {
    try
    {
      List folders = getFolders(foldernames);
      for (Iterator iter = folders.iterator(); iter.hasNext(); )
      {
        ((Folder)iter.next()).setSubscribed(subscribe);
      }
    }
    catch (Exception ex)
    {
      log.error(ex);
      throw new WBMailException("jwma.store.updatesubscription.failed", 
        true).setException(ex);
    }
  }

  private List getFolders(String[] foldernames)
    throws MessagingException, WBMailException
  {
    ArrayList folders = new ArrayList(foldernames.length);
    Folder f = null;
    for (int i = 0; i < foldernames.length; i++)
    {
      f = getFolder(foldernames[i]);

      if ((f.exists()) && (!isSpecialFolder(foldernames[i])))
      {
        folders.add(f);
      }
    }
    return folders;
  }

  private boolean isSpecialFolder(String fullname)
  {
    return (fullname.equalsIgnoreCase(this.m_RootFolder.getFullName())) || 
      (fullname.equalsIgnoreCase(this.m_InboxFolder.getName())) || 
      (fullname.equalsIgnoreCase(this.m_TrashFolder.getName())) || 
      (fullname.equalsIgnoreCase(this.m_DraftFolder.getFullName())) || 
      (fullname.equalsIgnoreCase(this.m_SentMailFolder.getFullName()));
  }

  public WBMailFolder[] listFolders(int type)
  {
    return this.m_Folders.createFolderArray(this.m_Folders
      .sublist(type, 
      true));
  }

  public WBMailFolder[] listFolders(int type, boolean subscribed)
  {
    String[] excludes = { this.m_RootFolder.getFullName(), 
      this.m_InboxFolder.getPath(), this.m_TrashFolder.getPath(), 
      this.m_DraftFolder.getFullName(), this.m_SentMailFolder.getFullName() };
    return this.m_Folders.createFolderArray(this.m_Folders
      .sublist(type, excludes, 
      subscribed));
  }

  public WBMailFolder[] listFolderMoveTargets()
  {
    return this.m_Folders.createFolderArray(this.m_Folders
      .sublist(5, this.m_ActualFolder, 
      true));
  }

  public WBMailFolder[] listMessageMoveTargets()
  {
    return this.m_Folders.createFolderArray(this.m_Folders
      .sublist(4, this.m_ActualFolder, 
      true));
  }

  public ArrayList<Folder> getUserDefinedFolders() throws MessagingException
  {
    System.out.println("------------getUserDefinedFolders");
    long st = System.currentTimeMillis();

    ArrayList rets = new ArrayList();
    Folder[] fds = this.m_RootFolder.list();
    for (Folder tmpfd : fds)
    {
      System.out.println(" >>>>>==" + tmpfd.getFullName());
      if (!isSpecialFolder(tmpfd.getFullName()))
      {
        System.out.println(" ******==is not special");
        rets.add(tmpfd);
      }
    }

    System.out.println("--end----------getUserDefinedFolders cost=" + (
      System.currentTimeMillis() - st));
    return rets;
  }

  public boolean isMixedMode()
  {
    return this.m_PostOffice.isType("mixed");
  }

  public char getFolderSeparator()
  {
    return this.m_FolderSeparator;
  }

  protected void setFolderSeparator(char c)
  {
    this.m_FolderSeparator = c;
  }

  public void close()
  {
    try
    {
      this.m_Store.close();
    }
    catch (MessagingException mex)
    {
      log.error(mex);
    }
  }

  public void prepare()
    throws WBMailException
  {
    log.debug("prepare()");
    try
    {
      String rootpath = this.folderMap.getRootFolderPath();
      if ((rootpath == null) || (rootpath.equals("")))
      {
        this.m_RootFolder = this.m_Store.getDefaultFolder();
      }
      else
      {
        this.m_RootFolder = this.m_Store.getFolder(rootpath);
        if ((this.m_RootFolder == null) || (!this.m_RootFolder.exists()))
        {
          this.m_RootFolder = this.m_Store.getDefaultFolder();
        }

      }

      if (!this.m_RootFolder.exists())
      {
        throw new WBMailException("jwma.store.rootfolder");
      }

      setFolderSeparator(this.m_RootFolder.getSeparator());

      this.m_Folders = WBMailFolderList.createStoreList(this.m_RootFolder);

      this.m_InboxFolder = getWBMailFolder("INBOX");

      this.m_InboxFolder.setOnlineCounting(true);

      this.m_WBMailRootFolder = getWBMailFolder(this.m_RootFolder.getFullName());
    }
    catch (MessagingException ex)
    {
      log.debug("prepare()", ex);
      throw new WBMailException("jwma.store.prepare").setException(ex);
    }
  }

  public boolean checkFolderExistence(String path)
    throws WBMailException
  {
    if ((path.equals(this.m_RootFolder.getFullName())) || (path.equals("INBOX")))
    {
      return true;
    }
    return this.m_Folders.contains(path);
  }

  public static WBMailStore createJwmaStoreImpl(WBMailSession session, Store mstore, FolderMap fm)
    throws WBMailException
  {
    WBMailStore store = new WBMailStore(session, mstore, fm);

    return store;
  }
}
