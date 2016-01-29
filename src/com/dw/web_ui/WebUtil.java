package com.dw.web_ui;

import java.io.*;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import javax.servlet.ServletRequest;
import javax.servlet.http.*;

import com.dw.system.Convert;

public class WebUtil
{
//	public static String byteArray2HexStr(byte[] bs)
//	{
//		if (bs == null)
//			return null;
//
//		if (bs.length == 0)
//			return "";
//
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < bs.length; i++)
//		{
//			int tmpi = 255 ;
//			tmpi = tmpi & bs[i] ;
//			String s = Integer.toHexString(tmpi);
//			if (s.length() == 1)
//				s = "0" + s;
//			sb.append(s);
//		}
//		return sb.toString();
//	}
//
//	public static byte[] hexStr2ByteArray(String hexstr)
//	{
//		if (hexstr == null)
//			return null;
//
//		if (hexstr.equals(""))
//			return new byte[0];
//
//		int s = hexstr.length() / 2;
//		byte[] ret = new byte[s];
//		for (int i = 0; i < s; i++)
//		{
//			ret[i] = (byte)Short.parseShort(hexstr.substring(i * 2, i * 2 + 2), 16);
//		}
//		return ret;
//	}

	public static String encodeHexUrl(String pv)
	{
		return Convert.encodeHexUrl(pv) ;
	}
	
	

	public static String decodeHexUrl(String hexu)
	{
		return Convert.decodeHexUrl(hexu) ;
	}
	
	public static String decodeSmartUrl(String u)
	{
		return Convert.decodeSmartUrl(u) ;
	}
	
	
	////////////////////////////////////
