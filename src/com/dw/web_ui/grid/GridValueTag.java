package com.dw.web_ui.grid;

import java.util.*;
import java.io.*;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import com.dw.system.Convert;
import com.dw.web_ui.WebUtil;
import com.dw.web_ui.table.XORMEachRowStyleTag;
import com.dw.web_ui.table.XORMTableTag;

/**
 * ����ֵ��ǩ
 * 
 * �����ض��ĸ���������,�����������ֵ�ı�ǩ
 * ����������ҳ��,�������е�Լ��,ͨ��ajax��ʽ���ʷ�������Ӧ��url
 * 
 * �����ؽ�������ҳ����
 * 
 * �ñ�ǩ����������ҳ������Ĺؼ�
 * 
 * ������url,url���Ը����ض������ƴ�϶���
 * 
 * @author Jason Zhu
 */
public class GridValueTag extends BodyTagSupport
{
	String type = null ;
	
	
	
	public void setType(String t)
	{
		type = t ;
		//if(Convert.isNullOrEmpty(type))
		//	type = "url" ;
	}
	
	protected GridContainerTag getMyContainer()
	{
		Tag t = this ;
		
		while((t=t.getParent())!=null)
		{
			if(t instanceof GridContainerTag)
			{
				return (GridContainerTag)t ;
			}
		}
		
		return null;
	}
	
	/**
	 * ���ģ�����ʼ����
	 */
	public int doStartTag() throws JspTagException
	{
		//AjaxLazyShower(buffer_len)
		return BodyTag.EVAL_BODY_BUFFERED;
	}
	
	public static class ValueItem
	{
		int row = -1 ;
		int col = -1 ;
		
		String valStr= null ;
		String valType = null ;
		
		
		public int getRow()
		{
			return row ;
		}
		
		public int getCol()
		{
			return col ;
		}
		
		public String getValStr()
		{
			return valStr ;
		}
		
		public String getValType()
		{
			return valType ;
		}
	}
	
	public static ValueItem parseStr(String dtnv)
	{// dt_0_2:double==u=5.0
		//
		ValueItem vi = new ValueItem() ;
		int d = dtnv.indexOf('=') ;
		String dtn = dtnv.substring(0,d) ;
		int p = dtn.indexOf(':') ;
		if(p<0)
		{
			vi.valType = "string" ;
			dtn = dtn.substring(3) ;
		}
		else
		{
			vi.valType = dtn.substring(p+1) ;
			dtn = dtn.substring(3,p) ;
		}
		int k = dtn.indexOf('_');
		if(k<=0)
			return null ;
		vi.row = Integer.parseInt(dtn.substring(0,k)) ;
		vi.col = Integer.parseInt(dtn.substring(k+1)) ;
		vi.valStr = WebUtil.decodeHexUrl(dtnv.substring(d+1)) ;
		return vi ;
	}
	
	private ValueItem extractValueItem(String inputs)
	{
		if(inputs==null)
			return null ;
		
		inputs = inputs.trim() ;
		ValueItem vi = new ValueItem() ;
		
		
		if(!inputs.startsWith("("))
		{
			vi.valStr = inputs ;
			return vi ;
		}
		
		
		int p = inputs.indexOf(')') ;
		if(p<=0)
			throw new IllegalArgumentException("GridValue Tag Content with data table value must like (x,y)=url") ;
		
		String tmps = inputs.substring(1,p) ;
		
		int k = inputs.indexOf('=',p);
		if(k<=0)
			throw new IllegalArgumentException("GridValue Tag Content with data table value must like (x,y):int=url") ;
		
		//(2,6):double=bizstaff_week_timesheet_ajax.jsp?cid=258&u=j
		//(0,1)= Firework in 1 x 40' container No.: INBU4910224[Survey-C
		int q = inputs.indexOf(':',p+1) ;
		if(q>=0&&q<k)
		{
			vi.valType = inputs.substring(q+1,k).trim() ;
			//inputs = inputs.substring(p+1).trim() ;
		}
		
		inputs = inputs.substring(k+1).trim() ;
		
		vi.valStr = inputs ;
		p = tmps.indexOf(',') ;
		if(p<=0)
			throw new IllegalArgumentException("GridValue Tag Content with data table value must like (x,y)=url") ;
		
		vi.row = Integer.parseInt(tmps.substring(0,p).trim()) ;
		vi.col = Integer.parseInt(tmps.substring(p+1).trim()) ;
		return vi ;
	}

