package com.dw.user;

import java.io.*;
import java.util.*;

public abstract class OrgProvider
{
	public abstract OrgNode CreateOrgNode(String name,String title, String desc,
			int ordernum, String parent_node_id) throws Exception;

	public abstract boolean UpdateOrgNode(String node_id, String name,String title,
			String desc, int ordernum) throws Exception;

	public abstract boolean ChangeOrgNodeParent(String node_id, String new_parent_id) throws Exception;

	public abstract boolean DeleteOrgNode(String node_id) throws Exception;

	public abstract List<OrgNode> GetAllOrgNodes() throws Exception;

	// public abstract List<OrgNode> GetOrgNodesByParentNodeId(int
	// parent_node_id);

	public void SetUserNamesToOrgNode(String[] usernames, String node_id) throws Exception
	{
		if (usernames == null || usernames.length == 0)
			return;

		for (String n : usernames)
		{
			try
			{
				AddUserNameToOrgNode(n, node_id);
			}
			catch (Throwable t)
			{
			}
		}
	}

	public abstract boolean AddUserNameToOrgNode(String username, String node_id) throws Exception;

	public abstract List<User> GetUsersInOrgNode(String node_id) throws Exception;

	public abstract List<User> GetUsersInOrgNodes(List<String> node_ids) throws Exception;
	
	public abstract List<String> GetOrgNodeIdsByUserName(String username) throws Exception;
	
	public void UnsetUserNamesFromOrgNode(String[] usernames, String node_id) throws Exception
	{
		if (usernames == null || usernames.length == 0)
			return;

		for (String n : usernames)
		{
			try
			{
				RemoveUserNameFromOrgNode(node_id, n);
			}
			catch (Throwable t)
			{
			}
		}
	}

	public abstract boolean RemoveUserNameFromOrgNode(String node_id,
			String username) throws Exception;
}