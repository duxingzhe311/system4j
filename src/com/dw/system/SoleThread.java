package com.dw.system ;
import java.lang.reflect.* ;
/**
 * SoleThread用来控制在一个JVM中，同一线程仅能保持一个实例运行。<br/>
 * 正常情况下，可以使用静态变量来保证实例的唯一性，
 * 但当线程运行在Application Server中时, 由于类更新等问题, 会导致类被重新装载
 * (使用不同的ClassLoader), 这就会导致同时又多个线程同时运行的情况。<br/>
 * SoleThread使用唯一的线程名来解决线程重复问题，使用{@link SoleRunnable}替代Runnable接口。<br/>
 * 当SoleThread实例被实例化时，会检查是否有同一线程运行，如果有，会通知该线程停止，并等待该线程终止。<br/>
 * 使用该类比较简单，普通的线程代码如下：<br/><pre>
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
 * 使用SoleThread的方法与其类似，代码如下：<br/><pre>
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
 * 需要注意的是，由于上述代码分别运行在两个线程中(release和run)，因此，应当避免产生冲突。<br>
 * @see SoleRunnable
 */
public final class SoleThread extends Thread
{
	private boolean isSole = true ;

	private SoleRunnable target = null ;

	/**
	 * 创建一个SoleThread并检查线程列表，以确保Sole。
	 * @param target 运行目标对象
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
	 * Is this Thread <code>Sole</code>? If not, this Thread should stop immediatly。
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
	 * 将唤醒(Interrupt)另一个线程，并调用其[@link SoleRunnable#release}。
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