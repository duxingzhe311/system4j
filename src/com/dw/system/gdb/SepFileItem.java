package com.dw.system.gdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.*;

/**
 * 用来标记存储在独立文件的文件对象
 * 
 * 改对象可以避免获取对象时并没有必要获取文件内容而占用系统内存资源
 * 
 * @author Jason Zhu
 */
public class SepFileItem
{
	private long id = -1 ;
	
	private File file = null ;
	
	/**
	 * 内容
	 */
	private transient byte[] cont = null ;
	
	
	/**
	 * 添加新内容时的构造方法
	 * @param c
	 */
	public SepFileItem(byte[] c)
	{
		cont = c ;
	}
	
	public SepFileItem(long id,File f)
	{
		this.id = id ;
		this.file = f ;
	}
	
	public long getId()
	{
		return id ;
	}
	/**
	 * 如果该文件内容已经存在内存中,可以通过该方法获取
	 * 新增的时候应该使用该方法
	 * @return
	 */
	public byte[] getTransientContent()
	{
		return cont ;
	}
	
	/**
	 * 如果对应的文件内容已经存在库中,则该方法可以返回对应的内容
	 * 
	 * 该方法会读取库中的文件内容到内存中,不推荐使用
	 * @return
	 */
	public byte[] readContentBytes() throws IOException
	{
		if(id<=0)
			throw new RuntimeException("Sep File Has no id");
		
		if(cont!=null)
			return cont ;
		
		//读取到内存中
		
		FileInputStream fis = null;
		try
		{
			byte[] buf = new byte[(int) file.length()];
			fis = new FileInputStream(file);
			fis.read(buf);

			cont = buf ;
			return cont ;
		}
		finally
		{
			if (fis != null)
				fis.close();
		}
	}
	
	/**
	 * 如果对应的文件内容已经存在库中,则该方法可以读取相关内容,并输出到输出流中
	 * 该方法占用内存资源小
	 * @param outs
	 */
	public void readContentToOutput(OutputStream outs)
		throws IOException
	{
		if(id<=0)
			throw new RuntimeException("Sep File Has no id");
		
		
		FileInputStream fis = null;
		try
		{
			byte[] buf = new byte[1024];
			fis = new FileInputStream(file);
			int l = 0 ;
			while((l=fis.read(buf))>0)
			{
				outs.write(buf, 0, l);
			}
		}
		finally
		{
			if (fis != null)
				fis.close();
		}
	}
	
	
	//public void writeContentFrom
}
