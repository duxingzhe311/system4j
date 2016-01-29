package com.dw.web_ui.dict;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.dw.system.Convert;
import com.dw.system.dict.*;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.temp.WebPageTemplate;

/**
 * 根据输入的参数中,指定的字典和相关的参数,输出字典选择select控件
 * 它支持字典的一级 ,二级选择
 * 
 * 选定的值以最终的字典值选择
 * 
 * 目前该标签只适合小字典的选择,对应大字典的选择需要采用ajax搜索技术实现
 * 
 * @author Jason Zhu
 */
public class DictSelectTag extends BodyTagSupport
{
	private static WebPageTemplate templ = null ;
	
	private static WebPageTemplate getTemplate() throws IOException
	{
		if(templ!=null)
		{
			return templ;
		}
		
		templ = new WebPageTemplate(0,WebRes.getResTxtContent("com/dw/web_ui/dict/HtmlDictSelect.htm")) ;
		return templ ;
	}
	
	String id = null;
	String name = null;
	//int val = -1 ;
	String dd_name = null ;
	String lan = null ;
	int selectLvl = 1 ;
	String onChange = "" ;
	boolean bLocalModule = false;
	/**
	 * 判断是否要支持选择父节点,也就是非叶子节点的选取
	 */
	boolean selectParent = false ;
	
	public void setId(String id)
	{
		this.id = id;
	}
	
	public void setName(String n)
	{
		this.name = n ;
	}
	
//	public void setVal(String v)
//	{
//		if(Convert.isNotNullEmpty(v))
//			this.val = Integer.parseInt(v) ;
//	}
	
	public void setDd_name(String ddn)
	{
		dd_name = ddn ;
	}
	
	public void setDd_local_module(String dlm)
	{
		bLocalModule = "true".equalsIgnoreCase(dlm) ;
	}
	
	public void setLan(String lan)
	{
		this.lan = lan ;
	}
	
	public void setSelect_lvl(String selv)
	{
		if(Convert.isNotNullEmpty(selv))
			selectLvl = Integer.parseInt(selv);
	}
	
	public void setSelect_parent(String sp)
	{
		selectParent = "true".equalsIgnoreCase(sp);
	}
	
	public void setOn_change(String oc)
	{
		if(oc!=null)
			onChange = oc ;
	}
	
	/**
	 * 
	 */
	public int doStartTag() throws JspTagException
	{
		
		return BodyTag.EVAL_BODY_BUFFERED;
	}
	
