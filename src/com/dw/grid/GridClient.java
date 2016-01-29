package com.dw.grid;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.encrypt.DES;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataWithFile;

public class GridClient
{
	public static final int SEND_RES_SUCC = 1;

	public static final int SEND_RES_CONN_FAILED = 0;

	public static final int SEND_RES_AUTH_FAILED = -1;

	public static final int SEND_RES_SEND_FAILED = -2;

	public static final int SEND_RES_RECV_RESP_FAILED = -3;

	// static ILogger log = LoggerManager.getLogger(GridClient.class);

	String id = null;

	String key = null;

	String host = null;

	int port = GpsMsg.DEFAULT_PORT;

	Socket client = null;

	InputStream inputStream = null;

	OutputStream outputStream = null;

	public GridClient(String id, String key, String host)
	{
		this(id, key, host, GpsMsg.DEFAULT_PORT);
	}

	public GridClient(String id, String key, String host, int p)
	{
		this.id = id;
		this.key = key;
		this.host = host;
		port = p;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}

	public boolean connect() throws Exception
	{
		if (client != null)
			return true;

		try
		{
			client = new Socket(host, port);

			// Get a stream object for reading and writing
			inputStream = client.getInputStream();
			outputStream = client.getOutputStream();

			// check right
			// 读取随机串
			String uuid = Util.readStreamLine(inputStream, 50);
			if (Convert.isNullOrEmpty(uuid))
				throw new Exception("no random gotten!");

			// 加密之,并提交给服务器
			String secstr = DES.encode(uuid, key);
			outputStream.write((id+"=").getBytes());
			outputStream.write(secstr.getBytes());
			outputStream.write((byte) '\n');
			//
			String serok = Util.readStreamLine(inputStream, 5);
			if (!"ok".equals(serok))
			{
				this.close();
				return false;
			}

			return true;
		}
		catch (Exception e)
		{
			// if (log.IsErrorEnabled)
			// log.error(e);

			this.close();

			// 出错的情况下，首先要确保连接的关闭，以避免占用资源
			// 同时，必须抛出错误，以使连接池不会出现误判连接成功
			throw e;
		}
	}

	/**
	 * 支持后台队列发送内容提供的方法
	 * 
	 * @param postdata
	 * @param bdel_localfile
	 * @return
	 */
	public int sendXmlDataWithFileNoCB(XmlDataWithFile postdata,
			boolean bdel_localfile)
	{
		try
		{
			client = new Socket(host, port);

			// Get a stream object for reading and writing
			inputStream = client.getInputStream();
			outputStream = client.getOutputStream();
		}
		catch (Exception conne)
		{
			this.close();
			return SEND_RES_CONN_FAILED;
		}

		try
		{
			// check right
			// 读取随机串
			String uuid = Util.readStreamLine(inputStream, 50);
			if (Convert.isNullOrEmpty(uuid))
				throw new Exception("no random gotten!");

			// 加密之,并提交给服务器
			String secstr = DES.encode(uuid, key);
			outputStream.write((id+"=").getBytes());
			outputStream.write(secstr.getBytes());
			outputStream.write((byte) '\n');
			//
			String serok = Util.readStreamLine(inputStream, 5);
			if (!"ok".equals(serok))
			{
				this.close();
				return SEND_RES_AUTH_FAILED;
			}
		}
		catch (Exception autoe)
		{
			this.close();
			return SEND_RES_AUTH_FAILED;
		}

		try
		{
			postdata.writeToStream(outputStream,
					new XmlDataWithFile.DefaultSendCB());

			outputStream.flush();
			//outputStream.close();
		}
		catch (Exception eee)
		{
			return SEND_RES_SEND_FAILED;
		}

		try
		{
			String resp = Util.readStreamLine(inputStream, 8);

			if ("reconnect".equals(resp))
				return SEND_RES_CONN_FAILED;

			if (!"ok".equals(resp) && !"succ".equalsIgnoreCase(resp))
				return SEND_RES_CONN_FAILED;
		}
		catch (Exception eee)
		{
			return SEND_RES_RECV_RESP_FAILED;
		}

		if (bdel_localfile)
		{// del local file
			List<String> lfps = postdata.getLocalFilePaths();
			if (lfps != null)
			{
				for (String lfp : lfps)
				{
					try
					{
						File tmpf = new File(lfp);
						if (tmpf.exists())
							tmpf.delete();
					}
					catch (Exception eee)
					{
					}
				}
			}
		}

		return SEND_RES_SUCC;
	}

//	public boolean sendXmlDataWithFile(XmlDataWithFile postdata,
//			XmlDataWithFile.ISendCallback cb, boolean bdel_localfile)
//	{
//		return sendXmlDataWithFile(null,postdata,cb, bdel_localfile) ;
//	}
	
