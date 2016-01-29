package com.dw.net.broadcast;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * ��Ϣ���ִ�Сд<br/>
 * ��Ϣ��������Ե�ָ���ַ�������β�е����������һ�𶼱�ʾһ���� .xxx..yyy.zzz. =  xxx.yyy.zzz (��С��λ���ַ���)<br/>
 * ����������ͨ��� * �� ? :<b>�ַ����в��ܰ����������ַ�</b>,
 * <b>���⣬һ�������в����г���һ�����ϵ�*ͳ���������*ֻ������׺</b></br>
 *    ? ����һ���ַ���<br/>
 *    * �������λ�õ�һ�������ַ���<br/>
 * ���磺xxx.yyy.zzz xxx.* xxx.zzz  xxx.yyy.? * ? ?.?.* ���ǺϷ��ַ���<br/>
 *       ����xxY*.zz   xx?.  aa..bb �ȶ��ǲ��Ϸ��ַ���<br/>
 * ����û��ͨ��������⣬ֻҪ�ж���ȾͿ��ԡ�
 * ����ͨ��������⣬������ƥ���ϵ����Ϊƥ������з����� ����----���գ����ڼ���ʱ������
 * ���նˣ����Կ��Է�Ϊ  <b>��(����)����</b>  �� <b>������</b>  ���⣺<br/>
 *   �����ԣ� * ƥ����������  ? ƥ��û��
 * aa.bb.cc
 * 100.3.kk
 */
public class MsgTopic
{
	private Hashtable topicBuf = new Hashtable();

	static String allListenTopic = "*"; //"<!@#$%^&*()>" ;
	/**
	 * ��byte����ȡ�����������
	 * <b>��ע����������ǰ����������appendToMsg��ӵ�ͷ��Ϣ</b>
	 */
	public MsgTopic()
	{
		//topicBuf.put (allListenTopic,"");
	}

	/**
	 * �ж�ĳһ�����Ƿ�Ϸ�
	 * @return String[] ������Ϸ�=null , ���򷵻طָ�������
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
			//���ַ����а���* ?
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
	 * �ж����������Ƿ�ƥ�䵱ǰ���������������⼯
	 */
	public boolean isMatch(String strtopic)
	{
		String[] intmps = isValidate(strtopic);
		if (intmps == null) //���Ϸ���������
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
		if (intmps1 == null) //���Ϸ���������
		{
			throw new RuntimeException("[" + strtopic1 + "] is invalid!");
		}

		String[] intmps2 = isValidate(strtopic2);
		if (intmps2 == null) //���Ϸ���������
		{
			throw new RuntimeException("[" + strtopic2 + "] is invalid!");
		}
		return isMatch(intmps1, intmps2);
	}

	/**
	 * �����ж����������Ƿ�ƥ��
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
		{ //���߶�û�к�׺
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
		{ //���߶��к�׺
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
			 {//topic1[i] ����ͨ���
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
				  {//���߶����ַ���
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