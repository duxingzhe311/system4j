package com.dw.system ;

/**
 * 该类用于测试某些程序的运行时间。
 * @deprecated Currently no used.
 */
public class TimeCounter
{
	private long time ;

	public TimeCounter ()
	{
		time = System.currentTimeMillis () ;
	}
	public long terminate ()
	{
		long current = System.currentTimeMillis () ;
		long gap = current - time ;
		time = current ;

		return gap ;
	}
	public void reset ()
	{
		time = System.currentTimeMillis () ;
	}
}