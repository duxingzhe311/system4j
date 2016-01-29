package com.dw.system.xmldata.obj;

import java.lang.reflect.*;
import java.net.URLEncoder;
import java.util.*;

import com.dw.system.xmldata.*;
import com.dw.system.xmldata.XmlDataStruct.StoreType;
import com.dw.system.xmldata.xrmi.XmlDataAndMethodCaller;

public class XmlDataObjHelper
{
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

		for (Field f : c.getDeclaredFields())
		{
			XmlDataField xdf = f.getAnnotation(XmlDataField.class);
			if (xdf == null)
				continue;

			String n = xdf.name();
			if (n == null || n.equals(""))
				n = f.getName();

			StoreType st = xdf.store();
			
			Class dc = f.getType();//.getDeclaringClass();
			//System.out.println(c.getCanonicalName()+"--fd--"+dc.getCanonicalName());
			IXmlDataDef xdd = transXDD(xdf.max_len(), dc,st);
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

	private static IXmlDataDef transXDD(int maxlen, Class c,StoreType st)
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
			return new XmlValDef(XmlVal.VAL_TYPE_DATE, barray, true, maxlen,
					st);
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
			return new XmlValDef(XmlVal.VAL_TYPE_STR, barray, true, maxlen,
					st);
		}
		else if (c == boolean.class || c == Boolean.class)
		{
			return new XmlValDef(XmlVal.VAL_TYPE_BOOL, barray, true, maxlen,
					st);
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

	/**
	 * 从对象中提取对应的XmlData
	 * 
	 * @param o
	 * @return
	 */
	public static XmlData extractXmlDataFromObj(Object o) throws Exception
	{
		if (o == null)
			return null;

		Class c = o.getClass();

		XmlData xd = new XmlData();

		for (Field f : c.getDeclaredFields())
		{
			
			
//			long tst = System.currentTimeMillis();
			XmlDataField xdf = f.getAnnotation(XmlDataField.class);
//			long tet = System.currentTimeMillis();
//			System.out.println("getAnnotation cost="+(tet-tst));
			if (xdf == null)
				continue;

			String n = xdf.name();
			if (n.equals(""))
				n = f.getName();

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
				if(c==int.class)
				{
					int[] tmpis = (int[])o;
					List<Object> objs = new ArrayList<Object>();
					for(int k = 0 ; k < tmpis.length ; k ++)
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
				if(c==short.class)
				{
					short[] tmpis = (short[])o;
					List<Object> objs = new ArrayList<Object>();
					for(int k = 0 ; k < tmpis.length ; k ++)
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
				if(c==long.class)
				{
					long[] tmpis = (long[])o;
					List<Object> objs = new ArrayList<Object>();
					for(int k = 0 ; k < tmpis.length ; k ++)
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
		else if (c == Date.class)
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
				if(c==float.class)
				{
					float[] tmpis = (float[])o;
					List<Object> objs = new ArrayList<Object>();
					for(int k = 0 ; k < tmpis.length ; k ++)
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
				if(c==double.class)
				{
					double[] tmpis = (double[])o;
					List<Object> objs = new ArrayList<Object>();
					for(int k = 0 ; k < tmpis.length ; k ++)
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
				if(c==boolean.class)
				{
					boolean[] tmpis = (boolean[])o;
					List<Object> objs = new ArrayList<Object>();
					for(int k = 0 ; k < tmpis.length ; k ++)
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
		if (o == null)
			return;
		
		if(xd==null)
			return ;

		Class c = o.getClass();
		for (Field f : c.getDeclaredFields())
		{
			XmlDataField xdf = f.getAnnotation(XmlDataField.class);
			if (xdf == null)
				continue;

			String n = xdf.name();
			if (n.equals(""))
				n = f.getName();

			Class tmpc = f.getType();
			f.setAccessible(true);
			
			XmlVal xv = xd.getParamXmlVal(n);
			if(xv==XmlVal.VAL_NULL)
				f.set(o, null);
			
			injectXVObj(o,f,tmpc,n,xd);
		}
	}

	private static void injectXVObj(Object o,Field f,Class c,String name,XmlData xd)
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
			if(xv==null)
				return ;
			f.set(o, xv.getObjectVal());
		}
		else if (c == int.class || c == Integer.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null ;
				if(objs!=null)
				{
					int s = objs.size();
					//Object os = Array.newInstance(c, s);
					if(c==int.class)
					{
						int[] tmpis = new int[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Integer)objs.get(i);
						}
						setv = tmpis ;
					}
					else
					{
						Integer[] tmpis = new Integer[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Integer)objs.get(i);
						}
						setv = tmpis ;
					}
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
			}
		}
		else if (c == short.class || c == Short.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null ;
				if(objs!=null)
				{
					int s = objs.size();
					//Object os = Array.newInstance(c, s);
					if(c==short.class)
					{
						short[] tmpis = new short[s];
						for(short i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Short)objs.get(i);
						}
						setv = tmpis ;
					}
					else
					{
						Short[] tmpis = new Short[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Short)objs.get(i);
						}
						setv = tmpis ;
					}
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
			}
		}
		else if (c == long.class || c == Long.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null ;
				if(objs!=null)
				{
					int s = objs.size();
					//Object os = Array.newInstance(c, s);
					if(c==long.class)
					{
						long[] tmpis = new long[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Long)objs.get(i);
						}
						setv = tmpis ;
					}
					else
					{
						Long[] tmpis = new Long[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Long)objs.get(i);
						}
						setv = tmpis ;
					}
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
			}
		}
		else if (c == Date.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null ;
				if(objs!=null)
				{
					Date[] tmpds = new Date[objs.size()];
					objs.toArray(tmpds);
					//setv = objs.toArray();
					setv = tmpds ;
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
			}
		}
		else if (c == float.class || c == Float.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null ;
				if(objs!=null)
				{
					int s = objs.size();
					//Object os = Array.newInstance(c, s);
					if(c==float.class)
					{
						float[] tmpis = new float[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Float)objs.get(i);
						}
						setv = tmpis ;
					}
					else
					{
						Float[] tmpis = new Float[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Float)objs.get(i);
						}
						setv = tmpis ;
					}
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
			}
		}
		else if (c == double.class || c == Double.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null ;
				if(objs!=null)
				{
					int s = objs.size();
					//Object os = Array.newInstance(c, s);
					if(c==double.class)
					{
						double[] tmpis = new double[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Double)objs.get(i);
						}
						setv = tmpis ;
					}
					else
					{
						Double[] tmpis = new Double[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Double)objs.get(i);
						}
						setv = tmpis ;
					}
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
			}
		}
		else if (c == String.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv =null ;
				if(objs!=null)
				{
					String[] tmpsss = new String[objs.size()];
					objs.toArray(tmpsss);
					setv = tmpsss;
					//setv = objs.toArray();
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
			}
		}
		else if (c == boolean.class || c == Boolean.class)
		{
			XmlVal xv = xd.getParamXmlVal(name);
			if(xv==null)
				return ;
			
			if (barray)
			{
				List<Object> objs = xv.getObjectVals();
				Object setv = null ;
				if(objs!=null)
				{
					int s = objs.size();
					//Object os = Array.newInstance(c, s);
					if(c==boolean.class)
					{
						boolean[] tmpis = new boolean[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Boolean)objs.get(i);
						}
						setv = tmpis ;
					}
					else
					{
						Boolean[] tmpis = new Boolean[s];
						for(int i = 0 ; i < s ; i ++)
						{
							tmpis[i] = (Boolean)objs.get(i);
						}
						setv = tmpis ;
					}
				}
				f.set(o, setv);
			}
			else
			{
				f.set(o, xv.getObjectVal());
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
				f.set(o, xd.getSubDataSingle(name));
			}
		}
		else if (IXmlDataable.class.isAssignableFrom(c))
		{
			if (barray)
			{
				throw new RuntimeException("not support XmlData[]");
			}
			
			Object setv = null ;
			XmlData tmpxd0 = xd.getSubDataSingle(name);
			if(tmpxd0!=null)
			{
				IXmlDataable ret0 = (IXmlDataable)c.newInstance();
				ret0.fromXmlData(tmpxd0);
			}
			f.set(o,setv);
		}
		else
		{
			throw new RuntimeException("not support XmlDataField class field:"
					+ c.getCanonicalName());
		}
	}
	
	// public static void injectXmlDataToObj(Object o,XmlData xd)
	// throws Exception
	// {
	// if(o==null)
	// return ;
	//		
	// Class c = o.getClass();
	// for(Field f:c.getDeclaredFields())
	// {
	// XmlDataField xdf = f.getAnnotation(XmlDataField.class);
	// if(xdf==null)
	// continue ;
	//			
	// String n = xdf.name();
	// if(n.equals(""))
	// n = f.getName();
	//			
	// XmlData tmpxd = xd.getSubDataSingle(n);
	// if(tmpxd==null)
	// continue ;
	//			
	// Object ov = XmlDataAndMethodCaller.unpackObjFromXmlData(tmpxd);
	// f.setAccessible(true);
	// f.set(o, ov);
	// }
	// }

	public static void main(String[] args) throws Exception
	{
		//System.out.println(URLEncoder.encode("2007-02-06T11:05:52.250+0800"));
		TC t = new TC(100,new String[]{"哈哈","pp"});
		t.ttt();
	}

	static class SC
	{
		@XmlDataField
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
			System.out.println("---cost="+(et-st)+"\n"+xd.toXmlString());
			xd.setParamValue("nnn", null);
			xd.removeSubData("xd_ttt");
			System.out.println("-----------------");
			System.out.println(xd.toXmlString());
			
			st = System.currentTimeMillis();
			injectXmlDataToObj(this,xd);
			et = System.currentTimeMillis();
			
			System.out.println("---cost="+(et-st)+"\n"+this);
		}
	}

	static class TC extends SC
	{
		@XmlDataField
		private long id = -1;

		@XmlDataField(name = "nnns")
		public String[] name = new String[]{"aaa","123"};
		
		@XmlDataField(name = "iii")
		public int[] iiii = new int[]{8,8,8};
		
		@XmlDataField(name = "IIII")
		public Integer[] III = new Integer[]{8,8,8};
		
		@XmlDataField(name = "ddd")
		protected Date dd = null ;
		
		@XmlDataField(name = "ddds")
		protected Date[] dds = new Date[]{new Date()} ;
		
		@XmlDataField(name="xd_ttt")
		XmlData tmpxd = new XmlData();

		public String sss = null;
		
		public TC(long id,String[] ns)
		{
			this.id = id ;
			name = ns ;
			tmpxd.setParamValue("abc", 55);
			tmpxd.setParamValue("ggg", "xxxxxx");
		}
		
		public String toString()
		{
			return "id="+id+" name="+name;
		}
	}
}
