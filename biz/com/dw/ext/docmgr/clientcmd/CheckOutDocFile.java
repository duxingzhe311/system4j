package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocFileItem;
import com.dw.ext.docmgr.AbstractDocItem;
import com.dw.ext.docmgr.AbstractDocItem.State;
import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.user.UserProfile;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CheckOutDocFile extends DocCmder
{
  public CheckOutDocFile(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "checkout_docfile";
  }

  protected boolean docDocCmd(UserProfile up, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson)
    throws Exception
  {
    long docid = checkGetDocId(parms);

    String ver = (String)parms.get("doc_ver");
    AbstractDocItem di = this.docMgr.getDocItem(docid);
    if (di == null)
    {
      failedreson.append("no doc item found with id=" + docid);
      return false;
    }

    if (di.getState() != AbstractDocItem.State.Normal)
    {
      failedreson.append("文件已经被锁定！");
      return false;
    }

    String strLocalFileTime = (String)parms.get("local_file_time");
    String strLocalFileLen = (String)parms.get("local_file_len");
    String local_ver = "";

    if ((!Convert.isNullOrEmpty(strLocalFileTime)) && (!Convert.isNullOrEmpty(strLocalFileLen)))
    {
      Date dt = Convert.toCalendar(strLocalFileTime).getTime();
      long flen = Long.parseLong(strLocalFileLen);
      AbstractDocFileItem dfi = di.getExistedVerFile(dt, flen);
      if (dfi != null) {
        local_ver = dfi.getVersion();
      }
    }
    if (!this.docMgr.changeDocFileState(up, docid, AbstractDocItem.State.Locked, failedreson)) {
      return false;
    }

    out_ret_parms.put("doc_file_date", di.getLastDocFileItem().getModifyDateStr());
    out_ret_parms.put("local_file_ver", local_ver);

    AbstractDocFileItem adfi = di.getLastDocFileItem();
    byte[] cont = GDB.getInstance().loadXORMFileCont(adfi.getClass(), "Content", adfi.getFileId());

    if (cont == null)
    {
      failedreson.append("找不到对应文件的内容");
      return false;
    }

    out_ret_parms.put("doc_id", docid);

    out_ret_parms.put("doc_file_id", adfi.getFileId());
    out_ret_parms.put("doc_file_ext", adfi.getFileExt());

    byte[] fcont = GDB.getInstance().loadXORMFileCont(adfi.getClass(), "Content", adfi.getFileId());

    String fn = adfi.getFileId() + "." + adfi.getFileExt();
    out_ret_parms.put("doc_file_name", fn);
    out_ret_parms.put("doc_file_date", adfi.getModifyDateStr());
    out_ret_files.put(fn, fcont);
    return true;
  }
}
