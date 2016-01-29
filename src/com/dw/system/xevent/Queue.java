package com.dw.system.xevent;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class Queue
{
	private LinkedList linkedList = new LinkedList();

	public Queue()
	{

	}

	synchronized public boolean isEmpty()
	{
		return (linkedList.size() == 0);
	}

	synchronized public int size()
	{
		return linkedList.size();
	}

	public synchronized void enqueue(Object o)
	{
		linkedList.addLast(o);
		notify();
	}

	public synchronized Object dequeue()
	{
		Object tmpb = null;
		if (linkedList.isEmpty())
		{
			try
			{
				//System.out.println("waiting...");
				wait();
			}
			catch (InterruptedException ie)
			{}
		}
		return linkedList.removeFirst();
	}

	/**
	 * 清空，队列中的所有内容
	 */
	public synchronized void emptyQueue()
	{
		linkedList.clear();
	}


}