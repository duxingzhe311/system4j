package com.dw.system.xevent;

import java.util.*;

/**
 * <p>Title: 线程池</p>
 * <p>Description: 为异步事件处理提供固定个数的线程资源</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */
class WorkThreadPool
{
	private final int nThreads;
	private final PoolWorker[] threads;
	private final LinkedList linkedList;

	public WorkThreadPool(int nThreads)
	{
		if (nThreads <= 0)
		{
			throw new IllegalArgumentException(
				"thread number must bigger than 0");
		}

		this.nThreads = nThreads;
		linkedList = new LinkedList();
		threads = new PoolWorker[nThreads];

		for (int i = 0; i < nThreads; i++)
		{
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	public int getQueueLength()
	{
		return linkedList.size();
	}

	public void execute(Runnable r)
	{
		synchronized (linkedList)
		{
			linkedList.addLast(r);
			linkedList.notify();
		}
	}

	private class PoolWorker
		extends Thread
	{
		public void run()
		{
			Runnable r;

			while (true)
			{
				//获得同步锁
				synchronized (linkedList)
				{
					while (linkedList.isEmpty())
					{
						try
						{
							//等待并释放锁
							linkedList.wait();
						}
						catch (InterruptedException ignored)
						{
						}
					}

					//wait结束后重新获得锁
					r = (Runnable) linkedList.removeFirst();
				}

				// If we don't catch RuntimeException,
				// the pool could leak threads
				try
				{
					r.run();
				}
				catch (Throwable t)
				{
					// You might want to log something here
				}
			}
		}
	}
}

class KeyThreadPool
{
	private PoolWorker[] threads = null;
	private int threadNum = 0;
	private Set keySet = Collections.synchronizedSet(new HashSet());

	public KeyThreadPool(int nthread)
	{
		if (nthread <= 0)
		{
			throw new IllegalArgumentException("Thread Num must bigger than 0!");
		}

		threadNum = nthread;
		//启动固定个数的处理线程
		threads = new PoolWorker[threadNum];
		for (int i = 0; i < threadNum; i++)
		{
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * 判断是否有空闲的线程
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
	 * 判断当前是否存在某种key的Runnable对象
	 * @param key 由外界指定的Runnable对象的key
	 * @return true表示已经有相关的Runnable对象正在运行
	 */
	public synchronized boolean containsRunnerKey(Object key)
	{
		return keySet.contains(key);
	}

	/**
	 * 往线程池中增加一个任务，并占用一个线程资源
	 * @param key 外界指定的Runnable对象对应的key
	 * @param r Runnable对象
	 */
	public synchronized void addRunner(Object key, Runnable r)
	{
		if (keySet.contains(key))
		{
			throw new RuntimeException("key:" + key + " is running!");
		}

		PoolWorker pw = getIdlePoolWorker();
		if (pw == null)
		{
			throw new RuntimeException("no idle thread in thread pool!");
		}

		//key集合中增加对应的key，这个key在任务完成之后会被删除
		keySet.add(key);
		//执行任务，如果任务结束，对应的key会被从keySet中删除。
		pw.execute(key, r);
	}

	class PoolWorker
		extends Thread
	{
		Runnable runner = null;
		Object key = null;

		public synchronized void execute(Object key, Runnable r)
		{
			runner = r;
			this.key = key;
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
					//运行任务
					try
					{
						runner.run();
					}
					catch (Throwable _t)
					{}

					//设置该key不被占用
					keySet.remove(key);
					//设置空闲
					runner = null;
				}
			}
			catch (Throwable t)
			{
				// You might want to log something here
			}
		}
	}

	public static void main(String[] args)
	{
		System.out.println( -1 % 3);
	}
} //end of keythreadpool

class ThreadPool
{
	private PoolWorker[] threads = null;
	private int threadNum = 0;

	public ThreadPool(int nthread)
	{
		if (nthread <= 0)
		{
			throw new IllegalArgumentException("Thread Num must bigger than 0!");
		}

		threadNum = nthread;
		//启动固定个数的处理线程
		threads = new PoolWorker[threadNum];
		for (int i = 0; i < threadNum; i++)
		{
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * 判断是否有空闲的线程
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
	 * @param key 外界指定的Runnable对象对应的key
	 * @param r Runnable对象
	 */
	public synchronized void addRunner(Runnable r)
	{
		PoolWorker pw = getIdlePoolWorker();
		if (pw == null)
		{
			throw new RuntimeException("no idle thread in thread pool!");
		}

		//执行任务，如果任务结束，对应的key会被从keySet中删除。
		pw.execute(r);
	}

	class PoolWorker
		extends Thread
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
					//运行任务
					try
					{
						runner.run();
					}
					catch (Throwable _t)
					{}

					//设置空闲
					runner = null;
				}
			}
			catch (Throwable t)
			{
				// You might want to log something here
			}
		}
	}
} //end of threadpool