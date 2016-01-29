package com.dw.web_ui.temp;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 存放页面模板信息的类
 * 
 * 页面模板中会定义多个插入点,每个插入点会有自己唯一标识名称--对应WebPageBlock
 * 
 * @author Jason Zhu
 */
public class WebPageTemplate
{
	ArrayList contList = new ArrayList() ;
	int id = -1 ;
	/**
	 * 模板兼容名称--兼容模板要求id一样,里面的数据项也完全一致
	 * 这样可以达到同一个数据可以使用不同的兼容模板
	 */
	String compatibleName = null ;
	String path = null;
	
	ArrayList<WebPageBlock> blocks = null ;
	
	public WebPageTemplate(String path, String temp_cont)
	{
		this(-1,path,temp_cont);
	}
	
	public WebPageTemplate(int id, String temp_cont)
	{
		this(id,null,temp_cont);
	}
	
	public WebPageTemplate(int id,String path, String temp_cont)
	{
		this.id = id ;
		this.path = path;

		if (temp_cont == null || temp_cont.equals(""))
			throw new IllegalArgumentException("temp cont cannot be null!");

		while(temp_cont!=null)
		{
			int sp = temp_cont.indexOf("[#");
			if (sp < 0)
			{
				if(!temp_cont.equals(""))
					contList.add(temp_cont);
				temp_cont = null;
				break ;
			}
			
			int ep = temp_cont.indexOf("#]", sp + 2);
			if (ep < 0)
				throw new IllegalArgumentException(
						"invalid template it must split by string like [#  #]");

			String tmps = temp_cont.substring(0, sp);
			if(!tmps.equals(""))
				contList.add(tmps);
			
			String bn = temp_cont.substring(sp+2,ep).trim();
			WebPageBlock bi = new WebPageBlock(bn);
			//System.out.println("find block==="+bn);
			contList.add(bi);
			
			temp_cont = temp_cont.substring(ep + 2);
		}
	}
	
	public int getId()
	{
		return id ;
	}

	public String getPath()
	{
		return path;
	}

	public ArrayList getContList()
	{
		return contList;
	}
	
	/**
	 * 获得模板内的所有页面块
	 * @return
	 */
	public ArrayList<WebPageBlock> getPageBlocks()
	{
		if(blocks!=null)
			return blocks ;
		
		ArrayList<WebPageBlock> pbs = new ArrayList<WebPageBlock>() ;
		for(Object o : contList)
		{
			if(o instanceof WebPageBlock)
				pbs.add((WebPageBlock)o) ;
		}
		blocks = pbs ;
		return blocks ;
	}
	
	public void writeOut(Writer w,HashMap<String,String> block2val) throws IOException
	{
		for(Object o :contList)
		{
			if(o instanceof String)
			{
				w.write((String)o) ;
				continue ;
			}
			
			if(o instanceof WebPageBlock)
			{
				WebPageBlock wpb = (WebPageBlock)o ;
				String v = block2val.get(wpb.getBlockName()) ;
				if(v!=null)
					w.write(v);
			}
		}
	}
	
	
	public String getContStr(HashMap<String,String> block2val)
	{
		StringBuilder sb = new StringBuilder() ;
		
		for(Object o :contList)
		{
			if(o instanceof String)
			{
				sb.append((String)o) ;
				continue ;
			}
			
			if(o instanceof WebPageBlock)
			{
				WebPageBlock wpb = (WebPageBlock)o ;
				String v = block2val.get(wpb.getBlockName()) ;
				if(v!=null)
					sb.append(v);
				else
				{
					String defv = wpb.getPropVal("default") ;
					if(defv!=null)
						sb.append(defv) ;
				}
			}
		}
		
		return sb.toString() ;
	}
}
