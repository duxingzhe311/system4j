package com.dw.system.xevent;

import java.io.*;
import java.util.*;
import java.net.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import com.dw.system.*;

/*
 * <p>Title: </p>
 * <p>Description:
 * <h3>对应异步事件处理</h3><br>
 * 1，每个事件都会有一个队列。所有的队列提供一个信号量，表示有事件准备被消费
 * 2，内部有一个线程池，它提供空闲线程信号量。
 * 3，有一个调度线程，根据两个信号量决定自己是否阻塞。
 * 4，当外界输入一个事件时，会notify调度线程。
 * 5，当一个线程池中的工作完成时，会notify调度线程
 * 6，调度线程有一个令牌，它指向一个队列，并且随着调度过程在队列中传递，每次调度开始的时候。
 *    调度线程从令牌指向的队列开始搜索有没事件</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: CssWeb</p>
 * @author Jason Zhu
 * @version 1.0
 */

/**
 * <p>Title: 事件派发中心</p>
 * <p>Description:
 * 1，每个事件都会有一个处理风格：SYN,ASYN_POOL,ASYN_MONO,分别表示处理该事件的监听器
 *    和出发事件的线程有如下关系：
 *    SYN         同步处理，监听器运行在同一个空间中
 *    ASYN_POOL   使用统一的线程池,（同一种事件不能保证顺序）
 *    ASYN_MONO   独占一个处理线程和队列，保证同一种事件可以被顺序处理
 * 2，对于全局事件监听器，系统使用一个队列和一个线程进行处理
 * 3，所有出发事件的模块和监听事件的模块必须在ini.xml填写相关的xevent注册信息。
 *    XEventDispatcher类在初始化的时候会遍历WEB-INF/classes目录和WEB-INF/lib下的所有
 *    jar文件对ini.xml进行扫描。使得所有的事件和监听器都被注册到内存中。
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: CssWeb</p>
 * @author Jason Zhu
 * @version 1.0
 */
public final class XEventDispatcher
{
	private static Log log = null;
	static
	{
		try
		{
			log = Log.getLog("XEventDispatcher.log");
		}
		catch (Throwable _t)
		{
			log = Log.getLog();
		}
	}

	/**
	 * 所有事件类对应EventStyle的映射
	 */
	static HashMap allEventMap = new HashMap();

	/**
	 * 事件对应的监听器映射。key为事件类，value是一个Vector对象
	 */
	private static Hashtable eventLisMap = new Hashtable();

	/**
	 * 全局监听器，它监听所有的事件，它和eventListMap中的事件监听器只能在其中一个
	 */
	private static Vector globalLis = new Vector();

	private static GlobalLisDispatcher globalLisDispathcer = new
		GlobalLisDispatcher();

	/**
	 * 保存所有的ListenerShell，以便于查询
	 */
	private static Vector allLisShell = new Vector();

	/**
	 * 远程通信接口,xevent通过reflector方式建立远程接口对象
	 */
	static RemoteInterface remoteInter = null;
	/**
	 * 使用remoteInter进行远程事件发送
	 */
	static RemoteSender remoteSender = null;

	/**
	 * 避免外界构造对象
	 */
	private XEventDispatcher()
	{}

	/**
	 * 缺省事件风格
	 */
	public final static EventStyle DEFAULT_EVENTSTYLE = new EventStyle();

	//找到所有的配置文件，使之被初始化
	static
	{
		try
		{
			initXEventElement();
			registerEventAndAddListener();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			log.log(e);
		}
	}

	transient static Element[] allEventEles = null;
	transient static Element[] allListenerEles = null;

