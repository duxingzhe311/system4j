package com.dw.comp;

import java.util.HashMap;

import com.dw.system.Convert;
import com.dw.system.task.*;
import com.dw.system.xmldata.XmlData;

/**
 * 构件中的任务
 * 
 * 继承本抽象类的基础是
 * @author Jason Zhu
 */
public class CompTask extends Task
{
	/**
	 * name=时效控制点定时报警
	 * title=
        action=/app_case_mgr/case_timelimit/timelimit_alert_task.action
        input=dx_/pn:int32
		interval=600000
	 * @param pms
	 * @return
	 */
	public static CompTask createFromProp(HashMap<String,String> pms)
		throws Exception
	{
		String taskname = pms.get("name");
		if(Convert.isNullOrEmpty(taskname))
			return null ;
		
		String title = pms.get("title");
		
		String actp = pms.get("action");
		if(Convert.isNullOrEmpty(actp))
			return null ;
		
		String strri = pms.get("interval");
		if(Convert.isNullOrEmpty(strri))
			return null ;
		
		long runri = -1 ;
		try
		{
			runri = Long.parseLong(strri);
		}
		catch(Exception eee)
		{
			runri = -1 ;
		}
		
		if(runri<=0)
			return null;
		
		String input = pms.get("input");
		XmlData xd = XmlData.parseFromUrlStr(input) ;
		
		return new CompTask(taskname,title,actp,runri,xd) ;
	}
	
	String name = null ;
	String title = null ;
	String actionPath = null ;
	long runInterval = -1 ;
	XmlData inputXd =null ;
	
	public CompTask(String name,String title,
			String act_path,long ri,XmlData inputxd)
	{
		if(Convert.isNullOrEmpty(name))
			throw new IllegalArgumentException("name cannot be null or empty!");
		
		this.name = name ;
		this.title = title ;
		if(Convert.isNullOrEmpty(this.title))
			this.title = name ;
		
		actionPath = act_path ;
		runInterval = ri ;
		inputXd = inputxd ;
	}
	
	public String getName()
	{
		return name ;
	}
	
	public String getTitle()
	{
		return title ;
	}
	
	public String getActionPath()
	{
		return actionPath ;
	}
	
	public long getRunInterval()
	{
		return runInterval ;
	}
	
	public XmlData getInputXmlData()
	{
		return inputXd ;
	}

	@Override
	public void run()
	{
		
	}
}
