package com.dw.system.xevent;

/**
 * <p>Title:�¼����������</p>
 * <p>Description:
 * ͬ��������������������������ingoreOtherException��ʾ����ͬ���¼�������������
 * ���ִ���ʱ�����������Ƿ�Ҫ�˳���<br>
 * maskSelfException��ʾ�����������ִ���ʱ�Ƿ�Ҫ���Լ��Ĵ������Σ��Ա���Ӱ������
 * ������ͬ�¼��ļ����������С�
 * </p>
 * <b><font color="red">ע��ͬ��������������Զ���¼�������������
 * ��ԭ����Զ���¼��϶����첽��</font></b>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */
public class ListenerShell
	implements Comparable
{
	/**
	 * ˳��ſ������������Լ�������ע��ļ������е�λ��
	 */
	int orderNum = Integer.MAX_VALUE;
	/**
	 * �¼�����
	 */
	XEventListener listener = null;
	/**
	 * ��������������ͬ�¼��Ĵ���
	 */
	boolean ingoreOtherException = false;
	/**
	 * �����Լ��Ĵ���
	 */
	boolean maskSelfException = false;
	/**
	 * �Ƿ���Զ���¼�
	 */
	boolean acceptRemoteEvent = true;
	/*
	 * �������¼����͡������null�����ʾ���������¼�
	  XEvent[] forEvents = null;
	 */
	/**
	 * ���������еĻ��ѵ����ʱ��
	 */
	long maxTime = Long.MIN_VALUE;
	/**
	 * �������ʱ���Ӧ���¼�����
	 */
	String maxTimeEvent = null;
	/**
	 * ���������л��ѵ���Сʱ��
	 */
	long minTime = Long.MAX_VALUE;
	/**
	 * ������Сʱ��Ķ�Ӧ�¼�����
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
	 * �ж��Ƿ��������������ͬ�¼��Ĵ���
	 * @return true��ʾ���ԡ������뱻���á�
	 */
	public boolean isIngoreException()
	{
		return ingoreOtherException;
	}

	/**
	 * �ж��Ƿ���Զ���¼�
	 * @return true��ʾ����Զ���¼���false��ʾ������
	 */
	public boolean isAcceptRemoteEvent()
	{
		return acceptRemoteEvent;
	}

	/**
	 * �õ��ü��������������ʱ�䣬��������¼�������������
	 * @return �������������ʱ�䣬���ϵͳ�������������ݶ�ʧ
	 */
	public long getMaxTime()
	{
		return maxTime;
	}

	/**
	 * �õ��ü������������ʱ������Ӧ���¼�
	 * @return �¼�����Ϣ
	 */
	public String getMaxTimeEvent()
	{
		return maxTimeEvent;
	}

	/**
	 * �õ��ü����������С���죩����ʱ�䣬��������¼�������������
	 * @return ��������С����ʱ�䣬���ϵͳ�������������ݶ�ʧ
	 */
	public long getMinTime()
	{
		return minTime;
	}

	/**
	 * �õ��ü�����������Сʱ������Ӧ���¼�
	 * @return �¼�����Ϣ
	 */
	public String getMinTimeEvent()
	{
		return minTimeEvent;
	}

	/**
	 * ����һ��������¼�
	 * @param xe �¼�����
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