package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.cache.Cacher;

/**
 * 在线用户信息表
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
	 * 如果某一个用户做了访问,则该方法被调用
	 * @param up
	 */
	public static void accessUserProfile(UserProfile up)
	{
		cache.cache(up.getUserName(), up, TIME_OUT) ;
	}
	
	/**
	 * 得到所有的在线用户信息
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
