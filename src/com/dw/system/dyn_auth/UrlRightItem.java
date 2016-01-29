package com.dw.system.dyn_auth;

import com.dw.user.right.RightRule;

/**
 * ����֧��ҳ����ʹ������ж�Ȩ�޵Ķ���
 * 
 * @author Jason Zhu
 *
 */
public class UrlRightItem
{
	String moduleName = null ;
	
	/**
	 * ������ģ���ڲ������ڵ�jsp·��������·��--�ֳ�Ϊservlet path
	 */
	String path = null ;
	
	/**
	 * ƥ��Ĳ������ƣ���Щ���ϣ�Ȩ�޵��ж���Ҫ�����������ȷ��
	 * ��һ��jspҳ�汻�ظ�ʹ�ã�ͬ����ͬ�Ĳ������ֲ�ͬ��Ȩ�ޣ�����Ҫ��������
	 * ���磺�����б���������б��༭�����ӣ�ɾ����ҳ��ֻ��Ҫ��һ��
	 *   ͨ�����ŵķ�����Ϊ���ּ���
	 * 
	 * ����ֵ����ֻ֧��һ����û�б�Ҫ֧�ֶ��
	 */
	String matchPN = null ;
	
	/**
	 * ���matchPNʹ�õ�ƥ�����ֵ
	 */
	String matchPV = null ;
	
	/**
	 * ���趨��Ȩ��
	 */
	RightRule rightRule = null ;
	
	
	public UrlRightItem(String modulen,String path,
			String matchpn,String matchpv,
			RightRule rr)
	{
		this.moduleName = modulen ;
		this.path = path ;
		this.matchPN = matchpn ;
		this.matchPV = matchpv ;
		
		this.rightRule = rr ;
	}
	
	
	
	public String getModuleName()
	{
		return moduleName ;
	}
	
	public String getPath()
	{
		return path ;
	}
	
	public String getMatchPN()
	{
		return matchPN ;
	}
	
	public String getMatchPV()
	{
		return matchPV ;
	}
	
	public RightRule getRightRule()
	{
		return rightRule ;
	}
}
