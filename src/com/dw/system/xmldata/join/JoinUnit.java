package com.dw.system.xmldata.join;

import java.util.ArrayList;

import com.dw.system.xmldata.XmlData;

/**
 * XmlData接口的定义容器,好比芯片的封装主体 它包含接口的定义,和数据存储实例的定义
 * 
 * 实现该接口的类能够指定它能够支持的数据接口
 * 
 * 所有利用join接口的顶层实现主要工作主要针对该类 1,为支持定制继承该类实现不同的单元,为定制提供接口依据
 * 2,在运行时,在特定的实例下(有具体数据),通过继承内部InsXXX类 实现不同接口(只根据接口名称)的具体操作
 * 
 * @author Jason Zhu
 */
public interface JoinUnit
{
	/**
	 * Unit名称
	 * 
	 * @return
	 */
	public String getJoinUnitName();

	/**
	 * 获得所有的XmlDataInterface
	 * 
	 * @return
	 */
	public ArrayList<JoinInterface> getAllJoinInterfaces();

	/**
	 * 根据接口名称获得对应的接口对象
	 * 
	 * @param name
	 * @return
	 */
	public JoinInterface getJoinInterface(String name);
}
