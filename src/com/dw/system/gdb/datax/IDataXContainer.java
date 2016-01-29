package com.dw.system.gdb.datax;

/**
 * 为支持DataX中的定义信息能够被远程的应用使用,并且能够统一接口
 * 特定义了该接口
 * 
 * 该接口描述了获取DataX中的相关信息的方法
 * 
 * 如果一个远程应用想获取IDataXContainer中的信息,其内部就必须存在
 * 一个IDataXContainer的具体实现的实例. 这样就可以统一使用DataX中的信息
 * 
 * @author Jason Zhu
 */
public interface IDataXContainer
{
	public DataXBase[] getAllDataXBase();
	
	public DataXBase getDataXBaseByName(String name);
	
	public DataXClass[] getAllDataXClassesInBase(String basen) ;
	
	public DataXClass getDataXClass(String basen,String classn);
}
