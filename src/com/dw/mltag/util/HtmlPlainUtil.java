package com.dw.mltag.util;

import java.io.StringReader;

import com.dw.mltag.AbstractNode;
import com.dw.mltag.NodeParser;
import com.dw.mltag.XmlNode;
import com.dw.mltag.XmlText;

public class HtmlPlainUtil
{
	/**
	 * ��һ��html��ʽ�����ݣ�ת���ɶ�Ӧ���ı�
	 * ����Ҫ��p,br,table tr td �����ݽ��д���
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
	
	//��Ҫ��һ�еı�ǩ
	private static final String NN_LINE_1 = "|br|table|tr|div|" ;
	//��Ҫ�����еı�ǩ
	private static final String NN_LINE_2 = "|p|" ;
	//��Ҫ���������ո�ı�ǩ
	private static final String NN_SPACE_2 = "|td|th|" ;
	
	
	private static final String NEW_LINE = "\r\n" ;
	private static final String SPACE_2 = "  " ;
	
	//��ǩ�ڲ����ı��ӽڵ�Ҫ���Եı�ǩ
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
