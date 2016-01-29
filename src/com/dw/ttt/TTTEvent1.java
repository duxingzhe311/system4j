package com.dw.ttt;

import com.dw.system.xevent.XEvent;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public class TTTEvent1
	extends XEvent
{
	int id = -1;
	public TTTEvent1(String n, int id)
	{
		super(n);
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public String toString()
	{
		return "TTTEvent1:id=" + id;
	}
}