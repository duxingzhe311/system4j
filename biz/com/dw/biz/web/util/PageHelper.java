package com.dw.biz.web.util;

import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.XmlData;
import java.io.File;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class PageHelper
{
  static String webappDir = null;
  static Hashtable id2PageItem = new Hashtable();

  static
  {
    try
    {
      webappDir = System.getProperty("webapps.home");
      File df = new File(webappDir);
      System.out.println("find webapp deploy path=" + df.getCanonicalPath());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static String getWebappDir()
  {
    return webappDir;
  }

  public static String getBundleString(String key, HttpSession session)
  {
    return ResManager.getBundleString(key, 
      (String)session.getAttribute("lan"));
  }

  public static void setSessionLanguage(HttpSession session, String lan)
  {
    session.setAttribute("lan", lan);
  }

  public static void updateXmlDataFromHttpRequest(XmlData xd, HttpServletRequest req, String p_prefix)
    throws Exception
  {
    XORMUtil.updateXmlDataFromHttpRequest(xd, 
      req, p_prefix);
  }
}
