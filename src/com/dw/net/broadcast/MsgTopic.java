package com.dw.net.broadcast;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * 消息区分大小写<br/>
 * 消息主题规则：以点分割的字符串，首尾有点或两个点在一起都表示一个点 .xxx..yyy.zzz. =  xxx.yyy.zzz (最小单位是字符串)<br/>
 * 另外有两个通配符 * 和 ? :<b>字符串中不能包含这两个字符</b>,
 * <b>另外，一个主题中不能有超过一个以上的*统配符，并且*只能做后缀</b></br>
 *    ? 代表一个字符串<br/>
 *    * 代表相关位置的一个或多个字符串<br/>
 * 例如：xxx.yyy.zzz xxx.* xxx.zzz  xxx.yyy.? * ? ?.?.* 都是合法字符串<br/>
 *       而：xxY*.zz   xx?.  aa..bb 等都是不合法字符串<br/>
 * 对于没有通配符的主题，只要判断相等就可以。
 * 带有通配符的主题，有如下匹配关系，因为匹配计算有方向性 发送----接收，但在计算时都是在
 * 接收端，所以可以分为  <b>被(本地)监听</b>  和 <b>被接收</b>  主题：<br/>
 *   很明显： * 匹配所有主题  ? 匹配没有
 * aa.bb.cc
 * 100.3.kk
 */
public class MsgTopic
{
	private Hashtable topicBuf = new Hashtable();

	static String allListenTopic = "*"; //"<!@#$%^&*()>" ;
	/**
	 * 从byte流中取出主体的内容
	 * <b>（注）输入流的前面必须包含由appendToMsg添加的头信息</b>
	 */
	public MsgTopic()
	{
		//topicBuf.put (allListenTopic,"");
	}

	/**
	 * 判断某一主题是否合法
	 * @return String[] 如果不合法=null , 否则返回分割后的内容
	 */
	public static String[] isValidate(String strtopic)
	{
		if (strtopic == null || strtopic.equals(""))
		{
			return null;
		}
		StringTokenizer tmpst = new StringTokenizer(strtopic, ".");
		int c = tmpst.countTokens();
		String[] rets = new String[c];
		for (int i = 0; i < c; i++)
		{
			rets[i] = tmpst.nextToken();
			//在字符串中包含* ?
			if (rets[i].length() > 1 &&
				(rets[i].indexOf('*') >= 0 || rets[i].indexOf('?') >= 0))
			{
				return null;
			}

			if (rets[i].length() == 1 && rets[i].charAt(0) == '*' &&
				i != (c - 1))
			{
				return null;
			}
		}
		return rets;
	}

	public static String[] divTopic(String strtopic)
	{
		StringTokenizer tmpst = new StringTokenizer(strtopic, ".");
		int c = tmpst.countTokens();
		String[] rets = new String[c];
		for (int i = 0; i < c; i++)
		{
			rets[i] = tmpst.nextToken();
		}
		return rets;
	}

	synchronized public void add(String strtopic)
	{
		String[] tmps = isValidate(strtopic);
		if (tmps == null)
		{
			throw new RuntimeException("Topic [" + strtopic + "] is invalid!");
		}
		topicBuf.put(strtopic, tmps);
	}

	synchronized public void remove(String strtopic)
	{
		topicBuf.remove(strtopic);
	}

	/**
	 * 判断输入主体是否匹配当前对象所包含的主题集
	 */
	public boolean isMatch(String strtopic)
	{
		String[] intmps = isValidate(strtopic);
		if (intmps == null) //不合法输入主题
		{
			return false;
		}

		String[] exts = null;
		for (Enumeration en = topicBuf.elements(); en.hasMoreElements(); )
		{
			exts = (String[]) en.nextElement();
			if (isMatch(intmps, exts))
			{
				return true;
			}
		}
		if("*".equals(strtopic))
			return true ;

		return false;
	}

	static public boolean testMatch(String strtopic1, String strtopic2)
	{
		String[] intmps1 = isValidate(strtopic1);
		if (intmps1 == null) //不合法输入主题
		{
			throw new RuntimeException("[" + strtopic1 + "] is invalid!");
		}

		String[] intmps2 = isValidate(strtopic2);
		if (intmps2 == null) //不合法输入主题
		{
			throw new RuntimeException("[" + strtopic2 + "] is invalid!");
		}
		return isMatch(intmps1, intmps2);
	}