	static void initXEventElement()
		throws IOException, ParserConfigurationException, SAXException
	{
		DocumentBuilderFactory docBuilderFactory
			= DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(false);
		docBuilderFactory.setValidating(false);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

		Vector v = XEventHelper.readIniFiles();
		Vector eventEles = new Vector();
		Vector lisEles = new Vector();
		if (v != null)
		{
			int s = v.size();
			//扫描所有的ini.xml文件，并注册所有XEvent对象
			for (int i = 0; i < s; i++)
			{
				FileDataItem fdi = (FileDataItem) v.elementAt(i);
				//
				ByteArrayInputStream bais = new ByteArrayInputStream(fdi.
					getContent());
				InputSource is = new InputSource(bais);
				is.setEncoding("gb2312");
				Document doc = docBuilder.parse(is);

				Element rootele = doc.getDocumentElement();

				System.out.println(">>parsing:" + fdi.getName());
				System.out.println("     [" + rootele.getAttribute("name") +
								   "][" + rootele.getAttribute("chname") + "]");

				Element[] xeeles = XEventHelper.getCurChildElement(rootele,
					"xevent");
				if (xeeles == null || xeeles.length == 0)
				{
					continue;
				}
				Element[] eventsEle = XEventHelper.getCurChildElement(xeeles[0],
					"events");
				if (eventsEle != null && eventsEle.length > 0)
				{
					Element[] evEles = XEventHelper.getCurChildElement(
						eventsEle[0], "event");
					for (int j = 0; j < evEles.length; j++)
					{
						System.out.println("       >find event:" +
										   evEles[j].getAttribute("class"));
						eventEles.addElement(evEles[j]);
					}
				}

				Element[] tmpeliss = XEventHelper.getCurChildElement(xeeles[0],
					"listeners");
				if (tmpeliss != null && tmpeliss.length > 0)
				{
					Element[] tmpel = XEventHelper.getCurChildElement(
						tmpeliss[0], "listener");
					for (int j = 0; j < tmpel.length; j++)
					{
						System.out.println("       >find listener:" +
										   tmpel[j].getAttribute("class"));
						lisEles.addElement(tmpel[j]);
					}
				}
			}
		}

		allEventEles = new Element[eventEles.size()];
		allListenerEles = new Element[lisEles.size()];
		eventEles.toArray(allEventEles);
		lisEles.toArray(allListenerEles);
	}

	private static void registerEventAndAddListener()
		throws Throwable
	{
		if (allEventEles != null)
		{
			for (int i = 0; i < allEventEles.length; i++)
			{
				String cn = allEventEles[i].getAttribute("class");
				if (cn == null || cn.equals(""))
				{
					throw new RuntimeException(
						"event element in XEvent ini file must has class attr!");
				}

				Class ec = Class.forName(cn);

				String sstyle = allEventEles[i].getAttribute("style");
				if (sstyle == null || sstyle.equals(""))
				{
					sstyle = "" + EventStyle.ASYN_POOL;
				}
				//short st = Short.parseShort(sstyle);
				short st = EventStyle.strToStyle(sstyle);

				String sremote = allEventEles[i].getAttribute("remote");
				boolean remote = !"false".equals(sremote);

				registerEvent(ec, st, remote);
			}
		}

		if (allListenerEles != null)
		{
			for (int i = 0; i < allListenerEles.length; i++)
			{
				String cn = allListenerEles[i].getAttribute("class");
				if (cn == null || cn.equals(""))
				{
					throw new RuntimeException(
						"event element in XEvent ini file must has class attr!");
				}

				Class lc = Class.forName(cn);
				XEventListener lis = (XEventListener) lc.newInstance();
				boolean ingoreoe = !"false".equals(
					allListenerEles[i].getAttribute("ingoreOtherException"));
				boolean maskselfe = !"false".equals(
					allListenerEles[i].getAttribute("maskSelfException"));
				boolean acceptremote = !"false".equals(
					allListenerEles[i].getAttribute("acceptRemote"));

				String sordernum = allListenerEles[i].getAttribute("ordernum");
				int ordernum = Integer.MAX_VALUE;
				if (sordernum != null && !sordernum.equals(""))
				{
					ordernum = Integer.parseInt(sordernum);

				}
				Class[] eventc = null;

				Element[] accevs = XEventHelper.getCurChildElement(
					allListenerEles[i], "event");
				if (accevs != null && accevs.length > 0)
				{
					eventc = new Class[accevs.length];
					for (int j = 0; j < eventc.length; j++)
					{
						String ecn = accevs[j].getAttribute("class");
						eventc[j] = Class.forName(ecn);
					}

				}
				addListener(eventc, lis, ingoreoe, maskselfe,
							acceptremote, ordernum);
			}
		}
	}

