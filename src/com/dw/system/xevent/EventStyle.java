package com.dw.system.xevent;

/**
 * <p>Title: �¼�������</p>
 * <p>Description: ÿ���¼��ڽ���ע���ʱ�򶼻�ȷ���¼�����¼����
 * ȷ���˼�����������һ�ַ�ʽ���д�����ͬ���첽������ȷ�����¼��Ƿ�Ҫ
 * ���¼�ͨ��Զ�̷�ʽ���з��͡�</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */
public class EventStyle
{
	/**
	 * ͬ������Ҳ������ͬһ���߳̿ռ�������
	 */
	public static final short SYN = 0;
	/**
	 * �첽����ֻ��һ�����У�����ʹ���̳߳ء�
	 * ע���ô���ʽ���ܱ�֤ͬһ���¼�����¼�����˳����
	 */
	public static final short ASYN_POOL = 1;
	/**
	 * �첽����ÿ���¼���Ӧһ�����к�һ���̡߳�
	 * �������ܹ���֤����˳�򡣵���ռһ���̡߳�
	 */
	public static final short ASYN_MONO = 2;

	/**
	 * ���ַ�����ʾ��styleת��Ϊshort����
	 * @param str �ַ�����ʾ�İ����
	 * @return �¼�����
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
	 * �Ƿ�ͬ������
	 */
	short style = ASYN_POOL;
	/**
	 * �Ƿ���Զ���¼�
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
	 * �õ��¼�����ķ��
	 * @return �������
	 */
	public short getStyle()
	{
		return style;
	}

	/**
	 * �ж��Ƿ���Զ���¼�
	 * @return true��ʾԶ���¼���false��ʾ��Զ���¼�
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