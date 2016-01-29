package com.dw.system.xevent;

/**
 * <p>Title: �¼��������</p>
 * <p>Description: �����¼����Ǽ̳и��ࡣ���й��췽������ָ���ǿյ��¼�����</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public class XEvent
	implements java.io.Serializable
{
	/**
	 * �ж��Ƿ���Զ���¼������ð����Ϳ���ʹ�ó�Ա���޸Ŀ��Ա�XEventDispathcer�޸�
	 */
	transient boolean _fromRemote = false;
	/**
	 * �¼�����
	 */
	protected String _eventName = null;

	/**
	 * ����һ��ԭ���¼�����
	 * @param eventn �¼����������
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
	 * �õ��¼�����
	 * @return �¼�����
	 */
	public String getEventName()
	{
		return _eventName;
	}

	/**
	 * �ж��Ƿ���Զ�̹������¼�����
	 * @return true��ʾԶ�̹������¼�����false��ʾ���ض���
	 */
	public boolean isFromRemote()
	{
		return _fromRemote;
	}

	/**
	 * ���ش������¼����ַ�����ʾ��
	 * @return �����¼�������ַ���
	 */
	public String toString()
	{
		return getClass().getName() + "[EventName=" + _eventName + "]";
	}
}