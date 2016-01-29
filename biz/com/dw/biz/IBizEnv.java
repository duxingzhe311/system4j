package com.dw.biz;

import com.dw.system.xmldata.XmlData;

public abstract interface IBizEnv
{
  public abstract XmlData getInputData();

  public abstract XmlData getCurEnvData();

  public abstract void setCurEnvData(XmlData paramXmlData);

  public abstract void combineAppendEnvData(XmlData paramXmlData);

  public abstract void appendSubDataArray(String paramString, XmlData paramXmlData);
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.IBizEnv
 * JD-Core Version:    0.6.2
 */