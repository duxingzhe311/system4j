package com.dw.user.sso;

import java.io.*;
import java.util.*;

import com.dw.user.*;
import com.dw.user.right.*;

/**
 * �����½����,��Ϊϵͳ�ṩ�˵����½֧��
 * 
 * ���ڲ�ͬ��ϵͳ��ҵ��ģ�鶼�п�����Ҫ��������֤���̡�����Ҫ����֤��Ϣ���û��������룩
 * �п���Ҳ��ͬ�����ԣ���Ҫ�Բ�ͬ��ϵͳ��ҵ��ģ���ṩ
 * 
 * ���磺�ʼ�ģ������ʼ�������ʱ������ܹ���SSO�л�ö�Ӧģ����û���֤��Ϣ�������
 * �ﵽϵͳ�Զ���½Ч��
 * 
 * ���ǵ������������ϵ�������,����Ҫ֧��ҵ��ģ�������,����ڷ����������Ƶ�½
 * ��Ӱ�쵽javawebserver��.net webserver������ʹ��
 * 
 * ���û���½��Ϣ������webserver�Լ�����.
 * 
 * ���Ե�ǰ,SSO�����κ��ж�,ֻҪ�û�����,���еĺ��û���صķ��ʶ���Ϊ�Ѿ���½
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
	 * ע��ģ����
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
	 * ����ģ�����ƺ��û�ע�����ƻ�ö�Ӧ���û���½��Ϣ
	 * @param module ģ�����������null�����ʾ��ǰ��½�û��������Ϣ
	 * @param username �û�Ψһע������
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
	 * �ж�һ���û��Ƿ��Ѿ���½
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
