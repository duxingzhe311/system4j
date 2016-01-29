package com.dw.net.dirqueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.UUID;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataWithFile;

public class SendDirQueue implements IObjHandler
{
	public static final int SEND_RES_SUCC = 1;

    public static final int SEND_RES_CONN_FAILED = 0;

    public static final int SEND_RES_AUTH_FAILED = -1;

    public static final int SEND_RES_SEND_FAILED = -2;

    public static final int SEND_RES_RECV_RESP_FAILED = -3;
    
	static SendDirQueue instance = null;

    String dirBase = null;
    String discardDirBase = null;
    String msgtype = null;

    QueueThreadNoWait queue = null;

    public SendDirQueue(String dirBase,String msgtype)
    {
        this.dirBase = dirBase;
        this.msgtype = msgtype;
        
        discardDirBase = dirBase + "discard" + File.separator;

        File dirB = new File(dirBase);
        if(!dirB.exists())
            dirB.mkdirs();

        File discardDirB = new File(discardDirBase);
        if(!discardDirB.exists())
            discardDirB.mkdirs();

        queue = new QueueThreadNoWait(this);
        queue.start();

        File[] fis = dirB.listFiles();
        if (fis != null)
        {
            for(File fi : fis)
            {
                String n = fi.getName();
                if (!n.endsWith(".qi"))
                    continue;

                String tmpid = n.substring(0, n.length() - 3);
                queue.enqueue(tmpid);
            }
        }
    }

    public void close()
    {
        queue.stop();
    }

    private String id2queue_filepath(String id)
    {
        return dirBase + id + ".qi";
    }

    private String id2queue_discard_filepath(String id)
    {
        return discardDirBase + id + ".qi";
    }

    public void sendXmlDataWithFileToQueue(XmlDataWithFile xdwf)
    	throws IOException
    {
        String id = UUID.randomUUID().toString();
        FileOutputStream fs = null;
        try
        {
        	String path = id2queue_filepath(id);
            fs = new FileOutputStream(path);
            byte[] tmpbs = xdwf.toLocalFormatXmlData().toBytesWithUTF8();

            fs.write(tmpbs);
        }
        finally
        {
            if (fs != null)
                fs.close();
        }
        queue.enqueue(id);
    }


    public int getQueueLength()
    {
        return queue.size();
    }

    static FilenameFilter ff =new FilenameFilter()
    {

		public boolean accept(File dir, String name)
		{
			return name.endsWith(".qi") ;
		}
    	
    };
    
    public int getQueueDiscardLength()
    {
            File di = new File(discardDirBase);
            if (!di.exists())
                return 0;

            String[] fis = di.list(ff);
            if (fis == null || fis.length <= 0)
                return 0;

            return fis.length;
    }

    public int processFailedRetryTimes()
    {
        return 3;
    }

    public long processRetryDelay(int retrytime)
    {
        return 3000 * retrytime;
    }

    public HandleResult processObj(Object o, int retrytime)
    {
        String id = (String)o;
        String fp = id2queue_filepath(id);
        File fi = new File(fp) ;
        if(!fi.exists())
            return HandleResult.Succ ;

        String tarurl = Util.getServerUrl();
        if (tarurl==null||tarurl.equals(""))//没有目标说明handler无效
            return HandleResult.Handler_Invalid;

        FileInputStream fis = null ;
        UrlSender urlSender = null;
        try
        {
            fis = new FileInputStream(fp) ;
            byte[] tmpbs = new byte[(int)fi.length()] ;
            fis.read(tmpbs,0,tmpbs.length) ;
            XmlData formatxd = XmlData.parseFromByteArrayUTF8(tmpbs);
            XmlDataWithFile xdwf = new XmlDataWithFile() ;
            xdwf.fromLocalFormatXmlData(formatxd) ;
            
            urlSender = new UrlSender();

            int res = urlSender.sendXmlDataWithFileNoCB(tarurl, msgtype,xdwf,true);
//Log.i("SendDir", "res============" + res);
            
            if (res == UrlSender.SEND_RES_SUCC)
            {
                fis.close();
                fis = null;
                fi.delete();

                return HandleResult.Succ;
            }
            else if (res == UrlSender.SEND_RES_CONN_FAILED)
            {
                return HandleResult.Handler_Invalid;
            }
            else
            {
                return HandleResult.Failed_Retry_Later;
            }
        }
        catch(Exception ioe)
        {
        	ioe.printStackTrace();
        	return HandleResult.Handler_Invalid;
        }
        finally
        {
            if (urlSender != null)
            {
                urlSender.close();
                urlSender = null;
            }

            if(fis!=null)
            {
            	try
            	{
            		fis.close() ;
            	}
            	catch(IOException ioee){}
            }
        }
    }

    public long handlerInvalidWait()
    {
        return 10000;
    }

    public void processObjDiscard(Object o)
    {
        String id = (String)o;
        String fp = id2queue_filepath(id);
        File fi = new File(fp);
        if (!fi.exists())
            return ;

        fi.renameTo(new File(id2queue_discard_filepath(id)));
    }
}
