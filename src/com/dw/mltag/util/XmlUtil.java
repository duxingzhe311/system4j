/*
 * Created on 2004-8-3
 *
 * Copyright (c) Jason Zhu
 */
package com.dw.mltag.util;

import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.dw.system.Convert;
/**
 * @author Jason Zhu
 *
 * Desc:
 */
public class XmlUtil
{
	/**
	 * ��ȡԪ�ص��������Ժ�ֵ
	 *@param ele Ԫ�ض���
	 *@return Hashtable ������Ժ�ֵ��Hash����keyΪ��������Ӧ��ֵΪ����ֵ
	 */
	public static Properties getElementAttributes(Element ele)
	{
		Properties ht = new Properties();
		NamedNodeMap nnm = ele.getAttributes();
		int len = nnm.getLength();
		Node tmpn = null;
		for (int k = 0; k < len; k++)
		{
			tmpn = nnm.item(k);
			String tmps = tmpn.getNodeValue();
			if (tmps == null)
			{
				tmps = "";
			}
			ht.put(tmpn.getNodeName(), tmps);
		}
		return ht;
	}

	/**
	 * �õ���ǰԪ�ص����е���Ԫ�أ�����Ԫ�ض���ָ���ı�ǩ��
	 *@param ele ��ǰԪ��
	 *@param tagname ��ǩ����"*" �������еı�ǩ��
	 *@return Element[] Ԫ������
	 */
	public static Element[] getSubChildElement(Element ele, String tagname)
	{
		return Convert.getSubChildElement(ele, tagname) ;
	}

	/**
	 * ���һ��Ԫ�صĵ�һ���ı�����
	 * @param ele
	 * @return
	 */
	public static String getElementFirstTxt(Element ele)
	{
		NodeList nl = ele.getChildNodes() ;
		if(nl==null||nl.getLength()<=0)
			return null ;
		
		int s = nl.getLength() ;
		for(int i = 0 ; i < s ; i ++)
		{
			Node n = nl.item(i) ;
			if(n instanceof Text)
			{
				return ((Text)n).getNodeValue() ;
			}
		}
		
		return null ;
	}
	/**
	 * �ѽ���е��漰ʵ�����õ��ַ�ת��Ϊʵ��
	 */
	public static String xmlEncoding(String input)
	{
		if (input == null)
		{
			return null;
		}
		String entitystr = "><&\'\"\r\n";
		StringTokenizer tmpst = new StringTokenizer(input, entitystr, true);
		StringBuffer tmpsb = new StringBuffer(input.length() + 100);
		String tmps = null;
		while (tmpst.hasMoreTokens())
		{
			tmps = tmpst.nextToken();
			if (tmps.length() == 1 && entitystr.indexOf(tmps) >= 0)
			{
				switch (tmps.charAt(0))
				{
					case '<':
						tmpsb.append("&lt;");
						break;
					case '>':
						tmpsb.append("&gt;");
						break;
					case '&':
						tmpsb.append("&amp;");
						break;
					case '\'':
						tmpsb.append("&apos;");
						break;
					case '\"':
						tmpsb.append("&quot;");
						break;
					case '\n':
						tmpsb.append("&#10;");
						break;
					case '\r':
						tmpsb.append("&#13;");
						break;
				}
			}
			else
			{
				tmpsb.append(tmps);
			}
		}

		return tmpsb.toString();
	}

	/**
		@param ls
		@return String
		@roseuid 3E6F3C4302CC
	 */
	public static String arrayOfLongToStr(long[] ls)
	{
		if (ls == null || ls.length == 0)
		{
			return "";
		}

		StringBuffer tmpsb = new StringBuffer();

		tmpsb.append('|');
		for (int i = 0; i < ls.length; i++)
		{
			tmpsb.append(ls[i])
				.append('|');
		}

		return tmpsb.toString();
	}

	/**
		@param str
		@return long[]
		@roseuid 3E6F3C680149
	 */
	public static long[] strToArrayOfLong(String str)
	{
		StringTokenizer tmpst = new StringTokenizer(str, "|", false);
		int len = tmpst.countTokens();
		long[] retl = new long[len];

		for (int i = 0; i < len; i++)
		{
			retl[i] = Long.parseLong(tmpst.nextToken());
		}

		return retl;
	}

	/**
	   @param str
	   @return long[]
	   @roseuid 3E6F3C680149
	 */
	public static String[] strToArrayOfString(String str)
	{
		if (str == null)
		{
			return new String[0];
		}
		StringTokenizer tmpst = new StringTokenizer(str, "|", false);
		int len = tmpst.countTokens();
		String[] retl = new String[len];

		for (int i = 0; i < len; i++)
		{
			retl[i] = tmpst.nextToken();
		}

		return retl;
	}

	/**
	 @param ls
	 @return String
	 @roseuid 3E6F3C4302CC
	 */
	public static String arrayOfStringToStr(String[] ls)
	{
		if (ls == null || ls.length == 0)
		{
			return "";
		}

		StringBuffer tmpsb = new StringBuffer();

		tmpsb.append('|');
		for (int i = 0; i < ls.length; i++)
		{
			tmpsb.append(ls[i])
				.append('|');
		}

		return tmpsb.toString();
	}
}
