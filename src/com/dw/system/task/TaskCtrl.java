package com.dw.system.task;

import java.io.*;
import java.util.*;

import com.dw.system.*;
import com.dw.system.xmldata.*;

/**
 * 对任务进行控制的数据存储
 * @author Jason Zhu
 */
public class TaskCtrl implements IXmlDataable
{
	/**
	 * 从配置文件中装载任务控制
	 *
	 */
	static TaskCtrl loadTaskCtrl() throws Exception
	{
		String fp = AppConfig.getDataDirBase()+"task/task_ctrl.xml";
		File f = new File(fp) ;
		if(!f.exists())
			return null ;
		
		XmlData xd = XmlData.readFromFile(fp) ;
		TaskCtrl tc = new TaskCtrl() ;
		tc.fromXmlData(xd) ;
		return tc ;
	}
	
	
	static void saveTaskCtrl(TaskCtrl tc) throws Exception
	{
		String fp = AppConfig.getDataDirBase()+"task/task_ctrl.xml";
		
		if(tc==null)
		{
			File f = new File(fp) ;
			if(f.exists())
				f.delete() ;
			return  ;
		}
		
		XmlData.writeToFile(tc.toXmlData(), fp) ;
	}
	
	
	public static class TaskCtrlItem implements IXmlDataable
	{
		String taskId = null ;
		
		boolean bRun = true ;//default is run task
		
		public TaskCtrlItem()
		{}
		
		public TaskCtrlItem(String taskid,boolean br)
		{
			taskId = taskid ;
			bRun = br ;
		}
		
		public String getTaskId()
		{
			return taskId ;
		}
		
		public boolean isRun()
		{
			return bRun ;
		}

		public XmlData toXmlData()
		{
			XmlData xd = new XmlData() ;
			xd.setParamValue("task_id", taskId) ;
			xd.setParamValue("is_run", bRun) ;
			return xd;
		}

		public void fromXmlData(XmlData xd)
		{
			taskId = xd.getParamValueStr("task_id") ;
			bRun = xd.getParamValueBool("is_run", true) ;
		}
	}
	
	
	
	HashMap<String,TaskCtrlItem> taskid2tc = new HashMap<String,TaskCtrlItem>() ;
	
	public TaskCtrl()
	{}
	
	
	public TaskCtrl(ArrayList<TaskCtrlItem> tcis)
	{
		for(TaskCtrlItem tci:tcis)
		{
			setTaskCtrlItem(tci) ;
		}
	}
	
	public TaskCtrlItem getTaskCtrlItem(String taskid)
	{
		return taskid2tc.get(taskid) ;
	}
	
	public void setTaskCtrlItem(TaskCtrlItem tci)
	{
		taskid2tc.put(tci.getTaskId(), tci) ;
	}
	
	/**
	 * 判断某个任务是否被控制运行
	 * @param taskid
	 * @return
	 */
	public boolean checkTaskCtrlRun(String taskid)
	{
		TaskCtrlItem tci = getTaskCtrlItem(taskid) ;
		if(tci==null)
			return true ;
		
		return tci.isRun() ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		List<XmlData> xds = xd.getOrCreateSubDataArray("ctrl_items") ;
		for(TaskCtrlItem tci:taskid2tc.values())
		{
			xds.add(tci.toXmlData()) ;
		}
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		if(xd==null)
			return ;
		
		List<XmlData> xds = xd.getSubDataArray("ctrl_items") ;
		if(xds!=null)
		{
			for(XmlData tmpxd:xds)
			{
				TaskCtrlItem tci = new TaskCtrlItem() ;
				tci.fromXmlData(tmpxd) ;
				
				setTaskCtrlItem(tci) ;
			}
		}
	}
}
