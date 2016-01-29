package com.dw.system.task;

import java.io.* ;
import java.util.* ;


/**
 * 任务及调度出错类
 */
public class TaskException extends RuntimeException
{
	public TaskException (String str)
	{
		super(str) ;
	}
	
	public String toString()
	{
		return super.toString() ;
	}
}