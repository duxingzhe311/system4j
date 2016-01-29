package com.dw.system.xevent;

/**
 * <p>Title: 事件风格对象</p>
 * <p>Description: 每个事件在进行注册的时候都会确定事件风格。事件风格
 * 确定了监听器按照那一种方式进行处理（如同步异步），还确定了事件是否要
 * 把事件通过远程方式进行发送。</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */
public class EventStyle
{
	/**
	 * 同步处理，也就是在同一个线程空间中运行
	 */
	public static final short SYN = 0;
	/**
	 * 异步处理，只有一个队列，并且使用线程池。
	 * 注：该处理方式不能保证同一种事件类的事件对象安顺序处理
	 */
	public static final short ASYN_POOL = 1;
	/**
	 * 异步处理，每个事件对应一个队列和一个线程。
	 * 该类型能够保证处理顺序。但独占一个线程。
	 */
	public static final short ASYN_MONO = 2;

	/**
	 * 把字符串表示的style转换为short类型
	 * @param str 字符串表示的阿风格
	 * @return 事件类型
	 */
	public static short strToStyle(String str)
	{
		if ("ASYN_POOL".equals(str))
		{
			return ASYN_POOL;
		}
		else if ("SYN".equals(str))
		{
			return SYN;
		}
		else if ("ASYN_MONO".equals(str))
		{
			return ASYN_MONO;
		}
		else
		{
			return Short.parseShort(str);
			//throw new IllegalArgumentException("Unknow Event Style:" + str);
		}
	}

	/**
	 * 是否同步处理
	 */
	short style = ASYN_POOL;
	/**
	 * 是否是远程事件
	 */
	boolean bRemote = true;

	EventStyle()
	{}

	EventStyle(short style, boolean bremote)
	{
		this.style = style;
		this.bRemote = bremote;
	}

	/**
	 * 得到事件处理的风格
	 * @return 风格类型
	 */
	public short getStyle()
	{
		return style;
	}

	/**
	 * 判断是否是远程事件
	 * @return true表示远程事件，false表示非远程事件
	 */
	public boolean isRemote()
	{
		return bRemote;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer(30);
		sb.append("EventStyle[style=");
		switch (style)
		{
			case SYN:
				sb.append("SYN");
				break;
			case ASYN_POOL:
				sb.append("ASYN_POOL");
				break;
			case ASYN_MONO:
				sb.append("ASYN_MONO");
				break;
			default:
				sb.append("Error");
		}
		sb.append(",remote=").append(bRemote).append("]");
		return sb.toString();
	}
}