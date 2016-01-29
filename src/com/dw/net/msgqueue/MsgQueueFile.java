package com.dw.net.msgqueue;

import java.io.*;
import java.util.*;

public class MsgQueueFile
{
	public static long MAX_MSG_FILELEN = 1024;

    long curFileLen = 0;
    File file = null;
    RandomAccessFile fileReadStream = null;
    RandomAccessFile fileWriteStream = null;

    static void writeInt32(RandomAccessFile s,int value) throws IOException
    {
        byte[] buf = new byte[4];
        buf[0] = (byte)value;
        buf[1] = (byte)(value >> 8);
        buf[2] = (byte)(value >> 0x10);
        buf[3] = (byte)(value >> 0x18);
        s.write(buf, 0, 4);
    }

    static void writeInt64(RandomAccessFile s,long value) throws IOException
    {
        byte[] buf = new byte[8];
        buf[0] = (byte)value;
        buf[1] = (byte)(value >> 8);
        buf[2] = (byte)(value >> 0x10);
        buf[3] = (byte)(value >> 0x18);
        buf[4] = (byte)(value >> 0x20);
        buf[5] = (byte)(value >> 40);
        buf[6] = (byte)(value >> 0x30);
        buf[7] = (byte)(value >> 0x38);
        s.write(buf, 0, 8);
    }

    static int readInt32(RandomAccessFile s) throws IOException
    {
        byte[] buf = new byte[4];
        s.read(buf, 0, 4);
        return (((buf[0] | (buf[1] << 8)) | (buf[2] << 0x10)) | (buf[3] << 0x18));
    }


    static long readInt64(RandomAccessFile s) throws IOException
    {
        byte[] buf = new byte[8];
        s.read(buf, 0, 8);
        long num1 = (long)(((buf[0] | (buf[1] << 8)) | (buf[2] << 0x10)) | (buf[3] << 0x18));
        long num2 = (long)(((buf[4] | (buf[5] << 8)) | (buf[6] << 0x10)) | (buf[7] << 0x18));
        return (long)((num2 << 0x20) | num1);
    }




    public MsgQueueFile(String fp) throws Exception
    {
        file = new File(fp);
        RandomAccessFile raf = new RandomAccessFile(file,"rw") ;
        fileReadStream = new RandomAccessFile(file,"rw") ;


        fileWriteStream = new RandomAccessFile(file,"rw") ;

        curFileLen = fileWriteStream.length();
        if (curFileLen == 0)
        {
            writeInt64(fileWriteStream, 8);
            
            curFileLen = 8;
        }
    }

    public long getCurFileLen()
    {
        return curFileLen;
    }

    public void addData(byte[] cont) throws Exception
    {
        if (cont == null)
            return;

        synchronized (fileWriteStream)
        {
            int len = cont.length;
            fileWriteStream.seek(fileWriteStream.length());
            writeInt32(fileWriteStream, len);
            fileWriteStream.write(cont, 0, cont.length);
            
            curFileLen += (4 + cont.length);
        }
    }

    public byte[] getDate() throws Exception
    {
        synchronized (fileReadStream)
        {
            fileReadStream.seek(fileReadStream.length());
            long curp = readInt64(fileReadStream);
            if (curp >= curFileLen)
                return null;

            fileReadStream.seek(curp);
            int len = readInt32(fileReadStream);
            byte[] buf = new byte[len];
            fileReadStream.read(buf, 0, len);


            fileReadStream.seek(0);
            writeInt64(fileReadStream, curp + 4 + len);

            return buf;
        }
    }

    
    public void dispose() throws IOException
    {
        if (fileReadStream != null)
            fileReadStream.close();

        if (fileWriteStream != null)
            fileWriteStream.close();
    }

    
}
