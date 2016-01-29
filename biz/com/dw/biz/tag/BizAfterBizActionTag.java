package com.dw.biz.tag;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizAfterBizActionTag extends BodyTagSupport
{
  public int doStartTag()
    throws JspTagException
  {
    BizOnActionTag vaft = (BizOnActionTag)findAncestorWithClass(this, BizOnActionTag.class);
    vaft.runAction();

    return 1;
  }

  public int doEndTag() throws JspTagException
  {
    return 6;
  }
}
