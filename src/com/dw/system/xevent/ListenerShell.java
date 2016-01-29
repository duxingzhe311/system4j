package com.dw.system.xevent;

/**
 * <p>Title:事件监听器外壳</p>
 * <p>Description:
 * 同步监听器会有另外两个参数，ingoreOtherException表示监听同样事件的其他监听器
 * 出现错误时，本监听器是否要退出。<br>
 * maskSelfException表示本监听器出现错误时是否要把自己的错误屏蔽，以避免影响其他
 * 监听相同事件的监听器的运行。
 * </p>
 * <b><font color="red">注：同步监听器不处理远程事件（多机情况），
 * 其原因是远程事件肯定是异步的</font></b>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */
public class ListenerShell
	implements Comparable
{
	/**
	 * 顺序号可以用来决定自己在所有注册的监听器中的位置
	 */
	int orderNum = Integer.MAX_VALUE;
	/**
	 * 事件对象
	 */
	XEventListener listener = null;
	/**
	 * 忽略其他监听相同事件的错误
	 */
	boolean ingoreOtherException = false;
	/**
	 * 屏蔽自己的错误
	 */
	boolean maskSelfException = false;
	/**
	 * 是否处理远程事件
	 */
	boolean acceptRemoteEvent = true;
	/*
	 * 监听的事件类型。如果＝null，则表示监听所有事件
	  XEvent[] forEvents = null;
	 */
	/**
	 * 监听器运行的花费的最大时间
	 */
	long maxTime = Long.MIN_VALUE;
	/**
	 * 花费最大时间对应的事件描述
	 */
	String maxTimeEvent = null;
	/**
	 * 监听器运行花费的最小时间
	 */
	long minTime = Long.MAX_VALUE;
	/**
	 * 花费最小时间的对应事件描述
	 */
	String minTimeEvent = null;

	ListenerShell(XEventListener lis)
	{
		this(lis, true, true, true, Integer.MAX_VALUE);
	}

	ListenerShell(XEventListener lis, boolean ingoreexp, boolean maskexp)
	{
		this(lis, ingoreexp, maskexp, true, Integer.MAX_VALUE);
	}

	ListenerShell(XEventListener lis, boolean ingoreexp, boolean maskexp,
				  boolean acceptremote)
	{
		this(lis, ingoreexp, maskexp, acceptremote, Integer.MAX_VALUE);
	}

	ListenerShell(XEventListener lis, boolean ingoreexp,
				  boolean maskexp, boolean acceptremote,
				  int ordernum)
	{
		if (lis == null)
		{
			throw new IllegalArgumentException("Listener cannot null!");
		}
		this.listener = lis;
		this.ingoreOtherException = ingoreexp;
		this.maskSelfException = maskexp;
		this.acceptRemoteEvent = acceptremote;
		this.orderNum = ordernum;
	}

	public int compareTo(Object o)
	{
		ListenerShell ols = (ListenerShell) o;
		return this.orderNum - ols.orderNum;
	}

	/**
	 * 判断是否忽略其他处理相同事件的错误
	 * @return true表示忽略——必须被调用。
	 */
	public boolean isIngoreException()
	{
		return ingoreOtherException;
	}

	/**
	 * 判断是否处理远程事件
	 * @return true表示处理远程事件，false表示不处理
	 */
	public boolean isAcceptRemoteEvent()
	{
		return acceptRemoteEvent;
	}

	/**
	 * 得到该监听器的最大运行时间，用来监控事件监听器的运行
	 * @return 曾经的最大运行时间，如果系统重新启动则数据丢失
	 */
	public long getMaxTime()
	{
		return maxTime;
	}

	/**
	 * 得到该监听器运行最大时间所对应的事件
	 * @return 事件类信息
	 */
	public String getMaxTimeEvent()
	{
		return maxTimeEvent;
	}

	/**
	 * 得到该监听器的最大小（快）运行时间，用来监控事件监听器的运行
	 * @return 曾经的最小运行时间，如果系统重新启动则数据丢失
	 */
	public long getMinTime()
	{
		return minTime;
	}

	/**
	 * 得到该监听器运行最小时间所对应的事件
	 * @return 事件类信息
	 */
	public String getMinTimeEvent()
	{
		return minTimeEvent;
	}

	/**
	 * 处理一个输入的事件
	 * @param xe 事件对象
	 */
	void dispatch(XEvent xe)
	{
		long ct = System.currentTimeMillis();
		try
		{
			if (maskSelfException)
			{
				try
				{
					listener.onEvent(xe);
				}
				catch (Throwable _t)
				{

				}
			}
			else
			{
				listener.onEvent(xe);
			}
		}
		finally
		{
			ct = System.currentTimeMillis() - ct;
			if (ct > maxTime)
			{
				maxTime = ct;
				maxTimeEvent = xe.getClass().getName() + "[EventName=" +
					xe.getEventName() + "]";
			}
			if (ct < minTime)
			{
				minTime = ct;
				minTimeEvent = xe.getClass().getName() + "[EventName=" +
					xe.getEventName() + "]";
			}
		}
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(listener.getClass().getName());
		sb.append("[ingore=").append(this.ingoreOtherException);
		sb.append(",mask=").append(this.maskSelfException);
		sb.append(",acceptremote=").append(this.acceptRemoteEvent);
		sb.append("][maxtime=");
		if (this.maxTime == Long.MIN_VALUE)
		{
			sb.append("?][mintime=");
		}
		else
		{
			sb.append(maxTime);
			sb.append(",{").append(this.maxTimeEvent).append("}][mintime=");
		}

		if (this.minTime == Long.MAX_VALUE)
		{
			sb.append("?]");
		}
		else
		{
			sb.append(minTime);
			sb.append(",{").append(this.maxTimeEvent).append("}]");
		}
		return sb.toString();
	}
}