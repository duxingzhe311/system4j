package com.dw.biz.tag;

import com.dw.biz.BizManager;
import com.dw.biz.BizView;
import com.dw.biz.BizView.Controller;
import com.dw.biz.platform.BizViewRunner;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class BizViewTag extends BodyTagSupport
{
  public static String ATTRN_BIZ_VIEW_JS_INC = "_biz_view_js_inc";
  public static String ATTRN_BIZ_VIEW_CTRL = "biz_view_ctrl";

  static BizManager bizMgr = BizManager.getInstance();

  private String viewPath = null;
  private String viewOutId = null;
  private boolean bReadOnly = false;

  private int win_width = -1;
  private int win_height = -1;

  private int width = -1;
  private int height = -1;
  private String scroll = null;
  private boolean autoResize = true;
  private String divContainer = null;

  private int resizeAddW = 15;
  private int resizeAddH = 100;

  private String viewCtrlVar = ATTRN_BIZ_VIEW_CTRL;
  private String viewInputXdVar = "biz_xml_data";

  public BizViewTag()
  {
    this.viewOutId = UUID.randomUUID().toString();
    this.viewOutId = this.viewOutId.replace("-", "");
  }

  public void setPath(String p) throws IOException
  {
    this.viewPath = p;
  }

  public void setDiv_container(String s)
  {
    this.divContainer = s;
  }

  public void setReadonly(String breadonly)
  {
    this.bReadOnly = "true".equalsIgnoreCase(breadonly);
  }

  public void setId(String id)
  {
    if ((id == null) || (id.equals(""))) {
      return;
    }
    this.viewOutId = id;
  }

  public void setWidth(String w)
  {
    if ((w == null) || (w.equals(""))) {
      return;
    }
    this.width = Integer.parseInt(w);
  }

  public void setHeight(String h)
  {
    if ((h == null) || (h.equals(""))) {
      return;
    }
    this.height = Integer.parseInt(h);
  }

  public void setScroll(String s)
  {
    this.scroll = s;
  }

  public void setAuto_resize(String ar)
  {
    this.autoResize = ((!"no".equalsIgnoreCase(ar)) && (!"false".equalsIgnoreCase(ar)));
  }

  public void setResize_add_width(String aw)
  {
    if ((aw == null) || (aw.equals(""))) {
      return;
    }
    this.resizeAddW = Integer.parseInt(aw);
  }

  public void setResize_add_height(String ah)
  {
    if ((ah == null) || (ah.equals(""))) {
      return;
    }
    this.resizeAddH = Integer.parseInt(ah);
  }

  public void setView_ctrl_var(String vcv)
  {
    if ((vcv == null) || (vcv.equals(""))) {
      return;
    }
    this.viewCtrlVar = vcv;
  }

  public void setView_input_var(String viv)
  {
    if ((viv == null) || (viv.equals(""))) {
      return;
    }
    this.viewInputXdVar = viv;
  }

  public int doStartTag()
    throws JspTagException
  {
    return 2;
  }

  private void writeJsInclude()
    throws IOException
  {
    if ("true".equals(this.pageContext.getAttribute(ATTRN_BIZ_VIEW_JS_INC))) {
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
    jw.write("<script LANGUAGE=\"Javascript\" src=\"" + CXT_ROOT + "js/expand.js\" charset=\"gb2312\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/calendar.js\" charset=\"gb2312\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/input_check.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/fitsearch.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/wfextend.js\"></script>\r\n");
    jw.write("<script LANGUAGE=\"Javascript\" SRC=\"" + CXT_ROOT + "js/inputpage.js\"></script>\r\n");

    this.pageContext.setAttribute(ATTRN_BIZ_VIEW_JS_INC, "true");
  }

  private XmlData getInputXmlData()
    throws Exception
  {
    XmlData xd = (XmlData)this.pageContext.getAttribute(this.viewInputXdVar);
    if (xd == null)
    {
      HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();
      xd = XORMUtil.getXmlDataFromRequest(req);
    }

    return xd;
  }

  private int getScrollWidth(BizView bv)
  {
    if (this.width > 0) {
      return this.width;
    }
    int w = bv.getViewWidth();
    if (w > 800)
      return 800;
    return w;
  }

  private int getScrollHeight(BizView bv)
  {
    if (this.height > 0) {
      return this.height;
    }
    int h = bv.getViewHeight();
    if (h > 500)
      return 500;
    return h;
  }

  private String getScrollDivStyle(BizView bv)
  {
    StringBuilder sb = new StringBuilder();
    if (!"no".equalsIgnoreCase(this.scroll)) {
      sb.append("overflow: scroll;");
    }
    sb.append("width:").append(getScrollWidth(bv)).append(";");

    sb.append("height:").append(getScrollHeight(bv)).append(";");

    return sb.toString();
  }

  private String getViewDivStyle(BizView bv)
  {
    StringBuilder sb = new StringBuilder();
    int w = bv.getViewWidth();
    int h = bv.getViewHeight();
    sb.append("width:").append(w).append(";");
    sb.append("height:").append(h).append(";");

    return sb.toString();
  }

  public int doEndTag()
    throws JspTagException
  {
    try
    {
      String viewpath = this.viewPath;
      if (this.bodyContent != null)
      {
        String tmps = this.bodyContent.getString().trim();
        if ((tmps != null) && (!tmps.equals(""))) {
          viewpath = tmps;
        }
      }
      XmlData xd = getInputXmlData();

      BizView bv = bizMgr.getBizViewByPath(viewpath);
      BizViewRunner bvr = bizMgr.RT_getBizViewRunner(this.viewOutId, bv);
      if (bvr == null) {
        throw new RuntimeException("cannot find biz view runner with path=" + this.viewPath);
      }
      HttpServletRequest req = (HttpServletRequest)this.pageContext.getRequest();

      writeJsInclude();

      JspWriter jw = this.pageContext.getOut();

      if (this.autoResize)
      {
        jw.write("<script>dlg.resize_to(");
        jw.write(getScrollWidth(bv) + this.resizeAddW);
        jw.write("," + (getScrollHeight(bv) + this.resizeAddH));
        jw.write(")</script>");
      }

      if (!"no".equalsIgnoreCase(this.divContainer))
      {
        if (!"no".equalsIgnoreCase(this.scroll))
        {
          jw.write("<div style='");
          jw.write(getScrollDivStyle(bv));
          jw.write("'>");
        }

        jw.write("<div style='");
        jw.write(getViewDivStyle(bv));
        jw.write("'>");
      }

      UserProfile up = UserProfile.getUserProfile(req);

      bvr.prepareRunner(req, (HttpServletResponse)this.pageContext.getResponse(), up, jw, bizMgr, xd);
      BizView.Controller ctrl = (BizView.Controller)this.pageContext.getAttribute(this.viewCtrlVar);
      if (ctrl != null) {
        bvr.setCellCtrl(ctrl);
      }
      if (this.bReadOnly)
        bvr.setRenderReadOnly();
      bvr.runIt();

      jw.flush();

      if (!"no".equalsIgnoreCase(this.divContainer))
      {
        jw.write("</div>");
        if (!"no".equalsIgnoreCase(this.scroll))
        {
          jw.write("</div>");
        }
      }
      jw.println("<script>biz_view_init_all_cell();</script>");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new JspTagException("IO Error: " + e.getMessage());
    }

    return 6;
  }
}
