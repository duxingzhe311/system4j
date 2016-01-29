package com.dw.system.task;

import java.io.*;
import java.text.*;
import java.util.*;

import com.dw.system.cache.Cacher;


/**
 * �����{@see TaskSchedule}�����������е��ȡ�<br>
 * ���������������У�
 * 1������������
 * 2����ǰ�������
 * 3�������װ�أ����ã�ж�ء�
 * 4��������о١�
 */
public class TaskTable
{
	//Cacher cache = Cacher.getCacher("_COM.CSS.CORE.SCHEDULE.TASKTABLE");

	static java.text.SimpleDateFormat dateFormat = new java.text.
		SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	Hashtable<String,Task> id2Task = new Hashtable<String,Task>() ;
	
	public TaskTable()
	{}
	
	public TaskTable(List<Task> ts)
	{
		if(ts==null||ts.size()<=0)
			return ;
		
		for(Task t:ts)
		{
			id2Task.put(t.getUniqueId(), t);
		}
	}
	/**
	 * װ�������
	 */
//	public void loadTasks(InputStream is)
//		throws IOException, ParseException, ClassNotFoundException,
//		InstantiationException, IllegalAccessException
//	{
//		Prop prop = new Prop();
//		prop.load(is);
//
//		//get task number
//		String tn = prop.getProperty("_TASKNUM");
//		int tasknum;
//		try
//		{
//			tasknum = Integer.parseInt(tn);
//		}
//		catch (NumberFormatException nfe)
//		{
//			throw new TaskException(
//				"Cannot get task number when loading tasks...");
//		}
//
//		Task[] tmpts = new Task[tasknum];
//		for (int i = 0; i < tasknum; i++)
//		{
//			Prop tmpp = getTaskProp(prop, i);
//			tmpts[i] = getTaskByProp(tmpp);
//		}
//
//		for (int i = 0; i < tasknum; i++)
//		{
//			setTask(tmpts[i]);
//		}
//	}

//	/**
//	 * �õ���index�����������
//	 */
//	private Prop getTaskProp(Prop wholeprop, int index)
//	{
//		String tn = wholeprop.getProperty("_TASKNUM");
//		int tasknum;
//		try
//		{
//			tasknum = Integer.parseInt(tn);
//		}
//		catch (NumberFormatException nfe)
//		{
//			return null;
//		}
//
//		if (index >= tasknum)
//		{
//			return null;
//		}
//
//		//��[index]Ϊ��׺�������л�ȡ
//		Prop retp = new Prop();
//		String prefix = "[" + index + "].";
//		int prefixlen = prefix.length();
//		for (Enumeration en = wholeprop.propertyNames(); en.hasMoreElements(); )
//		{
//			String name = (String) en.nextElement();
//			if (!name.startsWith(prefix))
//			{
//				continue;
//			}
//			retp.setProperty(name.substring(prefixlen),
//							 wholeprop.getProperty(name));
//		}
//
//		return retp;
//	}

	/**
	 * �������Եõ��������
	 */
//	private Task getTaskByProp(Prop prop)
//		throws ParseException, ClassNotFoundException,
//		InstantiationException, IllegalAccessException
//	{
//		//get name
//		String name = prop.getProperty("_NAME");
//		if (name == null || name.trim().equals(""))
//		{
//			throw new TaskException(
//				"Find task name is null when loading task ...");
//		}
//		//get type
//		String strtype = prop.getProperty("_TYPE");
//		if (strtype == null || strtype.trim().equals(""))
//		{
//			throw new TaskException("Find task type is null when loading task[" +
//									name + "] ...");
//		}
//		Short typeObj = (Short) Task.TYPE_MAP.get(strtype);
//		if (typeObj == null)
//		{
//			throw new TaskException("Find unsupported task type [" + strtype +
//									"] when loading task[" + name + "] ...");
//		}
//		short type = typeObj.shortValue();
//
//		//get starttime
//		Date startt = null;
//		String strstartt = prop.getProperty("_STARTTIME");
//		if (strstartt != null && !strstartt.trim().equals(""))
//		{
//			startt = dateFormat.parse(strstartt);
//		}
//
//		//get endtime
//		Date endt = null;
//		String strendt = prop.getProperty("_ENDTIME");
//		if (strendt != null && !strendt.trim().equals(""))
//		{
//			endt = dateFormat.parse(strendt);
//		}
//
//		Task task = null;
//		switch (type)
//		{
//			case Task.TASK_TYPE_NULL:
//				break;
//			case Task.TASK_TYPE_SOMETIME:
//			case Task.TASK_TYPE_EACH_MIN:
//			case Task.TASK_TYPE_EACH_HOUR:
//			case Task.TASK_TYPE_EACH_DAY:
//			case Task.TASK_TYPE_EACH_WEEK:
//			case Task.TASK_TYPE_EACH_MONTH:
//			case Task.TASK_TYPE_EACH_YEAR:
//
//				//get time
//				String strtime = prop.getProperty("_TIME");
//				if (strtime == null || strtime.trim().equals(""))
//				{
//					throw new TaskException(
//						"Find task _TIME is null when loading task[" + name +
//						"] ...");
//				}
//
//				//get time or interval
//				Date time = dateFormat.parse(strtime);
//				Calendar cal = Calendar.getInstance();
//				cal.setTime(time);
//				task = new Task(name, type, cal, prop, null);
//				break;
//			case Task.TASK_TYPE_INTERVAL:
//				String st0 = prop.getProperty("_INTERVAL_TIME");
//				if (st0 == null || st0.trim().equals(""))
//				{
//					throw new TaskException(
//						"Find task _INTERVAL_TIME is null when loading interval task[" +
//						name + "] ...");
//				}
//				long interval = Long.parseLong(st0);
//				task = new Task(name, interval, prop, null, startt, endt);
//				break;
//		}
//		//get handlers
//		String strHandler = prop.getProperty("_HANDLER");
//		TaskHandler[] taskHandlers = new TaskHandler[0];
//		if (strHandler != null && !strHandler.trim().equals(""))
//		{
//			StringTokenizer tmpst = new StringTokenizer(strHandler, "|", false);
//			int s = tmpst.countTokens();
//			taskHandlers = new TaskHandler[s];
//			for (int k = 0; k < s; k++)
//			{
//				String cn = tmpst.nextToken();
//				Class c = Class.forName(cn);
//				taskHandlers[k] = (TaskHandler) c.newInstance();
//				if (!taskHandlers[k].isSatisfy(task))
//				{
//					throw new TaskException("Task [" + name +
//											"]is not satisfied with Handler[" +
//											cn + "]!");
//				}
//			}
//		}
//		task.setHandlers(taskHandlers);
//
//		return task;
//	}

