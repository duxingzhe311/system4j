package com.dw.comp.webmail;

import com.dw.system.Convert;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class WebMailAddr
{
  String name = null;
  String email = null;

  public WebMailAddr()
  {
  }

  public WebMailAddr(String n, String em) {
    this.name = n;
    this.email = em;
  }

  public String getName()
  {
    return this.name;
  }

  public String getEmail()
  {
    return this.email;
  }

  private static String checkEmailStr(String str)
  {
    if ((str.indexOf("@") < 0) && (str.indexOf("ï¼?) < 0)) {
      return null;
    }
    str = Convert.toBj(str);

    return str;
  }

  public static WebMailAddr transIntenetAddr(String address)
  {
    if (Convert.isNullOrTrimEmpty(address))
      return null;
    StringTokenizer st = new StringTokenizer(address.trim(), "[]<>()\t'");
    int ct = st.countTokens();
    if (ct <= 0) {
      return null;
    }
    String mails = "";
    String personal = "";
    if (ct == 1)
    {
      mails = st.nextToken();

      String ss = checkEmailStr(mails);
      if (ss == null) {
        return null;
      }
      mails = ss;
    }

    if (ct >= 2)
    {
      ArrayList pns = new ArrayList();

      while (st.hasMoreTokens())
      {
        String ss0 = st.nextToken();
        if (!Convert.isNullOrTrimEmpty(ss0))
        {
          String ss1 = checkEmailStr(ss0);
          if (ss1 != null)
            mails = ss1;
          else
            pns.add(ss0);
        }
      }
      if (Convert.isNullOrEmpty(mails)) {
        return null;
      }
      if (pns.size() > 0) {
        personal = (String)pns.get(0);
      }
    }
    return new WebMailAddr(personal, mails);
  }
}
