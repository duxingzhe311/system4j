package com.dw.biz.api.util;

import java.net.InetAddress;

abstract class UUIDGenerator
  implements IdentifierGenerator
{
  private static final int ip = ipadd;

  private static short counter = 0;
  private static final int jvm = (int)(System.currentTimeMillis() >>> 8);

  static
  {
    int ipadd;
    try
    {
      ipadd = BytesHelper.toInt(InetAddress.getLocalHost().getAddress());
    }
    catch (Exception e)
    {
      int ipadd;
      ipadd = 0;
    }
  }

  protected int getJVM()
  {
    return jvm;
  }

  protected short getCount()
  {
    synchronized (UUIDGenerator.class)
    {
      if (counter < 0)
      {
        counter = 0;
      }
      return counter++;
    }
  }

  protected int getIP()
  {
    return ip;
  }

  protected short getHiTime()
  {
    return (short)(int)(System.currentTimeMillis() >>> 32);
  }

  protected int getLoTime()
  {
    return (int)System.currentTimeMillis();
  }
}
