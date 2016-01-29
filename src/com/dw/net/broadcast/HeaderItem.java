package com.dw.net.broadcast;

import java.io.*;
import java.util.*;

/**
 * <p>Title: ��Ϣͷ�����ӺͲü�</p>
 * <p>Description: һ����Ϣ�ڷ���֮ǰ��Ҫ�Դ�������Ϣͷ�����������Ϣ�ţ�˳��ŵ�����</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */
public class HeaderItem
{
	int len = -1;
	byte[] content = null;

	//private static byte[] currentBytes = null ;
	//private static byte[] currentBytes = null ;
	//private static byte[] currentBytes = null ;
	/**
	 * ��һ��byte[]����һ��ͷ������Ϣ
	 * @param sor ԭʼ��Ϣ
	 * @param hi ��Ϣͷ����
	 * @return ����ͷ��Ϣ��byte[]
	 */
	public static byte[] appendHeaderItem(byte[] sor, HeaderItem hi)
	{
		int l = hi.getLength();
		byte[] tmp = hi.getContent();
		if (l <= 0 || tmp == null)
		{
			throw new RuntimeException("Header length is little than content!!");
		}

		byte[] tmpb = new byte[sor.length + l + 4];

		byte[] lenb = intToBytes(l);
		System.arraycopy(lenb, 0, tmpb, 0, 4);

		System.arraycopy(tmp, 0, tmpb, 4, tmp.length);

		System.arraycopy(sor, 0, tmpb, l + 4, sor.length);

		return tmpb;
	}

	/**
	 * ��һ��ԭʼ��Ϣ�Ĳ��֣�����ͷ��Ϣ�󷵻�
	 * @param sor ԭʼ��Ϣ���ڵ�byte[]
	 * @param offset ��Ч��Ϣ����ʼλ��
	 * @param len ��Ч��Ϣ����
	 * @param hi ͷ��Ϣ
	 * @return ����ͷ��Ϣ��byte[]
	 */
	public static byte[] appendHeaderItem(byte[] sor, int offset, int len,
										  HeaderItem hi)
	{
		int l = hi.getLength();
		byte[] tmp = hi.getContent();
		if (l <= 0 || tmp == null)
		{
			throw new RuntimeException("Header length is little than content!!");
		}

		byte[] tmpb = new byte[len + l + 4];

		byte[] lenb = intToBytes(l);
		System.arraycopy(lenb, 0, tmpb, 0, 4);

		System.arraycopy(tmp, 0, tmpb, 4, tmp.length);

		System.arraycopy(sor, offset, tmpb, l + 4, len);

		return tmpb;
	}

	/**
	 * ���һ����Ϣ�е�ͷ��Ϣ
	 * @param sor �Ѿ�����ͷ��Ϣ����Ϣ
	 * @return ����Ϣ�ж�ȡ��ͷ������Ϣ
	 */
	public static HeaderItem fetchHeaderItem(byte[] sor)
	{
		byte[] lenb = new byte[4];
		System.arraycopy(sor, 0, lenb, 0, 4);
		int headerlen = bytesToInt(lenb);
		if (headerlen <= 0)
		{
			throw new RuntimeException("Cannot get header,no length info !!");
		}
		if (headerlen > sor.length - 4)
		{
			throw new RuntimeException("Illegal header length,too imposible !!");
		}

		byte[] tmpb = new byte[headerlen];
		System.arraycopy(sor, 4, tmpb, 0, headerlen);

		return new HeaderItem(tmpb);
	}

	/**
	 * ���һ����Ϣ�г���ͷ��Ϣ����һ����֮���������Ϣ
	 * @param sor ����ͷ��Ϣ����Ϣ
	 * @return �ų���ͷ��Ϣ���������Ϣ
	 */
	public static byte[] fetchContent(byte[] sor)
	{
		HeaderItem hi = fetchHeaderItem(sor);
		return cutHeaderItem(sor, hi);
	}

	/**
	 * ��һ������ͷ��Ϣ����Ϣ�вü������ݡ�
	 * @param sor ����ͷ��Ϣ����Ϣ
	 * @param hi ͷ��Ϣ
	 * @return �ü������Ϣ
	 */
	public static byte[] cutHeaderItem(byte[] sor, HeaderItem hi)
	{
		int l = hi.getLength();
		return cutHeaderItem(sor, l);
	}

