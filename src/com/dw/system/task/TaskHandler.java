package com.dw.system.task ;

import java.io.* ;
import java.util.* ;

import com.dw.system.xmldata.XmlData;

/**
 * 处理具体调度事务的处理器。它会被{@see TaskSchedule}调度使用。
 */
public interface TaskHandler
{
	public String getHandlerName() ;
	
	public List<TaskParam> getTaskParams() ;
	
	public void handleTask(Task t) throws Exception ;
	
	public boolean checkTask(Task t,StringBuilder failedreson) ;
	
	public boolean isSatisfy(Task t) ;
	
	public String getHandlerStatusInfo() ;
	
}