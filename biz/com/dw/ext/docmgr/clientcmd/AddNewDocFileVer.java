package com.dw.ext.docmgr.clientcmd;

import com.dw.ext.docmgr.AbstractDocManager;
import com.dw.ext.docmgr.DocCmder;
import com.dw.system.Convert;
import com.dw.user.UserProfile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class AddNewDocFileVer extends DocCmder
{
  public AddNewDocFileVer(AbstractDocManager adm)
  {
    super(adm);
  }

  public String getCmdName()
  {
    return "add_new_docfile_ver";
  }

  protected boolean docDocCmd(UserProfile up, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson)
    throws Exception
  {
    long docid = checkGetDocId(parms);
    if (files.size() != 1)
    {
      failedreson.append("AddNewDocFileVer must has one file submit!");
      return false;
    }

    String fn = (String)parms.get("doc_file_name");
    if (Convert.isNullOrEmpty(fn))
    {
      failedreson.append("AddNewDocFileVer must has file name submit!");
      return false;
    }

    Iterator ir = files.keySet().iterator();
    String k = (String)ir.next();
    byte[] cont = (byte[])files.get(k);

    int p = fn.lastIndexOf('.');
    String fext = "";
    if (p > 0)
    {
      fext = fn.substring(p + 1);
      fn = fn.substring(0, p);
    }
    return this.docMgr.addNewDocFileItem(up, docid, fn, fext, cont, null, failedreson);
  }
}
