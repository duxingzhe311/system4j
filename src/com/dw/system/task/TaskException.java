package com.dw.system.task;

import java.io.* ;
import java.util.* ;


/**
 * ���񼰵��ȳ�����
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