package com.dw.biz.client.form;

import java.util.List;

public abstract interface IFormInvoker
{
  public abstract String getFormId();

  public abstract String getFormName();

  public abstract String getFormDesc();

  public abstract void OnInvoker(String paramString, List<String> paramList1, List<String> paramList2);
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.client.form.IFormInvoker
 * JD-Core Version:    0.6.2
 */