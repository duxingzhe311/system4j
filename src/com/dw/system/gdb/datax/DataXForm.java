package com.dw.system.gdb.datax;

import java.io.*;
import java.util.*;

import com.dw.mltag.*;
import com.dw.system.xmldata.*;
/**
 * DataX对应类的表单，它可以绑定DataXClass中的XmlDataStruct
 * @author Jason Zhu
 */
public class DataXForm implements IXmlDataable
{
	public enum FormType
	{
		html,
		xml,
		flash,
	}
	
	public enum FormOption
	{
		view,//只能查看
		add, //添加新的DataX数据
		update,//更新DataX数据
	}
	
	private int id = -1 ;
	private FormType formType = FormType.html;
	private String name = null ;
	private String desc = null ;
	
	private String strCont = null ;
	
	private transient XmlNode rootNode = null ;
	
	public DataXForm()
	{
		
	}
	
	public DataXForm(int id,FormType ft,String name,String desc,String strcont)
	{
		this.id = id ;
		formType = ft ;
		this.name = name ;
		this.desc = desc ;
		
		this.strCont = strcont ;
	}
	
	public void init() throws Exception
	{
		if(strCont==null||strCont.equals(""))
			return ;
		
		StringReader sr = new StringReader(strCont);
		NodeParser np = new NodeParser(sr);
		np.parse();
		
		rootNode = np.getRoot() ;
	}
	
	public int getId()
	{
		return id ;
	}
	
	public FormType getFormType()
	{
		return formType ;
	}
	
	public String getName()
	{
		return name ;
	}
	
	public String getDesc()
	{
		return desc ;
	}
	
	public String getStrCont()
	{
		return strCont ;
	}
	
	void setStrCont(String strc)
	{
		strCont = strc ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		
		xd.setParamValue("id", id);
		xd.setParamValue("type", formType.toString());
		xd.setParamValue("name", name);
		if(desc!=null)
			xd.setParamValue("desc",desc) ;
		if(strCont!=null)
			xd.setParamValue("cont", strCont);
		
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		id = xd.getParamValueInt32("id", -1);
		String stype = xd.getParamValueStr("type");
		if(stype!=null&&!stype.equals(""))
			formType = FormType.valueOf(stype);
		else
			formType = FormType.html;
		
		name = xd.getParamValueStr("name");
		desc = xd.getParamValueStr("desc") ;
		strCont = xd.getParamValueStr("cont");
	}
	
}
