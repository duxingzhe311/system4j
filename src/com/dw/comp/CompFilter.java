package com.dw.comp;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.dw.user.*;
import com.dw.user.util.SessionVar;

/**
 * ����װ�ص�Filter
 * 
 * ����ÿ����������webapp��ʽ������Ϊ��ʹƽ̨�������ܹ�֪����ǰװ���˶���
 * ����������ÿ��������Ϣ����Ҫͨ��webapp�ı�׼��ʽʵ�֡�
 * 
 * ÿ��webapp��web.xml��Ӧ�����ø�Servlet�������ڲ�����ָ����������
 * 
 * �����ڱ�װ��ʱ
 * 
 * �ù������ڹ����ڲ���ҳ�汻����ǰ���Զ���ȫ�ֵ���Ϣװ�룬���Զ�װ���û���½��Ϣ
 * �����������Ϣ�ȡ�
 * @author Jason Zhu
 */
public class CompFilter implements Filter
{
	public void init(FilterConfig config) throws ServletException
	{
		
		CompManager cm = CompManager.getInstance();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		String realpath = config.getServletContext().getRealPath("/");
		
		String comp_rootname = realpath = realpath.replace('\\', '/');
		if(realpath.endsWith("/"))
			comp_rootname = comp_rootname.substring(0,realpath.length()-1);
		
		int p = comp_rootname.lastIndexOf('/');
		comp_rootname = comp_rootname.substring(p+1);
		
		AppInfo ci = new AppInfo(realpath,comp_rootname,cl);
		CompManager.getInstance().fireAppFinding(ci);
		//System.out.println(">>>load comp by webapp context name=["+config.getServletContext().getServletContextName()+"] real path /="+config.getServletContext().getRealPath("/")+" class loader--"+cl.getClass().getCanonicalName());
		String cs = config.getInitParameter("comp-classes");
		if(cs==null||(cs=cs.trim()).equals(""))
			return ;
		
		StringTokenizer tmpst = new StringTokenizer(cs,",|");
		while(tmpst.hasMoreTokens())
		{
			try
			{
				String n = tmpst.nextToken() ;
				AbstractComp ac = (AbstractComp)cl.loadClass(n).newInstance();
				cm.registerComp(cl,ac);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws ServletException, IOException
	{
		HttpServletRequest hsr = (HttpServletRequest)req;
		
		
		//
		
//        Thread th = Thread.currentThread() ;
//        System.out.println("thread name=="+th.getName()+" cn="+th.getClass().getName()) ;
        LoginSession ls = SessionManager.getCurrentLoginSession(hsr);
        
        //System.out.println("sessionid="+session.getId()) ;
        //String user = (String) session.getAttribute("user");
        //System.out.println("user="+user) ;
        if (ls == null)
        {
            String uri = hsr.getRequestURI();
            if (uri != null && uri.startsWith("/user"))
            { //�û����ҳ�治������
                fc.doFilter(req, resp);
                return;
            }
            String query = hsr.getQueryString();
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

            ((HttpServletResponse)resp).sendRedirect("/user/forcelogin.jsp");
            return;
        }
        
        if (ls != null)
        {
        	System.out.println("find login session in comp: username="+ls.getUserName());
        }
        
        HttpSession session = hsr.getSession();
        session.setAttribute(SessionVar.LOGIN_SESSION, ls);
        
        try
        {
        	session.setAttribute(SessionVar.USER_PROFILE,new UserProfile(ls)) ;
        }
        catch(Throwable _t)
        {}
        
        String servp = hsr.getServletPath() ;
		//String cxtpath = hsr.getContextPath();
		//System.out.println("cxtpath===="+cxtpath);
		RequestDispatcher rd = hsr.getRequestDispatcher(servp);
        //req.setAttribute("", session)
        
		System.out.println("comp forword="+servp);
		rd.forward(hsr, resp);
		//fc.doFilter(req, resp);
	}

	public void destroy()
	{
		
	}
}

