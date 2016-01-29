package com.dw.net.proxy;

import java.io.*;
import java.net.*;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.queue.HandleResult;
import com.dw.system.queue.IObjHandler;
import com.dw.system.queue.QueueThread;

/**
 * 代理指向被访问目标的socket client
 * 
 * @author Jason Zhu
 */
public class ProxyTarClient
{
	static ILogger log = LoggerManager.getLogger(ProxyTarClient.class);

    String host = null;
    int port = -1;

    Socket client = null;

    InputStream inputStream = null;
    OutputStream outputStream = null ;
   
    
    
    //Thread runST = null ;
    
    /**
     * 用来支持异步发送数据的队列
     */
    QueueThread queTh = null ;
    
    int queLenMax = 100 ;
    
    
    transient long sentLineCount = 0 ;
    transient long sentErrCount = 0 ;
    
    public ProxyTarClient(String host, int p,int quelen)
    {
        this.host = host;
        port = p;
        this.queLenMax = quelen ;
        
        //Process p = 
    }
    
    
    public String getHost()
    {
    	return host;
    }
    
    public int getPort()
    {
    	return port ;
    }
    
    public int getMaxQueLen()
    {
    	return queLenMax ;
    }
    /**
     * 获得当前队列长度
     * @return
     */
    public int getCurQueLen()
    {
    	if(queTh==null)
    		return -1 ;
    	
    	return queTh.size() ; 
    }
    
    /**
     * 判断是否连接成功
     * @return
     */
    public boolean isConnected()
    {
    	return outputStream!=null ;
    }
    
    public long getSentLineCount()
    {
    	return this.sentLineCount ;
    }
    
    public long getSentErrCount()
    {
    	return sentErrCount ;
    }
    
    public void clearSentLineCount()
    {
    	sentLineCount = 0 ;
    	sentErrCount = 0;
    }
    
    IObjHandler queObjHandler = new IObjHandler(){

		public int processFailedRetryTimes()
		{
			return 0;
		}

		public long processRetryDelay(int retrytime)
		{
			return 0;
		}

		public HandleResult processObj(Object o, int retrytime) throws Exception
		{
			try
			{
			//检查是否连接，如果没连接，则自动做链接
				if(outputStream==null)
				{
					connect() ;
					if(outputStream==null)
					{//说明尝试连接失败
						sentErrCount ++ ;
						return HandleResult.Succ;//.Failed_Retry_Later;
					}
				}
				
				//do send
				byte[] bs = ((String)o).getBytes() ;
		    	outputStream.write(bs);
		    	
		    	outputStream.flush() ;
		    	
		    	sentLineCount ++ ;
				//
		    	return HandleResult.Succ;
			}
			catch(Exception ee)
			{//发送失败，可能需要关闭连接，并再下次尝试重新连接服务器
				disconnect() ;
				
				if(log.isDebugEnabled())
				{
					log.error(ee) ;
				}
				
				sentErrCount ++ ;
				return HandleResult.Succ;//确保队列不要保留旧内容
			}
		}

		public long handlerInvalidWait()
		{
			return 0;
		}

		public void processObjDiscard(Object o) throws Exception
		{
			
		}} ;
    /**
     * 启动此终端，通过启动线程方式，可以保证网络传输失败后，支持
     * 重新自动尝试连接的功能
     */
    public synchronized void start()
    {
    	if(queTh!=null)
    		return ;
    	
    	queTh = new QueueThread(queObjHandler) ;
    	queTh.start() ;
    	
    	System.out.println("net client to tar "+this.host+":"+this.port+" started!") ;
//    	runST = new Thread(this) ;
//        runST.start() ;
    }
    
    public synchronized void stop()
    {
    	QueueThread qt = queTh ;
    	if(qt!=null)
    	{
    		qt.stop() ;
    		queTh = null ;
    	}
    	
//    	Thread t = runST ;
//    	if(t!=null)
//    	{
//    		t.interrupt() ;
//    		runST = null ;
//    	}
    }

    boolean connect()
    {
        if (client != null)
            return true;

        try
        {
        	//System.out.print("Connecting to Server "+host+":"+port) ;
            client = new Socket(host, port);

            // Get a stream object for reading and writing
            inputStream = client.getInputStream();
            outputStream = client.getOutputStream();
            
            System.out.println("Connecting to Server "+host+":"+port+" succ!") ;
            
            return true;
        }
        catch (Exception e)
        {
        	disconnect();

            //出错的情况下，首先要确保连接的关闭，以避免占用资源
            //同时，必须抛出错误，以使连接池不会出现误判连接成功
            //throw e;
        	//System.out.println(" failed!") ;
        	return false;
        }
    }
    
    void disconnect()
    {
    	if (client != null)
    	{
    		try
    		{
	    		client.shutdownInput();
	    		client.shutdownOutput();
    		}
    		catch(Exception e){}
    	}
    	
    	if(inputStream!=null)
    	{
    		try
    		{
    			inputStream.close() ;
    			inputStream = null ;
    		}
    		catch(Exception e){}
    	}
    	
    	if(outputStream!=null)
    	{
    		try
    		{
    			outputStream.close() ;
    			outputStream = null ;
    		}
    		catch(Exception e){}
    	}
        
        
    	if (client != null)
    	{
    		try
    		{
    			client.close();
    			client = null;
    		}
    		catch(Exception e)
    		{
    			
    		}
    	}
    }
    /**
     * 发送数据到NetServer
     * 为了防止此发送受到网络速度影响，进而影响数据接收（本地Com，NetServer）
     * 此方法，直接把数据扔到队列中，由队列另一头的线程处理
     * @param line
     * @throws IOException
     */
//    public boolean sendStrToServer(String line)
//    {
//    	if(queTh==null)
//    		return false ;
//    	
//    	if(queTh.size()>queLenMax)
//    	{
//    		return false;
//    	}
//    	queTh.enqueue(line) ;
//    	return true ;
//    }
    
    public boolean sendLineToServer(String line)
    {
    	if(queTh==null)
    		return false;
    	
    	if(queTh.size()>queLenMax)
    	{
    		return false;
    	}
    	queTh.enqueue(line+"\r\n") ;
    	return true ;
    }
    
//    public void run()
//	{
//		try
//		{
//			while(runST!=null)
//			{
//				try
//		        {
//		        	long st = System.currentTimeMillis();
//		        }
//		        catch(Exception ioe)
//		        {
//		        	
//		        }
//			}
//		}
//		finally
//		{
//			close();
//		}
//	}


    
    public void close()
    {
    	stop();
    	
    	disconnect() ;
//    	Thread t = runST ;
//    	if(t!=null)
//    	{
//    		try
//    		{
//    			t.interrupt() ;
//    			runST = null ;
//    		}
//    		catch(Exception ee)
//    		{}
//    	}
    	
    	
    }
    
    public boolean isClosed()
    {
    	if(client==null)
    		return true ;
    	
    	return client.isClosed() ;
    }
}