	/**
	 * 具体判断两个主题是否匹配
	 */
	static private boolean isMatch(String[] topic1, String[] topic2)
	{
		int last1 = topic1.length - 1;
		int last2 = topic2.length - 1;
		boolean suffix1 = false, suffix2 = false;
		if (topic1[last1].length() == 1 && topic1[last1].charAt(0) == '*')
		{
			suffix1 = true;
		}
		if (topic2[last2].length() == 1 && topic2[last2].charAt(0) == '*')
		{
			suffix2 = true;
		}
		if (!suffix1 && !suffix2)
		{ //两者都没有后缀
			if (last1 != last2)
			{
				return false;
			}

			for (int i = 0; i <= last1; i++)
			{
				if (topic1[i].length() == 1 && topic1[i].charAt(0) == '?')
				{
					continue;
				}
				if (topic2[i].length() == 1 && topic2[i].charAt(0) == '?')
				{
					continue;
				}
				if (!topic1[i].equals(topic2[i]))
				{
					return false;
				}
			}

			return true;
		}

		if (suffix1 && suffix2)
		{ //两者都有后缀
			int min = getMin(last1, last2);
			for (int i = 0; i < min; i++)
			{
				if (topic1[i].length() == 1 && topic1[i].charAt(0) == '?')
				{
					continue;
				}
				if (topic2[i].length() == 1 && topic2[i].charAt(0) == '?')
				{
					continue;
				}
				if (!topic1[i].equals(topic2[i]))
				{
					return false;
				}
			}

			return true;
		}

		if (!suffix1 && suffix2)
		{
			if (last1 < last2 - 1)
			{
				return false;
			}

			for (int i = 0; i < last2; i++)
			{
				if (topic1[i].length() == 1 && topic1[i].charAt(0) == '?')
				{
					continue;
				}
				if (topic2[i].length() == 1 && topic2[i].charAt(0) == '?')
				{
					continue;
				}
				if (!topic1[i].equals(topic2[i]))
				{
					return false;
				}
			}

			return true;
		}

		if (suffix1 && !suffix2)
		{
			if (last2 < last1 - 1)
			{
				return false;
			}

			for (int i = 0; i < last1; i++)
			{
				if (topic1[i].length() == 1 && topic1[i].charAt(0) == '?')
				{
					continue;
				}
				if (topic2[i].length() == 1 && topic2[i].charAt(0) == '?')
				{
					continue;
				}
				if (!topic1[i].equals(topic2[i]))
				{
					return false;
				}
			}

			return true;
		}

		return false;
	}

	static private int getMin(int a, int b)
	{
		if (a > b)
		{
			return b;
		}
		else
		{
			return a;
		}
	}

	/*
	  static private boolean isMatch (String[] topic1,String[] topic2)
	  {
		int i = 0 , j = 0 ;
		while ( i < topic1.length || j < topic2.length )
		{
			 if ("*".equals(topic1[i]))
			 {
				  i ++ ;
				  if (i==topic1.length)
					   return true ;
				  continue ;
			 }
			 else if ("?".equals(topic1[i]))
			 {
				  if ("*".equals(topic2[j]))
				  {
					   i ++ ;
					   if (i==topic1.length)
							return true ;
					   continue ;
				  }
				  if (i>0&&"*".equals(topic1[i-1]))
				  {
						   j ++ ;
						   if (j==topic2.length)
								return true ;
						   continue ;
					  }
				  i ++ ;
				  j ++ ;
				  if (i==topic1.length&&j==topic2.length)
					   return true ;
				  else if (i==topic1.length||j==topic2.length)
					   return false ;
				  continue ;
			 }
			 else
			 {//topic1[i] 不是通配符
				  if ("*".equals(topic2[j]))
				  {
					   j ++ ;
					   if (j==topic2.length)
							return true ;
					   continue ;
				  }
				  else if ("?".equals(topic2[j]))
				  {
					   if (j>0&&"*".equals(topic2[j-1]))
					   {
							i ++ ;
							if (i==topic1.length)
									 return true ;
							continue ;
					   }
					   i ++ ;
					   j ++ ;
					   if (i==topic1.length&&j==topic2.length)
							return true ;
					   else if (i==topic1.length||j==topic2.length)
							return false ;
					   continue ;
				  }
				  else
				  {//两者都是字符串
					   if (topic1[i].equals(topic2[j]))
					   {
							i ++ ;
							j ++ ;
							if (i==topic1.length&&j==topic2.length)
								 return true ;
							else if (i==topic1.length||j==topic2.length)
								 return false ;
							continue ;
					   }
					   else
					   {
							if (i>0&&"*".equals(topic1[i-1]))
							{
								 j ++ ;
								 if (j==topic2.length)
									  return true ;
								 continue ;
							}
							else if (j>0&&"*".equals(topic2[j-1]))
							{
								 i ++ ;
								 if (i==topic1.length)
									  return true ;
								 continue ;
							}
							else
								 return false ;
					   }
				  }
			 }
		}
		return true ;
	  }
	 */
	public String getTopicsStr()
	{
		int s = topicBuf.size();
		if (s <= 0)
		{
			return null;
		}

		String tmps = "";
		for (Enumeration en = topicBuf.keys(); en.hasMoreElements(); )
		{
			tmps += ("|" + (String) en.nextElement());
		}
		return tmps;
	}

	public Enumeration getAll()
	{
		return topicBuf.keys();
	}

	public void list()
	{
		System.out.println("*******************");
		for (Enumeration en = topicBuf.keys(); en.hasMoreElements(); )
		{
			System.out.println("->" + en.nextElement());
		}
		System.out.println("-------------------");
	}

	public static void main(String args[])
	{
		System.out.println("[" + args[0] + "]");
		System.out.println("[" + args[1] + "]");
		System.out.println(testMatch(args[0], args[1]));
	}
}