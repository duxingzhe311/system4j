package com.dw.net.broadcast;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.*;

/**
 * 统一实现UDP的传输接口
 */
public abstract class UDPBase
{
	protected static boolean DEBUG = true;

	public static boolean TCP = false;
	public static boolean GROUP = false;
	public static boolean NET = false;
	public static boolean VIR = false;

	public static HashSet IP_SCOPE = new HashSet();

	static
	{
		try
		{
			TCP = ( ("tcp".equals(Configuration.getProperty("udpbase.style"))) ? true : false);

			GROUP = ( ("group".equals(Configuration.getProperty("udpbase.style"))) ? true : false);

			NET = ( ("default".equals(Configuration.getProperty("udpbase.style"))) ? true : false);

			VIR = ( ("virtual".equals(Configuration.getProperty("udpbase.style"))) ? true : false);

			DEBUG = ( ("true".equals(Configuration.getProperty(
				"platform.release"))) ? false : true);

			String ipscope = Configuration.getProperty("topic_comm.ip.scope");
			if (ipscope != null && !ipscope.equals(""))
			{
				StringTokenizer st = new StringTokenizer(ipscope, "|, ");
				int c = st.countTokens();
				if (c >= 0)
				{
					while (st.hasMoreTokens())
					{
						IP_SCOPE.add(st.nextToken());
					}
				}
			}
		}
		catch (Throwable _t)
		{
			_t.printStackTrace();
		}
	}