	/**
	 * 接收远程事件的对象
	 */
	static RemoteCallback remoteCallback = new RemoteCallback()
	{
		/**
		 * 接收来自远程的事件。<br>
		 *
		 * @param evdata 远程事件数据
		 */
		public void onRecvedEventData(byte[] evdata)
		{
			try
			{
				ByteArrayInputStream istream = new ByteArrayInputStream(evdata);
				ObjectInputStream p = new ObjectInputStream(istream);
				XEvent xe = (XEvent) p.readObject();

				EventStyle es = (EventStyle) allEventMap.get(xe.getClass());
				if (es == null)
				{
					throw new IllegalArgumentException(
						"Event [" + xe.getClass().getName() +
						"] is not registered!");
				}

				fireRemoteEventToLocal(es.getStyle(), xe);
			}
			catch (Exception e)
			{
				log.log(e);
			}
		}
	};

	/**
	 * 派发远程来的事件对象
	 * <b>
	 * <font color="red">
	 * 注：
	 *   1，如果远程事件注册好的调用风格是同步(SYN)，由于事件经过远程处理已经没有同步的意义
	 *   所以在本地处理上采用线程池的方式运行，这样可以避免通信端的阻塞。<br>
	 *   2，如果远程事件注册好的调用风格是线程池(ASYN_POOL),在本地也用线程池方式<br>
	 *   3,如果远程事件注册好的调用风格是独占线程(ASYN_MONO),在本地也用独占方式<br>
	 * </font>
	 * </b>
	 * @param style 事件处理风格
	 * @param xe 事件对象
	 */
	private static void fireRemoteEventToLocal(short style, XEvent xe)
	{
		//设置事件来自远程
		xe._fromRemote = true;

		//派发给全局事件监听器（异步单线程运行）
		globalLisDispathcer.dispatch(xe);
		//判断事件处理风格
		switch (style)
		{
			case EventStyle.SYN:
			case EventStyle.ASYN_POOL:
				dispatchAsynPool(xe);
				break;
			case EventStyle.ASYN_MONO:
				dispatchAsynMono(xe);
				break;
			default:
				throw new RuntimeException("Not support event process style=" +
										   style);
		}

	}

	static class RemoteSender
		extends Thread
	{
		Queue queue = new Queue();

		public void sendEvent(XEvent xe)
		{
			queue.enqueue(xe);
		}

		public void run()
		{
			while (true)
			{
				try
				{
					Object e = queue.dequeue();
					//序列化事件对象
					ByteArrayOutputStream ostream = new ByteArrayOutputStream();
					ObjectOutputStream p = new ObjectOutputStream(ostream);
					p.writeObject(e);
					p.flush();

					//发送事件
					remoteInter.sendXEvent(ostream.toByteArray());
				}
				catch (Exception ee)
				{
					log.log(ee);
				}
			}
		}
	}

	static
	{
		StringBuffer reson = new StringBuffer();
		remoteInter = XEventHelper.getRemoteInterface(remoteCallback, reson);
		if (remoteInter == null)
		{
			System.out.println(
				"\n\n$$$$$$$       XEvent cannot support remote!     $$$$$$");
			System.out.println(reson.toString());
			System.out.println(
				"$$$$$$$$$$$       $$$$$$$$$$$$$$$$$$$$$$$$$         $$$$$$\n\n");
		}
		else
		{
			remoteSender = new RemoteSender();
			remoteSender.start();
			System.out.println(
				"\n\n******       XEvent    support remote!    *******");
			System.out.println(
				"      remote Sender started!");
			System.out.println(
				"************************************************\n\n");
		}
	}

//	static AsynEventDispatcher asynDispatcher = new AsynEventDispatcher();
	/**
	 * 注册事件类，方便提供查询功能
	 * @param eventc 事件类
	 * @param style 事件被处理的风格，如CS_SYN,CS_GLOBAL_QUEUE,CS_EVENT_QUEUE
	 * @param bremote 是否是远程事件
	 */
	synchronized public static void registerEvent(
		Class eventc,
		short style,
		boolean bremote)
	{
		//System.out.println("allEventMap==" + allEventMap + "   e=" + eventc);
		allEventMap.put(eventc, new EventStyle(style, bremote));
	}

