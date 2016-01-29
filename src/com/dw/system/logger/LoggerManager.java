package com.dw.system.logger;

import java.util.*;

import com.dw.system.logger.provider.ConsoleLogDo;

/**
 * 日志管理
 * @author Jason Zhu
 *
 */
public class LoggerManager
{
	static boolean Is_Debug = true;
	static boolean Is_Info = true;
	static boolean Is_Trace=true;
	static boolean Is_Error=true;
	static boolean Is_Fatal=true;
	static boolean Is_Warn=true ;

	static
	{
//		providerMap.put("console",
//						"com.dw.core.wfengine.log.provider.ConsoleLogProvider");
//		providerMap.put("null",
//						"com.dw.core.wfengine.log.provider.NullLogProvider");
//		providerMap.put("null",
//			"com.dw.core.wfengine.log.provider.Log4jProvider");
		
		
		
		//
		if(false)//if(AppConfig.isDebug())
		{
			
		}
		else
		{
			Is_Debug = false;
			Is_Info = false;
			Is_Trace = false;
			Is_Warn = false;
		}
	}

	private static HashMap<String,ILogger> id2log = new HashMap<String,ILogger>() ;
	
	private static boolean bInCtrl = false;
	
	private static HashSet<String> ctrlEnableIds = null ;
	/**
	 * 根据当前Log提供者，获取指定id的日志对象<br>
	 * 该方法应该为其他模块提供唯一获取Logger对象的入口。<br>
	 * 所有使用到日志的代码，不建议自己保存Logger对象——每次使用日志时通过如下代码：<br>
	 * <pre>
	 *     LoggerManager.getLogger(myid).info("xxxx") ;
	 * </pre>
	 * @param id 日志对象id
	 * @return 日志对象
	 */
	public static ILogger getLogger(String id)
	{
		ILogger logger = id2log.get(id);
		if(logger!=null)
			return logger ;
		
		synchronized(id2log)
		{
			if(logger!=null)
				return logger ;
			
			logger = new LoggerObj(id);
			id2log.put(id, logger);
			
			if(bInCtrl&&ctrlEnableIds!=null)
			{
				if(ctrlEnableIds.contains(id))
				{
					logger.setCtrl(ILogger.CTRL_ENABLE) ;
				}
				else
				{
					logger.setCtrl(ILogger.CTRL_DISABLE) ;
				}
			}
			return logger;
		}
	}
	
	/**
	 * 根据
	 * @param c
	 * @return
	 */
	public static ILogger getLogger(Class c)
	{
		//return provider.getLogger(c.getCanonicalName());
		return getLogger(c.getCanonicalName());
		
	}
	
	
	/**
	 * 得到文件Log
	 * @param logn
	 * @return
	 */
	public static ILogger getFileLogger(String fn)
	{
		return null;
	}
	/**
	 * 获得当前所有的Logger
	 * @return
	 */
	public static ILogger[] getAllLoggers()
	{
		ILogger[] rets = new ILogger[id2log.size()] ;
		id2log.values().toArray(rets) ;
		return rets ;
	}
	
	/**
	 * 根据id设置对应的log强制控制打开，和部分的强制控制关闭
	 * 该方法提供给调试时使用－－可以在运行过程中，改变log的控制状态
	 * @param logid
	 */
	public static void setupLoggerInCtrl(HashSet<String> enableids)
	{
		setupLoggerInCtrl(enableids,false);
	}
	
	public static void setupLoggerInCtrl(HashSet<String> enableids,boolean btrace)
	{
		bInCtrl = true ;
		ctrlEnableIds = enableids ;
		
		for(ILogger l:getAllLoggers())
		{
			String id = l.getLoggerId() ;
			if(ctrlEnableIds!=null&&ctrlEnableIds.contains(id))
			{
				l.setCtrl(ILogger.CTRL_ENABLE) ;
				l.setStackTrace(btrace) ;
			}
			else
			{
				l.setCtrl(ILogger.CTRL_DISABLE) ;
				l.setStackTrace(false) ;
			}
		}
	}
	
	/**
	 * 设置所有的log到缺省状态
	 * 该方法提供给调试结束后使用－－可以在运行过程中，恢复log的缺省控制状态
	 */
	public static void setupLoggerDefault()
	{
		bInCtrl = false ;
		
		for(ILogger l:getAllLoggers())
		{
			l.setCtrl(ILogger.CTRL_DEFAULT) ;
			l.setStackTrace(false) ;
		}
	}
	
	
	public static boolean isInCtrl()
	{
		return bInCtrl ;
	}
	
	public static HashSet<String> getInCtrlEnableIds()
	{
		if(bInCtrl)
			return ctrlEnableIds ;
		
		return null ;
	}
	
	private static ILogDo DEFAULT_LOGDO = new ConsoleLogDo() ;
	
	static ILogDo logDo = DEFAULT_LOGDO ;
	
	/**
	 * 被每个LoggerObj对象使用的获取具体做日志的动作的对象
	 * @return
	 */
	static ILogDo getLogDo()
	{
		return logDo ;
	}
	
	/**
	 * 设置具体的日志动作对象
	 * 比如在Service运行状态下，需要使用临时的连接查看日志信息
	 * @param ld
	 */
	public static void setLogDo(ILogDo ld)
	{
		if(ld==null)
		{
			logDo = DEFAULT_LOGDO ;
			return ;
		}
		
		logDo = ld ;
	}
}
