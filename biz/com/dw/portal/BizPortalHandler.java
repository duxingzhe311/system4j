package com.dw.portal;

import com.dw.biz.platform.BizViewRunner;
import com.dw.system.AppConfigFilter;
import com.dw.system.AppConfigFilter.PortalHandler;
import com.dw.system.AppConfigFilter.PortalReq;
import com.dw.system.Convert;
import com.dw.user.UserProfile;
import com.dw.user.access.AccessManager;
import com.dw.user.right.RightRule;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BizPortalHandler
  implements AppConfigFilter.PortalHandler
{
  static String SUFFIX_PAGE = ".page.jsp";
  static String SUFFIX_TEMP = ".temp.jsp";
  static String SUFFIX_PMGR = ".pmgr.jsp";

  static int SUFFIX_LEN = SUFFIX_PAGE.length();

  private boolean isPortalRequest(HttpServletRequest req)
  {
    String sp = req.getServletPath();
    if (sp.endsWith(SUFFIX_PAGE)) {
      return true;
    }
    return PageManager.getInstance().isPortalDomain(req.getServerName());
  }

  private void renderPortalPage(HttpServletRequest req, HttpServletResponse resp)
  {
    String servern = req.getServerName();
  }

  public AppConfigFilter.PortalReq checkPortalReq(HttpServletRequest req)
  {
    String servern = req.getServerName();
    if (!PageManager.getInstance().isPortalDomain(servern)) {
      return null;
    }
    String sp = req.getServletPath();
    if (sp.endsWith(SUFFIX_PAGE))
    {
      String pn;
      if ((sp == null) || (sp.equals("/")))
      {
        pn = "default";
      }
      else
      {
        int k = sp.lastIndexOf('/');
        if (k < 0) {
          return AppConfigFilter.PORTAL_EMPTY_REQ;
        }
        pn = sp.substring(k + 1, sp.length() - SUFFIX_LEN);
      }

      return new AppConfigFilter.PortalReq(servern, pn, false);
    }
    if (sp.endsWith(SUFFIX_TEMP))
    {
      String pn;
      if ((sp == null) || (sp.equals("/")))
      {
        pn = "default";
      }
      else
      {
        int k = sp.lastIndexOf('/');
        if (k < 0) {
          return AppConfigFilter.PORTAL_EMPTY_REQ;
        }
        pn = sp.substring(k + 1, sp.length() - SUFFIX_LEN);
      }

      return new AppConfigFilter.PortalReq(servern, pn, true);
    }
    if (sp.endsWith(SUFFIX_PMGR))
    {
      return AppConfigFilter.PORTAL_MGR_REQ;
    }
    if ((sp.endsWith("/")) || (sp.equals("/index.jsp")))
    {
      return new AppConfigFilter.PortalReq(servern, "index", false);
    }

    return AppConfigFilter.PORTAL_EMPTY_REQ;
  }

  public boolean checkMgrRight(AppConfigFilter.PortalReq preq, HttpServletRequest req, HttpServletResponse resp, UserProfile up, StringBuilder failedr)
    throws Exception
  {
    if (up == null)
    {
      failedr.append("no permission because of no user info!");
      return false;
    }

    if (!AccessManager.getInstance().isInnerRequest(req))
    {
      failedr.append("no right!");
      return false;
    }

    if (up.isAdministrator())
    {
      return true;
    }

    long pageid = Convert.parseToInt64(req.getParameter("_portal_pid"), -1L);
    if (pageid <= 0L)
    {
      failedr.append("no right because of no _portal_pid input!");
      return false;
    }

    long blockid = Convert.parseToInt64(req.getParameter("_portal_bid"), -1L);
    if (blockid <= 0L)
    {
      failedr.append("no right because of no _portal_bid input!");
      return false;
    }

    Page p = PageManager.getInstance().getTempOrPageById(pageid);
    if (p == null)
    {
      failedr.append("no right no Page found!");
      return false;
    }

    PageBlock pb = p.getPageBlockById(blockid);
    if (pb == null)
    {
      failedr.append("no right no PageBlock found!");
      return false;
    }

    RightRule rr = pb.getEditRightRule();
    if (rr == null)
    {
      failedr.append("no right because of no RightRule found!");
      return false;
    }

    if (!rr.CheckUserRight(up))
    {
      failedr.append("no right because of user is not fit RightRule!");
      return false;
    }

    BizViewRunner bvr = pb.getCommonBizViewRunner();
    if (!bvr.checkPortalEditRight(req, up, failedr)) {
      return false;
    }
    return true;
  }

  public void renderPortalReq(AppConfigFilter.PortalReq preq, HttpServletRequest req, HttpServletResponse resp)
    throws Exception
  {
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("text/html;charset=UTF-8");

    Page p = null;
    if (preq.isTemplate())
      p = PageManager.getInstance().getTempByDomainName(preq.getPageDomain(), preq.getPageName());
    else
      p = PageManager.getInstance().getPageByDomainName(preq.getPageDomain(), preq.getPageName());
    if (p == null)
    {
      return;
    }

    UserProfile up = UserProfile.getUserProfile(req);
    PrintWriter pw = resp.getWriter();

    p.render(req, resp, up, pw, null);
  }
}
