package com.dw.system.util ;

import java.util.* ;

public class MessageQueue extends Hashtable 
{
	public synchronized Object put (Object id , Object v)
	{
		Object obj = super.put (id , v) ;
		notifyAll () ;
		return obj ;
	}
	
	public synchronized Object get (Object id)
	{
		while (true)
		{
			if (containsKey (id))
				return remove (id) ;
			
			try
			{
				wait () ;
			}
			catch (Throwable _t)
			{
				_t.printStackTrace () ;
			}
		}
	}
} 