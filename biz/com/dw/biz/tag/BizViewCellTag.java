package com.dw.biz.tag;

import com.dw.biz.BizManager;
import com.dw.biz.platform.BizViewCellRunner;
import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlVal;
import com.dw.user.UserProfile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

public class BizViewCellTag extends BodyTagSupport
{
  public static String ATTRN_BIZ_VIEW_CELL_JS_INC = "_biz_view_cell_js_inc";
  public static String ATTRN_BIZ_VIEW_CTRL = "biz_view_ctrl";

  static BizManager bizMgr = BizManager.getInstance();

  private String path = null;
  private String cellId = null;
  private String clientIdVar = null;
  private String val = null;

  private String viewCtrlVar = ATTRN_BIZ_VIEW_CTRL;
  private String viewInputXdVar = "biz_xml_data";

  public void setPath(String p)
    throws IOException
  {
    this.path = p;
  }

  public void setCell_id(String s)
  {
    this.cellId = s;
  }

  public void setValue(String v)
  {
    this.val = v;
  }

  public void setClient_id_var(String civ)
  {
    this.clientIdVar = civ;
  }

  public int doStartTag()
    throws JspTagException
  {
    if (!"true".equals(this.pageContext.getAttribute(ATTRN_BIZ_VIEW_CELL_JS_INC)))
    {
      try
      {
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
      }
      catch (IOException localIOException)
      {
      }

      this.pageContext.setAttribute(ATTRN_BIZ_VIEW_CELL_JS_INC, "true");
    }

    this.pageContext.setAttribute(BizViewTag.ATTRN_BIZ_VIEW_JS_INC, "true");

    return 2;
  }

  private UserProfile getUserProfile()
    throws Exception
  {
    HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
    return UserProfile.getUserProfile(req);
  }

  public int doEndTag()
    throws JspTagException
  {
    try
    {
      BizViewCellRunner bvcr = bizMgr.createBizViewCellRunnerIns(this.path);

      bvcr.setCellName(this.cellId);

      BizViewFormTag bvf = (BizViewFormTag)TagSupport.findAncestorWithClass(this, BizViewFormTag.class);
      XmlData inputxd = null;
      boolean readonly = false;
      if (bvf != null)
      {
        bvcr.setContainerId(bvf.idName);
        inputxd = bvf.getInputXmlData();
        readonly = bvf.bReadOnly;
      }
      else
      {
        HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
        inputxd = XORMUtil.getXmlDataFromRequest(req);
      }

      if (Convert.isNotNullEmpty(this.clientIdVar)) {
        this.pageContext.setAttribute(this.clientIdVar, bvcr.getValueInputId());
      }
      String props = null;
      if (this.bodyContent != null)
      {
        props = this.bodyContent.getString().trim();
      }

      HashMap ps = Convert.transPropStrToMap(props);
      if (ps != null)
      {
        for (Map.Entry nv : ps.entrySet())
        {
          bvcr.setAttribute((String)nv.getKey(), (String)nv.getValue());
        }
      }

      if (this.val != null) {
        bvcr.setAttribute("value", this.val);
      }
      JspWriter jw = this.pageContext.getOut();

      UserProfile up = getUserProfile();

      bvcr.prepareRunner(up, jw, bizMgr, null, null, null);
      if (inputxd != null)
      {
        XmlVal xv = inputxd.getParamXmlVal(this.cellId);
        if (xv != null) {
          bvcr.setStrValue(xv.getStrVal());
        }
      }
      if (readonly)
      {
        bvcr.setReadonly(true);
      }
      bvcr.render(jw);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new JspTagException("IO Error: " + e.getMessage());
    }

    return 6;
  }
}
