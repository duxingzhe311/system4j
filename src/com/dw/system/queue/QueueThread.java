package com.dw.system.queue;

import java.util.LinkedList;

import com.dw.system.Convert;

/**
 * ʹ���龰:
 * 	{@see XmlDataWithFile} ��ʹ��������ʵ�ʵ����绷������ʵ����.���ɱ���Ļ��������.
 * �ر������ƶ��豸�Ļ�����. Ϊ�˱���ʵ��ʹ���и��û���ɵĲ���.��Ҫ�ܹ�֧�ֺ�̨��������.
 * Ҳ�����û�����ѡ���̨��������.
 *  �����������Ҫ�����������ر���,�����뵽���Ͷ�����. ���е���һͷ��һ������(����)�߳�.���߳�
 * �ȴ������е�����.һ�����������ݼ���,�ͽ��а�˳����.
 * 
 * �ö��п��ǵ������ʧ�����.����ʧ�ܵ����ݱ����´���. Ҳ���Ƿ��͵����ݲ���һ������˳��
 * 
 * @author Jason Zhu
 */
public class QueueThread
{
	private Thread procTh = null ;
	private IObjHandler handler = null ;
	
	
	/**
	 * �����б�
	 */
	private LinkedList<QueItem> normalList = new LinkedList<QueItem>();
	
	/**
	 * ���³����б�,�����������Ҫ������
	 */
	private LinkedList<QueItem> retryList = new LinkedList<QueItem>();
	
	public QueueThread(IObjHandler h)
	{
		handler = h ;
	}
	
	private Runnable runner = new Runnable()
	{
		public void run()
		{
			while(procTh!=null)
			{
				QueItem qi = dequeue_qi() ;
				
				try
				{
					HandleResult hr = handler.processObj(qi.objV,qi.retryTime) ;
					if(hr==HandleResult.Failed_Retry_Later)
					{
						doRetry(qi);
						continue ;
					}
					
					if(hr==HandleResult.Handler_Invalid)
					{//������������Ч,
						long hvw = handler.handlerInvalidWait();
						if(hvw>0)
						{
							try
							{
								Thread.sleep(hvw) ;
							}
							catch(Exception we)
							{}
						}
						continue ;
					}
					
//					����ɹ�
					remove_qi(qi) ;
					continue ;
				}
				catch(Exception e)
				{
					doRetry(qi) ;
				}
			}
		}

		private void doRetry(QueItem qi)
		{
			int rtn = handler.processFailedRetryTimes() ;
			int oldrtn = qi.retryTime ;
			if(rtn>0&&oldrtn<rtn)
			{
				long wt = handler.processRetryDelay(oldrtn+1) ;
				if(wt<=0)
					wt = 10 ;
				qi.retryTime++ ;
				qi.startProcT = System.currentTimeMillis()+wt ;
				remove_qi(qi) ;
				enqueue_retry_qi(qi) ;//add to queue again
			}
			else
			{
				//discard
				try
				{
					handler.processObjDiscard(qi.objV) ;
				}
				catch(Exception exp)
				{}
				
				remove_qi(qi) ;
			}
		}
	};
	
	public void start()
	{
		if(procTh!=null)
			return ;
		
		procTh = new Thread(runner,"queue_thread") ;
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
		return normalList.size()+retryList.size();
	}
	
	public void enqueue(Object o)
	{
		QueItem qi = new QueItem(o) ;
		enqueue_qi(qi) ;
	}

	private synchronized void enqueue_qi(QueItem o)
	{
		o.belongToLL = normalList ;
		normalList.addLast(o);
		
		notify();
	}
	
	private synchronized void enqueue_retry_qi(QueItem qi)
	{
		qi.belongToLL = retryList ;
		retryList.addLast(qi) ;
		Convert.sort(retryList) ;
		
		notify() ;
	}
	
	transient boolean lastPeekNormal = false;
	
	/**
	 * ʵ�ֶ��в���--�ܹ�����retry��delay������ɲ�����������
	 * @return
	 */
	private QueItem peekQueue()
	{
		if (normalList.isEmpty())
		{
			if(retryList.isEmpty())
				return null ;
			else
			{
				lastPeekNormal = false;//peek retry
				return retryList.getFirst() ;
			}
		}
		else
		{
			if(retryList.isEmpty())
			{
				lastPeekNormal = true ;
				return normalList.getFirst() ;
			}
			else
			{
				QueItem n_qi = normalList.getFirst() ;
				QueItem r_qi = retryList.getFirst() ;
				if(r_qi.startProcT>System.currentTimeMillis())
				{//���³����б��������Ҫ�ȴ�,normal���ȼ���
					lastPeekNormal = true ;
					return n_qi ;
				}
				
				//���������Ҫ�ȴ�,���洦��
				if(lastPeekNormal)
				{
					lastPeekNormal = false ;
					return r_qi ;
				}
				else
				{
					lastPeekNormal = true ;
					return n_qi ;
				}
			}
		}
	}

