package com.dw.system.task;

/**
 * 辅助进程管理总类
 * 
 * 
 * 很多种场合,需要主程序启动辅助进程来共同完成一些任务
 * 并且,主程序一般是7x24小时运行,而这些辅助进程也是如此. 同时,主程序应该可以完全
 * 控制辅助进程的启动和关闭--要求主程序被kill时,也能够使辅助进行自动停止
 * 
 * 本方法主要为主进程提供了一个统一控制辅助进程的方法
 * 关键要求是:<b>要求主程序被kill时,也能够使辅助进行自动停止</b>
 * 
 * 在Server中,每个webapp应用对应一个进程,里面可以有多个运行任务--每个任务自己决定自己运行频率.
 * 
 * @author Jason Zhu
 */
public class ProManager
{
	private static Object locker = new Object() ;
	
	private static ProManager proMgr = null;
	
	public static ProManager getInstance()
	{
		if(proMgr!=null)
			return proMgr ;
		
		synchronized(locker)
		{
			if(proMgr!=null)
				return proMgr ;
			
			proMgr = new ProManager() ;
			return proMgr ;
		}
	}
	
	private ProManager()
	{}
	
	//public 
}
