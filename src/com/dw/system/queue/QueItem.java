package com.dw.system.queue;

import java.util.LinkedList;

class QueItem implements Comparable<QueItem>
{
	Object objV = null ;
	/**
	 * 已经尝试次数
	 */
	int retryTime = 0 ;
	/**
	 * 开始处理时间
	 */
	long startProcT = -1 ;
	
	transient LinkedList<QueItem> belongToLL = null ;
	
	QueItem(Object ov)
	{
		objV = ov ;
	}
	
	void removeFromList()
	{
		belongToLL.remove(this) ;
	}

	public int compareTo(QueItem o)
	{
		long v = startProcT-o.startProcT ;
		if (v>0)
			return 1 ;
		if(v<0)
			return -1 ;
		return 0 ;
	}
}