	private synchronized QueItem dequeue_qi()
	{//Ϊ�˱���retry�б�����,dequeue֧�ֽ�����㷨.
		QueItem qi = peekQueue();
		
		if (qi==null)
		{
			try
			{
				wait();
			}
			catch (InterruptedException ie)
			{}
		}
		
		qi = peekQueue();
		
		long ct = System.currentTimeMillis() ;
		if(qi.startProcT>ct)
		{
			try
			{
				wait(qi.startProcT-ct);
			}
			catch (InterruptedException ie)
			{}
			//�����notify,Ҳ�п��ܶ��л��б仯,��Ҫ���»�ȡ
			qi = peekQueue();
		}
		
		
		return qi ;
	}
	
	private synchronized void remove_qi(QueItem qi)
	{
		qi.removeFromList() ;
	}

	/**
	 * ��գ������е���������
	 */
	public synchronized void emptyQueue()
	{
		normalList.clear();
		retryList.clear() ;
	}
	
	public int getNormalLen()
	{
		return normalList.size() ;
	}
	
	public int getRetryLen()
	{
		return retryList.size() ;
	}
	
	public static void main(String[] args) throws Exception
	{
		QueueThread qt = new QueueThread(new TestHandler()) ;
		qt.start() ;
		
		qt.enqueue(new TestObj("111-retry-1",
				new HandleResult[]{HandleResult.Failed_Retry_Later,HandleResult.Succ})) ;
		
		qt.enqueue(new TestObj("222-retry-0",null)) ;
		
		qt.enqueue(new TestObj("333-retry-2",
				new HandleResult[]{HandleResult.Failed_Retry_Later,HandleResult.Failed_Retry_Later,HandleResult.Succ})) ;
		
		qt.enqueue(new TestObj("444-retry-3",
				new HandleResult[]{HandleResult.Failed_Retry_Later,HandleResult.Failed_Retry_Later,HandleResult.Failed_Retry_Later,HandleResult.Succ})) ;
		
		qt.enqueue(new TestObj("xxx-retry-4",
				new HandleResult[]{HandleResult.Failed_Retry_Later,HandleResult.Failed_Retry_Later,HandleResult.Failed_Retry_Later,HandleResult.Failed_Retry_Later,HandleResult.Succ})) ;
		
		
		qt.enqueue(new TestObj("555-retry-0",
				new HandleResult[]{HandleResult.Succ})) ;
		
		qt.enqueue(new TestObj("666-retry-0",
				new HandleResult[]{HandleResult.Succ})) ;
		
		Thread.sleep(1000) ;
		
		qt.enqueue(new TestObj("after1s-retry-0",
				new HandleResult[]{HandleResult.Succ})) ;
		
		while (qt.size()>0)
		{
			System.out.println(qt.size()) ;
			Thread.sleep(1000) ;
		}
	}
	
	private static class TestObj
	{
		String id = null ;
		HandleResult[] hrs = null ;
		
		public TestObj(String id,HandleResult[] hrs)
		{
			this.id = id ;
			this.hrs = hrs ;
		}
		
		HandleResult processObj(int retrytime)
		{
			HandleResult hr = null ;
			
			if(hrs==null||hrs.length<=retrytime)
			{
				hr= HandleResult.Succ ;
			}
			else
				hr = hrs[retrytime] ;
			
			System.out.println("��"+retrytime+"������ process obj="+this+" "+hr) ;
			
			return hr ;
		}
		
		public String toString()
		{
			return id +"@" +System.currentTimeMillis();
		}
	}
	
	private static class TestHandler implements IObjHandler
	{
		public int processFailedRetryTimes()
		{
			return 3;
		}

		public long processRetryDelay(int retrytime)
		{
			return 3000 * retrytime ;
		}

		public HandleResult processObj(Object o,int retrytime) throws Exception
		{
			TestObj to = (TestObj)o ;
			return to.processObj(retrytime) ;
		}

		public long handlerInvalidWait()
		{
			return 0;
		}

		public void processObjDiscard(Object o) throws Exception
		{
			System.out.println("Discard="+o) ;
		}
	}
}
