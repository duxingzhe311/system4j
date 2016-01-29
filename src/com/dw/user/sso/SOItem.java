package com.dw.user.sso;

import java.util.*;

import com.dw.user.UserProfile;

/**
 * 已经登陆的用户信息项
 * 它保存用户Profile,登陆时间,最近访问时间等信息
 * @author Jason Zhu
 */
public class SOItem
{
	public static long TIMEOUT = 300*60*1000;//用户登陆后信息可以保存5个小时
	
	String session_id = null ;
	String userName = null ;
	long lastAccessTime = -1 ;
	long loginTime = -1 ;
	
	UserProfile userProfile = null ;
	
	public SOItem(UserProfile up)
	{
		session_id = UUID.randomUUID().toString();
		userName = up.getUserName() ;
		loginTime = lastAccessTime = System.currentTimeMillis();
		userProfile = up ;
	}
	
	public String getSessionId()
	{
		return session_id;
	}
	
	public String getUserName()
	{
		return userName ;
	}
	
	public long getLoginTime()
	{
		return loginTime ;
	}
	
	public long getLastAccessTime()
	{
		return lastAccessTime ;
	}
	
	void doAccess()
	{
		lastAccessTime = System.currentTimeMillis() ;
	}
	
	public UserProfile getUserProfile()
	{
		return userProfile;
	}
	
	public boolean isTimeOut()
	{
		return System.currentTimeMillis()-lastAccessTime>TIMEOUT ;
	}
}
