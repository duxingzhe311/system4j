package com.dw.net.proxy;

import java.io.*;
import java.net.*;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.queue.HandleResult;
import com.dw.system.queue.IObjHandler;
import com.dw.system.queue.QueueThread;

/**
 * ����ָ�򱻷���Ŀ���socket client
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
     * ����֧���첽�������ݵĶ���
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
     * ��õ�ǰ���г���
     * @return
     */
    public int getCurQueLen()
    {
    	if(queTh==null)
    		return -1 ;
    	
    	return queTh.size() ; 
    }
    
    /**
     * �ж��Ƿ����ӳɹ�
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
			//����Ƿ����ӣ����û���ӣ����Զ�������
				if(outputStream==null)
				{
					connect() ;
					if(outputStream==null)
					{//˵����������ʧ��
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
			{//����ʧ�ܣ�������Ҫ�ر����ӣ������´γ����������ӷ�����
				disconnect() ;
				
				if(log.isDebugEnabled())
				{
					log.error(ee) ;
				}
				
				sentErrCount ++ ;
				return HandleResult.Succ;//ȷ�����в�Ҫ����������
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
     * �������նˣ�ͨ�������̷߳�ʽ�����Ա�֤���紫��ʧ�ܺ�֧��
     * �����Զ��������ӵĹ���
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

            //���������£�����Ҫȷ�����ӵĹرգ��Ա���ռ����Դ
            //ͬʱ�������׳�������ʹ���ӳز�������������ӳɹ�
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
     * �������ݵ�NetServer
     * Ϊ�˷�ֹ�˷����ܵ������ٶ�Ӱ�죬����Ӱ�����ݽ��գ�����Com��NetServer��
     * �˷�����ֱ�Ӱ������ӵ������У��ɶ�����һͷ���̴߳���
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
