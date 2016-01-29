package com.dw.ext.docmgr;

import com.dw.system.Convert;
import com.dw.user.LoginSession;
import com.dw.user.UserProfile;
import com.dw.web_ui.http.IHttpClientCmdHandler;
import java.util.HashMap;

public abstract class DocCmder
  implements IHttpClientCmdHandler
{
  public static final String PN_DOC_ID = "doc_id";
  public static final String PN_DOC_VER = "doc_ver";
  public static final String PN_DOC_FILE_EXT = "doc_file_ext";
  public static final String PN_DOC_FILE_ID = "doc_file_id";
  public static final String PN_DOC_FILE_NAME = "doc_file_name";
  public static final String PN_LOCAL_FILE_TIME = "local_file_time";
  public static final String PN_LOCAL_FILE_LEN = "local_file_len";
  public static final String PN_FILE_DATE = "doc_file_date";
  public static final String PN_LOCAL_FILE_VER = "local_file_ver";
  public static final String PN_IS_PERMONENT = "is_permonent";
  protected AbstractDocManager docMgr = null;

  public DocCmder(AbstractDocManager adm)
  {
    this.docMgr = adm;
  }

  public boolean doCmd(LoginSession ls, HashMap<String, String> parms, HashMap<String, byte[]> files, HashMap<String, String> out_ret_parms, HashMap<String, byte[]> out_ret_files, StringBuilder succ_message, StringBuilder failedreson) throws Exception
  {
    UserProfile up = new UserProfile(ls);
    return docDocCmd(up, parms, files, 
      out_ret_parms, out_ret_files, 
      succ_message, failedreson);
  }

  protected long checkGetDocId(HashMap<String, String> parms)
  {
    String strdoc = (String)parms.get("doc_id");
    if (Convert.isNullOrEmpty(strdoc)) {
      throw new IllegalArgumentException("no doc_id input!");
    }
    return Long.parseLong(strdoc);
  }

  protected abstract boolean docDocCmd(UserProfile paramUserProfile, HashMap<String, String> paramHashMap1, HashMap<String, byte[]> paramHashMap2, HashMap<String, String> paramHashMap3, HashMap<String, byte[]> paramHashMap4, StringBuilder paramStringBuilder1, StringBuilder paramStringBuilder2)
    throws Exception;
}
