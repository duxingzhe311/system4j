package com.dw.user;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.encrypt.MD5;
import com.dw.system.setup.SetupManager;
import com.dw.system.util.HttpCookie;
import com.dw.user.*;
import com.dw.user.access.AccessManager;

public class SessionManager
{
	//public static final String TOMATO_SESSION = "tomato_session_id" ;
	
	public static final String TOMATO_COOKIE = "tomato";

	public static final String TOMATO_EXT_COOKIE = "tomato_ext";

	public static final String COOKIE_NAME_SESSION = "sessionid";

	public static final String COOKIE_NAME_USERID = "userid";
	
	public static final String COOKIE_NAME_DOMAINID = "domainid";

	public static final String COOKIE_NAME_USERNAME = "username";

	public static final String TOMATO_LOGIN_PASSWORD = "tomato.user.login_password";
	
	public static final String ATTRN_LOGIN_SESSION_TIMEOUT= "login_session_timeout";
	
	public static final String ATTRN_DEBUG_COOKIE_ONLY = "debug_cookie_only";
	
	public static final String ATTRN_LOGIN_PAGE = "login_page";
	
	public static final String ATTRN_IS_FORCE_REDIRECT = "is_force_redirect" ;
	
	public static final String ATTRN_LOGIN_REDIRECT_PAGE = "login_redirect_page";
	
	private static long SESSION_TIMEOUT = 30*60000 ;
	
	
	public static final String TBS_AUTH_ATTRN = "tbs_auth" ;
	
	/**
	 * 存放移动终端 id:uuid:secstr 的用户验证内容
	 */
	public static final String TBS_CLIENT_AUTH_ATTRN = "_tbs_client_auth_" ;
	/**
	 * 如果该变量=true,则只要IE不关闭,用户登陆信息永远有效--支持调试开发时使用
	 */
	private static boolean debugCookieOnly = false;
	
	private static String loginPage = null ;
	
	/**
	 * 是否强制重定向-true表示登录重定向不考虑r参数指定的内容
	 */
	private static boolean bForceRedirect = false ;
	
	private static String loginRedirectPage = null ;
	
