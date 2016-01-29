package com.dw.user.sso;

import java.io.*;
import java.util.*;

import com.dw.user.*;
import com.dw.user.right.*;

/**
 * 当点登陆管理,它为系统提供了当点登陆支持
 * 
 * 由于不同的系统和业务模块都有可能需要独立的验证过程。其需要的验证信息（用户名和密码）
 * 有可能也不同。所以，需要对不同的系统及业务模块提供
 * 
 * 例如：邮件模块访问邮件服务器时，如果能够在SSO中获得对应模块的用户验证信息，则可以
 * 达到系统自动登陆效果
 * 
 * 考虑到服务器技术上的特殊性,由于要支持业务模块的运行,如果在服务器中限制登陆
 * 会影响到javawebserver或.net webserver的正常使用
 * 
 * 而用户登陆信息可以由webserver自己控制.
 * 
 * 所以当前,SSO不做任何判断,只要用户存在,所有的和用户相关的访问都认为已经登陆
 * 
 * @author Jason Zhu
 */
public class SSOManager
{
	//RightManager rightMgr = null ;
	static SSOManager ssoMgr = new SSOManager();
	
	public static SSOManager getInstance()
	{
		return ssoMgr ;
	}
	
	SSOBuffer ssoBuf = new SSOBuffer();
	
	HashMap<String,ModuleInfo> moduleInfos = new HashMap<String,ModuleInfo>() ;
	
	private SSOManager()
	{
		//rightMgr = rm ;
	}
	
	/**
	 * 注册模块名
	 * @param mi
	 */
	public void registerModuleInfo(ModuleInfo mi)
	{
		moduleInfos.put(mi.getName(), mi);
	}
	
	public ArrayList<ModuleInfo> getRegisteredModuleInfo()
	{
		ArrayList<ModuleInfo> mis = new ArrayList<ModuleInfo>() ;
		mis.addAll(moduleInfos.values()) ;
		return mis ;
	}
	/**
	 * 根据模块名称和用户注册名称获得对应的用户登陆信息
	 * @param module 模块名，如果是null，则表示当前登陆用户输入的信息
	 * @param username 用户唯一注册名称
	 * @return
	 * @throws Exception 
	 */
	public ModuleUserAccount getModuleUserAccount(String module,String username) throws Exception
	{
		UserManager um = UserManager.getDefaultIns() ;
		if(module==null||module.equals(""))
		{
			String psw = um.getUserPsw(username);
			return new ModuleUserAccount(username,psw) ;
		}
		
		HashMap<String,String> hm = um.getProtectedExtInfo(username);
		if(hm==null)
			return null ;
		
		HashMap<String,String> mua_map = new HashMap<String,String>() ;
		String prefix = "MUA."+module+".";
		for(Map.Entry<String, String> k2v:hm.entrySet())
		{
			String k = k2v.getKey();
			if(!k.startsWith(prefix))
				continue ;
			
			mua_map.put(k.substring(prefix.length()), k2v.getValue()) ;
		}
		
		String n = mua_map.get("name") ;
		if(n==null||n.equals(""))
			return null ;
		
		String psw = mua_map.get("psw");
		return new ModuleUserAccount(n,psw);
	}
	
	
	public void setModuleUserAccount(String module,String username,ModuleUserAccount mua)
		throws Exception
	{
		if(module==null||module.equals(""))
			return ;
		
		UserManager um = UserManager.getDefaultIns() ;
		HashMap<String,String> hm = um.getProtectedExtInfo(username);
		if(hm==null)
			hm = new HashMap<String,String>() ;
		
		String prefix = "MUA."+module+".";
		hm.put(prefix+"name", mua.getLoginName());
		if(mua.getPasswordInfo()!=null)
			hm.put(prefix+"psw",mua.getPasswordInfo()) ;
		
		um.setProtectedExtInfo(username, hm);
	}
	/**
	 * 判断一个用户是否已经登陆
	 * @param usern
	 * @return
	 */
//	public boolean checkUserLogin(String usern)
//	{
//		UserProfile up = ssoBuf.accessSOUser(usern) ;
//		return up!=null;
//	}
	
	
	
	public LoginUserInfo login(String usern,String psw)
		throws Exception
	{
		UserProfile up = UserManager.getDefaultIns().ValidateUser(usern, psw) ;
		if(up==null)
			return null ;
		
//		UserProfile up = UserProfile.GetUserProfileByName(usern);
//		if(up==null)
//			return null ;
//		
		ssoBuf.setSOUser(up);
		return up.toLoginUserInfo() ;
	}
	
//	public List<LoginUserInfo> getLoginUsers()
//	{
//		List<UserProfile> ups = ssoBuf.getUserProfiles() ;
//		if(ups==null)
//			return null ;
//		
//		ArrayList<LoginUserInfo> luis = new ArrayList<LoginUserInfo>(ups.size());
//		for(UserProfile up:ups)
//		{
//			luis.add(up.toLoginUserInfo());
//		}
//		return luis ;
//	}
//	
//	public LoginUserInfo getLoginUser(String usern)
//	{
//		UserProfile up = ssoBuf.getUserProfile(usern);
//		if(up==null)
//			return null ;
//		
//		return up.toLoginUserInfo();
//	}
	
	/**
	 * 
	 * @param usern
	 * @return
	 */
	public UserProfile accessLoginUserProfile(String usern) throws Exception
	{
		//System.out.println("user name="+usern);
		if(usern==null||usern.equals(""))
			return null ;
		
		UserProfile up = ssoBuf.accessSOUser(usern);
		if(up!=null)
			return up;
		
		up = UserProfile.GetUserProfileByName(usern);
		if(up==null)
			return null ;
		
		ssoBuf.setSOUser(up);
		return up ;
	}
}
