package com.dw.system.encrypt;

import java.io.*;
import java.util.*;

public class DesInputStream extends InputStream
{
	static final int DEF_BUF_LEN=30 ;
	
	byte[] key = null ;
	DesJava desJ = null ;
	
	InputStream innerIS = null ;
	
	/**
	 * ���ܵ�ֵ
	 */
	byte[] enBuf = new byte[DEF_BUF_LEN];
	
	int enLen = 0 ;//��ǰ���ܴ�����
	int enReadLen = 0 ;//��ǰ��ȡ���ܴ�����
	
	LinkedList<byte[]> readbys = new LinkedList<byte[]>() ;
	int readFirstLen = 0 ;
	
	public DesInputStream(byte[] key,InputStream inneris) throws Exception
	{
		this.key = key ;
		desJ = new DesJava(key) ;
		
		innerIS = inneris ;
	}

	@Override
	synchronized public int read() throws IOException
	{
		if(readbys.size()>0)
		{
			byte[] fbs = readbys.getFirst();
			int r = fbs[readFirstLen] ;
			readFirstLen++ ;
			if(readFirstLen==fbs.length)
			{
				readFirstLen = 0 ;
				readbys.removeFirst() ;
			}
			return r ;
		}
		
		if(enLen<=0)
		{
			enLen = innerIS.read() ;
			if(enLen<=0)
			{
				throw new IOException("wrong DesInputStream data pack len 0,May be sec key error") ;
			}
			if(enLen>DEF_BUF_LEN)
				throw new IOException("wrong DesInputStream data pack len.May be sec key error") ;
			enReadLen = 0 ;
		}
		while(enReadLen<enLen)
		{
			int r =innerIS.read(enBuf, enReadLen, enLen-enReadLen) ;
			if(r<0)
				return -1 ;
			
			enReadLen += r ;
		}
		
		try
		{
			byte[] ddata = desJ.decrypt(enBuf, 0, enLen) ;
			//System.out.println("DEsInput dec="+new String(ddata)+" ddlen="+ddata.length) ;
			readbys.addLast(ddata) ;
			
			enLen = enReadLen = 0 ;
			
			return read() ;
		}
		catch(Exception ee)
		{
			//ee.printStackTrace();
			throw new IOException("encrypt err:"+ee.getMessage());
		}
	}
	
	public int available() throws IOException
	{
		if(readbys.size()<=0)
			return 0 ;
		
		int r = 0 ;
		for(byte[] bs:readbys)
		{
			r += bs.length ;
		}
		return r - readFirstLen ;
	}
	
	@Override
	public void close() throws IOException
	{
		innerIS.close() ;
	}
}
