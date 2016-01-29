package com.dw.user;

import java.util.List;

import com.dw.system.gdb.xorm.XORMProperty;

/**
 * 在不同的项目中,极有可能针对某一个组织机构节点下的所有子节点增加一些扩展信息
 * 这些扩展信息根据项目的需要各不相同.
 * 
 * 为了最方便这种扩展,特此定义了组织机构节点扩展的架构
 * 
 * 为了最方便开发--目标是几乎不用写程序,有如下限定
 * 1,用户扩展信息必须在一个表上,并且符合XORM中定义的表格式--也就是继承本类
 * 	  的类必须定义XORM信息,并注册--除此之外就不需要做任何开发了
 * 2,
 * 
 * 在不同的构建中，可能会针对不同的子节点进行设定
 * @author Jason Zhu
 */
public abstract class OrgNodeExtItem
{
	@XORMProperty(name="AutoId",has_col=true,is_pk=true,is_auto=true)
	protected String autoId = null ;
	
	//@XORMProperty(name="OrgNodeId",has_col=true,has_idx=true,max_len=15,is_unique_idx=true,has_fk=true,fk_table="security_org",fk_column="OrgNodeId",order_num=10)
	@XORMProperty(name="OrgNodeId",has_col=true,has_idx=true,max_len=15,is_unique_idx=true,order_num=10)
	protected String orgNodeId = null ;
	
	
	/**
	 * 在用户管理模块中对应的父节点路径名称
	 * @return
	 */
	public abstract String[] getParentOrgNodePathNameArray() ;
	
	public abstract String[] getParentOrgNodePathTitleArray() ;
	
	public abstract String[] getParentOrgNodePathDescArray() ;
	
	public abstract List<String> getExtNames() ;
	
	public abstract Object getExtValue(String extname) ;
	
	/**
	 * 被Org管理器添加时触发
	 */
	public abstract void onAddedByOrgMgr() ;
	
	/**
	 * 被Org管理器修改时触发
	 *
	 */
	public abstract void onUpdatedByOrgMgr() ;
	
	public String getAutoId()
	{
		return autoId ;
	}
	
	void setOrgNodeId(String un)
	{
		orgNodeId = un ;
	}
	
	public String getOrgNodeId()
	{
		return orgNodeId ;
	}
	
	public OrgNode getOrgNode() throws Exception
	{
		return OrgManager.getDefaultIns().GetOrgNodeById(orgNodeId);
	}
}
