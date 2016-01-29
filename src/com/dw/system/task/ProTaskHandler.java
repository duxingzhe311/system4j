package com.dw.system.task;

import java.util.List;

import com.dw.system.xmldata.XmlData;

public class ProTaskHandler implements TaskHandler
{

	public String getHandlerName()
	{
		return "pro_task";
	}

	public List<TaskParam> getTaskParams()
	{
		return null;
	}

	public boolean checkTask(Task t,StringBuilder failedreson)
	{
		return true ;
	}
	
	public void handleTask(Task t) throws Exception
	{
		
	}

	public boolean isSatisfy(Task t)
	{
		return false;
	}

	public String getHandlerStatusInfo()
	{
		return "" ;
	}
}
