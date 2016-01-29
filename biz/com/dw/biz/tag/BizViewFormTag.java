package com.dw.biz.tag;

import com.dw.biz.BizManager;
import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.XmlData;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizViewFormTag extends BodyTagSupport
{
  static final String POST_BACK = "__post_back_bat";
  static final String PN_ACT = "__post_back_pn_act";
  static BizManager bizMgr = BizManager.getInstance();

  String idName = null;
  private String action = null;

  private String inputXmlDataVar = null;
  private String checkJsFunc = "";
  private String submitJsFuncVar = null;
  private boolean hasFileSubmit = false;
  boolean bReadOnly = false;

  public void setId_name(String idn)
    throws IOException
  {
    this.idName = idn;
  }

  public void setAction(String act)
  {
    this.action = act;
  }

  public void setInput_xmldata_var(String ixdv)
  {
    this.inputXmlDataVar = ixdv;
  }

  public void setCheck_js_func(String cjf)
  {
    this.checkJsFunc = cjf;
  }

  public void setSubmit_js_func_var(String jfv)
  {
    this.submitJsFuncVar = jfv;
  }

  public void setSubmit_file(String submitf)
  {
    this.hasFileSubmit = (("true".equalsIgnoreCase(submitf)) || ("1".equals(submitf)));
  }

  public void setReadonly(String ro)
  {
    this.bReadOnly = (("true".equalsIgnoreCase(ro)) || ("1".equals(ro)));
  }

  XmlData getInputXmlData()
    throws Exception
  {
    if (Convert.isNotNullEmpty(this.inputXmlDataVar))
    {
      return (XmlData)this.pageContext.getAttribute(this.inputXmlDataVar);
    }

    HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
    return XORMUtil.getXmlDataFromRequest(req);
  }

  private void writeJsInclude()
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
    jw.println("function do_view_form_submit(ckf)");
    jw.println("{");
    jw.println(" if(ckf){if(!ckf()) return false;}");
    jw.println("if(!biz_view_check_before_submit()) return false;");

    jw.println("var f=document.getElementById('" + this.idName + "');");
    jw.println("f.submit(); return true;");
    jw.println("}");
    jw.println("</script>");

    this.pageContext.setAttribute(BizViewTag.ATTRN_BIZ_VIEW_JS_INC, "true");
  }

  public int doStartTag()
    throws JspTagException
  {
    try
    {
      HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();

      writeJsInclude();

      if (Convert.isNotNullEmpty(this.submitJsFuncVar))
      {
        if (Convert.isNotNullEmpty(this.checkJsFunc))
          this.pageContext.setAttribute(this.submitJsFuncVar, "do_view_form_submit(" + this.checkJsFunc + ")");
        else {
          this.pageContext.setAttribute(this.submitJsFuncVar, "do_view_form_submit()");
        }
      }
      JspWriter jw = this.pageContext.getOut();
      jw.write("<form method='post' onsubmit='return false;' style='margin: 0px;border: 0;padding: opx'");
      jw.write(" id='" + this.idName + "'");
      jw.write(" name='");
      jw.write(this.idName);
      jw.write("' action='");
      jw.write(this.action);
      jw.write("' ");
      if (this.hasFileSubmit)
      {
        jw.write("enctype='multipart/form-data'");
      }
      jw.write(">\r\n");
    }
    catch (Exception e)
    {
      throw new JspTagException("IO Error: " + e.getMessage());
    }

    return 1;
  }

  public int doEndTag() throws JspTagException
  {
    try
    {
      JspWriter jw = this.pageContext.getOut();
      jw.println("</form>");
      jw.println("<script>biz_view_init_all_cell();</script>");
    }
    catch (Exception e)
    {
      throw new JspTagException("IO Error: " + e.getMessage());
    }
    return 6;
  }
}
