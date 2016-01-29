package com.dw.system ;

import java.io.FileOutputStream ;
import java.io.OutputStreamWriter ;
import java.io.PrintWriter ;
import java.io.IOException ;
import java.io.RandomAccessFile ;
import java.util.Hashtable ;
import java.util.Date ;
/**
 * �����ṩͳһ��ϵͳ��־���ܡ�<br>
 * �ṩ��ͳһ����־����ӿڣ�����ͬʱ���������־�ļ�����־�ļ������ļ�����Ϊ��ʶ.<br>
 *
 * ������Ϣ���£�<b><i><pre>
 * log.directory : ��־�ļ�����Ŀ¼��
 * log.default   : ȱʡ��־�ļ����ơ�
 * log.seperate  : ��־�Ƿ�ʹ�õ������ļ���ȱʡֵΪfalse��
 * log.size      : ��־�ļ�����󳤶�(��λKB)�������ó��ȵĺ���־�ļ����ؽ���ȱʡ����Ϊ��512K��
 * </pre></i></b>
 * ������ܴ���log�ļ�����ʹ��System.out��
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
	 * ��ȡ��־���������־�Ѵ�������ʹ�����е���־��
	 * @param	name 	��־����Ҳ����־���ļ�����
	 * @return	���ظ�Log�����������־�Ѵ������򷵻����е���־��
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
	 * ��ȡȱʡ��ϵͳ��־��
	 * @return	���ظ�Log����
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
	 * �����־�ļ���С�Ƿ��ޣ�������ޣ��������־�ļ���
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
	 * ����־�м�¼һ��String��
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
	 * ��ȱʡ��־�м�¼һ���������ݣ��ö��󽫱�ת����String��
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
	 * ��¼�쳣��Ϣ��
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
	 * ��¼һ���쳣��
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
	 * �ر���־�����ر���־�ļ���
	 */
	public void close ()
	{
		out.flush () ;

		out.close () ;

		out = null ;

		logList.remove (logName) ;
	}

	/**
	 * ���������Ϣ�ĸ��¡�
	 */
	public void autoConfig ()
	{
		// initialize () ;

	}


}
