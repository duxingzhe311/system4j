package com.dw.net.msgqueue;

import java.io.*;
import java.util.*;
import com.dw.net.*;
import com.dw.system.*;

public class MsgQueueClient
{
    static String serverHost = null;
    static int serverPort = -1;

    static
    {
    	serverHost = Configuration.getProperty("msg_queue_server");

        if (serverHost==null||serverHost=="")
            serverHost = "127.0.0.1";

        String tmps = Configuration.getProperty("msg_queue_server_port");
        if (tmps!=null&&tmps!="")
            serverPort = Integer.parseInt(tmps);
    }

    MsgCmdClient mcc = null;

    public MsgQueueClient()
    	throws Exception
    {
        if(serverPort>0)
            mcc = new MsgCmdClient(serverHost, serverPort);
        else
            mcc = new MsgCmdClient(serverHost);
        
        mcc.connect();
    }


    /// <summary>
    /// 没有通过配置使用的方法
    /// </summary>
    /// <param name="host"></param>
    public MsgQueueClient(String host)
    	throws Exception
    {
        mcc = new MsgCmdClient(host);
        mcc.connect();
    }

    /// <summary>
    /// 没有通过配置使用的方法
    /// </summary>
    /// <param name="host"></param>
    /// <param name="port"></param>
    public MsgQueueClient(String host, int port)
    	throws Exception
    {
        mcc = new MsgCmdClient(host, port);
        mcc.connect();
    }

    public boolean addMsg(String quename, byte[] msgcont)
    	throws Exception
    {
        MsgCmd mc = new MsgCmd("msgque_addmsg "+quename,msgcont) ;
        mc = mcc.sendCmd(mc);
        if (mc == null)
            return false;

        String replycmd = mc.getCmd();
        if (replycmd.equals("msgque_ok"))
            return true;
        return false;
    }

    public boolean addMsgPacker(String quename, MsgPacker mp)
    	throws Exception
    {
        return addMsg(quename, mp.toBytes());
    }

    public byte[] getMsg(String quename)
    	throws Exception
    {
        MsgCmd mc = new MsgCmd("msgque_getmsg " + quename, null);
        mc = mcc.sendCmd(mc);
        if (mc == null)
        {
            return null ;
        }
        String replycmd = mc.getCmd();
        if (replycmd.equals("msgque_ok"))
        {
            return mc.getContent();
        }

        return null ;
    }

    public MsgPacker getMsgPacker(String quename)
    	throws Exception
    {
        byte[] cont =getMsg(quename) ; ;
        if(cont==null)
            return null;

        if (cont == null)
            return null;

        return MsgPacker.parseFrom(cont);
    }

    public void close()
    {
        if (mcc != null)
            mcc.close();
    }
    
	public static void main(String args[])
	{
		MsgQueueClient mqc = null ;
		try
		{
			mqc = new MsgQueueClient();
			
			String inputLine;
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
				System.in));

			while ((inputLine = in.readLine()) != null)
			{
				try
				{
					StringTokenizer st = new StringTokenizer(inputLine, " ", false);
					String cmds[] = new String[st.countTokens()];
					for (int i = 0; i < cmds.length; i++)
					{
						cmds[i] = st.nextToken();
					}
					if ("addmsg".equals(cmds[0]))
					{
						mqc.addMsg("java_test",cmds[1].getBytes());
					}
					else if ("getmsg".equals(cmds[0]))
					{
						byte[] tmpb = mqc.getMsg("java_test") ;
						if(tmpb!=null)
						{
							System.out.println(new String(tmpb));
						}
						else
						{
							System.out.println("get null");
						}
					}
					else if ("removetopic".equals(cmds[0]))
					{
						
					}
				}
				catch (Exception _e)
				{
					_e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			mqc.close();
		}
	}
}
