package com.dw.net.broadcast;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.*;

/**
 * 统一实现UDP的传输接口
 */
public class GroupUDPBase
	extends UDPBase
	implements SoleRunnable
{

	private String myID = null;
	private String localIP = null;

//	MsgTopic msgTopic = new MsgTopic();

	QuickQueue quickQueue = new QuickQueue();
	ProcMsgThd procMsgThd = null;
	SoleThread mainThread = null;

	static final long TIME_OUT = 1000;
	static final int LOOP_SEND_INTERVAL = 5;
	/**
	 * 获取一个唯一标识
	 * 其基本思想是，先获得本地ip地址，然后获得本地毫秒时间，加一个随机数
	 */
	private static int __count = 0;
	String getOneID0()
		throws UDPBaseException
	{
		try
		{
			//String strip = InetAddress.getLocalHost().getHostAddress();
			String strtime = "" + (System.currentTimeMillis() + __count);
			__count++;
			if (__count >= 100000)
			{
				__count = 0;
			}
			return localIP + "_" + strtime;
		}
		catch (Exception e)
		{
			throw new UDPBaseException("UDPBase geOneID() error=\n" +
									   e.toString());
		}
	}

	/**
	 * 通过IdGenerator产生的id
	 * @return 新的id
	 */
	String getOneID()
	{
		return IdGenerator.createNewId();
	}

	//////////////////////////////////////////////////////////////
	protected static int RECV_PORT = 6782;
	protected static int RECV_ACK_PORT = 6783;

	protected boolean bInitNull = false;

	protected static String GROUP_BROADCAST_ADDR = "230.0.0.2";
	protected String BROADCAST_ADDR = "192.168.0.255";
	protected DatagramSocket sendSocket = null;
	//protected MulticastSocket recvSocket = null ;
	protected DatagramSocket recvSocket = null;
	//protected MulticastSocket recvAckSocket = null ;
	protected InetAddress group = null;
	//protected UDPBaseCallback callBack = null ;
//	protected Vector callBack = new Vector();
	private boolean bGroup = false;

	GroupUDPBase()
		throws UDPBaseException
	{
		initNull();
	}

	GroupUDPBase(boolean bgroup)
		throws UDPBaseException
	{
		if (bgroup)
		{
			initGroup();
		}
		else
		{
			initNet();
		}
	}

	GroupUDPBase(String virtualip)
	{
		initVirtual(virtualip);
	}

	/**
	 * 启动初始化，不接受任何信息包，也不发送任何信息包
	 */
	public void initNull()
		throws UDPBaseException
	{
		try
		{
			localIP = InetAddress.getLocalHost().getHostAddress();
			int i = localIP.lastIndexOf('.');
			BROADCAST_ADDR = localIP.substring(0, i) + ".255";

			bInitNull = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new UDPBaseException("UDPBase init() error=\n" + e.toString());
		}
	}

	/**
	 * 启动初始化，他确定网络中有多少个其它UDPBase和相关的信息
	 */
	public void initNet()
		throws UDPBaseException
	{
		try
		{
			mainThread = new SoleThread(this);

			localIP = InetAddress.getLocalHost().getHostAddress();
			int i = localIP.lastIndexOf('.');
			BROADCAST_ADDR = localIP.substring(0, i) + ".255";
			//System.out.println ("lip=="+localIP) ;
			//sendSocket = new DatagramSocket () ;
			//recvSocket = new MulticastSocket (RECV_PORT) ;
			recvSocket = new DatagramSocket(RECV_PORT);
			sendSocket = recvSocket;
			//recvAckSocket = new MulticastSocket (RECV_ACK_PORT) ;
			group = InetAddress.getByName(BROADCAST_ADDR);
			//recvSocket.joinGroup (group) ;
			//recvAckSocket.joinGroup (group) ;

			procMsgThd = new ProcMsgThd();
			procMsgThd.start();
			//
			mainThread.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new UDPBaseException("UDPBase init() error=\n" + e.toString());
		}
	}

	public void initGroup()
		throws UDPBaseException
	{
		try
		{
			mainThread = new SoleThread(this);

			sendSocket = new DatagramSocket();
			recvSocket = new MulticastSocket(RECV_PORT);
			//recvAckSocket = new MulticastSocket (RECV_ACK_PORT) ;
			group = InetAddress.getByName(GROUP_BROADCAST_ADDR);
			( (MulticastSocket) recvSocket).joinGroup(group);
			//recvAckSocket.joinGroup (group) ;
			localIP = InetAddress.getLocalHost().getHostAddress();

			procMsgThd = new ProcMsgThd();
			procMsgThd.start();
			//
			mainThread.start();

			bGroup = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new UDPBaseException("UDPBase init() error=\n" + e.toString());
		}
	}

	public void initVirtual(String virtualip)
		throws UDPBaseException
	{
		if (virtualip == null)
		{
			virtualip = "vip:" + System.currentTimeMillis();
		}
		try
		{
			mainThread = new SoleThread(this);

			sendSocket = new DatagramSocket();
			recvSocket = new MulticastSocket(RECV_PORT);
			//recvAckSocket = new MulticastSocket (RECV_ACK_PORT) ;
			group = InetAddress.getByName(GROUP_BROADCAST_ADDR);
			( (MulticastSocket) recvSocket).joinGroup(group);
			//recvAckSocket.joinGroup (group) ;
			localIP = virtualip;

			procMsgThd = new ProcMsgThd();
			procMsgThd.start();
			//
			mainThread.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new UDPBaseException("UDPBase init() error=\n" + e.toString());
		}
	}

	public boolean isGroupMultiCast()
	{
		return bGroup;
	}

	public String getLocalIP()
	{
		return localIP;
	}

	public String getLocalAddr()
	{
		return localIP;
	}

	public Vector getActiveBaseIP()
	{
		throw new java.lang.UnsupportedOperationException(
			"Method getActiveBaseIP() not yet implemented.");
	}

	public void addTopic(String topic)
	{
		if (topic == null)
		{
			return;
		}
		msgTopic.add(topic);
	}

	public void addTopic(String[] topics)
	{
		for (int i = 0; i < topics.length; i++)
		{
			msgTopic.add(topics[i]);
		}
	}

	public String getTopicsStr()
	{
		return msgTopic.getTopicsStr();
	}

	public void removeTopic(String topic)
	{
		msgTopic.remove(topic);
	}

	private void startRecv()
	{

	}

	//private final static int MAX_PACKET_LENGTH = 8092 ;
	/**
	 * 在windows和sun solaris上长度=8029
	 * 在IBM AIX上=1461
	 */
	private final static int MAX_PACKET_LENGTH = 1200;
	private final static int MAX_HEADER_LENGTH = 200;
	/**
	 * 用广播方式发送一条消息
	 */
	public void send(String topic, byte[] infobuf)
		throws UDPBaseException
	{ //System.out.println ("Sending-----["+topic+"]["+infobuf.length+"]") ;
		//log.log("Sending-----["+topic+"]["+infobuf.length+"]") ;
		if (bInitNull)
		{
			return;
		}

		if (DEBUG)
		{
			log("UdpBase Sending--->>[" + topic + "][" + infobuf.length + "]");
		}

		if (topic.length() > 150)
		{
			throw new UDPBaseException("Topic is too long!!");
		}
		if (infobuf == null || infobuf.length == 0)
		{
			throw new UDPBaseException("Info to be send cannot null!");
		}

		try
		{
			String tmpid = getOneID();
			//System.out.println ("ONE ID="+tmpid) ;
			int bs = infobuf.length / MAX_PACKET_LENGTH;
			int sy = infobuf.length % MAX_PACKET_LENGTH;
			int pknum = bs + (sy == 0 ? 0 : 1);
			DatagramPacket packet = null;

			HeaderItem topicHeader = new HeaderItem(topic);
			HeaderItem idHeader = new HeaderItem(tmpid);
			byte[] tmpb = null;

			for (int i = 0; i < bs; i++)
			{
				//if (i>0)//循环发送信息每次发送间加间隔，以提高发送成功率
				Thread.sleep(LOOP_SEND_INTERVAL);
				//增加顺序头
				HeaderItem orderHeader = new HeaderItem("" + pknum + "_" + i);
				tmpb = HeaderItem.appendHeaderItem(
					infobuf, i * MAX_PACKET_LENGTH, MAX_PACKET_LENGTH,
					orderHeader);
				//增加唯一id
				tmpb = HeaderItem.appendHeaderItem(tmpb, idHeader);
				//增加主题头
				tmpb = HeaderItem.appendHeaderItem(tmpb, topicHeader);
				packet = new DatagramPacket(
					tmpb,
					0,
					tmpb.length,
					group,
					RECV_PORT);
				//System.out.println("send package");
				sendSocket.send(packet);

			}

			if (sy > 0)
			{
				Thread.sleep(LOOP_SEND_INTERVAL);
				//增加顺序头
				HeaderItem orderHeader = new HeaderItem("" + pknum + "_" + bs);
				tmpb = HeaderItem.appendHeaderItem(
					infobuf, bs * MAX_PACKET_LENGTH, sy, orderHeader);
				//增加唯一id
				tmpb = HeaderItem.appendHeaderItem(tmpb, idHeader);
				//增加主题头
				tmpb = HeaderItem.appendHeaderItem(tmpb, topicHeader);
				packet = new DatagramPacket(
					tmpb,
					0,
					tmpb.length,
					group,
					RECV_PORT);

				//System.out.println("send.."+new String(tmpb));
				sendSocket.send(packet);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new UDPBaseException("UDPBase send() error=\n" + e.toString());
		}
	}

	//boolean ackReceived ;
	/*
	 * 可靠地发送信息，要求每个ip报都要有一个回应
	 */
	/*
	  public void sendReliable ()
	 throws UDPBaseException
	  {
	 ackReceived = false ;
	 try
	 {
	  if (topic.length()>50)
	   throw new RuntimeException ("Topic is too long!!");
	  String tmpid = getOneID ();
	  int bs = infobuf.length / MAX_PACKET_LENGTH ;
	  int sy = infobuf.length % MAX_PACKET_LENGTH ;
	  int pknum = bs + (sy==0?0:1) ;
	  DatagramPacket packet = null ;
	  HeaderItem topicHeader = new HeaderItem (topic);
	  HeaderItem idHeader = new HeaderItem (tmpid);
	  byte[] tmpb = null ;
	  for (int i = 0 ; i < bs ; i ++)
	  {
	   //if (i>0)//循环发送信息每次发送间加间隔，以提高发送成功率
	 Thread.sleep (LOOP_SEND_INTERVAL) ;
	   //增加顺序头
	   HeaderItem orderHeader = new HeaderItem (""+pknum+"_"+i);
	   tmpb = HeaderItem.appendHeaderItem (
	 infobuf,i*MAX_PACKET_LENGTH,MAX_PACKET_LENGTH,orderHeader);
	   //增加唯一id
	   tmpb = HeaderItem.appendHeaderItem (tmpb,idHeader);
	   //增加主题头
	   tmpb = HeaderItem.appendHeaderItem (tmpb,topicHeader);
	   packet = new DatagramPacket(
	  tmpb,
	  0,
	  tmpb.length,
	  group,
	  RECV_PORT);
	   //System.out.println("send package");
	   sendSocket.send(packet);
	  }
	  if (sy>0)
	  {
	   Thread.sleep (LOOP_SEND_INTERVAL) ;
	   //增加顺序头
	   HeaderItem orderHeader = new HeaderItem (""+pknum+"_"+bs);
	   tmpb = HeaderItem.appendHeaderItem (
	 infobuf,bs*MAX_PACKET_LENGTH,sy,orderHeader);
	   //增加唯一id
	   tmpb = HeaderItem.appendHeaderItem (tmpb,idHeader);
	   //增加主题头
	   tmpb = HeaderItem.appendHeaderItem (tmpb,topicHeader);
	   packet = new DatagramPacket(
	  tmpb,
	  0,
	  tmpb.length,
	  group,
	  RECV_PORT);
	   //System.out.println("send.."+new String(tmpb));
	   sendSocket.send(packet);
	  }
	 }
	 catch(Exception e)
	 {
	  throw new UDPBaseException ("UDPBase send() error=\n"+e.toString());
	 }
	  }
	 */
	public void setRecvCallback(UDPBaseCallback udpbck)
	{
		if (callBack.contains(udpbck))
		{
			return;
		}
		callBack.addElement(udpbck);
		//callBack = udpbck ;
	}

	public void unsetRecvCallback(UDPBaseCallback udpbck)
	{
		if (!callBack.contains(udpbck))
		{
			return;
		}
		callBack.remove(udpbck);
		//callBack = udpbck ;
	}

	public void release()
	{
		System.out.println("DefaultUDPBase release because of sole!");
		destory();
	}

	public void run(SoleThread st)
	{
		try
		{
			byte[] buf = new byte[MAX_HEADER_LENGTH + MAX_PACKET_LENGTH];
			int len;
			byte[] tar = null;

			while(st.isSole())
			{
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				//System.out.println("recv...");
				recvSocket.receive(packet);

				len = packet.getLength();
				tar = new byte[len];
				System.arraycopy(buf, 0, tar, 0, len);

				//processMsg (tar);
				quickQueue.put(tar);
			}
		}
		catch (Exception e)
		{
			//throw new UDPBaseException ("UDPBase run() error=\n"+e.toString());
			System.out.println("Something error in recv thread!");
			e.printStackTrace();
		}
	}

	Hashtable idMsgBuf = new Hashtable();

	class ProcMsgThd
		extends Thread
	{
		boolean brun = true;

		byte[] buf = null;
		long start, end;

		public void stop0()
		{
			brun = false;
		}

		public void run()
		{
			while (brun)
			{
				start = System.currentTimeMillis();
				buf = quickQueue.get();
				end = System.currentTimeMillis();
				if ( (end - start) > TIME_OUT) //删除过期内容
				{
					idMsgBuf.clear();
				}
				processMsg(buf);
			}
		}
	}

	private void processMsg(byte[] msgbyte)
	{
		//得到主题
		HeaderItem hi = HeaderItem.fetchHeaderItem(msgbyte);
		String strtop = hi.getContentStr();
		//System.out.println("recved header="+strtop);
		if (!msgTopic.isMatch(strtop))
		{
			return;
		}
		byte[] tmpbytes = HeaderItem.cutHeaderItem(msgbyte, hi);

		//得到唯一id
		hi = HeaderItem.fetchHeaderItem(tmpbytes);
		String tmpid = hi.getContentStr();
		//System.out.println("recved id="+tmpid);
		Hashtable tmph = (Hashtable) idMsgBuf.get(tmpid);
		if (tmph == null)
		{
			tmph = new Hashtable();
			idMsgBuf.put(tmpid, tmph);
		}
		tmpbytes = HeaderItem.cutHeaderItem(tmpbytes, hi);

		//得到顺序号
		hi = HeaderItem.fetchHeaderItem(tmpbytes);
		tmpbytes = HeaderItem.cutHeaderItem(tmpbytes, hi);
		String strord = hi.getContentStr();
		//System.out.println("recved order id="+strord);
		tmph.put(strord, tmpbytes);
		//System.out.println("recved ="+new String(tmpbytes));
		int d = strord.indexOf('_');
		int pknum = Integer.parseInt(strord.substring(0, d));
		if (tmph.size() == pknum)
		{ //一个主题的信息收齐，通知顶层
			int s = callBack.size();
			byte[] tmprecv = merge(tmph);
			//System.out.println ("Recving-----"+strtop+"["+tmprecv.length+"]") ;
			if (DEBUG)
			{
				log("UdpBase Recved<<----[" + strtop + "][" + tmprecv.length +
					"]");
			}

			for (int k = 0; k < s; k++)
			{
				( (UDPBaseCallback) callBack.elementAt(k)).OnMsg(this, strtop,
					tmprecv);
			}

			idMsgBuf.remove(tmpid);
		}
	}

	private byte[] merge(Hashtable msght)
	{
		int s = msght.size();
		//System.out.println("--------------------s="+s);
		byte[] tmpb = (byte[]) msght.get("" + s + "_" + (s - 1));

		int tmpi = tmpb.length;
		//System.out.println("-----------------1tmpi="+tmpi);
		int len = (s - 1) * MAX_PACKET_LENGTH + tmpi;
		byte[] tmpbuf = new byte[len];
		//System.out.println("-----------------1---");
		System.arraycopy(tmpb, 0, tmpbuf, (s - 1) * MAX_PACKET_LENGTH, tmpi);
		for (int i = 0; i < (s - 1); i++)
		{
			tmpb = (byte[]) msght.get("" + s + "_" + i);
			System.arraycopy(tmpb, 0, tmpbuf, i * MAX_PACKET_LENGTH,
							 MAX_PACKET_LENGTH);
		}
		return tmpbuf;
	}

	public void finalize()
	{
		/*
		   if(group!=null)
		   {
		 try
		 {
		  ((MulticastSocket)recvSocket).leaveGroup (group);
		 }
		 catch(IOException ioe)
		 {}
		   }
		   sendSocket.close ();
		   recvSocket.close ();
		 */
		destory();
	}

	public void destory()
	{
		procMsgThd.stop0();
		procMsgThd.interrupt();
		mainThread.interrupt();
		if (group != null)
		{
			try
			{
				if (recvSocket instanceof MulticastSocket)
				{
					( (MulticastSocket) recvSocket).leaveGroup(group);
				}
			}
			catch (IOException ioe)
			{}
		}

		sendSocket.close();
		recvSocket.close();
	}

	public String toString()
	{
		return "["+getClass().getName()+"]UDP Broad cast [group=" + bGroup + "]";
	}

	//public
	/*
	 */
	/*
	 public static void main (String args[])
	 {
	  try
	  {
	   if (args[0].equals("recv"))
	   {//java com.css.push.udp.UDPBase recv 100.1 100.2 100.3
	 UDPBase ub = new UDPBase ();
	 ub.addTopic (args);
	 ub.setRecvCallback (new TestUDPCallback());
	 ub.startRecv ();
	   }
	   else if (args[0].equals("send"))
	   {//java com.css.push.udp.UDPBase send 100.1 test.dat
	 UDPBase ub = new UDPBase ();
	 //for (int i = 0 ; i<10;i++)
	 {
	  //System.out.println("Sending-->"+(args[2]+"["+args[1]+"]"));
	  //ub.send (args[1],(args[2]+"["+args[1]+"]").getBytes());
	  if (args.length<=2)
	   ub.send (args[1],new byte[0]);
	  else
	   ub.send (args[1],args[2].getBytes());
	  //Thread.sleep (20);
	 }
	 //Thread.sleep (1000);
	 //ub.send (args[1],new String("end").getBytes());
	 System.exit (0);
	   }
	   else if (args[0].equals("sendfile"))
	   {//java com.css.push.udp.UDPBase send 100.1 test.dat
	 UDPBase ub = new UDPBase ();
	 File f = new File (args[2]);
	 FileInputStream fis = new FileInputStream (f);
	 byte[] buf = new byte[(int)f.length()];
	 fis.read(buf);
	 fis.close ();
	 //while (true)
	 for (int i = 0 ; i<10;i++)
	 {
	  //System.out.println("Sending-->"+(args[2]+"["+args[1]+"]"));
	  //ub.send (args[1],(args[2]+"["+args[1]+"]").getBytes());
	  ub.send (args[1],buf);
	  //Thread.sleep (20);
	 }
	 //Thread.sleep (1000);
	 //ub.send (args[1],new String("end").getBytes());
	 System.exit (0);
	   }
	  }
	  catch(Exception e)
	  {
	   e.printStackTrace ();
	  }
	 }
	 */

}