	/**
	 * 得到所有注册事件
	 * @return 所有注册事件key
	 */
	synchronized public static Class[] getAllRegisteredEvent()
	{
		Class[] ecs = new Class[allEventMap.size()];
		allEventMap.keySet().toArray(ecs);
		return ecs;
	}

	/**
	 * 得到一个事件类型的事件风格
	 * @param eventc 事件类
	 * @return 事件风格
	 */
	public static EventStyle getEventStyle(Class eventc)
	{
		EventStyle es = (EventStyle) allEventMap.get(eventc);
		if (es != null)
		{
			return es;
		}
		return DEFAULT_EVENTSTYLE;
	}

	/**
	 * 得到所有的监听器
	 * @return 所有的监听器对象
	 */
	public static ListenerShell[] getAllListeners()
	{
		ListenerShell[] retls = new ListenerShell[allLisShell.size()];
		allLisShell.toArray(retls);
		return retls;
	}

	private static void addListener(Class[] eventc, ListenerShell ls)
	{
		allLisShell.addElement(ls);
		XEventHelper.sort(allLisShell);

		if (eventc == null || eventc.length <= 0)
		{ //全局事件监听器，监听所有的事件
			globalLis.addElement(ls);
			XEventHelper.sort(globalLis);
		}
		else
		{
			for (int i = 0; i < eventc.length; i++)
			{
				Vector v = (Vector) eventLisMap.get(eventc[i]);
				if (v == null)
				{
					v = new Vector();
					eventLisMap.put(eventc[i], v);
				}
				v.addElement(ls);
				XEventHelper.sort(v);
			}
		}
	}

	/**
	 * 增加一个事件监听器
	 * @param eventc 被监听得事件类，如果为null或，长度＝0，
	 * 则表示监听器监听所有事件
	 * @param lis 监听器对象
	 * @param ingoreOtherException 是否忽律其他监听相同事件的监听器出错。
	 *   也就是必须被运行
	 * @param maskSelfException 屏蔽自己的错误。
	 *   使之不影响其他监听相同事件的监听器运行
	 * @param acceptRemote 判断是否接收处理远程事件
	 * @param ordernum 监听器的顺序号
	 */
	public static void addListener(
		Class[] eventc,
		XEventListener lis,
		boolean ingoreOtherException,
		boolean maskSelfException,
		boolean acceptRemote, int ordernum)
	{
		ListenerShell ls = new ListenerShell(
			lis,
			ingoreOtherException,
			maskSelfException, acceptRemote, ordernum);

		addListener(eventc, ls);
	}

	/**
	 * 增加一个事件监听器
	 * @param eventc 被监听得事件类，如果为null或，长度＝0，
	 * 则表示监听器监听所有事件
	 * @param lis 监听器对象
	 * @param ingoreOtherException 是否忽律其他监听相同事件的监听器出错。
	 *   也就是必须被运行
	 * @param maskSelfException 屏蔽自己的错误。
	 *   使之不影响其他监听相同事件的监听器运行
	 */
	public static void addListener(
		Class[] eventc,
		XEventListener lis,
		boolean ingoreOtherException,
		boolean maskSelfException)
	{
		ListenerShell ls = new ListenerShell(
			lis,
			ingoreOtherException,
			maskSelfException);

		addListener(eventc, ls);
	}

	/**
	 * 根据输入的类名称和事件监听器相关的信息增加监听器
	 * @param eventcn 监听器监听得事件名称，如果＝null或长度＝0表示监听所有事件
	 * @param lis 监听器对象
	 * @param ingoreOtherException 是否忽律其他监听相同事件的监听器出错。
	 *   也就是必须被运行
	 * @param maskSelfException 屏蔽自己的错误。
	 *   使之不影响其他监听相同事件的监听器运行
	 * @throws ClassNotFoundException 如果指定的事件类没有找到，则注册失败，
	 *   如果事件名称数组长度大于1，只要有一个不成功，其他的也不会成功
	 */
	public static void addListener(
		String[] eventcn,
		XEventListener lis,
		boolean ingoreOtherException,
		boolean maskSelfException)
		throws ClassNotFoundException
	{
		Class[] ecs = null;
		if (eventcn != null || eventcn.length > 0)
		{
			ecs = new Class[eventcn.length];
			for (int i = 0; i < ecs.length; i++)
			{
				ecs[i] = Class.forName(eventcn[i]);
			}
		}

		addListener(
			ecs,
			lis,
			ingoreOtherException,
			maskSelfException);
	}

