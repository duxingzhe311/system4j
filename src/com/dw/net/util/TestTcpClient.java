package com.dw.net.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

import com.dw.system.Convert;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;

public class TestTcpClient implements ITestConnListener
{

	static ILogger log = LoggerManager.getLogger("MsgCmdClient");

    String host = null;
    int port = TestTcpServer.DEFAULT_PORT;

    Socket client = null;

    //InputStream inputStream = null;
   // OutputStream outputStream = null ;
    
   // String userName = null ;
   // String passwd = null ;
    
    
    TestConnForClient cfc = null ; 
    
    public TestTcpClient(String host)
    {
    	this(host,TestTcpServer.DEFAULT_PORT);
    }

    public TestTcpClient(String host, int p)
    {
        this.host = host;
        port = p;
    }
    
//    public TestTcpClient(String host,String usern,String psw)
//    {
//    	this(host,TestTcpServer.DEFAULT_PORT);
//    	
//    	userName = usern;
//    	passwd = psw ;
//    }
//    
//    public TestTcpClient(String host,int p,String usern,String psw)
//    {
//    	this(host,p);
//    	
//    	userName = usern;
//    	passwd = psw ;
//    }
    
    public String getHost()
    {
    	return host;
    }
    
    public int getPort()
    {
    	return port ;
    }

    public boolean connect()
    	throws Exception
    {
        if (client != null)
            return true;

        try
        {
            client = new Socket(host, port);

            cfc = new TestConnForClient(client,this) ;
            
            cfc.start() ;
           
            return true;
        }
        catch (Exception e)
        {
            //if (log.IsErrorEnabled)
            //log.error(e);

            if (client != null)
            {
                client.close();
                client = null;
            }

            //出错的情况下，首先要确保连接的关闭，以避免占用资源
            //同时，必须抛出错误，以使连接池不会出现误判连接成功
            throw e;
        }
    }
    
    boolean bHexShow = false;

	public void onNetLineStrRecved(TestConnForClient conn, byte[] rdata, int dlen)
	{
		if(bHexShow)
		{
			System.out.println(Convert.byteArray2HexStr(rdata, 0, dlen)) ;
		}
		else
		{
			System.out.print(new String(rdata,0,dlen)) ;
		}
	}
    
   
    synchronized public void sendStrLine(String line)
		throws Exception
	{
	    if (client == null)
	    {
	    	if(!connect())
	    		return;
	    }
	    
	    cfc.sendStrLine(line) ;
	}
    
    
    public void send(byte[] bs)throws Exception
    {
    	if (client == null)
	    {
	    	if(!connect())
	    		return;
	    }
	    
	    cfc.send(bs) ;
    }
    
    public void close()
    {
    	if (client != null)
    	{
    		try
    		{
    			client.close();
    		}
    		catch(Exception e)
    		{
    			
    		}
    	}
    }
    
    public boolean isClosed()
    {
    	if(client==null)
    		return true ;
    	
    	return client.isClosed() ;
    }

    
    
    public static void main(String[] args) throws Exception
    {
    	String inputLine;
		BufferedReader in = new BufferedReader(
			new InputStreamReader(
			System.in));
		
		System.out.print(">") ;
		
		String[] lastCmds = null ;
		String cmds[] = null;
		
		TestTcpClient ps = null;//new ProxyServer(Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]));
		
		if(args.length>0)
			ps = new TestTcpClient(args[0],Integer.parseInt(args[1]));
		else
			ps = new TestTcpClient(args[0]);
		
		//ps.start() ;
		
		while ( (inputLine = in.readLine()) != null)
		{
			try
			{
				if("/".equals(inputLine))
				{//run last cmd
					if(lastCmds==null)
						continue ;
					cmds = lastCmds ;
				}
				else
				{
					StringTokenizer st = new StringTokenizer(inputLine, " ", false);
					cmds = new String[st.countTokens()];
					for (int i = 0; i < cmds.length; i++)
					{
						cmds[i] = st.nextToken();
					}
					if(cmds.length<=0)
						continue ;
					
					lastCmds = cmds ;
				}
				
				if ("conn".equals(cmds[0]))
				{
					//artMgr.start() ;
					ps.connect();
				}
				else if("hex".equals(cmds[0]))
				{//查看状态
					ps.bHexShow = "true".equalsIgnoreCase(cmds[1]) ||
						"on".equalsIgnoreCase(cmds[1]) ||
						"1".equalsIgnoreCase(cmds[1]) ;
					
					System.out.println("recv show in hex="+ps.bHexShow) ;
				}
				else if("send".equals(cmds[0]))
				{//查看状态
					ps.send(cmds[1].getBytes()) ;
				}
				else if("sendln".equals(cmds[0]))
				{//查看状态
					ps.sendStrLine(cmds[1]) ;
				}
				else if("send_hex".equals(cmds[0]))
				{//查看状态
					byte[] tmpbs = Convert.hexStr2ByteArray(cmds[1]) ;
					
					ps.send(tmpbs) ;
					
				}
				
				else if("exit".equals(cmds[0]))
				{
					ps.close();
					break;
				}
			}
			catch (Exception _e)
			{
				_e.printStackTrace();
			}
			finally
			{
				System.out.print(">") ;
			}
		}
    }
}
