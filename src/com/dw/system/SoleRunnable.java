package com.dw.system ;

/**
 * �ýӿڽ�����SoleThread��<br/>
 * ��дSoleRunnable�ӿ�ʱӦ��ע�����¼������⣺<br/><pre>
 * 1. Ϊ�˽���ͬ�����գ�Ӧ����������ʱ��ָ�ΪС��ʱ�䵥Ԫ��
 *    ���ڸ�����Ԫ���{@link SoleThread#isSole()}��
 * 2. ����SoleThreadʵ����ʱ���ж������е��̣߳���ˣ�
 *    Ӧ�����̱߳��ж�ʱ���isSole ()��
 * <pre/>
 * ���磺<pre>
 *    // �߳�ֹͣ��־
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
	 * @param st �ö���������SoleThread����
	 */
	public void run (SoleThread st) ;

	/**
	 * �ͷŸ��߳�(ռ�õ���Դ)��������finalize��<br>
	 * ����һ���߳�����ʱ������ø÷�����
	 * @since 1.1
	 */
	public void release () ;

}