	public int doEndTag() throws JspTagException
	{
		try
		{
			int val = -1;
			String tmps = null;
			if(bodyContent!=null)
				tmps = bodyContent.getString();
			
			if(tmps!=null)
			{
				val = Convert.parseToInt32(tmps.trim(), -1) ;
			}
				
			JspWriter jw = pageContext.getOut() ;
			DataClass dc = null;
			if(bLocalModule)
			{
				HttpServletRequest req = (HttpServletRequest)pageContext.getRequest() ;
				dc = DictManager.getInstance().getModuleDataClass(req, dd_name) ;
			}
			else
			{
				dc = DictManager.getInstance().getDataClass(dd_name) ;
			}
			if(dc==null)
			{
				throw new JspTagException("cannot find DataClass with name="+dd_name);
			}
			
			if(selectLvl==1)
			{
				jw.write("<select name='"+name+"' id='"+id+"' onChange='"+onChange+"'>\r\n") ;
				for(DataNode dn:dc.getValidRootNodes())
				{
					jw.write("<option value='"+dn.getId()+"'");
					if(dn.getId()==val)
						jw.write(" selected='selected'");
					jw.write(">");
					jw.write(dn.getNameByLang(lan));
					jw.write("</option>\r\n") ;
				}
				jw.write("</select>\r\n") ;
			}
			else
			{//lvl=2
				StringBuilder lvl1_options = new StringBuilder() ;
				StringBuilder lvl2ids = new StringBuilder();
				StringBuilder lvl2names = new StringBuilder();
				
				int lvl1_val = -1 ;
				int lvl2_val = -1 ;
				if(val>0)
				{
					DataNode lvl2dn = dc.findDataNodeById(val) ;
					DataNode pdn = null ;
					if(lvl2dn!=null)
					{
						pdn = lvl2dn.getParentNode() ;
						if(pdn==null)
							pdn = lvl2dn ;
						lvl2_val = val ;
						lvl1_val = pdn.getId() ;
					}
				}
				//如果没有对应的值,则输入第一级第一个,及对应的第二级第一个
				DataNode sel_lvl1_node = null ;
				
				for(DataNode dn:dc.getValidRootNodes())
				{
					lvl1_options.append("<option value='")
						.append(dn.getId()).append("'");
					
					if(lvl1_val==dn.getId())
					{
						lvl1_options.append(" selected='selected'");
						sel_lvl1_node = dn ;
					}
					
					lvl1_options.append(">")
						.append(dn.getNameByLang(lan))
						.append("</option>") ;
					
					DataNode[] cdns = dn.getChildNodes() ;
					
					
					lvl2ids.append("case ").append(dn.getId())
						.append(":return new Array(") ;
					
					lvl2names.append("case ").append(dn.getId())
						.append(":return new Array(") ;

					if(selectParent)
					{
						lvl2ids.append('\'').append(dn.getId()).append('\'');
						lvl2names.append('\'').append("---").append('\'');
						if(cdns!=null&&cdns.length>0)
						{
							lvl2ids.append(',') ;
							lvl2names.append(',') ;
						}
					}

					if(cdns!=null&&cdns.length>0)
					{
						lvl2ids.append('\'').append(cdns[0].getId()).append('\'');
						lvl2names.append('\'').append(cdns[0].getNameByLang(lan)).append('\'');
						
						for(int k = 1 ; k < cdns.length ; k ++)
						{
							lvl2ids.append(",\'").append(cdns[k].getId()).append('\'') ;
							lvl2names.append(",\'").append(cdns[k].getNameByLang(lan)).append('\'');
						}
					}
					
				    lvl2ids.append(");\r\n") ;
				    lvl2names.append(");\r\n") ;
				}
				
				StringBuilder lvl2_options = new StringBuilder() ; 
				DataNode[] rdns = dc.getValidRootNodes() ;
				if(sel_lvl1_node==null&&(rdns!=null&&rdns.length>0))
				{
					sel_lvl1_node = dc.getValidRootNodes()[0];
				}
				
				if(sel_lvl1_node!=null)
				{
					if(selectParent)
					{
						lvl2_options.append("<option value='")
						.append(sel_lvl1_node.getId()).append("'");
					
						if(lvl2_val==sel_lvl1_node.getId())
						{
							lvl2_options.append(" selected='selected'");
						}
						
						lvl2_options.append(">")
							//.append(sel_lvl1_node.getNameByLang(lan))
							.append("---")
							.append("</option>") ;
					}
					
					for(DataNode tmpdn:sel_lvl1_node.getChildNodes())
					{
						lvl2_options.append("<option value='")
						.append(tmpdn.getId()).append("'");
					
						if(lvl2_val==tmpdn.getId())
						{
							lvl2_options.append(" selected='selected'");
						}
						
						lvl2_options.append(">")
							.append(tmpdn.getNameByLang(lan))
							.append("</option>") ;
					}
				}
				
				HashMap<String,String> block2val = new HashMap<String,String>() ;
				block2val.put("lvl1_options",lvl1_options.toString());
				block2val.put("lvl2_options",lvl2_options.toString());
				
				block2val.put("id", id) ;
				block2val.put("name", name) ;
				block2val.put("on_change", onChange) ;
				block2val.put("get_ids_by_lvl1_id", lvl2ids.toString()) ;
				block2val.put("get_names_by_lvl1_id", lvl2names.toString()) ;
				
				
				getTemplate().writeOut(jw, block2val) ;
			}
			return BodyTag.EVAL_PAGE;
		}
		catch (IOException ioe)
		{
			throw new JspTagException(ioe.getMessage());
		}
	}
	
	
}