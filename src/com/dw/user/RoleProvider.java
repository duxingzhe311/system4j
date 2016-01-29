package com.dw.user;

import java.io.*;
import java.util.*;

public abstract class RoleProvider
{
	public abstract List<Role> GetAllRoles() throws Exception;

	public Role CreateRole(String roleName,String roletitle, String roleDesc) throws Exception
	{
		return CreateRole(roleName,roletitle, roleDesc, "");
	}

	public abstract Role CreateRole(String roleName,String roletitle, String roleDesc,
			String roleExtInfo) throws Exception;

	public boolean ChangeRole(String roleid, String name,String title, String desc) throws Exception
	{
		return ChangeRole(roleid, name,title, desc, "");
	}

	public abstract boolean ChangeRole(String roleid, String name,String title, String desc,
			String roleExtInfo) throws Exception;

	public abstract boolean DeleteRole(String roleid) throws Exception;

//	 public abstract String[] FindUsersInRole(int role_id, String
	// usernameToMatch);

	public abstract List<String> GetRoleIdsForUserName(int domain,String username) throws Exception;

	public abstract List<User> GetUsersInRoles(int domain,String[] role_id) throws Exception;

	public abstract boolean IsUserNameInRole(int domain,String username, String role_id) throws Exception;

	public abstract boolean AddUserNameToRole(int domain,String username, String roleid) throws Exception;

	public void SetUsersToRoles(int domain,String[] usernames, String[] roles) throws Exception
	{
		if (roles == null || usernames == null)
			return;

		for (String r : roles)
		{
			for (String n : usernames)
			{
				try
				{
					AddUserNameToRole(domain,n, r);
				}
				catch (Throwable t)
				{
				}
			}
		}
	}

	public abstract boolean RemoveUserNameFromRole(int domain,String username, String roleid) throws Exception;

	public void RemoveUsersFromRoles(int domain,String[] usernames, String[] role_ids) throws Exception
	{
		if (role_ids == null || usernames == null)
			return;

		for (String r : role_ids)
		{
			for (String n : usernames)
			{
				try
				{
					RemoveUserNameFromRole(domain,n, r);
				}
				catch (Throwable t)
				{
				}
			}
		}
	}

	public abstract boolean RemoveUserAllRoles(int domain,String username) throws Exception;
	// public abstract Role GetRole(int role_id);
}
