package com.dw.system.task;

import java.util.*;

/**
 * 单任务,调度时刻可调整任务调度时钟
 * 每次只能运行一个任务。但对于起始时间可进行调整
 *
 * @author  zhuzhijun
 */

public class ScheduleTimer
	implements Runnable
{

	/**
	 * 时钟线程
	 */
	private Thread thread = null;

	/**
	 * 时钟任务
	 */
	private TimerTask timerTask = null;
	/**
	 * 任务执行时间
	 */
	private long runningTime = -1;
	
	ThreadGroup belongThg = null ;
	
	/**
	 * 创建一个时钟
	 */
	public ScheduleTimer(ThreadGroup tg)
	{
		belongThg = tg;
	}
	
	public void start()
	{
		if(thread!=null)
			return ;
		
		synchronized(this)
		{
			if(thread!=null)
				return ;
			
			thread = new Thread(belongThg,this,"task_mgr_scheduler");
			thread.start();
		}
	}
	
	public void stop()
	{
		
	}

	public void release()
	{

	}

	public void run()
	{
		while (true)
		{
			try
			{
				loopMain();
			}
			catch (Throwable _t)
			{
				_t.printStackTrace();
			}
		}
		
		//thread = null ;
	}

	synchronized private void loopMain()
		throws Throwable
	{
		if (timerTask == null)
		{
			//System.out.println ("ScheduleTimer== wait for timertask=="+timerTask) ;
			wait();
		}
		else if (runningTime <= System.currentTimeMillis())
		{
			//由于timerTask.run()本身会影响loopMain的运行（通过sched(TimerTask task,long time)）
			//而同时，timerTask又用来控制loopMain的运行，所以应先把timerTask=null
			TimerTask tmptt = timerTask;
			timerTask = null;
			//bTaskRunning = true ;
			tmptt.run();
			//bTaskRunning = false ;
			//timerTask = null ;
		}
		else
		{
			//System.out.println ("ScheduleTimer== wait for timertask=delsy"+(runningTime-System.currentTimeMillis())) ;
			wait(runningTime - System.currentTimeMillis());
		}
	}

	synchronized private void sched(TimerTask task, long time)
	{
		timerTask = task;
		runningTime = time;
		//System.out.println ("Notify thread!"+timerTask) ;
		notifyAll();
	}

	synchronized private void sched(long time)
	{
		if (timerTask == null)
		{
			throw new TaskException(
				"Cannot schedule time because of no timer task!");
		}
		runningTime = time;
		notifyAll();
	}

	/**
	 * 在指定的时间内调度指定任务
	 *
	 * @param task  被调度任务
	 * @param delay 在任务执行之前的延时毫秒数，如果<=0这说明任务立即被执行
	 * @throws TaskException 参数等不正确
	 */
	public void schedule(TimerTask task, long delay)
	{
		if (task == null)
		{
			throw new TaskException("TimerTask cannot be null!");
		}

		sched(task, System.currentTimeMillis() + delay);
	}

	/**
	 * 对已经存在的任务重新指派运行延迟(从当前系统时间开始)
	 *
	 * @param delay 在任务执行之前的延时毫秒数，如果<=0这说明任务立即被执行
	 * @throws TaskException 参数等不正确
	 */
	public void schedule(long delay)
	{
		sched(System.currentTimeMillis() + delay);
	}

	/**
	 * 在特定的时间点上调度指定任务。如果时间已经过去获唯空，任务就会被立即执行。
	 *
	 * @param task 被调度任务
	 * @param time 任务执行时间
	 */
	public void schedule(TimerTask task, Date time)
	{
		if (task == null)
		{
			throw new TaskException("TimerTask cannot be null!");
		}
		
		
		if (time != null)
		{
			sched(task, time.getTime());
		}
		else
		{
			sched(task, 1);
		}
	}

	/**
	 * 对已经存在的任务重新指派运行延迟
	 *
	 * @param time 任务执行时间
	 */
	public void schedule(Date time)
	{
		if (time != null)
		{
			sched(time.getTime());
		}
		else
		{
			sched(1);
		}
	}

	/**
	 * 停止当前的等待运行任务
	 */
	public void cancel()
	{
		timerTask = null;
	}

	/*
	 * 销毁时钟——对时钟的线程进行中断
	 * <b>如果当前有任务正在运行，则有可能会造成</b>
	 *
	  public void destory()
	 throws TaskException
	  {
	 if(bTaskRunning)
	  throw new TaskException("Cannot destory because has task running!") ;
	 thread.interrupt() ;
	  }*/
}