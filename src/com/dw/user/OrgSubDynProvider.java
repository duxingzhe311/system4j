package com.dw.user;

import java.util.*;

/**
 * 很多场合，需要根据业务要求，能够提供动态的子部门
 * 
 * 实现该接口要求如下：
 * 1,主键id用long类型
 * 2,需要自己实现UserName到 子节点id的映射，以满足多用户设置到子节点 （可能还需要提供子节点特有的角色）
 * 3,
 * @author Jason Zhu
 */
public interface OrgSubDynProvider
{
	public static class SubNode
	{
		long subNodeId = -1 ;
		
		String subNodeName = null ;
		
		String subNodeTitle = null ;
		
		public SubNode()
		{}
		
		public SubNode(long subnid,String name,String title)
		{
			subNodeId = subnid ;
			subNodeName = name ;
			subNodeTitle = title ;
		}
		
		public long getSubNodeId()
		{
			return subNodeId ;
		}
		
		public String getSubNodeName()
		{
			return subNodeName ;
		}
		
		public String getSubNodeTitle()
		{
			return subNodeTitle ;
		}
	}
	
	/**
	 * 在用户管理模块中对应的父节点路径名称
	 * @return
	 */
	public String[] getParentOrgNodePathNameArray() ;
	
	public String[] getParentOrgNodePathTitleArray() ;
	
	public String[] getParentOrgNodePathDescArray() ;
	
	/**
	 * 列举所有的子节点
	 * @return
	 */
	public List<SubNode> listSubNodes() ;
	
	public boolean hasSubNodes() ;
	
	public int getSubNodesCount() ;
	/**
	 * 列举
	 * @param subnodeid
	 * @return
	 */
	public List<String> listSubNodeUserNames(long subnodeid) ;
	
	/**
	 * 根据用户名列举包含该用户的子机构节点
	 * @param username
	 * @return
	 */
	public List<SubNode> listSubNodesContainsUserName(String username) ;
	
	/**
	 * 设置用户到子节点中
	 * @param usernames
	 * @param subnodeid
	 */
	public void setUsersToSubNode(String[] usernames,long subnodeid) ;
	
	/**
	 * 取消设置用户到子节点中
	 * @param usernames
	 * @param subnodeid
	 */
	public void unsetUsersFromSubNode(String[] usernames,long subnodeid) ;
}
