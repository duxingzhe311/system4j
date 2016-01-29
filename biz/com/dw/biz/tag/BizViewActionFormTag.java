package com.dw.biz.tag;

import com.dw.biz.BizManager;
import com.dw.system.Convert;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizViewActionFormTag extends BodyTagSupport
{
  static final String POST_BACK = "__post_back_bat";
  static final String PN_ACT = "__post_back_pn_act";
  static BizManager bizMgr = BizManager.getInstance();

  private String idName = null;

  public void setId_name(String idn)
    throws IOException
  {
    this.idName = idn;
  }

  boolean isDoAction()
  {
    HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
    String actp = req.getParameter("__post_back_pn_act");
    return Convert.isNotNullEmpty(actp);
  }

  private void writeJsInclude(String acturl)
    throws IOException
  {
    if ("true".equals(this.pageContext.getAttribute(BizViewTag.ATTRN_BIZ_VIEW_JS_INC))) {
      return;
    }
    JspWriter jw = this.pageContext.getOut();
    String CXT_ROOT = "/system/";
    jw.write("<script type=\"text/javascript\" src=\"" + CXT_ROOT + "ui/dlg.js\"></script>\r\n");
    jw.write("<link REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"" + CXT_ROOT + "css/expand.css\">\r\n");
    jw.write("<link REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"" + CXT_ROOT + "css/workflow.css\">\r\n");
    jw.write("<LINK href=\"" + CXT_ROOT + "biz/view.css\" type=\"text/css\" rel=\"stylesheet\" />\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/biz_view.js\"></script>\r\n");
    jw.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + CXT_ROOT + "css/dlg/subModal.css\" />\r\n");
    jw.write("<script type=\"text/javascript\" src=\"" + CXT_ROOT + "css/dlg/common.js\"></script>\r\n");
    jw.write("<script type=\"text/javascript\" src=\"" + CXT_ROOT + "ui/ajax.js\"></script>\r\n");

    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/biz.js\"></script>\r\n");
    jw.write("<script type=\"text/javascript\" src=\"" + CXT_ROOT + "js/biz_util.js\" ></script>\r\n");

    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/expand.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/calendar.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/input_check.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/fitsearch.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/wfextend.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/inputpage.js\"></script>\r\n");

    jw.println("<script>");
    jw.println("function do_action(p,ckf)");
    jw.println("{");
    jw.println(" if(ckf){if(!ckf()) return;}");
    jw.println("if(!biz_view_check_before_submit()) return;");
    jw.println("document.getElementById('__post_back_pn_act').value=p;");
    jw.println("var f=document.getElementById('" + this.idName + "');");
    jw.println("f.action='" + acturl + "';");
    jw.println("f.submit();");
    jw.println("}");
    jw.println("</script>");

    this.pageContext.setAttribute(BizViewTag.ATTRN_BIZ_VIEW_JS_INC, "true");
  }

  public int doStartTag()
    throws JspTagException
  {
    if (isDoAction())
    {
      return 1;
    }

    try
    {
      HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
      String u = req.getRequestURI();
      int p = u.lastIndexOf('/');
      if (p >= 0)
        u = u.substring(p + 1);
      String qs = req.getQueryString();
      if (Convert.isNotNullEmpty(qs)) {
        u = u + "?" + qs;
      }

      writeJsInclude(u);

      JspWriter jw = this.pageContext.getOut();
      jw.write("<form method='post' onsubmit='return false;' style='margin: 0px;border: 0;padding: opx'");
      jw.write(" id='" + this.idName + "'");
      jw.write(" name='");
      jw.write(this.idName);
      jw.write("' action='");
      jw.write(u);
      jw.write("'>\r\n");

      jw.println("<input type='hidden' id='__post_back_pn_act' name='__post_back_pn_act' />");
    }
    catch (Exception e)
    {
      throw new JspTagException("IO Error: " + e.getMessage());
    }

    return 1;
  }

  public int doEndTag()
    throws JspTagException
  {
    if (isDoAction())
    {
      return 0;
    }

    try
    {
      JspWriter jw = this.pageContext.getOut();
      jw.println("</form>");
    }
    catch (Exception e)
    {
      throw new JspTagException("IO Error: " + e.getMessage());
    }
    return 6;
  }
}