//	��һ���ַ���keywords��separator�ֿ�������
	public static String[] compart_KeyWord(String keywords, String separator)
	{
		StringTokenizer st = new StringTokenizer(keywords, separator);
		int tokenizernumber = st.countTokens();
		String[] keyWords = new String[tokenizernumber];
		int i = 0;
		while (st.hasMoreTokens())
		{
			keyWords[i] = st.nextToken();
			
			i++;
		}
		return keyWords;
	}

	//���ַ�������keywords��separator��ϳ��ַ���
	public static String link_KeyWord(String[] keywords, String separator)
	{
		String result = "";

		if (keywords == null || keywords.length == 0)
		{
			return result;
		}
		for (int i = 0; i < keywords.length; i++)
		{
			if (keywords.length == 1)
			{
				result = keywords[0];
			}
			else if (i < keywords.length - 1)
			{
				result = result + keywords[i] + separator;
			}
			else
			{
				result = result + keywords[i];
			}
		}
		return result;
	}

	//�����ַ���
	public static String plainToHtml(String input)
	{
		return Convert.plainToHtml(input) ;
//		if (input == null)
//		{
//			return "";
//		}
//
//		char[] array = input.toCharArray();
//
//		StringBuffer buf = new StringBuffer(array.length + array.length / 2);
//
//		for (int i = 0; i < array.length; i++)
//		{
//			if ( (i != 0) && (i % 60 == 0))
//			{
//				buf.append("<br/>");
//			}
//			switch (array[i])
//			{
//				case '<':
//					buf.append("&lt;");
//					break;
//				case '>':
//					buf.append("&gt;");
//					break;
//				case '&':
//					buf.append("&amp;");
//					break;
//				case '\n':
//					buf.append("<br/>");
//					break;
////				case ' ':
////					buf.append("&nbsp;");
////					break;
//				case '\'':
//					buf.append("''");
//				default:
//					buf.append(array[i]);
//					break;
//			}
//		}
//		return buf.toString();
	}

	//�����ַ���
	public static String filterString(String input)
	{
		if (input == null)
		{
			return "";
		}

		char[] array = input.toCharArray();

		StringBuffer buf = new StringBuffer(array.length + array.length / 2);

		for (int i = 0; i < array.length; i++)
		{
			//if((i!=0)&&(i%80 == 0))
			//buf.append("<br/>");
			switch (array[i])
			{
				case '<':
					buf.append("&lt;");
					break;
				case '>':
					buf.append("&gt;");
					break;
				case '&':
					buf.append("&amp;");
					break;
				case '\n':
					buf.append("<br/>");
					break;
				case ' ':
					buf.append("&nbsp;");
					break;
				case '\'':
					buf.append("''");
				default:
					buf.append(array[i]);
					break;
			}
		}
		return buf.toString();
	}

	/**�õ�ʱ���ַ���*/
	public static String parseDateString(java.util.Date myDate)
	{
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(myDate);

		return dateString;
	}

	/**
	 * ���ַ����г������ַ������е��ַ���ԭλ�ù��˵�
	 */
	public static String filterChars(String s, char[] chars)
	{
		if (s == null)
		{
			return "";
		}
		String result = "";
		for (int i = 0; i < s.length(); i++)
		{
			char ch = s.charAt(i);
			if (!inSet(chars, ch))
			{
				result += ch;
			}
		}
		return result;
	}

	/**
	 * �ж��Ƿ��ַ���ĳ���ַ�������
	 */
	public static boolean inSet(char[] chars, char ch)
	{
		for (int i = 0; i < chars.length; i++)
		{
			if (chars[i] == ch)
			{
				return true;
			}
		}
		return false;
	}

	//ȥ���ַ����е����пո�
	public static String deleteSpace(String source)
	{
		String tmp = "";

		for (int i = 0; i < source.length(); i++)
		{
			if (source.charAt(i) == ' ')
			{
				continue;
			}
			tmp = tmp + source.charAt(i);
		}
		return tmp;
	}

	//�ַ����滻
	public static String replaceString(String source, String oldStr,
									   String newStr)
	{
		String result = "";
		String rightStr = source;

		int leftPos = -1;
		int rightPos = 0;

		while ( (leftPos = rightStr.indexOf(oldStr)) != -1)
		{
			rightPos = leftPos + oldStr.length();
			result = result + rightStr.substring(0, leftPos) + newStr;
			rightStr = rightStr.substring(rightPos);
		}

		if (!rightStr.equals(""))
		{
			result = result + rightStr;

		}
		return result;
	}

	/**
	 *
	 * @param s
	 * @return
	 */
	private static int disLen(String s)
	{
		if (s == null)
		{
			return 0;
		}
		int l = s.length();
		int r = 0;
		for (int i = 0; i < l; i++)
		{
			if (s.charAt(i) <= 0xFF)
			{
				r += 1;
			}
			else
			{
				r += 2;
			}
		}
		return r;
	}

	private static String disSubstring(String s, int len)
	{
		if (s == null)
		{
			return "";
		}
		int l = s.length();
		StringBuffer sb = new StringBuffer(len);
		int r = 0;
		for (int i = 0; i < l && r < len; i ++)
		{
			sb.append(s.charAt(i));
			if (s.charAt(i) <= 0xFF)
			{
				r += 1;
			}
			else
			{
				r += 2;
			}
		}
		return sb.toString();
	}

	/**
	 * �ܶ��������Ҫ��ҳ�����������������ڣ��Ա�֤ҳ������ۡ�
	 * �÷������Ƕ���ʾ�����г��ȵ����ơ������ܹ��Զ��ѿո��ַ�ת��Ϊ&nbsp;
	 * @param s
	 * @param len
	 * @return
	 */
	public static String fitHTM(String s, int len)
	{
		if (s == null)
		{
			return "";
		}
		int oldlen = s.length() ;

		s = disSubstring(s, len * 2);
		if (s.length() < oldlen)
		{
			s += "...";
		}

		s = replaceString(s, "&nbsp;", " ");

		return s;
	}

	//
	//�ж��ַ����Ƿ�Ϊ��
	//
	public static boolean empty(String s)
	{
		if (s == null || s.equals(""))
		{
			return true;
		}

		return false;
	}
	
	/**
	 * ��ȡweb�����п��ܵ�����ֵ,�÷������������ܲ��������г���
	 * @param sr
	 * @param possiable_params
	 * @return
	 */
	public static String getPossiableReqValue(ServletRequest sr,String[] possiable_params)
	{
		if(possiable_params==null||possiable_params.length<=0)
			return null ;
		
		for(String pn:possiable_params)
		{
			String pv = sr.getParameter(pn);
			if(pv!=null)
				return pv ;
		}
		
		return null ;
	}
	
	
	/**
	 * ��һ��ҳ��������Ӧ�����У�ע����Ҫ����Դ
	 * �÷������Ա�����Ҫ����Դ���ظ��������js css�ļ���
	 * @param resurl
	 * @param req
	 * @param resp
	 */
	public static boolean registerPageWebReference(String resurl,HttpServletRequest req,Writer respw,HttpServletResponse resp)
		throws IOException
	{
		if(Convert.isNullOrEmpty(resurl))
			return false;
		String k = "__reg_res_" + resurl ;
		Boolean b = (Boolean)req.getAttribute(k) ;
		if(b!=null&&b)
			return false;//register already
		
		Writer w = respw ;
		if(w==null)
			w = resp.getWriter() ;
		
		String lu = resurl.toLowerCase() ;
		if(lu.endsWith(".js"))
		{
			w.write("<script type='text/javascript' src='");
			w.write(resurl);
			w.write("'></script>");
			
			req.setAttribute(k, true) ;
			return true;
		}
		
		if(lu.endsWith(".css"))
		{
			w.write("<link rel='stylesheet' href='");
			w.write(resurl);
			w.write("'/>");
			
			req.setAttribute(k, true) ;
			return true;
		}
		
		return false;
	}
}
