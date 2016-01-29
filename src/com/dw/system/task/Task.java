package com.dw.system.task;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.logger.*;

/**
 * 任务对象，{@see TaskTable}中的内容<br>
 * 任务有很多种类型，主要按照时间的特定进行划分
 */
public class Task
	implements Runnable, Comparable
{
	/**
	 * 空任务
	 */
	public static final short TASK_TYPE_NULL = -1;
	/**
	 * 按照固定时间间隔启动的任务。
	 * 他和startDate和endDate配合使用，如果startDate=null,则使用初时化时间
	 */
	public static final short TASK_TYPE_INTERVAL = 0;
	/**
	 * 某一时刻的任务，在指定的时间上运行一次
	 */
	public static final short TASK_TYPE_SOMETIME = 1;
	/**
	 * 每分钟运行一次
	 */
	public static final short TASK_TYPE_EACH_MIN = 2;
	/**
	 * 每小时运行一次
	 */
	public static final short TASK_TYPE_EACH_HOUR = 3;
	/**
	 * 每天运行一次
	 */
	public static final short TASK_TYPE_EACH_DAY = 4;
	/**
	 * 每周运行一次
	 */
	public static final short TASK_TYPE_EACH_WEEK = 5;
	/**
	 * 每月运行一次
	 */
	public static final short TASK_TYPE_EACH_MONTH = 6;
	/**
	 * 每年运行一次
	 */
	public static final short TASK_TYPE_EACH_YEAR = 7;

	/**
	 * 类型名称和值的对照关系
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
	 * 运行描述
	 */
	transient String runningDesc = null ;
	//transient long lastRunningTime = -1;
	
	/**
	 * 构造空任务
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
	 * 构造新任务
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
	 * 构造一个定时启动的任务
	 *@param name 任务名
	 *@param interval 任务执行时间间隔
	 *@param param 任务参数
	 *@param ths 任务处理者
	 *@param startt 任务起始时间，如果startt==null说明只要
	 *@param endt 任务结束时间
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
	 * 构造新任务
	 */
	public Task(String uid,String name, short type, Calendar d, Date startt, Date endt)
	{
		this(uid,name, type, d);

		this.startDate = startt;
		this.endDate = endt;
	}

	/**
	 * 构造新任务,并且指定一个运行最大时间
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
	 * 得到任务名称
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * 得到任务类型
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
	 * 设置任务起始时间
	 */
	public void setStartDate(Date startt)
	{
		startDate = startt;
	}
	
//	/**
//	 * 得到本任务的线程数
//	 * @return
//	 */
//	public int getMultiThreadNum()
//	{
//		return this.multiThreadNum ;
//	}
//	
//	/**
//	 * 得到本任务的多任务处理的参数生成
//	 * @return
//	 */
//	public String getMultiParamAct()
//	{
//		return this.multiParamAct ;
//	}

	/**
	 * 设置任务终止时间
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
	 * 得到最近任务执行时间
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
	 * 得到上一次被运行的时间
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
	 * 得到任务运行线程。
	 */
	public Thread getrunningThread()
	{
		return runningThread;
	}

	/**
	 * 执行任务：启动一个新的线程进行任务的运行
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
	 * 中断任务
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
	 * 判断任务是否在运行
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
	 * 判断该任务对象是否有效。
	 * 有六种种情况会使任务无效：<br>
	 * 1,空任务
	 * 2,通过调用{@link setValidate(boolean) setValidate}设置任务为无效
	 * 3,对于{@see TASK_TYPE_SOMETIME}类型的过期任务
	 * 4,没有任务的处理者
	 * 5,在终止时间之外
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
		   //还没到起始时间
		   if(startDate!=null&&System.currentTimeMillis()<startDate.getTime())
		 return false ;
		 */
		//已经到终止时间
		if (endDate != null && System.currentTimeMillis() >= endDate.getTime())
		{
			return false;
		}

		return true;
	}

	/**
	 * 设置任务是否有效。<br>
	 * <b>注：就算该方法使用true参数，由于还有很多因素决定任务是否有效，所以
	 * 不能保证任务有效。</b>
	 */
	public void setValidate(boolean bv)
	{
		bValidate = bv;
	}

	/**
	 * 任务的运行过程。它在线程空间内，调用所有相关的处理者进行处理。
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
			{//对应时间间隔的任务,则需要重新调度,因为时间间隔任务在结束之前
				//gap一直变化,不可能被调度到,一旦结束后就需要重新调度
				TaskManager.getInstance().schedule() ;
			}
		}
	}
	
	/**
	 * 为直接运行任务提供的方法，该方法运行任务时不影响任务调度本身
	 * 该方法一般为测试提供支持
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
	 * 获得时间间隔的毫秒数,年月日无效-也没有意义
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
	 * 得到本任务和当前时间比较，将要被调度的时间差。
	 *@return long 时间差毫秒数，如果<0,说明永远不可能被调度到。
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
					return Long.MAX_VALUE ;//运行中的interval任务不能被调度,因为不知道该任务会运行多长时间
				//long sl = getLastEndRunTime() ;
				RunningInfo lri = this.lastRunningInfo ;//避免被突然更新
				if(lri==null||lri.endRunTime<=0)
				{//还没被运行过,则等待一个Interval时间
					lri = new RunningInfo() ;
					lri.endRunTime = System.currentTimeMillis() ;//把当前时间作为上一次运行时间
					this.lastRunningInfo = lri ;
					return vl ;
				}
				
				long sl = lri.endRunTime ;//上一次运行结束时间
				long cl = System.currentTimeMillis();
				
				//已经过了启动时间
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
				//当前分钟或下一分钟
				return ( (retl >= 0) ? retl : (60000 + retl)); //60*1000
				//date.get(Calendar.)
			case TASK_TYPE_EACH_HOUR:
				tmpc = Calendar.getInstance();
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//当前小时或下一小时
				return ( (retl >= 0) ? retl : (3600000 + retl)); //3600*1000
			case TASK_TYPE_EACH_DAY:
				tmpc = Calendar.getInstance();
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//当前天或下一天
				return ( (retl >= 0) ? retl : (86400000 + retl)); //24*3600*1000
			case TASK_TYPE_EACH_WEEK:
				tmpc = Calendar.getInstance();
				tmpc.set(Calendar.DAY_OF_WEEK, date.get(Calendar.DAY_OF_WEEK));
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//当前周或下一周
				return ( (retl >= 0) ? retl : (604800000 + retl)); //7*24*3600*1000
			case TASK_TYPE_EACH_MONTH:
				tmpc = Calendar.getInstance();
				//当前的月份
				int curdayofmonth = tmpc.get(Calendar.DAY_OF_MONTH);
				tmpc.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//当前月
				if (retl >= 0)
				{
					return retl;
				}

				//设置为下一个月的同一时刻
				tmpc.set(Calendar.DAY_OF_MONTH, curdayofmonth + 1);
				return tmpc.getTime().getTime() - System.currentTimeMillis();
			case TASK_TYPE_EACH_YEAR:
				tmpc = Calendar.getInstance();
				//当前的月份
				int curyear = tmpc.get(Calendar.YEAR);
				tmpc.set(Calendar.MONTH, date.get(Calendar.MONTH));
				tmpc.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
				tmpc.set(Calendar.HOUR_OF_DAY, date.get(Calendar.HOUR_OF_DAY));
				tmpc.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
				tmpc.set(Calendar.SECOND, date.get(Calendar.SECOND));
				tmpc.set(Calendar.MILLISECOND, date.get(Calendar.MILLISECOND));
				retl = tmpc.getTime().getTime() - System.currentTimeMillis();
				//当前月
				if (retl >= 0)
				{
					return retl;
				}

				//设置为下一个年的同一时刻
				tmpc.set(Calendar.YEAR, curyear + 1);
				return tmpc.getTime().getTime() - System.currentTimeMillis();
		}
		return -1;
	}

	/**
	 * 安最近要被调度的顺序进行比较，值越小，比较值越小。
	 * 如果一个任务再也不会被调用到，那么它的值Integer.MAX_VALUE
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