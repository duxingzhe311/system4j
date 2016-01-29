package com.dw.system.dyn_auth;

import com.dw.user.right.RightRule;

/**
 * 用来支持页面访问过程中判断权限的对象
 * 
 * @author Jason Zhu
 *
 */
public class UrlRightItem
{
	String moduleName = null ;
	
	/**
	 * 构件（模块内部）对于的jsp路径或其他路径--又称为servlet path
	 */
	String path = null ;
	
	/**
	 * 匹配的参数名称，有些场合，权限的判断需要加入参数才能确定
	 * 如一个jsp页面被重复使用，同步不同的参数区分不同的权限，就需要这种设置
	 * 例如：新闻列表管理，其中列表，编辑，增加，删除等页面只需要做一套
	 *   通过新闻的分类作为区分即可
	 * 
	 * 参数值区分只支持一个，没有必要支持多个
	 */
	String matchPN = null ;
	
	/**
	 * 配合matchPN使用的匹配参数值
	 */
	String matchPV = null ;
	
	/**
	 * 被设定的权限
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
