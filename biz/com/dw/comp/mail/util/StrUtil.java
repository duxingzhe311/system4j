package com.dw.comp.mail.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

public class StrUtil
{
  public static String[] compart_KeyWord(String keywords, String separator)
  {
    StringTokenizer st = new StringTokenizer(keywords, separator);
    int tokenizernumber = st.countTokens();
    String[] keyWords = new String[tokenizernumber];
    int i = 0;
    while (st.hasMoreTokens())
    {
      try
      {
        keyWords[i] = st.nextToken();
      }
      catch (NoSuchElementException localNoSuchElementException)
      {
      }
      i++;
    }
    return keyWords;
  }

  public static String link_KeyWord(String[] keywords, String separator)
  {
    String result = "";

    if ((keywords == null) || (keywords.length == 0))
    {
      return result;
    }
    for (int i = 0; i < keywords.length; i++)
    {
      if (keywords.length == 1)
      {
        result = keywords[0];
      }
      else if (i < keywords.length - 1)
      {
        result = result + keywords[i] + separator;
      }
      else
      {
        result = result + keywords[i];
      }
    }
    return result;
  }

  public static String plainToHtml(String input)
  {
    if (input == null)
    {
      return "";
    }

    char[] array = input.toCharArray();

    StringBuffer buf = new StringBuffer(array.length + array.length / 2);

    for (int i = 0; i < array.length; i++)
    {
      switch (array[i])
      {
      case '<':
        buf.append("&lt;");
        break;
      case '>':
        buf.append("&gt;");
        break;
      case '&':
        buf.append("&amp;");
        break;
      case '\n':
        buf.append("<br/>");
        break;
      case ' ':
        buf.append("&nbsp;");
        break;
      case '\'':
        buf.append("''");
      default:
        buf.append(array[i]);
      }
    }

    return buf.toString();
  }

  public static String plainToLineHtml(String input)
  {
    return plainToLineHtml(input, 2147483647);
  }

  public static String plainToLineHtml(String input, int maxlen)
  {
    if (input == null)
    {
      return "";
    }

    char[] array = input.toCharArray();

    StringBuffer buf = new StringBuffer(array.length + array.length / 2);

    for (int i = 0; (i < array.length) && (i < maxlen); i++)
    {
      switch (array[i])
      {
      case '<':
        buf.append("&lt;");
        break;
      case '>':
        buf.append("&gt;");
        break;
      case '&':
        buf.append("&amp;");
        break;
      case '\r':
        break;
      case '\n':
        buf.append(" ");
        break;
      case ' ':
        buf.append("&nbsp;");
        break;
      case '\'':
        buf.append("''");
      default:
        buf.append(array[i]);
      }

    }

    if ((i == maxlen) && (i != array.length))
    {
      buf.append("...");
    }

    return buf.toString();
  }

  public static String filterString(String input)
  {
    if (input == null)
    {
      return "";
    }

    char[] array = input.toCharArray();

    StringBuffer buf = new StringBuffer(array.length + array.length / 2);

    for (int i = 0; i < array.length; i++)
    {
      switch (array[i])
      {
      case '<':
        buf.append("&lt;");
        break;
      case '>':
        buf.append("&gt;");
        break;
      case '&':
        buf.append("&amp;");
        break;
      case '\n':
        buf.append("<br/>");
        break;
      case ' ':
        buf.append("&nbsp;");
        break;
      case '\'':
        buf.append("''");
      default:
        buf.append(array[i]);
      }
    }

    return buf.toString();
  }

  public static String parseDateString(Date myDate)
  {
    SimpleDateFormat formatter = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss");
    String dateString = formatter.format(myDate);

    return dateString;
  }

  public static String filterChars(String s, char[] chars)
  {
    if (s == null)
    {
      return "";
    }
    String result = "";
    for (int i = 0; i < s.length(); i++)
    {
      char ch = s.charAt(i);
      if (!inSet(chars, ch))
      {
        result = result + ch;
      }
    }
    return result;
  }

  public static boolean inSet(char[] chars, char ch)
  {
    for (int i = 0; i < chars.length; i++)
    {
      if (chars[i] == ch)
      {
        return true;
      }
    }
    return false;
  }

