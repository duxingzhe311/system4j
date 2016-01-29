package com.dw.system.gdb;

import com.dw.system.Convert;
import com.dw.system.gdb.conf.*;

/**
 * 用来支持XORM对象对应的表的统计配置支持
 * 
 * 如，select count(col1) as col1,sum(col2) as col2,sum(col3) as col3 from t1 where col1>'2009'
 * @author Jason Zhu
 */
public class XORMStatDef
{
	Class xormC = null ;
	XORM xorm = null ;
	
	String[] countPS = null ;
	String[] sumPS = null ;
	
	String[] wherePS = null ;
	String[] whereOpers = null ;
	Object[] whereVals = null ;
	
	String groupByP = null ;
	
	String orderByP = null ;
	
	public XORMStatDef(Class xormc)
	{
		xormC = xormc ;
		xorm = Gdb.getXORMByGlobal(xormc) ;
		if(xorm==null)
			throw new IllegalArgumentException("no xorm found with class="+xormc.getCanonicalName()) ;
	}
	
	public Class getXORMClass()
	{
		return xormC ;
	}
	
	public void setCountPropNames(String[] count_ps)
	{
		countPS = count_ps ;
	}
	
	public void setSumPropNames(String[] sum_ps)
	{
		sumPS = sum_ps ;
	}
	
	public void setWherePropNames(String[] where_ps)
	{
		wherePS = where_ps ;
	}
	
	public void setWhereOpers(String[] where_opers)
	{
		whereOpers = where_opers ;
	}
	
	public void setWhereValues(Object[] where_vals)
	{
		whereVals = where_vals ;
	}
	
	public void setGroupByProp(String gb)
	{
		groupByP = gb ;
	}
	
	public void setOrderByProp(String ob)
	{
		orderByP = ob ;
	}
	
	public String calSqlString() throws ClassNotFoundException
	{
		StringBuilder sb = new StringBuilder() ;
		
		sb.append("select ");
		boolean bfirst = true ;
		if(countPS!=null)
		{
			for(String cp:countPS)
			{
				if(bfirst)
				{
					bfirst = false;
					sb.append("count(").append(cp).append(") as ").append(cp) ;
				}
				else
				{
					sb.append(",count(").append(cp).append(") as ").append(cp) ;
				}
			}
		}
		if(sumPS!=null)
		{
			for(String cp:sumPS)
			{
				if(bfirst)
				{
					bfirst = false;
					sb.append("sum(").append(cp).append(") as ").append(cp) ;
				}
				else
				{
					sb.append(",sum(").append(cp).append(") as ").append(cp) ;
				}
			}
		}
		
		if(Convert.isNotNullEmpty(groupByP))
		{
			sb.append(",").append(groupByP) ;
		}
		sb.append(" from ").append(xorm.getJavaTableInfo().getTableName());
		sb.append(xorm.getXORMWhereSqlByColOpers(wherePS,whereOpers,null,null,null));
		
		if(Convert.isNotNullEmpty(groupByP))
		{
			sb.append(" group by ").append(groupByP) ;
		}
		
		if(Convert.isNotNullEmpty(orderByP))
		{
			sb.append(" order by ").append(orderByP) ;
		}
		
		return sb.toString() ;
	}
}
