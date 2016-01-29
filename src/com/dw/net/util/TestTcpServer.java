package com.dw.net.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;


import com.dw.system.Convert;

/**
 * 命令行tcp服务器
 * @author jasonzhu
 *
 */
public class TestTcpServer implements Runnable,ITestConnListener
{
	public static final int DEFAULT_PORT = 12000 ;
	
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
    
    /**
     * 构造一个代理服务器。通过本地端口
     */
    public TestTcpServer(String loc_ip,int port)//, IMCmdHandler h
    {
    	this.locIP = loc_ip ;
        this.port = port;
        
        
        try
    	{
    		//loadConfig() ;
    	}
    	catch(Exception ee)
    	{
    		ee.printStackTrace() ;
    	}
    }
    
    
    public void setConfig(String loc_ip,int port)
    {
    	this.locIP = loc_ip ;
        this.port = port;
        
    }
    
    public TestTcpServer(int port)//, IMCmdHandler h
    {
    	this(null,port);
    }
    
    public TestTcpServer()//IMCmdHandler h
    {
    	this(DEFAULT_PORT);
    }
    
//    public void setLineListener(ITestConnListener lis)
//    {
//    	handler = lis ;
//    }
    
    
    
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
    	return TestConnForClient.getClientConnCount();
    }
    
    
    synchronized public void start()
    {
        if(serverThread!=null)
                return ;

        bRun = true ;
        serverThread = new Thread(this,"test_net_server") ;
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
    	
    	TestConnForClient.closeAllClients();
    	
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

                TestConnForClient sfc = new TestConnForClient(client,this);
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
    
    


//    MsgCmd DoCmd(MsgCmd mc)
//    {
//        return handler.OnCmd(mc);
//    }

    
    public void close()
    {
        stop() ;
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
    
    
//    
//    static ITestConnListener tcLis = new ITestConnListener(){
//
//		public void onNetLineStrRecved(TestConnForClient conn, byte[] rdata, int dlen)
//		{
//			
//		}};
    
    
    public static void main(String[] args) throws Exception
    {
    	String inputLine;
		BufferedReader in = new BufferedReader(
			new InputStreamReader(
			System.in));
		
		System.out.print(">") ;
		
		String[] lastCmds = null ;
		String cmds[] = null;
		
		TestTcpServer ps = null;//new ProxyServer(Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]));
		
		if(args.length>0)
			ps = new TestTcpServer(Integer.parseInt(args[0]));
		else
			ps = new TestTcpServer(DEFAULT_PORT);
		
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
				else if("hex".equals(cmds[0]))
				{//查看状态
					ps.bHexShow = "true".equalsIgnoreCase(cmds[1]) ||
						"on".equalsIgnoreCase(cmds[1]) ||
						"1".equalsIgnoreCase(cmds[1]) ;
					
					System.out.println("recv show in hex="+ps.bHexShow) ;
				}
				else if("send".equals(cmds[0]))
				{//查看状态
					for(TestConnForClient fc:TestConnForClient.getAllClients())
					{
						fc.send(cmds[1].getBytes()) ;
					}
				}
				else if("sendln".equals(cmds[0]))
				{//查看状态
					for(TestConnForClient fc:TestConnForClient.getAllClients())
					{
						fc.sendStrLine(cmds[1]) ;
					}
					//fc.(cmds[1]) ;
				}
				else if("send_hex".equals(cmds[0]))
				{//查看状态
					byte[] tmpbs = Convert.hexStr2ByteArray(cmds[1]) ;
					for(TestConnForClient fc:TestConnForClient.getAllClients())
					{
						fc.send(tmpbs) ;
					}
				}
				else if("ls".equals(cmds[0]))
				{
					for(TestConnForClient fc:TestConnForClient.getAllClients())
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
