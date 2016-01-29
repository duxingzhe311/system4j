package com.dw.web_ui.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.*;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.temp.WebPageBlock;
import com.dw.web_ui.temp.WebPageTemplate;

/**
 * 用来支持表列表的标签
 * 
 * @author Jason Zhu
 */
public class XORMTableTag extends BodyTagSupport
{
	static class RTItem
	{
		String whereStr = null;

		String orderBy = null;

		ArrayList<String> cols = new ArrayList<String>();
		
		ArrayList<String> col_styles = new ArrayList<String>() ;

		boolean bInLoop = false;

		DataTable selectDT = null;
		
		List selectObjs = null ;
		
		int selectNum = 0 ;

		//给每列做自身计数的变量,它在每行开始时被初始化成0 ;
		int indexCol = -1 ;
		
		int indexRow = -1;
		
		public boolean isFirstCol()
		{
			return indexCol==0;
		}
		
		public boolean isLastCol()
		{
			return indexCol==cols.size()-1 ;
		}
	}

	String xormClass = null;

	int pageSize = 20;

	String dataTableVar = null ;
	String noTableHead = null ;
	String xormObjListVar = null ;
	String rowVar = null;//"xorm_row";
	String rowObjVar = null;
	
	
	String accessDBName = null ;
	String accessDBParam = null ;
	
	boolean bSkin = false;
	String skinTableStyle = "skin_table" ;
	String skinHeadStyle = "skin_head" ;
	String skinRowStyle = "skin_row" ;

	public void setXorm_class(String xormc) throws ClassNotFoundException
	{
		xormClass = xormc;
	}
	
	public void setNo_table_head(String nth)
	{
		noTableHead = nth ;
	}
	
	public void setData_table_var(String dtv)
	{
		dataTableVar = dtv ;
	}
	
	public void setXorm_objlist_var(String xov)
	{
		xormObjListVar = xov ;
	}

	public void setRow_var(String rv)
	{
		rowVar = rv;
		if (rowVar == null || rowVar.equals(""))
			rowVar = "xorm_row";
	}
	
	public String getRowVar()
	{
		return rowVar ;
	}
	
	public void setRow_obj_var(String rov)
	{
		if(Convert.isNullOrTrimEmpty(rov))
			return ;
		
		rowObjVar = rov;
	}
	
	public String getRowObjVar()
	{
		return rowObjVar ;
	}
	
	public void setAccess_db_name(String dbn)
	{
		accessDBName = dbn ;
	}
	
	public void setAccess_db_ht_param(String dbp)
	{
		accessDBParam = dbp;
	}
	
	transient private Class xormC = null ;
	private Class getXormClass() throws ClassNotFoundException
	{
		if(xormC!=null)
			return xormC;
		
		xormC = Class.forName(xormClass, true, Thread.currentThread().getContextClassLoader());//forName(xormClass);
		return xormC;
	}

	void clearRTItem()
	{
		pageContext.removeAttribute("_xorm_table_rtitem");
	}
	
	RTItem getRTItem()
	{
		RTItem rt = (RTItem) this.pageContext
				.getAttribute("_xorm_table_rtitem");
		if (rt != null)
			return rt;

		rt = new RTItem();
		pageContext.setAttribute("_xorm_table_rtitem", rt);
		return rt;
	}

	// public void setWhere_str(String ws)
	// {
	// whereStr = ws ;
	// }
	//	
	// public void setOrder_by(String ob)
	// {
	// orderBy = ob;
	// }
	//	
	// public void setOrder_desc(String odd)
	// {
	// bOrderDesc = "true".equalsIgnoreCase(odd);
	// }

	public void setPage_size(String ps)
	{
		if (ps == null || ps.equals(""))
			return;

		pageSize = Integer.parseInt(ps);
	}

	public void setSkin(String bstr)
	{
		bSkin = "true".equalsIgnoreCase(bstr) ;
	}
	
	public void setSkin_table_style(String skin_ts)
	{
		if(Convert.isNullOrEmpty(skin_ts))
			skinTableStyle = "skin_table" ;
		else
			skinTableStyle = skin_ts ;
	}
	
	public void setSkin_head_style(String skin_heads)
	{
		if(Convert.isNullOrEmpty(skin_heads))
			skinHeadStyle = "skin_head" ;
		else
			skinHeadStyle = skin_heads ;
		
		
	}
	
