package com.dw.net.broadcast ;

import java.io.*;
import java.util.*;
/**
 * 经过测试，发现如果发送端发送的消息太快，
 * 单位时间内发送的消息个数在同等时间内接收端处理不过来，丢失包的概率很高。
 * 所以需要在接收端做一个队列，尽可能的减少接收时间——不受处理方法所花的时间影响
 * 该队列又必须尽可能的快和高效<br/>
 * 思想：
 * 	主队列用Vector实现，为了不使其长度太大，它保存的是有固定长度的数组。
 */
public class QuickQueue
{
	private static int ITEMARRAYLEN = 100 ;
	
	Vector queueV = new Vector ();
	
	
	Object[] headerArray = null ;
	Object[] tailArray = null ;
	
	int headerPos,tailPos ;
	public QuickQueue ()
	{
		headerArray = tailArray = new Object [ITEMARRAYLEN] ;
		queueV.add (headerArray);
		headerPos = tailPos = 0 ;
	}
	
	public synchronized void put (byte[] buf)
	{
		tailArray [tailPos] = buf ;
		tailPos ++ ;
		if ((tailPos%ITEMARRAYLEN)==0)
		{
			tailPos = 0 ;
			tailArray = new Object [ITEMARRAYLEN] ;
			queueV.add (tailArray);
		}
		notifyAll();
		return ;
	}
	
	public synchronized byte[] get ()
	{
		byte[] tmpb = null ;
		if (headerArray!=tailArray)
		{
			tmpb = (byte[])headerArray [headerPos] ;
			headerPos ++ ;
			if ((headerPos%ITEMARRAYLEN)==0)
			{
				headerPos = 0 ;
				queueV.remove (0);
				headerArray = (Object[])queueV.elementAt (0);
			}
			return tmpb ;
		}
		else
		{
			if (headerPos==tailPos)
			{//empty
				headerPos = tailPos = 0 ;
				try
				{
					//System.out.println("waiting...");
					wait ();
				}
				catch(InterruptedException ie)
				{ }
			}
			tmpb = (byte[])headerArray [headerPos] ;
			headerPos ++ ;
			return tmpb ;
		}
	}
	/**
	 * 清空，队列中的所有内容
	 */
	public synchronized void emptyQueue ()
	{
		
	}
	
	public synchronized void listContain ()
	{
		int s = queueV.size () - 1;
		Object[] tmpo ; 
		int i , j ;
		System.out.println("***********");
		for (i = 0 ; i < s ; i ++)
		{
			System.out.println("--->");
			tmpo = (Object[])queueV.elementAt (i);
			for (j = 0 ; j < ITEMARRAYLEN ; j ++)
			{
				System.out.println(new String((byte[])tmpo[j]));
			}
			System.out.println("--->");
		}
		
		tmpo = (Object[])queueV.elementAt (s);
		for (i = headerPos ; i < tailPos ; i ++)
		{
			System.out.println(new String((byte[])tmpo[i]));
		}
		System.out.println("###########");
	}
	
	public static void main (String args[])
	{
		QuickQueue qq = new QuickQueue ();
		Getter g = new Getter (qq);
		Putter p = new Putter (qq,args[0]);
		g.start ();
		p.start ();
	}
}

class Getter extends Thread
{
	QuickQueue qq = null ;
	public Getter (QuickQueue qq)
	{
		this.qq = qq ;
	}
	
	public void run ()
	{
		while (true)
		{
			byte[] tmpb = qq.get();
			System.out.println("Getter get="+new String(tmpb));
		}
	}
}

class Putter extends Thread
{
	QuickQueue qq = null ;
	String info = null ;
	public Putter (QuickQueue qq,String info)
	{
		this.qq = qq ;
		this.info = info ;
	}
	
	public void run ()
	{
		int count = 0 ;
		while (true)
		{
			for (int i = 0 ; i < 5 ; i ++)
			{
				System.out.println("Putter put="+info+count);
				qq.put((info+count).getBytes());
				count ++ ;
			}
			//qq.listContain ();
			try
			{
				sleep (1000);
			}
			catch(Exception e){}
		}
	}
}