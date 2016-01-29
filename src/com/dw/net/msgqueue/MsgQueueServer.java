package com.dw.net.msgqueue;

import java.io.*;
import java.util.*;

import com.dw.net.*;
import com.dw.system.*;

import com.dw.net.msgqueue.io.*;

public class MsgQueueServer implements IMsgCmdHandler
{
    static Object GetInsLock = new Object();

    static MsgQueueServer instance = null;

    public static MsgQueueServer getInstance()
    {
        if (instance != null)
            return instance;

        synchronized (GetInsLock)
        {
            if (instance != null)
                return instance;

            instance = new MsgQueueServer();
            return instance;
        }
    }
    
    String dirBase = null;

    MsgCmdServer msgCmdServer = null;

    IMsgQueueIO queIO = null;
    
    private MsgQueueServer()
    {
    	dirBase = Configuration.getProperty("msg_queue_server_dirbase");
        
        if(dirBase==null||dirBase=="")
            dirBase = "./MsgQueue";

        queIO = new FileDirMsgQueueIO(dirBase);

        String portstr = Configuration.getProperty("msg_queue_server_port");
        if (portstr==null||portstr=="")
        {
            msgCmdServer = new MsgCmdServer(this);
        }
        else
        {
            int p = Integer.parseInt(portstr);
            msgCmdServer = new MsgCmdServer(p,this);
        }
        msgCmdServer.Start();
    }

    public MsgCmd OnCmd(MsgCmd mc,MsgCmdClientInfo ci)
    {
        String cmd = mc.getCmd();

        String failedreson = null;

        if (cmd.startsWith("msgque_getmsg "))
        {
            String quename = cmd.substring("msgque_getmsg ".length());
            
            try
            {
            	byte[] cont = queIO.getMsgFromServer(quename);
            	return new MsgCmd("msgque_ok", cont);
            }
            catch(Exception e)
            {
            	failedreson = e.getMessage();
            }
        }
        else if (cmd.startsWith("msgque_addmsg "))
        {
            String quename = cmd.substring("msgque_addmsg ".length());
            
            try
            {
	            queIO.addMsgToServer(quename, mc.getContent());
	            return new MsgCmd("msgque_ok", null);
            }
            catch(Exception e)
            {
            	failedreson = e.getMessage();
            }
        }

        byte[] fdcont = null;
        
        try
        {
        	fdcont = failedreson.getBytes("UTF-8") ;
        }
        catch(Exception ee)
        {}
        
        if (failedreson == null)
            return new MsgCmd("msgque_err", null);
        else
            return new MsgCmd("msgque_err", fdcont);
    }

    public static void main(String[] args)
    {
    	MsgQueueServer.getInstance();
    }

	public boolean checkConnRight(String username, String psw)
	{
		return true;
	}
}