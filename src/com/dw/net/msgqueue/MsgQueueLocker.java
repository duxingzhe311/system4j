package com.dw.net.msgqueue;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class MsgQueueLocker
{
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	Object addLocker = new Object();
	Object getLocker = new Object();

    File dirBase = null;
    //string queName = null;
    public int count = 0;

    public MsgQueueLocker(String dirbase, String quename)
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

    public String getNewMsgFileName() throws IOException
    {
        synchronized (this)
        {
            //2147483647
            //2000000000
            count++;
            if (count >= 2000000000)
                count = 0;

            StringBuilder tmpsb = new StringBuilder();
            tmpsb.append(dirBase.getCanonicalPath())
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
}
