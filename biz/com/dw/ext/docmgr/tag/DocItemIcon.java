package com.dw.ext.docmgr.tag;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class DocItemIcon extends BodyTagSupport
{
  private String path = null;

  public void setPath(String p)
  {
    this.path = p;
  }

  public int doStartTag()
    throws JspTagException
  {
    return 1;
  }

  public int doEndTag()
    throws JspTagException
  {
    return 6;
  }
}
