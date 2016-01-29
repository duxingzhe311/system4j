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
 * <h3>��Ӧ�첽�¼�����</h3><br>
 * 1��ÿ���¼�������һ�����С����еĶ����ṩһ���ź�������ʾ���¼�׼��������
 * 2���ڲ���һ���̳߳أ����ṩ�����߳��ź�����
 * 3����һ�������̣߳����������ź��������Լ��Ƿ�������
 * 4�����������һ���¼�ʱ����notify�����̡߳�
 * 5����һ���̳߳��еĹ������ʱ����notify�����߳�
 * 6�������߳���һ�����ƣ���ָ��һ�����У��������ŵ��ȹ����ڶ����д��ݣ�ÿ�ε��ȿ�ʼ��ʱ��
 *    �����̴߳�����ָ��Ķ��п�ʼ������û�¼�</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: CssWeb</p>
 * @author Jason Zhu
 * @version 1.0
 */

/**
 * <p>Title: �¼��ɷ�����</p>
 * <p>Description:
 * 1��ÿ���¼�������һ��������SYN,ASYN_POOL,ASYN_MONO,�ֱ��ʾ������¼��ļ�����
 *    �ͳ����¼����߳������¹�ϵ��
 *    SYN         ͬ������������������ͬһ���ռ���
 *    ASYN_POOL   ʹ��ͳһ���̳߳�,��ͬһ���¼����ܱ�֤˳��
 *    ASYN_MONO   ��ռһ�������̺߳Ͷ��У���֤ͬһ���¼����Ա�˳����
 * 2������ȫ���¼���������ϵͳʹ��һ�����к�һ���߳̽��д���
 * 3�����г����¼���ģ��ͼ����¼���ģ�������ini.xml��д��ص�xeventע����Ϣ��
 *    XEventDispatcher���ڳ�ʼ����ʱ������WEB-INF/classesĿ¼��WEB-INF/lib�µ�����
 *    jar�ļ���ini.xml����ɨ�衣ʹ�����е��¼��ͼ���������ע�ᵽ�ڴ��С�
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
	 * �����¼����ӦEventStyle��ӳ��
	 */
	static HashMap allEventMap = new HashMap();

	/**
	 * �¼���Ӧ�ļ�����ӳ�䡣keyΪ�¼��࣬value��һ��Vector����
	 */
	private static Hashtable eventLisMap = new Hashtable();

	/**
	 * ȫ�ּ����������������е��¼�������eventListMap�е��¼�������ֻ��������һ��
	 */
	private static Vector globalLis = new Vector();

	private static GlobalLisDispatcher globalLisDispathcer = new
		GlobalLisDispatcher();

	/**
	 * �������е�ListenerShell���Ա��ڲ�ѯ
	 */
	private static Vector allLisShell = new Vector();

	/**
	 * Զ��ͨ�Žӿ�,xeventͨ��reflector��ʽ����Զ�̽ӿڶ���
	 */
	static RemoteInterface remoteInter = null;
	/**
	 * ʹ��remoteInter����Զ���¼�����
	 */
	static RemoteSender remoteSender = null;

	/**
	 * ������繹�����
	 */
	private XEventDispatcher()
	{}

	/**
	 * ȱʡ�¼����
	 */
	public final static EventStyle DEFAULT_EVENTSTYLE = new EventStyle();

	//�ҵ����е������ļ���ʹ֮����ʼ��
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
			//ɨ�����е�ini.xml�ļ�����ע������XEvent����
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
	 * ����Զ���¼��Ķ���
	 */
	static RemoteCallback remoteCallback = new RemoteCallback()
	{
		/**
		 * ��������Զ�̵��¼���<br>
		 *
		 * @param evdata Զ���¼�����
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
	 * �ɷ�Զ�������¼�����
	 * <b>
	 * <font color="red">
	 * ע��
	 *   1�����Զ���¼�ע��õĵ��÷����ͬ��(SYN)�������¼�����Զ�̴����Ѿ�û��ͬ��������
	 *   �����ڱ��ش����ϲ����̳߳صķ�ʽ���У��������Ա���ͨ�Ŷ˵�������<br>
	 *   2�����Զ���¼�ע��õĵ��÷�����̳߳�(ASYN_POOL),�ڱ���Ҳ���̳߳ط�ʽ<br>
	 *   3,���Զ���¼�ע��õĵ��÷���Ƕ�ռ�߳�(ASYN_MONO),�ڱ���Ҳ�ö�ռ��ʽ<br>
	 * </font>
	 * </b>
	 * @param style �¼�������
	 * @param xe �¼�����
	 */
	private static void fireRemoteEventToLocal(short style, XEvent xe)
	{
		//�����¼�����Զ��
		xe._fromRemote = true;

		//�ɷ���ȫ���¼����������첽���߳����У�
		globalLisDispathcer.dispatch(xe);
		//�ж��¼�������
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
					//���л��¼�����
					ByteArrayOutputStream ostream = new ByteArrayOutputStream();
					ObjectOutputStream p = new ObjectOutputStream(ostream);
					p.writeObject(e);
					p.flush();

					//�����¼�
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
	 * ע���¼��࣬�����ṩ��ѯ����
	 * @param eventc �¼���
	 * @param style �¼�������ķ����CS_SYN,CS_GLOBAL_QUEUE,CS_EVENT_QUEUE
	 * @param bremote �Ƿ���Զ���¼�
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
	 * �õ�����ע���¼�
	 * @return ����ע���¼�key
	 */
	synchronized public static Class[] getAllRegisteredEvent()
	{
		Class[] ecs = new Class[allEventMap.size()];
		allEventMap.keySet().toArray(ecs);
		return ecs;
	}

	/**
	 * �õ�һ���¼����͵��¼����
	 * @param eventc �¼���
	 * @return �¼����
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
	 * �õ����еļ�����
	 * @return ���еļ���������
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
		{ //ȫ���¼����������������е��¼�
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
	 * ����һ���¼�������
	 * @param eventc ���������¼��࣬���Ϊnull�򣬳��ȣ�0��
	 * ���ʾ���������������¼�
	 * @param lis ����������
	 * @param ingoreOtherException �Ƿ��������������ͬ�¼��ļ���������
	 *   Ҳ���Ǳ��뱻����
	 * @param maskSelfException �����Լ��Ĵ���
	 *   ʹ֮��Ӱ������������ͬ�¼��ļ���������
	 * @param acceptRemote �ж��Ƿ���մ���Զ���¼�
	 * @param ordernum ��������˳���
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
	 * ����һ���¼�������
	 * @param eventc ���������¼��࣬���Ϊnull�򣬳��ȣ�0��
	 * ���ʾ���������������¼�
	 * @param lis ����������
	 * @param ingoreOtherException �Ƿ��������������ͬ�¼��ļ���������
	 *   Ҳ���Ǳ��뱻����
	 * @param maskSelfException �����Լ��Ĵ���
	 *   ʹ֮��Ӱ������������ͬ�¼��ļ���������
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
	 * ��������������ƺ��¼���������ص���Ϣ���Ӽ�����
	 * @param eventcn �������������¼����ƣ������null�򳤶ȣ�0��ʾ���������¼�
	 * @param lis ����������
	 * @param ingoreOtherException �Ƿ��������������ͬ�¼��ļ���������
	 *   Ҳ���Ǳ��뱻����
	 * @param maskSelfException �����Լ��Ĵ���
	 *   ʹ֮��Ӱ������������ͬ�¼��ļ���������
	 * @throws ClassNotFoundException ���ָ�����¼���û���ҵ�����ע��ʧ�ܣ�
	 *   ����¼��������鳤�ȴ���1��ֻҪ��һ�����ɹ���������Ҳ����ɹ�
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
	 * ע������ض��¼��ļ����������������������������Ĵ��󣬲�����Ҫ�����Լ��Ĵ���
	 * @param eventc �¼���,�����null��ʾ�������е��¼�
	 * @param lis ����������
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
	 * ע����������¼��ļ����������������������������Ĵ��󣬲�����Ҫ�����Լ��Ĵ���
	 * @param lis �¼�����������
	 */
	public static void addListener(XEventListener lis)
	{
		addListener(new Class[0], lis, true, true);
	}

	/**
	 * ����һ���¼�
	 * @param xe �¼�����
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
	 * �ڲ��������е��̳߳�
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
	 * �����¼�����õ�����������¼��ļ�������<br>
	 * �¼����ɷ��ļ�����˳���ע��˳�����(����ȫ���¼�������)��
	 * @param xe �¼�����
	 * @return ��صļ�����,��ȫ�ֺ;ֲ����¼���������ϲ�����
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
	 * ���ش����¼��ľ������
	 * @param xe �¼�����
	 */
	private static void dispatchEventSynLocal(XEvent xe)
	{
		Vector v = (Vector) eventLisMap.get(xe.getClass());
		dispatchEventSynLocal(xe, v);
	}

	private static void dispatchEventSynLocal(XEvent xe, Vector liss)
	{
		//�������¼��ɷ�����
		//Vector v = getTargetListeners(xe.getClass());

		Throwable exp = null;
		if (liss != null)
		{
			int s = liss.size();
			for (int i = 0; i < s; i++)
			{
				ListenerShell ls = (ListenerShell) liss.elementAt(i);
				if (xe._fromRemote && !ls.isAcceptRemoteEvent())
				{ //���������������Զ���¼�
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
	 * ��ͬһ���߳̿ռ��н����¼���Ӧlistener���ɷ�
	 * @param style �����¼����
	 * @param xe �¼�����
	 */
	private static void fireEventLocal(short style, XEvent xe)
	{
		//�ɷ���ȫ���¼����������첽���߳����У�
		globalLisDispathcer.dispatch(xe);
		//�ж��¼�������
		switch (style)
		{
			case EventStyle.SYN:

				//ͬ���������
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
	 * ���¼�ͨ��Զ�̽ӿڽ��з��ͣ����Զ�̽ӿ�Ϊnull�������κ���
	 * @param e �¼�����
	 */
	private static void sendToRemote(XEvent e)
	{

	}

	/**
	 * <p>Title: ��ռһ���̵߳Ĵ������</p>
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
	 * ȫ���¼��������ַ�����
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
		 * ���������ͬ������׼
		 */
		private Object schedSynObj = new Object();
		/**
		 * ���в���ͬ������׼
		 */
		private Object queueSynObj = new Object();

		/**
		 * �������е��¼�������
		 */
		private int asynEventCount = 0;
		/**
		 * ѭ��ָ�룬������ͨ��ѭ��ɨ�����ж��б�֤���е��¼������е��¼������Ա����ȵ�
		 */
		private int asynCurPt = -1;

		/**
		 * �¼������飬�������߳�ɨ��
		 */
		private Vector eventQueues = new Vector();
		/**
		 * �¼�����ӳ�䣬�����ж�һ���¼��������Ƿ�����ض��в���
		 */
		private HashMap eventQueueMap = new HashMap();

		/**
		 * �����߳�
		 */
		private Thread schedThread = null;

		/**
		 * �������ж���Runnable����Ӧ��key������̳߳�
		 */
		private KeyThreadPool threadPool = null;

		AsynEventDispatcher()
		{
			//�����̳߳�
			threadPool = new KeyThreadPool(5);
			//���������߳�
			schedThread = new Thread(asynRunner);
			schedThread.start();
		}

		/**
		 * ��һ���¼������¼��������У����ܻ������������ĵ��ȹ���
		 * @param e �¼�����
		 */
		private void dispatchEvent(XEvent e)
		{
			synchronized (queueSynObj)
			{
				//�õ��¼���Ӧ�Ĵ�����У����û�У���������
				LinkedList queue = (LinkedList) eventQueueMap.get(e.getClass());
				if (queue == null)
				{
					queue = (LinkedList) Collections.synchronizedList(new
						LinkedList());
					eventQueues.addElement(queue);
					eventQueueMap.put(e.getClass(), queue);
				}

				//���¼����������
				queue.addLast(e);
				//�������¼�������1
				asynEventCount++;

				//notify to schedule
				schedSynObj.notify();
			}
		}

		/**
		 * ���ȷ����¼�����
		 * @throws Throwable
		 */
		private void schedule()
			throws Throwable
		{
			//��ǰ��������¼��������Ϊ�յȴ���
			if (asynEventCount <= 0)
			{
				schedSynObj.wait();
				//����ѭ�����ö�ջ�ܴ�
				return;
			}

			//��ǰ���û�п��е��̵߳ȴ�
			if (!threadPool.hasIdleWorker())
			{
				schedSynObj.wait();
				//����ѭ�����ö�ջ�ܴ�
				return;
			}

			XEvent xe = null;
			//ѭ��ɨ������飬ֱ���ҵ�һ���¼�����
			int s = eventQueues.size();
			if (s <= 0)
			{
				return;
			}
			//����ָ��ָ����һ������
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
				//��ȡһ���¼�
				xe = (XEvent) q.getFirst();
				//�ж��̳߳����Ƿ���ͬ�����͵��¼�������,������򲻴���
				if (threadPool.containsRunnerKey(xe.getClass()))
				{
					continue;
				}

				//���¼��ض�����ժ��
				q.removeFirst();
				synchronized (queueSynObj)
				{
					//�����������1
					asynEventCount--;
				}
				break;
			}

			//����Runnable����
			Worker w = new Worker(xe);
			//�����̳߳�������
			threadPool.addRunner(xe.getClass(), w);
		}

		/**
		 * ���ȴ���������һ���߳�������
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
	 * ���̳߳������е�Runnable����
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
			//��һ���߳��ڲ�����ͬ���ɷ��¼��ķ���:)
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
