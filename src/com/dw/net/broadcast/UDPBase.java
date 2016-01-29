package com.dw.net.broadcast;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.*;

/**
 * ͳһʵ��UDP�Ĵ���ӿ�
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
	//������е�UDPBase������Ϣ����
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
	 * Ϊϵͳ�ṩһ���ܹ�ͬʱʹ��Tcp��udp��ʽ���ֶΡ�
	 * ��tcp��udp֮���ǲ��ܽ���ͨ�ŵġ�
	 * @param bgroup �Ƿ�����鲥
	 * @return �������
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
	 * ���ͨ�Ŷ�����������ļ���û���κ����ݣ���ô���صĶ���û��ͨ��������
	 * �����GROUP���ͣ��������鲥����
	 * �����default���ͣ������ھ��������й㲥����
	 * �����virtual���ͣ�����������ͬһ̨������ģ���������ַ��������
	 * @return Ψһ��ͨ���������
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
	 * �õ�ʹ�������ַ�����࣬�÷���������������£�ģ���������������ʱʹ�á�
	 * �Է������ʹ�á�
	 * @param virtualip ���Ӧ�ó����Լ�ָ���ĵ�ַ
	 * @return ͨ������
	 * @throws UDPBaseException �������
	 */
	public static UDPBase getVirtualIPUDPBase(String virtualip)
		throws UDPBaseException
	{
		UDPBase tmpu = new DefaultUDPBase(virtualip);
		return tmpu;
	}

	/**
	 * ������Ϣ����Ķ���
	 */
	protected MsgTopic msgTopic = new MsgTopic();
	/**
	 * ������еĻص�����
	 */
	protected Vector callBack = new Vector();
	/**
	 * �õ����ص�ip��ַ
	 * @return ����ip��ַ
	 */
	public abstract String getLocalIP();

	/**
	 * �õ����ص�ַ������TCP��ʽ֧�ֵ���ʹ�ö���˿ڡ����Ը÷���
	 * �����˸�����ϸ�ĵ�ַ ip:port
	 * @return ����ip:port�ĵ�ַ��
	 */
	public abstract String getLocalAddr();

	/**
	 * ��õ�ǰ�����������IP��ַ
	 * @return ���е�ַ
	 */
	public abstract Vector getActiveBaseIP();

	/**
	 * �ж��Ƿ������л��������ip��С�ķ�����
	 * @return true��ʾ������������false��ʾ������������
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
	 * ������л������������������IP��ַ
	 * @return String IP��ַ
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
	 * ���Ӽ�����Ϣ�����⡣
	 * @param topic �����ַ���
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
	 * ���Ӷ��������Ϣ������
	 * @param topics �����ַ�������
	 */
	public void addTopic(String[] topics)
	{
		for (int i = 0; i < topics.length; i++)
		{
			msgTopic.add(topics[i]);
		}
	}

	/**
	 * �õ����еı����������������Ϣ���÷���
	 * @return ����������Ϣ
	 */
	public String getTopicsStr()
	{
		return msgTopic.getTopicsStr();
	}

	/**
	 * ɾ��һ������
	 * @param topic ����
	 */
	public void removeTopic(String topic)
	{
		msgTopic.remove(topic);
	}

	/**
	 * �ù㲥��ʽ����һ����Ϣ
	 * @param topic ��������Ϣ������
	 * @param infobuf ��������Ϣ��
	 * @throws UDPBaseException ������Ϣ��������
	 */
	public abstract void send(String topic, byte[] infobuf)
		throws UDPBaseException;

	//boolean ackReceived ;
	/*
	 * �ɿ��ط�����Ϣ��Ҫ��ÿ��ip����Ҫ��һ����Ӧ
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
	   //if (i>0)//ѭ��������Ϣÿ�η��ͼ�Ӽ��������߷��ͳɹ���
	 Thread.sleep (LOOP_SEND_INTERVAL) ;
	   //����˳��ͷ
	   HeaderItem orderHeader = new HeaderItem (""+pknum+"_"+i);
	   tmpb = HeaderItem.appendHeaderItem (
	 infobuf,i*MAX_PACKET_LENGTH,MAX_PACKET_LENGTH,orderHeader);
	   //����Ψһid
	   tmpb = HeaderItem.appendHeaderItem (tmpb,idHeader);
	   //��������ͷ
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
	   //����˳��ͷ
	   HeaderItem orderHeader = new HeaderItem (""+pknum+"_"+bs);
	   tmpb = HeaderItem.appendHeaderItem (
	 infobuf,bs*MAX_PACKET_LENGTH,sy,orderHeader);
	   //����Ψһid
	   tmpb = HeaderItem.appendHeaderItem (tmpb,idHeader);
	   //��������ͷ
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
	 * ���ô�����յ���Ϣ�Ķ���,Ҳ���Ǽ�����Ϣ�Ĵ������
	 * @param udpbck �ص�����
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
	 * ȡ��һ��������Ϣ�������
	 * @param udpbck �ص�����
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
