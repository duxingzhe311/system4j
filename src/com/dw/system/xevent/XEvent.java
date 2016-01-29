package com.dw.system.xevent;

/**
 * <p>Title: 事件对象基类</p>
 * <p>Description: 所有事件都是继承该类。其中构造方法必须指定非空的事件名称</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public class XEvent
	implements java.io.Serializable
{
	/**
	 * 判断是否是远程事件，采用包类型可以使该成员的修改可以被XEventDispathcer修改
	 */
	transient boolean _fromRemote = false;
	/**
	 * 事件名称
	 */
	protected String _eventName = null;

	/**
	 * 构造一个原型事件对象
	 * @param eventn 事件对象的名称
	 */
	public XEvent(String eventn)
	{
		if (eventn == null || eventn.equals(""))
		{
			throw new IllegalArgumentException("null event name!");
		}
		this._eventName = eventn;
	}

	/**
	 * 得到事件名称
	 * @return 事件名称
	 */
	public String getEventName()
	{
		return _eventName;
	}

	/**
	 * 判断是否是远程过来的事件对象
	 * @return true表示远程过来的事件对象，false表示本地对象
	 */
	public boolean isFromRemote()
	{
		return _fromRemote;
	}

	/**
	 * 返回代表这事件的字符串表示。
	 * @return 代表事件对象的字符串
	 */
	public String toString()
	{
		return getClass().getName() + "[EventName=" + _eventName + "]";
	}
}