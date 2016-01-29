package com.dw.system ;

import java.io.FileOutputStream ;
import java.io.OutputStreamWriter ;
import java.io.PrintWriter ;
import java.io.IOException ;
import java.io.RandomAccessFile ;
import java.util.Hashtable ;
import java.util.Date ;
/**
 * 本类提供统一的系统日志功能。<br>
 * 提供了统一的日志管理接口，可以同时建立多个日志文件。日志文件用其文件名作为标识.<br>
 *
 * 配置信息如下：<b><i><pre>
 * log.directory : 日志文件所在目录。
 * log.default   : 缺省日志文件名称。
 * log.seperate  : 日志是否使用单独的文件，缺省值为false。
 * log.size      : 日志文件的最大长度(单位KB)，超过该长度的后，日志文件将重建。缺省长度为：512K。
 * </pre></i></b>
 * 如果不能创建log文件，将使用System.out。
 *
 */
public class Log implements EnableAutoConfig
{
	private static Hashtable logList = null ;
	private static PrintWriter DEFAULT_LOG_OUT = new PrintWriter (System.err) ;
	private static Log DEFAULT_LOG ;
//	private static final String DEFAULT_LOG_FILE_NAME = "system.log" ;
	private static String LOG_DIRECTORY ;
	private String logName ;
	private PrintWriter out ;

	private RandomAccessFile file = null ;

	private static boolean initialized = false ;

	private static long logSize = 512*1024 ;

	private static boolean seperatedLog = false ;

	static // void initialize  () /*throws IOException*/
	{
		if (logList == null)
			logList = new Hashtable () ;

		LOG_DIRECTORY = Configuration.getProperty ("log.directory") ;

		// System.out.println (LOG_DIRECTORY) ;
		String name = Configuration.getProperty ("log.default") ;
		if (name == null)
			name = "system" ;
		seperatedLog = Boolean.getBoolean ("log.seperate") ;
		logSize = Integer.getInteger ("log.size" , 512).intValue () ;
		if (logSize < 64)
			logSize = 512 ;

		logSize *= 1024 ;

		if (LOG_DIRECTORY == null)
		{
			DEFAULT_LOG = new Log (name , new PrintWriter (System.out)) ;
			DEFAULT_LOG.log ("Can't create Log File! need property: log.directory and log.default. Use System.out as default.") ;
		}
		else
		{
			try
			{
				DEFAULT_LOG = new Log (name) ;
			}
			catch (Throwable _t)
			{
				DEFAULT_LOG = new Log (name , new PrintWriter (System.out)) ;
				DEFAULT_LOG.log (_t) ;
				DEFAULT_LOG.log ("Can't create Log File! need property: log.directory and log.default. Use System.out as default.") ;

			}
		}
		// Configuration.register (DEFAULT_LOG) ;

		logList.put (name , DEFAULT_LOG) ;
		initialized = true ;

	}


	private Log (String name , PrintWriter p) /*throws IOException*/
	{
		logName = name ;

		out = p ;
	}

	private Log (String name)
		throws IOException
	{
		logName = name ;

		if (LOG_DIRECTORY == null)
			return ;

		if (seperatedLog || DEFAULT_LOG == null)
		{
			out = new PrintWriter (new OutputStreamWriter (new FileOutputStream ("" + LOG_DIRECTORY + logName , true) , "GBK")) ;
			file = new RandomAccessFile (LOG_DIRECTORY + logName , "rw") ;

		}
	}

	/**
	 * 获取日志。如果该日志已创建，则使用已有的日志。
	 * @param	name 	日志名。也是日志的文件名。
	 * @return	返回该Log对象，如果该日志已创建，则返回已有的日志。
	 */
	public synchronized static Log getLog (String name) /* throws IOException */
	{
		/*
		if (! initialized)
			initialize () ;
		*/
		Log log = (Log) logList.get (name) ;

		if (log == null)
		{

			try
			{
				log = new Log (name) ;
			}
			catch (Throwable _t)
			{
				DEFAULT_LOG.log ("Error Occurs When get Log file [" +
					LOG_DIRECTORY + name + "]" + _t) ;

				log = new Log (name , null) ;
			}

		}

		logList.put (name , log) ;
		return log ;
	}

	/**
	 * 获取缺省的系统日志。
	 * @return	返回该Log对象。
	 */
	public static Log getLog () /* throws IOException */
	{
		/*
		if (! initialized)
			initialize () ;
		*/
		return DEFAULT_LOG ;
	}

	/**
	 * 检查日志文件大小是否超限，如果超限，则清空日志文件。
	 */
	protected synchronized void backup ()
	{
		try
		{
			if (file != null && file.length () > logSize)
				file.setLength (0) ;

		}
		catch (IOException _ioe)
		{
			_ioe.printStackTrace () ;
		}
	}

	/**
	 * 在日志中纪录一段String。
	 */
	public void log (String info)
	{
		if (out == null)
		{
			DEFAULT_LOG.log (logName , info) ;
			return ;
		}

		log (null , info) ;
	}

	private void log (String name , String info)
	{

		long t = System.currentTimeMillis () ;
		out.println ((name == null ? "" : "[" + name + "] ") +
			new java.sql.Date (t).toString () + " " + new java.sql.Time (t).toString () + " " + info) ;
		out.flush () ;

		backup () ;
	}

	/**
	 * 在缺省日志中纪录一个对象内容，该对象将被转换成String。
	 */
	public void log (Object info)
	{
		if (out == null)
		{
			DEFAULT_LOG.log (logName , info) ;
			return ;
		}
		log (null , info) ;
	}
	private void log (String name , Object info)
	{
		long t = System.currentTimeMillis () ;
		out.println ((name == null ? "" : "[" + name + "] ") +
			new java.sql.Date (t).toString () + " " + new java.sql.Time (t).toString () + " " + info) ;
		out.flush () ;

		backup () ;
	}
	/**
	 * 记录异常信息。
	 */
	private void log (String name , Throwable err)
	{
		long t = System.currentTimeMillis () ;
		out.println ((name == null ? "" : "[" + name + "] ") +
			new java.sql.Date (t).toString () + " " + new java.sql.Time (t).toString () + " ") ;
		err.printStackTrace (out) ;

		out.flush () ;

		backup () ;
	}

	/**
	 * 纪录一个异常。
	 */
	public void log (Throwable err)
	{

		if (out == null)
		{
			DEFAULT_LOG.log (logName , err) ;
			return ;
		}
		log (null , err) ;
	}
	/**
	 * 关闭日志。将关闭日志文件。
	 */
	public void close ()
	{
		out.flush () ;

		out.close () ;

		out = null ;

		logList.remove (logName) ;
	}

	/**
	 * 完成配置信息的更新。
	 */
	public void autoConfig ()
	{
		// initialize () ;

	}


}
