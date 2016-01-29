package com.dw.system.task ;

import java.io.* ;
import java.util.* ;

import com.dw.system.xmldata.XmlData;

/**
 * ��������������Ĵ����������ᱻ{@see TaskSchedule}����ʹ�á�
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