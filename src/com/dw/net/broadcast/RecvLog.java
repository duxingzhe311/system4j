package com.dw.net.broadcast ;

import java.io.*;
import java.net.*;
import java.util.*;

import com.dw.system.logger.*;



public class RecvLog implements UDPBaseCallback
{
	static boolean bPrint = false ;
	
	ILogger log = LoggerManager.getLogger("RecvLog.log");
	//Loggerlog = LoggerManager..getLog("RecvLog.log") ;
	
	UDPBase base = null ;
	
	public RecvLog()
	{
		base = UDPBase.getUDPBase() ;
		base.addTopic("*") ;
		base.setRecvCallback(this) ;
	}
	
	public void OnMsg (UDPBase base,String srctopic,byte[] infobuf)
	{
		if("MutexSyner.IAmHere".equals(srctopic))
			return ;
		if(bPrint)
			System.out.println("recved-----["+srctopic+"]["+infobuf.length+"]") ;
		log.info("recved-----["+srctopic+"]["+infobuf.length+"]") ;
	}
	
	public static void main(String[] args)
	{
		RecvLog rl = new RecvLog() ;
		/*
		try
		{
		 	String inputLine;
     		BufferedReader in = new BufferedReader(
     			new InputStreamReader(
     			System.in));
     		
     		while ((inputLine = in.readLine()) != null)
     		{
     			try
     			{
     				StringTokenizer st = new StringTokenizer (inputLine," ",false) ;
     				String cmds[] = new String[st.countTokens()] ;
     				for (int i = 0 ; i < cmds.length ; i ++)
     				     cmds[i] = st.nextToken () ;
     				
     				if ("p".equals(cmds[0]))
     				{
     				     bPrint = true ;
     				}
     				else if ("c".equals(cmds[0]))
     				{
     				     bPrint = false ;
     				}
     				
     		     }
     		     catch (Exception _e)
     		     {
     		          _e.printStackTrace () ;
     		     }
     		}
		}
		catch(Exception e)
		{
			e.printStackTrace ();
		}
		*/
	}
}