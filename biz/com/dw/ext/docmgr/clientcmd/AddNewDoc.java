package com.dw.ext.docmgr.clientcmd;

import com.dw.user.LoginSession;
import java.util.HashMap;

public class AddNewDoc
{
  public static final String PN_ORGNODEID = "doc_orgnodeid";
  public static final String PN_FILENAME = "doc_filename";

  public boolean doCmd(LoginSession ls, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson)
  {
    return false;
  }
}
