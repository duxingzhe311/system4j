package com.dw.net.broadcast;

import java.io.*;

import com.dw.system.xevent.*;

/**
 * <p>Title: ΪXEvent�ṩԶ��ͨ�Žӿ�</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public class XEventRemoter
	extends RemoteInterface
	implements UDPBaseCallback
{
	/**
	 * ����Ψһ��ַ
	 */
	public static String LOCAL_UNIQUE_ID = IdGenerator.createNewId();
	/**
	 * ��������
	 */
	public static String SEND_TOPIC = "xevent." + LOCAL_UNIQUE_ID;
	/**
	 * ���ռ�������
	 */
	public static String RECV_TOPIC = "xevent.*";

	UDPBase udpBase = UDPBase.getUDPBase();

	public XEventRemoter(RemoteCallback rcb)
	{
		super(rcb);
		udpBase.addTopic(RECV_TOPIC);
		udpBase.setRecvCallback(this);
	}

	public void sendXEvent(byte[] eventdata)
	{
		udpBase.send(SEND_TOPIC, eventdata);
	}

	public void OnMsg(UDPBase base, String srctopic, byte[] infobuf)
	{
		if (!srctopic.startsWith("xevent."))
		{//��xevent�¼�
			return;
		}

		if (srctopic.equals(SEND_TOPIC))
		{ //����Ǳ��ص��¼��������κ���
			return;
		}
		callback.onRecvedEventData(infobuf);
	}

	public static void main(String[] args)
		throws Exception
	{
		System.out.println("----test of UUIDHexGenerator-----");
		IdentifierGenerator gen = new UUIDHexGenerator("/");
		IdentifierGenerator gen2 = new UUIDHexGenerator();
		for (int i = 0; i < 10; i++)
		{
			String id = (String) gen.generate();
			System.out.println(id + ": " + id.length());
			String id2 = (String) gen2.generate();
			System.out.println(id2 + ": " + id2.length());
		}

		System.out.println("----test of UUIDStringGenerator-----");
		gen = new UUIDStringGenerator("/");
		for (int i = 0; i < 5; i++)
		{
			String id = (String) gen.generate();
			System.out.println(id + ": " + id.length());
		}
	}
}