	/**
	 * 注册监听特定事件的监听器，监听器忽略其他监听器的错误，并且需要屏蔽自己的错误
	 * @param eventc 事件类,如果＝null表示监听所有的事件
	 * @param lis 监听器对象
	 */
	public static void addListener(Class eventc, XEventListener lis)
	{
		if (eventc == null)
		{
			addListener(new Class[0], lis, true, true);
		}
		else
		{
			Class[] cs = new Class[1];
			cs[0] = eventc;
			addListener(cs, lis, true, true);
		}
	}

	/**
	 * 注册监听所有事件的监听器，监听器忽略其他监听器的错误，并且需要屏蔽自己的错误
	 * @param lis 事件监听器对象
	 */
	public static void addListener(XEventListener lis)
	{
		addListener(new Class[0], lis, true, true);
	}

	/**
	 * 触发一个事件
	 * @param xe 事件对象
	 */
	public static void fireEvent(XEvent xe)
	{
		EventStyle es = (EventStyle) allEventMap.get(xe.getClass());
		if (es == null)
		{
			throw new IllegalArgumentException(
				"Event [" + xe.getClass().getName() +
				"] is not registered!");
		}

		if (es.bRemote && remoteSender != null)
		{
			remoteSender.sendEvent(xe);
		}

		fireEventLocal(es.getStyle(), xe);
	}

	/**
	 * 内部包含队列的线程池
	 */
	static WorkThreadPool threadPool = new WorkThreadPool(5);

	private static void dispatchAsynPool(XEvent xe)
	{
		Worker wk = new Worker(xe);
		threadPool.execute(wk);
	}

	static Hashtable eventAsynMonoMap = new Hashtable();

	private synchronized static void dispatchAsynMono(XEvent xe)
	{
		AsynMonoDispatcher amd = (AsynMonoDispatcher) eventAsynMonoMap.get(xe.
			getClass());
		if (amd == null)
		{
			amd = new AsynMonoDispatcher();
			eventAsynMonoMap.put(xe.getClass(), amd);
		}
		amd.dispatch(xe); ;
	}

	/**
	 * 根据事件对象得到即将处理该事件的监听器。<br>
	 * 事件被派发的监听器顺序和注册顺序相关(包含全局事件监听器)。
	 * @param xe 事件对象
	 * @return 相关的监听器,把全局和局部的事件监听器组合并排序
	 */
	public static Vector getTargetListeners(Class eventc)
	{
		return (Vector) eventLisMap.get(eventc);
//		Vector v = (Vector) globalLis.clone();
//		Vector v0 = (Vector) eventLisMap.get(eventc);
//		if (v0 != null)
//		{
//			v.addAll(v0);
//			XEventHelper.sort(v);
//		}
//
//		return v;
	}

	private static void dispatchGlobalEventSynLocal(XEvent xe)
	{
		dispatchEventSynLocal(xe, globalLis);
	}

	/**
	 * 本地处理事件的具体过程
	 * @param xe 事件对象
	 */
	private static void dispatchEventSynLocal(XEvent xe)
	{
		Vector v = (Vector) eventLisMap.get(xe.getClass());
		dispatchEventSynLocal(xe, v);
	}

	private static void dispatchEventSynLocal(XEvent xe, Vector liss)
	{
		//真正的事件派发过程
		//Vector v = getTargetListeners(xe.getClass());

		Throwable exp = null;
		if (liss != null)
		{
			int s = liss.size();
			for (int i = 0; i < s; i++)
			{
				ListenerShell ls = (ListenerShell) liss.elementAt(i);
				if (xe._fromRemote && !ls.isAcceptRemoteEvent())
				{ //如果监听器不处理远程事件
					continue;
				}

				try
				{
					if (exp != null && !ls.isIngoreException())
					{
						continue;
					}

					ls.dispatch(xe);
				}
				catch (Throwable e)
				{
					exp = e;
				}
			}
		}
	}

