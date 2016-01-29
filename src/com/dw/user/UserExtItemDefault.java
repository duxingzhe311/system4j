package com.dw.user;

import java.util.*;

import com.dw.system.gdb.xorm.*;

/**
 * 缺省的用户扩展
 * @author Jason Zhu
 *
 */
@XORMClass(table_name="security_user_exts_def",inherit_parent=true)
public class UserExtItemDefault extends UserExtItem
{
	static ArrayList<String> EXT_NAMES = new ArrayList<String>() ;
	
	static
	{
		EXT_NAMES.add("MobilePhone") ;
		EXT_NAMES.add("Age") ;
		EXT_NAMES.add("Sex") ;
	}
	
	@XORMProperty(name="MobilePhone",title="手机号码",has_col=true,max_len=15,is_unique_idx=false,has_idx=true,order_num=20)
	private String mobilePhone = null ;
	
	@XORMProperty(name="Birtyday",title="生日",has_col=true,order_num=30)
	private Date birthday = null;
	
	@XORMProperty(name="Sex",title="性别",value_options="true:男|false:女",has_col=true,order_num=30)
	private boolean sex = true ;
	
	@Override
	public List<String> getExtNames()
	{
		return EXT_NAMES;
	}

	@Override
	public Object getExtValue(String extname)
	{
		if("MobilePhone".equalsIgnoreCase(extname))
		{
			return mobilePhone;
		}
		
		return null ;
	}

	public String getMobilePhone()
	{
		return mobilePhone ;
	}
	
	public Date getBirthday()
	{
		return birthday ;
	}
	
	public boolean getSex()
	{
		return sex ;
	}
}