	static
	{
		try
		{
			Element ele = AppConfig.getConfElement("user");
			if(ele!=null)
			{
				String lst = ele.getAttribute(ATTRN_LOGIN_SESSION_TIMEOUT) ;
				
				if(lst!=null&&!lst.equals(""))
				{
					long t = Long.parseLong(lst);
					if(t>0)
						SESSION_TIMEOUT = t * 60000 ;
				}
				
				debugCookieOnly = "true".equalsIgnoreCase(ele.getAttribute(ATTRN_DEBUG_COOKIE_ONLY));
				
				loginPage = ele.getAttribute(ATTRN_LOGIN_PAGE) ;
				
				loginRedirectPage = ele.getAttribute(ATTRN_LOGIN_REDIRECT_PAGE);
				
				bForceRedirect = "true".equalsIgnoreCase(ele.getAttribute(ATTRN_IS_FORCE_REDIRECT)) ;
			}
			
			SetupManager setupmgr = SetupManager.getInstance() ;
			if(!setupmgr.isRootPswToDB())
			{//设置配置中的root密码到数据库中
				String rootp = setupmgr.getRootPsw() ;
				if(rootp==null)
					rootp = "" ;
				
				if(UserManager.getDefaultIns().ChangePasswordByAdm(0, "root", rootp))
				{
					setupmgr.setRootPswToDB(true) ;
					setupmgr.saveConf() ;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 获得通过配置文件设置的登陆跳转页面,该页面为不同的项目自己实现的页面
	 * 里面调用了 view = /system/user/login.view
	 * @return
	 */
	public static String getLoginPage()
	{
		return loginPage ;
	}
	
	/**
	 * 得到登陆后跳转页面
	 * @return
	 */
	public static String getLoginRedirectPage()
	{
		return loginRedirectPage;
	}
	
	/**
	 * 判断是否每次登陆后都强制使用重定向处理
	 * @return
	 */
	public static boolean isForceRedirect()
	{
		return bForceRedirect;
	}
	/**
	 * 为客户端控件提供的cookie字符串到LoginSession之间的转换支持
	 * @throws UnsupportedEncodingException 
	 */
    public static LoginSession StrCookie2LoginSession(String s) throws UnsupportedEncodingException
    {
        if (Convert.isNullOrEmpty(s))
            return null;

        HashMap<String,String> ds = new HashMap<String,String>() ;
        StringTokenizer st = new StringTokenizer(s,";");
        while(st.hasMoreTokens())
        {
        	String s0 = st.nextToken() ;
        	String tmps = s0.trim();
            int i =tmps.indexOf('=');
            if(i<0)
                ds.put(tmps,"") ;
            else
                ds.put(tmps.substring(0,i),tmps.substring(i+1)) ;
        }

        String sbcookie = ds.get(TOMATO_COOKIE);
        if (sbcookie==null)//!ds.TryGetValue(WEBBRICKS_COOKIE, out sbcookie))
            return null;

        //ss = sbcookie.Split('&');
        st = new StringTokenizer(sbcookie,"&");
        HashMap<String, String> wbds = new HashMap<String, String>();
        while (st.hasMoreTokens())
        {
        	String s0 = st.nextToken() ;
            String tmps = s0.trim();
            int i = tmps.indexOf('=');
            if (i < 0)
                wbds.put(tmps,"");
            else
                wbds.put(tmps.substring(0, i), tmps.substring(i + 1));
        }

        String sessionid = wbds.get(COOKIE_NAME_SESSION);
        if(sessionid==null)
            return null;

        String userid = wbds.get(COOKIE_NAME_USERID);
        if(userid==null)//!wbds.TryGetValue(COOKIE_NAME_USERID,out userid))
            return null;
        
        int domainid = Convert.parseToInt32(wbds.get(COOKIE_NAME_DOMAINID), 0);

        String username = wbds.get(COOKIE_NAME_USERNAME);
        if(username==null)//!wbds.TryGetValue(COOKIE_NAME_USERNAME,out username))
            return null;

        LoginSession ls = new LoginSession(domainid,sessionid, userid, username);

        //get ext info
        String extcookie = ds.get(TOMATO_EXT_COOKIE);
        if (extcookie!=null)//ds.TryGetValue(WEBBRICKS_EXT_COOKIE, out extcookie))
        {
            //ss = extcookie.split('&') ;
            st = new StringTokenizer(extcookie,"&");
            ls.extInfo = new HashMap<String, String>();
            while(st.hasMoreTokens())
            {
            	String s0 = st.nextToken();
                String tmps = s0.trim();
                int i = tmps.indexOf('=');
                if (i < 0)
                    ls.extInfo.put(tmps,"");
                else
                    ls.extInfo.put(tmps.substring(0, i), URLDecoder.decode(tmps.substring(i + 1), "utf-8"));
            }
        }

        return ls;
    }
    
    private static HashMap<String,Long> USERNAME2AUTH_TIMELIMIT = new HashMap<String,Long>() ;
	/**
	 * 存放sessionid-最近访问时间的映射,该表可以快webapp应用,达到统一的session访问
	 */
	private static HashMap<String,Long> SESSIONID2LAST_ACCESS0 = new HashMap<String,Long>() ;
	private static HashMap<String,LoginSession> SESSIONID2LAST_ACCESS = new HashMap<String,LoginSession>() ;
	
	private static void clearTimeOutSession()
	{
		ArrayList<String> tobem = new ArrayList<String>();
		long curtm = System.currentTimeMillis();
		for(Map.Entry<String, LoginSession> s2la:SESSIONID2LAST_ACCESS.entrySet())
		{
			if((curtm-s2la.getValue().lastAcc)>SESSION_TIMEOUT)
				tobem.add(s2la.getKey());
		}
		
		for(String s:tobem)
		{
			SESSIONID2LAST_ACCESS.remove(s);
		}
	}
	
	private static LoginSession accessSession(String sessionid)
	{
		if(sessionid==null)
			return null;
		
		LoginSession ls = SESSIONID2LAST_ACCESS.get(sessionid);
		if(ls==null)
			return null ;
		
		if(debugCookieOnly)
			return ls;
		
		//check timeout
		
		long curtm = System.currentTimeMillis();
		if((curtm-ls.lastAcc)>SESSION_TIMEOUT)
		{
			synchronized(SESSIONID2LAST_ACCESS)
			{
				clearTimeOutSession();
			}
			return null ;
		}
		
//		synchronized(SESSIONID2LAST_ACCESS)
//		{
//			SESSIONID2LAST_ACCESS.put(sessionid, curtm);
//		}
		ls.lastAcc = curtm ;
		return ls ;
	}
	
//	private static void createSession(String sessionid)
//	{
//		if(sessionid==null)
//			return ;
//		synchronized(SESSIONID2LAST_ACCESS)
//		{
//			SESSIONID2LAST_ACCESS.put(sessionid, System.currentTimeMillis());
//		}
//	}
	
	private static void removeSession(String sessionid)
	{
		if(sessionid==null)
			return ;
		
		synchronized(SESSIONID2LAST_ACCESS)
		{
			SESSIONID2LAST_ACCESS.remove(sessionid);
		}
	}
	
	
	public static boolean login(String username, String password,
			HttpServletRequest req,HttpServletResponse hsr) throws Exception
	{
		return login(username, password,null,req,hsr) ;
	}
	
	public static boolean login(String username, String password,String md5_32_prefix,
			HttpServletRequest req,HttpServletResponse hsr) throws Exception
	{
		return login(0, username,  password,md5_32_prefix,
				req,hsr);
	}
	
	public static boolean login(int domainid,String username, String password,String md5_32_prefix,
			HttpServletRequest req,HttpServletResponse hsr) throws Exception
	{
		LoginSession ls = loginToSession(domainid,
				username, password,md5_32_prefix,
				req,hsr);
		return ls!=null ;
	}
	
	public static String loginToSessionId(String username, String password,
			HttpServletRequest req,HttpServletResponse hsr) throws Exception
	{
		LoginSession ls = loginToSession(0,
				username, password,null,
				req,hsr);
		if(ls==null)
			return null ;
		
		return ls.sessionId ;
	}
	
	public static LoginSession loginToSession(int domainid,String username, String password,String md5_32_prefix,
			HttpServletRequest req,HttpServletResponse hsr) throws Exception
	{
		
		UserProfile up = UserManager.getDefaultIns().ValidateUser(domainid,username, password,md5_32_prefix);
		if (up == null)
		{
			return null;
//			if(domainid!=0||!"root".equals(username))
//				return false;
//			
//			//判断是否满足配置密码
//			String rootpsw = SetupManager.getInstance().getRootPsw() ;
//			if(Convert.isNullOrEmpty(rootpsw))
//				return false;
//			
//			if(Convert.isNullOrTrimEmpty(md5_32_prefix))
//			{
//				if(!rootpsw.equals(password))
//				{
//					return false;
//				}
//			}
//			else
//			{
//				if(!password.equalsIgnoreCase(MD5.encryptMD5(md5_32_prefix+rootpsw)))
//				{
//					return false;
//				}
//			}
//			
//			//
//			up = UserManager.getDefaultIns().loadUserProfile(domainid, username);
//			if(up==null)
//				return false;
		}
		
		User.UserState ust = up.getUserInfo().getState();
		if(ust==User.UserState.Invalid)//||ust==User.UserState.Deleted)
			return null ;
		
		//check auth 从密钥验证结果中判断用户名称是否相同
		if(AccessManager.getInstance().isNeedAuth(req))
		{
			UserProfileAuth upa = UserProfileAuth.getUserProfileAuth(req) ;
			if(upa==null)
				return null;
			
			//key user must same as this login user
			if(!upa.getUserName().equals(username))
			{
				return null;
			}
		}
		
		
		LoginSession ls = up.toLoginSession() ;
		HashMap<String, String> extInfo = ls.getExtInfo();
		

		// 设置认证票cookie的作用域(退出也要做相应更改)
		// HttpCookie cookie =
		// FormsAuthentication.GetAuthCookie(UserName,IsRemember,"/");
		// cookie.Domain = UserInfo.domain ;
		// HttpContext.Current.Response.Cookies.Add(cookie);

		// 设置cookie信息
		HttpCookie wb_cookie = new HttpCookie(TOMATO_COOKIE);
		wb_cookie.setPath("/");
		// wb_cookie.setDomain(domain);
		String new_sessionid = UUID.randomUUID().toString()
		.replaceAll("-", "") ;
		
		
		if(debugCookieOnly)
		{
			wb_cookie.setValue(COOKIE_NAME_SESSION, new_sessionid);
			HttpCookie.addResponseCookie(wb_cookie, hsr);
			
			wb_cookie.setValue(COOKIE_NAME_USERID, "" + ls.getUserId());
			wb_cookie.setValue(COOKIE_NAME_DOMAINID, "" + ls.getUserDomainId());
			wb_cookie.setValue(COOKIE_NAME_USERNAME, ls.getUserName());//.getUniqueUserName());
	
			HttpCookie.addResponseCookie(wb_cookie, hsr);
	
			HttpCookie wb_ext_cookie = new HttpCookie(TOMATO_EXT_COOKIE, extInfo);
			wb_ext_cookie.setPath("/");
	
			HttpCookie.addResponseCookie(wb_ext_cookie, hsr);
		}
		else
		{
			wb_cookie.setValue(COOKIE_NAME_SESSION, new_sessionid);
			HttpCookie.addResponseCookie(wb_cookie, hsr);
		}
		
		// HttpContext.Current.Session[WEBBRICKS_LOGIN_PASSWORD] = password;
		//createSession(new_sessionid);
		//LoginSession ls = new LoginSession(domainid,username,ls.getUserId(), String username, HashMap<String, String> extinfo)
		ls.sessionId = new_sessionid;
		SESSIONID2LAST_ACCESS.put(new_sessionid, ls) ;
		
		UserManager.getDefaultIns().logUserLogin(domainid, username) ;
		
		return ls;
	}

	public static void logOut(HttpServletRequest req,HttpServletResponse hsr)
			throws UnsupportedEncodingException
	{
		LoginSession ls = getCurrentLoginSession(req);
		if(ls!=null)
			removeSession(ls.sessionId);
		
		UserProfileAuth.cancelAuth(req,hsr) ;
		
		HttpCookie wb_cookie = new HttpCookie(TOMATO_COOKIE);
		HttpCookie wb_ext_cookie = new HttpCookie(TOMATO_EXT_COOKIE);

		wb_cookie.setPath("/");
		// wb_cookie.setDomain(domain);

		wb_ext_cookie.setPath("/");
		// wb_ext_cookie.setDomain = domain;

		HttpCookie.addResponseCookie(wb_cookie, hsr);
		HttpCookie.addResponseCookie(wb_ext_cookie, hsr);
		// HttpContext.Current.Session[WEBBRICKS_LOGIN_PASSWORD] = "";
		
		if(ls!=null)
			UserManager.getDefaultIns().logUserActivity(ls.domainId,ls.userName) ;
	}
	
	/**
	 * 获得当前session中的用户Profile信息
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public static UserProfile getCurrentLoginUserProfile(HttpServletRequest req) throws Exception
	{
		LoginSession ls = getCurrentLoginSession(req);
		if(ls==null)
			return null ;
		
		return new UserProfile(ls);
	}

	/**
	 * 得到当前请求中的sessionid
	 * @param req
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getCurrentSessionId(HttpServletRequest req)
	throws UnsupportedEncodingException
	{
		HttpCookie wb_cookie = HttpCookie.getRequestCookie(TOMATO_COOKIE,
				req);
		if (wb_cookie == null)
			return null;

		String sessionid = wb_cookie.getValue(COOKIE_NAME_SESSION);
		if (sessionid == null || sessionid.equals(""))
			return null;

		if(debugCookieOnly)
			return sessionid ;//no timeout
		
		// 判断该sessionid是否过期
		LoginSession la = accessSession(sessionid);
		if(la==null)
			return null ;
		
		return sessionid ;
	}
	
	
	private static String SESS_PREFIX = TOMATO_COOKIE+"=" ;
	private static String SS_PREFIX = COOKIE_NAME_SESSION+"=" ;
	private static int SESS_PREFIX_LEN = SESS_PREFIX.length() ;
	private static int SS_PREFIX_LEN = SS_PREFIX.length() ;
	
	/**
	 * 根据cookie获得对应的sessionid
	 * @param cookiestr
	 * @return
	 */
	public static String getSessionIdFromCookieStr(String cookiestr)
	{
		
		return null ;
	}
	
	/**
	 * 根据cookie获得相关的当前登录用户信息
	 * @param cookiestr
	 * @return
	 * @throws Exception
	 */
	public static UserProfile getUserProfileByCookieStr(String cookiestr) throws Exception
	{
		HashMap<String,HttpCookie> n2hc = HttpCookie.fromCookieStr(cookiestr) ;
		if(n2hc==null)
			return null ;
		
		HttpCookie thc = n2hc.get(TOMATO_COOKIE) ;
		if(thc==null)
			return null ;
		LoginSession ls = null ;
		if(debugCookieOnly)
		{
			ls = loadLoginSessionInCookie(thc,n2hc.get(TOMATO_EXT_COOKIE));
			if(ls==null)
				return null ;
		}
		else
		{
			String sessionid = thc.getValue(COOKIE_NAME_SESSION);
			if (sessionid == null || sessionid.equals(""))
				return null;
			
			ls = accessSession(sessionid) ;
			if(ls==null)
				return null ;
		}
		
		return new UserProfile(ls) ;
	}
	
	
	/**
	 * 获得当前session中的登陆session信息
	 * @param req
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static LoginSession getCurrentLoginSession(HttpServletRequest req)
			throws UnsupportedEncodingException
	{
		HttpCookie wb_cookie = HttpCookie.getRequestCookie(TOMATO_COOKIE,
				req);

		if (wb_cookie == null)
		{
			String sessionid = req.getHeader(TBS_AUTH_ATTRN) ;
			if (sessionid == null || sessionid.equals(""))
			{
				
				return null;
			}
//			 判断该sessionid是否过期
			LoginSession ls = accessSession(sessionid);
			if(ls==null)
				return null ;
			return ls ;
		}

		HttpCookie wb_ext_cookie = HttpCookie.getRequestCookie(
				TOMATO_EXT_COOKIE, req);

		if(debugCookieOnly)
		{
			return loadLoginSessionInCookie(wb_cookie,wb_ext_cookie);
		}
		else
		{
			String sessionid = wb_cookie.getValue(COOKIE_NAME_SESSION);
			if (sessionid == null || sessionid.equals(""))
			{
				sessionid = req.getHeader(TBS_AUTH_ATTRN) ;
			}
			
			if (sessionid == null || sessionid.equals(""))
				return null;
			
//			 判断该sessionid是否过期
			LoginSession ls = accessSession(sessionid);
			if(ls==null)
				return null ;
			return ls ;
		}
	}
	
	private static LoginSession loadLoginSessionInCookie(HttpCookie wb_cookie,HttpCookie wb_ext_cookie)
	{
		String sessionid = wb_cookie.getValue(COOKIE_NAME_SESSION);
		if (sessionid == null || sessionid.equals(""))
			return null;
		
		String userid = wb_cookie.getValue(COOKIE_NAME_USERID);
		if (userid == null || userid.equals(""))
			return null;
		
		
		int domainid = Convert.parseToInt32(wb_cookie.getValue(COOKIE_NAME_DOMAINID), 0) ;
		
		String username = wb_cookie.getValue(COOKIE_NAME_USERNAME);
		if (username == null || username.equals(""))
			return null;

		LoginSession s = new LoginSession(domainid,sessionid, userid, username);

		
		if (wb_ext_cookie != null)
		{
			s.extInfo = wb_ext_cookie.getValues();
		}
		
		return s ;
	}
	
	/*
	public static LoginSession getCurrentLoginSession0(HttpServletRequest req)
		throws UnsupportedEncodingException
	{
	HttpCookie wb_cookie = HttpCookie.getRequestCookie(TOMATO_COOKIE,
			req);
	if (wb_cookie == null)
		return null;
	
	String sessionid = wb_cookie.getValue(COOKIE_NAME_SESSION);
	if (sessionid == null || sessionid.equals(""))
		return null;
	
	// 判断该sessionid是否过期
	Long la = accessSession(sessionid);
	if(la==null)
		return null ;
	
	String userid = wb_cookie.getValue(COOKIE_NAME_USERID);
	if (userid == null || userid.equals(""))
		return null;
	
	
	int domainid = Convert.parseToInt32(wb_cookie.getValue(COOKIE_NAME_DOMAINID), 0) ;
	
	String username = wb_cookie.getValue(COOKIE_NAME_USERNAME);
	if (username == null || username.equals(""))
		return null;
	
	LoginSession s = new LoginSession(domainid,sessionid, userid, username);
	
	HttpCookie wb_ext_cookie = HttpCookie.getRequestCookie(
			TOMATO_EXT_COOKIE, req);
	if (wb_ext_cookie != null)
	{
		s.extInfo = wb_ext_cookie.getValues();
	}
	
	return s;
	}*/
}