	//public static boolean GROUP = false ;
	//存放所有的UDPBase基本信息内容
	//static Hashtable allUDPBase = new Hashtable ();
	//
	private static Log log = null;
	static
	{
		try
		{
			if (!DEBUG)
			{
				log = Log.getLog("UDPBase.log");
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	private static void log(String str)
	{
		if (log == null)
		{
			System.out.println(str);
		}
		else
		{
			log.log(str);
		}
	}

	protected static void log(Throwable t)
	{
		if (log == null)
		{
			t.printStackTrace();
		}
		else
		{
			log.log(t);
		}
	}

	protected static void log(Object o)
	{
		if (log == null)
		{
			System.out.println(o.toString());
		}
		else
		{
			log.log(o);
		}
	}

	static UDPBase udpBase = null;

	static UDPBase defaultUdpBase = null;

	static UDPBase groupUdpBase = null;
	/**
	 * 为系统提供一个能够同时使用Tcp和udp方式的手段。
	 * 但tcp和udp之间是不能进行通信的。
	 * @param bgroup 是否采用组播
	 * @return 处理对象
	 */
	public synchronized static UDPBase getDefaultUDPBase()
	{
		//to make sure the main udp base is startd!
		//getUDPBase();

		if (defaultUdpBase != null)
		{
			return defaultUdpBase;
		}

		defaultUdpBase = new LocalUDPBase(false);
		return defaultUdpBase;

//		if ( (udpBase instanceof DefaultUDPBase)
//			&& ! ( (DefaultUDPBase) udpBase).isGroupMultiCast())
//		{
//			defaultUdpBase = udpBase;
//			return defaultUdpBase;
//		}
//		else
//		{
//			defaultUdpBase = new GroupUDPBase(false);
//			return defaultUdpBase;
//		}
	}

	public synchronized static UDPBase getGroupUDPBase()
	{
		if (groupUdpBase != null)
		{
			return groupUdpBase;
		}
		groupUdpBase = new GroupUDPBase(true);
		return groupUdpBase;
		//to make sure the main udp base is startd!
//		getUDPBase();
//
//		if (groupUdpBase != null)
//		{
//			return groupUdpBase;
//		}
//
//		if ( (udpBase instanceof DefaultUDPBase)
//			&& ( (DefaultUDPBase) udpBase).isGroupMultiCast())
//		{
//			groupUdpBase = udpBase;
//			return groupUdpBase;
//		}
//		else
//		{
//			groupUdpBase = new GroupUDPBase(true);
//			return groupUdpBase;
//		}
	}

	/**
	 * 获得通信对象，如果配置文件中没有任何内容，那么返回的对象没有通信能力。
	 * 如果是GROUP类型，则他有组播能力
	 * 如果是default类型，则它在局域网内有广播能力
	 * 如果是virtual类型，则它可以在同一台机器上模拟多个虚拟地址的能力。
	 * @return 唯一的通信主类对象
	 */
	public synchronized static UDPBase getUDPBase()
	{
		if (udpBase == null)
		{
			try
			{
				if (TCP)
				{
					udpBase = new TCPBase();
				}
				else if (GROUP)
				{
					udpBase = new DefaultUDPBase(true);
				}
				else if (NET)
				{
					udpBase = new DefaultUDPBase(false);
				}
				else if (VIR)
				{
					udpBase = getVirtualIPUDPBase("" + System.currentTimeMillis());

				}
				else
				{
					udpBase = new DefaultUDPBase();
				}
			}
			catch (Exception e)
			{
				log(e);
				System.out.println(
					"[****Broadcast Warning*****]:UDPBase init error:perhaps it send nothing!");
				if (udpBase == null)
				{
					udpBase = new DefaultUDPBase();
				}
			}
		}
		return udpBase;
	}

	/**
	 * 得到使用虚拟地址的主类，该方法用来单机情况下，模拟出两个主服务器时使用。
	 * 以方便调试使用。
	 * @param virtualip 外界应用程序自己指定的地址
	 * @return 通信主类
	 * @throws UDPBaseException 构造出错
	 */
	public static UDPBase getVirtualIPUDPBase(String virtualip)
		throws UDPBaseException
	{
		UDPBase tmpu = new DefaultUDPBase(virtualip);
		return tmpu;
	}

	/**
	 * 处理消息主题的对象
	 */
	protected MsgTopic msgTopic = new MsgTopic();
	/**
	 * 存放所有的回调对象
	 */
	protected Vector callBack = new Vector();
	/**
	 * 得到本地的ip地址
	 * @return 本地ip地址
	 */
	public abstract String getLocalIP();

	/**
	 * 得到本地地址，由于TCP方式支持单机使用多个端口。所以该方法
	 * 返回了更加详细的地址 ip:port
	 * @return 包含ip:port的地址串
	 */
	public abstract String getLocalAddr();

	/**
	 * 获得当前活动的所有主机IP地址
	 * @return 所有地址
	 */
	public abstract Vector getActiveBaseIP();

	/**
	 * 判断是否是所有活动服务器中ip最小的服务器
	 * @return true表示是主服务器，false表示不是主服务器
	 */
	public boolean isActiveMaster()
	{
		String mip = getActiveMasterIP();
		if (mip == null)
		{
			return false;
		}
		if (mip.equals(getLocalIP()))
		{
			return true;
		}
		return false;
	}

	/**
	 * 获得所有活动服务器中主服务器的IP地址
	 * @return String IP地址
	 */
	public String getActiveMasterIP()
	{
		Vector v = getActiveBaseIP();
		int s = v.size();
		if (s <= 0)
		{
			return null;
		}
		String tmpip = (String) v.elementAt(0);
		for (int i = 1; i < s; i++)
		{
			String ss = (String) v.elementAt(i);
			if (tmpip.compareTo(ss) > 0)
			{
				tmpip = ss;
			}
		}
		return tmpip;
	}

	/**
	 * 增加监听消息的主题。
	 * @param topic 主题字符串
	 */
	public void addTopic(String topic)
	{
		if (topic == null)
		{
			return;
		}
		msgTopic.add(topic);
	}

	/**
	 * 增加多个监听消息的主题
	 * @param topics 主题字符串数组
	 */
	public void addTopic(String[] topics)
	{
		for (int i = 0; i < topics.length; i++)
		{
			msgTopic.add(topics[i]);
		}
	}

	/**
	 * 得到所有的被监听主题的描述信息，该方法
	 * @return 主题描述信息
	 */
	public String getTopicsStr()
	{
		return msgTopic.getTopicsStr();
	}

	/**
	 * 删除一个主题
	 * @param topic 主题
	 */
	public void removeTopic(String topic)
	{
		msgTopic.remove(topic);
	}

	/**
	 * 用广播方式发送一条消息
	 * @param topic 被发送消息的主题
	 * @param infobuf 被发送消息体
	 * @throws UDPBaseException 发送信息发生错误
	 */
	public abstract void send(String topic, byte[] infobuf)
		throws UDPBaseException;

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

	/**
	 * 设置处理接收到消息的对象,也就是监听消息的处理程序
	 * @param udpbck 回调对象
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

	/**
	 * 取消一个监听消息处理程序
	 * @param udpbck 回调对象
	 */
	public void unsetRecvCallback(UDPBaseCallback udpbck)
	{
		if (!callBack.contains(udpbck))
		{
			return;
		}
		callBack.remove(udpbck);
		//callBack = udpbck ;
	}

//	public abstract void run();

	public static void main(String args[])
	{
		try
		{

			UDPBase ub = null;

//			if (args.length == 1 && args[0].equals("group"))
//			{
//				GROUP = true;
//				NET = false;
//				VIR = false;
//			}
//			else if (args.length == 2 && args[0].equals("virtual"))
//			{
//				GROUP = false;
//				NET = false;
//				VIR = true;
//			}
//			else if (args.length == 2 && args[0].equals("tcp"))
//			{
//				TCP = true;
//			}
			if (args[0].equals("group"))
			{
				GROUP = true;
				NET = false;
				VIR = false;
			}
			else if (args[0].equals("virtual"))
			{
				GROUP = false;
				NET = false;
				VIR = true;
			}
			else if (args[0].equals("tcp"))
			{
				TCP = true;
			}
			else
			{
				GROUP = false;
				NET = true;
				VIR = false;
			}

			ub = UDPBase.getUDPBase();
			ub.setRecvCallback(new TestUDPCallback("Nconfig"));
//			UDPBase.getDefaultUDPBase().setRecvCallback(new TestUDPCallback(
//				"Ndefault"));
			UDPBase.getGroupUDPBase().setRecvCallback(new TestUDPCallback(
				"Ngroup"));
			//ub.startRecv();

			String inputLine;
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
				System.in));

			while ( (inputLine = in.readLine()) != null)
			{
				try
				{
					StringTokenizer st = new StringTokenizer(inputLine, " ", false);
					String cmds[] = new String[st.countTokens()];
					for (int i = 0; i < cmds.length; i++)
					{
						cmds[i] = st.nextToken();

					}
					if ("addtopic".equals(cmds[0]))
					{
						for (int p = 1; p < cmds.length; p++)
						{
							ub.addTopic(cmds[p]);
						}
					}
					else if ("listtopic".equals(cmds[0]))
					{
						System.out.println(ub.getTopicsStr());
					}
					else if ("removetopic".equals(cmds[0]))
					{
						ub.removeTopic(cmds[1]);
					}
					else if ("protocal".equals(cmds[0]))
					{
						if (cmds.length > 1)
						{
							if ("defaultudp".equals(cmds[1]))
							{
								ub = UDPBase.getDefaultUDPBase();
							}
							else if ("groupudp".equals(cmds[1]))
							{
								ub = UDPBase.getGroupUDPBase();
							}
							else
							{
								ub = UDPBase.getUDPBase();
							}
						}
						System.out.println("current protocal =" +
										   ub.toString());
					}
					else if ("send".equals(cmds[0]))
					{
						ub.send(cmds[1], cmds[2].getBytes());
					}
					else if ("sendfile".equals(cmds[0]))
					{
						File f = new File(cmds[2]);
						FileInputStream fis = new FileInputStream(f);
						byte[] buf = new byte[ (int) f.length()];
						fis.read(buf);
						fis.close();
						ub.send(cmds[1], buf);
						System.out.println("     send len=" + buf.length);
					}
				}
				catch (Exception _e)
				{
					_e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

class TestUDPCallback
	implements UDPBaseCallback
{
	String name = "noname";
	int count = 0;
	int endtimes = 1;

	TestUDPCallback()
	{}

	TestUDPCallback(String name)
	{
		this.name = name;
	}

	public void OnMsg(UDPBase base, String srctopic, byte[] msgbyte)
	{
		//String tmps = new String(msgbyte);
		//System.out.println("testint callback---"+tmps);
		count++;
		//if (tmps.equals ("end"))
		{
			//System.out.println("received times="+count+" length="+msgbyte.length +"Content="+tmps);
			System.out.println(new String(msgbyte));
			System.out.println(name + " recv [" + srctopic + "] len=" +
							   msgbyte.length);
			/*
			 endtimes --;
			 if (endtimes==0)
			 System.exit (0);*/
		}
	}

}
