package com.dw.biz.bindview;

import com.dw.mltag.Attr;

public class XDSAttr extends Attr
{
  XDSStr xdsStr = null;

  public XDSAttr(String name, String val) {
    super(name, val);

    this.xdsStr = new XDSStr(val);
  }

  public XDSStr getXDSStrValue()
  {
    return this.xdsStr;
  }
}
