package com.dw.net.msgqueue;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;

/**
 * 提供对消息进行保存的支持
 * 
 * 实现思路：
 * 1，基于文件系统
 * 2，每个队列对应自己的数据文件
 * 3，数据文件头8个字节用来做当前数据块索引
 *     后续的内容为4字节的长度和内容，按顺序写入
 *     读取时，查找前8个字节的指针，定位到当前数据块。每次读取内容后，修改头指针。使之指向下一个内容
 * 4，每个文件都有最大值，如果达到最大值，则创建新文件进行写入。
 * 
 * @author Jason Zhu
 */
public class MsgQueueData
{
	File dirBase = null;
    String queName = null;
    long firstFileIdx = 0;
    long lastFileIdx = 0;

    MsgQueueFile firstQueFile = null;
    MsgQueueFile lastQueFile = null;

    public MsgQueueData(String dirb,String quename)
    	throws Exception
    {
        dirBase = new File(dirb);
        dirBase.mkdirs() ;
        queName = quename;

        File[] fs = dirBase.listFiles(new FilenameFilter(){

			public boolean accept(File dir, String name)
			{//quename + "_*.mq"
				if(name.startsWith(queName+"_")&&name.endsWith(".mq"))
					return true ;
				
				return false;
			}}) ;
        
        if (fs == null || fs.length == 0)
        {//new
            firstFileIdx = 1;
            lastFileIdx = 1;
        }
        else if (fs.length == 1)
        {
            firstFileIdx = lastFileIdx = getIdxByFileName(fs[0].getName());
        }
        else
        {
            List<String> ss = new ArrayList<String>(fs.length);
            for (File fi : fs)
            {
                ss.add(fi.getName());
            }

            Convert.sort(ss);

            firstFileIdx = getIdxByFileName(ss.get(0));
            lastFileIdx = getIdxByFileName(ss.get(ss.size()-1));
        }

        if (firstFileIdx == lastFileIdx)
        {
            String fn = dirBase.getCanonicalPath() + "/" + calFileNameByIdx(firstFileIdx);
            firstQueFile = lastQueFile = new MsgQueueFile(fn);
        }
        else
        {
            String fn = dirBase.getCanonicalPath() + "/" + calFileNameByIdx(firstFileIdx);
            String ln = dirBase.getCanonicalPath() + "/" +calFileNameByIdx(lastFileIdx);
            firstQueFile = new MsgQueueFile(fn);
            lastQueFile = new MsgQueueFile(ln);
        }
    }

    private String calFileNameByIdx(long idx)
    {
        //9223372036854775807
        String si = ""+idx;
        StringBuilder tmpsb = new StringBuilder(20) ;
        int c = 20 - si.length() ;
        for(int i = 0 ; i < c ; i ++)
            tmpsb.append('0') ;

        tmpsb.append(si).append(".mq") ;

        return queName + "_" + tmpsb.toString();
    }

    public long getIdxByFileName(String fn)
    {
        String tmps = fn;
        if (tmps.endsWith(".mq"))
            tmps = tmps.substring(0, tmps.length() - 3);

        if (tmps.startsWith(queName + "_"))
            tmps = tmps.substring(queName.length() + 1);

        return Long.parseLong(tmps);
    }

    
    private MsgQueueFile GetCanWriteLastQueueFile()
    {
        return null;
    }

    public void AddData(byte[] cont)
    {
    }

    
    public void dispose() throws IOException
    {
        if(firstQueFile!=null)
            firstQueFile.dispose() ;

        if (lastQueFile != null)
            lastQueFile.dispose();
    }

}
