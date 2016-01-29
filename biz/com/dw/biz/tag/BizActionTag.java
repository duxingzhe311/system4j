package com.dw.biz.tag;

import com.dw.biz.BizActionResult;
import com.dw.biz.BizManager;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizActionTag extends BodyTagSupport
{
  static BizManager bizMgr = BizManager.getInstance();

  private String actionPath = null;
  private String viewOutId = null;

  public BizActionTag()
  {
    this.viewOutId = UUID.randomUUID().toString();
    this.viewOutId = this.viewOutId.replace("-", "");
  }

  public void setPath(String p) throws IOException
  {
    this.actionPath = p;
  }

  public void setId(String id)
  {
    if ((id == null) || (id.equals(""))) {
      return;
    }
    this.viewOutId = id;
  }

  public int doStartTag()
    throws JspTagException
  {
    return 2;
  }

  private XmlData getInputXmlData()
    throws Exception
  {
    XmlData xd = (XmlData)this.pageContext.getAttribute("biz_xml_data");
    if (xd == null)
    {
      HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
      xd = XORMUtil.getXmlDataFromRequest(req);
    }

    return xd;
  }

  public int doEndTag()
    throws JspTagException
  {
    try
    {
      String actpath = this.actionPath;
      if (this.bodyContent != null)
      {
        String tmps = this.bodyContent.getString().trim();
        if ((tmps != null) && (!tmps.equals(""))) {
          actpath = tmps;
        }
      }
      XmlData xd = getInputXmlData();
      HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
      UserProfile up = UserProfile.getUserProfile(req);
      BizActionResult bar = bizMgr.RT_doBizAction(up, actpath, null, xd);
      XmlData outputxd = bar.getResultData();

      if (outputxd != null)
      {
        this.pageContext.setAttribute("biz_out_xmldata", outputxd);
      }

      pw = new PrintWriter(this.pageContext.getOut());
    }
    catch (Exception e)
    {
      PrintWriter pw;
      throw new JspTagException("IO Error: " + e.getMessage());
    }

    return 6;
  }
}
