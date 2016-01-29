package com.dw.system.queue;

import java.util.*;

import com.dw.system.*;

/**
 * ֧���޶����Ȼ��С�Ķ��У����е����ݿ�����
 * 
 * @author Jason Zhu
 */
public class LimitQueueThread
{
	public static interface ILimitHandler
	{
		/**
		 * ��һЩ���������ﵽʱ����Ҫ����һ���ֶ����еĴ������ݣ��Դ˲����Ļص�
		 * �÷���ʵ��ʱӦ�þ����ܵĿ죬��Ҫռ��̫����Դ
		 * @param objs
		 * @throws Exception
		 */
		public void onDisposeObjs(List<Object> objs) ;
		
		public void processObj(Object o) throws Exception;
	}
	
	/**
	 * ����ṩѰ����Ч�Ķ����ж����Ա��ڶ����Զ�������ǰɾ��
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
	 * �����б�
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
				//�ж��޶�����
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
	{//Ϊ�˱���retry�б�����,dequeue֧�ֽ�����㷨.
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
	 * ��գ������е���������
	 */
	public synchronized void emptyQueue()
	{
		normalList.clear();
	}
}
