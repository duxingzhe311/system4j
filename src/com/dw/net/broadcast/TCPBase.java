package com.dw.net.broadcast;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.*;

/**
 * 统一实现UDP的传输接口
 */
public class TCPBase
	extends UDPBase
	implements SoleRunnable, Runnable
{
	//SoleThread iamhereThread = null;
	Thread iamhereThread = null;
	String localIP = null;
	int localPort = -1;
	String localHostName = null;

	static final long TIME_OUT = 1000;
	static final int LOOP_SEND_INTERVAL = 5;

	TcpConn tcpConn = null;

	Addr localAddr = null;
	Vector targetAddrs = new Vector();

	TCPBase()
		throws UDPBaseException
	{
		init();
		//startRecv();
	}

	/**
	 * 启动初始化，不接受任何信息包，也不发送任何信息包
	 */
	void init()
		throws UDPBaseException
	{
		try
		{
			System.out.println(
				"------------------Tcp Base Init----------------");
			String tmpLocalAddr = System.getProperties().getProperty(
				"css_tcpbase_localaddr");
			System.out.println(">>>[local from jvm]>>>=" + tmpLocalAddr);
			if (tmpLocalAddr != null && !tmpLocalAddr.equals(""))
			{
				int p = tmpLocalAddr.indexOf(":");
				if (p > 0)
				{
					localIP = tmpLocalAddr.substring(0, p);
				}
				localPort = Integer.parseInt(tmpLocalAddr.substring(p + 1));
			}

			if (localIP == null || localIP.equals(""))
			{
				localIP = System.getProperties().getProperty(
					"css_tcpbase_localip");
			}
			if (localIP == null || localIP.equals(""))
			{
				localIP = InetAddress.getLocalHost().getHostAddress();
			}

			localHostName = InetAddress.getLocalHost().getHostName().
				toLowerCase();
			//System.out.println("name="+localHostName);
			//String tmps = Configuration.getProperty("udpbase.tcp.scope");
			String tmps = System.getProperties().getProperty(
				"udpbase.tcp.scope");

			if (tmps == null || tmps.equals(""))
			{
				throw new UDPBaseException(
					"TcpBase Error:no address to be parsed!");
			}
			StringTokenizer st = new StringTokenizer(tmps, "|");
			while (st.hasMoreTokens())
			{
				String ad = st.nextToken();
				int i = ad.indexOf(':');
				if (i < 0)
				{
					throw new UDPBaseException("Invalid tcp addr [" + ad + "]!");
				}
				String ip = ad.substring(0, i).toLowerCase();
				int port = Integer.parseInt(ad.substring(i + 1));
				if ( (localIP.equals(ip) || localHostName.equals(ip))
					&& (localPort == -1 || port == localPort))
				{
					localAddr = new Addr();
					localAddr.ip = ip;
					localAddr.hostName = ip;
					localAddr.port = port;
					System.out.println(">>>[Find Local from scope]>>>=" +
									   localAddr);
				}
				else
				{
					Addr tmpa = new Addr();
					tmpa.ip = ip;
					tmpa.hostName = ip;
					tmpa.port = port;
					System.out.println(">>>[Find other from scope]>>>=" + tmpa);
					targetAddrs.addElement(tmpa);
				}
			}

			System.out.println(
				"--------------------------------------------");
			if (localAddr == null)
			{
				throw new UDPBaseException(
					"Invalid tcp addr: no local address found!");
			}
			if (targetAddrs.size() == 0)
			{
				throw new UDPBaseException(
					"Invalid tcp addr:no any other address found!");
			}
			//创建新线程，并关闭其他对应线程
			//iamhereThread = new SoleThread(this);
			iamhereThread = new Thread(this);

			tcpConn = new TcpConn(tccb, localAddr);

			iamhereThread.start();
		}
		catch (UDPBaseException e)
		{
			throw e;
		}
		catch (Throwable _t)
		{
			_t.printStackTrace();
			throw new UDPBaseException("UDPBase init() error=\n" + _t.toString());
		}
	}

	public void release()
	{
		try
		{
			System.out.println("TCPBase Sole Thread Release!");
			tcpConn.stopAllConnAndAccept();
		}
		catch (IOException ioe)
		{
			System.out.println("警告：TCPBase独立遭到破坏，资源没有释放干净。可能需要重新启动服务器！");
			System.out.println(
				"[Warning:Sole TCPBase cannot release res,may be need reboot web server!]!");
			ioe.printStackTrace();
		}
	}

	/**
	 * 主线程主要运行发送IAMHere信息，以监控连接状态。或保证系统启动后就能够建立连接
	 */
	public void run(SoleThread st)
	{
		while (st.isSole())
		{
			int s = targetAddrs.size();
			for (int i = 0; i < s; i++)
			{
				Addr ad = (Addr) targetAddrs.elementAt(i);
				try
				{
//					System.out.println(
//						"Before send i am here111111111111111111");
//	 System.out.println("freemem=="+Runtime.getRuntime().freeMemory()/1024+"K") ;
//System.out.println("totalmem=="+Runtime.getRuntime().totalMemory()/1024+"K") ;
					tcpConn.send_I_Am_Here(ad.toString());
//					System.out.println("End send i am here2222222222222222222");
				}
				catch (Exception e0)
				{
					//e0.printStackTrace();
					System.out.println("Send I Am Here To [" + ad + "] Error:" +
									   e0.getMessage());
				}
			}

			try
			{
				Thread.sleep(5000);
			}
			catch (Exception e)
			{}
		}

		if (DEBUG)
		{
			System.out.println("Send I Am Here Sole Thread is finished!!!");
		}
	}

	public void run()
	{
		while (true)
		{
			int s = targetAddrs.size();
			for (int i = 0; i < s; i++)
			{
				Addr ad = (Addr) targetAddrs.elementAt(i);
				try
				{
//					System.out.println(
//						"Before send i am here111111111111111111");
//	 System.out.println("freemem=="+Runtime.getRuntime().freeMemory()/1024+"K") ;
//System.out.println("totalmem=="+Runtime.getRuntime().totalMemory()/1024+"K") ;
					tcpConn.send_I_Am_Here(ad.toString());
//					System.out.println("End send i am here2222222222222222222");
				}
				catch (Exception e0)
				{
					//e0.printStackTrace();
					System.out.println("Send I Am Here To [" + ad + "] Error:" +
									   e0.getMessage());
				}
			}

			try
			{
				Thread.sleep(5000);
			}
			catch (Exception e)
			{}
		}

	}

	/**
	 * 判断是否是配置中ip最小的服务器
	 * @return
	 */
	public boolean isMaster()
	{
		String mip = getMasterIP();
		if (mip.equals(localIP))
		{
			return true;
		}
		return false;
	}

	/**
	 * 获得主服务器的IP地址
	 * @return String IP地址
	 */
	public String getMasterIP()
	{
		String tmpip = localAddr.ip;
		int s = targetAddrs.size();
		for (int i = 0; i < s; i++)
		{
			Addr ad = (Addr) targetAddrs.elementAt(i);
			if (tmpip.compareTo(ad.ip) > 0)
			{
				tmpip = ad.ip;
			}
		}
		return tmpip;
	}

	/**
	 * 获得主服务器的地址
	 * @return String IP地址
	 * @since 1.1.2
	 */
	public String getMasterAddr()
	{
		String tmpip = localAddr.toString();
		int s = targetAddrs.size();
		for (int i = 0; i < s; i++)
		{
			Addr ad = (Addr) targetAddrs.elementAt(i);
			if (tmpip.compareTo(ad.toString()) > 0)
			{
				tmpip = ad.toString();
			}
		}
		return tmpip;
	}

	public String getLocalIP()
	{
		return localIP;
	}

	public String getLocalAddr()
	{
		return localAddr.toString();
	}

	public Vector getActiveBaseIP()
	{
		if (tcpConn == null)
		{
			return new Vector();
		}
		Vector v = tcpConn.getCurrentConnIPs();
		v.addElement(getLocalIP());
		return v;
	}

	public void listConn()
	{
		tcpConn.listConn();
	}

//	private void startRecv()
//	{
//		mainThread = new Thread(this);
//		mainThread.start();
//	}

	/**
	 * 用广播方式发送一条消息
	 */
	public void send(String topic, byte[] infobuf)
		throws UDPBaseException
	{ //System.out.println ("Sending-----["+topic+"]["+infobuf.length+"]") ;
		if (DEBUG)
		{
			log("TcpBase Sending--->>[" + topic + "][" + infobuf.length + "]");
		}

		try
		{
			if (topic.length() > 150)
			{
				throw new UDPBaseException("Topic is too long!!");
			}
			if (infobuf == null || infobuf.length == 0)
			{
				throw new UDPBaseException("Info to be send cannot null!");
			}
			//self recv first
			whenRecvMsg(topic, infobuf);

			HeaderItem topicHeader = new HeaderItem(topic);
			//增加主题头
			byte[] tmpb = HeaderItem.appendHeaderItem(infobuf, topicHeader);
			int s = targetAddrs.size();
			for (int i = 0; i < s; i++)
			{
				Addr ad = (Addr) targetAddrs.elementAt(i);
				tcpConn.sendNoDelay(ad.toString(), tmpb);
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			throw new UDPBaseException("send error:" + ioe.getMessage());
		}

	}

	private void whenRecvMsg(String topic, byte[] info)
	{
		if (!msgTopic.isMatch(topic))
		{
			return;
		}

		if (DEBUG)
		{
			log("TcpBase Recved<<----[" + topic + "][" + info.length + "]");
		}

		int s = callBack.size();
		for (int k = 0; k < s; k++)
		{
			( (UDPBaseCallback) callBack.elementAt(k)).OnMsg(TCPBase.this,
				topic,
				info);
		}
	}

	TcpConnCallback tccb = new TcpConnCallback()
	{
		public void recv(byte[] buf)
		{
			//得到主题
			HeaderItem hi = HeaderItem.fetchHeaderItem(buf);
			String strtop = hi.getContentStr();
			byte[] tmpbytes = HeaderItem.cutHeaderItem(buf, hi);
			whenRecvMsg(strtop, tmpbytes);
		}

		public void recv_I_Am_Here(String targetAddress)
		{
		}

		public void connAccepted(String sorip, int port)
		{
//			if (isMaster())
//			{
//				Vector v = tcpConn.getCurrentConnIPs() ;
//				StringBuffer sb = new StringBuffer() ;
//				int s = 0 ;
//				for(int i = 0 ; i < s ; i ++)
//				{
//					sb.append("|").append((String)v.elementAt(i)) ;
//				}
//
//			}
		}

	};
//	public void run()
//	{
//		try
//		{
//
//			while (true)
//			{
//
//			}
//		}
//		catch (Exception e)
//		{
//			//throw new UDPBaseException ("UDPBase run() error=\n"+e.toString());
//			System.out.println("Something error in recv thread!");
//			e.printStackTrace();
//		}
//	}

}

interface TcpConnCallback
{
	public void recv(byte[] buf);

	public void recv_I_Am_Here(String targetAddress);

	public void connAccepted(String sorip, int port);
}

/**
 * This class implements the communciation function in WebBase adapter
 * using Tcp protocol. It has a server socket which will listen all other
 * WebBase's connection. When a WebBase will send something to another
 * WebBase,it will check whether there is a connection already existed.
 * If not,it will do a connection request to another server socket. After
 * create connection,two WebBases all have a new socket object. Then it
 * will be preserved by them (each other). And two WebBase will create
 * threads each other to listen this socket. We can see,the two WebBase's
 * connection is created.
 *
 *
 */
class TcpConn
	implements Runnable
{
	int SERVER_PORT = 10001;
	//public static final int CLIENT_PORT = 10002 ;

	/**
	 * Each webbase's server,it accept other webbase's connection request
	 */
	ServerSocket serverSocket = null;
	/**
	 * preserve all connection thread which receive message.
	 */
	Hashtable connThreads = new Hashtable();

	Hashtable connSockets = new Hashtable();
	Hashtable connOutputStreams = new Hashtable();

	InetAddress localAddress = null;

	TcpConnCallback tcpConnCallback = null;

	boolean isStarted = false;

	public boolean isStarted()
	{
		return isStarted;
	}

	/**
	 * Contructor of TcpConn.
	 *
	 */
	public TcpConn(TcpConnCallback tccbk)
	{
		try
		{
			serverSocket = new ServerSocket(SERVER_PORT);
			localAddress = serverSocket.getInetAddress();
			tcpConnCallback = tccbk;

			Thread t = new Thread(this);
			t.start();

			isStarted = true;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.out.println("Could not create server socket!!!");
			//System.exit (1);
		}
	}

	public TcpConn(TcpConnCallback tccbk, Addr addr)
	{
		try
		{
			InetAddress ia = InetAddress.getByName(addr.ip);
			serverSocket = new ServerSocket(addr.port, 0, ia);
			SERVER_PORT = addr.port;
			localAddress = serverSocket.getInetAddress();
			tcpConnCallback = tccbk;

			Thread t = new Thread(this);
			t.start();

			isStarted = true;
			//System.out.println ("start server="+localAddress.getHostAddress()+":"+localPort);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.out.println("Could not create server socket!!!");
			//System.exit (1);
		}
	}

	public TcpConn(TcpConnCallback tccbk, int localPort)
	{
		try
		{
			serverSocket = new ServerSocket(localPort);
			SERVER_PORT = localPort;
			localAddress = serverSocket.getInetAddress();
			tcpConnCallback = tccbk;

			Thread t = new Thread(this);
			t.start();

			isStarted = true;
			//System.out.println ("start server="+localAddress.getHostAddress()+":"+localPort);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.out.println("Could not create server socket!!!");
			//System.exit (1);
		}
	}

	public Vector getCurrentConnIPs()
	{
		Vector v = new Vector();
		for (Enumeration en = connSockets.keys(); en.hasMoreElements(); )
		{
			String addr = (String) en.nextElement();
			int i = addr.indexOf(':');
			String ip = addr.substring(0, i);
			if (!v.contains(ip))
			{
				v.addElement(ip);
			}
		}
		return v;
	}

	public void listConn()
	{
		System.out.println("-------The current conntions--------");
		for (Enumeration en = connSockets.keys(); en.hasMoreElements(); )
		{
			System.out.println("##>" + en.nextElement());
		}
		System.out.println("------------------------------------");
	}

	/**
	 * 断开所有的连接，并且停止见听连接
	 */
	public void stopAllConnAndAccept()
		throws IOException
	{
		isStarted = false;
		serverSocket.close();
		serverSocket = null;

		String tmps = null;
		for (Enumeration en = connSockets.keys(); en.hasMoreElements(); )
		{
			tmps = (String) en.nextElement();
			Socket sk = (Socket) connSockets.get(tmps);
			sk.close();
		}
	}

	public void startAccept()
		throws IOException
	{
		serverSocket = new ServerSocket(SERVER_PORT);

		Thread t = new Thread(this);
		t.start();

		isStarted = true;
	}

	public void reconnectTo(String address)
		throws IOException
	{
		String tmps = null;
		for (Enumeration en = connSockets.keys(); en.hasMoreElements(); )
		{
			tmps = (String) en.nextElement();
			if (tmps.indexOf(address) >= 0)
			{
				Socket sk = (Socket) connSockets.get(tmps);
				sk.close();
				break;
			}
		}

		connectTo(address);
	}

	/**
	 * 地址结构为一个字符串 ip:port  例如：166.111.80.80:10001
	 *
	 *
	 */
	synchronized private void connectTo(String address)
		throws IOException
	{
		if (!isStarted)
		{
			throw new IOException("The TcpAdapter is not start yet!");
		}

		System.out.println("Try Connect To:" + address);

		int i = address.indexOf(':');
		String ip;
		int port;
		if (i < 0)
		{
//			ip = address;
//			port = SERVER_PORT;
			throw new IOException("address [" + address + "] has no port!");
		}
		else
		{
			ip = address.substring(0, i);
			port = Integer.parseInt(address.substring(i + 1));
		}
		//Socket tmpsk = new Socket (ip,Integer.parseInt(port),localAddress,CLIENT_PORT) ;
		//Socket tmpsk = new Socket (address,SERVER_PORT,localAddress,CLIENT_PORT) ;
		Socket tmpsk = null;
		try
		{
			tmpsk = new Socket(ip, port);
			createConnAndThread(ip, port, tmpsk);
		}
		catch (Exception e)
		{
			if (tmpsk != null)
			{
				System.out.println("Close Socket!!!");
				tmpsk.close();
			}
			//e.printStackTrace();
		}
	}

	/*
	  public void connectTo (String address,int localPort)
	 throws IOException
	  {
	 int i = address.indexOf (':') ;
	 String ip = address.substring (0,i) ;
	 String port = address.substring (i+1) ;
	 Socket tmpsk = null;
	 if (localPort>0)
	  tmpsk = new Socket (ip,Integer.parseInt(port),localAddress,localPort) ;
	 else
	  tmpsk = new Socket (ip,Integer.parseInt(port)) ;
	 createConnAndThread (address,tmpsk) ;
	  }
	 */
	/**
	 * Main thread of serverSocket listener,which accept
	 * other webbase's connection request.
	 */
	public void run()
	{
		while (true)
		{
			try
			{
				Socket tmpsk = serverSocket.accept();
				String tmps = getClientStringAddress(tmpsk);
				createConnAndThread(getClientHost(tmpsk), getClientPort(tmpsk),
									tmpsk);
//				tcpConnCallback.connAccepted(getClientHost(tmpsk),
//											 getClientPort(tmpsk));
				System.out.println("recv conn=" + tmps);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
				System.out.println("Something error[perhaps stoped] in thread=" +
								   ioe.toString());
				break;
			}
		}
	}

	/**
	 * After accept Tcp connection,there is a new socket created.
	 * using this function to get other webbase's address string
	 * which is like "ip:port"
	 */
	private String getClientStringAddress(Socket sk)
	{
		return sk.getInetAddress().getHostAddress() + ":" + sk.getPort();
	}

	/**
	 * get other webbase's host ip string.
	 */
	private String getClientHost(Socket sk)
	{
		return sk.getInetAddress().getHostAddress();
	}

	/**
	 * get other webbase's connection port string.
	 */
	private int getClientPort(Socket sk)
	{
		return sk.getPort();
	}

	/**
	 * When connectTo or main thread create or receive a connection.
	 * call this function to preserve some information and create a
	 * reveiving thread.
	 */
	private void createConnAndThread(String address, int port, Socket sk)
		throws IOException
	{
		connSockets.put(address + ":" + port, sk);
		connOutputStreams.put(address + ":" + port, sk.getOutputStream());
		ConnRecvListener crl = new ConnRecvListener(address, port, sk);
		connThreads.put(address + ":" + port, crl);
		Thread t = new Thread(crl);
		t.start();
	}

	/**
	 * sending a message (bytes stream) using target ip address.
	 * if the connection is not created,then it will call connectTo
	 * function to create the connection first.
	 */
	public void send(String targetAddress, byte[] msgbuf)
		throws IOException
	{
		if (!connOutputStreams.containsKey(targetAddress))
		{
//			System.out.println("Before connectTo --------->>>");
			connectTo(targetAddress);
//			System.out.println("End connectTo --------->>>");
			if (!connOutputStreams.containsKey(targetAddress))
			{
				throw new IOException("Cannot connect to [" + targetAddress +
									  "]!");
			}
		}

		//Socket tmpsk = (Socket)connSockets.get (targetAddress);
		OutputStream os = (OutputStream) connOutputStreams.get(targetAddress);
		synchronized (os)
		{
			//System.out.println("start send-------------");
			//tmpsk.getOutputStream () ;
			byte[] tmpb = null;
			if (msgbuf == null)
			{
				tmpb = intToBytes(0);
				os.write(tmpb);
				//System.out.println("end am here send-------------");
				return;
			}

			tmpb = intToBytes(msgbuf.length);
			os.write(tmpb);
			os.write(msgbuf);
			//System.out.println("end send-------------");
		}
	}

	/**
	 * 该方法发送信息时，如果发现连接没有建立。则立刻返回。以保证外界程序没有被阻塞
	 */
	public void sendNoDelay(String targetAddress, byte[] msgbuf)
		throws IOException
	{
		if (!connOutputStreams.containsKey(targetAddress))
		{
			return;
		}

		//Socket tmpsk = (Socket)connSockets.get (targetAddress);
		OutputStream os = (OutputStream) connOutputStreams.get(targetAddress);
		synchronized (os)
		{
			if (TCPBase.DEBUG)
			{
				TCPBase.log("sendNoDelay  start send------------->>");
				//tmpsk.getOutputStream () ;
			}

			byte[] tmpb = null;
			if (msgbuf == null)
			{
				tmpb = intToBytes(0);
				os.write(tmpb);
				//System.out.println("end am here send-------------");
				return;
			}

			tmpb = intToBytes(msgbuf.length);
			os.write(tmpb);
			os.write(msgbuf);
			//System.out.println("end send-------------");
		}
	}

	/**
	 *
	 */
	public void send_I_Am_Here(String targetAddress)
		throws IOException
	{
		send(targetAddress, null);
	}

	static int bytesToInt(byte[] bytes)
	{
		if (bytes == null || bytes.length != 4)
		{
			throw new IllegalArgumentException("byte array size must be 4!");
		}

		int i = 0;
		i = ( (bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
		i = (i << 8) | (bytes[2] & 0xFF);
		i = (i << 8) | (bytes[3] & 0xFF);

		return i;
	}

	static byte[] intToBytes(int i)
	{
		// int is 32bits, 4Bytes
		byte[] bytes = new byte[4];

		bytes[3] = (byte) (i & 0xFF);

		i = i >>> 8;
		bytes[2] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[1] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[0] = (byte) (i & 0xFF);

		return bytes;
	}

	public String toString()
	{
		return "Tcp Broadcast";
	}

	/**
	 * Tcp connection listening thread.
	 */
	class ConnRecvListener
		implements Runnable
	{
		byte[] lengthBuf = new byte[4];
		byte[] contentBuf = null;

		byte[] b = new byte[1];

		Socket sk = null;
		InputStream input = null;

		String targetAddress = null;
		int port = -1;

		public ConnRecvListener(String targetAddress, int port, Socket sk)
			throws IOException
		{
			this.port = port;
			this.sk = sk;
			input = sk.getInputStream();
			this.targetAddress = targetAddress;
		}

		int counter = 0, tmpi;

		public void run()
		{
			try
			{
				while (true)
				{
					//read header
					counter = 0;

					while ( (tmpi = input.read(lengthBuf, counter, 4 - counter)) >
						   0)
					{
						counter += tmpi;
					}
					if (tmpi == -1)
					{
						break;
					}
					if (counter == -1)
					{
						break;
					}
					int len = bytesToInt(lengthBuf);
					//System.out.println("TCP conn----------get length="+len);
					//receive I am here
					if (len == 0)
					{
						if (isStarted)
						{
							tcpConnCallback.recv_I_Am_Here(targetAddress);
							//System.out.println("TCP conn----------receive here am i="+targetAddress);
							continue;
						}
					}
					//System.out.println("recv length="+len);
					//read content
					contentBuf = new byte[len];
					counter = 0;

					while ( (tmpi = input.read(contentBuf, counter,
											   len - counter)) > 0)
					{
						counter += tmpi;
					}

					if (counter == -1)
					{
						break;
					}

					//System.out.println ("----TCP Conn received======"+new String(contentBuf)) ;
					tcpConnCallback.recv(contentBuf);
					//System.out.println ("----end of TCP Conn-------") ;
				}
			}
			catch (IOException ioe)
			{
				System.out.println("Connection error=" + ioe.toString());
				System.out.println("Disconnection it.......");
			}

			System.out.println("receive error =[end of stream]!!!");
			//clear hash buffer
			try
			{
				input.close();
				sk.close();
			}
			catch (IOException ioe)
			{
				System.out.println("Disconnection error=" + ioe.toString());
			}
			//System.out.println("removing="+targetAddress+":"+port);
			connThreads.remove(targetAddress + ":" + port);
			connSockets.remove(targetAddress + ":" + port);
			connOutputStreams.remove(targetAddress + ":" + port);
		}
	}

}

class Addr
{
	String ip = null;
	String hostName = null;
	int port;
	public String toString()
	{
		if (hostName != null)
		{
			return hostName + ":" + port;
		}
		else
		{
			return ip + ":" + port;
		}
	}
}
