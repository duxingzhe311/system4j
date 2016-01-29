package com.dw.system.util.lopt ;

import java.net.* ;
import java.io.* ;
import com.dw.system.util.* ;


public class HttpCaller
{
	URL host ;
	
	public HttpCaller (URL url)
	{
		host = url ;
	}
	public HttpCaller (String url)
	{
		try
		{
			host = new URL (url) ;
		}
		catch (Throwable _t)
		{
			throw new RuntimeException ("Wrong url [" + host + "]") ;
		}
	}
	public Object call (String className , String methodName , Object [] args)
		throws JERuntimeException
	{
		// connect to server
		Message msg = new Message () ;
		
		msg.add (className) ;
		msg.add (methodName) ;
		msg.add (args) ;
		
		try
		{
			msg = sendRecv (msg) ;
		}
		catch (Throwable _ioe)
		{
			_ioe.printStackTrace () ;
			throw new JERuntimeException (_ioe , _ioe.getMessage ()) ;
		}
		
		// System.out.println ("<----------\n" + msg) ;

		switch (((Integer) msg.get (0)).intValue ())
		{
		case 0 :
			return msg.get (1) ;
		default :
			throw new JERuntimeException ("Server Error: " + msg.get (1)) ;
		}
	}

	public Message sendRecv (Message msg) throws Throwable
	{
		URLConnection conn = host.openConnection () ;
		
		conn.setDoOutput (true) ;
		
		// set Content-Type into oct8
		conn.setRequestProperty ("Content-type" , "application/octet-stream") ;
		conn.setRequestProperty ("Connection" , "Keep-Alive") ;
		
		OutputStream out = conn.getOutputStream () ;
		
		msg.setEncoding ("GBK") ;
		// msg.log () ;
		out.write (msg.pack ()) ;
		
		out.flush () ;
		out.close () ;
		
		
		conn.connect () ;
		
		InputStream in = conn.getInputStream () ;
		
		return Message.unpack (in) ;
	
		
		/*
	
		Object obj = CallerServer.call (Message.unpack (msg.pack ())) ;
		
		Message m = new Message () ;
		m.add (0) ;
		
		m.add (obj) ;
		
		// System.out.println ("caller....") ;
		return Message.unpack (m.pack ()) ;
		*/
	}
}