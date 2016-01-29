package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocFileItem;
import com.dw.ext.docmgr.AbstractDocItem;
import com.dw.ext.docmgr.AbstractDocItem.State;
import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.system.Convert;
import com.dw.user.UserProfile;
import java.util.HashMap;

public class CheckInDocFile extends DocCmder
{
  public CheckInDocFile(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "checkin_docfile";
  }

  protected boolean docDocCmd(UserProfile up, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson)
    throws Exception
  {
    long docid = checkGetDocId(parms);
    String fn = (String)parms.get("doc_file_name");
    if (Convert.isNullOrEmpty(fn))
    {
      failedreson.append("No param name=doc_file_name");
      return false;
    }

    byte[] cont = (byte[])files.get(fn);
    if (cont == null)
    {
      failedreson.append("no file content with name=" + fn);
      return false;
    }

    String ver = (String)parms.get("doc_ver");

    AbstractDocItem di = this.docMgr.getDocItem(docid);
    if (di == null)
    {
      failedreson.append("没有找到该文件！");
      return false;
    }

    if (di.getState() != AbstractDocItem.State.Locked)
    {
      failedreson.append("文件没有被锁定！");
      return false;
    }

    if (!di.getLockUserName().equals(up.getUserName()))
    {
      failedreson.append("文件被用�? + di.getLockUserName() + "锁定，只能由锁定用户作签�?);
      return false;
    }

    if (!this.docMgr.addNewDocFileItem(up, docid, null, null, cont, ver, failedreson)) {
      return false;
    }
    if (!this.docMgr.changeDocFileState(up, docid, AbstractDocItem.State.Normal, failedreson))
    {
      return false;
    }

    di = this.docMgr.getDocItem(docid);
    out_ret_parms.put("doc_id", docid);

    out_ret_parms.put("doc_file_id", di.getLastDocFileItem().getFileId());
    out_ret_parms.put("doc_file_ext", di.getLastFileExt());

    out_ret_parms.put("doc_file_date", di.getLastDocFileItem().getModifyDateStr());

    return true;
  }
}
