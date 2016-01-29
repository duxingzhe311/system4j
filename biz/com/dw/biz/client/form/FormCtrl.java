package com.dw.biz.client.form;

public class FormCtrl
{
  private String ctrlName = null;
  private boolean bMultiValue = false;
  private boolean bSupportRead = true;
  private boolean bSupportWrite = true;
  private boolean bSupportHidden = true;

  public String getControlName()
  {
    return this.ctrlName;
  }

  public void setControlName(String cn)
  {
    this.ctrlName = cn;
  }

  public boolean isMultiValue()
  {
    return this.bMultiValue;
  }

  public boolean isSupportRead()
  {
    return this.bSupportRead;
  }

  public boolean isSupportWrite()
  {
    return this.bSupportWrite;
  }

  public boolean isSupportHidden()
  {
    return this.bSupportHidden;
  }
}
