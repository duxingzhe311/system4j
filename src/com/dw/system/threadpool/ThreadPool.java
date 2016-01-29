package com.dw.system.threadpool;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;

public class ThreadPool
{
	private static final ILogger log = LoggerManager.getLogger(ThreadPool.class
			.getName());
	private PoolWorker[] threads = null;

	private int threadNum = 0;

	public ThreadPool(int nthread)
	{
		if (nthread <= 0)
		{
			throw new IllegalArgumentException(
					"Thread Num must bigger than 0!");
		}

		threadNum = nthread;
		// 启动固定个数的处理线程
		threads = new PoolWorker[threadNum];
		for (int i = 0; i < threadNum; i++)
		{
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * 判断是否有空闲的线程
	 * 
	 * @return true表示当前有空闲线程
	 */
	public boolean hasIdleWorker()
	{
		for (int i = 0; i < threadNum; i++)
		{
			if (threads[i].isIdle())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 得到一个空闲线程
	 * 
	 * @return 空闲PoolWorker对象
	 */
	private PoolWorker getIdlePoolWorker()
	{
		for (int i = 0; i < threadNum; i++)
		{
			if (threads[i].isIdle())
			{
				return threads[i];
			}
		}
		return null;
	}

	/**
	 * 往线程池中增加一个任务，并占用一个线程资源
	 * 
	 * @param key
	 *            外界指定的Runnable对象对应的key
	 * @param r
	 *            Runnable对象
	 */
	public synchronized void addRunner(Runnable r)
	{
		PoolWorker pw = getIdlePoolWorker();
		if (pw == null)
		{
			throw new RuntimeException("no idle thread in thread pool!");
		}

		// 执行任务，如果任务结束，对应的key会被从keySet中删除。
		pw.execute(r);
	}

	class PoolWorker extends Thread
	{
		Runnable runner = null;

		public synchronized void execute(Runnable r)
		{
			runner = r;
			this.notify();
		}

		public boolean isIdle()
		{
			return runner == null;
		}

		public void run()
		{
			try
			{
				while (true)
				{
					synchronized (this)
					{
						if (runner == null)
						{
							wait();
						}
					}
					// 运行任务
					try
					{
						log.debug(" Thread Pool One thread--start running..../");
						runner.run();
					}
					catch (Throwable _t)
					{// 为了防止出现错误而线程停止。

					}

					// 设置空闲，它放在afterPoolThreadRun之前是为了
					// 不让调度线程在有空闲的情况下发生等待

					Runnable oldr = runner;

					runner = null;

					// 调用实例运行之后的相关处理
					// 之所以在最后，是因为它通知调度线程继续运行。
					afterPoolThreadRun(oldr);
				}
			}
			catch (Throwable t)
			{
				// You might want to log something here
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * 当一个实例一次运行结束之后，会调用该方法为实例以后的处理。
	 * 
	 * 因为该方法运行结束后，线程就结束，所以他通知因为没有空闲线程 而阻塞的调度线程继续运行。
	 * 该方法被ThreadPool中的PoolWorker在一次运行结束之后调用
	 */
	private void afterPoolThreadRun(Runnable r)
	{
		
	}
} // end of threadpool