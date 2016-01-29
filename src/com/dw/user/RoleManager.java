package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.logger.*;
import com.dw.user.provider.DefaultOrgProvider;
import com.dw.user.provider.DefaultRoleProvider;

public class RoleManager
{
	static Object lockObj = new Object();
	
	static RoleManager defRoleMgr = null;

	static ILogger log = LoggerManager.getLogger(RoleManager.class
			.getCanonicalName());

	public static RoleManager getDefaultIns()
	{
		if(defRoleMgr!=null)
			return defRoleMgr ;
		
		synchronized(lockObj)
		{
			if(defRoleMgr!=null)
				return defRoleMgr ;
			
			defRoleMgr = new RoleManager(new DefaultRoleProvider());
			return defRoleMgr ;
		}
	}
	
//	public static RoleManager getInstance()
//	{
//		if (roleMgr != null)
//			return roleMgr;
//
//		try
//		{
//			RoleProvider rp = null;// get from config
//			roleMgr = new RoleManager(rp);
//			return roleMgr;
//		}
//		catch (Exception ee)
//		{
//			log.error(ee);
//			ee.printStackTrace();
//			return null;
//		}
//	}

	private RoleProvider _provider = null;// new DefaultRoleProvider();

	private RoleProvider getProvider()
	{
		return _provider;
	}

	List<Role> allRoles = null;

	Hashtable<String, Role> id2role = null;
	
	transient private boolean bInited = false; 

	public RoleManager(RoleProvider rp)
	{
		_provider = rp;
		// 可能访问数据库作所有角色的装载
		//init();
	}

	private void init() throws Exception
	{
		if(bInited)
			return ;
		
		synchronized(this)
		{
			if(bInited)
				return ;
			
			List<Role> tmprs = getProvider().GetAllRoles();
			if (tmprs == null)
				tmprs = new ArrayList<Role>();
	
			Hashtable<String, Role> id2r = new Hashtable<String, Role>();
			
			for (Role r : tmprs)
			{
				id2r.put(r.getId(), r);
			}
			
			allRoles = tmprs ;
			id2role = id2r;
			
			bInited = true ;
		}
	}
	// / <summary>
	// / 获得所有的角色
	// / </summary>
	public Role[] getAllRoles() throws Exception
	{
		if(!bInited)
			init();
		
		Role[] rs = new Role[allRoles.size()];
		allRoles.toArray(rs);
		return rs;
	}

	// / <summary>
	// / 获取用户web下拉列表用的ListItem[]数组
	// / </summary>
	// public ListItem[] WebListItemAllRoles
	// {
	// get
	// {
	// List<ListItem> ll = new List<ListItem>();
	//
	// foreach(Role r in allRoles)
	// {
	// ll.Add(new ListItem(r.Name,""+r.Id)) ;
	// }
	//
	// return ll.ToArray();
	// }
	// }
	// / <summary>
	// / 根据id获取角色对象
	// / </summary>
	// / <param name="id"></param>
	// / <returns></returns>
	public Role GetRole(String id) throws Exception
	{
		if(!bInited)
			init();
		
		return id2role.get(id);
	}

	// / <summary>
	// / 根据角色名称前缀，获取所有相关角色
	// / </summary>
	// / <param name="name_prefix"></param>
	// / <returns></returns>
	public List<Role> GetRolesByPrefix(String name_prefix)
	{
		return null;
	}
	
	/**
	 * 获得或者创建对应的角色
	 * @param rolename
	 * @return
	 * @throws Exception
	 */
	public Role getOrCreateRole(String rolename) throws Exception
	{
		Role r = GetRoleByName(rolename) ;
		if(r!=null)
			return r ;
		
		synchronized(this)
		{
			return CreateRole(rolename,rolename) ;
		}
	}
	
	public void createRoleWhenNotExists(String rolename,String title) throws Exception
	{
		Role r = GetRoleByName(rolename) ;
		if(r!=null)
			return  ;
		
		synchronized(this)
		{
			CreateRole(rolename,title,title) ;
		}
	}

	public Role GetRoleByName(String role_name) throws Exception
	{
		if(!bInited)
			init();
		
		for (Role r : allRoles)
		{
			if (r.getName().equals(role_name))
				return r;
		}

		return null;
	}
	
	public Role CreateRole(String name,String desc) throws Exception
	{
		return CreateRole(name,null, desc) ;
	}

	public Role CreateRole(String name,String title, String desc) throws Exception
	{
		if(!bInited)
			init();
		
		//if (name == null || name.equals(""))
		//	throw new IllegalArgumentException("name cannot be null or empty!");
		Convert.checkVarName(name);

		Role r = GetRoleByName(name);
		if (r != null)
			throw new RuntimeException("role with name " + name
					+ "is already existed");
		
		r = getProvider().CreateRole(name,title, desc);

		bInited = false ;
		init();
		
		return r ;
	}
	
	public void ChangeRole(String roleid, String name, String desc)
	throws Exception
	{
		ChangeRole(roleid, name,null, desc);
	}

