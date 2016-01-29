package com.dw.user.provider;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.*;
import com.dw.system.util.IdCreator;
import com.dw.user.*;


public class DefaultRoleProvider extends RoleProvider
{

	@Override
	public List<Role> GetAllRoles() throws Exception
	{
		return (List<Role>)GDB.getInstance().accessDBAsObjList("Security_Role.GetAllRoles", null, Role.class);
	}

	@Override
	public Role CreateRole(String roleName,String roletitle, String roleDesc, String roleExtInfo) throws Exception
	{
		Hashtable ht = new Hashtable();
		String nid = IdCreator.newSeqId() ;
		ht.put("@RoleId", nid) ;
        ht.put("@RoleName",roleName);
        if(roletitle!=null)
        	ht.put("@RoleTitle", roletitle);
        ht.put("@RoleDesc",roleDesc);
        ht.put("@RoleExtInfo",roleExtInfo);
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.AddRole", ht);
        
        //DataTable dt = dbr.getResultTable(1);
        //Number n = (Number)dt.getFirstColumnOfFirstRow();//dbr.getResultFirstColumnOfFirstRow() ;
        //long rid = n.longValue();
        //if(rid<=0)
        //	return null ;
        return new Role(nid,roleName,roleDesc);
	}

	@Override
	public boolean ChangeRole(String roleid, String name,String title, String desc, String roleExtInfo) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@RoleName",name);
        if(title!=null)
        	ht.put("@RoleTitle", title);
        ht.put("@RoleDesc",desc);
        ht.put("@RoleExtInfo",roleExtInfo);
        ht.put("@RoleId",roleid);
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.UpdateRole", ht);
        return dbr.getLastRowsAffected()==1;
	}

	@Override
	public boolean DeleteRole(String roleid) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@RoleId",roleid);
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.DelRole", ht);
        return dbr.getLastRowsAffected()==1;
	}

	@Override
	public List<String> GetRoleIdsForUserName(int domain,String username) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<=0)
			ht.put("@UserName",username);
		else
			ht.put("@UserName", username+"@"+domain) ;
		
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.GetRoleIdsByUserName", ht);
        ArrayList<String> rets = new ArrayList<String>() ;
        DataTable dt = dbr.getResultFirstTable();
        int rn = dt.getRowNum() ;
        for(int i = 0 ; i < rn ; i ++)
        {
        	DataRow dr = dt.getRow(i);
        	//Number n = (Number)dr.getValue(0);
        	String n = (String)dr.getValue(0);
        	rets.add(n);
        }
        return rets ;
	}

	public List<User> GetUsersInRole(int domain,String role_id) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain = 0 ;
		ht.put("@DomainId", domain) ;
        ht.put("@RoleId",role_id);
        return (List<User>)GDB.getInstance().accessDBAsObjList("Security_Role.GetUsersByRole", ht, User.class);
	}

	@Override
	public List<User> GetUsersInRoles(int domain,String[] role_ids) throws Exception
	{
		if(role_ids==null||role_ids.length<=0)
			return null ;
		
		Hashtable ht = new Hashtable();
		if(domain<0)
			domain = 0 ;
		StringBuilder sb = new StringBuilder() ;
		sb.append('\'').append(role_ids[0]).append('\'') ;
		for(int k = 1 ; k < role_ids.length ; k ++)
			sb.append(",\'").append(role_ids[k]).append("\'") ;
		
		ht.put("@DomainId", domain) ;
        ht.put("$RoleIdStr",sb.toString());
        return (List<User>)GDB.getInstance().accessDBAsObjList("Security_Role.GetUsersByRoles", ht, User.class);
	}
	
	@Override
	public boolean IsUserNameInRole(int domain,String username, String role_id) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@RoleId",role_id);
        if(domain<=0)
			ht.put("@UserName",username);
		else
			ht.put("@UserName", username+"@"+domain) ;
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.IsUserNameInRole",ht) ;
        Number n=(Number)dbr.getResultFirstColumnOfFirstRow();
        return n.intValue()>0 ;
	}

	@Override
	public boolean AddUserNameToRole(int domain,String username, String roleid) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<=0)
			ht.put("@UserName",username);
		else
			ht.put("@UserName", username+"@"+domain) ;
        ht.put("@RoleId",roleid);
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.AddUserNameToRole", ht);
        return dbr.getLastRowsAffected()==1;
	}

	@Override
	public boolean RemoveUserNameFromRole(int domain,String username, String roleid) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<=0)
			ht.put("@UserName",username);
		else
			ht.put("@UserName", username+"@"+domain) ;
        ht.put("@RoleId",roleid);
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.RemoveUserNameFromRole", ht);
        return dbr.getLastRowsAffected()==1;
	}
	
	/**
	 * 删除一个用户的所有角色信息
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean RemoveUserAllRoles(int domain,String username) throws Exception
	{
		Hashtable ht = new Hashtable();
		if(domain<=0)
			ht.put("@UserName",username);
		else
			ht.put("@UserName", username+"@"+domain) ;
        DBResult dbr = GDB.getInstance().accessDB("Security_Role.RemoveUserRoles", ht);
        return dbr.getLastRowsAffected()>=1;
	}
	
	
}
