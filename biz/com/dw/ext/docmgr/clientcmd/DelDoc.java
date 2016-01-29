package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.user.UserProfile;
import java.util.HashMap;

public class DelDoc extends DocCmder
{
  public DelDoc(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "del_doc";
  }

  protected boolean docDocCmd(UserProfile up, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson)
    throws Exception
  {
    long docid = checkGetDocId(parms);
    boolean bpermonent = "true".equalsIgnoreCase((String)parms.get("is_permonent"));

    return this.docMgr.removeDocItem(up, docid, bpermonent, failedreson);
  }
}
