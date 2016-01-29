package com.dw.net.broadcast;

import java.io.*;
import java.util.*;

/**
 * <p>Title: 消息头的增加和裁减</p>
 * <p>Description: 一个消息在发送之前需要对此增加消息头，如主题和消息号，顺序号等内容</p>
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
	 * 对一个byte[]增加一个头对象信息
	 * @param sor 原始信息
	 * @param hi 消息头对象
	 * @return 增加头信息后byte[]
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
	 * 把一个原始消息的部分，增加头信息后返回
	 * @param sor 原始信息所在地byte[]
	 * @param offset 有效信息的起始位置
	 * @param len 有效信息长度
	 * @param hi 头信息
	 * @return 增加头信息后byte[]
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
	 * 获得一个信息中的头信息
	 * @param sor 已经包含头信息的消息
	 * @return 从信息中读取得头对象信息
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
	 * 获得一个信息中除了头信息（第一个）之外的内容信息
	 * @param sor 包含头信息的消息
	 * @return 排除了头信息后的内容信息
	 */
	public static byte[] fetchContent(byte[] sor)
	{
		HeaderItem hi = fetchHeaderItem(sor);
		return cutHeaderItem(sor, hi);
	}

	/**
	 * 从一个包含头信息的消息中裁减出内容。
	 * @param sor 包含头信息的消息
	 * @param hi 头信息
	 * @return 裁减后的消息
	 */
	public static byte[] cutHeaderItem(byte[] sor, HeaderItem hi)
	{
		int l = hi.getLength();
		return cutHeaderItem(sor, l);
	}

	/**
	 * 从一个包含头信息的消息中裁减出内容。
	 * @param sor 包含头信息的消息
	 * @param headerlen 头信息的长度
	 * @return 裁减后的消息
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
	 * 把整数转换为固定长度的byte[]表示
	 * @param i 输入的整数
	 * @return byte[]表示的信息
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
	 * 把byte[4]表示的长度信息转换为整数
	 * @param bytes byte[4]表示的长度信息
	 * @return 整数信息
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
	 * 构造头信息
	 * @param cont 初始化头内容
	 */
	public HeaderItem(byte[] cont)
	{
		content = cont;
		len = content.length;
	}

	/**
	 * 构造头信息
	 * @param cont 初始化头内容
	 */
	public HeaderItem(String cont)
	{
		content = cont.getBytes(); //("GBK");
		len = content.length;
	}

	/**
	 * 设置头信息的内容
	 * @param cont 内容
	 */
	public void setContent(byte[] cont)
	{
		content = cont;
		len = content.length;
	}

	/**
	 * 设置头信息的内容
	 * @param cont 内容
	 */
	public void setContent(String cont)
	{
		content = cont.getBytes(); //("GBK");
		len = content.length;
	}

	/**
	 * 设置头信息的内容
	 * @param cont 内容
	 * @param len 长度
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
	 * 得到头信息的长度
	 * @return 长度
	 */
	public int getLength()
	{
		return len;
	}

	/**
	 * 得到头信息的内容
	 * @return 内容
	 */
	public byte[] getContent()
	{
		return content;
	}

	/**
	 * 得到头信息的内容
	 * @return 字符串表示的内容
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