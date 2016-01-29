package com.dw.net.proxy;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class ServiceWrapperMain implements WrapperListener
{
	//ScadaClient sc = new ScadaClient() ;
	
	private ProxyServer ps = null ;
	
	private ServiceWrapperMain()
	{
		try
		{//�˴������ŵ����캯���У�����ʹopc��������
			//��Ϊ�˲��ֿ��ܲ�����service��������
			//ProxyServer.getInstance().start() ;
			ps = new ProxyServer() ;
		}
		catch(Exception ee)
		{
			ee.printStackTrace() ;
		}
	}
	
	public Integer start(String[] args)
	{
		try
		{
			ps.setConfig(null,Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]));
			
			ps.start() ;
			return null;
		}
		catch(Throwable t)
		{
			t.printStackTrace() ;
			return -1 ;
		}
	}

	public int stop(int extcode)
	{
		try
		{
			ps.stop() ;
		}
		catch(Exception ee)
		{
			ee.printStackTrace();
		}
		
		System.exit( extcode );
        return extcode;
	}

	public void controlEvent(int arg)
	{
		
	}

	public static void main( String[] args )
    {
        System.out.println( "Initializing..." );
        
        // Start the application.  If the JVM was launched from the native
        //  Wrapper then the application will wait for the native Wrapper to
        //  call the application's start method.  Otherwise the start method
        //  will be called immediately.
        WrapperManager.start(new ServiceWrapperMain(), args);
    }
}