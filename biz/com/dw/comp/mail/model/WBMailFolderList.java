package com.dw.comp.mail.model;

import com.dw.comp.mail.WBMailException;
import com.dw.comp.mail.util.StringUtil;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.mail.Folder;
import javax.mail.MessagingException;

class WBMailFolderList
{
  private static ILogger log = LoggerManager.getLogger(WBMailFolderList.class);

  public static boolean c_SubscribedOnly = true;
  private Folder m_Folder;
  private List m_Folders;
  private boolean m_Recursive;
  private boolean m_SubscribedOnly = c_SubscribedOnly;
  private String m_Pattern;
  public static final Comparator LEXOGRAPHICAL = new Comparator()
  {
    public int compare(Object o1, Object o2)
    {
      WBMailFolder f1 = (WBMailFolder)o1;
      WBMailFolder f2 = (WBMailFolder)o2;
      return f1.getName().compareTo(f2.getName());
    }
  };

  private WBMailFolderList(Folder f, boolean recurse)
  {
    this.m_Recursive = recurse;
    if (recurse)
    {
      this.m_Pattern = "*";
    }
    else
    {
      this.m_Pattern = "%";
    }
    this.m_Folder = f;
  }

  public boolean isSubscribedOnly()
  {
    return this.m_SubscribedOnly;
  }

  public void setSubscribedOnly(boolean subscribedOnly)
  {
    this.m_SubscribedOnly = subscribedOnly;
  }

  public String getPattern()
  {
    return this.m_Pattern;
  }

  public void setPattern(String pattern)
  {
    this.m_Pattern = pattern;
  }

  public Iterator iterator()
  {
    return (Iterator)this.m_Folders.listIterator();
  }

  public int size()
  {
    return this.m_Folders.size();
  }

  public List sublist(int type)
  {
    if (type == 10)
    {
      return this.m_Folders;
    }

    List folders = new ArrayList();
    for (Iterator iter = (Iterator)this.m_Folders.listIterator(); iter.hasNext(); )
    {
      WBMailFolder f = (WBMailFolder)iter.next();
      switch (type)
      {
      case 4:
        if ((f.isType(1)) || 
          (f.isType(3)))
        {
          folders.add(f);
        }
        break;
      case 5:
        if ((f.isType(2)) || 
          (f.isType(3)))
        {
          folders.add(f);
        }
        break;
      default:
        if (f.isType(type))
        {
          folders.add(f);
        }
        break;
      }
    }
    Collections.sort(folders, LEXOGRAPHICAL);
    return folders;
  }

  public List sublist(int type, boolean subscribed)
  {
    if (type == 10)
    {
      return this.m_Folders;
    }

    List folders = new ArrayList();
    for (Iterator iter = (Iterator)this.m_Folders.listIterator(); iter.hasNext(); )
    {
      WBMailFolder f = (WBMailFolder)iter.next();
      switch (type)
      {
      case 4:
        if ((f.isType(1)) || 
          (f.isType(3)))
        {
          if (subscribed)
          {
            if (f.isSubscribed())
            {
              folders.add(f);
            }
          }
          else
          {
            folders.add(f);
          }
        }
        break;
      case 5:
        if ((f.isType(2)) || 
          (f.isType(3)))
        {
          if (subscribed)
          {
            if (f.isSubscribed())
            {
              folders.add(f);
            }
          }
          else
          {
            folders.add(f);
          }
        }
        break;
      default:
        if (f.isType(type))
        {
          if (subscribed)
          {
            if (f.isSubscribed())
            {
              folders.add(f);
            }
          }
          else
          {
            folders.add(f);
          }
        }
        break;
      }
    }
    Collections.sort(folders, LEXOGRAPHICAL);
    return folders;
  }

  public List sublist(int type, WBMailFolder folder)
  {
    List folders = new ArrayList();
    for (Iterator iter = (Iterator)this.m_Folders.listIterator(); iter.hasNext(); )
    {
      WBMailFolder f = (WBMailFolder)iter.next();
      if (!f.equals(folder))
      {
        switch (type)
        {
        case 10:
          folders.add(f);
          break;
        case 4:
          if ((f.isType(1)) || 
            (f.isType(3)))
          {
            folders.add(f);
          }
          break;
        case 5:
          if ((f.isType(2)) || 
            (f.isType(3)))
          {
            folders.add(f);
          }
          break;
        case 6:
        case 7:
        case 8:
        case 9:
        default:
          if (f.isType(type))
          {
            folders.add(f);
          }break;
        }
      }
    }
    Collections.sort(folders, LEXOGRAPHICAL);
    return folders;
  }

