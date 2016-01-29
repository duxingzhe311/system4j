package com.dw.web_ui.http;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

public class HttpCmdHelper
{
	//private String plugObjId = "UCFileTransfer";
	private static String plugCodeBase = "/system/client/FileTransSupporter.CAB" ;
	private static String plugVer = "1,1,0,163";
	
	public static String CXT_ATTRN = "_WB_HTTP_CMD_";
	
	public static void preparePageClientOCX(String objid,PageContext pc) throws IOException
	{
		if(!"true".equals(pc.getAttribute(CXT_ATTRN)))
		{
			JspWriter jw = pc.getOut() ;
			jw.write("<object ID='");
			jw.write(objid);
			jw.write("' CLASSID='CLSID:16E8B71E-3F9D-4D47-8D39-5946E7313450' CODEBASE='");
			jw.write(plugCodeBase);
			jw.write("#version=") ;
			jw.write(plugVer);
			jw.write("' width='100%' height='20' style='display:none'><PARAM NAME='LPKPath' VALUE='sft.lpk'></PARAM></object>\r\n");
			jw.write("<script>\r\n");
			jw.write("function check_has_ocx(o)\r\n") ;
			jw.write("{\r\n") ;
			jw.write("try{o.Cmd_Cookie = document.cookie;\r\nreturn true;}catch(e){return false;}") ;
			jw.write("}\r\nvar has_ocx=document.getElementById('"+objid+"')!=null;\r\n") ;
			jw.write("if(has_ocx)") ;
			jw.write("\r\n{");
			//jw.write("has_ocx=check_has_ocx("+objid+");\r\n");
			
			jw.write(objid);
			jw.write(".Cmd_TargetUrl = 'http://'+location.host+'/system/WebCmd';\r\n") ;
			//jw.write("alert('http://'+location.host+'/system/WebCmd');\r\n") ;
			jw.write(objid);
			jw.write(".Cmd_Cookie = document.cookie;\r\n") ;
			jw.write("\r\n}");
			jw.write("</script>\r\n") ;
			jw.write("<script type='text/javascript' src='/system/ui/fts.js'></script>\r\n") ;
			//pageContext.getOut().write("PLUG_OBJ");
			//pageContext.getOut().write("<script src=\"/system/ui/toolbar.js\"></script>");
			pc.setAttribute(CXT_ATTRN, "true");
		}
	}
}
