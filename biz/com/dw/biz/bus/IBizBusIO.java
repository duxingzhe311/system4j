package com.dw.biz.bus;

public abstract interface IBizBusIO
{
  public abstract String[] getAllBusLineNames()
    throws Exception;

  public abstract byte[] loadBusLineCont(String paramString)
    throws Exception;

  public abstract void saveBusLineCont(String paramString, byte[] paramArrayOfByte)
    throws Exception;

  public abstract void renameBusLine(String paramString1, String paramString2)
    throws Exception;

  public abstract byte[] loadAdpConfig(String paramString)
    throws Exception;

  public abstract void saveAdpConfig(String paramString, byte[] paramArrayOfByte)
    throws Exception;
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.bus.IBizBusIO
 * JD-Core Version:    0.6.2
 */