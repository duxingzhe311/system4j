package com.dw.user.provider;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.*;
import com.dw.system.util.IdCreator;
import com.dw.user.*;

public class DefaultOrgProvider extends OrgProvider
{

	@Override
	public OrgNode CreateOrgNode(String name,String title, String desc, int ordernum, String parent_node_id) throws Exception
	{
		Hashtable ht = new Hashtable();
		String nid = IdCreator.newSeqId() ;
		ht.put("@OrgNodeId", nid) ;
        ht.put("@OrgNodeName",name);
        if(title!=null)
        	ht.put("@OrgNodeTitle", title);
        
        ht.put("@OrgNodeDesc",desc);
        ht.put("@OrgNodeOrder",ordernum);
        ht.put("@OrgNodeParentId",parent_node_id);
        DBResult dbr = GDB.getInstance().accessDB("Security_Org.AddOrgNode", ht);
        //DataTable dt = dbr.getResultTable(1);
        //Number n= (Number)dt.getFirstColumnOfFirstRow();//dbr.getResultFirstColumnOfFirstRow();
        //long nid = n.longValue() ;
        return new OrgNode(nid, name,title, desc,ordernum, parent_node_id);
	}

	@Override
	public boolean UpdateOrgNode(String node_id, String name,String title, String desc, int ordernum) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@OrgNodeName",name) ;
        if(title!=null)
        	ht.put("@OrgNodeTitle", title);
        
        ht.put("@OrgNodeDesc",desc);
        ht.put("@OrgNodeOrder",ordernum);
        ht.put("@OrgNodeId",node_id);
        DBResult dbr = GDB.getInstance().accessDB("Security_Org.UpdateOrgNode", ht);
        return dbr.getLastRowsAffected()==1;
	}

	@Override
	public boolean ChangeOrgNodeParent(String node_id, String new_parent_id) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@OrgNodeParentId",new_parent_id) ;
        ht.put("@OrgNodeId",node_id);
        DBResult dbr = GDB.getInstance().accessDB("Security_Org.ChangeNodeParent", ht);
        return dbr.getLastRowsAffected()==1;
	}

	@Override
	public boolean DeleteOrgNode(String node_id) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@OrgNodeId",node_id);
        DBResult dbr = GDB.getInstance().accessDB("Security_Org.DelOrgNode", ht);
        return dbr.getLastRowsAffected()==1;
	}

	@Override
	public List<OrgNode> GetAllOrgNodes() throws Exception
	{
		return (List<OrgNode>)GDB.getInstance().accessDBAsObjList("Security_Org.GetAllOrgNode", null,OrgNode.class);
	}

	@Override
	public boolean AddUserNameToOrgNode(String username, String node_id) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@UserName",username) ;
        ht.put("@OrgNodeId",node_id);
        DBResult dbr = GDB.getInstance().accessDB("Security_Org.AddUserNameToOrgNode", ht);
        return dbr.getLastRowsAffected()==1;
	}

	@Override
	public List<User> GetUsersInOrgNode(String node_id) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@OrgNodeId",node_id);
        return (List<User>) GDB.getInstance().accessDBAsObjList("Security_Org.GetUsersByOrgNode", ht,User.class);
	}

	@Override
	public List<User> GetUsersInOrgNodes(List<String> node_ids) throws Exception
	{
		if(node_ids==null||node_ids.size()<=0)
			return null ;
		
		StringBuilder sb = new StringBuilder();
		sb.append("'").append(node_ids.get(0)).append("'");
		for(int i = 1 ; i < node_ids.size() ; i ++)
			sb.append(",'").append(node_ids.get(i)).append("'");
		Hashtable ht = new Hashtable();
        ht.put("$OrgNodeIdStr",sb.toString());
        return (List<User>) GDB.getInstance().accessDBAsObjList("Security_Org.GetUsersByOrgNodeIds", ht,User.class);
	}

	@Override
	public List<String> GetOrgNodeIdsByUserName(String username) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@UserName",username);
        DBResult dbr = GDB.getInstance().accessDB("Security_Org.GetOrgNodeIdsByUserName", ht);
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

	@Override
	public boolean RemoveUserNameFromOrgNode(String node_id, String username) throws Exception
	{
		Hashtable ht = new Hashtable();
        ht.put("@UserName",username) ;
        ht.put("@OrgNodeId",node_id);
        DBResult dbr = GDB.getInstance().accessDB("Security_Org.RemoveUserFromOrgNode", ht);
        return dbr.getLastRowsAffected()==1;
	}
}
