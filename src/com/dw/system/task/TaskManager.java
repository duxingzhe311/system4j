package com.dw.system.task;

import java.io.*;
import java.util.*;

import org.w3c.dom.Element;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.logger.*;
import com.dw.system.xmldata.XmlHelper;

/**
 * 任务调度。
 * <b>注：该任务调度器，不适合调度频度在分钟之内的任务。</b>
 */
public class TaskManager
	extends TimerTask
{
	static String[] handlerCns = new String[]{
		"com.dw.biz.BizActionTaskHandler",
	} ;
	
	static ILogger log = LoggerManager.getLogger("TaskSchedule.log");

	
	
	static HashMap<String,TaskHandler> name2handler = null ;
	
	static Object locker1 = new Object() ;
	
	public static TaskHandler getTaskHandler(String handler_name)
	{
		return getNameTaskHandlers().get(handler_name);
	}
	
	
	private static HashMap<String,TaskHandler> getNameTaskHandlers()
	{
		if(name2handler!=null)
			return name2handler;
		
		synchronized(locker1)
		{
			if(name2handler!=null)
				return name2handler;
			
			HashMap<String,TaskHandler> n2h = new HashMap<String,TaskHandler>();
			
			for(String cn : handlerCns)
			{
				try
				{
					Class c = Class.forName(cn);
					if(c==null)
						continue ;
					
					TaskHandler th = (TaskHandler)c.newInstance() ;
					n2h.put(th.getHandlerName(), th);
				}
				catch(Exception e)
				{
					if(log.isErrorEnabled())
						log.error(e);
				}
			}
			
			name2handler = n2h ;
			return name2handler;
		}
	}
	
	/**
	 * 获得所有的任务处理器
	 * @return
	 */
	public static List<TaskHandler> getAllTaskHandler()
	{
		HashMap<String,TaskHandler> n2h = getNameTaskHandlers();
		ArrayList<TaskHandler> rets =new ArrayList<TaskHandler> () ;
		for(TaskHandler th : n2h.values())
			rets.add(th);
		return rets ;
	}
	
	static TaskManager taskMgr = null;

	static Object locker = new Object() ;
	
	static ThreadGroup taskThGroup = new ThreadGroup("Task") ;
	
	public static ThreadGroup getTaskThreadGroup()
	{
		return taskThGroup ;
	}
	
	static
	{
		
	}

	public static TaskManager getInstance()
	{
		if(taskMgr!=null)
			return taskMgr ;
		
		synchronized(locker)
		{
			try
			{
				taskMgr = new TaskManager();
				taskMgr.loadConf();
				return taskMgr ;
			}
			catch (Throwable _t)
			{
				if(log.isErrorEnabled())
					log.error(_t);
				return null ;
			}
		}
	}

	
	TaskTable taskTable = new TaskTable() ;
	
	
	
	/**
	 * 当前任务
	 */
	Task[] recentTasks = null;
	/**
	 * 任务调度计数器
	 */
	ScheduleTimer taskScheduleTimer = null;
	
	
	TaskCtrl taskCtrl = null ;
	
	/**
	 * 构造一个任务调度器
	 */
	private TaskManager()
		throws Throwable
	{
		//taskScheduleTimer.s

		//schedule();
		taskCtrl = TaskCtrl.loadTaskCtrl() ;
		
		taskScheduleTimer = new ScheduleTimer(taskThGroup);
		
		
	}
	
	
	private void loadConf()
	{
		Element ele = AppConfig.getConfElement("tasks");
		if(ele==null)
			return ;
		Element[] eles = XmlHelper.getSubChildElement(ele, "task");
		if(eles==null||eles.length<=0)
			return ;
		
		for(Element sele:eles)
		{
			HashMap<String, String> taskinfo = new HashMap<String, String>();
			HashMap<String, String> taskpms = new HashMap<String, String>();

			try
			{
				HashMap<String,String> n2v = XmlHelper.getElementAttrMap(sele) ;
				for(String pn:n2v.keySet())
				{
					if (pn.startsWith("_"))
						taskinfo.put(pn, n2v.get(pn));
					else
						taskpms.put(pn, n2v.get(pn));
				}
				
				String taskid = taskinfo.get(Task.T_NAME);
				Task t = new Task(taskid,taskinfo, taskpms);
				// cm.getTaskTable().setTask(t);
				if(log.isInfoEnabled())
					log.info("find task="+t.toString());
				TaskManager.getInstance().setTask(t);
				
				//appTaskIds.add(taskid) ;
			}
			catch (Throwable ioe)
			{
				//ioe.printStackTrace();
				System.out.println("Warn : set task error="+ioe.getMessage()) ;
				
				if(log.isErrorEnabled())
					log.error(ioe);
				continue;
			}
		}
		
	}
	
	public TaskCtrl getTaskCtrl()
	{
		return taskCtrl ;
	}
	
	public void setTaskCtrl(TaskCtrl tc) throws Exception
	{
		TaskCtrl.saveTaskCtrl(tc) ;
		taskCtrl = tc ;
	}
	/**
	 * 得到任务表
	 */
	public TaskTable getTaskTable()
	{
		return taskTable;
	}
	
	void checkTask(Task t)
	{
		TaskHandler th = getTaskHandler(t.handler);
		if(th==null)
		{
			throw new IllegalArgumentException("no handler found="+t.handler) ;
		}
		
		StringBuilder sb = new StringBuilder () ;
		if(!th.checkTask(t, sb))
			throw new IllegalArgumentException(sb.toString()) ;
	}

	
	public void setTask(Task t)
	{
		checkTask(t) ;
		
		taskScheduleTimer.start();
		
		taskTable.setTask(t);
		schedule();
	}
	
	
	public void unsetTask(String taskid)
	{
		taskTable.removeTask(taskid) ;
		schedule();
	}
	
	/**
	 * 重新装载任务表
	 */
	synchronized public void setTaskTable(TaskTable tt)
	//	throws Throwable
	{
		taskScheduleTimer.start();
		
		taskTable = tt ;

		schedule();
	}

	/**
	 * 对任务进行调度或重新调度
	 * 一般来说，当任务表的内容改变时，该方法要被调用到，进行重新调度
	 */
	public void schedule()
	{
		//Task tmpt = taskTable.getRecentTask() ;
		if(taskTable==null)
			return ;
		
		Task[] tmpt = taskTable.getAllRecentTask();
		if (tmpt == null || tmpt.length == 0)
		{ //System.out.println ("no task scheduled") ;
			recentTasks = null;
			return;
		}

		//System.out.println ("schedule task=="+tmpt) ;

		Date rrt = tmpt[0].getRecentRuningTime();
		if (rrt == null)
		{
			return;
		}
		//System.out.println ("------->"+rrt.getTime()) ;
		recentTasks = tmpt;
		taskScheduleTimer.schedule(this, rrt);
	}

	

	public void run()
	{
		long interval = 1 ;
		try
		{
			if (recentTasks == null || recentTasks.length == 0)
			{
				return;
			}

			for (int i = 0; i < recentTasks.length; i++)
			{
				if (recentTasks[i].isRunning())
				{
					if(recentTasks[i].getType()==Task.TASK_TYPE_INTERVAL)
						continue ;
					
					if(log.isWarnEnabled())
						log.warn("Task[" + recentTasks[i].getName() +
							"] is scheduled in running!");
				}
				if((System.currentTimeMillis()-recentTasks[i].getLastRunningTime())<100)
					continue ;

				recentTasks[i].runTask();
			}

			interval = recentTasks[0].getScheduledGap() ;
		}
		catch (Throwable _t)
		{
			if(log.isErrorEnabled())
				log.error(_t);
		}
		finally
		{
			recentTasks = null;
			try
			{ //sleep 2 millisseconds before schedule
				Thread.sleep(1);
			}
			catch (Exception ee)
			{}
			schedule();
		}
	}
	
	/**
	 * 根据任务id，直接运行之，该运行不影响原有task的调度过程
	 * 该方法一般为测试提供支持
	 * @param taskid
	 */
	public void runTaskNow(String taskid)
	{
		Task t = taskTable.getTaskById(taskid) ;
		if(t==null)
			return ;
		
		t.runInner() ;
	}

	public static void main(String[] args)
	{
		TaskManager tsh = TaskManager.getInstance();

		TaskTable tt = tsh.getTaskTable();

		try
		{
			//SockConn sss = connPool.getSock() ;
			String inputLine;
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
				System.in));

			String lastCmds = null;
			while ( (inputLine = in.readLine()) != null)
			{
				if (inputLine == null || inputLine.trim().equals(""))
				{
					if (lastCmds != null)
					{
						inputLine = lastCmds;
					}
					else
					{
						continue;
					}
				}
				else
				{
					lastCmds = inputLine;
				}

				try
				{
					StringTokenizer st = new StringTokenizer(inputLine, " ", false);
					String cmds[] = new String[st.countTokens()];
					for (int i = 0; i < cmds.length; i++)
					{
						cmds[i] = st.nextToken();

					}
					if ("ls".equals(cmds[0]))
					{
						Task[] ts = tt.getAllTasks();
						if (ts == null)
						{
							continue;
						}
						Arrays.sort(ts);
						for (int i = 0; i < ts.length; i++)
						{
							System.out.println(ts[i]);
						}
						System.out.println(">>>>>>>>>>>>>>>>>>recent task...");
						ts = tt.getAllRecentTask();
						for (int i = 0; i < ts.length; i++)
						{
							System.out.println(ts[i]);
						}
					}
					else if ("addtask".equals(cmds[0]))
					{
						System.out.print("Input Name>");
						String name = in.readLine();
						System.out.print("Input Type>");
						String strtype = in.readLine();
						short type = Short.parseShort(strtype);
						System.out.print("Input time[yyyy-MM-dd HH:mm:ss]>");
						String strdate = in.readLine();
						java.text.SimpleDateFormat sdf = new java.text.
							SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date tmpd = sdf.parse(strdate);
						Calendar cal = Calendar.getInstance();
						cal.setTime(tmpd);

						//Task t1 = new Task(name, type, cal, null,
						//				   new TaskHandler[1]);
						//tt.setTask(t1);
					}
					else if ("load".equals(cmds[0]))
					{
						File f = new File(cmds[1]);
						FileInputStream fis = new FileInputStream(f);
						//tt.loadTasks(fis);
						fis.close();
					}
					else if ("schedule".equals(cmds[0]))
					{
						tsh.schedule();
					}
					else if ("reload".equals(cmds[0]))
					{
						//tsh.reloadTaskTable();
					}
					else if ("remove".equals(cmds[0]))
					{
						tt.removeTask(cmds[1]);
					}
					else if ("clear".equals(cmds[0]))
					{
						tt.clear();
					}
					else if ("x".equals(cmds[0]))
					{
						System.exit(0);
					}

				}
				catch (Throwable _e)
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