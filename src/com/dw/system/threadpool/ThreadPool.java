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
		// �����̶������Ĵ����߳�
		threads = new PoolWorker[threadNum];
		for (int i = 0; i < threadNum; i++)
		{
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	/**
	 * �ж��Ƿ��п��е��߳�
	 * 
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
	 * 
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
	 * 
	 * @param key
	 *            ���ָ����Runnable�����Ӧ��key
	 * @param r
	 *            Runnable����
	 */
	public synchronized void addRunner(Runnable r)
	{
		PoolWorker pw = getIdlePoolWorker();
		if (pw == null)
		{
			throw new RuntimeException("no idle thread in thread pool!");
		}

		// ִ��������������������Ӧ��key�ᱻ��keySet��ɾ����
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
					// ��������
					try
					{
						log.debug(" Thread Pool One thread--start running..../");
						runner.run();
					}
					catch (Throwable _t)
					{// Ϊ�˷�ֹ���ִ�����߳�ֹͣ��

					}

					// ���ÿ��У�������afterPoolThreadRun֮ǰ��Ϊ��
					// ���õ����߳����п��е�����·����ȴ�

					Runnable oldr = runner;

					runner = null;

					// ����ʵ������֮�����ش���
					// ֮�������������Ϊ��֪ͨ�����̼߳������С�
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
	 * ��һ��ʵ��һ�����н���֮�󣬻���ø÷���Ϊʵ���Ժ�Ĵ���
	 * 
	 * ��Ϊ�÷������н������߳̾ͽ�����������֪ͨ��Ϊû�п����߳� �������ĵ����̼߳������С�
	 * �÷�����ThreadPool�е�PoolWorker��һ�����н���֮�����
	 */
	private void afterPoolThreadRun(Runnable r)
	{
		
	}
} // end of threadpool