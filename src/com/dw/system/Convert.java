package com.dw.system;

import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.mltag.util.HtmlPlainUtil;
import com.dw.system.util.HTMLUtil;
import com.dw.system.xmldata.XmlData;

import net.sourceforge.pinyin4j.PinyinHelper;

public class Convert
{
	static SimpleDateFormat FshortYMD = new SimpleDateFormat("yyyy-MM-dd");

	public static String toShortYMD(Date d)
	{
		if (d == null)
			return "";
		return FshortYMD.format(d);
	}

	static SimpleDateFormat YM = new SimpleDateFormat("yyyy-MM");

	public static String toYM(Date d)
	{
		if (d == null)
			return "";
		return YM.format(d);
	}

	static SimpleDateFormat FfulYMD = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static String toFullYMDHMS(Date d)
	{
		if (d == null)
			return "";
		return FfulYMD.format(d);
	}
	
	static SimpleDateFormat XmlDateStr = new SimpleDateFormat(
	"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public static String toXmlValDateStr(Date d)
	{
		if(d==null)
			return "" ;
		return XmlDateStr.format(d);
	}

	static SimpleDateFormat FYMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static String toYMDHM(Date d)
	{
		if (d == null)
			return "";
		return FYMDHM.format(d);
	}
	
	static SimpleDateFormat HM = new SimpleDateFormat("HH:mm");
	
	public static String toHM(Date d)
	{
		if (d == null)
			return "";
		return HM.format(d);
	}

	static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	/**
	 * ���ַ�����ʾ��ʱ����Ϣ,ת��Ϊʱ����� �ַ�����ʽ֧������ yyyy-MM-ddThh:mi:ss.SSSZ
	 * yyyy-MM-ddThh:mi:ss.SSS yyyy-MM-dd hh:mi:ss yyyy-MM-dd hh:mi yyyy-MM-dd
	 * hh yyyy-MM-dd yyyy-MM yyyy
	 * 
	 * @param datastr
	 * @return
	 */
	public static Calendar toCalendar(String datastr)
	{
		if (datastr == null || datastr.equals(""))
			return null;

		int y = 0, m = 0, d = 0, h = 0, mi = 0, s = 0, n = 0;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, y);
		cal.set(Calendar.MONTH, m - 1);
		cal.set(Calendar.DAY_OF_MONTH, d);
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, mi);
		cal.set(Calendar.SECOND, s);
		cal.set(Calendar.MILLISECOND, n);

		datastr = datastr.trim();

		int p = 0, q;
		// yyyy-...
		q = datastr.indexOf('-', p);
		if (q <= 0)
		{
			y = Integer.parseInt(datastr);
			cal.set(Calendar.YEAR, y);
			return cal;
		}

		y = Integer.parseInt(datastr.substring(p, q));
		cal.set(Calendar.YEAR, y);

		p = q + 1;
		// yyyy-mm-...
		q = datastr.indexOf('-', p);
		if (q < 0)
		{
			m = Integer.parseInt(datastr.substring(p));
			cal.set(Calendar.MONTH, m - 1);
			return cal;
		}

		m = Integer.parseInt(datastr.substring(p, q));
		cal.set(Calendar.MONTH, m - 1);

		p = q + 1;
		// yyyy-mm-dd ...
		q = datastr.indexOf('T', p);
		if (q < 0)
			q = datastr.indexOf(' ', p);

		if (q < 0)
		{
			d = Integer.parseInt(datastr.substring(p));
			cal.set(Calendar.DAY_OF_MONTH, d);
			return cal;
		}

		d = Integer.parseInt(datastr.substring(p, q));
		cal.set(Calendar.DAY_OF_MONTH, d);

		p = q + 1;
		// yyyy-mm-dd hh:...
		q = datastr.indexOf(':', p);
		if (q < 0)
		{
			h = Integer.parseInt(datastr.substring(p));
			cal.set(Calendar.HOUR_OF_DAY, h);
			return cal;
		}

		h = Integer.parseInt(datastr.substring(p, q).trim());
		cal.set(Calendar.HOUR_OF_DAY, h);

		p = q + 1;
		// yyyy-mm-dd hh:mi:...
		q = datastr.indexOf(':', p);
		if (q < 0)
		{
			mi = Integer.parseInt(datastr.substring(p));
			cal.set(Calendar.MINUTE, mi);
			return cal;
		}

		mi = Integer.parseInt(datastr.substring(p, q));
		cal.set(Calendar.MINUTE, mi);

		p = q + 1;
		// yyyy-mm-dd hh:mi:ss...
		q = datastr.indexOf('.', p);
		if (q < 0)
		{
			s = Integer.parseInt(datastr.substring(p).trim());
			cal.set(Calendar.SECOND, s);
			return cal;
		}

		s = Integer.parseInt(datastr.substring(p, q));
		cal.set(Calendar.SECOND, s);

		p = q + 1;
		// yyyy-mm-dd hh:mi:ss.ns
		String tmps = datastr.substring(p);
		if (tmps.length() >= 3)
		{
			n = Integer.parseInt(datastr.substring(p, p + 3).trim());
			cal.set(Calendar.MILLISECOND, n);
		}

		// ss.+0800
		if (tmps.length() == 8)
		{// zone
			int zmillis = Integer.parseInt(tmps.substring(4, 6)) * 3600000
					+ Integer.parseInt(tmps.substring(6, 8)) * 60000;

			if (tmps.charAt(3) == '-')
				zmillis = -zmillis;

			cal.set(Calendar.ZONE_OFFSET, zmillis);
		}

