package com.dw.system.gdb.xorm;

import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.dw.system.*;
import com.dw.system.gdb.DataRow;
import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.conf.DBType;
import com.dw.system.gdb.conf.Func;
import com.dw.system.gdb.conf.ORMap;
import com.dw.system.gdb.conf.XORM;
import com.dw.system.gdb.conf.autofit.*;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.XmlDataStruct.StoreType;

public class XORMUtil
{
	public static final String COL_XORM_EXT = "xorm_ext";

	public static class XORMPropWrapper implements Comparable<XORMPropWrapper>
	{
		XORMProperty xormP = null;

		Member fieldOrMethod = null;
		
		boolean bSynClient = false;

		XORMPropWrapper(XORMProperty xormp, Member m)
		{
			xormP = xormp;
			fieldOrMethod = m;
		}

		public XORMProperty getXORMProperty()
		{
			return xormP;
		}

		public Member getFieldOrMethod()
		{
			return fieldOrMethod;
		}
		
//		public Class getFieldOrMethodValType()
//		{
//			if(fieldOrMethod instanceof Field)
//			{
//				Field f = (Field)fieldOrMethod;
//				f.getType()
//			}
//		}
		
		public String getFieldOrMethodName()
		{
			return fieldOrMethod.getName();
		}

		public int compareTo(XORMPropWrapper o)
		{
			return xormP.order_num() - o.xormP.order_num();
		}
	}
	
	public static final String ATTRN_IN_XMLDATA = "biz_xml_data";
	
	public static final String ATTRN_OUT_XMLDATA = "biz_out_xmldata";
	/**
	 * 根据web请求对象,获得对应的提交XmlData
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public static XmlData getXmlDataFromRequest(HttpServletRequest req) throws Exception
	{
		return Convert.getXmlDataFromRequest(req) ;
	}
	
	
	public static void updateXmlDataFromHttpRequest(XmlData xd,
			HttpServletRequest req, String p_prefix) throws Exception
	{
		XmlData.updateXmlDataFromHttpRequest(xd,req, p_prefix) ;
	}
	
	/**
	 * 递归查找类中应该使用的XORMProperty定义的域
	 * @param c
	 * @return
	 */
	private static ArrayList<Field> listXORMPropFields(Class c)
	{
		ArrayList<Field> rets = new ArrayList<Field>() ;
		listXORMPropFields(c,rets);
		return rets ;
	}
	
	private static void listXORMPropFields(Class c,ArrayList<Field> fs)
	{
		XORMClass xormc = (XORMClass) c.getAnnotation(XORMClass.class);
		
		for(Field f:c.getDeclaredFields())
		{
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;
			
			fs.add(f);
		}
		
		if(xormc!=null&&xormc.inherit_parent())
		{//从父类中提取
			Class sc = c.getSuperclass();
			if(sc!=null)
			{
				listXORMPropFields(sc,fs) ;
			}
		}
	}
	
	private static Field getXORMPropField(Class c,String fieldn)
	{
		Field f = null;
		try
		{
			f = c.getDeclaredField(fieldn) ;
			if(f!=null)
				return f ;
		}
		catch(NoSuchFieldException nsme)
		{
			
		}
		
		XORMClass xormc = (XORMClass) c.getAnnotation(XORMClass.class);
		if(xormc!=null&&xormc.inherit_parent())
		{//从父类中提取
			Class sc = c.getSuperclass();
			if(sc!=null)
			{
				return getXORMPropField(sc,fieldn);
			}
		}
		
		return null ;
	}
	
	/**
	 * 递归查找类中应该使用的XORMProperty定义的方法
	 * @param c
	 * @return
	 */
	private static ArrayList<Method> listXORMPropMethods(Class c)
	{
		ArrayList<Method> rets = new ArrayList<Method>() ;
		listXORMPropMethods(c,rets);
		return rets ;
	}
	
	private static void listXORMPropMethods(Class c,ArrayList<Method> ms)
	{
		XORMClass xormc = (XORMClass) c.getAnnotation(XORMClass.class);
		
		for(Method f:c.getDeclaredMethods())
		{
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;
			
			ms.add(f);
		}
		
		if(xormc!=null&&xormc.inherit_parent())
		{//从父类中提取
			Class sc = c.getSuperclass();
			if(sc!=null)
			{
				listXORMPropMethods(sc,ms) ;
			}
		}
	}
	
	
	private static Method getXORMPropMethod(Class c,String methodn,Class[] parms)
	{
		Method m = null;
		try
		{
			m = c.getDeclaredMethod(methodn, parms) ;
			if(m!=null)
				return m ;
		}
		catch(NoSuchMethodException nsme)
		{
			
		}
		
		XORMClass xormc = (XORMClass) c.getAnnotation(XORMClass.class);
		if(xormc!=null&&xormc.inherit_parent())
		{//从父类中提取
			Class sc = c.getSuperclass();
			if(sc!=null)
			{
				return getXORMPropMethod(sc,methodn,parms);
			}
		}
		
		return null ;
	}
	
	public static XORMPropWrapper extractPkXORMPropWrapper(Class c)
	{
		if (c == null)
			return null;

		for (Field f : listXORMPropFields(c))
		{
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;

			if (xdf.is_pk())
				return new XORMPropWrapper(xdf, f);
		}
		return null;
	}

	public static HashMap<XORMPropWrapper, Class> extractXORMProperties(Class c)
	{
		if (c == null)
			return null;

		HashMap<XORMPropWrapper, Class> rets = new HashMap<XORMPropWrapper, Class>();

		for (Field f : listXORMPropFields(c))
		{
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;

			String n = xdf.name();
			if (n == null || n.equals(""))
				continue;

			Class dc = f.getType();
			rets.put(new XORMPropWrapper(xdf, f), dc);
		}

		for (Method m : listXORMPropMethods(c))
		{
			XORMProperty xdf = m.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;

			String n = xdf.name();
			if (n == null || n.equals(""))
				continue;

			String mn = m.getName();
			Class dc = null;
			if (mn.startsWith("get"))
				dc = m.getReturnType();// .getType();//.getDeclaringClass();
			else if (mn.startsWith("set"))
				dc = m.getParameterTypes()[0];

			if (dc == null)
				continue;

			rets.put(new XORMPropWrapper(xdf, m), dc);
		}
		return rets;
	}

