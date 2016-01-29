<%request.setCharacterEncoding ("GBK") ;%>
<%@page import = 
	"com.css.system.* , com.css.util.* , java.util.* , com.css.lopt.*"  %><%

	com.css.right.util.RightsContext.register (session) ;
	
	OutputStream os = response.getOutputStream () ;
	InputStream in = request.getInputStream () ;
	try
	{
		Message msg = Message.unpack (in) ;
		
		// Log.getLog ("call.log").log ("-------->\n" + msg) ;
		
		in.close () ;
		try
		{
			Object obj = CallerServer.call (msg) ;
			msg.clear () ;
			msg.add (0) ;
			msg.add (obj) ;
	 	}
	 	catch (java.lang.reflect.InvocationTargetException _ite)
	 	{
	 		Throwable _t = _ite.getTargetException () ;
	 		Log.getLog ("call.log").log (_t) ;
	 		msg = new Message () ;
			msg.setEncoding ("GBK") ;
	 		msg.add (-1) ;
	 		msg.add ("[" + _t.getClass ().getName () + "] " + _t.getMessage ()) ;
	 	}
	 	catch (Throwable _ioe)
	 	{
	 		Log.getLog ("call.log").log (_ioe) ;
	 		msg = new Message () ;
			msg.setEncoding ("GBK") ;
	 		msg.add (-1) ;
	 		msg.add ("[" + _ioe.getClass ().getName () + "] " + _ioe.getMessage ()) ;
	 	}
	 	
	 	// Log.getLog ("call.log").log ("<--------\n" + msg) ;
	 	os.write (msg.pack ()) ;
	 	
	 	os.flush () ;
	}
	catch (Throwable _t)
	{
		Log.getLog ("call.log").log (_t) ;
	}
	 	
	os.close () ;
%>