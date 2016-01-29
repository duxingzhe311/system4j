package com.dw.web_ui.export;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.*;

import com.dw.system.Convert;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.WebUtil;
import com.dw.web_ui.grid.GridContainerTag;
import com.dw.web_ui.grid.GridValueTag.ValueItem;
import com.dw.web_ui.temp.WebPageTemplate;

/**
 * 用来输出一个页面按钮,点击该按钮,可以输出页面中指定id的所对应的内容到Excel文件中,并下载
 * 
 * 该标签要求之间的内容是一个页面元素id
 * 
 * 该标签在页面输出一个隐藏iframe,并且里面指定了 /system/util/excel_export.jsp
 * 
 * 通过输出相关脚本,和按钮
 * 
 * 当用户点击按钮时,脚本根据输入的id定位页面元素,并把找到的元素html文本设定给iframe页面
 * 的<textarea></textarea>
 * 并使之提交到 excel_export_do.jsp -- 该页面做excel格式的内容输出,即可
 * 
 * @author Jason Zhu
 *
 */
public class ExcelExportButtonTag extends BodyTagSupport
{
	private static WebPageTemplate templ = null ;
	
	private static WebPageTemplate getTemplate() throws IOException
	{
		if(templ!=null)
		{
			return templ;
		}
		
		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/export/HtmlExcelExport.htm")) ;
		return templ ;
	}
	
	/**
	 * 设置Excel文件名称
	 * @param excelfn
	 */
	public void setExcel_file(String excelfn)
	{
		
	}
	
	
	/**
	 * 输出模板的起始内容
	 */
	public int doStartTag() throws JspTagException
	{
		//AjaxLazyShower(buffer_len)
		return BodyTag.EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspTagException
	{
		String exp_info = null ;
		if(bodyContent!=null)
			exp_info = bodyContent.getString() ;
		if(exp_info==null)
			return EVAL_PAGE;
		
		//filename=aa page_ele_id= btn_title
		//
		String uid = UUID.randomUUID().toString().replace("-", "") ;
		HashMap<String,String> ps = Convert.transPropStrToMap(exp_info) ;
		
		String eleid = ps.get("id") ;
		if(Convert.isNullOrEmpty(eleid))
			eleid = ps.get("ele_id") ;
		if(Convert.isNullOrEmpty(eleid))
			eleid = ps.get("page_ele_id") ;
		
		if(Convert.isNullOrEmpty(eleid))
			throw new JspTagException("no page element id found in tag");
		
		String file = ps.get("filename") ;
		if(Convert.isNullOrEmpty(file))
			file = "Excel.xls" ;
		if(!file.toLowerCase().endsWith(".xls"))
			file += ".xls" ;
		
		String btntitle = ps.get("btn_title") ;
		if(Convert.isNullOrEmpty(btntitle))
			btntitle = "Export Excel" ;
		
		String cn = ps.get("class") ;
		if(Convert.isNullOrEmpty(cn))
			cn = "button_dc" ;
		
		JspWriter jw = pageContext.getOut() ;

		try
		{
			HashMap<String,String> block2val = new HashMap<String,String>() ;
			block2val.put("uid",uid);
			block2val.put("btntitle",btntitle);
			block2val.put("eleid", eleid) ;
			block2val.put("filename", file) ;
			block2val.put("btn_cn", cn) ;
			
			getTemplate().writeOut(jw, block2val) ;
		}
		catch(IOException ioe)
		{
			throw new JspTagException(ioe);
		}
		
		return EVAL_PAGE;
	}
}
