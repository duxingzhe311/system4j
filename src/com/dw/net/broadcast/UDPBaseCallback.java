package com.dw.net.broadcast ;

/**
 * ͳһʵ��UDP�Ĵ���ӿ�
 */
public interface UDPBaseCallback
{
	void OnMsg (UDPBase base,String srctopic,byte[] infobuf);
}