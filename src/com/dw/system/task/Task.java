package com.dw.system.task;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.logger.*;

/**
 * �������{@see TaskTable}�е�����<br>
 * �����кܶ������ͣ���Ҫ����ʱ����ض����л���
 */
public class Task
	implements Runnable, Comparable
{
	/**
	 * ������
	 */
	public static final short TASK_TYPE_NULL = -1;
	/**
	 * ���չ̶�ʱ��������������
	 * ����startDate��endDate���ʹ�ã����startDate=null,��ʹ�ó�ʱ��ʱ��
	 */
	public static final short TASK_TYPE_INTERVAL = 0;
	/**
	 * ĳһʱ�̵�������ָ����ʱ��������һ��
	 */
	public static final short TASK_TYPE_SOMETIME = 1;
	/**
	 * ÿ��������һ��
	 */
	public static final short TASK_TYPE_EACH_MIN = 2;
	/**
	 * ÿСʱ����һ��
	 */
	public static final short TASK_TYPE_EACH_HOUR = 3;
	/**
	 * ÿ������һ��
	 */
	public static final short TASK_TYPE_EACH_DAY = 4;
	/**
	 * ÿ������һ��
	 */
	public static final short TASK_TYPE_EACH_WEEK = 5;
	/**
	 * ÿ������һ��
	 */
	public static final short TASK_TYPE_EACH_MONTH = 6;
	/**
	 * ÿ������һ��
	 */
	public static final short TASK_TYPE_EACH_YEAR = 7;

	/**
	 * �������ƺ�ֵ�Ķ��չ�ϵ
	 */
	public static Hashtable<String,Short> TYPE_MAP = new Hashtable<String,Short>();
	static
	{
		TYPE_MAP.put("EMPTY", new Short(TASK_TYPE_NULL));
		TYPE_MAP.put("INTERVAL", new Short(TASK_TYPE_INTERVAL));
		TYPE_MAP.put("SOMETIME", new Short(TASK_TYPE_SOMETIME));
		TYPE_MAP.put("EACH_MINUTE", new Short(TASK_TYPE_EACH_MIN));
		TYPE_MAP.put("EACH_HOUR", new Short(TASK_TYPE_EACH_HOUR));
		TYPE_MAP.put("EACH_DAY", new Short(TASK_TYPE_EACH_DAY));
		TYPE_MAP.put("EACH_WEEK", new Short(TASK_TYPE_EACH_WEEK));
		TYPE_MAP.put("EACH_MONTH", new Short(TASK_TYPE_EACH_MONTH));
		TYPE_MAP.put("EACH_YEAR", new Short(TASK_TYPE_EACH_YEAR));
	}
	
	public static final String T_NAME = "_NAME" ;
	public static final String T_TYPE = "_TYPE" ;
	public static final String T_HANDLER = "_HANDLER" ;
	public static final String T_DATE = "_DATE" ;
	public static final String T_START_DATE = "_START_DATE" ;
	public static final String T_END_DATE = "_END_DATE" ;
	public static final String T_VALIDATE = "_VALIDATE" ;
	public static final String T_RUN_TIMEOUT = "_RUN_TIMEOUT" ;
	

	static ILogger log = LoggerManager.getLogger("TaskSchedule.log");
	
	public static class RunningInfo
	{
		long startRunTime = -1 ;
		long endRunTime = -1 ;
		
		public long getStartRunTime()
		{
			return startRunTime ;
		}
		
		public long getEndRunTime()
		{
			return endRunTime ;
		}
	}
	
//	public static Task createTask(HashMap<String,String> taskinfo,HashMap<String,String> taskpms)
//	{
//		try
//		{
//			return new Task(taskinfo,taskpms) ;
//		}
//		catch(Exception e)
//		{
//			return null ;
//		}
//	}

	String uniqueid = null ;
	
	String name = null;
	short type = TASK_TYPE_NULL;
	String handler = null ;
	Calendar date = null;
	Date startDate = null;
	Date endDate = null;
	boolean bValidate = true;
	long runningTimeout = -1;
	
	HashMap<String,String> taskParms = null ;
	
	transient Thread runningThread = null;
	
	transient RunningInfo lastRunningInfo = null ;
	
	transient RunningInfo curRunningInfo = null ;
	/**
	 * ��������
	 */
	transient String runningDesc = null ;
	//transient long lastRunningTime = -1;
	
	/**
	 * ���������
	 */
	public Task()
	{

	}
	
	public Task(String uid,HashMap<String,String> taskinfo,HashMap<String,String> task_pms)
	{
		if(Convert.isNullOrEmpty(uid))
			throw new IllegalArgumentException("task unique id cannot be null or empty!");
		
		uniqueid = uid ;
		
		name = taskinfo.get(T_NAME);
		if(Convert.isNullOrEmpty(name))
			throw new IllegalArgumentException("no task "+T_NAME+" found!");
		
		handler = taskinfo.get(T_HANDLER);
		if(Convert.isNullOrEmpty(handler))
			throw new IllegalArgumentException("no task "+T_HANDLER+" found!");
		
		String str_type = taskinfo.get(T_TYPE);
		type = TYPE_MAP.get(str_type);
		
		String str_date = taskinfo.get(T_DATE) ;
		date = Convert.toCalendar(str_date);
		
		String str_st = taskinfo.get(T_START_DATE);
		if(!Convert.isNullOrEmpty(str_st))
		{
			startDate = Convert.toCalendar(str_st).getTime();
		}
		else
		{
			startDate = new Date() ;
		}
		
		String str_et = taskinfo.get(T_END_DATE);
		if(!Convert.isNullOrEmpty(str_et))
		{
			endDate = Convert.toCalendar(str_et).getTime() ;
		}
		
		String str_timeout = taskinfo.get(T_RUN_TIMEOUT);
		if(!Convert.isNullOrEmpty(str_timeout))
		{
			runningTimeout = Long.parseLong(str_timeout);
		}
		
		taskParms = task_pms ;
	}

	/**
	 * ����������
	 */
	public Task(String uid,String name, short type, Calendar d)
	{
		if(Convert.isNullOrEmpty(uid))
			throw new IllegalArgumentException("task unique id cannot be null or empty!");
		
		uniqueid = uid ;
		
		if (type == TASK_TYPE_INTERVAL)
		{
			throw new TaskException("interval task type require start time");
		}
		this.name = name;
		this.type = type;
		this.date = d;
	}

	/**
	 * ����һ����ʱ����������
	 *@param name ������
	 *@param interval ����ִ��ʱ����
	 *@param param �������
	 *@param ths ��������
	 *@param startt ������ʼʱ�䣬���startt==null˵��ֻҪ
	 *@param endt �������ʱ��
	 */
	public Task(String uid,String name, long interval,
				Date startt, Date endt)
	{
		if(Convert.isNullOrEmpty(uid))
			throw new IllegalArgumentException("task unique id cannot be null or empty!");
		
		uniqueid = uid ;
		
		this.name = name;
		this.type = TASK_TYPE_INTERVAL;

		this.date = Calendar.getInstance();
		this.date.setTime(new Date(interval));

		if (startt == null)
		{
			this.startDate = new Date();
		}
		else
		{
			this.startDate = startt;
		}

		this.endDate = endt;
	}

	/**
	 * ����������
	 */
	public Task(String uid,String name, short type, Calendar d, Date startt, Date endt)
	{
		this(uid,name, type, d);

		this.startDate = startt;
		this.endDate = endt;
	}

	/**
	 * ����������,����ָ��һ���������ʱ��
	 */
	public Task(String uid,String name, short type, Calendar d,long rtimeout)
	{
		if(Convert.isNullOrEmpty(uid))
			throw new IllegalArgumentException("task unique id cannot be null or empty!");
		
		uniqueid = uid ;
		
		this.name = name;
		this.type = type;
		this.date = d;
		//this.param = param;
		//this.handlers = ths;
		this.runningTimeout = rtimeout;
	}
	
	public HashMap<String,String> getTaskParms()
	{
		return taskParms ;
	}
	
	public String getUniqueId()
	{
		return uniqueid;
	}
	/**
	 * �õ���������
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * �õ���������
	 */
	public short getType()
	{
		return type;
	}
	
	public ThreadGroup getTaskThreadGroup()
	{
		return TaskManager.taskThGroup ;
	}

	/**
	 * ����������ʼʱ��
	 */
	public void setStartDate(Date startt)
	{
		startDate = startt;
	}
	
//	/**
//	 * �õ���������߳���
//	 * @return
//	 */
//	public int getMultiThreadNum()
//	{
//		return this.multiThreadNum ;
//	}
//	
//	/**
//	 * �õ�������Ķ�������Ĳ�������
//	 * @return
//	 */
//	public String getMultiParamAct()
//	{
//		return this.multiParamAct ;
//	}

	/**
	 * ����������ֹʱ��
	 */
	public void setEndDate(Date endt)
	{
		endDate = endt;
	}

	public void setRunningDesc(String rd)
	{
		runningDesc = rd ;
	}
	/**
	 * �õ��������ִ��ʱ��
	 */
	public Date getRecentRuningTime()
	{
		long gap = getScheduledGap();
		if (gap < 0)
		{
			return null;
		}

		Date tmpd = new Date();
		tmpd.setTime(tmpd.getTime() + gap);
		return tmpd;
	}

	/**
	 * �õ���һ�α����е�ʱ��
	 * @return
	 */
	RunningInfo getLastRunningInfo()
	{
		return lastRunningInfo;
	}

	public long getLastRunningTime()
	{
		if(lastRunningInfo==null)
			return -1 ;
		
		return lastRunningInfo.startRunTime ;
	}
	/**
	 * �õ����������̡߳�
	 */
	public Thread getrunningThread()
	{
		return runningThread;
	}

	/**
	 * ִ����������һ���µ��߳̽������������
	 */
	public synchronized void runTask()
	{
		if(isRunning())
			return;
		
		TaskCtrl tc = TaskManager.getInstance().getTaskCtrl() ;
		if(tc!=null)
		{
			if(!tc.checkTaskCtrlRun(getUniqueId()))
			{
				if(log.isDebugEnabled())
					log.debug("Task - "+getUniqueId() +" is not run by ctrl!") ;
				return ;
			}
		}
		
		runningThread = new Thread(TaskManager.taskThGroup,this,"Task-"+this.getUniqueId());
		runningThread.start();
		if(log.isInfoEnabled())
			log.info("Task>>" + toString());
	}

	/**
	 * �ж�����
	 */
	public void interruptTask()
	{
		Thread t = runningThread ;
		if (t == null || !t.isAlive())
		{
			return;
		}
		t.interrupt();
		runningThread = null;
	}

	/**
	 * �ж������Ƿ�������
	 */
	public boolean isRunning()
	{
//		Thread t = runningThread ;
//		if (t == null)
//		{
//			return false;
//		}
//		return t.isAlive();
		return runningThread!=null;
	}

	/**
	 * �жϸ���������Ƿ���Ч��
	 * �������������ʹ������Ч��<br>
	 * 1,������
	 * 2,ͨ������{@link setValidate(boolean) setValidate}��������Ϊ��Ч
	 * 3,����{@see TASK_TYPE_SOMETIME}���͵Ĺ�������
	 * 4,û������Ĵ�����
	 * 5,����ֹʱ��֮��
	 *
	 */
	public boolean isValidate()
	{
		if (type == TASK_TYPE_NULL)
		{
			return false;
		}
		if (!bValidate)
		{
			return false;
		}
		if (TASK_TYPE_SOMETIME == type)
		{
			if (date == null)
			{
				return false;
			}
			if (date.getTime().getTime() < System.currentTimeMillis())
			{
				return false;
			}
		}
		
		/*
		   //��û����ʼʱ��
		   if(startDate!=null&&System.currentTimeMillis()<startDate.getTime())
		 return false ;
		 */
		//�Ѿ�����ֹʱ��
		if (endDate != null && System.currentTimeMillis() >= endDate.getTime())
		{
			return false;
		}

		return true;
	}

	/**
	 * ���������Ƿ���Ч��<br>
	 * <b>ע������÷���ʹ��true���������ڻ��кܶ����ؾ��������Ƿ���Ч������
	 * ���ܱ�֤������Ч��</b>
	 */
	public void setValidate(boolean bv)
	{
		bValidate = bv;
	}

	/**
	 * ��������й��̡������߳̿ռ��ڣ�����������صĴ����߽��д���
	 */
	public void run()
	{
		if(log.isInfoEnabled())
		{
			log.info("Task Running..."+getName());
		}
		
		RunningInfo ri = new RunningInfo();
		
		try
		{
			TaskHandler th = TaskManager.getTaskHandler(handler);
			if(th==null)
			{
				if(log.isWarnEnabled())
				{
					log.warn("no task handler with name="+handler);
				}
				return ;
			}
			
			ri.startRunTime = System.currentTimeMillis() ;
			
			th.handleTask(this);
			
			if(log.isInfoEnabled())
			{
				log.info("Task Running finish:"+getName());
			}
		}
		catch(Exception e)
		{
			if(log.isErrorEnabled())
				log.error(e);
		}
		finally
		{
			ri.endRunTime = System.currentTimeMillis() ;
			this.lastRunningInfo = ri ;
			
			this.runningThread = null ;
			
			if(this.type==Task.TASK_TYPE_INTERVAL)
			{//��Ӧʱ����������,����Ҫ���µ���,��Ϊʱ���������ڽ���֮ǰ
				//gapһֱ�仯,�����ܱ����ȵ�,һ�����������Ҫ���µ���
				TaskManager.getInstance().schedule() ;
			}
		}
	}
	
	/**
	 * Ϊֱ�����������ṩ�ķ������÷�����������ʱ��Ӱ��������ȱ���
	 * �÷���һ��Ϊ�����ṩ֧��
	 */
	void runInner()
	{
		if(log.isInfoEnabled())
		{
			log.info("Task Inner Running..."+getName());
		}
		
		TaskHandler th = TaskManager.getTaskHandler(handler);
		if(th==null)
		{
			if(log.isWarnEnabled())
			{
				log.warn("no task handler with name="+handler);
			}
			return ;
		}
		
		try
		{
			th.handleTask(this);
			if(log.isInfoEnabled())
			{
				log.info("Task Inner Running finish:"+getName());
			}
		}
		catch(Exception e)
		{
			if(log.isErrorEnabled())
				log.error(e);
		}
		finally
		{
			
		}
	}
	
	
	private transient long dateInterval = -1 ;
	
	/**
	 * ���ʱ�����ĺ�����,��������Ч-Ҳû������
	 * @return
	 */
	private long getDateInterval()
	{
		if(dateInterval>0)
			return dateInterval ;
		
		long v = date.get(Calendar.HOUR_OF_DAY)*3600 ;
		v += date.get(Calendar.MINUTE) * 60 ;
		v += date.get(Calendar.SECOND) ;
		
		v *= 1000 ;
		return v ;
	}

	/**
	 * �õ�������͵�ǰʱ��Ƚϣ���Ҫ�����ȵ�ʱ��
	 *@return long ʱ�������������<0,˵����Զ�����ܱ����ȵ���
	 */
	long getScheduledGap()
	{
		/*
		   if(endDate!=null&&endDate.getTime()<=System.currentTimeMillis())
		 return -1 ;
		 */
		if (!isValidate())
		{
			return -1;
		}

		Calendar tmpc = null;
		long retl = -1;
		switch (type)
		{
			case TASK_TYPE_NULL:
				return -1;
			case TASK_TYPE_SOMETIME:
				return date.getTime().getTime() - System.currentTimeMillis();
			case TASK_TYPE_INTERVAL:
//				interval
				long vl = getDateInterval() ;
				if(this.isRunning())
					return Long.MAX_VALUE ;//�����е�interval�����ܱ�����,��Ϊ��֪������������ж೤ʱ��
				//long sl = getLastEndRunTime() ;
				RunningInfo lri = this.lastRunningInfo ;//���ⱻͻȻ����
				if(lri==null||lri.endRunTime<=0)
				{//��û�����й�,��ȴ�һ��Intervalʱ��
					lri = new RunningInfo() ;
					lri.endRunTime = System.currentTimeMillis() ;//�ѵ�ǰʱ����Ϊ��һ������ʱ��
					this.lastRunningInfo = lri ;
					return vl ;
				}
				
				long sl = lri.endRunTime ;//��һ�����н���ʱ��
				long cl = System.currentTimeMillis();
				
				//�Ѿ���������ʱ��
				if (cl >= sl)
				{
					return vl - ( (cl - sl) % vl);
				}
				else
				{
					return vl ;
				}
			case TASK_TYPE_EACH_MIN:
				tmpc = Calendar.getInstance();
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//��ǰ���ӻ���һ����
				return ( (retl >= 0) ? retl : (60000 + retl)); //60*1000
				//date.get(Calendar.)
			case TASK_TYPE_EACH_HOUR:
				tmpc = Calendar.getInstance();
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//��ǰСʱ����һСʱ
				return ( (retl >= 0) ? retl : (3600000 + retl)); //3600*1000
			case TASK_TYPE_EACH_DAY:
				tmpc = Calendar.getInstance();
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//��ǰ�����һ��
				return ( (retl >= 0) ? retl : (86400000 + retl)); //24*3600*1000
			case TASK_TYPE_EACH_WEEK:
				tmpc = Calendar.getInstance();
				tmpc.set(Calendar.DAY_OF_WEEK, date.get(Calendar.DAY_OF_WEEK));
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//��ǰ�ܻ���һ��
				return ( (retl >= 0) ? retl : (604800000 + retl)); //7*24*3600*1000
			case TASK_TYPE_EACH_MONTH:
				tmpc = Calendar.getInstance();
				//��ǰ���·�
				int curdayofmonth = tmpc.get(Calendar.DAY_OF_MONTH);
				tmpc.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//��ǰ��
				if (retl >= 0)
				{
					return retl;
				}

				//����Ϊ��һ���µ�ͬһʱ��
				tmpc.set(Calendar.DAY_OF_MONTH, curdayofmonth + 1);
				return tmpc.getTime().getTime() - System.currentTimeMillis();
			case TASK_TYPE_EACH_YEAR:
				tmpc = Calendar.getInstance();
				//��ǰ���·�
				int curyear = tmpc.get(Calendar.YEAR);
				tmpc.set(Calendar.MONTH, date.get(Calendar.MONTH));
				tmpc.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//��ǰ��
				if (retl >= 0)
				{
					return retl;
				}

				//����Ϊ��һ�����ͬһʱ��
				tmpc.set(Calendar.YEAR, curyear + 1);
				return tmpc.getTime().getTime() - System.currentTimeMillis();
		}
		return -1;
	}

	/**
	 * �����Ҫ�����ȵ�˳����бȽϣ�ֵԽС���Ƚ�ֵԽС��
	 * ���һ��������Ҳ���ᱻ���õ�����ô����ֵInteger.MAX_VALUE
	 */
	public int compareTo(Object o)
	{
		if (! (o instanceof Task))
		{
			throw new TaskException(
				"Task Object Cannot Compare to other Class!");
		}
		Task other = (Task) o;

		long mysg = getScheduledGap();
		long otsg = other.getScheduledGap();

//		int mhvalue = Integer.MAX_VALUE;
//		int othervalue = Integer.MAX_VALUE;

//		if (mysg >= 0)
//		{
//			mhvalue = (int) mysg / 1000;
//		}
//		if (otsg >= 0)
//		{
//			othervalue = (int) otsg / 1000;
//		}
//		return mhvalue - othervalue;
		
		if(mysg>otsg)
			return 1 ;
		if(mysg<otsg)
			return -1 ;
		return 0 ;
	}

	public String toString()
	{
		if (type == TASK_TYPE_NULL)
		{
			return "[^][" + name + "]";
		}
		java.text.SimpleDateFormat spf = new java.text.SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
		String strdate = spf.format(date.getTime());
		return "id="+this.getUniqueId()+"\r\n\t[date=" + strdate + "][type=" + type + "][" + name + "][" +
			isValidate() + "][gap=" + getScheduledGap() + "][running="+isRunning()+" >Running Desc="+runningDesc+"]";
	}

}