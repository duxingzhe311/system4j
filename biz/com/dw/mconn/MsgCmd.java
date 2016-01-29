package com.dw.mconn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgCmd
{
  public static final int DEFAULT_PORT = 55322;
  String cmd;
  byte[] content;

  public static MsgCmd packCheckConnRight(String usern, String psw)
    throws Exception
  {
    if ((usern != null) && (!usern.equals("")))
    {
      if (usern.indexOf('\r') >= 0) {
        throw new IllegalArgumentException("user name cannot has \\r char");
      }
      if (psw == null) {
        psw = "";
      }
      String s = usern + "\r" + psw;
      byte[] cont = s.getBytes("UTF-8");
      return new MsgCmd("*", cont);
    }

    return new MsgCmd("*", null);
  }

  public static String[] unpackCheckConnRight(MsgCmd mc)
    throws Exception
  {
    if (!"*".equals(mc.getCmd())) {
      throw new IllegalArgumentException("not check conn cmd");
    }
    byte[] b = mc.getContent();
    if ((b == null) || (b.length <= 0)) {
      return null;
    }
    String s = new String(b, "UTF-8");
    int ri = s.indexOf('\r');
    if (ri <= 0) {
      throw new IllegalArgumentException("invalid check conn cmd");
    }
    return new String[] { s.substring(0, ri), s.substring(ri + 1) };
  }

  public static MsgCmd packCheckConnRightResult(boolean bsucc)
  {
    if (bsucc)
    {
      return new MsgCmd("*_succ", null);
    }

    return new MsgCmd("*_failed", null);
  }

  public static boolean unpackCheckConnRightResult(MsgCmd mc)
  {
    if ("*_succ".equals(mc.getCmd())) {
      return true;
    }
    return false;
  }

  public MsgCmd(String cmd, byte[] content)
  {
    if ((cmd.indexOf('\r') >= 0) || (cmd.indexOf('\n') >= 0)) {
      throw new IllegalArgumentException("name cannot has multi line");
    }
    this.cmd = cmd;
    this.content = content;
  }

  public String getCmd()
  {
    return this.cmd;
  }

  public byte[] getContent()
  {
    return this.content;
  }

  public void writeOut(OutputStream s)
    throws Exception
  {
    int contlen = -1;
    if (this.content != null) {
      contlen = this.content.length;
    }
    String cmdh = this.cmd + " " + contlen + '\n';
    byte[] bn = cmdh.getBytes("UTF-8");

    s.write(bn, 0, bn.length);
    if (contlen > 0)
      s.write(this.content, 0, this.content.length);
    s.flush();
  }

  public static MsgCmd readFrom(InputStream s)
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int b;
      while ((b = s.read()) != -1)
      {
        int b;
        if (b == 10)
        {
          break;
        }

        baos.write(b);
      }

      if (b == -1)
      {
        return null;
      }

      byte[] firstline = baos.toByteArray();
      String firstl = new String(firstline, "UTF-8");
      int p = firstl.lastIndexOf(' ');
      if (p <= 0) {
        throw new RuntimeException("MsgCmd Read From Stream Error: Invalid First Line");
      }
      String c = firstl.substring(0, p);
      int len = Integer.parseInt(firstl.substring(p + 1));

      byte[] cont = (byte[])null;
      if (len >= 0) {
        cont = new byte[len];
      }
      if (len > 0)
      {
        int x = 0;
        while (true)
        {
          int l = s.read(cont, x, len - x);
          if (l < 0)
            throw new RuntimeException("MsgCmd Read From Stream Error: read content(len=" + len + ") failed");
          x += l;
          if (x == len)
          {
            break;
          }
        }

      }

      return new MsgCmd(c, cont);
    }
    catch (IOException ode)
    {
    }
    return null;
  }
}
