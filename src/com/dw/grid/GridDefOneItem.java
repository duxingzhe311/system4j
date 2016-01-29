package com.dw.grid;

import java.util.*;

import com.dw.system.gdb.xorm.*;

/**
 * ���������ı�
 * ������һ������ʱ-��Ҫ�Ը��Ӷ�����б���
 * 
 * @author Jason Zhu
 */
@XORMClass(table_name="grid_def")
public class GridDefOneItem
{
	/**
	 * �ն�id
	 */
	@XORMProperty(name = "AutoId", has_col = true, is_pk = true, is_auto = true)
	long autoId = -1 ;
	
	/**
	 * �������ж�������ʡ�����Ӧһ�������һ���������
	 */
	@XORMProperty(name = "DefName",max_len=30, has_col = true,has_idx=true, order_num=7)
	String defName = null ;
	
	/**
	 * ������룬���� A1 B3 A1-4 ��
	 * ����Ǵ����-����������ڵ�9��С��
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
