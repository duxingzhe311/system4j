package com.dw.biz.api;

public abstract interface WFElement extends Idable
{
  public static final boolean READONLY = false;

  public abstract WFElement getParentWFElement();
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.api.WFElement
 * JD-Core Version:    0.6.2
 */