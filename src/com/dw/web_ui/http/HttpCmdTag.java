package com.dw.web_ui.http;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;


public class HttpCmdTag extends BodyTagSupport
{
	static String PLUG_OBJ = "<object ID='UCFileTransfer' CLASSID='CLSID:16E8B71E-3F9D-4D47-8D39-5946E7313450' CODEBASE='/_webbricks_core/js/FileTransSupporter.CAB#version=1,1,0,163' width='100%' height='20' style='display:none'><PARAM NAME='LPKPath' VALUE='sft.lpk'></PARAM></object>" ;
	
	public static String CXT_ATTRN = "_WB_HTTP_CMD_";
	
	private String plugObjId = "UCFileTransfer";
	private String plugCodeBase = "/system/client/FileTransSupporter.CAB" ;
	private String plugVer = "1,1,0,163";
	private int width = 300 ;
	private int height = 300 ;
	
	public void setPlug_obj_id(String objid)
	{
		if(Convert.isNullOrEmpty(objid))
			return ;
		
		plugObjId = objid ;
	}
	
	public void setPlug_code_base(String plugcb)
	{
		if(Convert.isNullOrEmpty(plugcb))
			return ;
		
		this.plugCodeBase = plugcb ;
	}
	
	public void setPlug_ver(String plugv)
	{
		plugVer = plugv ;
	}

	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(CXT_ATTRN)))
		{
			try
			{
				JspWriter jw = pageContext.getOut() ;
				jw.write("<object ID='");
				jw.write(plugObjId);
				jw.write("' CLASSID='CLSID:16E8B71E-3F9D-4D47-8D39-5946E7313450' CODEBASE='");
				jw.write(plugCodeBase);
				jw.write("#version=") ;
				jw.write(plugVer);
				jw.write("' width='100%' height='20' style='display:none'><PARAM NAME='LPKPath' VALUE='sft.lpk'></PARAM></object>");
				
				jw.write(plugObjId);
				jw.write(".Cmd_TargetUrl = 'http://'+location.host+'';") ;
			    
				jw.write(plugObjId);
				jw.write(".Cmd_Cookie = document.cookie;") ;
				//pageContext.getOut().write("PLUG_OBJ");
				//pageContext.getOut().write("<script src=\"/system/ui/toolbar.js\"></script>");
				pageContext.setAttribute(CXT_ATTRN, "true");
			}
			catch(IOException ioe)
			{
				throw new JspTagException(ioe);
			}
		}
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspTagException
	{
		//System.out.println("do end Tag...");
		try
		{
			if (bodyContent != null)
			{
				String tmps = bodyContent.getString();
				
				if(tmps!=null&&!(tmps=tmps.trim()).equals(""))
				{
					pageContext.getOut().write("");
				}
				
			}
		}
		catch (java.io.IOException e)
		{
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return EVAL_PAGE;
	}
}