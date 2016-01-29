package com.dw.system.xmldata.join;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.*;

/**
 * ҵ��ģ�����Ľ���XmlData���ݶ����Գ����XmlDataInterface
 * 
 * ÿ���ӿڶ�
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
//	 * ��������Ҫ���ýӿ�ʱ,��ͨ�����ø÷���
//	 * @param in_data ����ӿ���in,inout����,������in_data��Ч
//	 * @param out_data ����ӿ���out ,inout����,�����Ӧ���ṩһ����XmlData����
//	 * 		���ý�����,�ö���ᱻ�Զ���������.
//	 */
//	public void pulse(XmlData in_data,XmlData out_data)
//		throws Exception
//	{
//		if(pType==Type.in||pType==Type.inout)
//		{
//			//�ж���������ݸ�ʽ�Ƿ���Ч
//			StringBuffer fr = new StringBuffer();
//			if(!dataStruct.checkMatchStruct(in_data, fr))
//				throw new Exception("input data is not match DataStuct:"+fr.toString());
//		}
//		
//		//provider.onXmlDataInterfacePulsed(name, in_data, out_data);
//		
//		if(pType==Type.out||pType==Type.inout)
//		{
//			//�жϲ�����������ݸ�ʽ�Ƿ���Ч
//			//���������,�����������Provider��������������ݽӿڶ���Ľṹ
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