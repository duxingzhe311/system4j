package com.dw.user.sso;

/**
 * 在很多情况下，系统需要支持对其他系统功能的自动登陆验证。每个其他系统可以认为是一个
 * 本系统的模块－通过一个唯一模块名称区分。本系统可以根据模块名称保存和获取每个注册用户
 * 相对于对应模块的用户登陆验证信息－就是本类。
 * 
 * 如果模块名称为null或空，则认为是系统缺省的用户验证信息。
 * 
 * 例如：mail构件需要为每个用户的自动通过邮件服务器的验证提供用户名和密码。这就需要对本构件
 * 	专门做相关的信息存储
 * 
 * @author Jason Zhu
 */
public class ModuleUserAccount
{
	String loginName = null ;
	String pswInfo = "" ;
	
	public ModuleUserAccount()
	{}
	
	public ModuleUserAccount(String ln,String psw)
	{
		loginName = ln ;
		pswInfo = psw ;
	}
	
	public String getLoginName()
	{
		return loginName ;
	}
	
	public void setLoginName(String ln)
	{
		loginName = ln ;
	}
	
	public String getPasswordInfo()
	{
		return pswInfo ;
	}
	
	public void setPasswordInfo(String pi)
	{
		pswInfo = pi ;
	}
}
