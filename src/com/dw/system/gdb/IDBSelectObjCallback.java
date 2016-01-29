package com.dw.system.gdb;

/**
 * 在一些情况下，需要对查找的数据结果进行处理，但每次需要处理的数据量很大
 * 如果通过直接查找语句查找结果，并形成结果集的话，需要占用极大的内存
 * 
 * 为了避免出现这种情况，外界可以实现处理数据接口，并在数据库查询的时候作为
 * 参数提供。
 * 
 * 该方法为从数据库中列举XORM对象列表过程中，直接进行处理的方法
 * @author Jason Zhu
 *
 */
public interface IDBSelectObjCallback<T>
{
	/**
	 * 返回true表示继续处理下一行的对应XORM对象
	 * 返回false表示停止处理
	 * @param rowidx
	 * @param o
	 * @return
	 */
	public boolean onFindObj(int rowidx,T o);
}