  public static String deleteSpace(String source)
  {
    String tmp = "";

    for (int i = 0; i < source.length(); i++)
    {
      if (source.charAt(i) != ' ')
      {
        tmp = tmp + source.charAt(i);
      }
    }
    return tmp;
  }

  public static String replaceString(String source, String oldStr, String newStr)
  {
    String result = "";
    String rightStr = source;

    int leftPos = -1;
    int rightPos = 0;

    while ((leftPos = rightStr.indexOf(oldStr)) != -1)
    {
      rightPos = leftPos + oldStr.length();
      result = result + rightStr.substring(0, leftPos) + newStr;
      rightStr = rightStr.substring(rightPos);
    }

    if (!rightStr.equals(""))
    {
      result = result + rightStr;
    }

    return result;
  }

  private static int disLen(String s)
  {
    if (s == null)
    {
      return 0;
    }
    int l = s.length();
    int r = 0;
    for (int i = 0; i < l; i++)
    {
      if (s.charAt(i) <= 'ÿ')
      {
        r++;
      }
      else
      {
        r += 2;
      }
    }
    return r;
  }

  private static String disSubstring(String s, int len)
  {
    if (s == null)
    {
      return "";
    }
    int l = s.length();
    StringBuffer sb = new StringBuffer(len);
    int r = 0;
    for (int i = 0; (i < l) && (r < len); i++)
    {
      sb.append(s.charAt(i));
      if (s.charAt(i) <= 'ÿ')
      {
        r++;
      }
      else
      {
        r += 2;
      }
    }
    return sb.toString();
  }

  public static String fitHTM(String s, int len)
  {
    if (s == null)
    {
      return "";
    }
    int oldlen = s.length();

    s = disSubstring(s, len * 2);
    if (s.length() < oldlen)
    {
      s = s + "...";
    }

    s = replaceString(s, "&nbsp;", " ");

    return s;
  }

  public static boolean empty(String s)
  {
    if ((s == null) || (s.equals("")))
    {
      return true;
    }

    return false;
  }

  public static String composeStringArray(String[] s, char div)
  {
    if ((s == null) || (s.length == 0))
      return "";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length; i++)
    {
      sb.append(s[i]).append(div);
    }
    return sb.toString();
  }

  public static String composeIntStringArray(int[] s, char div)
  {
    if ((s == null) || (s.length == 0))
      return "";
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length; i++)
    {
      sb.append(s[i]).append(div);
    }
    return sb.toString();
  }

  public static String[] splitStringToArray(String s, char div)
  {
    if ((s == null) || (s.equals("")))
      return null;
    StringTokenizer st = new StringTokenizer(s, div);
    int c = st.countTokens();
    if (c <= 0)
      return null;
    String[] rets = new String[c];
    for (int i = 0; i < c; i++)
      rets[i] = st.nextToken();
    return rets;
  }

  public static int[] splitStringToIntArray(String s, char div)
  {
    if ((s == null) || (s.equals("")))
      return null;
    StringTokenizer st = new StringTokenizer(s, div);
    int c = st.countTokens();
    if (c <= 0)
      return null;
    int[] rets = new int[c];
    for (int i = 0; i < c; i++)
      rets[i] = Integer.parseInt(st.nextToken());
    return rets;
  }

  public static String readLine(InputStream is)
    throws IOException
  {
    StringBuffer sb = new StringBuffer();
    int c;
    while ((c = is.read()) > 0)
    {
      int c;
      if (c == 13)
      {
        is.read();
        return sb.toString();
      }

      sb.append((char)c);
    }

    if (sb.length() > 0)
      return sb.toString();
    return null;
  }

  public static String readLine(InputStream is, String encoding)
    throws IOException
  {
    Vector v = new Vector();
    int c;
    while ((c = is.read()) > 0)
    {
      int c;
      if (c == 13)
      {
        is.read();
        return byteBuffToString(v, encoding);
      }

      v.addElement(new Byte((byte)c));
    }

    if (v.size() > 0)
      return byteBuffToString(v, encoding);
    return null;
  }

  private static String byteBuffToString(Vector v, String encoding)
    throws IOException
  {
    int s = v.size();
    byte[] buf = new byte[s];
    for (int i = 0; i < s; i++)
    {
      buf[i] = ((Byte)(Byte)v.elementAt(i)).byteValue();
    }
    return new String(buf, encoding);
  }
}