  public List sublist(int type, WBMailFolder folder, boolean subscribed)
  {
    List folders = new ArrayList();
    for (Iterator iter = (Iterator)this.m_Folders.listIterator(); iter.hasNext(); )
    {
      WBMailFolder f = (WBMailFolder)iter.next();
      if (!f.equals(folder))
      {
        switch (type)
        {
        case 10:
          if (subscribed)
          {
            if (f.isSubscribed())
            {
              folders.add(f);
            }
          }
          else
          {
            folders.add(f);
          }
          break;
        case 4:
          if ((f.isType(1)) || 
            (f.isType(3)))
          {
            if (subscribed)
            {
              if (f.isSubscribed())
              {
                folders.add(f);
              }
            }
            else
            {
              folders.add(f);
            }
          }
          break;
        case 5:
          if ((f.isType(2)) || 
            (f.isType(3)))
          {
            if (subscribed)
            {
              if (f.isSubscribed())
              {
                folders.add(f);
              }
            }
            else
            {
              folders.add(f);
            }
          }
          break;
        case 6:
        case 7:
        case 8:
        case 9:
        default:
          if (f.isType(type))
          {
            if (subscribed)
            {
              if (f.isSubscribed())
              {
                folders.add(f);
              }
            }
            else
            {
              folders.add(f);
            }
          }
          break;
        }
      }
    }
    Collections.sort(folders, LEXOGRAPHICAL);
    return folders;
  }

  public List sublist(int type, String[] exfolders, boolean subscribed)
  {
    List folders = new ArrayList();
    for (Iterator iter = (Iterator)this.m_Folders.listIterator(); iter.hasNext(); )
    {
      WBMailFolder f = (WBMailFolder)iter.next();

      if (!StringUtil.contains(exfolders, f.getPath()))
      {
        switch (type)
        {
        case 10:
          if (subscribed)
          {
            if (f.isSubscribed())
            {
              folders.add(f);
            }
          }
          else
          {
            folders.add(f);
          }
          break;
        case 4:
          if ((f.isType(1)) || 
            (f.isType(3)))
          {
            if (subscribed)
            {
              if (f.isSubscribed())
              {
                folders.add(f);
              }
            }
            else
            {
              folders.add(f);
            }
          }
          break;
        case 5:
          if ((f.isType(2)) || 
            (f.isType(3)))
          {
            if (subscribed)
            {
              if (f.isSubscribed())
              {
                folders.add(f);
              }
            }
            else
            {
              folders.add(f);
            }
          }
          break;
        case 6:
        case 7:
        case 8:
        case 9:
        default:
          if (f.isType(type))
          {
            if (subscribed)
            {
              if (f.isSubscribed())
              {
                folders.add(f);
              }
            }
            else
            {
              folders.add(f);
            }
          }
          break;
        }
      }
    }
    Collections.sort(folders, LEXOGRAPHICAL);
    return folders;
  }

  public boolean contains(String path)
  {
    for (Iterator iter = iterator(); iter.hasNext(); )
    {
      if (path.equals(((WBMailFolder)iter.next()).getPath()))
      {
        return true;
      }
    }
    return false;
  }

  public boolean contains(WBMailFolder folder)
  {
    for (Iterator iter = iterator(); iter.hasNext(); )
    {
      if (folder.equals(iter.next()))
      {
        return true;
      }
    }
    return false;
  }

  public WBMailFolder[] createFolderArray(List folders)
  {
    WBMailFolder[] list = new WBMailFolder[folders.size()];
    return (WBMailFolder[])folders.toArray(list);
  }

  public void removeFolderFromList(String path)
  {
    for (Iterator iter = this.m_Folders.iterator(); iter.hasNext(); )
    {
      if (path.equals(((WBMailFolder)iter.next()).getPath()))
      {
        iter.remove();

        break;
      }
    }
  }

  public void addFolderToList(WBMailFolder folder)
  {
    this.m_Folders.add(folder);
    Collections.sort(this.m_Folders, LEXOGRAPHICAL);
  }

  public void rebuild()
    throws MessagingException, WBMailException
  {
    Folder[] folders = this.m_Folder.list(this.m_Pattern);

    this.m_Folders = new ArrayList(folders.length);
    buildFolderList(folders);
    Collections.sort(this.m_Folders, LEXOGRAPHICAL);
  }

  private void buildFolderList(Folder[] folders)
    throws WBMailException, MessagingException
  {
    for (int i = 0; i < folders.length; i++)
    {
      this.m_Folders.add(WBMailFolder.createLight(folders[i]));
    }
  }

  public static WBMailFolderList createStoreList(Folder folder)
    throws WBMailException
  {
    try
    {
      WBMailFolderList flist = new WBMailFolderList(folder, true);
      flist.rebuild();

      return flist;
    }
    catch (MessagingException mex)
    {
      throw new WBMailException(mex.getMessage()).setException(mex);
    }
  }

  public static WBMailFolderList createSubfolderList(Folder folder)
    throws WBMailException
  {
    try
    {
      WBMailFolderList flist = new WBMailFolderList(folder, false);
      flist.rebuild();
      return flist;
    }
    catch (MessagingException mex)
    {
      throw new WBMailException(mex.getMessage()).setException(mex);
    }
  }
}
