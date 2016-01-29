package com.dw.grid;

import java.util.*;

import com.dw.system.gdb.xorm.*;

/**
 * 存放网格定义的表
 * 当定义一个区域时-需要对格子定义进行保存
 * 
 * @author Jason Zhu
 */
@XORMClass(table_name="grid_def")
public class GridDefOneItem
{
	/**
	 * 终端id
	 */
	@XORMProperty(name = "AutoId", has_col = true, is_pk = true, is_auto = true)
	long autoId = -1 ;
	
	/**
	 * 在配置中定义的名词。他对应一个基点和一个区域起点
	 */
	@XORMProperty(name = "DefName",max_len=30, has_col = true,has_idx=true, order_num=7)
	String defName = null ;
	
	/**
	 * 网格代码，形如 A1 B3 A1-4 等
	 * 如果是大格子-则代表大格子内的9个小格
	 */
	@XORMProperty(name = "GridCode",max_len=8, has_col = true, order_num=10)
	String gridCode = null ;
	
	public GridDefOneItem()
	{}
	
	public GridDefOneItem(String defn,String gridc)
	{
		defName = defn ;
		gridCode = gridc ;
	}
	
	public long getAutoId()
	{
		return autoId ;
	}
	
	public String getDefName()
	{
		return defName ;
	}
	
	public String getGridCode()
	{
		return gridCode ;
	}
	
	private transient ArrayList<String> smGCS = null ;
	
	public ArrayList<String> getSmallGridCode()
	{
		if(smGCS!=null)
			return smGCS ;
		
		smGCS =GridDef.calSmallGridCodes(gridCode);
		return smGCS ;
	}
}