	/**
	 * ��һ������ͷ��Ϣ����Ϣ�вü������ݡ�
	 * @param sor ����ͷ��Ϣ����Ϣ
	 * @param headerlen ͷ��Ϣ�ĳ���
	 * @return �ü������Ϣ
	 */
	public static byte[] cutHeaderItem(byte[] sor, int headerlen)
	{
		if (sor.length < headerlen)
		{
			throw new RuntimeException("Header length is bigger than content!!");
		}

		int contlen = sor.length - headerlen - 4;
		byte[] tmpb = new byte[contlen];
		System.arraycopy(sor, headerlen + 4, tmpb, 0, contlen);

		return tmpb;
	}

	/**
	 * ������ת��Ϊ�̶����ȵ�byte[]��ʾ
	 * @param i ���������
	 * @return byte[]��ʾ����Ϣ
	 */
	final static byte[] intToBytes(int i)
	{
		// int is 32bits, 4Bytes
		byte[] bytes = new byte[4];

		bytes[3] = (byte) (i & 0xFF);

		i = i >>> 8;
		bytes[2] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[1] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[0] = (byte) (i & 0xFF);

		return bytes;
	}

	/**
	 * ��byte[4]��ʾ�ĳ�����Ϣת��Ϊ����
	 * @param bytes byte[4]��ʾ�ĳ�����Ϣ
	 * @return ������Ϣ
	 */
	final static int bytesToInt(byte[] bytes)
	{
		if (bytes == null || bytes.length != 4)
		{
			throw new IllegalArgumentException("byte array size must be 4!");
		}

		int i = 0;
		i = ( (bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
		i = (i << 8) | (bytes[2] & 0xFF);
		i = (i << 8) | (bytes[3] & 0xFF);

		return i;
	}

	/**
	 * ����ͷ��Ϣ
	 * @param cont ��ʼ��ͷ����
	 */
	public HeaderItem(byte[] cont)
	{
		content = cont;
		len = content.length;
	}

	/**
	 * ����ͷ��Ϣ
	 * @param cont ��ʼ��ͷ����
	 */
	public HeaderItem(String cont)
	{
		content = cont.getBytes(); //("GBK");
		len = content.length;
	}

	/**
	 * ����ͷ��Ϣ������
	 * @param cont ����
	 */
	public void setContent(byte[] cont)
	{
		content = cont;
		len = content.length;
	}

	/**
	 * ����ͷ��Ϣ������
	 * @param cont ����
	 */
	public void setContent(String cont)
	{
		content = cont.getBytes(); //("GBK");
		len = content.length;
	}

	/**
	 * ����ͷ��Ϣ������
	 * @param cont ����
	 * @param len ����
	 */
	public void setContent(byte[] cont, int len)
	{
		if (cont.length > len)
		{
			throw new RuntimeException("the length is little than content!!");
		}
		content = cont;
		this.len = len;
	}

	/**
	 * �õ�ͷ��Ϣ�ĳ���
	 * @return ����
	 */
	public int getLength()
	{
		return len;
	}

	/**
	 * �õ�ͷ��Ϣ������
	 * @return ����
	 */
	public byte[] getContent()
	{
		return content;
	}

	/**
	 * �õ�ͷ��Ϣ������
	 * @return �ַ�����ʾ������
	 */
	public String getContentStr()
	{
		return new String(content); //,"GBK");
	}

	public static void main(String args[])
	{
		byte[] content = args[1].getBytes();
		System.out.println("Input--->\n\tHeader=" + args[0] + "\n\tContent=" +
						   args[1] + "\n");

		HeaderItem hi = new HeaderItem(args[0]);
		long s = System.currentTimeMillis();
		content = HeaderItem.appendHeaderItem(content, hi);
		content = HeaderItem.appendHeaderItem(content, hi);
		content = HeaderItem.appendHeaderItem(content, hi);

		hi = HeaderItem.fetchHeaderItem(content);
		content = HeaderItem.fetchContent(content);
		//System.out.println("fetched header="+hi.getContentStr());

		hi = HeaderItem.fetchHeaderItem(content);
		content = HeaderItem.fetchContent(content);
		//System.out.println("fetched header="+hi.getContentStr());

		hi = HeaderItem.fetchHeaderItem(content);
		content = HeaderItem.fetchContent(content);
		long e = System.currentTimeMillis();
		System.out.println("cost=" + (e - s));
		System.out.println("fetched header=" + hi.getContentStr());

		//content = HeaderItem.fetchContent (content);
		System.out.println("fetched contnet=" + new String(content));
	}
}