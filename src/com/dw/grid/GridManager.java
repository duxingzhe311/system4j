package com.dw.grid;

import java.io.*;
import java.util.*;

import com.dw.system.*;

public class GridManager
{
	static Object locker = new Object();

	static GridManager instance = null;

	public static GridManager getInstance()
	{
		if (instance != null)
			return instance;

		synchronized (locker)
		{
			if (instance != null)
				return instance;

			instance = new GridManager();
			return instance;
		}
	}

	File tmpRecvDir = null;

	private GridManager()
	{
		tmpRecvDir = new File(AppConfig.getDataDirBase() + "/xdwf_recv/");
		tmpRecvDir.mkdirs();
	}

	public File getTmpRecvDir()
	{
		return tmpRecvDir;
	}

	public File getTmpRecvFile(String name)
	{
		return new File(tmpRecvDir, name + ".qi");
	}

	FilenameFilter ff = new FilenameFilter()
	{
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".qi");
		}

	};

	public ArrayList<String> getTmpRecvFileNames()
	{
		ArrayList<String> rets = new ArrayList<String>();
		File[] fs = tmpRecvDir.listFiles(ff);
		if (fs == null)
			return rets;

		for (File f : fs)
		{
			String fn = f.getName().trim();
			rets.add(fn.substring(0, fn.length() - 3));
		}
		return rets;
	}
}
