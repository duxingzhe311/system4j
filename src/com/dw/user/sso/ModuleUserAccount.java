package com.dw.user.sso;

/**
 * �ںܶ�����£�ϵͳ��Ҫ֧�ֶ�����ϵͳ���ܵ��Զ���½��֤��ÿ������ϵͳ������Ϊ��һ��
 * ��ϵͳ��ģ�飭ͨ��һ��Ψһģ���������֡���ϵͳ���Ը���ģ�����Ʊ���ͻ�ȡÿ��ע���û�
 * ����ڶ�Ӧģ����û���½��֤��Ϣ�����Ǳ��ࡣ
 * 
 * ���ģ������Ϊnull��գ�����Ϊ��ϵͳȱʡ���û���֤��Ϣ��
 * 
 * ���磺mail������ҪΪÿ���û����Զ�ͨ���ʼ�����������֤�ṩ�û��������롣�����Ҫ�Ա�����
 * 	ר������ص���Ϣ�洢
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
