package com.dw.system ;
import java.lang.reflect.* ;
/**
 * SoleThread����������һ��JVM�У�ͬһ�߳̽��ܱ���һ��ʵ�����С�<br/>
 * ��������£�����ʹ�þ�̬��������֤ʵ����Ψһ�ԣ�
 * �����߳�������Application Server��ʱ, ��������µ�����, �ᵼ���౻����װ��
 * (ʹ�ò�ͬ��ClassLoader), ��ͻᵼ��ͬʱ�ֶ���߳�ͬʱ���е������<br/>
 * SoleThreadʹ��Ψһ���߳���������߳��ظ����⣬ʹ��{@link SoleRunnable}���Runnable�ӿڡ�<br/>
 * ��SoleThreadʵ����ʵ����ʱ�������Ƿ���ͬһ�߳����У�����У���֪ͨ���߳�ֹͣ�����ȴ����߳���ֹ��<br/>
 * ʹ�ø���Ƚϼ򵥣���ͨ���̴߳������£�<br/><pre>
 *   Runnable target = new Runnable ()
 *   {
 *       public void run ()
 *       {
 *           // some code.
 *       }
 *   }
 *   Thread th = new Thread (target) ;
 *   th.start () ;
 * </pre>
 * ʹ��SoleThread�ķ����������ƣ��������£�<br/><pre>
 *   SoleRunnable target = new SoleRunnable ()
 *   {
 *       public void run (SoleThread sth)
 *       {
 *          // some code.
 *       }
 *       public void release ()
 *       {
 *          // close file. and so on.
 *       }
 *
 *   }
 *   SoleThread th = new SoleThread (target) ;
 *   th.start () ;
 * </pre>
 * ��Ҫע����ǣ�������������ֱ������������߳���(release��run)����ˣ�Ӧ�����������ͻ��<br>
 * @see SoleRunnable
 */
public final class SoleThread extends Thread
{
	private boolean isSole = true ;

	private SoleRunnable target = null ;

	/**
	 * ����һ��SoleThread������߳��б���ȷ��Sole��
	 * @param target ����Ŀ�����
	 * @exception NullPointerException if target is NULL.
	 */
	public SoleThread (SoleRunnable target)
	{
		super (target.getClass ().getName ()) ;
		if (target == null)
			throw new NullPointerException ("Runnable Target is NULL!") ;

		this.target = target ;

		// try to kill other exist thread.
		init () ;
	}

	/**
	 * Run Target.
	 */
	public void run ()
	{
		target.run (this) ;
	}

	/**
	 * Is this Thread <code>Sole</code>? If not, this Thread should stop immediatly��
	 */
	public boolean isSole ()
	{
		return isSole ;
	}

	/**
	 * @see #kill(Thread)
	 */
	private void kill ()
	{
		isSole = false ;
		// release () ;
		target.release () ;
	}

	private void init ()
	{
		ThreadGroup parent = getThreadGroup () ;

		while (parent.getParent () != null)
			parent = parent.getParent () ;

		Thread [] threadList = new Thread [parent.activeCount ()] ;
		System.out.println (parent.enumerate (threadList , true)) ;

		for (int i = 0 ; i < threadList.length ; i ++)
		{
			if (threadList [i] == null)
				break ;

			if (equals (threadList [i]))
			{
				// kill this dup thread.
				kill (threadList [i]) ;
				// waitting for thread death.
				try
				{
					threadList [i].join () ;
				}
				catch (Throwable _t)
				{
					_t.printStackTrace () ;
				}
			}
		} // end of for
	}

	/**
	 * ������(Interrupt)��һ���̣߳���������[@link SoleRunnable#release}��
	 */
	private void kill (Thread th)
	{
		Class cls = th.getClass () ;

		try
		{
			Method m = cls.getDeclaredMethod ("kill" , (Class[])null) ;

			m.setAccessible (true) ;

			m.invoke (th , (Object[])null) ;

			th.interrupt () ;
		}
		catch (Throwable _t)
		{
			_t.printStackTrace () ;
			// ignore
		}
	}

	public boolean equals (Object obj)
	{
		if (obj == null || ! (obj instanceof Thread))
			return false ;
		Thread th = (Thread) obj ;

		return th.getClass ().getName ().equals (
			this.getClass ().getName ()) &&
			this.getName ().equals (th.getName ()) ;

	}
}