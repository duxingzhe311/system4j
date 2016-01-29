package com.dw.p2p;

public class P2PServer
{
  static Object locker = new Object();

  static P2PServer ins = null;

  public static P2PServer getInstance()
  {
    if (ins != null) {
      return ins;
    }
    synchronized (locker)
    {
      if (ins != null) {
        return ins;
      }
      ins = new P2PServer();
      return ins;
    }
  }

  public void start()
  {
  }

  public void stop()
  {
  }
}
