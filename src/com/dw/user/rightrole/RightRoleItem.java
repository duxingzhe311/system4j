package com.dw.user.rightrole;

import java.util.Date;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;

@XORMClass(table_name = "security_right_role_item")
public class RightRoleItem
{
	@XORMProperty(name = "RightId", has_col = true, is_pk = true, is_auto = true, order_num = 10, title = "Primary key")
	String rightId = "";

	@XORMProperty(name = "RecordTime", has_col = true, has_idx = true, order_num = 20, title = "Record Time")
	Date recordTime = new Date();
	
	@XORMProperty(name = "RoleId", has_col = true, max_len = 30, order_num = 30, title = "Role Id")
	String roleId = "";
	
	@XORMProperty(name = "RightName", has_col = true, has_idx = true, max_len = 100, order_num = 40, title = "Right Name")
	String rightName = "";
	
	@XORMProperty(name = "HasRight", has_col = true, order_num = 50, title = "HasRight")
	boolean hasRight = false;

	public boolean isHasRight()
	{
		return hasRight;
	}

	public void setHasRight(boolean hasRight)
	{
		this.hasRight = hasRight;
	}

	public Date getRecordTime()
	{
		return recordTime;
	}

	public void setRecordTime(Date recordTime)
	{
		this.recordTime = recordTime;
	}

	public String getRightId()
	{
		return rightId;
	}

	public String getRightName()
	{
		return rightName;
	}

	public void setRightName(String rightName)
	{
		this.rightName = rightName;
	}
	
	public String getRoleId()
	{
		return roleId;
	}

	public void setRoleId(String roleId)
	{
		this.roleId = roleId;
	}
	
}
