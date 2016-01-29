package com.dw.system.xevent;

import java.util.*;

/**
 * <p>Title: �̳߳�</p>
 * <p>Description: Ϊ�첽�¼������ṩ�̶��������߳���Դ</p>
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
				//���ͬ����
				synchronized (linkedList)
				{
					while (linkedList.isEmpty())
					{
						try
						{
							//�ȴ����ͷ���
							linkedList.wait();
						}
						catch (InterruptedException ignored)
						{
						}
					}

					//wait���������»����
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
		//�����̶������Ĵ����߳�
		threads = new PoolWorker[threadNum];
		for (int i = 0; i < threadNum; i++)
		{
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * �ж��Ƿ��п��е��߳�
	 * @return true��ʾ��ǰ�п����߳�
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
	 * �õ�һ�������߳�
	 * @return ����PoolWorker����
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
	 * �жϵ�ǰ�Ƿ����ĳ��key��Runnable����
	 * @param key �����ָ����Runnable�����key
	 * @return true��ʾ�Ѿ�����ص�Runnable������������
	 */
	public synchronized boolean containsRunnerKey(Object key)
	{
		return keySet.contains(key);
	}

	/**
	 * ���̳߳�������һ�����񣬲�ռ��һ���߳���Դ
	 * @param key ���ָ����Runnable�����Ӧ��key
	 * @param r Runnable����
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

		//key���������Ӷ�Ӧ��key�����key���������֮��ᱻɾ��
		keySet.add(key);
		//ִ��������������������Ӧ��key�ᱻ��keySet��ɾ����
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
					//��������
					try
					{
						runner.run();
					}
					catch (Throwable _t)
					{}

					//���ø�key����ռ��
					keySet.remove(key);
					//���ÿ���
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
		//�����̶������Ĵ����߳�
		threads = new PoolWorker[threadNum];
		for (int i = 0; i < threadNum; i++)
		{
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * �ж��Ƿ��п��е��߳�
	 * @return true��ʾ��ǰ�п����߳�
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
	 * �õ�һ�������߳�
	 * @return ����PoolWorker����
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
	 * ���̳߳�������һ�����񣬲�ռ��һ���߳���Դ
	 * @param key ���ָ����Runnable�����Ӧ��key
	 * @param r Runnable����
	 */
	public synchronized void addRunner(Runnable r)
	{
		PoolWorker pw = getIdlePoolWorker();
		if (pw == null)
		{
			throw new RuntimeException("no idle thread in thread pool!");
		}

		//ִ��������������������Ӧ��key�ᱻ��keySet��ɾ����
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
					//��������
					try
					{
						runner.run();
					}
					catch (Throwable _t)
					{}

					//���ÿ���
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