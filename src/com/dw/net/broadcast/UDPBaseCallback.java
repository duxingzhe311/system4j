package com.dw.net.broadcast ;

/**
 * 统一实现UDP的传输接口
 */
public interface UDPBaseCallback
{
	void OnMsg (UDPBase base,String srctopic,byte[] infobuf);
}