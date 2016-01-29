package com.dw.mltag.util;

import java.io.StringReader;

import com.dw.mltag.AbstractNode;
import com.dw.mltag.NodeParser;
import com.dw.mltag.XmlNode;
import com.dw.mltag.XmlText;

public class HtmlPlainUtil
{
	/**
	 * 把一个html格式的内容，转换成对应的文本
	 * 其中要对p,br,table tr td 等内容进行处理
	 * @param htmlstr
	 * @return
	 */
	public static String convertHtmlToPlain(String htmlstr)
	{
		if(htmlstr==null)
			return null ;
		
		if("".equals(htmlstr.trim()))
			return htmlstr ;
		
		StringReader sr = new StringReader(htmlstr);
		NodeParser parser = null;
		
		try
		{
			parser = new NodeParser(sr);
			// parser.setParserFilter(filter) ;
			parser.setIgnoreCase(true);
	
			parser.parse();
		}
		catch(Exception ee)
		{
			throw new RuntimeException("html parse error!") ;
		}

		XmlNode tmpxn = parser.getRoot();
		StringBuilder sb = new StringBuilder() ;
		int cc = tmpxn.getChildCount() ;
		for(int i = 0 ; i < cc ; i ++)
			convertHtmlToPlain(tmpxn.getChildByIdx(i),sb,false) ;
		return sb.toString() ;
	}
	
	//需要换一行的标签
	private static final String NN_LINE_1 = "|br|table|tr|div|" ;
	//需要换两行的标签
	private static final String NN_LINE_2 = "|p|" ;
	//需要加入两个空格的标签
	private static final String NN_SPACE_2 = "|td|th|" ;
	
	
	private static final String NEW_LINE = "\r\n" ;
	private static final String SPACE_2 = "  " ;
	
	//标签内部的文本子节点要忽略的标签
	private static final String IGNORE_TXT_TAG_NAME= "|table|tr|tbody|" ;
	
	private static void convertHtmlToPlain(AbstractNode n,StringBuilder sb,boolean ignoretxt)
	{
		if(n instanceof XmlText && !ignoretxt)
		{
			XmlText xt = (XmlText)n ;
			sb.append(xt.getTextValue()) ;
			return ;
		}
		
		if(n instanceof XmlNode)
		{
			XmlNode xn = (XmlNode)n ;
			String nn = "|"+xn.getNodeName().toLowerCase()+"|" ;
			if(NN_LINE_1.indexOf(nn)>=0)
			{
				sb.append(NEW_LINE) ;
			}
			else if(NN_LINE_2.indexOf(nn)>=0)
			{
				sb.append(NEW_LINE).append(NEW_LINE) ;
			}
			else if(NN_SPACE_2.indexOf(nn)>=0)
			{
				sb.append(SPACE_2) ;
			}
			else
			{//default using one space
				sb.append(' ') ;
			}
			
			int cc = xn.getChildCount() ;
			for(int k = 0 ; k < cc ; k++)
			{
				boolean big = IGNORE_TXT_TAG_NAME.indexOf(nn)>=0 ;
				convertHtmlToPlain(xn.getChildByIdx(k),sb,big) ;
			}
		}
	}
}
