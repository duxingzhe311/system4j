package com.dw.comp;

import java.util.*;

import com.dw.system.loader.*;
import com.dw.system.logger.*;
import com.dw.system.task.TaskTable;

public class CompManager
{
	private static ILogger log = LoggerManager.getLogger(CompManager.class.getCanonicalName()) ;
	
	private static CompManager compMgr = null;

	private static Object locker = new Object();

	public static CompManager getInstance()
	{
		if (compMgr != null)
			return compMgr;

		synchronized (locker)
		{
			if (compMgr != null)
				return compMgr;

			compMgr = new CompManager();
			return compMgr;
		}
	}
	
	private Hashtable<String,AbstractComp> name2comp = new Hashtable<String,AbstractComp>() ;
	//private HashSet<ClassLoader> compCls = new HashSet<ClassLoader>() ;
	private Hashtable<String,AppInfo> contextName2CompInfo = new Hashtable<String,AppInfo>() ;
	
	private ArrayList<ICompListener> compLis = new ArrayList<ICompListener>() ;
	
	//private HashMap<String,ArrayList<CompTask>> comp2Tasks = new HashMap<String,ArrayList<CompTask>>() ;
	//TaskTable taskTable = new TaskTable() ;

	private CompManager()
	{

	}
	
	public void registerCompListener(ICompListener cl)
	{
		if(!compLis.contains(cl))
			compLis.add(cl);
	}
	
//	public TaskTable getTaskTable()
//	{
//		return taskTable ;
//	}
//	/**
//	 * 注册构件任务
//	 * 每个构件的任务按照构件名称保存,可以保证同一个构件在重新发布后
//	 * 里面的任务内容能够使用最新的
//	 * @param compname
//	 * @param ct
//	 */
//	public void registerTask(String compname,ArrayList<CompTask> cts)
//	{
//		synchronized(this)
//		{
//			if(cts==null)
//			{
//				comp2Tasks.remove(compname);
//			}
//			else
//			{
//				comp2Tasks.put(compname, cts);
//			}
//		}
//	}
	
	public void fireAppFinding(AppInfo ci)
	{
		contextName2CompInfo.put(ci.getContextName(), ci);
		System.out.println(">>>find comp="+ci.toString());
		for(ICompListener cl:compLis)
		{
			cl.onCompFinding(ci);
		}
	}
	
	public AppInfo getAppInfo(String contextn)
	{
		return contextName2CompInfo.get(contextn);
	}
	
	public AppInfo[] getAllAppInfo()
	{
		AppInfo[] rets = new AppInfo[contextName2CompInfo.size()];
		contextName2CompInfo.values().toArray(rets);
		return rets;
	}
	
	public AppInfo getAppInfo(ClassLoader cl)
	{
		for(AppInfo ai:contextName2CompInfo.values())
		{
			if(ai.getRelatedClassLoader()==cl)
				return ai ;
		}
		return null ;
	}
	
	public AppInfo getAppInfo(Class c)
	{
		return getAppInfo(c.getClassLoader()) ;
	}
	
	public void registerComp(ClassLoader cl,AbstractComp ac)
	{
		log.info("\nLoad Comp="+ac.getCompName());
		name2comp.put(ac.getCompName(), ac);
	}
	
	public String[] getCompNames()
	{
		String[] rets = new String[name2comp.size()];
		return name2comp.keySet().toArray(rets);
	}
	
	public AbstractComp getComp(String name)
	{
		return name2comp.get(name);
	}

	public static void main(String[] args) throws Exception
	{
		CompClassLoader mail_ccl = CompLoader.getCompClassLoader(
				"E:/working/biz_comps", "mail");
		CompClassLoader tcomp_ccl = CompLoader.getCompClassLoader(
				"E:/working/biz_comps", "t_comp1");
		CompClassLoader tdoc = CompLoader.getCompClassLoader(
				"E:/working/biz_comps", "doc_mgr");

		Class c1 = mail_ccl.loadClass("TC1");
		Class c2 = tcomp_ccl.loadClass("TC1");
		Class c3 = tdoc.loadClass("TC1");

		AbstractComp ac1 = (AbstractComp) c1.newInstance();
		AbstractComp ac2 = (AbstractComp) c2.newInstance();
		AbstractComp ac3 = (AbstractComp) c3.newInstance();

		System.out.println("1=" + ac1.getCompName());
		System.out.println("2=" + ac2.getCompName());
		System.out.println("3=" + ac3.getCompName());
	}
}
