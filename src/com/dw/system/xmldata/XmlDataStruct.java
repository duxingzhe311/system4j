package com.dw.system.xmldata;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.xmldata.XmlDataPath.PathItem;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * 为XmlData数据提供结构性约束，并且为外界使用提供更加具体细致的数据结构
 * 
 * 它本身也可以生成XmlData格式
 * 
 * XmlDataStruct的成员StructItem用来定义一个结构下的数据成员信息
 * 其中StructItem如果指定了StoreType。则可能会影响XmlData数据保存的实现
 * 
 * 其中，StoreType.Noraml表示不做任何操作
 * StoreType.Separate表示，该数据在存储实现时会被分离存储，这样就可以减少修改该数据成员的负担
 * 	如果用关系数据库表示的话，该标记意味则使用关系数据库表的某一列来保存该数据成员
 * StoreType.SeparateIdx表示，该数据在存储实现时会被分离存储，并且还会建立索引，这样不仅
 * 	可以减少修改该数据成员的负担，而且还可以通过该数据成员进行快速查询
 * 
 * 分离存储好处之一是，可以快速提取出已经定义成分离存储的数据（不需要访问大Xml数据块），而这些
 * 数据往往和列表信息有关——并不需要显示详细信息。
 * @author Jason Zhu
 */
@XRmi(reg_name = "xmldata_struct")
public class XmlDataStruct implements IXmlDataDef
{
	public static enum StoreType
	{
		Normal(1), //缺省情况
		Separate(2), //对应的数据项分离存储
		SeparateIdx(3); //对应的数据项分离存储，并且建立索引
		
		private final int val;
		StoreType(int val) {
	    	this.val = val;
	    }
		
		public int getIntValue()
		{
			return val ;
		}
		
		public static StoreType valueOf(int v)
		{
			switch(v)
			{
			case 1:
				return Normal;
			case 2:
				return Separate;
			case 3:
				return SeparateIdx;
			default:
				throw new IllegalArgumentException("unknow StoreType value="+v);
			}
		}
	}
	
	/**
	 * 对数据成员的控制类型
	 * 
	 * @author Jason Zhu
	 */
//	public static enum CtrlType
//	{
//		None(1), //缺省情况,不做任何控制
//		Hidden(2), //
//		ReadOnly(3); //
//		
//		private final int val;
//		CtrlType(int val) {
//	    	this.val = val;
//	    }
//		
//		public int getIntValue()
//		{
//			return val ;
//		}
//		
//		public static StoreType valueOf(int v)
//		{
//			switch(v)
//			{
//			case 1:
//				return Normal;
//			case 2:
//				return Separate;
//			case 3:
//				return SeparateIdx;
//			default:
//				throw new IllegalArgumentException("unknow StoreType value="+v);
//			}
//		}
//	}
	//public static final XmlDataStruct EMPTY_STRUCT = new XmlDataStruct();
	
	/**
	 * 为在同一个进程空间内进行拷贝粘贴操作所做的支持
	 */
	public static Object COPY_OBJ = null ;
	
	public String getValueTypeStr()
	{
		return "xds" ;
	}
	
	public static void copy(XmlDataMember si)
	{
		COPY_OBJ = si ;
	}
	
	public static void copy(XmlDataStruct xds)
	{
		COPY_OBJ = xds ;
	}
	
	public static boolean canPasteTo(XmlDataStruct parentxds)
	{
		if(parentxds==null)
			return false;
		
		if(COPY_OBJ==null)
			return false;
		
		if(COPY_OBJ instanceof XmlDataMember)
			return true ;
		
		if(COPY_OBJ instanceof XmlDataStruct)
		{
			return true ;
		}
		
		return false;
	}
	
	public static void pasteTo(XmlDataStruct parentxds)
	{
		if(COPY_OBJ==null)
			return;
		
		if(COPY_OBJ instanceof XmlDataMember)
		{
			XmlDataMember si = (XmlDataMember)COPY_OBJ ;
			si = si.copyMe();
			si.belongTo = parentxds;
			parentxds.pname2XmlDataMember.put(si.pname, si);
			return ;
		}
		
		if(COPY_OBJ instanceof XmlDataStruct)
		{
			XmlDataStruct xds = (XmlDataStruct)COPY_OBJ;
			xds = xds.copyMe();
			
			if(xds.name==null||xds.name.equals(""))
			{//根内部信息拷贝
				for(String n:xds.pname2XmlDataMember.keySet())
				{
					XmlDataMember si = xds.pname2XmlDataMember.get(n);
					si.belongTo = parentxds ;
					parentxds.pname2XmlDataMember.put(n, si);
				}
				
				for(String n:xds.pname2SubST.keySet())
				{
					XmlDataStruct tmpxds = xds.pname2SubST.get(n);
					tmpxds.parent = parentxds ;
					parentxds.pname2SubST.put(n, tmpxds);
				}
			}
			else
			{
				xds.parent = parentxds ;
				parentxds.pname2SubST.put(xds.name, xds);
			}
			return ;
		}
	}
	
