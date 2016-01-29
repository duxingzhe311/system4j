package com.dw.system.cache;

import java.io.*;
import java.util.*;

import com.dw.system.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

class CacherLogger
{
	static Log log = null;
	static Thread logThreader = null;
	static boolean bRun = false;

	static
	{
		try
		{
			log = Log.getLog("CacherMonLogger.log");
		}
		catch (Exception e)
		{
			log = Log.getLog();
		}
	}

	static Runnable logRunner = new Runnable()
	{
		public void run()
		{
			while (bRun)
			{
				try
				{
					Thread.sleep(120000);
					doLog();
				}
				catch (Exception e)
				{
					if (log != null)
					{
						log.log(e);
					}
				}
			}
			logThreader = null;
		}
	};

	static void startLog()
	{
		if (bRun||logThreader!=null)
		{
			return;
		}
		bRun = true;
		logThreader = new Thread(logRunner,"cache-logger");
		logThreader.start();
	}

	static void stopLog()
	{
		bRun = false;
	}

	static void doLog()
	{
		if (log == null)
		{
			return;
		}

		log.log("\n------------CacherLogger[" + new Date() + "]-----------\n");
		String[] allCacherNames = Cacher.getAllCacherNames();
		for (int k = 0; k < allCacherNames.length; k++)
		{
			Cacher tmpccc = Cacher.getCacher(allCacherNames[k]);
			log.log(allCacherNames[k] +"["+ tmpccc.getAllKeys().length+"]");
		}
		log.log("\n----------------------------------------\n");
	}
}