	/**
	 * 在同一个线程空间中进行事件对应listener的派发
	 * @param style 处理事件风格
	 * @param xe 事件对象
	 */
	private static void fireEventLocal(short style, XEvent xe)
	{
		//派发给全局事件监听器（异步单线程运行）
		globalLisDispathcer.dispatch(xe);
		//判断事件处理风格
		switch (style)
		{
			case EventStyle.SYN:

				//同步处理过程
				dispatchEventSynLocal(xe);
				break;
			case EventStyle.ASYN_POOL:
				dispatchAsynPool(xe);
				break;
			case EventStyle.ASYN_MONO:
				dispatchAsynMono(xe);
				break;
			default:
				throw new RuntimeException("Not support event process style=" +
										   style);
		}

	}

	/**
	 * 把事件通过远程接口进行发送，如果远程接口为null，则不做任何事
	 * @param e 事件对象
	 */
	private static void sendToRemote(XEvent e)
	{

	}

	/**
	 * <p>Title: 独占一个线程的处理队列</p>
	 * <p>Description: </p>
	 * <p>Copyright: Copyright (c) 2003</p>
	 * <p>Company: </p>
	 * @author Jason Zhu
	 * @version 1.0
	 */
	static class AsynMonoDispatcher
		implements Runnable
	{
		Thread thread = null;
		Queue queue = new Queue();

		AsynMonoDispatcher()
		{
			thread = new Thread(this);
			thread.start();
		}

		public void dispatch(XEvent xe)
		{
			queue.enqueue(xe);
		}

		public void run()
		{
			while (true)
			{
				XEvent xe = (XEvent) queue.dequeue();
				dispatchEventSynLocal(xe);
			}
		}
	}

	/**
	 * 全局事件监听器分发处理
	 * <p>Title: </p>
	 * <p>Description: </p>
	 * <p>Copyright: Copyright (c) 2003</p>
	 * <p>Company: </p>
	 * @author Jason Zhu
	 * @version 1.0
	 */
	static class GlobalLisDispatcher
		implements Runnable
	{
		Thread thread = null;
		Queue queue = new Queue();

		GlobalLisDispatcher()
		{
			thread = new Thread(this);
			thread.start();
		}

		public void dispatch(XEvent xe)
		{
			queue.enqueue(xe);
		}

		public void run()
		{
			while (true)
			{
				XEvent xe = (XEvent) queue.dequeue();
				dispatchGlobalEventSynLocal(xe);
			}
		}
	}

	static class AsynEventDispatcher
	{
		/**
		 * 任务调度器同步锁基准
		 */
		private Object schedSynObj = new Object();
		/**
		 * 队列操作同步锁基准
		 */
		private Object queueSynObj = new Object();

		/**
		 * 队列组中的事件计数器
		 */
		private int asynEventCount = 0;
		/**
		 * 循环指针，调度器通过循环扫描所有队列保证所有的事件队列中的事件都可以被调度到
		 */
		private int asynCurPt = -1;

		/**
		 * 事件队列组，被调度线程扫描
		 */
		private Vector eventQueues = new Vector();
		/**
		 * 事件队列映射，用来判断一个事件的类型是否有相关队列产生
		 */
		private HashMap eventQueueMap = new HashMap();

		/**
		 * 调度线程
		 */
		private Thread schedThread = null;

		/**
		 * 包含运行对象（Runnable）对应的key对象的线程池
		 */
		private KeyThreadPool threadPool = null;

		AsynEventDispatcher()
		{
			//创建线程池
			threadPool = new KeyThreadPool(5);
			//启动调度线程
			schedThread = new Thread(asynRunner);
			schedThread.start();
		}