	private static void checkName(String pname)
	{
		StringBuffer fr = new StringBuffer();
		if(!checkName(pname,fr))
			throw new IllegalArgumentException("invalid name["+pname+"] reson:"+fr.toString());
	}
	
	public static boolean checkName(String pname,StringBuffer failedreson)
	{
		if(pname==null||pname.equals(""))
		{
			failedreson.append("name cannot be null or empty!");
			return false;
		}
		
		char c0 = pname.charAt(0);
		if(!((c0>='a'&&c0<='z')||(c0>='A'&&c0<='Z')||c0=='_'))
		{
			failedreson.append("name first char must be a-z|A-Z|_");
			return false;
		}
		
		int len = pname.length();
		for(int i = 1 ; i < len ; i ++)
		{
			char c = pname.charAt(i);
			if(!((c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9')||c=='_'))
			{
				failedreson.append("name char must be a-z|A-Z|0-9|_");
				return false;
			}
		}
		
		return true ;
	}
	
	private Hashtable<String, XmlDataMember> pname2XmlDataMember = new Hashtable<String, XmlDataMember>();

	private Hashtable<String, XmlDataStruct> pname2SubST = new Hashtable<String, XmlDataStruct>();

	private String name = "";

	private boolean bArray = false;

	private boolean bNullable = true;
	
	/**
	 * 分离存储标记
	 * 
	 * 如果该是子结构,同时该结构的祖先结构不存在分离存储标记,则该结构就可以用来作为分离存储数据的标记
	 * 
	 * 该参数一般情况下没有什么用处,如果在使用环境中要考虑存储的高效性,该参数可以起作用
	 * 
	 * 使用例子:
	 * 	比如流程的实例使用XmlData进行存储,而实例中包含的多个ActivityIns,可以考虑分离存储.这样
	 * 	对应增删改子内容.
	 */
	private boolean bSepStorage = false;

	transient XmlDataStruct parent = null;

	public XmlDataStruct()
	{

	}
	
	public XmlDataStruct(String n)
	{
		this(n,false,true);
	}
	
	public XmlDataStruct(String n,boolean ba,boolean nullable)
	{
		checkName(n);
		
		name = n ;
		bArray = ba ;
		bNullable = nullable ;
	}
	
	public XmlDataStruct copyMe()
	{
		XmlDataStruct xds = new XmlDataStruct();
		
		xds.name = name;

		xds.bArray = bArray;

		xds.bNullable = bNullable;
		
		xds.bSepStorage = bSepStorage;
		
		for(String n:pname2XmlDataMember.keySet())
		{
			XmlDataMember si = pname2XmlDataMember.get(n);
			si = si.copyMe() ;
			si.belongTo = xds ;
			xds.pname2XmlDataMember.put(n, si);
		}
		
		for(String n:pname2SubST.keySet())
		{
			XmlDataStruct tmpxds = pname2SubST.get(n);
			tmpxds = tmpxds.copyMe() ;
			tmpxds.parent = xds ;
			xds.pname2SubST.put(n, tmpxds);
		}
		
		return xds ;
	}
	
	/**
	 * 为了支持对已经存在的数据(肯定符合数据结构)做更新操作
	 * 需要一个弱化的结构,可以考虑使用该方法--该方法返回的结构
	 * 与原来的基本相同,但没有必填内容
	 * @return
	 */
	public XmlDataStruct copyMeWithAllNullable()
	{
		XmlDataStruct xds = new XmlDataStruct();
		
		xds.name = name;

		xds.bArray = bArray;

		xds.bNullable = true;
		
		xds.bSepStorage = bSepStorage;
		
		for(String n:pname2XmlDataMember.keySet())
		{
			XmlDataMember si = pname2XmlDataMember.get(n);
			si = si.copyMe() ;
			si.belongTo = xds ;
			si.setNullable(true) ;
			xds.pname2XmlDataMember.put(n, si);
		}
		
		for(String n:pname2SubST.keySet())
		{
			XmlDataStruct tmpxds = pname2SubST.get(n);
			tmpxds = tmpxds.copyMe() ;
			tmpxds.parent = xds ;
			tmpxds.bNullable = true ;
			xds.pname2SubST.put(n, tmpxds);
		}
		
		return xds ;
	}
	
	/**
	 * 把其他结构中的信息组合到本结构中
	 * @param oxds
	 * @param bcover true表示,如果有重名内容,则用其他结构中的内容进行覆盖
	 * 		false表示不覆盖(=忽略)
	 */
	public void combineAppend(XmlDataStruct oxds,boolean bcover)
	{
		for(Map.Entry<String, XmlDataMember> n2m:oxds.pname2XmlDataMember.entrySet())
		{
			String n = n2m.getKey();
			if(pname2XmlDataMember.containsKey(n)&&!bcover)
				continue ;
			
			XmlDataMember si = n2m.getValue();
			si = si.copyMe() ;
			si.belongTo = this ;
			pname2XmlDataMember.put(n, si);
		}
		
		for(Map.Entry<String, XmlDataStruct> n2s:oxds.pname2SubST.entrySet())
		{
			String n = n2s.getKey();
			if(pname2SubST.containsKey(n)&&!bcover)
				continue ;
			
			XmlDataStruct tmpxds = n2s.getValue();
			tmpxds = tmpxds.copyMe() ;
			tmpxds.parent = this ;
			pname2SubST.put(n, tmpxds);
		}
	}
	
	public void combineAppend(XmlDataStruct oxds)
	{
		combineAppend(oxds,false);
	}

	public boolean isEmptyStruct()
	{
		if (pname2XmlDataMember.size() > 0)
			return false;

		if (pname2SubST.size() > 0)
			return false;

		return true;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof XmlDataStruct))
			return false;

		XmlDataStruct xds = (XmlDataStruct) o;
		if (name == null || name.equals(""))
		{
			if (xds.name != null && !xds.name.equals(""))
				return false;
		}

		if (name == null || !name.equals(xds.name))
			return false;

		if (bArray != xds.bArray)
			return false;
		if (bNullable != xds.bNullable)
			return false;
		
		if(bSepStorage!=xds.bSepStorage)
			return false;

		if (pname2XmlDataMember.size() != xds.pname2XmlDataMember.size())
			return false;

		if (pname2SubST.size() != xds.pname2SubST.size())
			return false;

		for (Enumeration en = pname2XmlDataMember.keys(); en.hasMoreElements();)
		{
			String k = (String) en.nextElement();
			XmlDataMember msi = pname2XmlDataMember.get(k);
			XmlDataMember osi = xds.pname2XmlDataMember.get(k);
			if (!msi.equals(osi))
				return false;
		}

		for (Enumeration en = pname2SubST.keys(); en.hasMoreElements();)
		{
			String k = (String) en.nextElement();
			XmlDataStruct mds = pname2SubST.get(k);
			XmlDataStruct ods = xds.pname2SubST.get(k);
			if (!mds.equals(ods))
				return false;
		}

		return true;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String n)
	{
		checkName(n);
		name = n;
	}
//	public void setName(String n)
//	{
//		name = n;
//	}

	public String getPath()
	{
		if (parent == null)
		{
//			if (name == null||name.equals(""))
//				return "/";
//
//			return "/" + name + "/";
			return "/";
		}

		return parent.getPath() + name + "/";
	}
	
	
	
	/**
	 * 或者该结构下面可以做分离存储的成员
	 * @return
	 */
	public List<XmlDataMember> getSeparateXmlValDefs()
	{
		ArrayList<XmlDataMember> sis = new ArrayList<XmlDataMember>();
		getSeparateXmlValDefs(sis);
		return sis ;
	}
	
	/**
	 * 根据本结构定义的分离存储
	 * 过滤出有分离存储结构组成的子集结构
	 * @return 由分离存储组成的结构
	 */
	public XmlDataStruct filterSubSetSeparateStruct()
	{
		XmlDataStruct xds = new XmlDataStruct();
		List<XmlDataMember> sis = getSeparateXmlValDefs() ;
		for(XmlDataMember si:sis)
		{
			String ps = si.getPath() ;
			XmlDataPath xdp = new XmlDataPath(ps);
			xds.setXmlDataMemberByPath(xdp.getPath(), si.getValType(), si.isArray(), si.isNullable(), si.getMaxLen(), si.getStoreType());
		}
		return xds ;
	}
	
	private void getSeparateXmlValDefs(List<XmlDataMember> sis)
	{
		for(XmlDataMember si:pname2XmlDataMember.values())
		{
			StoreType st = si.getStoreTypeWithBelongTo() ;
			if(st==StoreType.Separate||st==StoreType.SeparateIdx)
			{
				sis.add(si) ;
			}
		}
		
		for(XmlDataStruct subxds:pname2SubST.values())
		{
			subxds.getSeparateXmlValDefs(sis);
		}
	}
	
	
	/**
	 * 随机更加自身的结构产生符合本结构的XmlData数据， 该方法用来产生测试数据
	 * 
	 * @return
	 */
	public XmlData randomCreateData()
	{
		XmlData xd = new XmlData();

		for (Enumeration<String> en = pname2XmlDataMember.keys(); en
				.hasMoreElements();)
		{
			String n = en.nextElement();
			XmlDataMember si = pname2XmlDataMember.get(n);
			Object v = si.randomCreateValue();
			if (v != null)
			{
				if (si.isArray())
					xd.setParamValues(n, (Object[]) v);
				else
					xd.setParamValue(n, v);
			}
		}

		for (Enumeration<String> en = pname2SubST.keys(); en.hasMoreElements();)
		{
			String n = en.nextElement();
			XmlDataStruct xds = pname2SubST.get(n);
			if (xds.isArray())
			{
				int c = new Random().nextInt(5);
				List<XmlData> tmpxds = xd.getOrCreateSubDataArray(n);
				for (int i = 0; i < c; i++)
				{
					XmlData xd0 = xds.randomCreateData();
					tmpxds.add(xd0);
				}
			}
			else
			{
				XmlData xd0 = xds.randomCreateData();
				xd.setSubDataSingle(n, xd0);
			}
		}
		return xd;
	}

	public String toString()
	{
		return getPath();
	}

	public boolean isArray()
	{
		return bArray;
	}
	
	public boolean isSepStorage()
	{
		return bSepStorage;
	}

	/**
	 * 判断是否是有效的分离存储标记
	 * 如果本结构是根,则无效,如果本结构的父结构存在有效的分离标记
	 * @return
	 */
	public boolean isValidSepStorage()
	{
		if(!bSepStorage)
			return false;
		
		//根结构没有意义
		if(this.parent==null)
			return false;
		
		XmlDataStruct tmpp = parent ;
		
		while(tmpp!=null)
		{
			if(tmpp.isValidSepStorage())
				return false;
			tmpp = tmpp.parent;
		}
		
		return true ;
	}
	/**
	 * 判断本身，并且继承了父结构的是否是多值——相对于一个XmlData数据元素
	 * 
	 * @return
	 */
	public boolean isArrayWithParent()
	{
		if (parent == null)
			return false;//根只能是非数组

		if (bArray)
			return true;

		boolean parray = parent.isArrayWithParent();
		if (parray)
			return true;

		return bArray;
	}

	public void setIsArray(boolean b)
	{
		bArray = b;
	}
	
	public void setIsSepStorage(boolean b)
	{
		bSepStorage = b ;
	}

	public boolean isNullable()
	{
		return bNullable;
	}

	public void setIsNullable(boolean b)
	{
		bNullable = b;
	}

	public XmlDataStruct getParent()
	{
		return parent;
	}
	

	public XmlDataMember setXmlDataMember(String pname, String valtype)
	{
		return setXmlDataMember(pname, valtype, false, true, -1,StoreType.Normal);
	}
	
	public XmlDataMember setXmlDataMember(String pname ,String title, String valtype)
	{
		return setXmlDataMember(pname,title, valtype, false, true, -1,StoreType.Normal,false,null);
	}
	
	public XmlDataMember setXmlDataMember(String pname ,String title, String valtype,String defstrv)
	{
		return setXmlDataMember(pname,title, valtype, false, true, -1,StoreType.Normal,false,defstrv);
	}
	
	public XmlDataMember setXmlDataMember(String pname ,String title, String valtype,boolean bmulti_rows)
	{
		return setXmlDataMember(pname,title, valtype, false, true, -1,StoreType.Normal,bmulti_rows,null);
	}
	
	public XmlDataMember setXmlDataMember(String pname ,String title, String valtype,boolean bmulti_rows,String strval)
	{
		return setXmlDataMember(pname,title, valtype, false, true, -1,StoreType.Normal,bmulti_rows,strval);
	}

	/**
	 * 设置结构项
	 * 
	 * @param path
	 * @param valtype
	 * @param barray
	 */
//	public void setStructItem(String pname, String valtype, boolean barray,
//			boolean bnullable, int maxlen)
//	{
//		setStructItem(pname, valtype, barray,
//				bnullable, maxlen,StoreType.Normal);
//		
//	}
	
	public XmlDataMember setXmlDataMember(String pname, String valtype, boolean barray,
			boolean bnullable, int maxlen)
	{
		return setXmlDataMember(pname, valtype, barray,
				bnullable, maxlen,StoreType.Normal);
	}
	
	public XmlDataMember setXmlDataMember(String pname,String valtype, boolean barray,
			boolean bnullable, int maxlen,StoreType st)
	{
		return setXmlDataMember(pname,null,valtype, barray,
				bnullable, maxlen,st,false,null);
	}
	
	public XmlDataMember setXmlDataMember(String pname,String title, String valtype, boolean barray,
			boolean bnullable, int maxlen,StoreType st)
	{
		return setXmlDataMember(pname,title, valtype, barray,
				bnullable, maxlen,st,false,null) ;
	}
	
	public XmlDataMember setXmlDataMember(String pname,String title, String valtype, boolean barray,
			boolean bnullable, int maxlen,StoreType st,boolean bmulti_rows,String defstrval)
	{
		//查看名称是否与之不区分大小写的同名
		for(String s:pname2XmlDataMember.keySet())
		{
			if(s.equalsIgnoreCase(pname))
			{//如果有，就用原来的名称
				pname = s ;
				break;
			}
		}
		
		XmlDataMember si = new XmlDataMember(pname,title, valtype, barray, bnullable,
				maxlen,st,bmulti_rows,defstrval);
		si.belongTo = this;
		si.orderNum = pname2XmlDataMember.size()+1 ;
		pname2XmlDataMember.put(pname, si);
		return si ;
	}
	
	public XmlDataMember setXmlDataMember(String pname,XmlValDef xvd)
	{
		XmlDataMember si = new XmlDataMember(pname,xvd);
		si.belongTo = this;
		pname2XmlDataMember.put(pname, si);
		return si ;
	}

	public void unsetXmlDataMember(String pname)
	{
		XmlDataMember si = pname2XmlDataMember.remove(pname);
		if (si != null)
			si.belongTo = null;
	}
	
	public List<XmlDataMember> getXmlDataMembers()
	{
		ArrayList<XmlDataMember> rets = new ArrayList<XmlDataMember>() ;
		rets.addAll(pname2XmlDataMember.values()) ;
		
		Convert.sort(rets) ;
		return rets;
	}

	public String[] getXmlDataMemberNames()
	{
		String[] rets = new String[pname2XmlDataMember.size()];
		pname2XmlDataMember.keySet().toArray(rets);
		return rets;
	}

	public XmlDataMember getXmlDataMember(String pname)
	{
		return pname2XmlDataMember.get(pname);
	}
	
	public List<XmlDataMember> getSubXmlDataMembers()
	{
		ArrayList<XmlDataMember> rets = new ArrayList<XmlDataMember>() ;
		rets.addAll(pname2XmlDataMember.values()) ;
		return rets;
	}

	public XmlDataStruct getOrCreateSubStruct(String pname)
	{
		StringBuffer fr = new StringBuffer();
		if(!checkName(pname,fr))
			throw new IllegalArgumentException("invalid StructItem name for:"+fr.toString());
			
//		查看名称是否与之不区分大小写的同名
		for(String s:pname2SubST.keySet())
		{
			if(s.equalsIgnoreCase(pname))
			{//如果有，就用原来的名称
				pname = s ;
				break;
			}
		}
		
		XmlDataStruct xds = pname2SubST.get(pname);
		if (xds != null)
			return xds;

		synchronized (this)
		{
			//进入同步代码后再寻找一次
			xds = pname2SubST.get(pname);
			if (xds != null)
				return xds;
			
			xds = new XmlDataStruct();
			xds.parent = this;
			xds.name = pname;
			pname2SubST.put(pname, xds);
			return xds;
		}
	}

	public XmlDataStruct setSubStruct(String pname, boolean barray,
			boolean bnullable,boolean bsep_store)
	{
		StringBuffer fr = new StringBuffer();
		if(!checkName(pname,fr))
			throw new IllegalArgumentException("invalid StructItem name for:"+fr.toString());
		
//		查看名称是否与之不区分大小写的同名
		for(String s:pname2SubST.keySet())
		{
			if(s.equalsIgnoreCase(pname))
			{//如果有，就用原来的名称
				pname = s ;
				break;
			}
		}
		
		XmlDataStruct xds = pname2SubST.get(pname);
		if (xds == null)
		{
			synchronized (this)
			{
				if (xds == null)
				{
					xds = new XmlDataStruct();
					xds.parent = this;
					pname2SubST.put(pname, xds);
				}
			}
		}

		xds.name = pname;
		xds.bArray = barray;
		xds.bNullable = bnullable;
		xds.bSepStorage = bsep_store ;

		return xds;
	}
	
	public XmlDataStruct setSubStruct(XmlDataStruct xds)
	{
		String pname = xds.getName();
		if(pname==null||pname.equals(""))
			throw new IllegalArgumentException("sub xml data struct must has name");
		
		StringBuffer fr = new StringBuffer();
		if(!checkName(pname,fr))
			throw new IllegalArgumentException("invalid StructItem name for:"+fr.toString());
		
//		查看名称是否与之不区分大小写的同名
		for(String s:pname2SubST.keySet())
		{
			if(s.equalsIgnoreCase(pname))
			{//如果有，就用原来的名称
				pname = s ;
				break;
			}
		}
		
		
		xds.parent = this;

		pname2SubST.put(pname, xds);
		
		return xds;
	}
	
	public XmlDataStruct setSubStruct(String pname,XmlDataStruct xds, boolean barray,
			boolean bnullable,boolean bsep_store)
	{
		StringBuffer fr = new StringBuffer();
		if(!checkName(pname,fr))
			throw new IllegalArgumentException("invalid StructItem name for:"+fr.toString());
		
//		查看名称是否与之不区分大小写的同名
		for(String s:pname2SubST.keySet())
		{
			if(s.equalsIgnoreCase(pname))
			{//如果有，就用原来的名称
				pname = s ;
				break;
			}
		}
		
		
		xds.name = pname;
		xds.bArray = barray;
		xds.bNullable = bnullable;
		xds.bSepStorage = bsep_store ;
		xds.parent = this;

		pname2SubST.put(pname, xds);
		
		return xds;
	}

	public String[] getSubStructNames()
	{
		String[] rets = new String[pname2SubST.size()];
		pname2SubST.keySet().toArray(rets);
		return rets;
	}

	public XmlDataStruct getSubStruct(String pname)
	{
//		查看名称是否与之不区分大小写的同名
		for(String s:pname2SubST.keySet())
		{
			if(s.equalsIgnoreCase(pname))
			{//如果有，就用原来的名称
				pname = s ;
				break;
			}
		}
		
		return pname2SubST.get(pname);
	}

	public XmlDataStruct getSubStructByPath(String[] path)
	{
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("path cannot be null");

		XmlDataStruct tmpxds = this;
		for (int i = 0; i < path.length; i++)
		{
			tmpxds = tmpxds.getSubStruct(path[i]);
			if (tmpxds == null)
				return null;
		}

		return tmpxds;
	}
	
	public XmlDataStruct getSubStructByPath(PathItem[] path)
	{
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("path cannot be null");

		XmlDataStruct tmpxds = this;
		for (int i = 0; i < path.length; i++)
		{
			tmpxds = tmpxds.getSubStruct(path[i].getPathItemName());
			if (tmpxds == null)
				return null;
		}

		return tmpxds;
	}
	
	public XmlDataStruct getSubStructByPath(XmlDataPath xdp)
	{
		if(xdp.isRoot())
			return this ;
		
		return getSubStructByPath(xdp.getPath());
	}

	public void unsetSubStruct(String pname)
	{
		XmlDataStruct xds = pname2SubST.remove(pname);
		if (xds != null)
			xds.parent = null;
	}

	public void setXmlDataMemberByPath(String[] path, String valtype)
	{
		setXmlDataMemberByPath(path, valtype, false, true, -1);
	}
	
	
	
	public void setXmlDataMemberByPath(String strpath, String valtype)
	{
		XmlDataPath xdp = new XmlDataPath(strpath);
		setXmlDataMemberByPath(xdp.getPath(), valtype, false, true, -1);
	}
	
	public void setXmlDataMemberByPath(String strpath, String valtype,
			boolean barray, boolean bnullable, int maxlen)
	{
		XmlDataPath xdp = new XmlDataPath(strpath);
		setXmlDataMemberByPath(xdp.getPath(), valtype, barray, bnullable, maxlen);
	}
	
	public void setXmlDataMemberByPath(String[] path, String valtype,
			boolean barray, boolean bnullable, int maxlen)
	{
		 setXmlDataMemberByPath(path, valtype,
					barray, bnullable, maxlen,StoreType.Normal);
	}
	
	public void setXmlDataMemberByPath(PathItem[] path, String valtype,
			boolean barray, boolean bnullable, int maxlen)
	{
		 setXmlDataMemberByPath(path, valtype,
					barray, bnullable, maxlen,StoreType.Normal);
	}

	public void setXmlDataMemberByPath(String[] path, String valtype,
			boolean barray, boolean bnullable, int maxlen,StoreType st)
	{
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("path cannot be null");

		XmlDataStruct tmpxds = this;
		for (int i = 0; i < path.length - 1; i++)
		{
			tmpxds = tmpxds.getOrCreateSubStruct(path[i]);
		}

		tmpxds.setXmlDataMember(path[path.length - 1], valtype, barray, bnullable,
				maxlen,st);
	}
	
	/**
	 * 根据XmlDataPath对象设置结构中的内容,设置结构信息
	 * @param xdp
	 * @param valtype
	 * @param bnullable
	 * @param maxlen
	 * @param st
	 * @return 如果设置的是子结构,则返回路径指向的子结构,如果是成员,则返回路径指向成员的父结构
	 */
	public XmlDataStruct setByPath(XmlDataPath xdp,String valtype,boolean bnullable,int maxlen,StoreType st)
	{
//		if(xdp.isStruct())
//			throw new IllegalArgumentException("path ="+xdp.toString()+" is struct");
		
		PathItem[] pis = xdp.getPath() ;
		int num = pis.length ;
		XmlDataStruct curxds = this ;
		for(int i = 0 ; i < num - 1 ; i ++)
		{
			curxds = curxds.getOrCreateSubStruct(pis[i].getPathItemName());
			curxds.setIsArray(pis[i].isArray());
			//curxds.setIsNullable(pis[i].)
		}
		
		PathItem lpi = pis[num-1];
		if(xdp.isStruct())
		{
			curxds = curxds.getOrCreateSubStruct(lpi.getPathItemName());
			curxds.setIsArray(lpi.isArray());
			curxds.setIsNullable(bnullable);
		}
		else
		{
			curxds.setXmlDataMember(lpi.getPathItemName(), valtype, lpi.isArray(), bnullable, maxlen, st);
		}
		
		return curxds ;
	}
	
	public XmlDataStruct setSubStructByPath(XmlDataPath xdp,boolean bnullable)
	{
		if(!xdp.isStruct())
			throw new IllegalArgumentException("path="+xdp.toString()+" is not struct!");
		
		return setByPath(xdp,null,bnullable,-1,StoreType.Normal);
	}
	
	public void setXmlDataMemberByPath(PathItem[] path, String valtype,
			boolean barray, boolean bnullable, int maxlen,StoreType st)
	{
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("path cannot be null");

		XmlDataStruct tmpxds = this;
		for (int i = 0; i < path.length - 1; i++)
		{
			tmpxds = tmpxds.getOrCreateSubStruct(path[i].getPathItemName());
		}

		tmpxds.setXmlDataMember(path[path.length - 1].getPathItemName(), valtype, barray, bnullable,
				maxlen,st);
	}

	public XmlDataMember getXmlDataMemberByPath(String[] path)
	{
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("path cannot be null");

		XmlDataStruct tmpxds = this;
		for (int i = 0; i < path.length - 1; i++)
		{
			tmpxds = tmpxds.getSubStruct(path[i]);
			if (tmpxds == null)
				return null;
		}

		return tmpxds.getXmlDataMember(path[path.length - 1]);
	}
	
	public XmlDataMember getXmlDataMemberByPath(PathItem[] path)
	{
		if (path == null || path.length <= 0)
			throw new IllegalArgumentException("path cannot be null");

		XmlDataStruct tmpxds = this;
		for (int i = 0; i < path.length - 1; i++)
		{
			tmpxds = tmpxds.getSubStruct(path[i].getPathItemName());
			if (tmpxds == null)
				return null;
		}

		return tmpxds.getXmlDataMember(path[path.length - 1].getPathItemName());
	}
	
	public XmlDataMember getSingleXmlDataMemberByPath(String p)
	{
		XmlDataPath xdp = new XmlDataPath(p);
		if(xdp.isStruct())
			throw new IllegalArgumentException("invalid path becase it is a struct path");
		
		XmlDataMember si = getXmlDataMemberByPath(xdp.getPath());
		if(si==null)
			return null ;
		
		if(si.isArrayWithBelongTo())
			throw new IllegalArgumentException("path ="+p+" is array");
		
		return si ;
	}

	public boolean checkMatchStruct(XmlData xd, StringBuffer failedreson)
	{
		for (XmlDataMember si : pname2XmlDataMember.values())
		{
			// sixds.add(si.toXmlData());
			if (!si.isNullable())
			{
				if (si.isArray())
				{
					List v = xd.getParamValues(si.pname);
					if (v == null || v.size() <= 0)
					{
						failedreson.append(si.pname + " cannot be null!");
						return false;
					}
				}
				else
				{
					Object o = xd.getParamValue(si.pname);
					if (o == null)
					{
						failedreson.append(si.pname + " cannot be null!");
						return false;
					}
				}
			}

			if (si.getValType().equals(XmlVal.VAL_TYPE_STR))
			{
				if (si.isArray())
				{
					List v = xd.getParamValues(si.pname);
					if (v != null)
					{
						for (Object tmpo : v)
						{
							String tmps = (String) tmpo;
							if (tmps.length() > si.getMaxLen())
							{
								failedreson.append(si.pname
										+ " string len big then max len="
										+ si.getMaxLen());
								return false;
							}
						}
					}
				}
				else
				{
					String tmps = (String) xd.getParamValue(si.pname);
					if (tmps != null && tmps.length() > si.getMaxLen())
					{
						failedreson.append(si.pname
								+ " string len big then max len=" + si.getMaxLen());
						return false;
					}
				}
			}
		}

		for (XmlDataStruct xds : pname2SubST.values())
		{
			if (!xds.bNullable)
			{
				if (xds.bArray)
				{
					List<XmlData> lxds = xd.getSubDataArray(xds.name);
					if (lxds == null || lxds.size() <= 0)
					{
						failedreson.append(xds.name + " cannot be null!");
						return false;
					}

					for (XmlData tmpxd : lxds)
					{
						if (!checkMatchStruct(tmpxd, failedreson))
							return false;
					}
				}
				else
				{
					XmlData lxds = xd.getSubDataSingle(xds.name);
					if (lxds == null)
					{
						failedreson.append(xds.name + " cannot be null!");
						return false;
					}

					if (!checkMatchStruct(lxds, failedreson))
						return false;
				}
			}

		}

		return true;
	}

	/**
	 * 判断满足本结构的XmlData是否可以适应目标XmlDataStruct结构
	 * 
	 * 它可以用来判断不同的输出输入是否可以互串
	 * 
	 * @param tarxds
	 * @return
	 */
	public boolean checkFitFor(XmlDataStruct tarxds, StringBuffer failedreson)
	{
		if (tarxds == null)
			return true;

		// 判断目标和本结构的对应项是否类型一致，必填项在本结构中是否必填
		for (XmlDataMember tarsi : tarxds.pname2XmlDataMember.values())
		{
			XmlDataMember si = pname2XmlDataMember.get(tarsi.getName());
			if (si == null)
			{//如果源成员为null，并且目标不能为null，则不能匹配
				if (failedreson != null)
					failedreson
							.append("no member with path=" + tarsi.getPath()+" that target needed!");
				return false;
			}

			if (!tarsi.isNullable() && si.isNullable())
			{
				if (failedreson != null)
					failedreson.append("tar member with path="
							+ tarsi.getPath() + " is not nullable");
				return false;
			}

			// 判断数组是否一致
			if (tarsi.isArray() != si.isArray())
			{
				if (failedreson != null)
					failedreson.append("tar member with path="
							+ tarsi.getPath() + " array=" + tarsi.isArray());
				return false;
			}

			// 判断类型
			if (!tarsi.getValType().equals(si.getValType()))
			{
				if (failedreson != null)
					failedreson.append("tar member with path="
							+ tarsi.getPath() + " type=" + tarsi.getValType());
				return false;
			}

			// 如果是字符串，则判断长度是否冲突
			if (si.getValType().equals(XmlVal.VAL_TYPE_STR))
			{
				if (tarsi.getMaxLen() < si.getMaxLen())
				{
					if (failedreson != null)
						failedreson.append("tar string member with path="
								+ tarsi.getPath() + " max len="
								+ tarsi.getMaxLen());
					return false;
				}
			}
		}

		for (XmlDataStruct tar_subxds : tarxds.pname2SubST.values())
		{
			XmlDataStruct subxds = pname2SubST.get(tar_subxds.getName());
			if (subxds == null)
			{
				if (failedreson != null)
					failedreson.append("no sub struct with name="
							+ tar_subxds.getName());
				return false;
			}

			if (!tar_subxds.isNullable() && subxds.isNullable())
			{
				if (failedreson != null)
					failedreson.append("tar sub struct with name="
							+ tar_subxds.getName() + " is not nullable");
				return false;
			}

			// 判断数组是否一致
			if (tar_subxds.isArray() != subxds.isArray())
			{
				if (failedreson != null)
					failedreson.append("tar sub struct with name="
							+ tar_subxds.getName() + " array="
							+ tar_subxds.isArray());
				return false;
			}

			if (!subxds.checkFitFor(tar_subxds, failedreson))
				return false;
		}

		return true;
	}

	/**
	 * 列举本结构中可以用来和其他结构进行连接映射的所有路径
	 * 
	 * 规则如下:
	 * 1,如果是空结构,则返回空
	 * 2,第一级子节点都可以作为连接映射路径(包含成员和子结构)
	 * 3,如果第一级的子结构是单值子结构,则可以递归往下寻找路径
	 * @return
	 */
	public ArrayList<String> listCanJoinMatchPaths()
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		
		listCanJoinMatchPaths(rets);
		return rets ;
	}
	
