package com.dw.system.logger;

/**
 * 日志管理
 * @author Jason Zhu
 */
public interface ILogger
{
	/**
	 * 针对具体的log控制值，缺省情况下继承全局
	 */
	public static final int CTRL_DEFAULT = 0 ;
	
	/**
	 * 针对具体的log控制值，自定义情况下打开
	 */
	public static final int CTRL_ENABLE = 1 ;
	
	/**
	 * 针对具体的log控制值，自定义情况下关闭
	 */
	public static final int CTRL_DISABLE = -1 ;
	
	
	public String getLoggerId() ;
	
	/**
	 * 获得当前的控制
	 * @return
	 */
	public int getCtrl();
	
	/**
	 * 设置当前的控制
	 * @param c
	 */
	public void setCtrl(int c) ;
	
	public boolean isStackTrace();
	
	public void setStackTrace(boolean b) ;
	
	/**
	 * 类似System.out.println方法，不受控制限制
	 * @param msg
	 */
	public void print(String msg) ;
	
	public void println(String msg) ;
	
	public void printException(Exception ex) ;

	public void fatal(String msg);
	
	public void fatal(Throwable t);

	public void error(String msg);
	
	public void error(Throwable t);

	public void warn(String msg);
	
	public void warn(String msg,Throwable t);

	public void info(String msg);
	
	public void info(String msg,Throwable t);

	public void debug(String msg);
	
	public void debug(String msg,Throwable t);
	
	public void trace(String msg);
	
	public void trace(String msg,Throwable t);

	public boolean isTraceEnabled() ;
	
	public boolean isDebugEnabled() ;
	
	public boolean isInfoEnabled();
	
	public boolean isWarnEnabled() ;
	
	public boolean isErrorEnabled();
	
	public boolean isFatalEnabled() ;
	//public boolean is
	
	
//	public void setTraceEnabled(boolean b) ;
//	
//	public void setDebugEnabled(boolean b) ;
//	
//	public void setInfoEnabled(boolean b) ;
//	
//	public void setWarnEnabled(boolean b) ;
//	
//	public void setErrorEnabled(boolean b) ;
//	
//	public void setFatalEnabled(boolean b) ;
}
