package com.dw.user.access;

import java.util.*;

import com.dw.system.gdb.xorm.*;

/**
 * 临时授权项
 * @author Jason Zhu
 */
@XORMClass(table_name="security_tmp_auth")
public class TmpAuthItem
{
	@XORMProperty(name="AutoId",has_col=true,is_pk=true,is_auto=true)
	String autoId = null ;
	
	/**
	 * 用户名称
	 */
	@XORMProperty(name="UserName",has_col=true,has_idx=true,is_unique_idx=true,max_len=100,order_num=10)
	String userName = null ;
	
	/**
	 * 起始时间
	 */
	@XORMProperty(name="StartDate",has_col=true,order_num=20)
	Date startDate = null ;
	
	/**
	 * 结束时间
	 */
	@XORMProperty(name="EndDate",has_col=true,order_num=30)
	Date endDate = null ;
	
	public TmpAuthItem()
	{}
	
	public TmpAuthItem(String usern,Date startd,Date endd)
	{
		userName = usern ;
		startDate = startd ;
		endDate = endd ;
	}
	
	public String getAutoId()
	{
		return autoId ;
	}
	
	public String getUserName()
	{
		return userName ;
	}
	
	public void setUserName(String usern)
	{
		userName = usern ;
	}
	
	public Date getStartDate()
	{
		return startDate ;
	}
	
	public void setStartDate(Date sd)
	{
		startDate = sd ;
	}
	
	public Date getEndDate()
	{
		return endDate ;
	}
	
	public void setEndDate(Date ed)
	{
		endDate = ed ;
	}
}
