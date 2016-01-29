package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocFileItem;
import com.dw.ext.docmgr.AbstractDocItem;
import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.system.gdb.GDB;
import com.dw.user.UserProfile;
import java.util.HashMap;

public class ViewDocFile extends DocCmder
{
  public ViewDocFile(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "view_docfile";
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

    AbstractDocFileItem adfi = di.getLastDocFileItem();

    out_ret_parms.put("doc_id", docid);

    out_ret_parms.put("doc_file_id", adfi.getFileId());
    out_ret_parms.put("doc_file_ext", adfi.getFileExt());

    byte[] fcont = GDB.getInstance().loadXORMFileCont(adfi.getClass(), "Content", adfi.getFileId());

    String fn = adfi.getFileId() + "." + adfi.getFileExt();
    out_ret_parms.put("doc_file_name", fn);
    out_ret_files.put(fn, fcont);
    return true;
  }
}
