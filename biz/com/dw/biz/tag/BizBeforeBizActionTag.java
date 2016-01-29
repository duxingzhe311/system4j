package com.dw.biz.tag;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizBeforeBizActionTag extends BodyTagSupport
{
  public int doStartTag()
    throws JspTagException
  {
    return 1;
  }

  public int doEndTag()
    throws JspTagException
  {
    BizOnActionTag vaft = (BizOnActionTag)findAncestorWithClass(this, BizOnActionTag.class);
    vaft.runAction();

    return 6;
  }
}
