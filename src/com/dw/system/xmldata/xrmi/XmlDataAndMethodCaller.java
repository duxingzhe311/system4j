package com.dw.system.xmldata.xrmi;

import java.util.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.*;

public class XmlDataAndMethodCaller
{
	static XRmiClassRegister rcr = null;
	
	public static XmlData packObjToXmlData(Object o)
		throws Exception
	{
		XmlData xd = new XmlData();
		
		if(o==null)
		{
			xd.setParamValue("__type", "null");
			return xd ;
		}
		
		if(o instanceof XmlData)
		{
			xd.setParamValue("__type", "xmldata");
			xd.setSubDataSingle("__obj", (XmlData)o);
			return xd ;
		}
		
		if(o instanceof IXmlDataable)
		{
			IXmlDataable xda = (IXmlDataable)o;
			String regname = XRmiClassRegister.getRegNameByClass(o.getClass());
			if(regname==null||regname.equals(""))
				throw new RuntimeException("cannot get register name for class c="+o.getClass().getName());
			
			XmlData tmpxd = xda.toXmlData() ;
			xd.setSubDataSingle("__obj", tmpxd);
			xd.setParamValue("__type", "IXmlDataable");
			
			xd.setParamValue("__type_regname", regname);
			return xd ;
		}
		
		if((o instanceof Object[])&&!(o instanceof byte[]))
		{
			Object[] arrayos = (Object[])o;
			Class compt = o.getClass().getComponentType();
			String xv_type = XmlVal.class2xmlValType(compt);
			
			List<XmlData> subxds = xd.getOrCreateSubDataArray("__obj");
			for(Object tmpo:arrayos)
			{
				XmlData subxd = packObjToXmlData(tmpo) ;
				subxds.add(subxd);
			}
			xd.setParamValue("__type", "array");
			if(xv_type!=null)
				xd.setParamValue("__xmlval_type", xv_type);
			return xd ;
		}
		
		if(o instanceof List)
		{
			List arrayos = (List)o;
			
			List<XmlData> subxds = xd.getOrCreateSubDataArray("__obj");
			for(Object tmpo:arrayos)
			{
				XmlData subxd = packObjToXmlData(tmpo) ;
				subxds.add(subxd);
			}
			xd.setParamValue("__type", "list");
			return xd ;
		}
		
		//基本类型
		xd.setParamValue("__obj", o);
		
		return xd ;
	}
	
	
	
	public static Object unpackObjFromXmlData(XmlData xd)
		throws Exception
	{
		if(xd==null)
			return null;
		
		String type = xd.getParamValueStr("__type");
		if(type==null||type.equals(""))
		{
			//基本类型
			return xd.getParamValue("__obj");
		}
		else if("null".equals(type))
		{
			return null ;
		}
		else if("xmldata".equals(type))
		{
			return xd.getSubDataSingle("__obj");
		}
		else if("array".equals(type))
		{
			List<XmlData> subxds = xd.getSubDataArray("__obj");
			if(subxds==null)
				return null ;
			
			Object[] rets = null ;
			String xv_type = xd.getParamValueStr("__xmlval_type");
			if(xv_type!=null&&!xv_type.equals(""))
				rets = XmlVal.createObjArrayByType(xv_type, subxds.size());
			
			if(rets==null)
				rets = new Object[subxds.size()];
			
			for(int i = 0 ; i < rets.length ; i ++)
			{
				rets[i] = unpackObjFromXmlData(subxds.get(i));
			}
			
			return rets ;
		}
		else if("list".equals(type))
		{
			List<XmlData> subxds = xd.getSubDataArray("__obj");
			if(subxds==null)
				return null ;
			
			int c = subxds.size();
			ArrayList ll = new ArrayList(c);
			
			for(int i = 0 ; i < c ; i ++)
			{
				ll.add(unpackObjFromXmlData(subxds.get(i)));
			}
			
			return ll ;
		}
		else if("IXmlDataable".equals(type))
		{
			String regname = xd.getParamValueStr("__type_regname");
			IXmlDataable tmpxd = XRmiClassRegister.createNewInstanceByRegName(regname);
			if(tmpxd==null)
				throw new RuntimeException("Cannot create new instance for regname="+regname);
			
			XmlData subxd = xd.getSubDataSingle("__obj");
			if(subxd==null)
				return null ;
			
			tmpxd.fromXmlData(subxd) ;
			return tmpxd ;
		}
		else
		{
			throw new RuntimeException("unknow pack data __type"+type);
		}
	}
	
	public static XmlData callServerMethod(Object o,Method m,XmlData xdparam)
		throws Throwable
	{
		Annotation[][] anns = m.getParameterAnnotations();
		Object retobj = null ;
		
		try
		{
			if(anns.length<=0)
			{
				//no method
				retobj = m.invoke(o, (Object[])null);
			}
			else
			{
				Object obj = unpackObjFromXmlData(xdparam);
				Object[] args = (Object[])obj;
				retobj = m.invoke(o, args);
			}
		}
		catch(InvocationTargetException ite)
		{
			throw ite.getTargetException() ;
		}
		
		if(retobj==null)
			return null ;
		
		return packObjToXmlData(retobj);
	}
	
	public static XmlData packClientMethodParams(Object[] params)
		throws Exception
	{
		if(params==null||params.length<=0)
			return null ;
		
		return packObjToXmlData(params);
	}
}
