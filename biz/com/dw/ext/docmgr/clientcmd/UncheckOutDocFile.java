package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocFileItem;
import com.dw.ext.docmgr.AbstractDocItem;
import com.dw.ext.docmgr.AbstractDocItem.State;
import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.system.gdb.GDB;
import com.dw.user.UserProfile;
import java.util.HashMap;

public class UncheckOutDocFile extends DocCmder
{
  public UncheckOutDocFile(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "uncheckout_docfile";
  }

  protected boolean docDocCmd(UserProfile up, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson)
    throws Exception
  {
    long docid = checkGetDocId(parms);

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

    if ((up == null) || (!up.getUserName().equals(di.getLockUserName())))
    {
      failedreson.append("当前用户不一致！");
      return false;
    }

    if (!this.docMgr.changeDocFileState(up, docid, AbstractDocItem.State.Normal, failedreson))
    {
      return false;
    }

    AbstractDocFileItem adfi = di.getLastDocFileItem();

    out_ret_parms.put("doc_file_date", adfi.getModifyDateStr());
    out_ret_parms.put("doc_file_name", di.getDocFileName());

    byte[] cont = GDB.getInstance().loadXORMFileCont(adfi.getClass(), "Content", adfi.getFileId());

    if (cont == null)
    {
      failedreson.append("找不到对应文件的内容");
      return false;
    }

    out_ret_files.put(di.getDocFileName(), cont);

    return true;
  }
}
