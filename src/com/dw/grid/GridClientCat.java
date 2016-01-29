package com.dw.grid;

import com.dw.system.gdb.xorm.*;

@XORMClass(table_name="grid_client_cat")
public class GridClientCat
{
	@XORMProperty(name = "CatId", has_col = true, is_pk = true, is_auto = true)
	String catId = null ;
	
	/**
	 * 
	 */
	@XORMProperty(name = "CatName",max_len=30,has_col=true,order_num=5)
	String catName = null ;
	
	/**
	 * 系统定义的用户名称
	 * null表示此终端没有关联用户
	 */
	@XORMProperty(name = "Title",max_len=100,has_col=true,order_num=8)
	String title = null ;
	
	/**
	 * 
	 */
	@XORMProperty(name = "Description",max_len=1000,has_col=true,order_num=10)
	String desc = null ;
	
	public GridClientCat()
	{}
	
	public GridClientCat(String catn,String title,String desc)
	{
		catName = catn ;
		this.title = title ;
		this.desc = desc ;
	}
	
	public String getCatId()
	{
		return catId ;
	}
	
	public String getCatName()
	{
		return catName ;
	}
	
	public String getTitle()
	{
		return title ;
	}
	
	public String getDescription()
	{
		return desc ;
	}
}
