package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.*;

public class UserUtil
{
	/**
	 * �����û����ƻ���û���Ӧ���ŵĲ��Ź���Ա
	 * @param usern
	 * @return null��ʾʧ�ܣ������û�������,���û�������ĳ�����ŵ�
	 */
	public static List<User> getSubOrgnodeMgrsByUser(String usern)
		throws Exception
	{
		if(Convert.isNullOrEmpty(usern))
			return null ;
		
			ArrayList<String> us = new ArrayList<String>(1) ;
			us.add(usern);
			return getSubOrgnodeMgrsByUsers(us);
	}
	
	public static List<User> getSubOrgnodeMgrsByUsers(List<String> userns)
		throws Exception
	{
		if(userns==null||userns.size()<=0)
		{
			return null;
		}
		
		Role r = RoleManager.getDefaultIns().GetRoleByName(OrgManager.ROLE_SUB_ORGNODE_MGR);
		if(r==null)
		{
			return null;
		}
		
		ArrayList<User> rets = new ArrayList<User>() ;
		
		List<User> us = RoleManager.getDefaultIns().GetUsersInRole(r.getId());
		if(us==null||us.size()<=0)
		{
			return rets;
		}

		HashSet<OrgNode> related_ons = new HashSet<OrgNode>() ;
		for(String usern:userns)
		{
			List<OrgNode> ons = OrgManager.getDefaultIns().GetOrgNodesByUserName(usern);
			if(ons==null)
				continue ;
	
			for(OrgNode tmpon:ons)
			{
				related_ons.add(tmpon) ;
			}
		}
		
		for(User tmpu:us)
		{
			OrgNode tmpon = tmpu.getBelongToOrgNode();
			if(tmpon==null)
				continue ;
			if(related_ons.contains(tmpon))
			{
				rets.add(tmpu) ;
			}
		}
		return rets ;
	}
	
	
	public static List<User> getBelongToOrgnodeMgrsByUser(String usern)
		throws Exception
	{
		if(Convert.isNullOrEmpty(usern))
			return null ;
	
		Role r = RoleManager.getDefaultIns().GetRoleByName(OrgManager.ROLE_SUB_ORGNODE_MGR);
		if(r==null)
		{
			return null;
		}
		
		User u = UserManager.getDefaultIns().GetUser(usern) ;
		if(u==null)
		{
			return null;
		}
	
		OrgNode bon = u.getBelongToOrgNode() ;
		if(bon==null)
		{
			return null;
		}
		
		ArrayList<User> rets = new ArrayList<User>() ;
		
		List<User> us = RoleManager.getDefaultIns().GetUsersInRole(r.getId());
		if(us==null||us.size()<=0)
		{
			return rets;
		}
	
		for(User tmpu:us)
		{
			OrgNode tmpon = tmpu.getBelongToOrgNode();
			if(tmpon==null)
				continue ;
			if(tmpon.equals(bon))
			{
				rets.add(tmpu) ;
			}
		}
		return rets ;
	}
}