	public int doEndTag() throws JspTagException
	{
		GridContainerTag sct = getMyContainer() ;
		String dt_var = "" ;
		if(sct!=null)
		{
			dt_var = sct.getId();//.getInnerDataTableVarName() ;
			if(Convert.isNullOrEmpty(dt_var))
				dt_var = sct.getId() ;
		}
		
		
		
		JspWriter jw = pageContext.getOut() ;
		
		try
		{
			String tmpid=dt_var+"_"+UUID.randomUUID().toString().replace("-", "") ;
			if("ajax".equals(type)||"url".equalsIgnoreCase(type))
			{
				String tmps = this.bodyContent.getString() ;
				if(tmps==null)
					throw new JspTagException("no url info found in grid value!");
				
				tmps = tmps.trim() ;
				if(tmps.equals(""))
					throw new JspTagException("no url info found in grid value!");
				//�ж�url���Ƿ����post����
				BufferedReader br = new BufferedReader(new StringReader(tmps)) ;
				String line = null ;
				String url = null ;
				String postv = null ;
				while((line=br.readLine())!=null)
				{
					line = line.trim() ;
					if("".equals(line))
						continue ;
					if(url==null)
						url = line ;
					
					int p = line.indexOf('=') ;
					if(p>0&&"post".equalsIgnoreCase(line.substring(0,p).trim()))
					{
						postv = line.substring(p+1).trim() ;
						if(postv.toLowerCase().startsWith("js_func:"))
						{
							postv = postv.substring("js_func:".length()) ;
						//else if(postv.toLowerCase().startsWith("script"))
						//	postv = postv.substring("js_func:".length()) ;
							if(postv.indexOf("(")>0)
							{//script
								postv = "\"js:"+Convert.plainToJsStr(postv)+"\"" ;
							}
						}
						else
							postv = "'"+postv+"'" ;
						break ;
					}
				}
				
				
				//
				//���ajax����
				ValueItem vi = extractValueItem(url) ;
				if(vi==null)
					throw new JspTagException("no url info found in grid value!");
				
				
				jw.write("<span id='"+tmpid+"'");
				if(vi.row>=0&&vi.col>=0)
				{
					jw.write(" dt_row='"+vi.row+"'") ;
					jw.write(" dt_col='"+vi.col+"'") ;
					if(vi.valType!=null)
						jw.write(" dt_valtype='"+vi.valType+"'") ;
				}
				jw.write("></span>\r\n<script>\r\n");
				jw.write(sct.getAjaxLazyShowerVar()+".addLazyShowItem('"+tmpid+"','"+vi.valStr+"',"+postv+");");
				//pageContext.getOut().write("<script src=\"/system/ui/toolbar.js\"></script>");
				jw.write("</script>\r\n");
			}
			else
			{//�ѱ�ǩ�е�����ֱ����Ϊ�������
				String inputs = this.bodyContent.getString() ;
				ValueItem vi = extractValueItem(inputs) ;
				if(vi==null)
					throw new JspTagException("no url info found in grid value!");
				
				jw.write("<span id='"+tmpid+"'");
				if(vi.row>=0&&vi.col>=0)
				{
					jw.write(" dt_row='"+vi.row+"'") ;
					jw.write(" dt_col='"+vi.col+"'") ;
					if(vi.valType!=null)
						jw.write(" dt_valtype='"+vi.valType+"'") ;
				}
				jw.write(">");
				jw.write(vi.valStr) ;
				jw.write("</span>") ;
			}
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe);
		}
		
		return EVAL_PAGE;
	}
}