	/**
	 * 从类中分析成员,并提取出该类可以提供的XmlData结构
	 * 
	 * @param c
	 * @return
	 */
	public static XmlDataStruct extractXDSFromClass(Class c)
	{
		if (c == null)
			return null;

		XmlDataStruct xds = new XmlDataStruct();

		for (Field f : listXORMPropFields(c))
		{
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;
			
			if(xdf.is_transient())
				continue;

			String n = xdf.name();
			if (n == null || n.equals(""))
				continue;

			Class dc = f.getType();// .getDeclaringClass();

			IXmlDataDef xdd = transPropertyAndValClass(xdf, dc);
			if (xdd == null)
				continue;

			if (xdd instanceof XmlValDef)
			{
				xds.setXmlDataMember(n, (XmlValDef) xdd).setOrderNum(xdf.order_num());
			}
			else if (xdd instanceof XmlDataStruct)
			{
				XmlDataStruct tmpxds = (XmlDataStruct) xdd;
				tmpxds.setName(n);
				xds.setSubStruct(tmpxds);
			}
		}

		for (Method m : listXORMPropMethods(c))
		{
			XORMProperty xdf = m.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;
			
			if(xdf.is_transient())
				continue;

			String n = xdf.name();
			if (n == null || n.equals(""))
				continue;

			String mn = m.getName();
			Class dc = null;
			if (mn.startsWith("get"))
				dc = m.getReturnType();// .getType();//.getDeclaringClass();
			else if (mn.startsWith("set"))
				dc = m.getParameterTypes()[0];

			if (dc == null)
				throw new RuntimeException(
						"invalid method with annotion with XORMProperty,no value data type found!");

			IXmlDataDef xdd = transPropertyAndValClass(xdf, dc);
			if (xdd == null)
				continue;

			if (xdd instanceof XmlValDef)
			{
				xds.setXmlDataMember(n, (XmlValDef) xdd);
			}
			else if (xdd instanceof XmlDataStruct)
			{
				XmlDataStruct tmpxds = (XmlDataStruct) xdd;
				tmpxds.setName(n);
				xds.setSubStruct(tmpxds);
			}
		}
		return xds;
	}

	private static IXmlDataDef transPropertyAndValClass(XORMProperty xdf,
			Class dc)
	{
		StoreType st = StoreType.Normal;
		if (xdf.has_col())
		{
			if (xdf.has_idx() || xdf.is_pk())
				st = StoreType.SeparateIdx;
			else
				st = StoreType.Separate;
		}

		// System.out.println(c.getCanonicalName()+"--fd--"+dc.getCanonicalName());
		IXmlDataDef xdd = transXDD(xdf.max_len(), dc, st);
		if(xdd instanceof XmlValDef)
		{
			if(xdf.is_pk())
			{
				((XmlValDef)xdd).setIsPk(true) ;
				if (dc == String.class)
					((XmlValDef)xdd).setMaxLen(30);
			}
			else if(xdf.has_idx())
			{
				((XmlValDef)xdd).setHasIdx(true) ;
			}
		}
		return xdd;
	}

