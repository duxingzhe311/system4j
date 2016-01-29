package com.dw.system.queue;

import java.util.*;

import com.dw.system.*;

/**
 * 支持限定长度或大小的队列，队列的内容可抛弃
 * 
 * @author Jason Zhu
 */
public class LimitQueueThread
{
	public static interface ILimitHandler
	{
		/**
		 * 当一些限制条件达到时，需要抛弃一部分队列中的处理内容，以此产生的回调
		 * 该方法实现时应该尽可能的快，不要占用太多资源
		 * @param objs
		 * @throws Exception
		 */
		public void onDisposeObjs(List<Object> objs) ;
		
		public void processObj(Object o) throws Exception;
	}
	
	/**
	 * 外界提供寻找无效的队列中对象，以便于队列自动做处理前删除
	 * @author Jason Zhu
	 *
	 */
	public static interface ILimitFinder
	{
		public List<Object> findLimitDisposeObjs(LinkedList<Object> queue_objs) ;
	}
	
	private Thread procTh = null ;
	private ILimitHandler handler = null ;
	private ILimitFinder finder = null ;
	
	/**
	 * 正常列表
	 */
	private LinkedList<Object> normalList = new LinkedList<Object>();
	
	
	public LimitQueueThread(ILimitHandler h,ILimitFinder f)
	{
		handler = h ;
		finder = f ;
	}
	
	private Runnable runner = new Runnable()
	{
		public void run()
		{
			while(procTh!=null)
			{
				//判断限定长度
				List<Object> lobjs = findLimitObjs();
				if(lobjs!=null&&lobjs.size()>0)
				{
					handler.onDisposeObjs(lobjs) ;
				}
				
				Object o = dequeue() ;
				try
				{
					handler.processObj(o) ;
					continue ;
				}
				catch(Exception e)
				{
				}
			}
		}
	};
	
	public void start()
	{
		if(procTh!=null)
			return ;
		
		procTh = new Thread(runner,"queue_thread_limit") ;
		procTh.start() ;
	}
	
	public void stop()
	{
		if(procTh==null)
			return ;
		
		procTh.interrupt() ;
		procTh = null ;
	}
	
	

	synchronized public boolean isEmpty()
	{
		return (size() == 0);
	}

	synchronized public int size()
	{
		return normalList.size();
	}
	
	public synchronized void enqueue(Object o)
	{
		normalList.addLast(o);
		
		notify();
	}
	
	private synchronized List<Object> findLimitObjs()
	{
		return finder.findLimitDisposeObjs(normalList) ;
	}
	
	private synchronized Object dequeue()
	{//为了避免retry列表被忽略,dequeue支持交替的算法.
		if(normalList.size()<=0)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ie)
			{
				//return null ;
			}
		}
		
		return normalList.removeFirst() ;
	}
	
	/**
	 * 清空，队列中的所有内容
	 */
	public synchronized void emptyQueue()
	{
		normalList.clear();
	}
}