	public boolean sendXmlDataWithFile(XmlDataWithFile postdata,
			XmlDataWithFile.ISendCallback cb, boolean bdel_localfile)
	{
		cb.onSendingMsg("开始连接服务器");

		try
		{
			client = new Socket(host, port);

			// Get a stream object for reading and writing
			inputStream = client.getInputStream();
			outputStream = client.getOutputStream();

			cb.onSendingMsg("连接服务器成功");
		}
		catch (Exception conne)
		{
			//rets.append(conne.getMessage());
			try
			{
				cb.sendException("连接服务器失败", conne);
			}
			catch(Exception ee)
			{}
			this.close();
			return false;
		}

		try
		{
			// check right
			// 读取随机串
			String uuid = Util.readStreamLine(inputStream, 50);
			if (Convert.isNullOrEmpty(uuid))
				throw new Exception("no random gotten!");

			System.out.println("uuid="+uuid) ;
			// 加密之,并提交给服务器
			String secstr = DES.encode(uuid, key);
			System.out.println("secstr="+secstr) ;
			//Thread.sleep(6000);
			outputStream.write((this.id+"=").getBytes());
			outputStream.write(secstr.getBytes());
			outputStream.write((byte) '\n');
			//
			String serok = Util.readStreamLine(inputStream, 5);
			if (!"ok".equals(serok))
			{
				cb.onAuthFailed();
				this.close();
				return false;
			}
		}
		catch (Exception autoe)
		{
			//rets.append(autoe.getMessage());
			try
			{
				cb.onAuthFailed();
			}
			catch (Exception eeee)
			{
			}

			this.close();
			return false;
		}

		try
		{
			postdata.writeToStream(outputStream,cb);

			outputStream.flush();
			//outputStream.close();
		}
		catch (Exception eee)
		{
			//rets.append(eee.getMessage());
			this.close();
			try
			{
				cb.sendException("传输数据错误", eee);
			}
			catch (Exception ex)
			{
			}
			return false;
		}

		try
		{
			String resp = Util.readStreamLine(inputStream, 8);

			if (resp == "reconnect")
			{
				try
				{
					cb.sendException("服务器要求重新连接尝试", new Exception());
				}
				catch (Exception ex)
				{
				}

				this.close();
				return false;
			}

			if (!"ok".equals(resp) && !"succ".equalsIgnoreCase(resp))
			{
				try
				{
					cb.sendException("服务器通知错误", new Exception(resp));
				}
				catch (Exception ex)
				{
				}
				this.close();
				return false;
			}
		}
		catch (Exception eee)
		{
			try
			{
				cb.sendException("服务器响应错误", eee);
			}
			catch (Exception ex)
			{
			}

			this.close();
			return false;
		}

		if (bdel_localfile)
		{// del local file
			List<String> lfps = postdata.getLocalFilePaths();
			if (lfps != null)
			{
				for (String lfp : lfps)
				{
					try
					{
						File tmpf = new File(lfp);
						if (tmpf.exists())
							tmpf.delete();
					}
					catch (Exception eee)
					{
					}
				}
			}
		}

		cb.sendFinished();
		return true;
	}

	public void close()
	{
		if (inputStream != null)
		{
			try
			{
				inputStream.close();
				inputStream = null;
			}
			catch (Exception e)
			{

			}
		}

		if (outputStream != null)
		{
			try
			{
				outputStream.close();
				outputStream = null;
			}
			catch (Exception e)
			{

			}
		}

		if (client != null)
		{
			try
			{
				client.close();
				client = null;
			}
			catch (Exception e)
			{

			}
		}
	}

	public boolean isClosed()
	{
		if (client == null)
			return true;

		return client.isClosed();
	}
	
	
	static class TestScb implements XmlDataWithFile.ISendCallback
	{

		public void onConnFailed() throws Exception
		{
			System.out.println("conn failed");
		}

		public void onAuthFailed() throws Exception
		{
			System.out.println("auth failed");
		}

		public void onSendingMsg(String msg)
		{
			System.out.println("sending msg="+msg);
		}

		public void beforeSendXmlData(XmlData sendxd, XmlData innerxd)
		{
			System.out.println("beforeSendXmlData="+sendxd.toXmlString());
		}

		public void afterSendXmlData(XmlData sendxd, XmlData innerxd, long sent_len)
		{
			System.out.println("afterSendXmlData="+sendxd.toXmlString());
		}

		public void beforeSendFile(String filepath, long filelen)
		{
			System.out.println("beforeSendFile="+filepath+" ["+filelen+"]");
		}

		public void duringSendFile(String filepath, long filelen, long sentlen, int readlen)
		{
			System.out.println("duringSendFile="+filepath+" "+sentlen+"/"+filelen+" - "+readlen);
		}

		public void afterSendFile(String filepath, long filelen)
		{
			System.out.println("afterSendFile="+filepath+" ["+filelen+"]");
		}

		public void sendFinished()
		{
			System.out.println("sendFinished") ;
		}

		public void sendException(String title, Exception ee) throws Exception
		{
			System.out.println("sendException="+title) ;
			ee.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) throws Throwable
	{
		GridClient gc = null;
		try
		{
			gc = new GridClient("0", "12345678", "localhost") ;
			StringBuilder sb = new StringBuilder() ;
			XmlData innerxd = new XmlData() ;
			innerxd.setParamValue("xxx", "打对方") ;
			innerxd.setParamValue("ddd", new Date()) ;
			ArrayList<String> localfps = new ArrayList<String>() ;
			if(args!=null)
			{
				for(String s:args)
				{
					localfps.add(s) ;
				}
			}
			XmlDataWithFile xdwf = new XmlDataWithFile(innerxd,localfps) ;
			gc.sendXmlDataWithFile(xdwf, new TestScb(), false) ;
		}
		finally
		{
			if(gc!=null)
				gc.close() ;
		}
		
	}
}
