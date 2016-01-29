package com.dw.ext.docmgr;

import com.dw.ext.docmgr.clientcmd.AddNewDocFileVer;
import com.dw.ext.docmgr.clientcmd.ChangeDocFileName;
import com.dw.ext.docmgr.clientcmd.CheckInDocFile;
import com.dw.ext.docmgr.clientcmd.CheckOutDocFile;
import com.dw.ext.docmgr.clientcmd.DelDoc;
import com.dw.ext.docmgr.clientcmd.DownloadDocFile;
import com.dw.ext.docmgr.clientcmd.RecoverDoc;
import com.dw.ext.docmgr.clientcmd.UncheckOutDocFile;
import com.dw.ext.docmgr.clientcmd.ViewDocFile;
import com.dw.system.Convert;
import com.dw.system.gdb.DBResult;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GdbException;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.user.UserProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractDocManager
{
  HashMap<String, DocCmder> cmdn2cmd = new HashMap();

  private transient String docItemTable = null;

  private transient String docFileItemTable = null;

  static Hashtable docFileId2LockObj = new Hashtable();
  static Object fileLockObj = new Object();

  public AbstractDocManager()
  {
    setDocCmder(new AddNewDocFileVer(this));
    setDocCmder(new ViewDocFile(this));
    setDocCmder(new DownloadDocFile(this));
    setDocCmder(new CheckOutDocFile(this));
    setDocCmder(new CheckInDocFile(this));
    setDocCmder(new UncheckOutDocFile(this));
    setDocCmder(new ChangeDocFileName(this));
    setDocCmder(new DelDoc(this));
    setDocCmder(new RecoverDoc(this));
  }

  private void setDocCmder(DocCmder dc)
  {
    this.cmdn2cmd.put(dc.getCmdName(), dc);
  }

  public abstract Class getDocItemClass();

  public abstract Class getDocFileItemClass();

  public String getDocItemTable()
  {
    if (this.docItemTable != null) {
      return this.docItemTable;
    }
    Class c = getDocItemClass();
    XORMClass xormc = (XORMClass)c.getAnnotation(XORMClass.class);
    this.docItemTable = xormc.table_name();
    return this.docItemTable;
  }

  public String getDocFileItemTable()
  {
    if (this.docFileItemTable != null) {
      return this.docFileItemTable;
    }
    Class c = getDocFileItemClass();
    XORMClass xormc = (XORMClass)c.getAnnotation(XORMClass.class);
    this.docFileItemTable = xormc.table_name();
    return this.docFileItemTable;
  }

  public AbstractDocItem getDocItem(long docid)
    throws GdbException, Exception
  {
    AbstractDocItem ret = (AbstractDocItem)GDB.getInstance().getXORMObjByPkId(getDocItemClass(), Long.valueOf(docid));
    if (ret == null) {
      return null;
    }
    List ll = GDB.getInstance().listXORMAsObjList(getDocFileItemClass(), "DocId=" + docid, "ModifyDate desc", 0, 1);
    if ((ll != null) && (ll.size() == 1))
    {
      ret.lastDocFileItem = ((AbstractDocFileItem)ll.get(0));
    }

    ret.docMgr = this;
    return ret;
  }

  protected List<AbstractDocItem> listDocItemByWhereStr(String wherestr)
    throws Exception
  {
    Hashtable ht = new Hashtable();

    ht.put("$DocItemTable", getDocItemTable());
    ht.put("$DocFileItemTable", getDocFileItemTable());
    ht.put("$WhereStr", wherestr);
    DBResult dbr = GDB.getInstance().accessDBPage("DocMgr.ListDocItemByWhereStr", ht, 0, -1);
    List rets = (List)dbr.transTable2XORMObjList(0, getDocItemClass());
    List dfi = (List)dbr.transTable2XORMObjList(0, getDocFileItemClass());
    int s = rets.size();
    for (int i = 0; i < s; i++)
    {
      AbstractDocItem adi = (AbstractDocItem)rets.get(i);
      adi.docMgr = this;
      adi.lastDocFileItem = ((AbstractDocFileItem)dfi.get(i));
    }
    return rets;
  }

  public List<AbstractDocFileItem> listDocFileItem(long docid)
    throws Exception
  {
    return GDB.getInstance().listXORMAsObjList(getDocFileItemClass(), "DocId=" + docid, "ModifyDate desc", 0, -1);
  }

  public void addNewDocFile(String username, AbstractDocItem df, String fileext, byte[] cont)
    throws GdbException, Exception
  {
    if (username == null) {
      username = "";
    }
    GDB.getInstance().addXORMObjWithNewId(df);
    long newid = df.getDocId();
    AbstractDocFileItem dfi = (AbstractDocFileItem)getDocFileItemClass().newInstance();

    dfi.setDocId(newid);
    dfi.setUserName(username);
    dfi.setFileExt(fileext);
    dfi.setUserName(username);
    dfi.setVer(DocVer.createFirstAutoVer());
    dfi.setFileLen(cont.length);
    dfi.content = cont;

    GDB.getInstance().addXORMObjWithNewId(dfi);
  }

  Object getDocFileLockObj(long fileid)
  {
    Object o = docFileId2LockObj.get(Long.valueOf(fileid));
    if (o != null) {
      return o;
    }
    synchronized (fileLockObj)
    {
      o = docFileId2LockObj.get(Long.valueOf(fileid));
      if (o != null) {
        return o;
      }
      o = new Object();
      docFileId2LockObj.put(Long.valueOf(fileid), o);
      return o;
    }
  }

  public boolean addNewDocFileItem(UserProfile up, long docid, String fn, String fileext, byte[] content, String version, StringBuilder failedreson) throws GdbException, Exception
  {
    if (content == null)
    {
      failedreson.append("文件内容不能为null�?);
      return false;
    }

    synchronized (getDocFileLockObj(docid))
    {
      AbstractDocItem df = getDocItem(docid);
      if (df == null)
      {
        failedreson.append("没有对应的文档id=" + docid);
        return false;
      }

      AbstractDocItem.State st = df.getState();
      if (st == AbstractDocItem.State.Locked)
      {
        if (!up.getUserName().equals(df.getLockUserName()))
        {
          failedreson.append("文件已经被用�? + df.getLockUserName() + "锁定�?);
          return false;
        }
      }
      else if (st == AbstractDocItem.State.Freezed)
      {
        failedreson.append("文件已经被冻结！");
        return false;
      }

      DocVer dv = null;
      DocVer olddv = new DocVer(df.getLastFileVersion());
      if (Convert.isNullOrEmpty(version))
      {
        dv = olddv.autoIncrease();
      }
      else
      {
        dv = new DocVer(version);
        if (dv.compareTo(olddv) <= 0)
        {
          failedreson.append("新文档版本必须比现有的大�?);
          return false;
        }

      }

      AbstractDocFileItem dfi = (AbstractDocFileItem)getDocFileItemClass().newInstance();
      dfi.setDocId(docid);
      if (Convert.isNullOrEmpty(fileext))
      {
        fileext = df.getLastFileExt();
      }
      dfi.setFileExt(fileext);
      dfi.setVerstion(dv.toString());
      dfi.content = content;
      dfi.setFileLen(content.length);
      dfi.setUserName(up.getUserName());
      GDB.getInstance().addXORMObjWithNewId(dfi);
    }

    failedreson = null;
    return true;
  }

  public boolean changeDocFileState(UserProfile up, long docid, AbstractDocItem.State state, StringBuilder failedreson)
    throws Exception
  {
    if (up == null)
    {
      failedreson.append("no user profile!");
      return false;
    }

    synchronized (getDocFileLockObj(docid))
    {
      String username = up.getUserName();

      AbstractDocItem df = getDocItem(docid);
      if (df == null)
      {
        failedreson.append("cannot find DocItem with id=" + docid);
        return false;
      }

      if ((df.getState() == AbstractDocItem.State.Locked) && (!username.equals(df.getLockUserName())))
      {
        if (!up.isAdministrator())
        {
          failedreson.append("文件已经被用�? + df.getLockUserName() + "锁定");
          return false;
        }
      }

      if ((df.getState() == AbstractDocItem.State.Freezed) && (state == AbstractDocItem.State.Normal) && (!up.isAdministrator()))
      {
        failedreson.append("只有管理员才可以做文件解冻！");
        return false;
      }

      if ((df.getState() == AbstractDocItem.State.Delete) && (state == AbstractDocItem.State.Normal))
      {
        if (!up.isAdministrator())
        {
          failedreson.append("只有管理员才可以做文件恢复！");
          return false;
        }
      }

      String locku = "";
      if (state == AbstractDocItem.State.Locked) {
        locku = username;
      }
      Hashtable ht = new Hashtable();
      ht.put("@DocId", Long.valueOf(docid));
      ht.put("$DocItemTable", getDocItemTable());

      ht.put("@State", Integer.valueOf(state.getValue()));
      if (state == AbstractDocItem.State.Locked)
        ht.put("@LockUserName", username);
      else {
        ht.put("@LockUserName", "");
      }
      DBResult dbr = GDB.getInstance().accessDB("DocMgr.UpdateDocState", ht);

      if (dbr.getLastRowsAffected() != 1)
      {
        failedreson.append("ChangeDocFileState error when access io!");
        return false;
      }

      return true;
    }
  }

  public boolean removeDocItem(UserProfile up, long docid, boolean bpermonent, StringBuilder failedreson)
    throws Exception
  {
    synchronized (getDocFileLockObj(docid))
    {
      AbstractDocItem df = getDocItem(docid);
      if (df == null)
      {
        failedreson.append("cannot find DocFile with id=" + docid);
        return false;
      }

      if ((df.getState() == AbstractDocItem.State.Locked) && (!up.getUserName().equals(df.getLockUserName())))
      {
        failedreson.append("文件已经被用�? + df.getLockUserName() + "锁定�?);
        return false;
      }

      if (!bpermonent)
      {
        return changeDocFileState(up, docid, AbstractDocItem.State.Delete, failedreson);
      }

      List dfis = listDocFileItem(docid);

      if ((GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(docid), getDocItemClass())) && (dfis != null))
      {
        for (AbstractDocFileItem dfi : dfis)
        {
          GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(dfi.getFileId()), getDocFileItemClass());
        }

      }

    }

    failedreson = null;
    return true;
  }

  public List<Long> removeDocItemsPermonentByCond(String where_str)
    throws Exception
  {
    List ids = GDB.getInstance().listXORMPkIds(getDocItemClass(), where_str, null, 0, -1);
    if ((ids == null) || (ids.size() <= 0)) {
      return null;
    }
    ArrayList rets = new ArrayList(ids.size());
    Iterator localIterator2;
    label168: for (Iterator localIterator1 = ids.iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Object oid = localIterator1.next();

      long docid = ((Long)oid).longValue();

      List dfis = listDocFileItem(docid);

      if ((!GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(docid), getDocItemClass())) || (dfis == null))
        break label168;
      rets.add(Long.valueOf(docid));

      localIterator2 = dfis.iterator(); continue; AbstractDocFileItem dfi = (AbstractDocFileItem)localIterator2.next();

      GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(dfi.getFileId()), getDocFileItemClass());
    }

    return rets;
  }

  public boolean recoverDeletedDoc(UserProfile up, long docid, String newfilename, StringBuilder failedreson)
    throws Exception
  {
    synchronized (getDocFileLockObj(docid))
    {
      AbstractDocItem df = getDocItem(docid);
      if (df == null)
      {
        failedreson.append("cannot find DocFile with id=" + docid);
        return false;
      }

      if (df.getState() != AbstractDocItem.State.Delete)
      {
        failedreson.append("文件没有被删除！");
        return false;
      }

      if ((Convert.isNotNullEmpty(newfilename)) && (!df.getDocFileName().equals(newfilename)))
      {
        if (!changeDocFileName(up, docid, newfilename, failedreson)) {
          return false;
        }
      }

      if (!changeDocFileState(up, docid, AbstractDocItem.State.Normal, failedreson))
      {
        return false;
      }

      failedreson = null;
      return true;
    }
  }

  public boolean changeDocFileName(UserProfile up, long docid, String filename, StringBuilder failedreson)
    throws Exception
  {
    if (up == null)
    {
      failedreson.append("no user profile!");
      return false;
    }

    synchronized (getDocFileLockObj(docid))
    {
      String username = up.getUserName();

      AbstractDocItem df = getDocItem(docid);
      if (df == null)
      {
        failedreson.append("cannot find DocItem with id=" + docid);
        return false;
      }

      AbstractDocItem.State st = df.getState();

      if (st != AbstractDocItem.State.Normal)
      {
        if (!up.isAdministrator())
        {
          failedreson.append("file must in normal state!");
          return false;
        }

      }

      Hashtable ht = new Hashtable();
      ht.put("@DocId", Long.valueOf(docid));
      ht.put("$DocItemTable", getDocItemTable());

      ht.put("@DocFileName", filename);

      DBResult dbr = GDB.getInstance().accessDB("DocMgr.ChangeDocItemName", ht);

      if (dbr.getLastRowsAffected() != 1)
      {
        failedreson.append("ChangeDocFileName error when access io!");
        return false;
      }

      return true;
    }
  }

  public DocCmder getDocCmder(String doc_cmd_name)
  {
    return (DocCmder)this.cmdn2cmd.get(doc_cmd_name);
  }
}
