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
 * �������һ��ҳ�水ť,����ð�ť,�������ҳ����ָ��id������Ӧ�����ݵ�Excel�ļ���,������
 * 
 * �ñ�ǩҪ��֮���������һ��ҳ��Ԫ��id
 * 
 * �ñ�ǩ��ҳ�����һ������iframe,��������ָ���� /system/util/excel_export.jsp
 * 
 * ͨ�������ؽű�,�Ͱ�ť
 * 
 * ���û������ťʱ,�ű����������id��λҳ��Ԫ��,�����ҵ���Ԫ��html�ı��趨��iframeҳ��
 * ��<textarea></textarea>
 * ��ʹ֮�ύ�� excel_export_do.jsp -- ��ҳ����excel��ʽ���������,����
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
	 * ����Excel�ļ�����
	 * @param excelfn
	 */
	public void setExcel_file(String excelfn)
	{
		
	}
	
	
	/**
	 * ���ģ�����ʼ����
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
