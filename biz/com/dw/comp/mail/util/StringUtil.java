package com.dw.comp.mail.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class StringUtil
{
  public static SimpleDateFormat DEFAULT_DATEFORMAT = new SimpleDateFormat("E, dd.MMM yyyy hh:mm:ss (z)");

  public static String[] split(String str, String delim)
  {
    StringTokenizer strtok = new StringTokenizer(str, delim);
    String[] result = new String[strtok.countTokens()];

    for (int i = 0; i < result.length; i++) {
      result[i] = strtok.nextToken();
    }

    return result;
  }

  public static String join(String[] items, String delim)
  {
    StringBuffer sbuf = new StringBuffer();
    for (int i = 0; i < items.length; i++) {
      sbuf.append(items[i]);
      if (i < items.length - 1) {
        sbuf.append(delim);
      }
    }
    return sbuf.toString();
  }

  public static String repairPath(String path)
  {
    int idx = -1;
    String newpath = "";

    idx = path.indexOf(File.separator + ".." + File.separator);
    if (idx > 0) {
      newpath = path.substring(0, idx);
      newpath = newpath.substring(0, newpath.lastIndexOf(File.separator));
      newpath = newpath + path.substring(idx + 3, path.length());
      return newpath;
    }
    return path;
  }

  public static boolean contains(String[] strs, String str)
  {
    for (int i = 0; i < strs.length; i++) {
      if (str.equals(strs[i])) {
        return true;
      }
    }
    return false;
  }

  public static String getFormattedDate()
  {
    return DEFAULT_DATEFORMAT.format(new Date());
  }
}
