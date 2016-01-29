package com.dw.comp.view;

import java.util.ArrayList;
import java.util.List;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.*;

/**
 * ר��ΪView�ṩ��ʾ���Ƶ�Ԫ��Ԫ��
 * 
 * �����ܶ�������,���������View������
 * 
 * ����Ҫ���������View��Ҫ����н���Ԫ�ص��ض�����,չʾ,�����
 * 
 * �������Ը��ݲ�ͬ�Ŀ���״̬����ͬ����ʾ,����ͬ�����
 * 
 * ����,����һ��BizView����ͬʱ���������ͬ��Cell,��ͨ����ͬ��name��������
 * BizViewCellҪ��֤Cell֮�䲻����ɳ�ͻ
 * 
 * Cell���Լ���һ��ֵ,�������ǻ�������,Ҳ������XmlData����,Ҳ����û��.
 * ��Cell��View������ָ��������֮��,�Ϳ��Ա���Ϊ���ֵ�ṩ
 * 
 * @author Jason Zhu
 */
public class ViewCell
{
	public static enum CtrlType
	{
		ignore,//cell�����������
		hidden,//cell ����,����ֵ��������
		read, //cell��ʾ,����������--�����ϲ����޸���ֵ
		write, //cell��ʾ,���������
		write_need,//cell��ʾ,���������,���ұ���,Ҳ�����������Ϊnull
	}
	
	String title = null ;
	
	String desc = null ;
	
	String strCont = null ;
	
	IXmlDataDef cellXmlDataDef = null ;
	
	
	transient boolean bValid = false ;
	
	
	public ViewCell()
	{
		
	}
	
	public ViewCell(String title,String desc,String strcont,
			IXmlDataDef xmldata_def)
	{		
		this.title = title ;
		
		this.desc = desc ;
		
		this.strCont = strcont ;
		
		cellXmlDataDef = xmldata_def ;
		
		
	}
	
	

	
	public boolean isValid()
	{
		return bValid ;
	}
	
	public String getTitle()
	{
		return title ;
	}
	
	public void setTitle(String t)
	{
		title = t ;
	}
	
	public String getDesc()
	{
		return desc ;
	}
	
	public void setDesc(String d)
	{
		desc = d ;
	}
	
	public String getStrCont()
	{
		return strCont ;
	}
	
	public void setStrCont(String sc)
	{
		strCont = sc ;
	}
	
	public IXmlDataDef getCellXmlDataDef()
	{
		return cellXmlDataDef ;
	}
	
	public void setCellXmlDataDef(IXmlDataDef xdd)
	{
		cellXmlDataDef = xdd;
	}
	
	
	
	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		
		if(title!=null)
			xd.setParamValue("title",title);
		
		if(desc!=null)
			xd.setParamValue("desc",desc) ;
		
		if(strCont!=null)
			xd.setParamValue("cont", strCont);
		
		//xd.setParamValue("cont_type", contType.toString()) ;
		
		if(cellXmlDataDef!=null)
		{
			XmlData tmpxd = cellXmlDataDef.toXmlData() ;
			
			if(cellXmlDataDef instanceof XmlDataStruct)
			{
				tmpxd.setParamValue("def_type", "struct");
			}
			else if(cellXmlDataDef instanceof XmlValDef)
			{
				tmpxd.setParamValue("def_type", "val");
			}
			
			xd.setSubDataSingle("xmldate_def", tmpxd);
		}
		
	
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		title = xd.getParamValueStr("title");

		desc = xd.getParamValueStr("desc") ;
		strCont = xd.getParamValueStr("cont");

		XmlData tmpxd = xd.getSubDataSingle("xmldate_def");
		if(tmpxd!=null)
		{
			String deft = tmpxd.getParamValueStr("def_type") ;
			if("struct".equals(deft))
			{
				cellXmlDataDef = new XmlDataStruct();
				cellXmlDataDef.fromXmlData(tmpxd);
			}
			else if("val".equals(deft))
			{
				cellXmlDataDef = new XmlValDef();
				cellXmlDataDef.fromXmlData(tmpxd);
			}
		}
	}
}

