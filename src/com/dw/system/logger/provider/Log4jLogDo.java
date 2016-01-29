package com.dw.system.logger.provider;

import org.apache.log4j.Logger;

import com.dw.system.logger.ILogDo;

public class Log4jLogDo implements ILogDo
{

	private Logger l4jLog = null;
	
	public void print(String msg)
	{
		System.out.print(msg) ;
	}
	
	public void printException(Exception ex)
	{
		ex.printStackTrace() ;
	}

	public Log4jLogDo(Logger l4j)
	{
		l4jLog = l4j;
	}

	public void fatal(String message)
	{
		l4jLog.fatal(message);
	}

	public void fatal(Throwable t)
	{
		l4jLog.fatal("", t);
	}

	public void fatal(String msg, Throwable t)
	{
		l4jLog.fatal(msg, t);
	}

	public void error(String message)
	{
		l4jLog.equals(message);
	}

	public void error(Throwable t)
	{
		l4jLog.error("", t);
	}

	public void error(String msg, Throwable t)
	{
		l4jLog.error(msg, t);
	}

	public void warn(String message)
	{
		l4jLog.warn(message);
	}

	public void warn(String msg, Throwable t)
	{
		l4jLog.warn(msg, t);
	}

	public void info(String message)
	{
		l4jLog.info(message);
	}

	public void info(String msg, Throwable t)
	{
		l4jLog.info(msg, t);
	}

	public void trace(String msg)
	{
		l4jLog.trace(msg);
	}

	public void trace(String msg, Throwable t)
	{
		l4jLog.trace(msg, t);
	}

	public boolean isTraceEnabled()
	{
		return l4jLog.isTraceEnabled();
	}

	public boolean isInfoEnabled()
	{
		return l4jLog.isInfoEnabled();
	}

	public boolean isWarnEnabled()
	{
		return l4jLog.isDebugEnabled();
	}

	public boolean isErrorEnabled()
	{
		return l4jLog.isDebugEnabled();
	}

	public boolean isFatalEnabled()
	{
		return l4jLog.isDebugEnabled();
	}

	/**
	 * 打印debug级别的信息
	 * 
	 * @param message
	 */
	public void debug(String message)
	{
		l4jLog.debug(message);
	}

	public void debug(String msg, Throwable t)
	{
		l4jLog.debug(msg, t);
	}

	public boolean isDebugEnabled()
	{
		return l4jLog.isDebugEnabled();
	}
}