	public void ChangeRole(String roleid, String name,String title, String desc)
			throws Exception
	{
		if(!bInited)
			init();
		
		//if (name == null || name.equals(""))
		//	throw new IllegalArgumentException("name cannot be null or empty!");
		Convert.checkVarName(name);

		Role r = GetRole(roleid);
		if (r == null)
			throw new RuntimeException("Cannot find role with id=" + roleid);

		if(r.getName().equals("root"))
			throw new RuntimeException("root role cannot be changed!");
		
		if (r.getName().equals(name))
		{
			if(Convert.checkStrEqualsIgnoreNullEmpty(r.getDesc(),desc)
					&&
					Convert.checkStrEqualsIgnoreNullEmpty(r.getTitle(),title))
				return ;
		}
		else
		{
			Role tmpr = GetRoleByName(name);
			if (tmpr != null)
				throw new RuntimeException("role with name " + name
						+ "is already existed");
		}

		getProvider().ChangeRole(roleid, name,title, desc);

		r.name = name;
		r.title = title ;
		r.desc = desc;
	}

	public void DeleteRole(String roleid) throws Exception
	{
		if(!bInited)
			init();
		
		if (roleid==null)
			throw new IllegalArgumentException("roleid is null");

		Role r = GetRole(roleid);
		if (r == null)
			throw new RuntimeException("Cannot find role with id=" + roleid);

		if(r.getName().equals("root"))
			throw new RuntimeException("root role cannot be deleted!");
		
		getProvider().DeleteRole(roleid);
		
		bInited = false;
		init();
	}
	
	public void SetUsersToRoles(String[] usernames, String[] roleids)
		throws Exception
	{
		SetUsersToRoles(0,usernames, roleids);
	}

	public void SetUsersToRoles(int domain,String[] usernames, String[] roleids)
			throws Exception
	{
		if(!bInited)
			init();
		
		getProvider().SetUsersToRoles(domain,usernames, roleids);
	}

	// public String[] FindUsersInRole(int role_id, String usernameToMatch)
	// {
	// return Provider.FindUsersInRole(role_id, usernameToMatch);
	// }
	public List<Role> GetRolesForUser(String username) throws Exception
	{
		return GetRolesForUser(0,username);
	}
	
	public List<Role> GetRolesForUser(int domain,String username) throws Exception
	{
		if(!bInited)
			init();
		
		List<String> rids = getProvider().GetRoleIdsForUserName(domain,username);
		List<Role> rs = new ArrayList<Role>(rids.size());
		for(String rid:rids)
		{
			rs.add(this.GetRole(rid));
		}
		return rs;
	}
	
	public List<User> GetUsersInRole(String role_id) throws Exception
	{
		return GetUsersInRole(0,role_id);
	}

	public List<User> GetUsersInRole(int domain,String role_id) throws Exception
	{
		if(!bInited)
			init();
		
		return getProvider().GetUsersInRoles(domain,new String[]{role_id});
	}
	
	public List<User> GetUsersInRoles(int domain,String[] role_ids) throws Exception
	{
		if(!bInited)
			init();
		
		return getProvider().GetUsersInRoles(domain,role_ids);
	}
	
	public boolean IsUserInRole(String username, String role_id) throws Exception
	{
		return IsUserInRole(0,username, role_id);
	}

	public boolean IsUserInRole(int domain,String username, String role_id) throws Exception
	{
		if(!bInited)
			init();
		
		return getProvider().IsUserNameInRole(domain,username, role_id);
	}
	
	public boolean isRootUser(String usern) throws Exception
	{
		if(!bInited)
			init();
		
		Role r = this.GetRoleByName("root");
		if(r==null)
			return false;
		
		return IsUserInRole(usern, r.getId());
	}
	
	public void RemoveUsersFromRoles(String[] usernames, String[] role_ids)
		throws Exception
	{
			RemoveUsersFromRoles(0,usernames, role_ids);
	}

	public void RemoveUsersFromRoles(int domain,String[] usernames, String[] role_ids)
			throws Exception
	{
		if(!bInited)
			init();
		
		if(role_ids!=null)
		{
			for(String rid:role_ids)
			{
				if(!"1".equals(rid))
					continue ;
				
				if("1".equals(rid))
				{
					if(usernames!=null&&domain==0)
					{
						for(String un:usernames)
						{
							if("root".equals(un))
								throw new Exception("user=root cannot be remove from role=root");
						}
					}
				}
			}
		}
		
		getProvider().RemoveUsersFromRoles(domain,usernames, role_ids);
	}
	
	
	/**
	 * 删除一个用户的所有角色
	 * @param username
	 * @throws Exception
	 */
	public void removeUserAllRoles(String username)throws Exception
	{
		removeUserAllRoles(0,username);
	}
	
	public void removeUserAllRoles(int domain,String username)throws Exception
	{
		if("root".equals(username))
			throw new Exception("user=root cannot be remove");
		
		if(!bInited)
			init();
		
		getProvider().RemoveUserAllRoles(domain,username);
	}
}
