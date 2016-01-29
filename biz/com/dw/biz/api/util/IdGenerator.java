package com.dw.biz.api.util;

import java.io.PrintStream;

public class IdGenerator
{
  private static IdentifierGenerator gen = new UUIDHexGenerator();

  public static String createNewId() {
    return (String)gen.generate();
  }

  public static void main(String[] args)
  {
    for (int i = 0; i < 10; i++)
    {
      System.out.println(createNewId());
    }
  }
}
