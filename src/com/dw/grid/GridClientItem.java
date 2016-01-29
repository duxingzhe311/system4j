package com.dw.grid;

import java.util.*;

//import com.dw.ext.notify.NotifyItem.State;
import com.dw.system.gdb.xorm.*;

@XORMClass(table_name="grid_client")
public class GridClientItem
{
	public static enum State
	{
		Normal(0),//
		Invalid(1);//��Ч
		
		
		private final int val ;
		
		State(int v)
		{
			val = v ;
		}
		
		public int getValue()
		{
			return val ;
		}
		
		public static State valueOf(int v)
		{
			switch(v)
			{
			case 0:
			    return Normal;
			case 1:
			    return Invalid;
			default:
				return Normal ;
			}
		}
	}
	
	/**
	 * �ն�id
	 */
	@XORMProperty(name = "ClientId", has_col = true, is_pk = true, is_auto = true)
	long clientId = -1 ;
	
	/**
	 * ϵͳ������û�����
	 * null��ʾ���ն�û�й����û�
	 */
	@XORMProperty(name = "UserName",max_len=100,has_idx=true,has_col=true,order_num=3)
	String userName = null ;
	
	/**
	 * �ն˷���id
	 * �ܶ�������ն����ⲿ��Աʹ�ã�������Ա��ϵͳ��û���ʺţ�����Ҫ���з���
	 */
	@XORMProperty(name = "CatName",max_len=20,has_idx=true,has_col=true,order_num=4)
	String clientCatName = null ;
	
	/**
	 * ֪ͨ��״̬
	 */
	State state = State.Normal;
	@XORMProperty(name = "State",has_col = true,order_num=5)
	private int get_State()
	{
		if(state==null)
			return State.Normal.getValue() ;
		return state.getValue() ;
	}
	private void set_State(int st)
	{
		state = State.valueOf(st) ;
	}
	
	@XORMProperty(name = "Title",max_len=30, has_col = true, order_num=7)
	String title = null ;
	
	/**
	 * ���ܴ�-Ŀǰ֧��8λ
	 */
	@XORMProperty(name = "SecKey",max_len=8, has_col = true, order_num=10)
	String secKey = null ;
	
	/**
	 * �ն˵绰
	 */
	@XORMProperty(name = "PhoneNum",max_len=20, has_col = true,has_idx=true, order_num=20)
	String phoneNum = null ;
	
	/**
	 * �绰����ʱ��
	 */
	@XORMProperty(name = "PhoneNumUpDT",has_col = true,order_num=25)
	Date phoneNumUpDT = null ;
	
	/**
	 * 
	 */
	@XORMProperty(name = "Email",max_len=20, has_col = true, order_num=30)
	String email = null ;
	
	public GridClientItem()
	{}
	
	public GridClientItem(String title,String phonenum,String email)
	{
		this.title = title ;
		//secKey = sec_key ;
		phoneNum = phonenum ;
		this.email = email ;
	}
	
	public long getClientId()
	{
		return clientId ;
	}
	
	public String getUserName()
	{
		return userName ;
	}

	public String getClientCatName()
	{
		return clientCatName ;
	}
	
	public void setClientCatName(String catn)
	{
		clientCatName = catn ;
	}
	
	public State getState()
	{
		return state ;
	}
	
	public String getTitle()
	{
		return title ;
	}
	/**
	 * ����ն���Կ
	 * @return
	 */
	public String getSecKey()
	{
		return secKey ;
	}
	
	public String getPhoneNum()
	{
		return phoneNum ;
	}
	
	public Date getPhoneNumUpDT()
	{
		return phoneNumUpDT ;
	}
	
	public String getEmail()
	{
		return email ;
	}
}
