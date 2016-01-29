package com.dw.comp.view;

import java.util.ArrayList;
import java.util.List;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.*;

/**
 * 专门为View提供显示控制单元的元素
 * 
 * 它不能独立运行,必须包含在View中运行
 * 
 * 它主要功能是配合View的要求进行界面元素的特定输入,展示,及输出
 * 
 * 它还可以根据不同的控制状态做不同的显示,及不同的输出
 * 
 * 另外,由于一个BizView可以同时包含多个相同的Cell,并通过不同的name进行区分
 * BizViewCell要保证Cell之间不会造成冲突
 * 
 * Cell有自己的一个值,它可以是基本类型,也可以是XmlData类型,也可以没有.
 * 当Cell被View包含并指定了名称之后,就可以被作为输出值提供
 * 
 * @author Jason Zhu
 */
public class ViewCell
{
	public static enum CtrlType
	{
		ignore,//cell不做输入输出
		hidden,//cell 隐藏,但有值输入和输出
		read, //cell显示,有输入和输出--界面上不能修改其值
		write, //cell显示,有输入输出
		write_need,//cell显示,有输入输出,并且必填,也就是输出不能为null
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

