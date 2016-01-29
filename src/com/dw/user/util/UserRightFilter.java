package com.dw.user.util;

import javax.servlet.*;
import javax.servlet.http.*;

import com.dw.user.LoginSession;
import com.dw.user.SessionManager;
import com.dw.user.UserProfile;
import com.dw.web_ui.WebRes;

/**
 * 支持Web应用权限判断的过滤器
 * @author Jason Zhu
 */
public class UserRightFilter implements Filter
{
    public UserRightFilter()
    {
    }

    public void init(FilterConfig parm1)
        throws javax.servlet.ServletException
    {

    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
        throws javax.servlet.ServletException, java.io.IOException
    {
        //if (filterConfig == null)
        //return;
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String cxt_root = WebRes.getContextRootPath(req);
//        Thread th = Thread.currentThread() ;
//        System.out.println("thread name=="+th.getName()+" cn="+th.getClass().getName()) ;
        LoginSession ls = SessionManager.getCurrentLoginSession((HttpServletRequest)request);
        
        //System.out.println("sessionid="+session.getId()) ;
        //String user = (String) session.getAttribute("user");
        //System.out.println("user="+user) ;
        String uri = req.getServletPath() ;
        //System.out.println("User Right Filter uri="+uri) ;
        if (ls == null)
        {
            if (uri != null && uri.startsWith("/user"))
            { //用户相关页面不作处理
                chain.doFilter(req, res);
                return;
            }
            String query = req.getQueryString();
            //System.out.println("uri="+uri) ;
            //System.out.println("que="+query) ;
            if (query != null && !query.equals(""))
            {
                query = "?" + query;
            }
            String url = null;
            if (query == null)
            {
                url = uri;
            }
            else
            {
                url = uri + query;
            }

//            RequestDispatcher rd = req.getRequestDispatcher(
//                "/user/forcelogin.jsp?url=" + url);
//            rd.include(request, response);

            res.sendRedirect(cxt_root+"user/forcelogin.jsp");
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute(SessionVar.LOGIN_SESSION, ls);
        
        try
        {
        	session.setAttribute(SessionVar.USER_PROFILE,new UserProfile(ls)) ;
        }
        catch(Throwable _t)
        {}
        
        //查看权限
//        PageHelper.PageItem pi = PageHelper.getPageItemByUrlPath(uri) ;
//        if(pi==null)
//        {
//            chain.doFilter(req, res);
//            return ;
//        }
//
//        if(!PageHelper.checkCanAccess(pi,session))
//        {
//            res.getWriter().print("You have no right to access!");
//            return ;
//        }

        //System.out.println("right ok:"+uri);
        req.setCharacterEncoding("UTF-8");
        
        //res.getWriter().print("1111111111111111");
        chain.doFilter(req, res);
        //res.getWriter().print("222222222");
    }

    public void destroy()
    {

    }

}