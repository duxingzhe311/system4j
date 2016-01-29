package com.dw.net.proxy;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.*;
import java.util.*;

import org.w3c.dom.Element;

import com.dw.system.Convert;


//import com.dw.system.*;
//import com.dw.system.logger.ILogger;
//import com.dw.system.logger.LoggerManager;
//import com.dw.system.xmldata.*;

/**
 * AIS网络主节点，它接受罗干NetClient的链接请求，并通过请求接收NetClient的串口数据
 * 
 * @author Jason Zhu
 */
public class ProxyServer implements Runnable
{
	public static final int DEFAULT_PORT = 5600 ;
	
	//static ILogger log = LoggerManager.getLogger(ProxyServer.class);

	/**
	 * 本地ip，适合多个网卡及地址的机器
	 */
	String locIP = null ;
	
    int port = DEFAULT_PORT;

    ServerSocket server = null;
    Thread serverThread = null ;
    boolean bRun = false;
    
    static int MAX_CLIENT_NUM = 100 ;
    int CHECK_CLIENT_INTERVAL = 30000 ;//检测终端的时间间隔
    
    transient INetLineListener lineLis = null ;

    String tarHost = null ;
    int tarPort = 0 ;
    //IMCmdHandler handler = null;
    /**
     * 构造一个代理服务器。通过本地端口
     */
    public ProxyServer(String loc_ip,int port,String tarhost,int tarport)//, IMCmdHandler h
    {
    	this.locIP = loc_ip ;
        this.port = port;
        //handler = h;
        this.tarHost = tarhost ;
        this.tarPort = tarport ;
        
        try
    	{
    		//loadConfig() ;
    	}
    	catch(Exception ee)
    	{
    		ee.printStackTrace() ;
    	}
    }
    
    
    public void setConfig(String loc_ip,int port,String tarhost,int tarport)
    {
    	this.locIP = loc_ip ;
        this.port = port;
        //handler = h;
        this.tarHost = tarhost ;
        this.tarPort = tarport ;
    }
    
    public ProxyServer(int port,String tarhost,int tarport)//, IMCmdHandler h
    {
    	this(null,port,tarhost,tarport);
    }
    
    public ProxyServer(String tarhost,int tarport)//IMCmdHandler h
    {
    	this(DEFAULT_PORT,tarhost,tarport);
    }
    
    
    ProxyServer()
    {}
    
    public void setLineListener(INetLineListener lis)
    {
    	lineLis = lis ;
    }
    
    public int getConfigPort()
    {
    	return port ;
    }
    
    public static int getConfMaxClientN()
    {
    	return MAX_CLIENT_NUM ;
    }
    
    /**
     * 得到当前客户端连接数
     * @return
     */
    public int getClientConnCount()
    {
    	return ProxyConnForClient.getClientConnCount();
    }
    
    
    synchronized public void start()
    {
        if(serverThread!=null)
                return ;

        bRun = true ;
        serverThread = new Thread(this,"proxy_net_server") ;
        serverThread.start() ;
        
        
        
    }

    synchronized public void stop()
    {
    	if (server != null)
        {
        	try
        	{
        		server.close();
        	}
        	catch(Exception e)
        	{}
        	
        	server = null ;
        }
        
    	Thread st = serverThread;
    	// t = serverScheTh;
    	
    	if(st!=null)
    	{
    		st.interrupt() ;
    	}
    	
    	ProxyConnForClient.closeAllClients();
    	
        bRun = false;
        //serverScheTh = null ;
        serverThread = null ;
    }

    public void run()
    {
        try
        {
            //IPAddress.
            //IPAddress localAddr = IPAddress.Parse("127.0.0.1");
        	if(Convert.isNullOrEmpty(this.locIP))
        		server = new ServerSocket(port,100);
        	else
        	{
        		InetAddress addr = Inet4Address.getByName(locIP) ;
        		server = new ServerSocket(port,0,addr);
        	}
            //server..Start();
            // Enter the listening loop.
            System.out.println("Proxy Server started..<<<<<.,ready to recv client connection on port="+port);
            while (bRun)
            {
                Socket client = server.accept() ;

                ProxyConnForClient sfc = new ProxyConnForClient(client,this);
                sfc.start();
            }
        }
        catch (Exception e)
        {
        	e.printStackTrace();
            //if (log.IsErrorEnabled)
                //log.error(e);
        }
        finally
        {
            // Stop listening for new clients.
        	close();

        	System.out.println("MCmd Server stoped..") ;
            serverThread = null ;
            //server = null;
            bRun = false;
        }
    }
    
    
    public InetSocketAddress findTarAddr(Socket sor,InputStream sor_ins,OutputStream sor_outs)
    {
    	//InetAddress ina = new InetAddress() ;
    	return new InetSocketAddress(tarHost,tarPort) ;
    }
    
    public String getTarHost()
    {
    	return tarHost ;
    }
    
    public int getTarPort()
    {
    	return tarPort;
    }


//    MsgCmd DoCmd(MsgCmd mc)
//    {
//        return handler.OnCmd(mc);
//    }

    
    public void close()
    {
        stop() ;
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
		
		ProxyServer ps = null;//new ProxyServer(Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]));
		
		if(args.length>0)
			ps = new ProxyServer(Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]));
		else
			ps = new ProxyServer(50000,"localhost",60000);
		
		ps.start() ;
		
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
				
				if ("start".equals(cmds[0]))
				{
					//artMgr.start() ;
					ps.start() ;
				}
				else if("st".equals(cmds[0]))
				{//查看状态
					
				}
				else if("ls".equals(cmds[0]))
				{
					for(ProxyConnForClient fc:ProxyConnForClient.getAllClients())
					{
						System.out.println(fc) ;
					}
				}
				else if("exit".equals(cmds[0]))
				{
					ps.stop() ;
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