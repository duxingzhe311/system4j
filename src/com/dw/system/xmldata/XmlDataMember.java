/**
 * 
 */
package com.dw.system.xmldata;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

import com.dw.system.xmldata.XmlDataStruct.StoreType;
import com.dw.system.xmldata.XmlVal.XmlValType;

public class XmlDataMember implements IXmlDataable,Comparable<XmlDataMember>
{
	String pname = null;
	
	String title = null ;

	XmlValDef xmlValDef = new XmlValDef();
	
	/**
	 * ˳���ںܶೡ�ϣ����ݳ�Ա��Ҫ��װһ����˳��չʾ
	 */
	transient int orderNum = 100 ;

	transient XmlDataStruct belongTo = null;

	public XmlDataMember()
	{
	}
	
	public XmlDataMember(String n, String vt, boolean ba, boolean bnullable,
			int maxlen,StoreType st)
	{
		setStructInfo(n,null,vt, ba, bnullable, maxlen,st,false,null);
	}
	
	public XmlDataMember(String n,String title, String vt, boolean ba, boolean bnullable,
			int maxlen,StoreType st)
	{
		setStructInfo(n,title, vt, ba, bnullable, maxlen,st,false,null);
	}

	public XmlDataMember(String n,String title, String vt, boolean ba, boolean bnullable,
			int maxlen,StoreType st,boolean bmultirows,String defstrv)
	{
		setStructInfo(n,title, vt, ba, bnullable, maxlen,st,bmultirows,defstrv);
	}
	
	public XmlDataMember(String n,XmlValDef xvd)
	{
		if(xvd==null)
			throw new IllegalArgumentException("XmlValDef cannot be null");
		
		StringBuffer fr = new StringBuffer();
		if(!XmlDataStruct.checkName(n,fr))
			throw new IllegalArgumentException("invalid StructItem name for:"+fr.toString());
		
		pname = n;
		xmlValDef = xvd ;
	}
	
	public XmlDataMember copyMe()
	{
		XmlDataMember si = new XmlDataMember();
		si.pname = pname;
		si.title = title ;

		si.xmlValDef = xmlValDef.copyMe();
		
		return si ;
	}

	public XmlValDef getXmlValDef()
	{
		return xmlValDef ;
	}
	
	public Object randomCreateValue()
	{
		return xmlValDef.randomCreateValue();
	}

	void setStructInfo(String n,String t, String vt, boolean ba, boolean bnullable,
			int maxlen,StoreType st,boolean bmultirows,String defstrval)
	{
		StringBuffer fr = new StringBuffer();
		if(!XmlDataStruct.checkName(n,fr))
			throw new IllegalArgumentException("invalid StructItem name for:"+fr.toString());
		
		pname = n;
		title = t ;
		xmlValDef.setStructInfo(vt, ba, bnullable, maxlen, st,bmultirows,defstrval);
	}

	public XmlDataStruct getBelongTo()
	{
		return belongTo;
	}

	public String getPath()
	{
		if(belongTo==null)
			return pname ;
		
		return belongTo.getPath() + pname;
	}

	public boolean equals(Object o)
	{
		XmlDataMember si = (XmlDataMember) o;
		if (!pname.equals(si.pname))
			return false;
		
		return xmlValDef.equals(si.xmlValDef);
	}
	
	public boolean equalsByBelongTo(Object o)
	{
		XmlDataMember si = (XmlDataMember) o;
		if (!pname.equals(si.pname))
			return false;
		
		if(!xmlValDef.equals(si.xmlValDef))
			return false;
		
		if (isArrayWithBelongTo() != si.isArrayWithBelongTo())
			return false;
		
		if(getStoreTypeWithBelongTo()!=si.getStoreTypeWithBelongTo())
			return false;

		return true;
	}

	public String getName()
	{
		return pname;
	}

	public String getValType()
	{
		return xmlValDef.getValType();
	}
	
	public String getDefaultStrVal()
	{
		return xmlValDef.getDefaultStrVal() ;
	}
	
	public boolean isMultiRows()
	{
		return xmlValDef.isMultiRows() ;
	}

	public boolean isArray()
	{
		return xmlValDef.isArray();
	}

	/**
	 * �жϱ������Ҽ̳��˸��ṹ���Ƿ��Ƕ�ֵ���������һ��XmlData����Ԫ��
	 * 
	 * @return
	 */
	public boolean isArrayWithBelongTo()
	{
		if (isArray())
			return true;

		if (belongTo == null)
			return false;

		boolean parray = belongTo.isArrayWithParent();
		if (parray)
			return true;

		return false;
	}

	public boolean isNullable()
	{
		return xmlValDef.isNullable();
	}
	
	void setNullable(boolean b)
	{
		xmlValDef.bNullable = b ;
	}

	public int getMaxLen()
	{
		return xmlValDef.getMaxLen();
	}

	public StoreType getStoreType()
	{
		return xmlValDef.getStoreType() ;
	}
	
	/**
	 * �жϱ���Ա�Ƿ���������������ݷ���洢
	 * Ŀǰֻ֧�ֵ�ֵ���ݳ�Ա
	 * 
	 * �÷����������������ж��Ƿ���Ҫ��������洢��֧��
	 * @return
	 */
	public StoreType getStoreTypeWithBelongTo()
	{
		if(getStoreType()==StoreType.Normal)
			return StoreType.Normal ;
		
		if(isArrayWithBelongTo()) //���鲻ֱ��֧�ַ���洢
			return StoreType.Normal;
		
		int maxLen = getMaxLen();
		String valType = getValType();
		if(XmlVal.VAL_TYPE_STR.equals(getValType()))
		{
			
			if(maxLen<=0||maxLen>=1000)
				return StoreType.Normal;
			
			if(maxLen>500&&maxLen<1000)//���ȴ���500���ַ��������ܽ�������
				return StoreType.Separate ;
		}
		else if(XmlVal.VAL_TYPE_BYTEARRAY.equals(valType))
		{
			if(maxLen<=0||maxLen>=1000)
				return StoreType.Normal;
			
			return StoreType.Separate ;
		}
		
		return getStoreType() ;
	}
	
	public String toFullPath()
	{
		return XmlDataPath.createPath(this).toFullString();
	}
	
	public String getTitle()
	{
		if(title!=null&&!title.equals(""))
			return title ;
		return pname ;
	}
	
	public void setTitle(String t)
	{
		title = t ;
	}
	
	public int getOrderNum()
	{
		return orderNum ;
	}
	
	public void setOrderNum(int ordn)
	{
		orderNum = ordn ;
	}
	//public String getDefaultXml
	
	
	public XmlData toXmlData()
	{
		XmlData xd = xmlValDef.toXmlData();
		if(pname!=null)
			xd.setParamValue("name", pname);
		
		if(title!=null)
			xd.setParamValue("title", title);
		
		xd.setParamValue("order_num", orderNum);
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		xmlValDef.fromXmlData(xd);
		pname = xd.getParamValueStr("name");
		title = xd.getParamValueStr("title");
		orderNum = xd.getParamValueInt32("order_num", 100) ;
	}

	public int compareTo(XmlDataMember o)
	{
		return orderNum - o.orderNum;
	}
}