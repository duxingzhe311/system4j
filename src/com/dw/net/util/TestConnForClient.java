package com.dw.net.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;



public class TestConnForClient implements Runnable
{
	//static ILogger log = LoggerManager.getLogger(ProxyConnForClient.class);

    static Object lockObj = new Object();
    static ArrayList<TestConnForClient> ALL_CLIENTS = new ArrayList<TestConnForClient>() ;
    
    static void increaseCount(TestConnForClient c)
    {
    	synchronized(lockObj)
    	{
    		ALL_CLIENTS.add(c) ;
    	}
    }
    
    static void decreaseCount(TestConnForClient c)
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
    
    public static TestConnForClient[] getAllClients()
    {
    	synchronized(lockObj)
    	{
    		TestConnForClient[] rets = new TestConnForClient[ALL_CLIENTS.size()];
    		ALL_CLIENTS.toArray(rets);
    		return rets;
    	}
    }
    
    
    static void closeAllClients()
    {
    	for(TestConnForClient pcf:getAllClients())
    		pcf.close() ;
    }
    
    public static List<TestConnForClient> getAllClientsList()
    {
    	synchronized(lockObj)
    	{
	    	List<TestConnForClient> rets = new ArrayList<TestConnForClient>() ;
	    	rets.addAll(ALL_CLIENTS);
	    	return rets ;
    	}
    }
    
    
    public static TestConnForClient getClientById(String clientid)
    {
    	for(TestConnForClient c:ALL_CLIENTS)
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
   
    ITestConnListener belongTo = null ;
    //MsgCmdServer server = null;
    //IMCmdHandler cmdHandler = null ;
    Socket sorSocket = null;
    
    InputStream sorInstream = null ;
    OutputStream sorOutstream = null ;
    
    Thread sorRecvTh = null;
    
    transient InetAddress sorClientAddr = null ;
    transient int sorClientPort = -1 ;
    
    /**
     * 命令连续出错次数
     */
    transient int cmdErrorNum = 0 ;

    //transient boolean bCmdRun = false;
    transient long lineCount = 0 ;

    public TestConnForClient(Socket sortcp,ITestConnListener belongto) throws Exception//IMCmdHandler cmdhandler,
    {
    	//cmdHandler = cmdhandler ;
        sorSocket = sortcp;
        
        belongTo = belongto ;
        
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
    
    public ITestConnListener getBelongTo()
    {
    	return this.belongTo ;
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
    
    public void send(byte[] bs) throws IOException
    {
    	sorOutstream.write(bs) ;
    	sorOutstream.flush() ;
    }
    
    
    static byte[] LRLN = "\r\n".getBytes() ;

    synchronized public void sendStrLine(String line)
    	throws Exception
    {
    	//if(this.)
        try
        {
        	long st = System.currentTimeMillis();
        	sorOutstream.write(line.getBytes()) ;
        	sorOutstream.write(LRLN) ;
	        long et = System.currentTimeMillis();
	        sorOutstream.flush() ;
	        
        }
        catch(SocketException se)
        {
        	se.printStackTrace();
        	
        	sorOutstream.close();
        	sorInstream.close();
        	if (sorSocket != null)
        	{
        		sorSocket.close() ;
        		sorSocket = null ;
        	}
        	return  ;
        }
    }


    public void run()
    {
        try
        {
        	//可能需要根据验证信息，查找相关的目标地址，并进行连接
        	System.out.print("conn ready "+sorClientAddr+":"+sorClientPort) ;
        	
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
        		if(rbytes>0 && this.belongTo!=null)
        		{//转发
        			this.belongTo.onNetLineStrRecved(this, readBuf,rbytes);
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
    	//Thread tth = tarRecvTh ;
    	Thread sth = sorRecvTh ;
    	
    	if(sth!=null)
    	{
    		sth.interrupt() ;
    		sorRecvTh = null ;
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
    	
    	return ret ;
    }
    
}