	/**
	 * �õ����еı�װ�ص�����
	 */
	public Task[] getAllTasks()
	{
		if(id2Task==null||id2Task.size()<=0)
			return null ;
		Task[] ts = new Task[id2Task.size()];
		id2Task.values().toArray(ts);
		
		return ts;
	}

	/**
	 * ����һ�����������ͬ���Ƶ������Ѿ����ڣ������µ����񸲸Ǿɵġ�
	 * ���û����ͬ���Ƶ�����������
	 */
	public void setTask(Task t)
	{
		if (t == null || t.getName() == null)
		{
			return;
		}

		//cache.cache(t.getName(), t);
		id2Task.put(t.getUniqueId(), t);
	}

	/**
	 * ���������ɾ��һ��������������������У����ᱻ�ж�
	 */
	public void removeTask(String taskid)
	{
		id2Task.remove(taskid);
	}
	
	
	public Task getTaskById(String taskid)
	{
		return id2Task.get(taskid) ;
	}

	/**
	 * ��������.��������������У����ᱻ�ж�
	 */
	public void clear()
	{
		id2Task.clear();
	}

	
	/**
	 * �õ������Ҫ�����ȵ���������
	 */
	public Task[] getAllRecentTask()
	{
		Task[] ts = getAllTasks() ;
		if(ts==null)
			return null ;
		
		Arrays.sort(ts);

		long mysg = ts[0].getScheduledGap();
		if (mysg < 0)
		{
			return null;
		}

		Task[] tmpts = new Task[ts.length];
		//��һ������
		tmpts[0] = ts[0];
		int c = 1;
		//��������
		for (int i = 1; i < ts.length; i++)
		{
			if (!ts[i].isValidate())
			{
				continue;
			}
			//ȷ��ÿ�αȽϵ�ʱ�򶼾����ܵ����������ݡ�
			long tmpt = ts[i].getScheduledGap() - ts[0].getScheduledGap();
			if (tmpt < 0)
			{
				tmpt = -tmpt;
			}
			//��100����֮�ڵ�������񶼿���������ǰ����
			if (tmpt < 100)
			{
				tmpts[c] = ts[i];
				c++;
			}
			else
			{
				break;
			}
		}

		Task[] rets = new Task[c];
		System.arraycopy(tmpts, 0, rets, 0, c);
		return rets;
	}

	/**
	 * �õ������Ҫ�����ȵ�һ������
	 */
	public Task getRecentTask()
	{
		Task[] ts = getAllTasks();

		if (ts == null || ts.length == 0)
		{
			return null;
		}

		Arrays.sort(ts);
		if (!ts[0].isValidate())
		{
			return null;
		}

		return ts[0];
	}

	public static void main(String[] args)
	{
		TaskTable tt = new TaskTable(null);

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

//						Task t1 = new Task(name, type, cal);
//						tt.setTask(t1);
					}
					else if ("load".equals(cmds[0]))
					{
						File f = new File(cmds[1]);
						FileInputStream fis = new FileInputStream(f);
						//tt.loadTasks(fis);
						fis.close();
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