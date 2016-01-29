package com.dw.ttt;

import com.dw.system.xevent.XEventListener;
import com.dw.system.xevent.XEvent;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public class TTTLis
	implements XEventListener
{
	public void onEvent(XEvent event)
	{
		System.out.println("TTTLis recved start :" + event);
		try
		{
			Thread.sleep(5000);
		}
		catch (Exception e)
		{}
		System.out.println("TTTLis recved end :" + event);
	}

	public String getListenerDesc()
	{
		return "TTTLis";
	}
}