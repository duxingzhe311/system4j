package com.dw.system.logger;

import java.io.*;
import java.util.*;

import com.dw.system.*;

/**
 * 一些应用需要专门做操作日志，以便于后续的检查追溯。一般这种情况都使用文件记录的方式进行
 *  记录时间，人物，做什么事等等
 * @author Jason Zhu
 */
public class FileLogger
{
	String filePath = null ;
	
	public FileLogger(String fp)
	{
		filePath = fp ;
	}
	
	/**
	 * 记录特定的时间，用户，和内容
	 * 系统自动加入文件换行等字符串内容
	 * @param logdt
	 * @param username
	 * @param str
	 */
	public void logLine(Date logdt,String username,String str)
		throws IOException
	{
		FileWriter fw = null;
		try
		{
			fw = new FileWriter(filePath,true) ;
			StringBuilder sb = new StringBuilder() ;
			sb.append(Convert.toFullYMDHMS(new Date())) ;
			sb.append("|") ;
			if(logdt!=null)
				sb.append(Convert.toFullYMDHMS(logdt)) ;
			sb.append("|") ;
			if(username!=null)
				sb.append(username) ;
			sb.append("|") ;
			if(str!=null)
				sb.append(str) ;
			sb.append("\r\n") ;
			
			fw.write(sb.toString());
		}
		finally
		{
			fw.close() ;
		}
	}
	
	public void logLine(String username,String str)
	throws IOException
	{
		logLine(null, username, str) ;
	}
}
