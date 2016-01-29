package com.dw.biz.api.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class Prop extends Hashtable
{
  private static final long serialVersionUID = 4112578634029874840L;
  protected Prop defaults;
  private static final String keyValueSeparators = "=: \t\r\n\f";
  private static final String strictKeyValueSeparators = "=:";
  private static final String specialSaveChars = "=: \t\r\n\f#!";
  private static final String whiteSpaceChars = " \t\r\n\f";
  private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', 
    '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  public Prop()
  {
    this(null);
  }

  public Prop(Prop defaults)
  {
    this.defaults = defaults;
  }

  public static Prop getFromProperties(Properties p)
  {
    Prop rp = new Prop();
    for (Enumeration en = p.propertyNames(); en.hasMoreElements(); )
    {
      String key = (String)en.nextElement();
      rp.setProperty(key, p.getProperty(key));
    }

    return rp;
  }

  public synchronized Object setProperty(String key, String value)
  {
    return put(key, value);
  }

  public synchronized void load(InputStream inStream)
    throws IOException
  {
    if (System.getProperty("file.encoding") != null)
      load(inStream, System.getProperty("file.encoding"));
    else
      load(inStream, System.getProperty("GBK"));
  }

  public synchronized void load(InputStream inStream, String encoding)
    throws IOException
  {
    BufferedReader in = new BufferedReader(new InputStreamReader(inStream, 
      encoding));
    while (true)
    {
      String line = in.readLine();
      if (line == null) {
        return;
      }
      if (line.length() > 0)
      {
        char firstChar = line.charAt(0);
        if ((firstChar != '#') && (firstChar != '!'))
        {
          while (continueLine(line))
          {
            String nextLine = in.readLine();
            if (nextLine == null)
              nextLine = "";
            String loppedLine = line
              .substring(0, line.length() - 1);

            int startIndex = 0;
            for (startIndex = 0; startIndex < nextLine.length(); startIndex++)
              if (" \t\r\n\f".indexOf(
                nextLine.charAt(startIndex)) == -1)
                break;
            nextLine = nextLine.substring(startIndex, 
              nextLine.length());
            line = new String(loppedLine + nextLine);
          }

          int len = line.length();

          for (int keyStart = 0; keyStart < len; keyStart++)
          {
            if (" \t\r\n\f".indexOf(line.charAt(keyStart)) == -1)
            {
              break;
            }
          }
          if (keyStart != len)
          {
            for (int separatorIndex = keyStart; separatorIndex < len; separatorIndex++)
            {
              char currentChar = line.charAt(separatorIndex);
              if (currentChar == '\\')
                separatorIndex++;
              else if ("=: \t\r\n\f".indexOf(currentChar) != -1)
                {
                  break;
                }
            }

            for (int valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
              if (" \t\r\n\f".indexOf(line.charAt(valueIndex)) == -1) {
                break;
              }
            }
            if ((valueIndex < len) && 
              ("=:".indexOf(
              line.charAt(valueIndex)) != -1)) {
              valueIndex++;
            }

            while (valueIndex < len)
            {
              if (" \t\r\n\f".indexOf(line.charAt(valueIndex)) == -1)
                break;
              valueIndex++;
            }
            String key = line.substring(keyStart, separatorIndex);
            String value = separatorIndex < len ? line
              .substring(valueIndex, len) : "";

            key = loadConvert(key);
            value = loadConvert(value);
            put(key, value);
          }
        }
      }
    }
  }

  private boolean continueLine(String line)
  {
    int slashCount = 0;
    int index = line.length() - 1;
    while ((index >= 0) && (line.charAt(index--) == '\\'))
    {
      slashCount++;
    }
    return slashCount % 2 == 1;
  }

  private String loadConvert(String theString)
  {
    int len = theString.length();
    StringBuffer outBuffer = new StringBuffer(len);

    for (int x = 0; x < len; )
    {
      char aChar = theString.charAt(x++);
      if (aChar == '\\')
      {
        aChar = theString.charAt(x++);
        if (aChar == 'u')
        {
          int value = 0;
          for (int i = 0; i < 4; i++)
          {
            aChar = theString.charAt(x++);
            switch (aChar)
            {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              value = (value << 4) + aChar - 48;
              break;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
              value = (value << 4) + 10 + aChar - 97;
              break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
              value = (value << 4) + 10 + aChar - 65;
              break;
            case ':':
            case ';':
            case '<':
            case '=':
            case '>':
            case '?':
            case '@':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '[':
            case '\\':
            case ']':
            case '^':
            case '_':
            case '`':
            default:
              throw new IllegalArgumentException(
                "Malformed \\uxxxx encoding.");
            }
          }
          outBuffer.append((char)value);
        }
        else
        {
          if (aChar == 't')
          {
            aChar = '\t';
          }
          else if (aChar == 'r')
          {
            aChar = '\r';
          }
          else if (aChar == 'n')
          {
            aChar = '\n';
          }
          else if (aChar == 'f')
          {
            aChar = '\f';
          }
          outBuffer.append(aChar);
        }
      }
      else
      {
        outBuffer.append(aChar);
      }
    }
    return outBuffer.toString();
  }

  private String saveConvert(String theString, boolean escapeSpace)
  {
    int len = theString.length();
    StringBuffer outBuffer = new StringBuffer(len * 2);

    for (int x = 0; x < len; x++)
    {
      char aChar = theString.charAt(x);
      switch (aChar)
      {
      case ' ':
        if ((x == 0) || (escapeSpace))
        {
          outBuffer.append('\\');
        }

        outBuffer.append(' ');
        break;
      case '\\':
        outBuffer.append('\\');
        outBuffer.append('\\');
        break;
      case '\t':
        outBuffer.append('\\');
        outBuffer.append('t');
        break;
      case '\n':
        outBuffer.append('\\');
        outBuffer.append('n');
        break;
      case '\r':
        outBuffer.append('\\');
        outBuffer.append('r');
        break;
      case '\f':
        outBuffer.append('\\');
        outBuffer.append('f');
        break;
      default:
        if ((aChar < ' ') || (aChar > '~'))
        {
          outBuffer.append('\\');
          outBuffer.append('u');
          outBuffer.append(toHex(aChar >> '\f' & 0xF));
          outBuffer.append(toHex(aChar >> '\b' & 0xF));
          outBuffer.append(toHex(aChar >> '\004' & 0xF));
          outBuffer.append(toHex(aChar & 0xF));
        }
        else
        {
          if ("=: \t\r\n\f#!".indexOf(aChar) != -1)
          {
            outBuffer.append('\\');
          }
          outBuffer.append(aChar);
        }break;
      }
    }
    return outBuffer.toString();
  }

  /** @deprecated */
  public synchronized void save(OutputStream out, String header)
  {
    try
    {
      store(out, header);
    }
    catch (IOException localIOException)
    {
    }
  }

  public synchronized void store(OutputStream out, String header)
    throws IOException
  {
    BufferedWriter awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
    if (header != null)
    {
      writeln(awriter, "#" + header);
    }
    writeln(awriter, "#" + new Date().toString());
    for (Enumeration e = keys(); e.hasMoreElements(); )
    {
      String key = (String)e.nextElement();
      String val = (String)get(key);
      key = saveConvert(key, true);

      val = saveConvert(val, false);
      writeln(awriter, key + "=" + val);
    }
    awriter.flush();
  }

  private static void writeln(BufferedWriter bw, String s) throws IOException
  {
    bw.write(s);
    bw.newLine();
  }

  public String getProperty(String key)
  {
    Object oval = super.get(key);
    String sval = (oval instanceof String) ? (String)oval : null;
    return (sval == null) && (this.defaults != null) ? 
      this.defaults.getProperty(key) : sval;
  }

  public String getProperty(String key, String defaultValue)
  {
    String val = getProperty(key);
    return val == null ? defaultValue : val;
  }

  public Enumeration propertyNames()
  {
    Hashtable h = new Hashtable();
    enumerate(h);
    return h.keys();
  }

  public void list(PrintStream out)
  {
    out.println("-- listing Prop --");
    Hashtable h = new Hashtable();
    enumerate(h);
    for (Enumeration e = h.keys(); e.hasMoreElements(); )
    {
      String key = (String)e.nextElement();
      String val = (String)h.get(key);
      if (val.length() > 40)
      {
        val = val.substring(0, 37) + "...";
      }
      out.println(key + "=" + val);
    }
  }

  public void list(PrintWriter out)
  {
    out.println("-- listing Prop --");
    Hashtable h = new Hashtable();
    enumerate(h);
    for (Enumeration e = h.keys(); e.hasMoreElements(); )
    {
      String key = (String)e.nextElement();
      String val = (String)h.get(key);
      if (val.length() > 40)
      {
        val = val.substring(0, 37) + "...";
      }
      out.println(key + "=" + val);
    }
  }

  private synchronized void enumerate(Hashtable h)
  {
    if (this.defaults != null)
    {
      this.defaults.enumerate(h);
    }
    for (Enumeration e = keys(); e.hasMoreElements(); )
    {
      String key = (String)e.nextElement();
      h.put(key, get(key));
    }
  }

  private static char toHex(int nibble)
  {
    return hexDigit[(nibble & 0xF)];
  }
}
