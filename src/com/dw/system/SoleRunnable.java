package com.dw.system ;

/**
 * 该接口仅用于SoleThread。<br/>
 * 编写SoleRunnable接口时应当注意以下几个问题：<br/><pre>
 * 1. 为了降低同步风险，应尽量将运行时间分隔为小的时间单元，
 *    并在各个单元检查{@link SoleThread#isSole()}。
 * 2. 由于SoleThread实例化时会中断已运行的线程，因此，
 *    应当在线程被中断时检查isSole ()。
 * <pre/>
 * 例如：<pre>
 *    // 线程停止标志
 *    boolean flag = true ;
 *    public void run (SoleThread st)
 *    {
 *	     while (flag && st.isSole ())
 *	     {
 *	     	// do some thing
 *	     	try
 *	     	{
 *	     		Thread.sleep (60*1000) ;
 *	     	}
 *	     	catch (InterruptedException e)
 *	     	{
 *	     		e.printStackTrace () ;
 *	     	}
 *	     }
 *
 *	     // Thread terminated.
 *    }
 * </pre>
 * @see SoleThread
 */
public interface SoleRunnable
{
	/**
	 * @param st 该对象所属的SoleThread对象。
	 */
	public void run (SoleThread st) ;

	/**
	 * 释放该线程(占用的资源)。类似于finalize。<br>
	 * 当另一个线程启动时，会调用该方法。
	 * @since 1.1
	 */
	public void release () ;

}