	public void setSkin_row_style(String skin_rows)
	{
		if(Convert.isNullOrEmpty(skin_rows))
			skinRowStyle = "skin_row" ;
		else
			skinRowStyle = skin_rows ;
	}
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		clearRTItem();
		
		try
		{
			if(Convert.isNotNullEmpty(xormObjListVar))
			{
				Object tmpo = this.pageContext.getAttribute(xormObjListVar) ;
				if(tmpo instanceof GDBPageAccess)
				{
					HttpServletRequest hsr = (HttpServletRequest)pageContext.getRequest() ;
					GDBPageAccess pa = (GDBPageAccess)tmpo;
					pa.setHttpRequest(pageSize, hsr) ;
				}
			}
			
			String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
			JspWriter jw = pageContext.getOut() ;
			
			if(!bSkin)
			{
				jw.write("<script type='text/javascript' src='"+CXTROOT+"WebRes?r=com/dw/web_ui/res/expand.js'></script>");
				jw.write("<script type='text/javascript' src='/system/ui/sorttable.js'></script>");
				jw.write("<style>");
				jw.write(".TableLine1  { BACKGROUND-color: #F3F3F3;}") ;
				jw.write(".TableLine2  { BACKGROUND-color: #FFFFFF;}") ;
				jw.write(".SelectedLine  { BACKGROUND-color: #dddddd;}") ;
				
				jw.write(".pages{margin:1px 0;font:11px/12px Tahoma}") ;
				jw.write(".pages *{vertical-align:middle;}") ;
				jw.write(".pages a{padding:1px 4px 1px;border:1px solid #353535;margin:0 1px 0 0;text-align:center;text-decoration:none;font:normal 12px/14px verdana;}") ;
				jw.write(".pages a:hover{border:#353535 1px solid;background:#666;text-decoration:none;color:#FFF}") ;
				jw.write(".pages input{margin-bottom:0px;border:1px solid #353535;height:15px;font:bold 12px/15px Verdana;padding-bottom:1px;padding-left:1px;margin-right:1px;color:#353535;}") ;
				
				jw.write("</style>");
			}
			
			String rid = UUID.randomUUID().toString() ;
			
			//writePage(jw);
			if(bSkin)
				jw.println("<table id='"+rid+"' class='"+skinTableStyle+"'>");
			else
				jw.println("<table id='"+rid+"' class='sortable' style='width: 100%; font-size: 10pt;margin-left: 0;margin-top: 0' border='0' cellpadding='0' cellspacing='0'>");
			
			RTItem rt = getRTItem();
			if (!rt.bInLoop)
			{
				return BodyTag.EVAL_BODY_INCLUDE;
			}

			// if(rt.selectDT==null||rt.selectDT.getRowNum()<=rt.indexRow)
			// {
			// return BodyTag.SKIP_BODY;
			// }

			return BodyTag.SKIP_BODY;
		}
		catch (Exception ioe)
		{
			ioe.printStackTrace() ;
			throw new JspTagException(ioe.getMessage());
		}
	}

	// public void setBodyContent(BodyContent bodyContent)
	// {
	// bodyContent.
	// System.out.println("setBodyContent...");
	// this.bodyContent = bodyContent;
	// }
	//
	public void doInitBody() throws JspTagException
	{
//		XORMTableTag.RTItem rt = pt.getRTItem() ;
//		if(!rt.bInLoop)
//		{
//			//myIdx = pt.getRTItem().cols.size();
//			pt.getRTItem().cols.add(headTxt);
//			if(headStyle!=null)
//				pt.getRTItem().col_styles.add(headStyle) ;
//			else
//				pt.getRTItem().col_styles.add("") ;
//			
//			return SKIP_BODY ;
//		}
	}

	public int doAfterBody() throws JspTagException
	{
		// 输出还没有输出的模板剩余内容
		RTItem rt = getRTItem();
		try
		{
			JspWriter pw = pageContext.getOut();
			if (!rt.bInLoop)
			{
				// 输出表头
				
				if(!"true".equalsIgnoreCase(noTableHead))
				{
					String CXTROOT = WebRes.getContextRootPath((HttpServletRequest)this.pageContext.getRequest());
					if(bSkin)
						pw.println("<thead>\r\n<tr class='"+skinHeadStyle+"'>");
					else
						pw.println("<tr style='height:0;border-bottom: 1px solid rgb(104, 104, 104);' height='20'>");
					int cc = rt.cols.size() ;
					for(int k = 0 ; k < cc ; k ++)
					{
						String c = "";
						String s = "";
						
						//if(!"true".equalsIgnoreCase(noTableHead))
						{
							c = rt.cols.get(k) ;
							s = rt.col_styles.get(k) ;
						}
						
						if(bSkin)
						{
							if(Convert.isNotNullEmpty(s))
								pw.println("	<td style='"+s+"'>"
										+ c +"</td>");
							else
								pw.println("	<td>"+ c +"</td>");
						}
						else
						{
							if(Convert.isNotNullEmpty(s))
								pw.println("	<td style='"+s+"'>"
											+ c +"</td>");
							else
								pw.println("	<td style='border-bottom: 1px solid rgb(204, 204, 204);background-image: url("+CXTROOT+"WebRes?r=com/dw/web_ui/res/tool-bkgd.jpg);'>"
						
									+ c +"</td>");
						}
						// pw.println(" <td>State</td>
					}
	
					if(bSkin)
						pw.println("</tr></thead>\r\n<tbody>\r\n");
					else
						pw.println("</tr>");
				}

				pw.flush();
				// 获取DataTable
				if(!Convert.isNullOrEmpty(dataTableVar))
				{
					rt.selectDT = (DataTable)this.pageContext.getAttribute(dataTableVar) ;
					rt.selectNum = rt.selectDT.getRowNum() ;
				}
				else if(!Convert.isNullOrEmpty(xormObjListVar))
				{
					Object tmpo = this.pageContext.getAttribute(xormObjListVar) ;
					if(tmpo instanceof GDBPageAccess)
					{
						GDBPageAccess pa = (GDBPageAccess)tmpo;
						rt.selectObjs = pa.getListObjs() ;
						rt.selectNum = 0;
						if(rt.selectObjs!=null)
							rt.selectNum = rt.selectObjs.size() ;
					}
					else if(tmpo instanceof List)
					{
						rt.selectObjs = (List)tmpo;
						rt.selectNum = 0;
						if(rt.selectObjs!=null)
							rt.selectNum = rt.selectObjs.size() ;
					}
				}
				else if(Convert.isNotNullEmpty(accessDBName)&&Convert.isNotNullEmpty(accessDBParam))
				{
					int page = 0 ;
					Integer pp = (Integer)pageContext.getAttribute(accessDBName+"_PAGE") ;
					if(pp!=null)
						page = pp ;
					Hashtable ht = (Hashtable)pageContext.getAttribute(accessDBParam);
					rt.selectObjs = GDB.getInstance().accessDBPageAsXORMObjList(accessDBName, ht, getXormClass(),page, pageSize);
					rt.selectNum = 0;
					if(rt.selectObjs!=null)
						rt.selectNum = rt.selectObjs.size() ;
				}
				else
				{
					rt.selectDT = GDB.getInstance().listXORMAsTable(getXormClass(),
							rt.whereStr, rt.orderBy, 0, pageSize);
					rt.selectNum = rt.selectDT.getRowNum() ;
				}
				
				rt.bInLoop = true;

				if (rt.selectNum <= 0)
				{
					return SKIP_BODY;
				}
			}

			rt.indexRow++;
			
			if (rt.selectNum <= rt.indexRow)
			{
				//wirtePage(pw);
				return SKIP_BODY;
			}
			
			if(rt.selectDT!=null)
			{
				DataTable tmpdt = rt.selectDT ;
				DataRow dr = tmpdt.getRow(rt.indexRow);
	
				if(rowVar!=null)
					pageContext.setAttribute(rowVar, dr);
				if(rowObjVar!=null)
				{
					Class c = getXormClass() ;
					Object o = c.newInstance() ;
					XORMUtil.fillXORMObjByDataRow(dr, o);
					pageContext.setAttribute(rowObjVar, o);
				}
			}
			else
			{//obj list
				Object xormobj = rt.selectObjs.get(rt.indexRow) ;
				if(rowObjVar!=null)
					pageContext.setAttribute(rowObjVar, xormobj) ;
				
				if(rowVar!=null)
				{
					DataRow dr = XORMUtil.extractDataRowFromObj(xormobj);
					pageContext.setAttribute(rowVar, dr);
				}
			}

			//初时化每列计数
			rt.indexCol = 0 ;
			// return BodyTag.EVAL_BODY_INCLUDE ;

			return EVAL_BODY_AGAIN;
		}
		catch (Exception e)
		{
			e.printStackTrace() ;
			throw new JspTagException("IO Error: " + e.getMessage());
		}
	}
	
	
	public Object getCurRowXormObj()
	{
		if(rowObjVar==null)
			return null ;
		
		return pageContext.getAttribute(rowObjVar) ;
	}
	
	private void writePage(JspWriter jw)
		throws IOException
	{
		if(Convert.isNullOrEmpty(xormObjListVar))
			return ;
		
		Object tmpo = this.pageContext.getAttribute(xormObjListVar) ;
		int curp = -1 ;
		if(tmpo instanceof GDBPageAccess)
		{
			GDBPageAccess pa = (GDBPageAccess)tmpo;
			//int total = pa.getListObjTotalNum() ;
			curp = pa.getPageCur() ;
		}
		else if(Convert.isNotNullEmpty(accessDBName)&&Convert.isNotNullEmpty(accessDBParam))
		{
			curp = 0 ;
			Integer pp = (Integer)pageContext.getAttribute(accessDBName+"_PAGE") ;
			if(pp!=null)
				curp = pp ;
		}
		else
		{
			return ;
		}
			
		//int pagen = pa.getPageTotalNum() ;
		jw.println("<div class='pages'><table height='10' style='border: 0;margin: 0' cellpadding='0' cellspacing='0'><tr><td width='99%' align='left'>&nbsp;</td><td nowrap='nowrap'>") ;
		
		HttpServletRequest req = (HttpServletRequest)pageContext.getRequest() ;
		String u = req.getRequestURI() ;
		int p = u.lastIndexOf('/') ;
		if(p>=0)
			u = u.substring(p+1) ;
		String qs = req.getQueryString() ;
		if(qs!=null)
		{
			StringTokenizer st = new StringTokenizer(qs,"&") ;
			StringBuilder sb = new StringBuilder() ;
			while(st.hasMoreTokens())
			{
				String ss = st.nextToken() ;
				if(ss.startsWith(GDBPageAccess.PN_PAGE_CUR))
					continue ;
				
				sb.append(ss).append('&') ;
			}
			qs = sb.toString() ;
		}
		else
		{
			qs = "" ;
		}
		
		jw.print("<span style='font-size: 10pt'>当前页"+(curp+1)+"&nbsp;&nbsp;</span>") ;
		
		if(curp>0)
		{
			jw.println("<a href='"+u+"?"+qs+GDBPageAccess.PN_PAGE_CUR+"="+(curp-1)+"' style='font-weight: bold;'>上一页</a>");
		}
		
		//jw.println("<a href='"+u+"?"+qs+GDBPageAccess.PN_PAGE_CUR+"="+(curp+1)+"' style='font-weight: bold;'>下一页</a>");
		
		RTItem rt = getRTItem();
		if(rt==null||rt.selectNum>=pageSize)
		{
			jw.println("<a href='"+u+"?"+qs+GDBPageAccess.PN_PAGE_CUR+"="+(curp+1)+"' style='font-weight: bold;'>下一页</a>");
		}
		/*
		jw.println("<a href='thread.php?fid-136-search--page-1.html' style='font-weight: bold;'>&lt;&lt;</a>");
		
		jw.println("<b> 2 </b>");
		jw.println("<a href='thread.php?fid-136-search--page-3.html'>3</a>");
		jw.println("<a href='thread.php?fid-136-search--page-4.html'>4</a>");
		jw.println("<a href='thread.php?fid-136-search--page-5.html'>5</a>");
		jw.println("<a href='thread.php?fid-136-search--page-6.html'>6</a>");
		jw.println("<input size='3' onkeydown='javascript: if(event.keyCode==13){ location='thread.php?fid=136&search=&page='+this.value+'';return false;}' type='text'>");
		jw.println("<a href='thread.php?fid-136-search--page-53.html' style='font-weight: bold;'>&gt;&gt;</a>");
		jw.println(" 第 "+(pa.getPageCur()+1)+" 页 共"+pagen+" 页");
		*/
		jw.println("</td></tr></table></div>") ;
	}

	public int doEndTag() throws JspTagException
	{
		try
		{
			JspWriter jw = pageContext.getOut();
			if(bSkin)
				jw.println("</tbody>\r\n</table>");
			else
				jw.println("</table>");
			
			writePage(jw);
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
}
