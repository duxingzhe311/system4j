package com.dw.system.dyn_auth;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;

/**
 *
 */
@XORMClass(table_name="system_right")

public class MgrNodeRightRoleItem
{
	
	@XORMProperty(name="pkId",has_col=true,is_pk=true,is_auto=true)
	String pkid = null ;
	
	/**
	 * MgrNode id即MgrNode的name
	 */
	@XORMProperty(name="MgrNodeId",has_col=true,max_len=50)
	String mgrNodeId = null ;

	/**
	 * 权限
	 */
	@XORMProperty(name="RightRuleStr",has_col=true,max_len=2000)
	String rightRuleStr = null ;

	public String getMgrNodeId()
	{
		return mgrNodeId;
	}

	public void setMgrNodeId(String mgrNodeId)
	{
		this.mgrNodeId = mgrNodeId;
	}

	public String getRightRuleStr()
	{
		return rightRuleStr;
	}

	public void setRightRuleStr(String rightRuleStr)
	{
		this.rightRuleStr = rightRuleStr;
	}

	public String getPkid()
	{
		return pkid;
	}

	public void setPkid(String pkid)
	{
		this.pkid = pkid;
	}
}
