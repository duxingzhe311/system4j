package com.dw.net.proxy;

import java.io.*;
import java.net.*;
import java.util.*;

//import com.dw.system.*;
//import com.dw.system.logger.ILogger;
//import com.dw.system.logger.LoggerManager;

/**
 * 
 */
public class ProxyConnForClient implements Runnable
{
	//static ILogger log = LoggerManager.getLogger(ProxyConnForClient.class);

    static Object lockObj = new Object();
    static ArrayList<ProxyConnForClient> ALL_CLIENTS = new ArrayList<ProxyConnForClient>() ;
    
    static void increaseCount(ProxyConnForClient c)
    {
    	synchronized(lockObj)
    	{
    		ALL_CLIENTS.add(c) ;
    	}
    }
    
    static void decreaseCount(ProxyConnForClient c)
    {
    	//System.out.println("client disconn="+c) ;
    	synchronized(lockObj)
    	{
    		ALL_CLIENTS.remove(c);
    	}
    }
    
    
    public static int getClientConnCount()
    {
    	return ALL_CLIENTS.size() ;
    }
    
    public static ProxyConnForClient[] getAllClients()
    {
    	synchronized(lockObj)
    	{
    		ProxyConnForClient[] rets = new ProxyConnForClient[ALL_CLIENTS.size()];
    		ALL_CLIENTS.toArray(rets);
    		return rets;
    	}
    }
    
    
    static void closeAllClients()
    {
    	for(ProxyConnForClient pcf:getAllClients())
    		pcf.close() ;
    }
    
    public static List<ProxyConnForClient> getAllClientsList()
    {
    	synchronized(lockObj)
    	{
	    	List<ProxyConnForClient> rets = new ArrayList<ProxyConnForClient>() ;
	    	rets.addAll(ALL_CLIENTS);
	    	return rets ;
    	}
    }
    
    
    public static ProxyConnForClient getClientById(String clientid)
    {
    	for(ProxyConnForClient c:ALL_CLIENTS)
    	{
    		//if(clientid.equals(c.getClientInfo().devId))
    		//	return c ;
    	}
    	return null ;
    }
    
    static long TMP_ID_C = 0 ;
    
    synchronized static long newTmpId()
    {
    	TMP_ID_C++ ;
    	return TMP_ID_C ;
    }
   
    ProxyServer belongToServer = null ;
    //MsgCmdServer server = null;
    //IMCmdHandler cmdHandler = null ;
    Socket sorSocket = null;
    
    InputStream sorInstream = null ;
    OutputStream sorOutstream = null ;
    
    Socket tarSocket = null ;
    InputStream tarInstream = null ;
    OutputStream tarOutstream = null ;

    Thread sorRecvTh = null;
    
    Thread tarRecvTh = null;
    //boolean bRun = false;
    
    transient InetAddress sorClientAddr = null ;
    transient int sorClientPort = -1 ;
    
    /**
     * 命令连续出错次数
     */
    transient int cmdErrorNum = 0 ;

    //transient boolean bCmdRun = false;
    transient long lineCount = 0 ;

    public ProxyConnForClient(Socket sortcp,ProxyServer belongto) throws Exception//IMCmdHandler cmdhandler,
    {
    	//cmdHandler = cmdhandler ;
        sorSocket = sortcp;
        
        belongToServer = belongto ;
        
//        clientInfo = new MCmdClientInfo(
//        		tcp.getInetAddress().getHostAddress(),
//        		tcp.getPort(),null
//        		);
        
        increaseCount(this);
    	sorSocket.setSoTimeout(60000) ;
    	sorSocket.setTcpNoDelay(true);
        // Get a stream object for reading and writing
        sorInstream = sorSocket.getInputStream();
        sorOutstream = sorSocket.getOutputStream();
        
        sorClientAddr = sorSocket.getInetAddress();
        sorClientPort = sorSocket.getPort() ;
        
        
        Socket client = null;

        InputStream inputStream = null;
        OutputStream outputStream = null ;
       
        
    }
    
    public ProxyServer getBelongToServer()
    {
    	return this.belongToServer ;
    }
    
    public InetAddress getClientAddr()
    {
    	return sorClientAddr ;
    }
    
    public int getClientPort()
    {
    	return sorClientPort ;
    }
    
    public String getClientInfo()
    {
    	return sorClientAddr.getHostAddress()+":"+sorClientPort ;
    }
    
    public long getRecvedLineCount()
    {
    	return lineCount ;
    }
    
    public void clearLineCount()
    {
    	lineCount = 0 ;
    }

    synchronized public void start()
    {
        if (sorRecvTh != null)
                return;

        sorRecvTh = new Thread(this);//,"NetConnForClient");
        sorRecvTh.start();
    }

    
    public boolean isCmdRunning()
    {
    	return sorRecvTh!=null;
    }
    
