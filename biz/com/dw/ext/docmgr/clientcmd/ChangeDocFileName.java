package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.system.Convert;
import com.dw.user.UserProfile;
import java.util.HashMap;

public class ChangeDocFileName extends DocCmder
{
  public ChangeDocFileName(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "change_docfile_name";
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

    return this.docMgr.changeDocFileName(up, docid, fn, failedreson);
  }
}
