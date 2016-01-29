package com.dw.web_ui.table;

import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.system.gdb.DataRow;
import com.dw.system.gdb.*;

public class XORMAutoColumnTag extends BodyTagSupport
{

	Class xormClass = null ;
	
	String headStyle = null ;
	
	String xormProp = null ;
	
	HashSet<String> autoCreateColIgnore = new HashSet<String>() ;
	
	/**
	 * �Զ������к����У����Զ�������ʱ��Ӧ�ú��Ե���
	 * @param acc
	 */
	public void setIgnore_columns(String acc)
	{
		autoCreateColIgnore.clear() ;
		if(acc!=null)
		{
			StringTokenizer st = new StringTokenizer(acc,",|") ;
			while(st.hasMoreTokens())
			{
				String c = st.nextToken().trim() ;
				if(Convert.isNullOrEmpty(c))
					continue ;
				autoCreateColIgnore.add(c.toUpperCase());
			}
		}
	}
	
	public void setHead_style(String hs)
	{
		headStyle = hs ;
	}
	
	public void setXorm_prop(String xp)
	{
		xormProp = xp;
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
			if(Convert.isNotNullEmpty(pt.dataTableVar))
			{
				DataTable tmpdt = (DataTable)this.pageContext.getAttribute(pt.dataTableVar) ;
			
				for(DataColumn dc:tmpdt.getColumns())
				{
					String n = dc.getName() ;
					if(this.autoCreateColIgnore.contains(n))
						continue ;
					
					pt.getRTItem().cols.add(dc.getTitle());
					if(headStyle!=null)
						pt.getRTItem().col_styles.add(headStyle) ;
					else
						pt.getRTItem().col_styles.add("") ;
				}
			}
			
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
				
				if(rt.indexRow%2==0)
					pw.print("<tr class='TableLine1' style='"+style+"' onmouseover='mouseover(this)' onmouseout='mouseout(this)'>");
				else
					pw.print("<tr class='TableLine2' style='"+style+"' onmouseover='mouseover(this)' onmouseout='mouseout(this)'>");
				
				
			}
			
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
			DataTable tmpdt = rt.selectDT ;
			DataRow dr = tmpdt.getRow(rt.indexRow);
			
			//����ִ��
			JspWriter pw = pageContext.getOut();
			for(DataColumn dc:tmpdt.getColumns())
			{
				String n = dc.getName() ;
				if(this.autoCreateColIgnore.contains(n))
					continue ;
				
				Object o = dr.getValue(dc.getName()) ;
				pw.write("<td>");
				if(o!=null)
					pw.write(o.toString()) ;
				else
					pw.write("&nbsp;") ;
				
				pw.write("</td>");
			}
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