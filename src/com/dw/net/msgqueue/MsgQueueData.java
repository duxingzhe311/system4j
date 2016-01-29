package com.dw.net.msgqueue;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;

/**
 * �ṩ����Ϣ���б����֧��
 * 
 * ʵ��˼·��
 * 1�������ļ�ϵͳ
 * 2��ÿ�����ж�Ӧ�Լ��������ļ�
 * 3�������ļ�ͷ8���ֽ���������ǰ���ݿ�����
 *     ����������Ϊ4�ֽڵĳ��Ⱥ����ݣ���˳��д��
 *     ��ȡʱ������ǰ8���ֽڵ�ָ�룬��λ����ǰ���ݿ顣ÿ�ζ�ȡ���ݺ��޸�ͷָ�롣ʹָ֮����һ������
 * 4��ÿ���ļ��������ֵ������ﵽ���ֵ���򴴽����ļ�����д�롣
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
