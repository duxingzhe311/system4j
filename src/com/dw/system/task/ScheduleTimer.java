package com.dw.system.task;

import java.util.*;

/**
 * ������,����ʱ�̿ɵ����������ʱ��
 * ÿ��ֻ������һ�����񡣵�������ʼʱ��ɽ��е���
 *
 * @author  zhuzhijun
 */

public class ScheduleTimer
	implements Runnable
{

	/**
	 * ʱ���߳�
	 */
	private Thread thread = null;

	/**
	 * ʱ������
	 */
	private TimerTask timerTask = null;
	/**
	 * ����ִ��ʱ��
	 */
	private long runningTime = -1;
	
	ThreadGroup belongThg = null ;
	
	/**
	 * ����һ��ʱ��
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
			//����timerTask.run()�����Ӱ��loopMain�����У�ͨ��sched(TimerTask task,long time)��
			//��ͬʱ��timerTask����������loopMain�����У�����Ӧ�Ȱ�timerTask=null
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
	 * ��ָ����ʱ���ڵ���ָ������
	 *
	 * @param task  ����������
	 * @param delay ������ִ��֮ǰ����ʱ�����������<=0��˵������������ִ��
	 * @throws TaskException �����Ȳ���ȷ
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
	 * ���Ѿ����ڵ���������ָ�������ӳ�(�ӵ�ǰϵͳʱ�俪ʼ)
	 *
	 * @param delay ������ִ��֮ǰ����ʱ�����������<=0��˵������������ִ��
	 * @throws TaskException �����Ȳ���ȷ
	 */
	public void schedule(long delay)
	{
		sched(System.currentTimeMillis() + delay);
	}

	/**
	 * ���ض���ʱ����ϵ���ָ���������ʱ���Ѿ���ȥ��Ψ�գ�����ͻᱻ����ִ�С�
	 *
	 * @param task ����������
	 * @param time ����ִ��ʱ��
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
	 * ���Ѿ����ڵ���������ָ�������ӳ�
	 *
	 * @param time ����ִ��ʱ��
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
	 * ֹͣ��ǰ�ĵȴ���������
	 */
	public void cancel()
	{
		timerTask = null;
	}

	/*
	 * ����ʱ�ӡ�����ʱ�ӵ��߳̽����ж�
	 * <b>�����ǰ�������������У����п��ܻ����</b>
	 *
	  public void destory()
	 throws TaskException
	  {
	 if(bTaskRunning)
	  throw new TaskException("Cannot destory because has task running!") ;
	 thread.interrupt() ;
	  }*/
}