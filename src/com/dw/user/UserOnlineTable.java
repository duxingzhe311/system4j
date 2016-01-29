package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.cache.Cacher;

/**
 * �����û���Ϣ��
 * @author Jason Zhu
 */
public class UserOnlineTable
{
	public static final long TIME_OUT = 30 * 60000 ;
	
	public static class UserItem
	{
		long lastAccess = -1 ;
		UserProfile userProfile = null ;
		
		public UserItem(long la,UserProfile up)
		{
			lastAccess = la ;
			userProfile = up ;
		}
	}
	
	static Cacher cache = null;
	static
	{
		cache = Cacher.getCacher(UserOnlineTable.class.getCanonicalName());
	}
	
	/**
	 * ���ĳһ���û����˷���,��÷���������
	 * @param up
	 */
	public static void accessUserProfile(UserProfile up)
	{
		cache.cache(up.getUserName(), up, TIME_OUT) ;
	}
	
	/**
	 * �õ����е������û���Ϣ
	 * @return
	 */
	public static List<UserProfile> getOnlineUsers()
	{
		ArrayList<UserProfile> ups = new ArrayList<UserProfile>() ;
		
		Object[] os = cache.getAllContents() ;
		if(os==null)
			return ups ;
		
		for(Object o:os)
		{
			if(!(o instanceof UserProfile))
				continue ;
			
			ups.add((UserProfile)o) ;
		}
		
		return ups ;
	}
}
