package com.dw.user;

import java.util.*;

/**
 * �ܶೡ�ϣ���Ҫ����ҵ��Ҫ���ܹ��ṩ��̬���Ӳ���
 * 
 * ʵ�ָýӿ�Ҫ�����£�
 * 1,����id��long����
 * 2,��Ҫ�Լ�ʵ��UserName�� �ӽڵ�id��ӳ�䣬��������û����õ��ӽڵ� �����ܻ���Ҫ�ṩ�ӽڵ����еĽ�ɫ��
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
	 * ���û�����ģ���ж�Ӧ�ĸ��ڵ�·������
	 * @return
	 */
	public String[] getParentOrgNodePathNameArray() ;
	
	public String[] getParentOrgNodePathTitleArray() ;
	
	public String[] getParentOrgNodePathDescArray() ;
	
	/**
	 * �о����е��ӽڵ�
	 * @return
	 */
	public List<SubNode> listSubNodes() ;
	
	public boolean hasSubNodes() ;
	
	public int getSubNodesCount() ;
	/**
	 * �о�
	 * @param subnodeid
	 * @return
	 */
	public List<String> listSubNodeUserNames(long subnodeid) ;
	
	/**
	 * �����û����оٰ������û����ӻ����ڵ�
	 * @param username
	 * @return
	 */
	public List<SubNode> listSubNodesContainsUserName(String username) ;
	
	/**
	 * �����û����ӽڵ���
	 * @param usernames
	 * @param subnodeid
	 */
	public void setUsersToSubNode(String[] usernames,long subnodeid) ;
	
	/**
	 * ȡ�������û����ӽڵ���
	 * @param usernames
	 * @param subnodeid
	 */
	public void unsetUsersFromSubNode(String[] usernames,long subnodeid) ;
}
