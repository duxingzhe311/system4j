package com.dw.biz.bindview;

import com.dw.mltag.AbstractNode;
import com.dw.mltag.XmlText;

public class XDSText extends XmlText
{
  private XDSStr xdsStr = null;

  public XDSText(XmlText oldxt)
  {
    super(oldxt.getText());

    AbstractNode pn = (AbstractNode)oldxt.getParent();

    this.xdsStr = new XDSStr(getText());

    if (pn != null)
    {
      int p = pn.getIndex(oldxt);
      pn.setChild(this, p);
    }
  }

  public XDSStr getXDSTextValue()
  {
    return this.xdsStr;
  }
}
