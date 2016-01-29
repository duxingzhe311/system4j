package com.dw.user;


import java.io.*;
import java.util.*;

import com.dw.system.gdb.xorm.XORMProperty;

/**
 * 在不同的项目中,极有可能针对用户基本信息之外增加一些扩展信息
 * 这些扩展信息根据项目的需要各不相同.
 * 
 * 为了最方便这种扩展,特此定义了用户扩展的架构
 * 
 * 为了最方便开发--目标是几乎不用写程序,有如下限定
 * 1,用户扩展信息必须在一个表上,并且符合XORM中定义的表格式--也就是继承本类
 * 	  的类必须定义XORM信息,并注册--除此之外就不需要做任何开发了
 * 2,
 * 
 * @author Jason Zhu
 */
public abstract class UserExtItem
{
	@XORMProperty(name="AutoId",has_col=true,is_pk=true,is_auto=true)
	protected long autoId = -1 ;
	
	@XORMProperty(name="UserName",has_col=true,max_len=50,has_idx=true,is_unique_idx=true,has_fk=true,fk_table="security_users",fk_column="UserName",order_num=10)
	protected String userName = null ;
	
	public abstract List<String> getExtNames() ;
	
	public abstract Object getExtValue(String extname) ;
	
	public long getAutoId()
	{
		return autoId ;
	}
	
	void setUserName(String un)
	{
		userName = un ;
	}
	
	public String getUserName()
	{
		return userName ;
	}
}
