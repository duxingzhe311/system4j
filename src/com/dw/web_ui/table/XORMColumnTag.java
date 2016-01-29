package com.dw.web_ui.table;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class XORMColumnTag extends BodyTagSupport
{

	Class xormClass = null ;
	
	String headTxt = null ;
	
	String headStyle = null ;
	
	String cellStyle = "" ;
	
	String xormProp = null ;
	
	
	public void setHead_txt(String ht)
	{
		headTxt = ht;
	}
	
	public void setHead_style(String hs)
	{
		headStyle = hs ;
	}
	
	public void setXorm_prop(String xp)
	{
		xormProp = xp;
	}
	
	public void setCell_style(String rs)
	{
		cellStyle = rs ;
		if(cellStyle==null)
			cellStyle = "" ;
	}
	
	public String getHeadTxt()
	{
		return headTxt ;
	}
	
	public String getXormProp()
	{
		return xormProp ;
	}

//	/**
//	 * �ж������Ƿ��ǵ�һ����
//	 * @return
//	 */
//	private boolean isFirstColumn()
//	{
//		return myIdx==0 ;
//	}
//	
//	private boolean isLastColumn()
//	{
//		XORMTableTag pt = (XORMTableTag)this.getParent();
//		return myIdx == (pt.getRTItem().cols.size()-1) ;
//	}
	/**
	 * ���ģ�����ʼ����
	 */
	public int doStartTag() throws JspTagException
	{
		XORMTableTag pt = (XORMTableTag)this.getParent();
		XORMTableTag.RTItem rt = pt.getRTItem() ;
		if(!rt.bInLoop)
		{
			//myIdx = pt.getRTItem().cols.size();
			pt.getRTItem().cols.add(headTxt);
			if(headStyle!=null)
				pt.getRTItem().col_styles.add(headStyle) ;
			else
				pt.getRTItem().col_styles.add("") ;
			
			return SKIP_BODY ;
		}
		
		try
		{
//			����ִ��
			JspWriter pw = pageContext.getOut() ;
			if(rt.isFirstCol())
			{//���ÿ��tr
				String style = (String)pageContext.getAttribute(XORMEachRowStyleTag.ATTRN);
				if(style==null)
					style="";
				
				
				HashMap<String,String> rowps = (HashMap<String,String>)pageContext.getAttribute(XORMEachRowPropsTag.ATTRN);
				
				if(pt.bSkin)
				{
					//pw.print("<tr class='"+pt.skinRowStyle+"'");
					pw.print("<tr style='"+style+"'");
				}
				else
				{
					if(rt.indexRow%2==0)
						pw.print("<tr class='TableLine1' style='"+style+"' onmouseover='mouseover(this)' onmouseout='mouseout(this)'");
					else
						pw.print("<tr class='TableLine2' style='"+style+"' onmouseover='mouseover(this)' onmouseout='mouseout(this)'");
				}
				
				if(rowps!=null)
				{
					for(Map.Entry<String, String> n2v:rowps.entrySet())
					{
						pw.print(" "+n2v.getKey());
						pw.print("=\""+n2v.getValue()+"\"") ;
					}
				}
				
				pw.print(">");
			}
			
			if(pt.bSkin)
				pw.write("<td style='"+cellStyle+"'>");
			else
				pw.write("<td style='"+cellStyle+"'>");
			
			
			return BodyTag.EVAL_BODY_INCLUDE;
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}


	public int doEndTag() throws JspTagException
	{
		// �����û�������ģ��ʣ������
		//XORMTableTag pt = (XORMTableTag)this.getParent();
		XORMTableTag pt = (XORMTableTag)this.getParent();
		XORMTableTag.RTItem rt = pt.getRTItem() ;
		if(!rt.bInLoop)
		{
			return EVAL_PAGE;
		}
		
		try
		{
			//����ִ��
			JspWriter pw = pageContext.getOut();
			pw.write("</td>");
			
			//���һ�������,���������
			if(rt.isLastCol())
			{
				pw.print("</tr>");
			}
			
//			������һ�м���
			rt.indexCol ++ ;
			return EVAL_PAGE;
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
		
	}
}