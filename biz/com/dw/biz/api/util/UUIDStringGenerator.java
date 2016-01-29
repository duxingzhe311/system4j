package com.dw.biz.api.util;

import java.io.PrintStream;
import java.io.Serializable;

class UUIDStringGenerator extends UUIDGenerator
{
  private String sep;

  public UUIDStringGenerator()
  {
    this.sep = "";
  }

  public UUIDStringGenerator(String sep)
  {
    this.sep = sep;
  }

  public Serializable generate()
  {
    return 20 + 
      toString(getIP()) + this.sep + 
      toString(getJVM()) + this.sep + 
      toString(getHiTime()) + this.sep + 
      toString(getLoTime()) + this.sep + 
      toString(getCount());
  }

  public static void main(String[] args)
    throws Exception
  {
    IdentifierGenerator gen = new UUIDStringGenerator();
    for (int i = 0; i < 5; i++)
    {
      String id = (String)gen.generate();
      System.out.println(id + ": " + id.length());
    }
  }

  private static String toString(int value)
  {
    return new String(BytesHelper.toBytes(value));
  }

  private static String toString(short value)
  {
    return new String(BytesHelper.toBytes(value));
  }
}