	private void listCanJoinMatchPaths(ArrayList<String> paths)
	{
		if(this.isEmptyStruct())
			return ;
		
		//自己既然被判断,则肯定可以(包含根/)
		paths.add(toFullPath());
		//加入所有成员路径
		for(XmlDataMember xvd:pname2XmlDataMember.values())
		{
			paths.add(xvd.toFullPath()) ;
		}
		
		for(XmlDataStruct xds:this.pname2SubST.values())
		{
			if(!xds.isArray())
			{//如果是非数组,则往下递归
				xds.listCanJoinMatchPaths(paths);
			}
			else
			{
				paths.add(xds.toFullPath());
			}
		}
		
	}
	// private boolean checkMatchStruct(XmlDataStruct)

	public String toFullPath()
	{
		return XmlDataPath.createPath(this).toFullString();
	}
	
	
	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();

		if (name != null)
			xd.setParamValue("name", name);
		xd.setParamValue("is_array", bArray);
		xd.setParamValue("nullable", bNullable);
		xd.setParamValue("is_sep_storage", bSepStorage);

		List<XmlData> sixds = xd.getOrCreateSubDataArray("struct_item");
		for (XmlDataMember si : pname2XmlDataMember.values())
		{
			sixds.add(si.toXmlData());
		}

		List<XmlData> subxds = xd.getOrCreateSubDataArray("sub_struct");
		for (XmlDataStruct xds : pname2SubST.values())
		{
			subxds.add(xds.toXmlData());
		}

		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		name = xd.getParamValueStr("name");
		bArray = xd.getParamValueBool("is_array", false);
		bNullable = xd.getParamValueBool("nullable", true);
		bSepStorage = xd.getParamValueBool("is_sep_storage", false);

		List<XmlData> sixds = xd.getSubDataArray("struct_item");
		if (sixds != null)
		{
			for (XmlData tmpxd : sixds)
			{
				XmlDataMember si = new XmlDataMember();
				si.fromXmlData(tmpxd);
				si.belongTo = this;
				pname2XmlDataMember.put(si.pname, si);
			}
		}

		List<XmlData> subxds = xd.getSubDataArray("sub_struct");
		if (subxds != null)
		{
			for (XmlData tmpxd : subxds)
			{
				XmlDataStruct xds = new XmlDataStruct();
				xds.fromXmlData(tmpxd);
				xds.parent = this;
				pname2SubST.put(xds.name, xds);
			}
		}
	}
}
