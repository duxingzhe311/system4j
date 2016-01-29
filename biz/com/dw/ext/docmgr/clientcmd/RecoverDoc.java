package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.user.UserProfile;
import java.util.HashMap;

public class RecoverDoc extends DocCmder
{
  public RecoverDoc(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "recover_doc";
  }

  protected boolean docDocCmd(UserProfile up, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson)
    throws Exception
  {
    long docid = checkGetDocId(parms);

    return this.docMgr.recoverDeletedDoc(up, docid, null, failedreson);
  }
}
