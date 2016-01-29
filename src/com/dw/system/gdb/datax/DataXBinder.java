package com.dw.system.gdb.datax;

import java.util.*;

import com.dw.system.xmldata.IXmlDataable;

/**
 * DataX对象绑定支持
 * 
 * 以DataX为基础,为顶层使用DataX的类提供快速开发应用的支持.
 * 
 * 如果顶层应用要使用DataX,则只需要实现的相关的接口和符合规定的模式
 * 1,DataX的数据保存
 * 2,DataX对应的Seperate信息可以通过独立的内容获得
 * 
 * @author Jason Zhu
 */
public class DataXBinder
{
	public static interface IBinderStub extends IXmlDataable
	{
		public long getDataXId();
		
		public void setDataXId(long xdid);
	}
	
	/**
	 * 一个类如果实现该接口,则说明它的构造需要从DataX中获取完整的数据
	 * 
	 * @author Jason Zhu
	 *
	 */
	public static interface IBinderFull extends IXmlDataable
	{
		public long getDataXId();
		
		public void setDataXId(long xdid);
	}
	
	public List<IBinderStub> listBinderStubs()
	{
		return null ;
	}
	
	public IBinderFull getBinderFull(long xdid)
	{
		return null ;
	}
}
