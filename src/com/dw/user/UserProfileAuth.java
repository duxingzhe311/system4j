package com.dw.user;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dw.system.Convert;
import com.dw.system.encrypt.DES;
import com.dw.system.util.HttpCookie;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import com.dw.user.right.RightManager;
import com.dw.user.access.*;

/**
 * 用户验证信息
 * 
 * @author Jason Zhu
 */
public class UserProfileAuth
{

	public static final String TOMATO_AUTH = "tomato_auth" ;
	public static final String COOKIE_AUTH_INFO = "auth_info" ;
	//public static final String TOMATO_AUTH = "tomato_auth" ;
	
	private static String TMP_KEY = "" ;
	static
	{
		TMP_KEY = UUID.randomUUID().toString().replaceAll("-", "") ;
		
	}
	/**
	 * 根据Http请求获得对应的验证信息
	 * @param req
	 * @return
	 */
	public static UserProfileAuth getUserProfileAuth(HttpServletRequest req) throws Exception
	{
		UserProfileAuth upa = (UserProfileAuth)req.getAttribute(TOMATO_AUTH) ;
		if(upa!=null)
			return upa ;
		
		HttpCookie wb_cookie = HttpCookie.getRequestCookie(TOMATO_AUTH,req);
		if (wb_cookie == null)
			return null;
	
		String ai = wb_cookie.getValue(COOKIE_AUTH_INFO);
		if (ai == null || ai.equals(""))
			return null;
	
		upa = new UserProfileAuth();
		if(!upa.fromEncrpytStr(ai))
			return null;
		
		req.setAttribute(TOMATO_AUTH, upa) ;
			return upa;
	}
	
	/**
	 * 对用户的密钥进行验证--如果成功,返回用户名
	 * @param username
	 * @param keytxt
	 * @param req
	 * @param hsr
	 * @return
	 * @throws Exception 
	 * @throws  
	 */
	public static boolean keyAuth(String keytxt,HttpServletRequest req,HttpServletResponse hsr) throws Exception
	{
		UserManager umgr = UserManager.getDefaultIns() ;
		String usern = umgr.checkUserUsbKey(keytxt) ;
	  	if(Convert.isNullOrEmpty(usern))
	  		return false ;
	  	
  		String new_sessionid = UUID.randomUUID().toString().replaceAll("-", "") ;
  		
  		UserProfile up = umgr.loadUserProfile(usern) ;
  		if(up==null)
  			return false;
  		
  		long l = AccessManager.getInstance().checkTmpAuth(up) ;
  		
  		UserProfileAuth upa = new UserProfileAuth(new_sessionid,usern,l) ;
  		
  		HttpCookie wb_cookie = new HttpCookie(TOMATO_AUTH);
		wb_cookie.setPath("/");
		// wb_cookie.setDomain(domain);
		
		wb_cookie.setValue(COOKIE_AUTH_INFO, upa.toEncrpytStr());
		
		HttpCookie.addResponseCookie(wb_cookie, hsr);
  		
		return true;
	}

	public static void cancelAuth(HttpServletRequest req,HttpServletResponse hsr)
		throws UnsupportedEncodingException
	{
		HttpCookie wb_cookie = new HttpCookie(TOMATO_AUTH);
		
		wb_cookie.setPath("/");
		
		HttpCookie.addResponseCookie(wb_cookie, hsr);
	}
	
	String authId = "" ;
	
	String userName = "" ;
	
	long timeout = -1 ;
	
	private UserProfileAuth()
	{}
	
	private UserProfileAuth(String authid,String usern,long timeout)
	{
		this.authId = authid ;
		this.userName = usern ;
		this.timeout = timeout ;
	}
	
	public String getAuthId()
	{
		return authId ;
	}
	
	public String getUserName()
	{
		return userName ;
	}
	
	public long getTimeOut()
	{
		return timeout ;
	}
	
	public String toEncrpytStr()
	{
		return DES.encode(authId+":"+timeout+":"+userName,TMP_KEY) ;
	}
	
	public boolean fromEncrpytStr(String s)
	{
		s = DES.decode(s,TMP_KEY) ;
		int p = s.indexOf(':') ;
		if(p<=0)
			return false;
		
		authId = s.substring(0,p) ;
		s = s.substring(p+1) ;
		p = s.indexOf(':') ;
		if(p<=0)
			return false;
		
		try
		{
			timeout = Long.parseLong(s.substring(0,p)) ;
		}
		catch(Exception ee)
		{
			return false;
		}
		userName = s.substring(p+1) ;
		return true ;
	}
}