package com.dw.web_ui.skin;

import java.io.File;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.w3c.dom.Element;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.DataRow;
import com.dw.system.gdb.DataTable;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GDBPageAccess;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.web_ui.WebRes;


public class SkinTag extends BodyTagSupport
{
	/**
	 * skin配置内容是否有效
	 */
	static boolean bSkinOk = false;
	
	static String eleHead = null ;
	static String eleTail = null ;
	static String eleCss = null ;
	
	static String headTxt = null ;
	static String tailTxt = null ;
	static String cssTxt = null ;
	
	static
	{
		Element ele = AppConfig.getConfElement("skin") ;
		if(ele!=null)
		{
			eleHead = ele.getAttribute("head") ;
			eleTail = ele.getAttribute("tail") ;
			eleCss =  ele.getAttribute("css") ;
			String enc = ele.getAttribute("encoding") ;
			
			if(eleHead!=null&&!(eleHead=eleHead.trim()).equals(""))
			{
				if(eleHead.toLowerCase().endsWith(".js"))
				{
					headTxt = "<script type=\'text/javascript\' src=\'"+eleHead+"\'></script>" ;
				}
				else if(eleHead.toLowerCase().endsWith(".htm")||eleHead.toLowerCase().endsWith(".html"))
				{
					File tmpf = new File(AppConfig.getTomatoWebappBase()+eleHead) ;
					headTxt = readFileTxt(tmpf,enc) ;
				}
			}
			
			if(eleTail!=null&&!(eleTail=eleTail.trim()).equals(""))
			{
				if(eleTail.toLowerCase().endsWith(".js"))
				{
					tailTxt = "<script type=\'text/javascript\' src=\'"+eleTail+"\'></script>" ;
				}
				else if(eleTail.toLowerCase().endsWith(".htm")||eleTail.toLowerCase().endsWith(".html"))
				{
					File tmpf = new File(AppConfig.getTomatoWebappBase()+eleTail) ;
					tailTxt = readFileTxt(tmpf,enc) ;
				}
			}
			
			if(eleCss!=null&&!(eleCss=eleCss.trim()).equals(""))
			{
				cssTxt = "<link rel=\"stylesheet\" type=\"text/css\" href=\""+eleCss+"\" />" ;
			}
		}
	}
	
	
	static String readFileTxt(File f,String enc)
	{
		if(!f.exists())
			return null ;
		Long flen = f.length();
		byte[] fcont = new byte[flen.intValue()];
		
		FileInputStream in = null ;
		try
		{
			in = new FileInputStream(f);
			in.read(fcont);
			if(enc!=null&&!enc.equals(""))
				return new String(fcont) ;
			else
				return new String(fcont, enc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null ;
		}
		finally
		{
			if(in!=null)
			{
				try
				{
					in.close() ;
				}
				catch(IOException ioe){}
			}
		}
	}
	
	boolean bHasFrame = true ;
	
	
	
	public void setFrame(String t)
	{
		bHasFrame = !"false".equalsIgnoreCase(t) ;
		//if(Convert.isNullOrEmpty(type))
		//	type = "url" ;
	}
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		if(headTxt==null&&cssTxt==null)
			return BodyTag.EVAL_BODY_INCLUDE;
		
		try
		{
			JspWriter jw = pageContext.getOut() ;
			if(headTxt!=null && bHasFrame)
				jw.write(headTxt);
			if(cssTxt!=null)
				jw.write(cssTxt);
			return BodyTag.EVAL_BODY_INCLUDE;
			
		}
		catch (Exception ioe)
		{
			ioe.printStackTrace() ;
			throw new JspTagException(ioe.getMessage());
		}
	}

	

	public int doEndTag() throws JspTagException
	{
		if(tailTxt==null || !bHasFrame)
			return BodyTag.EVAL_PAGE;
			
		try
		{
			JspWriter jw = pageContext.getOut();
			jw.println(tailTxt);
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}
