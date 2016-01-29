package com.dw.net.msgqueue.io;

import java.io.*;
import java.util.*;
import java.text.*;

import com.dw.net.msgqueue.*;

public class FileDirMsgQueueIO implements IMsgQueueIO
{
    Hashtable quename2lock = new Hashtable();

    String dirBase = null;

    public FileDirMsgQueueIO(String dirb)
    {
        dirBase = dirb;
    }
    
    
    //int count = 0;

    synchronized private QueueLocker GetQueueLocker(String quename)
    {
        QueueLocker o = (QueueLocker)quename2lock.get(quename);
        if (o!=null)
            return o;

        o = new QueueLocker(dirBase, quename);
        quename2lock.put(quename,o);
        return o;
    }

    public void addMsgToServer(String quename, byte[] cont)
    	throws Exception
    {
        QueueLocker o = GetQueueLocker(quename);
        synchronized (o.getAddLocker())
        {
            String fn = o.GetNewMsgFileName();

            FileOutputStream fs = null;

            try
            {
                fs = new FileOutputStream(fn + "_a");//, FileMode.Create, FileAccess.Write);
                fs.write(cont);
                fs.close();
                fs = null;

                File fi = new File(fn + "_a");
                fi.renameTo(new File(fn));
            }
            finally
            {
                if (fs != null)
                    fs.close();
            }
        }
    }

    FilenameFilter ff = new FilenameFilter()
    {
    	public boolean accept(File dir, String name)
    	{
    		if(name.endsWith(".mq"))
    			return true ;
    		
    		return false;
    	}
    };
    
    public byte[] getMsgFromServer(String quename)
    	throws Exception//, out byte[] cont, out string failedreson)
    {
        QueueLocker o = GetQueueLocker(quename);

        String getfn = null;
        File getf = null;
        synchronized (o.getGetLocker())
        {
            File[] fs = o.getDirBase().listFiles(ff);
            if (fs == null || fs.length <= 0)
                return null;// empty queue

            //获取最小的文件
            File fi = fs[0];
            for (int i = 1; i < fs.length; i++)
            {
                if (fi.getName().compareTo(fs[i].getName()) > 0)
                    fi = fs[i];
            }

            getfn = fi.getAbsolutePath() + "_g";
            getf  = new File(getfn);
            fi.renameTo(getf);
        }

        byte[] cont = new byte[(int)getf.length()];
        FileInputStream fis = null ;
        try
        {
        	fis = new FileInputStream(getf);
        	fis.read(cont);
        }
        finally
        {
        	if(fis!=null)
        		fis.close();
        }
        
        getf.delete();

        return cont;
    }
}

class QueueLocker
{
    Object addLocker = new Object();
    Object getLocker = new Object();

    File dirBase = null;
    //string queName = null;
    public int count = 0;

    public QueueLocker(String dirbase, String quename)
    {
        dirBase = new File(dirbase + "/" + quename);
        dirBase.mkdirs();
    }

    public Object getAddLocker()
    {
        return addLocker;
    }

    public Object getGetLocker()
    {
        return getLocker;
    }

    public File getDirBase()
    {
        return dirBase;
    }

    synchronized public String GetNewMsgFileName()
    {
        //2147483647
        //2000000000
        count++;
        if (count >= 2000000000)
            count = 0;

        StringBuffer tmpsb = new StringBuffer();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date d = new Date();
        
        tmpsb.append(dirBase.getAbsolutePath())
            .append("/")
            .append(sdf.format(new Date()))
            .append("_");

        String strc = ""+count;
        int c = 10 - strc.length();
        for (int i = 0; i < c; i++)
            tmpsb.append('0');

        tmpsb.append(strc).append(".mq") ;

        return  tmpsb.toString() ;
    }
}