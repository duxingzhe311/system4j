package com.dw.user.sso;

import java.util.*;

import com.dw.user.UserProfile;

/**
 * �Ѿ���½���û���Ϣ��
 * �������û�Profile,��½ʱ��,�������ʱ�����Ϣ
 * @author Jason Zhu
 */
public class SOItem
{
	public static long TIMEOUT = 300*60*1000;//�û���½����Ϣ���Ա���5��Сʱ
	
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
