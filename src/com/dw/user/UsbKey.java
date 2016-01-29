package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.encrypt.DES;

public class UsbKey
{
	
	
	public static class KeyItem
	{
		/**
		 * ��Ӧ�û�����
		 */
		String userName = null;
		/**
		 * �ͻ����豸id,��usb���кŵ�
		 */
		String clientId = null ;
		
		/**
		 * ������id,ÿ�������µĺ���ʱ,�����ݻ�ı�
		 */
		String serverId = null ;
		
		public String getUserName()
		{
			return userName ;
		}
		
		public String getClientId()
		{
			return clientId ;
		}
		
		public String getServerId()
		{
			return serverId ;
		}
	}
	
	public static interface IUsbKeyValidator
	{
		String getUsbKeyEncTypeName();
	
		KeyItem parseUsbKey(String serverkeytxt);
	
		String createNewUsbKey(String username, String client_usb_serial);
	}
}

class DefaultUsbKeyValidator implements UsbKey.IUsbKeyValidator
{
	private static String ENC_KEY = "to$ma%to" ;
	
	public String getUsbKeyEncTypeName()
	{
		return "";
	}

	public UsbKey.KeyItem parseUsbKey(String serverkeytxt)
	{
		if (serverkeytxt == null || serverkeytxt.equals(""))
			return null;

		String s = null;
		
		try
		{
			s = DES.decode(serverkeytxt, ENC_KEY) ;
		}
		catch(Exception e)
		{
			return null ;
		}
		
		int i = s.indexOf('+');
		if (i <= 0)
			return null;

		UsbKey.KeyItem ki = new UsbKey.KeyItem() ;
		ki.serverId = s.substring(0,i) ;
		
		s = s.substring(i+1) ;
		i = s.indexOf('+') ;
		if(i<0)
			return null ;
		
		ki.userName = s.substring(0,i) ;
		ki.clientId = s.substring(i+1) ;

		return ki;
	}

	public String createNewUsbKey(String username, String client_usb_serial)
	{
		if(username==null)
			username = "" ;
		if(client_usb_serial==null)
			client_usb_serial = "" ;
		
		String s = UUID.randomUUID().toString() +"+"+ username+"+" + client_usb_serial;
		return DES.encode(s, ENC_KEY) ;
	}
}

class UsbKeyValidatorManager
{
	static UsbKey.IUsbKeyValidator defaultKv = new DefaultUsbKeyValidator();

	// / <summary>
	// / �������û�ȡ��֤�����������ṩ���û�ϵͳ�����û���Usb��Կ
	// / </summary>
	// / <returns></returns>
	public static UsbKey.IUsbKeyValidator GetUsbKeyValidatorBySetting()
	{
		return defaultKv;
	}

	// / <summary>
	// / ���Ӽ��ܷ�ʽ��ȡ��֤��
	// / </summary>
	// / <param name="enctype"></param>
	// / <returns></returns>
	public static UsbKey.IUsbKeyValidator GetUsbKeyValidator(String enctype)
	{
		return defaultKv;
	}
}
