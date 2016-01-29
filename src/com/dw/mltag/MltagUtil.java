package com.dw.mltag;

import java.util.Hashtable;
import java.util.StringTokenizer;
/**
 * <p>Title: 工作流引擎</p>
 * <p>Description: 提供一些辅助的方法</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * Created on 2004-7-22
 * @author Jason Zhu
 * @version 2.0
 */
public class MltagUtil
{
	static Hashtable s2charMap = new Hashtable();
	static
	{
		s2charMap.put("lt",new char[]{'<'});
		s2charMap.put("gt",new char[]{'>'});
		s2charMap.put("apos",new char[]{'\''});
		s2charMap.put("amp",new char[]{'&'});
		s2charMap.put("quot",new char[]{'\"'});
		s2charMap.put("nbsp",new char[]{' '});
	}
	
	public static char getDecodeChar(String s)
	{
		char[] cs = (char[])s2charMap.get(s);
		if(cs!=null)
			return cs[0];
		
		if(!s.startsWith("#"))
			return (char)-1 ;
		
		try
		{
			return (char)Integer.parseInt(s.substring(1)) ;
		}
		catch(Exception ee)
		{
			return (char)-1 ;
		}
	}
	
	public static String getEncodeStr(char c)
	{
		switch (c)
		{
			case '<':
				return "lt";
			case '>':
				return "gt";
			case '&':
				return "amp";
			case '\'':
				return "apos";
			case '\"':
				return "quot";
			case ' ':
				return "nbsp";
			default:
				return "#"+c ;
		}
	}
	//static String entitystr = "><&\'\"\r\n" ;
	static String entitystr = "><&\'\"" ;
	
	public static String xmlEncoding(String input)
	{
		return xmlEncoding(input,entitystr);
	}
	/**
	 * 把结果中的涉及实体引用的字符转换为实体
	 */
	public static String xmlEncoding(String input,String delimiter)
	{
		if(input==null||input.equals(""))
			return input;
		
		delimiter+= '&' ;
		
		StringTokenizer tmpst = new StringTokenizer(input,delimiter,true);
		StringBuffer tmpsb = new StringBuffer(input.length()+100);
		String tmps = null ;
		while (tmpst.hasMoreTokens())
		{
			tmps = tmpst.nextToken();
			if ( tmps.length()==1 && delimiter.indexOf(tmps)>=0 )
			{
				tmpsb.append('&').append(getEncodeStr(tmps.charAt(0))).append(';');
				/*switch (tmps.charAt(0))
				{
				case '<':
					tmpsb.append("&lt;");
					break ;
				case '>':
					tmpsb.append("&gt;");
					break ;
				case '&':
					tmpsb.append("&amp;");
					break ;
				case '\'':
					tmpsb.append("&apos;");
					break ;
				case '\"':
					tmpsb.append("&quot;");
					break ;
				case '\n':
					tmpsb.append("&#10;") ;
					break;
				case '\r':
					tmpsb.append("&#13;") ;
					break;
				}*/
			}
			else
			{
				tmpsb.append (tmps);
			}
		}
		
		return tmpsb.toString();
	}
	
	
	
	public static String xmlDecoding(String input)
	{
		return xmlDecoding(input,entitystr);
	}
	
	public static String xmlDecoding(String input,String delimiter)
	{
		if(input==null||input.equals(""))
			return input ;
		
		delimiter+= '&' ;
		
		StringBuffer sb = new StringBuffer(input.length());
		int p = 0 ;
		while(true)
		{
			int a = input.indexOf('&', p);
			if(a<0)
			{
				sb.append(input.substring(p));
				break ;
			}
			else
			{
				sb.append(input.substring(p,a));
				p = a ;
				int b = input.indexOf(';',p) ;
				if(b<0)
				{
					sb.append(input.substring(p));
					break ;
				}
				else
				{
					String s = input.substring(p+1,b) ;
					char cc = getDecodeChar(s) ;
					//System.out.println(s+">-->"+cc);
					if(delimiter.indexOf(cc)<0)
					{
						sb.append('&').append(s).append(';');
						p = b + 1 ;
						continue ;
					}
					else
					{
						sb.append(cc);
						p = b +1 ;
						continue ;
					}
				}
			}
		}
		return sb.toString();
	}
	
	public static String xmlEncoding(String input, String delimiter, boolean enablecoding)
    {
        if (!enablecoding)
            return input ;

        if(input==null||input.equals(""))
            return input;
	
        delimiter+= '&' ;
	
        StringTokenizer tmpst = new StringTokenizer(input,delimiter,true);
        StringBuilder tmpsb = new StringBuilder(input.length()+100);
        String tmps = null ;
        while (tmpst.hasMoreTokens())
        {
            tmps = tmpst.nextToken();
            if (tmps.length()==1 && delimiter.indexOf(tmps)>=0 )
            {
                tmpsb.append('&').append(getEncodeStr(tmps.charAt(0))).append(';');
            }
            else
            {
                tmpsb.append (tmps);
            }
        }
	
        return tmpsb.toString();
    }
	
	
//	private static String getEncodeStr(char c)
//    {
//        switch (c)
//        {
//            case '<':
//                return "lt";
//            case '>':
//                return "gt";
//            case '&':
//                return "amp";
//            case '\'':
//                return "apos";
//            case '\"':
//                return "quot";
//            case ' ':
//                return "nbsp";
//            default:
//                return "#"+c ;
//        }
//    }
	
	public static String xmlDecoding(String input,String delimiter,boolean enablecoding)
    {
        if (!enablecoding)
            return input ;

        if(input==null||input=="")
            return input ;
	
        delimiter+= '&' ;
	
        StringBuilder sb = new StringBuilder(input.length());
        int p = 0 ;
        while(true)
        {
            int a = input.indexOf('&', p);
            if(a<0)
            {
                sb.append(input.substring(p));
                break ;
            }
            else
            {
                sb.append(input.substring(p,a-p));
                p = a ;
                int b = input.indexOf(';',p) ;
                if(b<0)
                {
                    sb.append(input.substring(p));
                    break ;
                }
                else
                {
                    String s = input.substring(p+1,b-(p+1)) ;

                    char cc = getDecodeChar(s) ;
                    //System.out.println(s+">-->"+cc);
                    if(delimiter.indexOf(cc)<0)
                    {
                        sb.append('&').append(s).append(';');
                        p = b + 1 ;
                        continue ;
                    }
                    else
                    {
                        sb.append(cc);
                        p = b +1 ;
                        continue ;
                    }
                }
            }
        }
        return sb.toString();
    }

	
	public static void main(String[] args)
	{
		String dem = "\"";
		String[] sss = new String[]{
				"asdfasdf asdfgasdfa\nsd",
				"asdfasdf asdfg&asdfa\nsd",
				"asdfasdf asd\'fga&s&dfasd\"",
				"&nbsp;"
		};
		
		for(int i = 0 ; i < sss.length ; i ++)
		{
			System.out.println(sss[i]);
			String es = xmlEncoding(sss[i],dem);
			String ds = xmlDecoding(es,dem);
			System.out.println("--->"+es);
			System.out.println("<---"+ds+"   eq=="+sss[i].equals(ds));
		}
	}
}
