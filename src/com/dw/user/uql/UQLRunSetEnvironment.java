package com.dw.user.uql;

import java.util.*;

import com.dw.system.codedom.*;
import com.dw.user.*;
import com.dw.user.right.RightManager;

/**
 * 根据条件查询满足条件的用户，角色，组织机构节点
 * 
 * @author Jason Zhu
 */
public class UQLRunSetEnvironment extends AbstractRunEnvironment
{
	RightManager rightMgr = null ;
	
	public UQLRunSetEnvironment(RightManager rm)
	{
		rightMgr = rm ;
	}
	@Override
	protected Object[] globalSupportedObj()
	{
		return new Object[]{new UQLSupporter(rightMgr)};
	}

	@Override
	public IOperRunner getEnvOperRunner(String oper_str)
	{
		if ("or".equals(oper_str) || "||".equals(oper_str)
				|| "|".equals(oper_str))
		{
			return new OperRunnerOr();
		}
		else if ("and".equals(oper_str) || "&".equals(oper_str)
				|| "&&".equals(oper_str))
		{
			return new OperRunnerAnd();
		}

		return null;
	}

	public List<User> getUsersByExp(String exp_str,Hashtable in_params)
		throws Exception
	{
		Set s = (Set)this.runExp(exp_str,null, in_params);
		List<User> us = new ArrayList<User>();
		us.addAll(s);
		return us ;
	}
}

class OperRunnerOr implements IOperRunner
{
	public Object calculate(AbstractRunEnvironment env, RunContext context,
			IDomNode leftn, IDomNode rightn, Object leftv, Object rightv)
	{
		Set lb = (Set) leftv;
		Set rb = (Set) rightv;
		// 运行并集操作
		Set ns = new HashSet();
		if (lb != null)
			ns.addAll(lb);
		if (rb != null)
			ns.addAll(rb);
		return ns;
	}
}

class OperRunnerAnd implements IOperRunner
{
	public Object calculate(AbstractRunEnvironment env, RunContext context,
			IDomNode leftn, IDomNode rightn, Object leftv, Object rightv)
	{
		if (leftv == null)
			return null;

		if (rightv == null)
			return null;

		Set lb = (Set) leftv;
		Set rb = (Set) rightv;
		// 运行交集操作
		HashSet hs = new HashSet();
		for (Iterator ir = lb.iterator(); ir.hasNext();)
		{
			Object v = ir.next();
			if (rb.contains(v))
				hs.add(v);
		}

		return hs;
	}
}

class UQLSupporter
{
	RightManager rightMgr = null ;
	
	public UQLSupporter(RightManager rm)
	{
		rightMgr = rm ;
	}
	
	public Role getRoleByName(String role_name) throws Exception
	{
		return rightMgr.getRoleManager().GetRoleByName(role_name);
	}

	public Set getUserSetInRole(String role_name) throws Exception
	{
		Role r = rightMgr.getRoleManager().GetRoleByName(role_name);
		if(r==null)
			return null ;
		
		List<User> us = rightMgr.getRoleManager().GetUsersInRole(r.getId());
		HashSet hs = new HashSet();
		hs.addAll(us);
		return hs;
	}

	public Set getUserSetInOrgNode(String orgnode_name) throws Exception
	{
		OrgManager om = rightMgr.getOrgManager();
		List<OrgNode> tmpons = om.findOrgNodeByName(orgnode_name);
		if (tmpons == null || tmpons.size() <= 0)
			return null;

		List<User> us = om.GetUsersInOrgNode(tmpons.get(0).getOrgNodeId());
		HashSet hs = new HashSet();
		hs.addAll(us);
		return hs;
	}
}