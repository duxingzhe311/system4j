package com.dw.web_ui.tree;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.web_ui.WebRes;

/**
 * 对树型结构进行输出的支持
 * 其中level表示层次
 * 
 * &lt;wbt:tree&gt;

[option]
target=

[item]
level=1+
title=系统设置
url=5.htm?j=option

[item]
title=网站统计
level=2+
ico=../web/icon/208.gif

[item]
level=3
tree=访问人数统计
url=5.htm?j=count

[item]
level=3
title=点击数统计
url=5.htm?j=count&t1=2

[item]
level=3
title=最近访问IP地址
url=5.htm?j=countip'

[item]
level=3
title=返回进入页
url=5.htm?j=first

[item]
level=1
title=帮助说明
url=5.htm?j=help'

[item]
level=1
title=在线支持
url=5.htm?j=f&f=helponline

[item]
level=1*
title=退出系统
url=javascript:exit()

[item]
level=2
title=退出到首页
url=javascript:top.location="./"

[item]
level=2
title=退出到登陆页面
url=javascript:top.location=String(top.location)
&lt;/wbt:tree&gt;
 * 
 * 
 * 
 * @author Jason Zhu
 *
 */
public class TreeTag extends BodyTagSupport
{
	public static String CXT_ATTRN = "_WEB_PAGE_TREE";
	
	private int height = 20 ;
	
	public void setHeight(String h)
	{
		if(h==null||h.equals(""))
			return ;
		
		height = Integer.parseInt(h);
	}
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		if(!"true".equals(pageContext.getAttribute(CXT_ATTRN)))
		{
			try
			{
				pageContext.getOut().write("<script src=\"/system/ui/main.js\"></script>");
				pageContext.getOut().write("<script src=\"/system/ui/4funtree.js\"></script>");
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
		try
		{
			JspWriter jw = pageContext.getOut() ;
			String jstxt = this.bodyContent.getString() ;
			String t = UUID.randomUUID().toString().replaceAll("-", "");
			String nid = "p"+t ;
			String qid = "q"+t;
			String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			jw.write("<textarea id='"+nid+"' style='display:none'>") ;
			jw.write(jstxt);
			jw.write("</textarea><div id=\""+qid+"\"></div>") ;
			//jstxt = Convert.plainToJsStr(jstxt);
			
			jw.println("<script>f23('tree',document.getElementById('"+nid+"').value,'"+qid+"')</script>");
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}