    Runnable tarRecvRunner = new Runnable()
    {
		public void run()
		{
			int rbytes ;
			byte[] readBuf = new byte[1024] ;
			
			try
			{
				while(tarRecvTh!=null)
				{
					rbytes = tarInstream.read(readBuf) ;
	        		if(rbytes>0)
	        		{//转发
	        			sorOutstream.write(readBuf, 0, rbytes) ;
	        			sorOutstream.flush() ;
	        		}
				}
			}
			catch(Exception e)
			{
				//log.error("tar err:"+e.getMessage());
				System.out.println("tar err:"+e.getMessage());
			}
			finally
			{
				close() ;
			}
		}};
    

    public void run()
    {
        try
        {
        	//可能需要根据验证信息，查找相关的目标地址，并进行连接
//        	System.out.print("Connecting to Server "+host+":"+port) ;
        	//InetSocketAddress taraddr = this.belongToServer.findTarAddr(sorSocket, sorInstream, tarOutstream) ;
            //tarSocket = new Socket(taraddr.getHostName(), taraddr.getPort());
            tarSocket = new Socket(this.belongToServer.getTarHost(),this.belongToServer.getTarPort());

            
            tarInstream = tarSocket.getInputStream();
            tarOutstream = tarSocket.getOutputStream();
            
            tarRecvTh = new Thread(tarRecvRunner) ;
            tarRecvTh.start() ;
            
        	/**
        	 * 临时存放当前行
        	 */
        	StringBuilder curLine = new StringBuilder() ;
        	byte[] readBuf = new byte[1024] ;
        	
        	int rbytes = 0 ;
        	//InputStreamReader isr = new InputStreamReader(instream,"US-ASCII") ;
        	//BufferedReader br = new BufferedReader(isr) ;
        	//String line =null;
//    		while ((line = br.readLine()) != null)
//    		{
//				if (!"".equals((line = line.trim())))
//				{
//					//System.out.println("["+line+"]") ;
//					lineCount ++ ;
//					if(this.belongToServer.lineLis!=null)
//						this.belongToServer.lineLis.onNetLineStrRecved(this, line);
//				}
//    		}
        	
        	while(sorRecvTh!=null)
        	{
        		rbytes = 0 ;
        		//if(sorInstream.available()>0)
        		//{
        		rbytes = sorInstream.read(readBuf) ;
        		if(rbytes>0)
        		{//转发
        			tarOutstream.write(readBuf, 0, rbytes) ;
        			tarOutstream.flush() ;
        		}
        		//}
        		
        		
//        		if(rbytes==0)
//        		{
//        			tarSocket.sendUrgentData(0xFF);
//        			sorSocket.sendUrgentData(0xFF);
////        			if(!sorSocket.isConnected())
////        				break ;
////        			if(!tarSocket.getChannel().isConnected())
////        				break ;
//        			Thread.sleep(3) ;//sleep
//        		}
        	}
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            //log.error("sor err:"+e.getMessage());
        }
        finally
        {
        	close() ;
        }
        
    }
    
    public void close()
    {
    	Thread tth = tarRecvTh ;
    	Thread sth = sorRecvTh ;
    	if(tth!=null)
    	{
    		tth.interrupt() ;
    		tarRecvTh = null ;
    	}
    	if(sth!=null)
    	{
    		sth.interrupt() ;
    		sorRecvTh = null ;
    	}
    	
    	if (tarInstream != null)
        {
        	try
        	{
        		tarInstream.close();
        		tarInstream = null;
        	}
        	catch(Exception e)
        	{}
        }
    	
    	if (tarOutstream != null)
        {
        	try
        	{
        		tarOutstream.close();
        		tarOutstream = null;
        	}
        	catch(Exception e)
        	{}
        }
            
        if (tarSocket != null)
        {
        	try
        	{
	            tarSocket.close();
	            tarSocket = null;
        	}
        	catch(Exception e)
        	{}
        }
        
    	if (sorInstream != null)
        {
        	try
        	{
        		sorInstream.close();
        		sorInstream = null;
        	}
        	catch(Exception e)
        	{}
        }
    	
    	if (sorOutstream != null)
        {
        	try
        	{
        		sorOutstream.close();
        		sorOutstream = null;
        	}
        	catch(Exception e)
        	{}
        }
            
        if (sorSocket != null)
        {
        	try
        	{
	            sorSocket.close();
	            sorSocket = null;
        	}
        	catch(Exception e)
        	{}
        }
        
        decreaseCount(this);
    }

    public String toString()
    {
    	String ret = "sor=" ;
    	if(this.sorSocket!=null)
    	{
    		SocketAddress sa = this.sorSocket.getRemoteSocketAddress() ;
    		ret += sa.toString() ;
    	}
    	ret += " tar=" ;
    	if(this.tarSocket!=null)
    	{
    		SocketAddress sa = this.tarSocket.getRemoteSocketAddress() ;
    		ret += sa.toString() ;
    	}
    	return ret ;
    }
    
}
