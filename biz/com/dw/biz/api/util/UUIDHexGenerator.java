package com.dw.biz.api.util;

import java.io.PrintStream;
import java.io.Serializable;

class UUIDHexGenerator extends UUIDGenerator
{
  private final String sep;

  public UUIDHexGenerator()
  {
    this.sep = "";
  }

  public UUIDHexGenerator(String sep)
  {
    this.sep = sep;
  }

  protected String format(int intval)
  {
    String formatted = Integer.toHexString(intval);
    StringBuffer buf = new StringBuffer("00000000");
    buf.replace(8 - formatted.length(), 8, formatted);
    return buf.toString();
  }

  protected String format(short shortval)
  {
    String formatted = Integer.toHexString(shortval);
    StringBuffer buf = new StringBuffer("0000");
    buf.replace(4 - formatted.length(), 4, formatted);
    return buf.toString();
  }

  public Serializable generate()
  {
    return 36 + 
      format(getIP()) + this.sep + 
      format(getJVM()) + this.sep + 
      format(getHiTime()) + this.sep + 
      format(getLoTime()) + this.sep + 
      format(getCount());
  }

  public static void main(String[] args)
    throws Exception
  {
    IdentifierGenerator gen = new UUIDHexGenerator("/");
    IdentifierGenerator gen2 = new UUIDHexGenerator("/");
    for (int i = 0; i < 10; i++)
    {
      String id = (String)gen.generate();
      System.out.println(id + ": " + id.length());
      String id2 = (String)gen2.generate();
      System.out.println(id2 + ": " + id2.length());
    }
  }
}
