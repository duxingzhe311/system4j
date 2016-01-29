package com.dw.biz.platform;

import java.io.File;
import java.io.FileInputStream;

public class BizClassLoader extends ClassLoader
{
  String basePath = null;

  BizClassLoader(String basepath)
  {
    int lastpos = basepath.length() - 1;
    if ((basepath.charAt(lastpos) == '\\') || (basepath.charAt(lastpos) == '/'))
      this.basePath = basepath;
    else
      this.basePath = (basepath + '/');
  }

  BizClassLoader(String basepath, ClassLoader cl)
  {
    super(cl);

    int lastpos = basepath.length() - 1;
    if ((basepath.charAt(lastpos) == '\\') || (basepath.charAt(lastpos) == '/'))
      this.basePath = basepath;
    else
      this.basePath = (basepath + '/');
  }

  public Class findClass(String name)
  {
    byte[] b = loadClassData(name);
    return defineClass(name, b, 0, b.length);
  }

  private byte[] loadClassData(String name)
  {
    try
    {
      String name0 = name.replace('.', '/');

      String filepath = this.basePath + name0 + ".class";
      filepath = filePathLocaler(filepath);

      File f = new File(filepath);
      byte[] buf = new byte[(int)f.length()];
      FileInputStream fis = new FileInputStream(f);
      fis.read(buf);
      fis.close();
      return buf;
    }
    catch (Exception e)
    {
      throw new RuntimeException("\nClassLoader error=" + e.toString());
    }
  }

  static String filePathLocaler(String fp)
  {
    if (fp == null)
      return fp;
    if (fp.equals("")) {
      return "";
    }
    String ss = System.getProperty("file.separator");
    char c = ss.charAt(0);
    if (c == '\\')
      return fp.replace('/', c);
    if (c == '/') {
      return fp.replace('\\', c);
    }
    return fp;
  }
}
