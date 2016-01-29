package com.dw.system.xmldata.join;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.*;

/**
 * 业务模块对外的交互XmlData数据都可以抽象成XmlDataInterface
 * 
 * 每个接口都
 * @author Jason Zhu
 */
public class JoinInterface
{
	JoinType pType = JoinType.in;
	String name = null ;
	XmlDataStruct dataStruct = null ;
	
	public JoinInterface(JoinType it,String name,XmlDataStruct xds)
	{
		if(name==null||name.equals(""))
			throw new IllegalArgumentException("interface name cannot be null!");
		
		pType = it ;
		this.name = name ;
		dataStruct = xds ;
		if(dataStruct==null)
			dataStruct = new XmlDataStruct();
	}
	
	public String getJoinInterfaceName()
	{
		return name ;
	}
	
	public JoinType getJoinInterfaceType()
	{
		return pType ;
	}
	
	public XmlDataStruct getJoinInterfaceStruct()
	{
		return dataStruct;
	}
	
	
//	/**
//	 * 当控制器要调用接口时,就通过调用该方法
//	 * @param in_data 如果接口是in,inout类型,则输入in_data有效
//	 * @param out_data 如果接口是out ,inout类型,则外界应该提供一个空XmlData对象
//	 * 		调用结束后,该对象会被自动填入内容.
//	 */
//	public void pulse(XmlData in_data,XmlData out_data)
//		throws Exception
//	{
//		if(pType==Type.in||pType==Type.inout)
//		{
//			//判断输入的数据格式是否有效
//			StringBuffer fr = new StringBuffer();
//			if(!dataStruct.checkMatchStruct(in_data, fr))
//				throw new Exception("input data is not match DataStuct:"+fr.toString());
//		}
//		
//		//provider.onXmlDataInterfacePulsed(name, in_data, out_data);
//		
//		if(pType==Type.out||pType==Type.inout)
//		{
//			//判断产生的输出数据格式是否有效
//			//如果有问题,则问题出现在Provider的输出不符合数据接口定义的结构
//			StringBuffer fr = new StringBuffer();
//			if(!dataStruct.checkMatchStruct(out_data, fr))
//				throw new Exception("output data is not match DataStuct:"+fr.toString());
//		}
//	}
	
	
//	public void pulseInData(IJoinInsRunner r,XmlData inxd)
//	throws Exception
//{
////JoinInterface ji = getJoinInterface(interfacen);
////if (ji == null)
////	throw new Exception("cannot find JoinInterface with name="
////			+ interfacen);
//
//if (pType != Type.in)
//	throw new Exception("interface is no type in!");
//
//r.onPulseInData(ji, inxd);
//}
//
//public XmlData pulseOutData(String interfacen) throws Exception
//{
//JoinInterface ji = getJoinInterface(interfacen);
//if (ji == null)
//	throw new Exception("cannot find JoinInterface with name="
//			+ interfacen);
//
//if (ji.getJoinInterfaceType() != JoinInterface.Type.out)
//	throw new Exception("interface with name =" + interfacen
//			+ " is no type out!");
//
//return onPulseOutData(ji);
//}

//public XmlData pulseInOutData(String interfacen, XmlData in_data)
//	throws Exception
//{
//JoinInterface ji = getJoinInterface(interfacen);
//if (ji == null)
//	throw new Exception("cannot find JoinInterface with name="
//			+ interfacen);
//
//if (ji.getJoinInterfaceType() != JoinInterface.Type.inout)
//	throw new Exception("interface with name =" + interfacen
//			+ " is no type inout!");
//
//return onPulseInOutData(ji, in_data);
//}

}