		// System.out.println("input="+datastr+"
		// output="+sdf.format(cal.getTime()));
		return cal;
	}

	/**
	 * �������Plain�ı�ת����html�ı�,���ʺ�ҳ�����չʾ����Ҫ
	 * 
	 * @param input
	 * @return
	 */
	public static String plainToHtml(String input)
	{
		return plainToHtml(input,true) ;
	}
	public static String plainToHtml(String input,boolean trans_br)
	{
		return plainToHtml(input,trans_br,false) ;
	}
	public static String plainToHtml(String input,boolean trans_br,boolean trans_space)
	{
		if (input == null)
		{
			return "";
		}

		char[] array = input.toCharArray();

		StringBuffer buf = new StringBuffer(array.length + array.length / 2);

		for (int i = 0; i < array.length; i++)
		{
			// if ( (i != 0) && (i % 60 == 0))
			// {
			// buf.append("<br/>");
			// }

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
				if(trans_br)
					buf.append("<br/>");
				else
					buf.append('\n') ;
				break;
			case ' ':
				if(trans_space)
					buf.append("&nbsp;");
				else
					buf.append(array[i]);
				break;
			case '\"':
				buf.append("&#34;");
				break;
			case '\'':
				buf.append("&#39;");
				break;
			default:
				buf.append(array[i]);
				break;
			}
		}
		return buf.toString();
	}
	
	/**
	 * ת��html��ʽ�����ݵ��ı�
	 * @param htmlstr
	 * @return
	 */
	public static String htmlToPlain(String htmlstr)
	{
		return HtmlPlainUtil.convertHtmlToPlain(htmlstr) ;
	}
	
	public static String htmlToPlain0(String htmlstr)
	{
		return HTMLUtil.textFromHTML(htmlstr) ;
	}

	
	/**
	 * 
	 * @param txt
	 * @return
	 */
	public static String plainToJsStr(String txt)
	{
		if (txt == null)
			return null;

		if (txt.equals(""))
			return "";

		StringBuilder sb = new StringBuilder();
		int len = txt.length();
		for (int i = 0; i < len; i++)
		{
			char c = txt.charAt(i);
			switch (c)
			{
			case '\'':
				sb.append("\\\'");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\n':
				sb.append("\\n");
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ��
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNullOrEmpty(String s)
	{
		if (s == null)
			return true;

		return s.equals("");
	}

	public static boolean isNotNullEmpty(String s)
	{
		if (s == null)
			return false;

		return !s.equals("");
	}

	/**
	 * �ж��ַ����Ƿ�Ϊnull,��ֻ�пհ��ַ���
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNullOrTrimEmpty(String s)
	{
		if (s == null)
			return true;

		return s.trim().equals("");
	}

	public static boolean isNotNullTrimEmpty(String s)
	{
		if (s == null)
			return false;

		return !s.trim().equals("");
	}

	public static long parseToInt64(String s, long defv)
	{
		if (isNullOrTrimEmpty(s))
			return defv;

		return Long.parseLong(s);
	}

	public static int parseToInt32(String s, int defv)
	{
		if (isNullOrTrimEmpty(s))
			return defv;

		return Integer.parseInt(s);
	}

	public static short parseToInt16(String s, short defv)
	{
		if (isNullOrTrimEmpty(s))
			return defv;

		return Short.parseShort(s);
	}

	public static double parseToDouble(String s, double defv)
	{
		if (isNullOrTrimEmpty(s))
			return defv;

		return Double.parseDouble(s);
	}

	public static float parseToFloat(String s, float defv)
	{
		if (isNullOrTrimEmpty(s))
			return defv;

		return Float.parseFloat(s);
	}
	
	public static String toDecimalDigitsStr(double d,int dec_digit_num,boolean five_in)
	{
        String   rStr="";
        
        if(five_in)
		{
        	BigDecimal tmpv = new BigDecimal(d);
        	tmpv=tmpv.setScale(dec_digit_num, BigDecimal.ROUND_HALF_UP) ;
        	return tmpv.toString() ;
		}
        
        while(rStr.length()<dec_digit_num)
        	rStr += "#";
        java.text.DecimalFormat   df   =   new   java.text.DecimalFormat("##."+rStr);   
        return df.format(d);   
	}
	/**
	 * ת��������,����С����λ�����ַ���
	 * @param d ԭʼ����
	 * @param dec_digit_num С��λ��
	 * @param five_in �Ƿ���������
	 * @return
	 */
	public static String toDecimalDigitsStr0(double d,int dec_digit_num,boolean five_in)
	{
		String s = ""+d ;
		int p = s.indexOf('.') ;
		if(p<0)
			return s ;
		
		if(s.length()-p-1<=dec_digit_num)
			return s ;
		
		if(five_in)
		{
			//return (new BigDecimal(d)).setScale(dec_digit_num,BigDecimal.ROUND_UP).toString();
			
			double tenm = Math.pow(10,dec_digit_num);
			double dv = d*tenm;
			String tmps = ""+dv;
			int k = tmps.indexOf('.') ;
			if(k<0)
				return s ;
			
			long lv = Long.parseLong(tmps.substring(0,k)) ;
			if(dv-lv>0.5)
			{
				lv ++ ;
			}
			
			tmps = ""+lv ;
			
			if(d<1)
				return "0."+tmps ;
			else
				return tmps.substring(0,p)+"."+tmps.substring(p) ;
		}
		else
		{
			return s.substring(0,p+dec_digit_num+1) ;
		}
	}
	
	public static String toDecimalDigitsStr(double d,int dec_digit_num)
	{
		return toDecimalDigitsStr(d,dec_digit_num,true) ;
	}

	/**
	 * �жϱ��������Ƿ���Ч �÷�������Ϊ������Ч�ı�����������֤
	 * 
	 * @param n
	 */
	public static void checkVarName(String n)
	{
		StringBuilder sb = new StringBuilder();
		if (!checkVarName(n, sb))
			throw new IllegalArgumentException(sb.toString());

		return;
	}

	public static boolean checkVarName(String n, StringBuilder invalidreson)
	{
		if (n == null || n.equals(""))
		{
			invalidreson.append("name cannot be null or empty!");
			return false;
		}

		char c1 = n.charAt(0);
		boolean bc1 = (c1 >= 'a' && c1 <= 'z') || (c1 >= 'A' && c1 <= 'Z')
				|| c1 == '_';
		if (!bc1)
		{
			invalidreson.append("name first char must be a-z A-Z");
			return false;
		}

		int s = n.length();
		for (int i = 1; i < s; i++)
		{
			char c = n.charAt(i);
			boolean bc = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| (c >= '0' && c <= '9') || c == '_';
			if (!bc)
			{
				invalidreson.append("invalid char =" + c);
				return false;
			}
		}
		return true;
	}

	/**
	 * �ж������ַ����ں���null��Empty������Ƿ���ͬ ����null��Empty��Ϊ��ͬ
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static boolean checkStrEqualsIgnoreNullEmpty(String str1, String str2)
	{
		if (isNullOrEmpty(str1))
		{
			return isNullOrEmpty(str2);
		}

		return str1.equals(str2);
	}

	public static int getBytesLen(String s, String encoding)
	{
		if (s == null)
			return 0;

		if ("utf-8".equalsIgnoreCase(encoding)
				|| "utf8".equalsIgnoreCase(encoding))
		{
			int c = 0;
			int l = s.length();
			for (int i = 0; i < l; i++)
			{
				if (s.charAt(i) > 255)
					c += 3;
				else
					c++;
			}
			return c;
		}

		if ("utf-16".equalsIgnoreCase(encoding)
				|| "utf16".equalsIgnoreCase(encoding))
		{
			return s.length() * 2;
		}

		if (Convert.isNullOrEmpty(encoding))
			return s.length();
		else
			return s.length() * 2;
	}

//	/**
//	 * �ж��Ƿ���ȫ���ַ�
//	 * 
//	 * @param c
//	 * @return
//	 */
//	public static boolean isQjChar(char c)
//	{
//		int i = (int) c;
//		int f1 = i & 0xF0;
//		int f2 = i & 0x0F;
//		f1 = f1 >> 8;
//		return f1 == 0xA3;
//	}
//
//	public static char transQjToBj(char c)
//	{
//		int i = (int) c;
//
//		int f1 = i & 65280;// 0xFF00 ;
//		int f2 = i & 255;// 0x00FF ;
//
//		f1 = f1 >>> 8;
//		System.out.println("c=" + c + " i=" + i + " f1=" + f1 + "  f2=" + f2);
//		if (f1 == 0xA3)
//		{
//			if (f2 > 128)
//				return (char) (f2 - 128);
//			return c;
//		}
//		return c;
//	}
//
//	/**
//	 * �ж��Ƿ��ǰ���ַ�
//	 * 
//	 * @param c
//	 * @return
//	 */
//	public static boolean isBjChar(char c)
//	{
//		int i = (int) c;
//		return i >= 32 && i <= 126;
//	}

	static String QJCS = "�࡫�������磥�䣦�������ߣ����������ۣݣ��ܣ�������������������" ;
	static String BJCS = "`~!@#$%^&*()_+-={}[]|\\\"\':;<>,.?/" ;
	
	static HashMap<Character,Character> QJ2BJ = new HashMap<Character,Character>() ;
	static
	{
		for(int i = 0 ; i < 26 ; i ++)
		{
			QJ2BJ.put((char)('��'+i), (char)('a'+i)) ;QJ2BJ.put((char)('��'+i), (char)('A'+i)) ;
		}
		
		for(int i = 0 ; i < 10 ; i ++)
			QJ2BJ.put((char)('��'+i), (char)('0'+i)) ;
		
		int len = QJCS.length() ;
		for(int i = 0 ; i < len ; i ++)
			QJ2BJ.put(QJCS.charAt(i),BJCS.charAt(i)) ;
	}
	/**
	 * ���ַ���ת��Ϊ����ַ� ���� �������� -> abcdef
	 * 
	 * @param s
	 * @return
	 */
	public static String toBj(String s)
	{
		if (Convert.isNullOrTrimEmpty(s))
			return s;

		try
		{
			int l = s.length();
			StringBuilder sb = new StringBuilder(l);
			//String tmps = null;
			//byte[] b = null;

			for (int i = 0; i < l; i++)
			{
				char tmpc = s.charAt(i);
				Character tmpbc = QJ2BJ.get(tmpc) ;
				if(tmpbc==null)
					sb.append(tmpc) ;
				else
					sb.append(tmpbc) ;
			}
			return sb.toString();
		}
		catch (Exception e)
		{
			return s;
		}
	}
	
//	public static String toBj(String s)
//	{
//		if (Convert.isNullOrTrimEmpty(s))
//			return s;
//
//		try
//		{
//			int l = s.length();
//			StringBuilder sb = new StringBuilder(l);
//			String tmps = null;
//			byte[] b = null;
//
//			for (int i = 0; i < l; i++)
//			{
//				tmps = "" + s.charAt(i);
//				b = tmps.getBytes("unicode");
//
//				if (b[3] == -1)
//				{
//					b[2] = (byte) (b[2] + 32);
//					b[3] = 0;
//
//					sb.append(new String(b, "unicode"));
//				}
//				else
//				{
//					sb.append(tmps);
//				}
//			}
//			return sb.toString();
//		}
//		catch (Exception e)
//		{
//			return s;
//		}
//	}
	
	/**
	 * �����ĵ��ַ���ת��Ϊ����ƴ�� ��һ����ĸ����д
	 * @param chinese_str
	 * @return
	 */
	public static String transChinese2PinyinAbbr(String chinese_str)
	{
		if(chinese_str==null)
			return null ;
		
		if("".equals(chinese_str))
			return chinese_str ;
		
		int len = chinese_str.length() ;
		StringBuilder sb = new StringBuilder() ;
		for(int i = 0 ; i < len ; i ++)
		{
			String[] phs = PinyinHelper.toHanyuPinyinStringArray(chinese_str.charAt(i)) ;
			if(phs==null||phs.length<=0)
				continue ;
			
			if(phs[0].length()<=0)
				continue ;
			
			sb.append(phs[0].charAt(0)) ;
		}
		
		return sb.toString() ;
	}
	
	/**
	 * �õ�ĳһ�����ʼʱ��ͽ���ʱ��
	 * @param year
	 * @return
	 */
	public static long[] getYearStartEnd(int year)
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR,year) ;
		c.set(Calendar.MONTH,0) ;
		c.set(Calendar.DAY_OF_MONTH,1) ;
		c.set(Calendar.HOUR_OF_DAY,0) ;
		c.set(Calendar.MINUTE,0) ;
		c.set(Calendar.SECOND,0) ;
		c.set(Calendar.MILLISECOND,0) ;
		
		long st = c.getTimeInMillis();
		
		c.set(Calendar.MONTH,11) ;
		c.set(Calendar.DAY_OF_MONTH,c.getActualMaximum(Calendar.DAY_OF_MONTH)) ;
		c.set(Calendar.HOUR_OF_DAY,23) ;
		c.set(Calendar.MINUTE,59) ;
		c.set(Calendar.SECOND,59) ;
		c.set(Calendar.MILLISECOND,999) ;
		
		long et = c.getTimeInMillis();
		return new long[]{st,et};
	}
	
	/**
	 * �õ�ĳһ�·ݵ���ʼ����ʱ��
	 * @param year
	 * @param month
	 * @return
	 */
	public static long[] getMonthStartEnd(int year,int month)
	{
		Calendar c = Calendar.getInstance() ;
		c.set(Calendar.YEAR,year) ;
		c.set(Calendar.MONTH,month-1) ;
		c.set(Calendar.DAY_OF_MONTH,1) ;
		c.set(Calendar.HOUR_OF_DAY,0) ;
		c.set(Calendar.MINUTE,0) ;
		c.set(Calendar.SECOND,0) ;
		c.set(Calendar.MILLISECOND,0) ;
		
		long st = c.getTimeInMillis() ;
		
		c.set(Calendar.DAY_OF_MONTH,c.getActualMaximum(Calendar.DAY_OF_MONTH)) ;
		c.set(Calendar.HOUR_OF_DAY,23) ;
		c.set(Calendar.MINUTE,59) ;
		c.set(Calendar.SECOND,59) ;
		c.set(Calendar.MILLISECOND,999) ;
		
		long et = c.getTimeInMillis() ;
		return new long[]{st,et} ;
	}
	
	
	public static List<long[]> getWeekStartEndsInMonth(int year,int mon)
	{
		return getWeekStartEndsInMonth(year,mon,false) ;
	}
	/**
	 * ���һ����֮�ڵ������ܵ���ʼ����ʱ��
	 * ��һ�ܺ����һ��������첻�ڱ���֮��,���ų�
	 * @param year
	 * @param mon
	 * @param b_week_start_monday �Ƿ�������һ��Ϊ�ܵĿ�ʼ,ȱʡ����������Ϊ�ܿ�ʼ
	 * @return
	 */
	public static List<long[]> getWeekStartEndsInMonth(int year,int mon,boolean b_week_start_monday)
	{
		List<long[]> rets = new ArrayList<long[]>() ;
		Calendar sc = Calendar.getInstance() ;
		sc.set(Calendar.YEAR, year) ;
		sc.set(Calendar.MONTH, mon-1) ;
		sc.set(Calendar.DAY_OF_MONTH, 1) ;
		
		sc.set(Calendar.HOUR_OF_DAY, 0);
		sc.set(Calendar.MINUTE, 0);
		sc.set(Calendar.SECOND, 0);
		sc.set(Calendar.MILLISECOND, 0);
		//��һ��
		long s1 = sc.getTimeInMillis() ;
		
		Calendar ec = Calendar.getInstance() ;
		ec.setTimeInMillis(s1) ;
		
		int df = ec.get(Calendar.DAY_OF_WEEK) ;
		if(b_week_start_monday)
		{
			if(df!=Calendar.SUNDAY)
				ec.add(Calendar.DAY_OF_MONTH, Calendar.SATURDAY+1 - df) ;
		}
		else
		{
			ec.add(Calendar.DAY_OF_MONTH, Calendar.SATURDAY - df) ;
		}
		ec.set(Calendar.HOUR_OF_DAY, 23);
		ec.set(Calendar.MINUTE, 59);
		ec.set(Calendar.SECOND, 59);
		ec.set(Calendar.MILLISECOND, 999);
		long e1 = ec.getTimeInMillis() ;
		rets.add(new long[]{s1,e1});
		
		sc.set(Calendar.DAY_OF_MONTH, ec.get(Calendar.DAY_OF_MONTH)-6) ;
		
		for(int i = 0 ; i < 3 ; i ++)
		{
			sc.add(Calendar.DAY_OF_MONTH, 7) ;
			ec.add(Calendar.DAY_OF_MONTH, 7) ;
			rets.add(new long[]{sc.getTimeInMillis(),ec.getTimeInMillis()}) ;
		}
		
		for(int i = 0 ; i < 2 ; i ++)
		{
			sc.add(Calendar.DAY_OF_MONTH, 7) ;
			if(sc.get(Calendar.MONTH)+1!=mon)
				return rets ;
			
			ec.add(Calendar.DAY_OF_MONTH, 7) ;
			if(ec.get(Calendar.MONTH)+1==mon)
			{
				rets.add(new long[]{sc.getTimeInMillis(),ec.getTimeInMillis()}) ;
			}
			else
			{// read end of month day
				ec.add(Calendar.MONTH, -1) ;
				ec.set(Calendar.DAY_OF_MONTH, ec.getActualMaximum(Calendar.DAY_OF_MONTH)) ;
				rets.add(new long[]{sc.getTimeInMillis(),ec.getTimeInMillis()}) ;
				break ;
			}
		}
		
		return rets ;
	}
	

	static class LineReader
	{
		String strs = null;
		int idx=0 ;
		public LineReader(String s)
		{
			strs = s ;
		}
		
		
		public String readLine()
		{
			if(this.idx==this.strs.length())
				return null ;
			int i ;
			for(i = this.idx ; i < this.strs.length() ; i ++)
			{
				if(this.strs.charAt(i)=='\n')
				{//find line
					String tmps = this.strs.substring(this.idx,i+1).trim() ;
					//alert(tmps) ;
					this.idx = i +1 ;
					return tmps ;
				}
			}
			if(i==this.strs.length())
			{
				String tmps = this.strs.substring(this.idx).trim() ;
				this.idx = this.strs.length() ;
				return tmps ;
			}
			else
			{
				return null ;
			}
		}
	}
	/**
	 * ���������� xx=yy ���ַ�������ӳ��ת��
	 * @param inputs
	 * @return
	 */
	public static HashMap<String,String> transPropStrToMap(String inputs)
	{
		HashMap<String,String> ret = new HashMap<String,String>() ;
		if(inputs==null)
			return ret ;
		
		BufferedReader br = new BufferedReader(new StringReader(inputs)) ;
		String line = null ;
		
		try
		{
			while((line=br.readLine())!=null)
			{
				line = line.trim() ;
				if("".equals(line))
					continue ;
				
				int p = line.indexOf('=') ;
				if(p<0)
				{
					ret.put(line, "") ;
				}
				else
				{
					ret.put(line.substring(0,p).trim(), line.substring(p+1).trim());
				}
			}
			return ret ;
		}
		catch(IOException ioe)
		{
			return ret ;
		}
	}
	
	/**
	 * �����ж�ȡ�ֵ�ӳ��
	 * @param ins
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static HashMap<String,String> readStringMapFromStream(InputStream ins,String charset)
		throws Exception
	{
		HashMap<String,String> rets = new HashMap<String,String>() ;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(ins,charset)) ;
		String line ;
		while((line=br.readLine())!=null)
		{
			line = line.trim() ;
			
			if("".equals(line))
				continue ;
			
			if(line.startsWith("#"))
				continue ;
			
			int p = line.indexOf('=') ;
			if(p>0)
			{
				rets.put(line.substring(0,p), line.substring(p+1)) ;
			}
			else
			{
				rets.put(line, "") ;
			}
		}
		
		return rets ;
	}
	
	//////////////
	public static String byteArray2HexStr(byte[] bs)
	{
		return byteArray2HexStr(bs,0,bs.length) ;
	}
	
	public static String byteArray2HexStr(byte[] bs,int offset,int len)
	{
		if (bs == null)
			return null;

		if (bs.length == 0 || len<=0)
			return "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++)
		{
			int tmpi = 255 ;
			tmpi = tmpi & bs[i+offset] ;
			String s = Integer.toHexString(tmpi);
			if (s.length() == 1)
				s = "0" + s;
			sb.append(s);
		}
		return sb.toString().toUpperCase();
	}

	public static byte[] hexStr2ByteArray(String hexstr)
	{
		if (hexstr == null)
			return null;

		if (hexstr.equals(""))
			return new byte[0];

		int s = hexstr.length() / 2;
		byte[] ret = new byte[s];
		for (int i = 0; i < s; i++)
		{
			ret[i] = (byte)Short.parseShort(hexstr.substring(i * 2, i * 2 + 2), 16);
		}
		return ret;
	}
	
	
	public static void sort(List ll)
	{
		if(ll==null)
			return ;
		
		if(ll.size()<=0)
			return ;
		
		Comparable[] ss = new Comparable[ll.size()];
		
		ll.toArray(ss);
		
		Arrays.sort(ss);
		
		ll.clear();
		for(Comparable c:ss)
		{
			ll.add(c);
		}
	}
	
	
	public static void sort(List ll,Comparator cp)
	{
		if(ll==null)
			return ;
		
		if(ll.size()<=0)
			return ;
		
		Object[] ss = new Object[ll.size()];
		
		ll.toArray(ss);
		
		Arrays.sort(ss,cp);
		
		ll.clear();
		for(Object c:ss)
		{
			ll.add(c);
		}
	}
	
	/**
	 * �õ���ǰԪ�ص����е���Ԫ�أ�����Ԫ�ض���ָ���ı�ǩ��
	 *@param ele ��ǰԪ��
	 *@param tagname ��ǩ����"*" �������еı�ǩ��
	 *@return Element[] Ԫ������
	 */
	public static Element[] getSubChildElement(Element ele, String tagname)
	{
		if (ele == null)
		{
			return null;
		}

		boolean isall = false;
		if (tagname.equals("*"))
		{
			isall = true;
		}
		
		int p0 = tagname.indexOf(':');
		if(p0>=0)
			tagname = tagname.substring(p0+1);
		
		NodeList tmpnl = ele.getChildNodes();

		Node tmpn = null;

		Vector v = new Vector();
		int k;
		for (k = 0; k < tmpnl.getLength(); k++)
		{
			tmpn = tmpnl.item(k);

			if (tmpn.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}
			
			Element eee = (Element) tmpn;
			String noden = eee.getNodeName();
			int p = noden.indexOf(':') ;
			if(p>=0)
				noden = noden.substring(p+1);
			
			if (isall || tagname.equals(noden))
			{
				v.add(eee);
			}
		}

		int s = v.size();
		Element[] tmpe = new Element[s];
		for (k = 0; k < s; k++)
		{
			tmpe[k] = (Element) v.elementAt(k);

		}
		return tmpe;
	}
	
	/**
	 * ����jspҳ���е�http������ҳ���Ӧ�����ģ�飨����������
	 * @param req
	 * @return
	 */
	public static String getModuleNameByHttpReq(HttpServletRequest req)
	{
		String cp = req.getContextPath() ;
		if (Convert.isNullOrEmpty(cp))
			return null;

		if(cp.equals("/"))
			return null ;
		
		if(cp.startsWith("/"))
			return cp.substring(1);
		else
			return cp ;
	}
	
	/**
	 * �������ø������ĸ�ģ����װ�ص�ģ������
	 * @param c
	 * @return
	 */
	public static String getModuleNameByClass(Class c)
	{
		AppInfo ai = CompManager.getInstance().getAppInfo(c) ;
		if(ai==null)
			return null ;
		
		return ai.getContextName() ;
	}
	
	public static final String ATTRN_IN_XMLDATA = "biz_xml_data";
	
	public static XmlData getXmlDataFromRequest(HttpServletRequest req)
		throws Exception
	{
		XmlData xd = (XmlData)req.getAttribute(ATTRN_IN_XMLDATA) ;
	
		if(xd==null)
		{
			xd = new XmlData() ;
			XmlData.updateXmlDataFromHttpRequest(xd,req, "dx_") ;
			req.setAttribute(ATTRN_IN_XMLDATA, xd);
		}
		return xd ;
	}
	
	
	public static String encodeHexUrl(String pv)
	{
		if (pv == null)
			return null;

		if (pv.equals(""))
			return "";

		try
		{
			byte[] buf = pv.getBytes("UTF-8");
			return "=h="+Convert.byteArray2HexStr(buf);
		}
		catch (UnsupportedEncodingException ee)
		{
			throw new RuntimeException(ee.getMessage());
		}
	}
	
	

	public static String decodeHexUrl(String hexu)
	{
		if(hexu==null)
			return null ;
		
		if(hexu.startsWith("=h="))
		{
			hexu = hexu.substring(3);
			byte[] buf = Convert.hexStr2ByteArray(hexu);
			if (buf == null)
				return null;
			try
			{
				return new String(buf, "UTF-8");
			}
			catch (UnsupportedEncodingException ee)
			{
				throw new RuntimeException(ee.getMessage());
			}
		}
		else if(hexu.startsWith("=u="))
		{
			hexu = hexu.substring(3);
			
			try
			{
				//return URLDecoder.decode(hexu, "UTF-8");
				String s = URLDecoder.decode(hexu, "UTF-8") ;
				if(s.indexOf('%')<0)
					return s ;
				
				return URLDecoder.decode(URLDecoder.decode(hexu, "US-ASCII"),"UTF-8") ;
			}
			catch(Exception e)
			{
				throw new RuntimeException(e.getMessage());
			}
		}
		else
		{
			return hexu ;
		}
	}
	
	public static String decodeSmartUrl(String u)
	{
		if(u==null)
			return null ;
		if("".equals(u))
			return u ;
		
		if(u.startsWith("=h="))
		{
			return decodeHexUrl(u) ;
		}
		else if(u.startsWith("=u="))
		{//�ͻ��˽ű��ṩ��utf8urlencode���ı���--���Ҹñ���û��ͨ��ie��׼�ύ
			//��urlͨ��ajax��ʽֱ���ύ��
			u = u.substring(3) ;
			
			String s = null ;
			try
			{
				//return URLDecoder.decode(URLDecoder.decode(u, "US-ASCII"),"UTF-8") ;
				s = URLDecoder.decode(u, "UTF-8") ;
				if(s.indexOf('%')<0)
					return s ;
				
				return URLDecoder.decode(URLDecoder.decode(u, "US-ASCII"),"UTF-8") ;
			}
			catch(Exception e)
			{
				//throw new RuntimeException(e.getMessage());
				return s ;
			}
		}
		else
		{
			return u ;
		}
	}
	
	private static final int BYTE_MASK = 0x0f;
	private static final char[] HEX_DIGITS = new char[]
	                                                    {
	                                                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	                                                    };
	/**
	 * ����ĳ�ּ�ֵת����ʮ�����Ʊ�ʾ���ļ���
	 * 
	 * �÷�����James��Ŀ�У��洢�յ����ʼ�body�õ�
	 * 
	 * ͬʱ���ᱻWebMail��֧���õ�
	 * @param key
	 * @return
	 */
	public static String encodeKeyToFileName(String key)
	{
		final byte[] bytes = key.getBytes();
        final char[] buffer = new char[ bytes.length << 1 ];

        for( int i = 0, j = 0; i < bytes.length; i++ )
        {
            final int k = bytes[ i ];
            buffer[ j++ ] = HEX_DIGITS[ ( k >>> 4 ) & BYTE_MASK ];
            buffer[ j++ ] = HEX_DIGITS[ k & BYTE_MASK ];
        }
        
        return new String(buffer) ;
	}
	
	/**
	 * �ļ�����ת�ؼ�ֵ
	 * 
	 * �÷�����James��Ŀ�У��洢�յ����ʼ�body�õ�
	 * @param filename
	 * @return
	 */
	public static String decodeKeyFromFileName(String filename)
	{
		final int size = filename.length();
        final byte[] bytes = new byte[ size >>> 1 ];

        for( int i = 0, j = 0; i < size; j++ )
        {
            bytes[ j ] = Byte.parseByte( filename.substring( i, i + 2 ), 16 );
            i +=2;
        }

        return new String( bytes );
	}
	
	/**
	 * ���������ʱ����󣬼����ʱ����ڵ�ĳһ�����ʼʱ��
	 * Ҳ���� yyyy-MM-dd 00:00:00:000
	 * @param one_day
	 * @return
	 */
	public static Date calDayStart(Date one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(one_day) ;
		cal.set(Calendar.HOUR_OF_DAY, 0) ;
		cal.set(Calendar.MINUTE, 0) ;
		cal.set(Calendar.SECOND, 0) ;
		cal.set(Calendar.MILLISECOND, 0) ;
		
		return cal.getTime();
	}
	
	public static Date calDayStart(Calendar one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTimeInMillis(one_day.getTimeInMillis()) ;
		
		cal.set(Calendar.HOUR_OF_DAY, 0) ;
		cal.set(Calendar.MINUTE, 0) ;
		cal.set(Calendar.SECOND, 0) ;
		cal.set(Calendar.MILLISECOND, 0) ;
		
		return cal.getTime();
	}
	
	/**
	 * ���������ʱ����󣬼����ʱ����ڵ�ĳһ��Ľ���ʱ��
	 * Ҳ���� yyyy-MM-dd 23:59:59:999
	 * @param one_day
	 * @return
	 */
	public static Date calDayEnd(Date one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(one_day) ;
		cal.set(Calendar.HOUR_OF_DAY, 23) ;
		cal.set(Calendar.MINUTE, 59) ;
		cal.set(Calendar.SECOND, 59) ;
		cal.set(Calendar.MILLISECOND, 999) ;
		
		return cal.getTime();
	}
	
	
	public static Date calDayEnd(Calendar one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTimeInMillis(one_day.getTimeInMillis()) ;
		cal.set(Calendar.HOUR_OF_DAY, 23) ;
		cal.set(Calendar.MINUTE, 59) ;
		cal.set(Calendar.SECOND, 59) ;
		cal.set(Calendar.MILLISECOND, 999) ;
		
		return cal.getTime();
	}
	
	public static Date calWeekStart(Date one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(one_day) ;
		cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY) ;
		cal.set(Calendar.HOUR_OF_DAY, 0) ;
		cal.set(Calendar.MINUTE, 0) ;
		cal.set(Calendar.SECOND, 0) ;
		cal.set(Calendar.MILLISECOND, 0) ;
		
		return cal.getTime();
	}
	
	public static Date calWeekEnd(Date one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(one_day) ;
		cal.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY) ;
		cal.set(Calendar.HOUR_OF_DAY, 23) ;
		cal.set(Calendar.MINUTE, 59) ;
		cal.set(Calendar.SECOND, 59) ;
		cal.set(Calendar.MILLISECOND, 999) ;
		
		return cal.getTime();
	}
	
	
	public static Date calMonthStart(Date one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(one_day) ;
		cal.set(Calendar.DAY_OF_MONTH,1) ;
		cal.set(Calendar.HOUR_OF_DAY, 0) ;
		cal.set(Calendar.MINUTE, 0) ;
		cal.set(Calendar.SECOND, 0) ;
		cal.set(Calendar.MILLISECOND, 0) ;
		
		return cal.getTime();
	}
	
	public static Date calMonthEnd(Date one_day)
	{
		if(one_day==null)
			return null ;
		
		Calendar cal = Calendar.getInstance() ;
		cal.setTime(one_day) ;
		cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH)) ;
		cal.set(Calendar.HOUR_OF_DAY, 23) ;
		cal.set(Calendar.MINUTE, 59) ;
		cal.set(Calendar.SECOND, 59) ;
		cal.set(Calendar.MILLISECOND, 999) ;
		
		return cal.getTime();
	}
	
	/**
	 * �������·��
	 * @param parent_p ��·��
	 * @param p ������·�� /aa/bb��ʾ�Ѿ��Ǿ���·��  ./aa/bb aa/bb ��ǰ·�� ../��һ��·��
	 * @return
	 */
	public static String calAbsPath(String parent_p,String p)
	{
		if(p.startsWith("/"))
			return p ;
		
		if(!parent_p.endsWith("/"))
			parent_p += "/";
		
		if(p.startsWith("./"))
		{
			return parent_p + p.substring(2) ;
		}
		
		StringTokenizer st = new StringTokenizer(parent_p,"/\\") ;
		ArrayList<String> pps = new ArrayList<String>() ;
		while(st.hasMoreTokens())
			pps.add(st.nextToken()) ;
	
		if(p.startsWith("../"))
		{
			while(p.startsWith("../"))
			{
				p = p.substring(3) ;
				if(pps.size()>0)
					pps.remove(pps.size()-1) ;
			}
			
			StringBuilder sb = new StringBuilder() ;
			sb.append("/") ;
			for(String s:pps)
			{
				sb.append(s).append("/") ;
			}
			
			sb.append(p) ;
			
			return sb.toString() ;
		}
		
		return parent_p + p ;
	}
	
	/**
	 * ���ݵ�ǰurl������ڶ���������·��
	 * @param parent_p
	 * @param p
	 * @return
	 */
	public static String calAbsHttpUrl(String cur_url,String p)
	{
		if(p.toLowerCase().startsWith("http"))
			return p ;
		
		//StringBuilder sb = new StringBuilder() ;
		
		if(!cur_url.toLowerCase().startsWith("http"))
		{
			cur_url="http://"+cur_url ;
		}
		
		String url_prefix = null ;
		String parent_p = "/" ;
		int k = cur_url.indexOf('/',8);
		if(k<=0)
		{
			url_prefix = cur_url ;
		}
		else
		{
			url_prefix = cur_url.substring(0,k) ;
			parent_p = cur_url.substring(k) ;
			if(!parent_p.endsWith("/"))
			{
				int s = parent_p.lastIndexOf('/') ;
				if(s>0)
					parent_p = parent_p.substring(0,s) ;
			}
		}
		
		if(p.startsWith("./"))
		{
			return parent_p + p.substring(2) ;
		}
		
		StringTokenizer st = new StringTokenizer(parent_p,"/\\") ;
		ArrayList<String> pps = new ArrayList<String>() ;
		while(st.hasMoreTokens())
			pps.add(st.nextToken()) ;
	
		if(p.startsWith("../"))
		{
			while(p.startsWith("../"))
			{
				p = p.substring(3) ;
				if(pps.size()>0)
					pps.remove(pps.size()-1) ;
			}
			
			StringBuilder sb = new StringBuilder() ;
			sb.append("/") ;
			for(String s:pps)
			{
				sb.append(s).append("/") ;
			}
			
			sb.append(p) ;
			
			return sb.toString() ;
		}
		
		return url_prefix+parent_p + p ;
	}
	
	/**
	 * ����һ���ķָ���з�֧����
	 * @param str
	 * @param delimi
	 * @return
	 */
	public static List<String> splitStrWith(String str,String delimi)
	{
		if(isNullOrEmpty(str))
			return null ;
		
		ArrayList<String> rets = new ArrayList<String>() ;
		StringTokenizer st = new StringTokenizer(str,delimi) ;
		while(st.hasMoreTokens())
			rets.add(st.nextToken()) ;
		
		return rets ;
	}
	
	/**
	 * ����url�����ַ���
	 * ���磺aa=bb&xx=1
	 * @param urlpstr
	 * @return
	 */
	public static HashMap<String,String> parseUrlParamStr(String urlpstr)
	{
		if(urlpstr==null)
			return null ;
		HashMap<String,String> ret = new HashMap<String,String>() ;
		StringTokenizer st = new StringTokenizer(urlpstr,"&") ;
		while(st.hasMoreTokens())
		{
			String tmps = st.nextToken() ;
			int i = tmps.indexOf('=') ;
			if(i<=0)
				ret.put(tmps,"") ;
			else
				ret.put(tmps.substring(0,i), tmps.substring(i+1)) ;
		}
		return ret ;
	}
	/**
	 * ת���ַ������ʺϵ��ļ�����
	 * @param fn
	 * @return
	 */
	public static String fitFileName(String fn)
	{
		if(fn==null)
			return null;
		
		fn = fn.replace("/","");
		fn = fn.replace("\\","");
		fn = fn.replace(":","");
		fn = fn.replace("*","");
		fn = fn.replace("?","");
		fn = fn.replace("\"","");
		fn = fn.replace("<","");
		fn = fn.replace(">","");
		fn = fn.replace("|","");
		
		return fn ;
	}
	
	public static void main(String[] args) throws Exception
	{
		//String tmps = "http://babelfish.altavista.com/babelfish/trurl_pagecontent?lp=zh_en&url=http%3A%2F%2Fwww.souln.cn" ;
		//System.out.println(URLEncoder.encode(tmps,"UTF-8")) ;
		try
		{
			System.out.println(">>"+encodeKeyToFileName("Mail1218364208991-2")) ;
			System.out.println("res="+Convert.toDecimalDigitsStr(0.34543333, 2));
			System.out.println("res="+Convert.toDecimalDigitsStr(324.2321, 3));
			System.out.println("res="+Convert.toDecimalDigitsStr(234658.34543333, 4));
			System.out.println("res="+Convert.toDecimalDigitsStr(234658.34543778E5, 2));
		}
		catch(Exception eee)
		{
			eee.printStackTrace() ;
		}
	}
}