	private static IXmlDataDef transXDD(int maxlen, Class c, StoreType st)
	{
		boolean barray = false;
		if (c.isArray())
		{
			barray = true;
			c = c.getComponentType();
		}

		if (c == byte.class)
		{
			if (barray)
			{
				return new XmlValDef(XmlVal.VAL_TYPE_BYTEARRAY, false, true,
						maxlen, st);
			}
			else
			{
				return new XmlValDef(XmlVal.VAL_TYPE_BYTE, false, true, maxlen,
						st);
			}
		}
		else if (c == int.class || c == Integer.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_INT32, barray, true, maxlen,
					st);
		}
		else if (c == short.class || c == Short.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_INT16, barray, true, maxlen,
					st);
		}
		else if (c == long.class || c == Long.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_INT64, barray, true, maxlen,
					st);
		}
		else if (c == Date.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_DATE, barray, true, maxlen, st);
		}
		else if (c == float.class || c == Float.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_FLOAT, barray, true, maxlen,
					st);
		}
		else if (c == double.class || c == Double.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_DOUBLE, barray, true, maxlen,
					st);
		}
		else if (c == String.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_STR, barray, true, maxlen, st);
		}
		else if (c == boolean.class || c == Boolean.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_BOOL, barray, true, maxlen, st);
		}
		else if (c == XmlData.class)
		{
			return new XmlDataStruct();
		}
		else if (IXmlDataable.class.isAssignableFrom(c))
		{
			XmlDataStruct xds = new XmlDataStruct();
			xds.setIsArray(barray);
			return xds;
		}
		else
		{
			XmlDataStruct xds = extractXDSFromClass(c);
			xds.setIsArray(barray);
			return xds;
		}
	}

	public static Hashtable extractXORMObjAsSqlInputParam(Object xormobj)
	throws Exception
	{
		return extractXORMObjAsSqlInputParam(xormobj,"") ;
	}
	/**
	 * 从XORM对象中提取出用来insert或update的输入参数 如果碰到Date对象,会自动对此进行Timestamp转换
	 * 
	 * 它查找对象所有的XORMProperty标记,如果发现has_col=true,则把值直接提取出
	 * 如果has_col=false,则把值放入xorm_ext中的xmldata结构中 最终xorm_ext转换成byte[]参数
	 * 
	 * @param xormobj
	 * @return
	 */
	public static Hashtable extractXORMObjAsSqlInputParam(Object xormobj,String param_name_prefix)
			throws Exception
	{
		Hashtable ret = new Hashtable();

		if(param_name_prefix==null)
			param_name_prefix = "" ;
		
		Class c = xormobj.getClass();

		XmlData xd = new XmlData();

		boolean has_ext = false;
		for (Field f : listXORMPropFields(c))
		{
			// long tst = System.currentTimeMillis();
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			// long tet = System.currentTimeMillis();
			// System.out.println("getAnnotation cost="+(tet-tst));
			if (xdf == null)
				continue;
			
			if(xdf.is_transient())
				continue ;

			String n = xdf.name();
			f.setAccessible(true);

			Object pv = f.get(xormobj);
			if(tt(ret, xd, xdf,pv,param_name_prefix))
				has_ext = true;
		}

		for (Method m : listXORMPropMethods(c))
		{

			// long tst = System.currentTimeMillis();
			XORMProperty xdf = m.getAnnotation(XORMProperty.class);
			// long tet = System.currentTimeMillis();
			// System.out.println("getAnnotation cost="+(tet-tst));
			if (xdf == null)
				continue;
			
			if(xdf.is_transient())
				continue;

			String n = xdf.name();
			String mn = m.getName();
			String xxxn = mn.substring(3);
			Method tmpm = m;
			if (!mn.startsWith("get"))
			{
				//tmpm = c.getDeclaredMethod("get" + xxxn, (Class[]) null);
				tmpm = getXORMPropMethod(c,"get" + xxxn, (Class[]) null);
			}
			if (tmpm == null)
				continue;

			tmpm.setAccessible(true);

			Object pv = tmpm.invoke(xormobj, (Object[]) null);
			if(tt(ret, xd, xdf,pv,param_name_prefix))
				has_ext = true;
//			if (xdf.has_col())
//			{
//				if (pv != null)
//				{
//					if ((pv instanceof java.util.Date)
//							&& !(pv instanceof java.sql.Timestamp))
//						pv = new java.sql.Timestamp(((java.util.Date) pv)
//								.getTime());
//					else if (pv instanceof XmlData)
//					{
//						pv = ((XmlData) pv).toBytesWithUTF8();
//					}
//					else if (pv instanceof IXmlDataable)
//					{
//						pv = ((IXmlDataable) pv).toXmlData().toBytesWithUTF8();
//					}
//					ret.put(n, pv);
//				}
//			}
//			else
//			{
//				has_ext = true;
//				IXmlStringable xs = transXV(pv);
//
//				if (xs instanceof XmlVal)
//				{
//					xd.setParamXmlVal(n, (XmlVal) xs);
//				}
//				else if (xs instanceof XmlData)
//				{
//					xd.setSubDataSingle(n, (XmlData) xs);
//				}
//			}
		}
		
		
		if (has_ext)
		{
			ret.put(param_name_prefix+COL_XORM_EXT, xd.toBytesWithUTF8());
		}

		return ret;
	}

	private static boolean tt(Hashtable ret, XmlData xd, XORMProperty xdf, Object pv,String param_name_prefix)
	{
		String n = xdf.name();
		if (xdf.has_col()||xdf.store_as_file())
		{
			if (pv != null)
			{
				if ((pv instanceof java.util.Date)
						&& !(pv instanceof java.sql.Timestamp))
				{
					pv = new java.sql.Timestamp(((java.util.Date) pv)
							.getTime());
				}
				else if (pv instanceof XmlData)
				{
					pv = ((XmlData) pv).toBytesWithUTF8();
				}
				else if (pv instanceof IXmlDataable)
				{
					pv = ((IXmlDataable) pv).toXmlData().toBytesWithUTF8();
				}
				else if(pv instanceof String)
				{//查看是否要自动截断
					if(xdf.has_col()&&xdf.max_len()>0&&xdf.auto_truncate())
					{
						String pvs = (String)pv ;
						int blen = Convert.getBytesLen(pvs,"utf-8") ;
						if(blen>xdf.max_len()*2)
						{
							pvs = truncatStrToByteLenUTF8(pvs,(xdf.max_len()-3)*2)+"..." ;//pvs.substring(0,xdf.max_len()-3) + "...";
							pv = pvs ;
						}
					}
				}
				ret.put(param_name_prefix+n, pv);
			}
			return false;
		}
		else
		{
			
				IXmlStringable xs = transXV(pv);
	
				if (xs instanceof XmlVal)
					xd.setParamXmlVal(n, (XmlVal) xs);
				else if (xs instanceof XmlData)
					xd.setSubDataSingle(n, (XmlData) xs);
				return true ;
		}
	}
	
	private static String truncatStrToByteLenUTF8(String s,int max_bytelen)
	{
		if(s==null)
			return s ;
		if(s=="")
			return "" ;
		
		int len = s.length() ;
		StringBuilder sb = new StringBuilder();
		
		int cl = 0 ;
		for(int i = 0 ; i < len ; i ++)
		{
			char c = s.charAt(i) ;
			if(c<=255)
			{
				cl++ ;
				if(cl>max_bytelen)
					break ;
				
			}
			else
			{
				cl += 3 ;
				if(cl>max_bytelen)
					break ;
			}
			sb.append(c) ;
		}
		
		return sb.toString() ;
	}

	public static List<XmlData> extractXmlDataFromObjList(List os)
	throws Exception
	{
		if(os==null)
			return null ;
		
		if(os.size()<=0)
			return new ArrayList<XmlData>(0);
		
		ArrayList<XmlData> rets = new ArrayList<XmlData>(os.size());
		for(Object o:os)
		{
			XmlData xd = extractXmlDataFromObj(o) ;
			if(xd==null)
				continue ;
			
			rets.add(xd) ;
		}
		return rets ;
	}
	/**
	 * 从对象中提取对应的XmlData
	 * 
	 * @param o
	 * @return
	 */
	public static XmlData extractXmlDataFromObj(Object o) throws Exception
	{
		return extractXmlDataFromObj(o,false);
	}
	
	/**
	 * 
	 * @param o
	 * @param include_transient 在很多非存储场合，要求提取的xmldata里面包含临时内容
	 * @return
	 * @throws Exception
	 */
	public static XmlData extractXmlDataFromObj(Object o,boolean include_transient) throws Exception
	{
		if (o == null)
			return null;

		Class c = o.getClass();

		XmlData xd = new XmlData();

		for (Field f : listXORMPropFields(c))
		{

			// long tst = System.currentTimeMillis();
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			// long tet = System.currentTimeMillis();
			// System.out.println("getAnnotation cost="+(tet-tst));
			if (xdf == null)
				continue;
			
			if(!include_transient && xdf.is_transient())
				continue;

			String n = xdf.name();
			f.setAccessible(true);

			IXmlStringable xs = transXV(f.get(o));

			if (xs instanceof XmlVal)
			{
				xd.setParamXmlVal(n, (XmlVal) xs);
			}
			else if (xs instanceof XmlData)
			{
				xd.setSubDataSingle(n, (XmlData) xs);
			}
		}

		for (Method m : listXORMPropMethods(c))
		{
			// long tst = System.currentTimeMillis();
			XORMProperty xdf = m.getAnnotation(XORMProperty.class);
			// long tet = System.currentTimeMillis();
			// System.out.println("getAnnotation cost="+(tet-tst));
			if (xdf == null)
				continue;
			
			if(xdf.is_transient())
				continue;

			String n = xdf.name();
			String mn = m.getName();
			String xxxn = mn.substring(3);
			Method tmpm = m;
			if (!mn.startsWith("get"))
			{
				//tmpm = c.getDeclaredMethod("get" + xxxn, (Class[]) null);
				tmpm = getXORMPropMethod(c,"get" + xxxn, (Class[]) null);
			}
			if (tmpm == null)
				continue;

			tmpm.setAccessible(true);

			IXmlStringable xs = transXV(tmpm.invoke(o, (Object[]) null));

			if (xs instanceof XmlVal)
			{
				xd.setParamXmlVal(n, (XmlVal) xs);
			}
			else if (xs instanceof XmlData)
			{
				xd.setSubDataSingle(n, (XmlData) xs);
			}
		}

		return xd;
	}

	private static IXmlStringable transXV(Object o)
	{
		if (o == null)
			return XmlVal.VAL_NULL;

		Class c = o.getClass();
		boolean barray = false;
		if (c.isArray())
		{
			barray = true;
			c = c.getComponentType();
		}

		if (c == byte.class)
		{
			return XmlVal.createSingleVal(o);
		}
		else if (c == int.class || c == Integer.class)
		{
			if (barray)
			{
				if (c == int.class)
				{
					int[] tmpis = (int[]) o;
					List<Object> objs = new ArrayList<Object>();
					for (int k = 0; k < tmpis.length; k++)
						objs.add(tmpis[k]);
					return XmlVal.createArrayVal(objs);
				}
				else
				{
					return XmlVal.createArrayVal((Object[]) o);
				}
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (c == short.class || c == Short.class)
		{
			if (barray)
			{
				if (c == short.class)
				{
					short[] tmpis = (short[]) o;
					List<Object> objs = new ArrayList<Object>();
					for (int k = 0; k < tmpis.length; k++)
						objs.add(tmpis[k]);
					return XmlVal.createArrayVal(objs);
				}
				else
				{
					return XmlVal.createArrayVal((Object[]) o);
				}
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (c == long.class || c == Long.class)
		{
			if (barray)
			{
				if (c == long.class)
				{
					long[] tmpis = (long[]) o;
					List<Object> objs = new ArrayList<Object>();
					for (int k = 0; k < tmpis.length; k++)
						objs.add(tmpis[k]);
					return XmlVal.createArrayVal(objs);
				}
				else
				{
					return XmlVal.createArrayVal((Object[]) o);
				}
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (Date.class.isAssignableFrom(c))// == Date.class ||
		// c==java.sql.Timestamp.class)
		{
			if (barray)
			{
				return XmlVal.createArrayVal((Object[]) o);
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (c == float.class || c == Float.class)
		{
			if (barray)
			{
				if (c == float.class)
				{
					float[] tmpis = (float[]) o;
					List<Object> objs = new ArrayList<Object>();
					for (int k = 0; k < tmpis.length; k++)
						objs.add(tmpis[k]);
					return XmlVal.createArrayVal(objs);
				}
				else
				{
					return XmlVal.createArrayVal((Object[]) o);
				}
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (c == double.class || c == Double.class)
		{
			if (barray)
			{
				if (c == double.class)
				{
					double[] tmpis = (double[]) o;
					List<Object> objs = new ArrayList<Object>();
					for (int k = 0; k < tmpis.length; k++)
						objs.add(tmpis[k]);
					return XmlVal.createArrayVal(objs);
				}
				else
				{
					return XmlVal.createArrayVal((Object[]) o);
				}
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (c == String.class)
		{
			if (barray)
			{
				return XmlVal.createArrayVal((Object[]) o);
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (c == boolean.class || c == Boolean.class)
		{
			if (barray)
			{
				if (c == boolean.class)
				{
					boolean[] tmpis = (boolean[]) o;
					List<Object> objs = new ArrayList<Object>();
					for (int k = 0; k < tmpis.length; k++)
						objs.add(tmpis[k]);
					return XmlVal.createArrayVal(objs);
				}
				else
				{
					return XmlVal.createArrayVal((Object[]) o);
				}
			}
			else
			{
				return XmlVal.createSingleVal(o);
			}
		}
		else if (c == XmlData.class)
		{
			if (barray)
			{
				throw new RuntimeException("not support XmlData[]");
			}
			else
			{
				return (XmlData) o;
			}
		}
		else if (IXmlDataable.class.isAssignableFrom(c))
		{
			if (barray)
			{
				throw new RuntimeException("not support XmlData[]");
			}

			IXmlDataable tmpxd = (IXmlDataable) o;
			return tmpxd.toXmlData();
		}
		else
		{
			throw new RuntimeException("not support XmlDataField class field:"
					+ c.getCanonicalName());
		}
	}

	/**
	 * 把XmlData信息注入到对象中
	 * 
	 * @param o
	 * @param xd
	 */
	public static void injectXmlDataToObj(Object o, XmlData xd)
			throws Exception
	{
		injectXmlDataRowToObj(o, null, xd);
	}
	
	/**
	 * 把XmlData数据注入到对应的对象中
	 * @param o
	 * @param xd
	 * @param update_cols 限定的列内容
	 * @throws Exception
	 */
	public static void injectXmlDataToObj(Object o, XmlData xd,String[] update_cols)
		throws Exception
	{
		injectXmlDataRowToObj(o, null, xd,update_cols);
	}

	/**
	 * 根据
	 * 
	 * @param o
	 * @param dr
	 * @param xd
	 * @throws Exception
	 */
	private static void injectXmlDataRowToObj(Object o, DataRow dr, XmlData xd)
	throws Exception
	{
		injectXmlDataRowToObj(o, dr, xd,null);
	}
	
	private static void injectXmlDataRowToObj(Object o, DataRow dr, XmlData xd,String[] limit_cols)
			throws Exception
	{
		if (o == null)
			return;

		HashSet<String> limit_col_set = null;
		if(limit_cols!=null&&limit_cols.length>0)
		{
			limit_col_set = new HashSet<String>() ;
			for(String lcols:limit_cols)
			{
				limit_col_set.add(lcols.toUpperCase());
			}
		}
		// System.out.println("injectXmlDataRowToObj------\n"+xd.toXmlString());

		Class c = o.getClass();
		for (Field f : listXORMPropFields(c))
		{
			XORMProperty xdf = f.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;

			//is_transient=true的内容也应该加入
			
			String n = xdf.name();
			if(limit_col_set!=null&&!limit_col_set.contains(n.toUpperCase()))
				continue ;//非限定的列

			Object ov = null;
			if (dr != null && (xdf.has_col()||xdf.store_as_file()||xdf.is_transient()) && dr.hasColumn(n))
			{
				ov = dr.getValue(n);
				if (ov != null)
				{
					setFieldVal(o, f, ov);
				}
				continue;
			}
			if (xd != null)
			{
				Class tmpc = f.getType();
				f.setAccessible(true);

				ov = extractObjFromXmlData(tmpc, n, xd);
				setFieldVal(o, f, ov);
				// XmlVal xv = xd.getParamXmlVal(n);
				// if (xv == XmlVal.VAL_NULL)
				// f.set(o, null);
				// else
				// {
				// ov = extractObjFromXmlData(tmpc, n, xd);
				// if (ov != null)
				// {
				// setFieldVal(o, f, ov);
				// }
				// }
				continue;
			}
			// injectXVObj(o,f,tmpc,n,xd);
		}
		
		for (Method m : listXORMPropMethods(c))
		{
			XORMProperty xdf = m.getAnnotation(XORMProperty.class);
			if (xdf == null)
				continue;

			String n = xdf.name();

			Method tmpm = m;
			String mn = m.getName();

			if (!mn.startsWith("set"))
			{
//				tmpm = c.getDeclaredMethod("set" + mn.substring(3),
//						new Class[] { m.getReturnType() });
				
				tmpm = getXORMPropMethod(c,"set" + mn.substring(3),new Class[] { m.getReturnType() });
			}
			else
			{

			}
			if (tmpm == null)
				continue;

			if (dr != null && (xdf.has_col()||xdf.store_as_file()||xdf.is_transient()) && dr.hasColumn(n))
			{
				Object ov = dr.getValue(n);
				if (ov != null)
				{
					setMethodVal(o, tmpm, ov);
				}
				continue;
			}
			if (xd != null)
			{
				Class tmpc = tmpm.getParameterTypes()[0];
				tmpm.setAccessible(true);

				Object ov = extractObjFromXmlData(tmpc, n, xd);
				setMethodVal(o, tmpm, ov);
				// XmlVal xv = xd.getParamXmlVal(n);
				// if (xv == XmlVal.VAL_NULL)
				// tmpm.invoke(o, new Object[] { null });
				// else
				// {
				// Object ov = extractObjFromXmlData(tmpc, n, xd);
				// if (ov != null)
				// {
				// setMethodVal(o, tmpm, ov);
				// }
				// //tmpm.invoke(o, new Object[] { ov });
				// }
				continue;
			}
		}
	}

	private static void setMethodVal(Object o, Method tmpm, Object ov)
			throws Exception, InvocationTargetException
	{
		Class mc = tmpm.getParameterTypes()[0];
		if (ov == null && mc.isPrimitive())
		{
			return;
		}

		tmpm.setAccessible(true);

		if (mc == Boolean.class || mc == boolean.class)
		{// 对bool情况做特殊处理,使之能够适应整数
			if (ov instanceof Number)
			{
				tmpm.invoke(o, new Object[] { ((Number) ov).intValue() > 0 });
			}
			else
			{
				tmpm.invoke(o, new Object[] { ov });
			}

			return;
		}

		if (mc == XmlData.class)
		{
			if (ov instanceof byte[])
				ov = XmlData.parseFromByteArrayUTF8((byte[]) ov);
		}
		else if (IXmlDataable.class.isAssignableFrom(mc))
		{
			IXmlDataable tmpv = (IXmlDataable) mc.newInstance();
			if (ov instanceof byte[])
				ov = XmlData.parseFromByteArrayUTF8((byte[]) ov);
			tmpv.fromXmlData((XmlData) ov);
			ov = tmpv;
		}

		tmpm.invoke(o, new Object[] { ov });
	}

	private static void setFieldVal(Object o, Field f, Object ov)
			throws Exception
	{
		Class fc = f.getType();
		if (ov == null && fc.isPrimitive())
		{
			return;
		}

		f.setAccessible(true);

		// System.out.println("field class==>>>"+fc.getCanonicalName());
		if (fc == Boolean.class || fc == boolean.class)
		{// 对bool情况做特殊处理,使之能够适应整数
			if (ov instanceof Number)
			{
				f.setBoolean(o, ((Number) ov).intValue() > 0);
			}
			else if(ov instanceof String)
			{//for derby err
				f.setBoolean(o, "true".equals(ov) || "1".equals(ov)) ;
			}
			else
			{
				f.set(o, ov);
			}
			return;
		}
		
		if(ov instanceof Number)
		{
			if(fc==ov.getClass())
			{
				f.set(o,ov) ;
				return;
			}
			
			Number ovn = (Number)ov ;
			if(fc==short.class||fc==Short.class)
				f.set(o, ovn.shortValue()) ;
			else if(fc==int.class||fc==Integer.class)
				f.set(o, ovn.intValue()) ;
			else if(fc==long.class||fc==Long.class)
				f.set(o, ovn.longValue()) ;
			else if(fc==float.class||fc==Float.class)
				f.set(o, ovn.floatValue()) ;
			else if(fc==double.class||fc==Double.class)
				f.set(o, ovn.doubleValue()) ;
			else if(fc==byte.class||fc==Byte.class)
				f.set(o, ovn.byteValue()) ;
			else
				f.set(o, ovn) ;
			return ;
		}

		if (fc == XmlData.class)
		{
			if (ov instanceof byte[])
				ov = XmlData.parseFromByteArrayUTF8((byte[]) ov);
		}
		else if (IXmlDataable.class.isAssignableFrom(fc))
		{
			IXmlDataable tmpv = (IXmlDataable) fc.newInstance();
			if (ov instanceof byte[])
				ov = XmlData.parseFromByteArrayUTF8((byte[]) ov);
			tmpv.fromXmlData((XmlData) ov);
			ov = tmpv;
		}

		f.set(o, ov);
	}

	private static Object extractObjFromXmlData(Class c, String name, XmlData xd)
			throws Exception
	{
		boolean barray = false;
		if (c.isArray())
		{
			barray = true;
			c = c.getComponentType();
		}

		if (c == byte.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;
			return xv.getObjectVal();
		}
		else if (c == int.class || c == Integer.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					int s = objs.size();
					// Object os = Array.newInstance(c, s);
					if (c == int.class)
					{
						int[] tmpis = new int[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Integer) objs.get(i);
						}
						setv = tmpis;
					}
					else
					{
						Integer[] tmpis = new Integer[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Integer) objs.get(i);
						}
						setv = tmpis;
					}
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == short.class || c == Short.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					int s = objs.size();
					// Object os = Array.newInstance(c, s);
					if (c == short.class)
					{
						short[] tmpis = new short[s];
						for (short i = 0; i < s; i++)
						{
							tmpis[i] = (Short) objs.get(i);
						}
						setv = tmpis;
					}
					else
					{
						Short[] tmpis = new Short[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Short) objs.get(i);
						}
						setv = tmpis;
					}
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == long.class || c == Long.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					int s = objs.size();
					// Object os = Array.newInstance(c, s);
					if (c == long.class)
					{
						long[] tmpis = new long[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Long) objs.get(i);
						}
						setv = tmpis;
					}
					else
					{
						Long[] tmpis = new Long[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Long) objs.get(i);
						}
						setv = tmpis;
					}
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == Date.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					Date[] tmpds = new Date[objs.size()];
					objs.toArray(tmpds);
					// setv = objs.toArray();
					setv = tmpds;
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == float.class || c == Float.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					int s = objs.size();
					// Object os = Array.newInstance(c, s);
					if (c == float.class)
					{
						float[] tmpis = new float[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Float) objs.get(i);
						}
						setv = tmpis;
					}
					else
					{
						Float[] tmpis = new Float[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Float) objs.get(i);
						}
						setv = tmpis;
					}
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == double.class || c == Double.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					int s = objs.size();
					// Object os = Array.newInstance(c, s);
					if (c == double.class)
					{
						double[] tmpis = new double[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Double) objs.get(i);
						}
						setv = tmpis;
					}
					else
					{
						Double[] tmpis = new Double[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Double) objs.get(i);
						}
						setv = tmpis;
					}
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == String.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					String[] tmpsss = new String[objs.size()];
					objs.toArray(tmpsss);
					setv = tmpsss;
					// setv = objs.toArray();
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == boolean.class || c == Boolean.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if (xv == null)
				return null;

			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null;
				if (objs != null)
				{
					int s = objs.size();
					// Object os = Array.newInstance(c, s);
					if (c == boolean.class)
					{
						boolean[] tmpis = new boolean[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Boolean) objs.get(i);
						}
						setv = tmpis;
					}
					else
					{
						Boolean[] tmpis = new Boolean[s];
						for (int i = 0; i < s; i++)
						{
							tmpis[i] = (Boolean) objs.get(i);
						}
						setv = tmpis;
					}
				}
				return setv;
			}
			else
			{
				return xv.getObjectVal();
			}
		}
		else if (c == XmlData.class)
		{
			if (barray)
			{
				throw new RuntimeException("not support XmlData[]");
			}
			else
			{
				XmlData xd00 = xd.getSubDataSingle(name);
				if(xd00==null)
					xd00 = new XmlData() ;
				return xd00 ;
			}
		}
		else if (IXmlDataable.class.isAssignableFrom(c))
		{
			if (barray)
			{
				throw new RuntimeException("not support XmlData[]");
			}

			Object setv = null;
			XmlData tmpxd0 = xd.getSubDataSingle(name);
			if (tmpxd0 != null)
			{
				IXmlDataable ret0 = (IXmlDataable) c.newInstance();
				ret0.fromXmlData(tmpxd0);
			}
			return setv;
		}
		else
		{
			throw new RuntimeException("not support XmlDataField class field:"
					+ c.getCanonicalName());
		}
	}

	/**
	 * 从数据库中查找出的结果(可能包含xorm_ext列),中的内容装填XORM对象
	 * 
	 * @param <T>
	 * @param t
	 * @param dr
	 * @param o
	 * @throws Exception
	 */
	public static <T> void fillXORMObjByDataRow(DataRow dr, T o)
			throws Exception
	{
		DataTable dt = dr.getBelongToTable();
		byte[] ext_cont = (byte[]) dr.getValue(COL_XORM_EXT);
		XmlData xd = null;
		if (ext_cont != null && ext_cont.length > 0)
			xd = XmlData.parseFromByteArrayUTF8(ext_cont);

		injectXmlDataRowToObj(o, dr, xd);
	}

	public static DataRow extractDataRowFromObj(Object o) throws Exception
	{
		DataRow dr = new DataRow() ;
		Hashtable ht = extractXORMObjAsSqlInputParam(o);
		for(Enumeration en =ht.keys();en.hasMoreElements();)
		{
			String k = (String)en.nextElement() ;
			dr.put(k, ht.get(k));
		}
		return dr ;
	}
	/**
	 * 根据xorm类,获得对应的JavaTableInfo对象
	 * 
	 * @param xorm_class
	 * @return
	 */
//	public static JavaTableInfo extractJavaTableInfo(Class xorm_class,
//			StringBuilder failedreson)
//	{
//		return extractJavaTableInfo(xorm_class,false,
//				failedreson) ;
//	}
	
	public static JavaTableInfo extractJavaTableInfo(Class xorm_class,
			StringBuilder failedreson)
	{
		XORMClass xormc = (XORMClass) xorm_class.getAnnotation(XORMClass.class);
		if (xormc == null)
		{
			failedreson.append("no XORMClass annotion found in Class");
			return null;
		}

		String tablen = xormc.table_name();
		if (tablen == null || tablen.equals(""))
		{
			failedreson.append("no table name in Class annotion XORMClass");
			return null;
		}

		ArrayList<JavaColumnInfo> norcols = new ArrayList<JavaColumnInfo>();
		JavaColumnInfo pkcol = null;
		ArrayList<JavaForeignKeyInfo> fks = new ArrayList<JavaForeignKeyInfo>();

		HashMap<XORMPropWrapper, Class> xormp2c = extractXORMProperties(xorm_class);
		if (xormp2c == null || xormp2c.size() <= 0)
		{
			failedreson.append("no XORMProperty found in Class");
			return null;
		}

		boolean has_ext_blob = false;

		XORMPropWrapper[] xormpws = new XORMPropWrapper[xormp2c.size()];
		xormp2c.keySet().toArray(xormpws);
		Arrays.sort(xormpws);

		for (XORMPropWrapper pw : xormpws)
		{
			XORMProperty p = pw.getXORMProperty();
			
			//transient=true不做数据输出
			if(p.is_transient())
				continue;
			
			Class c = xormp2c.get(pw);

			if (!p.has_col()&&!p.store_as_file())
			{
				has_ext_blob = true;
				continue;
			}
			
			if(p.store_as_file())
			{
				continue ;
			}
			
			boolean b_read_on_demand = p.read_on_demand() ;
			boolean b_update_as_single = p.update_as_single() ;

			String coln = p.name();
			if (coln.toLowerCase().startsWith("xorm"))
			{
				failedreson
						.append("XORMProperty column["+coln+"] name cannot start with [xorm],it is reserved for inner use!");
				return null;
			}

			XmlVal.XmlValType xvt = null;
			
			int max_len = p.max_len()*2 ;

			if (XmlData.class == c || IXmlDataable.class.isAssignableFrom(c))
			{
				xvt = XmlVal.XmlValType.vt_byte_array;
			}
			else
			{
				xvt = XmlVal.class2VT(c);

				if (xvt == XmlVal.XmlValType.vt_string)// ||xvt==XmlVal.XmlValType.vt_byte_array)
				{
					if(p.is_pk()&&p.is_auto())
					{
						max_len = 30 ;
					}
					
					if (max_len <= 0)
					{
						failedreson.append(p.name() + " in "
								+ c.getCanonicalName()
								+ " String or ByteArray must has max_len > 0!");
						return null;
					}
				}
			}

			JavaColumnInfo jci = new JavaColumnInfo(coln,p.is_pk(), xvt, max_len,
					p.has_idx(), p.is_unique_idx(), p.is_auto(),p.auto_value_start(), p
							.default_str_val(),b_read_on_demand,b_update_as_single);

			if (p.is_pk())
				pkcol = jci;
			else
				norcols.add(jci);

			if (p.has_fk())
			{
				if(p.fk_base_class())
				{//基于主类的外键关联,通过定义的主类查找对应的主类pk作为外键关联
					Class bc = xormc.base_class();
					XORMClass bc_xormc = (XORMClass)bc.getAnnotation(XORMClass.class);
					if(bc_xormc==null)
						throw new RuntimeException("Class ["+bc.getCanonicalName()+"] has no XORMClass annotation!");
					
					XORMPropWrapper bc_pkpw = extractPkXORMPropWrapper(bc) ;
					if(bc_pkpw==null)
						throw new RuntimeException("Class ["+bc.getCanonicalName()+"] has no pk XORMProperty annotation!");
					
					fks.add(new JavaForeignKeyInfo(coln, bc_xormc.table_name(), bc_pkpw.getXORMProperty().name()));
				}
				else
				{
					fks.add(new JavaForeignKeyInfo(coln, p.fk_table(), p
							.fk_column()));
				}
			}
		}

		if (has_ext_blob)
			norcols.add(new JavaColumnInfo(COL_XORM_EXT,false,
					XmlVal.XmlValType.vt_byte_array, -1, false, false, false,-1,
					null,true,false));
		return new JavaTableInfo(tablen, pkcol, norcols, fks);
	}

	/**
	 * 根据xorm类,提取出创建对应数据库表的sql语句
	 * 
	 * @param xorm_class
	 * @return
	 */
	public static List<String> extractCreationDBSqls(String tablename,
			DBType dbt,
			Class xorm_class, StringBuilder failedreson)
	{
		JavaTableInfo jti = extractJavaTableInfo(xorm_class, failedreson);
		if (jti == null)
			return null;

		if(tablename!=null&&!"".equals(tablename))
			jti.setTableName(tablename) ;
		
		XORMClass xormc = (XORMClass) xorm_class.getAnnotation(XORMClass.class);
		if (xormc == null)
			return null;
		
		return DbSql.getDbSqlByDBType(dbt).getCreationSqls(jti);
		
//		int dm = xormc.distributed_mode() ;
//		if(dm==0)
//		{//非分布式情况
//			return DbSql.getDbSqlByDBType(dbt).getCreationSqls(jti);
//		}
//		else if(dm==1)
//		{//mode1
//			if(is_distributed_proxy)//模式1代理正常
//				return DbSql.getDbSqlByDBType(dbt).getCreationSqls(jti);
//			else//server端
//				return DbSql.getDbSqlByDBType(dbt).getCreationDistributedMode1Sqls(jti);
//		}
//		else if(dm==2)
//		{
//			return DbSql.getDbSqlByDBType(dbt).getCreationDistributedMode2Sqls(jti);
//		}
		
//		return null ;
	}
	
	public static List<String> extractCreationDBSqls(
			DBType dbt,
			Class xorm_class, StringBuilder failedreson)
	{
		return extractCreationDBSqls(null,
				dbt,xorm_class, failedreson) ;
	}

	public static String getDropXORMClassTable(Class xorm_c)
	{
		XORMClass xormc = (XORMClass) xorm_c.getAnnotation(XORMClass.class);
		if (xormc == null)
		{
			return null;
		}

		String tablen = xormc.table_name();
		if (tablen == null || tablen.equals(""))
		{
			return null;
		}

		return "drop table " + tablen;
	}

	public static StringBuilder getSelectByPkSql(DBType dbt, Class xorm_class,
			StringBuilder failedreson)
	{
		JavaTableInfo jti = extractJavaTableInfo(xorm_class, failedreson);
		if (jti == null)
			return null;
		return DbSql.getDbSqlByDBType(dbt).getSelectByPkIdSql(jti);
	}

	public static StringBuffer[] getInsertWithNewIdReturnSqls(DBType dbt,
			Class xorm_class, StringBuilder failedreson)
	{
		JavaTableInfo jti = extractJavaTableInfo(xorm_class, failedreson);
		if (jti == null)
			return null;
		return DbSql.getDbSqlByDBType(dbt).getInsertSqlWithNewIdReturn(jti);
	}
	
	
	/**
	 * 根据类名称获得所有的SupportAuto的列，不包含主键
	 * @param xorm_c
	 * @return
	 */
	public static String[] getSupportAutoXORMColumns(Class xorm_c)
	{
		HashMap<XORMPropWrapper,Class> xormpws = XORMUtil.extractXORMProperties(xorm_c) ;
		if(xormpws==null)
			return null ;
		ArrayList<String> rets = new ArrayList<String>() ;
		
		for(XORMPropWrapper pw:xormpws.keySet())
		{
			XORMProperty p = pw.getXORMProperty() ;
			if(p.support_auto())
				rets.add(p.name()) ;
		}
		
		String[] rr = new String[rets.size()];
		rets.toArray(rr);
		return rr ;
	}
	

	public static void main(String[] args) throws Exception
	{
		// System.out.println(URLEncoder.encode("2007-02-06T11:05:52.250+0800"));
		TC t = new TC(100, new String[] { "哈哈", "pp" });
		t.ttt();
	}

	static class SC
	{
		@XORMProperty(name = "sss")
		public String sss = "ss";

		protected String sssppp = null;

		public void ttt() throws Exception
		{
			for (Field f : this.getClass().getFields())
			{
				System.out.println("ff=" + f.getName());
			}

			for (Field f : this.getClass().getDeclaredFields())
			{
				System.out.println("dff=" + f.getName());
			}

			long st = System.currentTimeMillis();
			XmlData xd = extractXmlDataFromObj(this);
			long et = System.currentTimeMillis();
			System.out
					.println("---cost=" + (et - st) + "\n" + xd.toXmlString());
			xd.setParamValue("nnn", null);
			xd.removeSubData("xd_ttt");
			System.out.println("-----------------");
			System.out.println(xd.toXmlString());

			st = System.currentTimeMillis();
			injectXmlDataToObj(this, xd);
			et = System.currentTimeMillis();

			System.out.println("---cost=" + (et - st) + "\n" + this);

			StringBuilder sb = new StringBuilder();

			System.out.println("creation sql for dbtype=" + DBType.mysql);
			List<String> sqls = XORMUtil.extractCreationDBSqls(DBType.mysql,
					TC.class, sb);
			if (sqls == null)
			{
				System.out.println("create sql failed =" + sb.toString());
			}
			else
			{
				for (String s : sqls)
				{
					System.out.println(s);
				}
			}

			System.out.println("creation sql for dbtype=" + DBType.sqlserver);
			sqls = XORMUtil.extractCreationDBSqls(DBType.sqlserver, TC.class,
					sb);
			if (sqls == null)
			{
				System.out.println("create sql failed =" + sb.toString());
			}
			else
			{
				for (String s : sqls)
				{
					System.out.println(s);
				}
			}

			System.out.println("creation sql for dbtype=" + DBType.derby);
			sqls = XORMUtil.extractCreationDBSqls(DBType.derby, TC.class, sb);
			if (sqls == null)
			{
				System.out.println("create sql failed =" + sb.toString());
			}
			else
			{
				for (String s : sqls)
				{
					System.out.println(s);
				}
			}
		}
	}

	@XORMClass(table_name = "sc")
	static class TC extends SC
	{
		@XORMProperty(name = "id", has_col = true, is_pk = true, is_auto = true)
		private long id = -1;

		@XORMProperty(name = "refid", has_col = true, has_idx = true, is_unique_idx = true, has_fk = true, fk_table = "xxx", fk_column = "id")
		private long refId = -1;

		@XORMProperty(name = "nnns")
		public String[] name = new String[] { "aaa", "123" };

		@XORMProperty(name = "iii")
		public int[] iiii = new int[] { 8, 8, 8 };

		@XORMProperty(name = "IIII")
		public Integer[] III = new Integer[] { 8, 8, 8 };

		@XORMProperty(name = "ddd", has_col = true)
		protected Date dd = null;

		@XORMProperty(name = "kks", has_col = true, max_len = 100, default_str_val = "")
		protected String kks = null;

		@XORMProperty(name = "ddds")
		protected Date[] dds = new Date[] { new Date() };

		@XORMProperty(name = "xd_ttt")
		XmlData tmpxd = new XmlData();

		public String sss = null;

		public TC(long id, String[] ns)
		{
			this.id = id;
			name = ns;
			tmpxd.setParamValue("abc", 55);
			tmpxd.setParamValue("ggg", "xxxxxx");
		}

		public String toString()
		{
			return "id=" + id + " name=" + name;
		}
	}
}