		/**
		 * 建一个事件抛入事件队列组中，可能会引发调度器的调度过程
		 * @param e 事件对象
		 */
		private void dispatchEvent(XEvent e)
		{
			synchronized (queueSynObj)
			{
				//得到事件对应的处理队列，如果没有，则建立队列
				LinkedList queue = (LinkedList) eventQueueMap.get(e.getClass());
				if (queue == null)
				{
					queue = (LinkedList) Collections.synchronizedList(new
						LinkedList());
					eventQueues.addElement(queue);
					eventQueueMap.put(e.getClass(), queue);
				}

				//把事件放入队列中
				queue.addLast(e);
				//待处理事件个数加1
				asynEventCount++;

				//notify to schedule
				schedSynObj.notify();
			}
		}

		/**
		 * 调度分派事件过程
		 * @throws Throwable
		 */
		private void schedule()
			throws Throwable
		{
			//当前待处理的事件个数如果为空等待。
			if (asynEventCount <= 0)
			{
				schedSynObj.wait();
				//避免循环调用堆栈很大
				return;
			}

			//当前如果没有空闲的线程等待
			if (!threadPool.hasIdleWorker())
			{
				schedSynObj.wait();
				//避免循环调用堆栈很大
				return;
			}

			XEvent xe = null;
			//循环扫描队列组，直到找到一个事件对象
			int s = eventQueues.size();
			if (s <= 0)
			{
				return;
			}
			//令牌指针指向下一个队列
			asynCurPt++;
			//asynCurPt = asynCurPt % s;
			for (int i = 0; i < s; i++)
			{
				asynCurPt += i;
				asynCurPt = asynCurPt % s;
				LinkedList q = (LinkedList) eventQueues.elementAt(asynCurPt);
				if (q.size() <= 0)
				{
					continue;
				}
				//获取一个事件
				xe = (XEvent) q.getFirst();
				//判断线程池中是否有同样类型的事件在运行,如果有则不处理
				if (threadPool.containsRunnerKey(xe.getClass()))
				{
					continue;
				}

				//把事件重队列中摘除
				q.removeFirst();
				synchronized (queueSynObj)
				{
					//待处理计数减1
					asynEventCount--;
				}
				break;
			}

			//创建Runnable对象
			Worker w = new Worker(xe);
			//抛入线程池中运行
			threadPool.addRunner(xe.getClass(), w);
		}

		/**
		 * 调度处理本身是在一个线程中运行
		 */
		Runnable asynRunner = new Runnable()
		{
			public void run()
			{
				try
				{
					while (true)
					{
						schedule();
					}
				}
				catch (Throwable _t)
				{}
			}
		};
	} //end of asyndispatcher

	/**
	 * 在线程池中运行的Runnable内容
	 * <p>Title: </p>
	 * <p>Description: </p>
	 * <p>Copyright: Copyright (c) 2003</p>
	 * <p>Company: </p>
	 * @author Jason Zhu
	 * @version 1.0
	 */
	static class Worker
		implements Runnable
	{
		XEvent xe = null;
		public Worker(XEvent xe)
		{
			if (xe == null)
			{
				throw new IllegalArgumentException("Event Cannot be null!");
			}
			this.xe = xe;
		}

		public void run()
		{
			//在一个线程内部调用同步派发事件的方法:)
			dispatchEventSynLocal(xe);
		}

	}

	public static void main(String[] args)
	{
		try
		{
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

					if ("listevent".equals(cmds[0]))
					{
						Class[] cs = getAllRegisteredEvent();
						for (int i = 0; i < cs.length; i++)
						{
							System.out.println(cs[i].getName());
							EventStyle es = getEventStyle(cs[i]);
							System.out.println("   " + es);
						}
					}
					else if ("listlis".equals(cmds[0]))
					{
						ListenerShell[] lss = getAllListeners();
						for (int i = 0; i < lss.length; i++)
						{
							System.out.println(lss[i]);
						}
					}
					else if ("checkevent".equals(cmds[0]))
					{
						if (cmds.length < 2)
						{
							System.out.println(
								"   Error:no event class name input!");
							continue;
						}

						try
						{
							Class c = Class.forName(cmds[1]);
							Vector v = getTargetListeners(c);
							int s = v.size();
							for (int i = 0; i < s; i++)
							{
								System.out.println(v.elementAt(i));
							}
						}
						catch (Exception e0)
						{
							e0.printStackTrace();
						}
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
} // end of XEventDispather
