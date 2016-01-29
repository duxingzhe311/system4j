package com.dw.user;


import java.io.*;
import java.util.*;

import com.dw.system.gdb.xorm.XORMProperty;

/**
 * �ڲ�ͬ����Ŀ��,���п�������û�������Ϣ֮������һЩ��չ��Ϣ
 * ��Щ��չ��Ϣ������Ŀ����Ҫ������ͬ.
 * 
 * Ϊ�����������չ,�ش˶������û���չ�ļܹ�
 * 
 * Ϊ����㿪��--Ŀ���Ǽ�������д����,�������޶�
 * 1,�û���չ��Ϣ������һ������,���ҷ���XORM�ж���ı��ʽ--Ҳ���Ǽ̳б���
 * 	  ������붨��XORM��Ϣ,��ע��--����֮��Ͳ���Ҫ���κο�����
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
