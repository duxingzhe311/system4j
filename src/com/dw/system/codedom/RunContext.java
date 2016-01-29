package com.dw.system.codedom;

import java.io.*;
import java.util.*;

/**
 * ��������һ�ε�������
 * @author Jason Zhu
 */
public class RunContext
{
	protected Object defaultContextObj = null ;
	/**
	 * ��һ���������е�ȫ�ֱ���
	 */
	protected Hashtable<String,ValWrapper> globalVal = new Hashtable<String,ValWrapper>();
	
	protected Stack<Object> stack = new Stack<Object>();
	
	public RunContext()
	{
	}
	
	public RunContext(Hashtable gvals)
	{
		for (Enumeration en = gvals.keys(); en.hasMoreElements();)
		{
			String kn = (String) en.nextElement();
			Object v = gvals.get(kn);
			ValWrapper vw = new ValWrapper();
			vw.val = v;
			globalVal.put(kn,vw);
		}
	}
	
	public RunContext(String[] g_vars)
	{
		for (String vn:g_vars)
		{
			ValWrapper vw = new ValWrapper();
			
			globalVal.put(vn,vw);
		}
	}
	
	
	
	public void setDefaultContextObj(Object o)
	{
		defaultContextObj = o ;
	}
	
	public Object getDefaultContextObj()
	{
		return defaultContextObj ;
	}
	/**
	 * ���������е�һ���µĴ����ʱ��Ӧ�õ��ø÷���
	 * @return
	 */
	public Hashtable<String,ValWrapper> pushSubContext()
	{
		Hashtable<String,ValWrapper> ht = new Hashtable<String,ValWrapper>();
		stack.push(ht);
		return ht ;
	}
	
	
	public ValWrapper getValueWrapper(String var_name)
		throws Exception
	{
		int s = stack.size();
		for(int i = s - 1 ; i >= 0 ; i --)
		{
			Hashtable<String,ValWrapper> ht = (Hashtable<String,ValWrapper>)stack.elementAt(i);
			ValWrapper vw = ht.get(var_name);
			if(vw!=null)
				return vw;
		}
		
		return globalVal.get(var_name);
//		
//		ValWrapper vw = globalVal.get(var_name);
//		if(vw!=null)
//			return vw;
		
		//throw new RuntimeException("Cannot get var value with var name="+var_name);
	}
	
	/**
	 * �õ���ǰ����飬����Է��ʵ��ı�������
	 * @param var_name
	 * @return
	 */
//	public final Object getVarValue(String var_name)
//	{
//		ValWrapper vw = getValueWrapper(var_name) ;
//		if(vw==null)
//			return null ;
//		
//		return vw.val;
//	}
	
	/**
	 * ���õ�ǰ��ջ����ʱ������ȫ�ֵı�����ֵ����ǰ�᣺���������������Ѿ�����
	 * @param val_name
	 * @param v
	 */
	public void setValValue(String var_name,Object v)
	{
		int s = stack.size();
		for(int i = s - 1 ; i >= 0 ; i --)
		{
			Hashtable<String,ValWrapper> ht = (Hashtable<String,ValWrapper>)stack.elementAt(i);
			ValWrapper vw = ht.get(var_name);
			if(vw!=null)
			{
				vw.val = v ;
				return;
			}
		}
		
		ValWrapper vw = globalVal.get(var_name);
		if(vw!=null)
		{
			vw.val = v;
			return ;
		}
		
		throw new RuntimeException("Cannot get var value with var name="+var_name);
	}
	
	/**
	 * �ڵ�ǰ��ջ��ȫ�ֶ�ջ�����ӱ�����ֵ
	 * @param var_name
	 * @param val
	 * @return
	 */
	public void addVar(String var_name,Object val)
	{
		ValWrapper vw = new ValWrapper();
		vw.val = val;
		
		int s = stack.size();
		if(s>0)
		{
			Hashtable<String,ValWrapper> ht = (Hashtable<String,ValWrapper>)stack.peek();
			ht.put(var_name, vw);
			return;
		}
		
		globalVal.put(var_name,vw);
	}
	/**
	 * �����������뿪�����ʱ��Ӧ�õ��ø÷����Ե�ǰ������������
	 * @return
	 */
	public Hashtable<String,ValWrapper> popSubContext()
	{
		return (Hashtable<String,ValWrapper>)stack.pop();
	}
	
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Global Vars---------->\r\n") ;
		for(Map.Entry<String,ValWrapper> n2vw:globalVal.entrySet())
		{
			sb.append("  ").append(n2vw.getKey()).append('=').append(n2vw.getValue().val) ;
		}
		return sb.toString();
	}
}
