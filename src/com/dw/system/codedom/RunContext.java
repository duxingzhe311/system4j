package com.dw.system.codedom;

import java.io.*;
import java.util.*;

/**
 * 代码运行一次的上下文
 * @author Jason Zhu
 */
public class RunContext
{
	protected Object defaultContextObj = null ;
	/**
	 * 在一个上下文中的全局变量
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
	 * 当代码运行到一个新的代码块时，应该调用该方法
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
	 * 得到当前代码块，或可以访问到的变量名称
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
	 * 设置当前堆栈中临时变量或全局的变量的值——前提：变量在上下文中已经存在
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
	 * 在当前堆栈或全局堆栈中增加变量及值
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
	 * 当代码运行离开代码块时，应该调用该方法对当前代码块进行消除
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
