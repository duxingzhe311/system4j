package com.dw.system.gdb;

/**
 * 在一些情况下，需要对查找的数据结果进行处理，但每次需要处理的数据量很大
 * 如果通过直接查找语句查找结果，并形成结果集的话，需要占用极大的内存
 * 
 * 为了避免出现这种情况，外界可以实现处理数据接口，并在数据库查询的时候作为
 * 参数提供。
 * 
 * GDB在结果集中滚动过程中，就可以直接处理，并没必要形成结果集。并且可以根据情况
 * 随时中断结束。
 * 
 * 使用回调方式的数据库查询访问，特别适用与不需要返回结果集的后台任务处理情况
 * 
 * @author Jason Zhu
 */
public interface IDBSelectCallback
{
	/**
	 * 当一个数据查询的结果的表结构发现时，回调的方法
	 * 该方法如果返回true，则继续处理每一行.
	 * 
	 * 如果false，则后续的处理立刻结束
	 * @param dt
	 * @return
	 * @throws Exception
	 */
	public boolean onFindDataTable(int tableidx,DataTable dt) throws Exception ;
	
	/**
	 * 在处理每一行时应该考虑的内容
	 * 
	 * @param tableidx
	 * @param dt
	 * @param rowidx
	 * @param dr
	 * @return 如果false，则后续的处理立刻结束
	 * @throws Exception
	 */
	public boolean onFindDataRow(int tableidx,DataTable dt,int rowidx,DataRow dr) throws Exception;
}


class XORMSelCallback implements IDBSelectCallback
{
	Class xormC = null ;
	IDBSelectObjCallback objCB = null ;
	
	public XORMSelCallback(Class xormc,IDBSelectObjCallback objcb)
	{
		xormC = xormc ;
		objCB = objcb ;
	}
	
	public boolean onFindDataTable(int tableidx, DataTable dt)
	{
		return true;
	}

	public boolean onFindDataRow(int tableidx, DataTable dt, int rowidx, DataRow dr)
	 throws Exception
	{
		Object o = DBResult.transDataRow2XORMObj(xormC,dr) ;
		return objCB.onFindObj(rowidx, o) ;
	}
	
}
