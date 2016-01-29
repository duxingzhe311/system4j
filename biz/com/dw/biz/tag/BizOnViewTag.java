package com.dw.biz.tag;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizOnViewTag extends BodyTagSupport
{
  public int doStartTag()
    throws JspTagException
  {
    BizViewActionFormTag vaft = (BizViewActionFormTag)findAncestorWithClass(this, BizViewActionFormTag.class);
    if (vaft.isDoAction())
    {
      return 0;
    }

    return 1;
  }

  public int doEndTag() throws JspTagException
  {
    return 6;
  }
}
