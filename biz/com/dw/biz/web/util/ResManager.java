package com.dw.biz.web.util;

import com.dw.biz.api.util.Prop;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class ResManager
{
  private static Hashtable RESBUNDLE_MAP = new Hashtable();

  private static String[] allLanguageNames = null;

  private static ResourceBundle basebundle = null;

  private static ResourceBundle defaultbundle = null;

  private static boolean bInit = false;

  private static void init()
  {
    try
    {
      Locale len = new Locale("en", "US");
      RESBUNDLE_MAP.put(len.toString(), ResourceBundle.getBundle(
        "com.dw.biz.web.util.bundle.bizweb", len));

      Locale lzh = new Locale("zh", "CN");
      RESBUNDLE_MAP.put(lzh.toString(), ResourceBundle.getBundle(
        "com.dw.biz.web.util.bundle.bizweb", lzh));

      Locale lja = new Locale("ja", "JP");
      RESBUNDLE_MAP.put(lja.toString(), ResourceBundle.getBundle(
        "com.dw.biz.web.util.bundle.bizweb", lja));

      Locale ltw = Locale.TRADITIONAL_CHINESE;
      RESBUNDLE_MAP.put(ltw.toString(), ResourceBundle.getBundle(
        "com.dw.biz.web.util.bundle.bizweb", ltw));

      allLanguageNames = new String[] { len.toString(), lzh.toString(), 
        lja.toString(), ltw.toString() };

      defaultbundle = (ResourceBundle)RESBUNDLE_MAP.get(
        Locale.getDefault().toString());
      if (defaultbundle == null)
      {
        defaultbundle = (ResourceBundle)RESBUNDLE_MAP.get(
          len.toString());
      }

      basebundle = (ResourceBundle)RESBUNDLE_MAP.get(len.toString());

      bInit = true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static String[] getAllLanguageNames()
  {
    if (!bInit)
      init();
    return allLanguageNames;
  }

  public static String getLanguageDesc(String lang_name)
  {
    if (!bInit) {
      init();
    }
    if ((lang_name != null) && (!lang_name.equals("")))
    {
      return getBundleString("ResBundle.language." + lang_name);
    }

    return null;
  }

  public static Locale getLocale()
  {
    if (!bInit) {
      init();
    }

    return defaultbundle.getLocale();
  }

  public static boolean setResBundle(String n)
  {
    if (!bInit) {
      init();
    }
    ResourceBundle ob = defaultbundle;
    defaultbundle = (ResourceBundle)RESBUNDLE_MAP.get(n);
    if (defaultbundle == null)
    {
      defaultbundle = ob;
      return false;
    }

    return true;
  }

  public static void setResBundleByLanguage(String n)
  {
    if (!bInit) {
      init();
    }
    setResBundle(n);
  }

  public static String getBundleString(String key)
  {
    if (!bInit) {
      init();
    }
    return getBundleString(key, null);
  }

  public static String getBundleString(String key, String lan)
  {
    if (!bInit) {
      init();
    }
    String value = null;
    ResourceBundle bd = null;
    if (lan != null)
    {
      bd = (ResourceBundle)RESBUNDLE_MAP.get(lan);
    }

    if (bd == null) {
      bd = defaultbundle;
    }
    try
    {
      value = bd.getString(key);
    }
    catch (MissingResourceException e)
    {
      if ((value == null) && (basebundle != bd))
      {
        try
        {
          value = basebundle.getString(key);
        }
        catch (MissingResourceException localMissingResourceException1)
        {
        }
      }

    }

    if (value == null)
    {
      value = "[X]" + key + "[X]";
    }
    return value;
  }

  private static Hashtable divideProp(Prop sorp)
  {
    Hashtable ht = new Hashtable();
    StringTokenizer st;
    for (Enumeration en = sorp.propertyNames(); en.hasMoreElements(); 
      st.hasMoreTokens())
    {
      String n = (String)en.nextElement();
      String v = sorp.getProperty(n);
      System.out.println("diving->" + v);
      st = new StringTokenizer(v, "|");
      continue;

      String nt = st.nextToken();
      String lan = null;
      String val = null;
      if (nt.startsWith("<"))
      {
        int p = nt.indexOf('>');
        lan = nt.substring(1, p);
        val = nt.substring(p + 1);
      }
      else
      {
        val = nt;
      }

      if (lan == null)
      {
        lan = "_en_US";
      }
      else
      {
        lan = "_" + lan;
      }

      Prop tmpp = (Prop)ht.get(lan);
      if (tmpp == null)
      {
        tmpp = new Prop();
        ht.put(lan, tmpp);
      }

      tmpp.setProperty(n, val);
    }

    return ht;
  }

  private static void concreteProp(Hashtable afterdiv)
  {
    for (Enumeration en = afterdiv.elements(); en.hasMoreElements(); )
    {
      Prop ps = (Prop)en.nextElement();
      concrete(ps);
    }
  }

  private static void concrete(Prop ps)
  {
    for (Enumeration en = ps.propertyNames(); en.hasMoreElements(); )
    {
      String n = (String)en.nextElement();
      String v = ps.getProperty(n);

      int i = 0;
      while (true)
      {
        i = v.indexOf('%', i);
        if (i < 0)
          break;
        int j = v.indexOf('%', i + 1);
        if (j < 0) {
          break;
        }
        String inn = v.substring(i + 1, j);
        String inv = ps.getProperty(inn);
        if (inv != null)
        {
          v = v.substring(0, i) + inv + v.substring(j + 1);

          i = j + 1;
          System.out.println("concrete " + n + "=" + v);
          ps.setProperty(n, v);
        }
      }
    }
  }

  public static void createResBundleFile(String sorfile, String encoding, String tardir, String tarresname) throws Exception
  {
    FileInputStream fis = null;
    try
    {
      fis = new FileInputStream(sorfile);
      Prop sorp = new Prop();
      sorp.load(fis, encoding);

      Hashtable ht = divideProp(sorp);
      concreteProp(ht);

      for (Enumeration en = ht.keys(); en.hasMoreElements(); )
      {
        String lan = (String)en.nextElement();
        Prop tmpp = (Prop)ht.get(lan);

        FileOutputStream fos = null;
        try
        {
          String tarfile = tardir + "/" + tarresname + lan + 
            ".properties";

          System.out.println("create tar file=" + tarfile);
          fos = new FileOutputStream(tarfile);

          tmpp.store(fos, "");
        }
        finally
        {
          if (fos != null)
          {
            fos.close();
          }
        }
      }
    }
    finally
    {
      if (fis != null)
      {
        fis.close();
      }
    }
  }

  public static void main(String[] args) throws Exception
  {
    createResBundleFile(args[0], args[1], args[2], args[3]);
  }
}
