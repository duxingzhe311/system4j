package com.dw.biz.tag;

import com.dw.biz.BizActionResult;
import com.dw.biz.BizManager;
import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizOnActionTag extends BodyTagSupport
{
  static final String INPUTXD = "__input_xd";
  static final String ONACT_RUN = "__on_act_run";
  static BizManager bizMgr = BizManager.getInstance();

  private String jsFuncVar = null;
  private String checkJsFunc = "";
  private String actionPath = null;
  private String outputXmlDataVar = null;
  private String inputXmlDataVar = null;
  private String resultVar = null;

  public void setPath(String p)
  {
    this.actionPath = p;
  }

  public void setJs_func_var(String uv)
  {
    this.jsFuncVar = uv;
  }

  public void setCheck_js_func(String cjf)
  {
    this.checkJsFunc = cjf;
    if (this.checkJsFunc == null)
      this.checkJsFunc = "";
  }

  public void setResult_var(String rv)
  {
    this.resultVar = rv;
  }

  public void setOutput_xmldata_var(String ovx)
  {
    this.outputXmlDataVar = ovx;
  }

  public void setInput_xmldata_var(String ivx)
  {
    this.inputXmlDataVar = ivx;
  }

  XmlData getInputXmlData()
    throws Exception
  {
    XmlData xd = (XmlData)this.pageContext.getAttribute("__input_xd");
    if (xd == null)
    {
      HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
      xd = XORMUtil.getXmlDataFromRequest(req);
      if (xd != null) {
        this.pageContext.setAttribute("__input_xd", xd);
      }
    }
    return xd;
  }

  void runAction()
    throws JspTagException
  {
    HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
    String actp = req.getParameter("__post_back_pn_act");
    if (!this.actionPath.equals(actp)) {
      return;
    }
    if ("true".equals(this.pageContext.getAttribute("__on_act_run"))) {
      return;
    }
    try
    {
      XmlData xd = getInputXmlData();

      UserProfile up = UserProfile.getUserProfile(req);
      BizActionResult bar = bizMgr.RT_doBizAction(up, this.actionPath, null, xd);
      if (Convert.isNotNullEmpty(this.resultVar))
      {
        String res = bar.getResultStr();
        if (res != null)
          this.pageContext.setAttribute(this.resultVar, res);
      }
      XmlData outputxd = bar.getResultData();

      if ((outputxd != null) && (Convert.isNotNullEmpty(this.outputXmlDataVar)))
      {
        this.pageContext.setAttribute(this.outputXmlDataVar, outputxd);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new JspTagException("IO Error: " + e.getMessage());
    }
    finally
    {
      this.pageContext.setAttribute("__on_act_run", "true");
    }
  }

  public int doStartTag()
    throws JspTagException
  {
    BizViewActionFormTag vaft = (BizViewActionFormTag)findAncestorWithClass(this, BizViewActionFormTag.class);
    if (!vaft.isDoAction())
    {
      if (Convert.isNotNullEmpty(this.jsFuncVar))
      {
        if (Convert.isNotNullEmpty(this.checkJsFunc))
          this.pageContext.setAttribute(this.jsFuncVar, "do_action('" + this.actionPath + "'," + this.checkJsFunc + ")");
        else {
          this.pageContext.setAttribute(this.jsFuncVar, "do_action('" + this.actionPath + "')");
        }
      }
      return 0;
    }

    HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
    String actp = req.getParameter("__post_back_pn_act");
    if (!this.actionPath.equals(actp)) {
      return 0;
    }
    try
    {
      XmlData xd = getInputXmlData();

      if ((xd != null) && (Convert.isNotNullEmpty(this.inputXmlDataVar)))
      {
        this.pageContext.setAttribute(this.inputXmlDataVar, xd);
      }
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
    runAction();

    this.pageContext.removeAttribute("__input_xd");
    if (Convert.isNotNullEmpty(this.inputXmlDataVar)) {
      this.pageContext.removeAttribute(this.inputXmlDataVar);
    }
    this.pageContext.removeAttribute("__on_act_run");

    return 6;
  }
}
