package com.dw.net.broadcast;

import java.io.*;
import java.net.*;
/**
 * <p>Title: UUID²úÉúÆ÷</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */
public class IdGenerator
{
	private static IdentifierGenerator gen = new UUIDHexGenerator();
	public static String createNewId()
	{
		return (String) gen.generate();
	}

	public static void main(String[] args)
	{
		for (int i = 0; i < 10; i++)
		{
			System.out.println(createNewId());
		}
	}
}

class UUIDHexGenerator
	extends UUIDGenerator
{

	private final String sep;

	public UUIDHexGenerator()
	{
		super();
		sep = "";
	}

	public UUIDHexGenerator(String sep)
	{
		super();
		this.sep = sep;
	}

	protected String format(int intval)
	{
		String formatted = Integer.toHexString(intval);
		StringBuffer buf = new StringBuffer("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);
		return buf.toString();
	}

	protected String format(short shortval)
	{
		String formatted = Integer.toHexString(shortval);
		StringBuffer buf = new StringBuffer("0000");
		buf.replace(4 - formatted.length(), 4, formatted);
		return buf.toString();
	}

	public Serializable generate()
	{
		return new StringBuffer(36)
			.append(format(getIP())).append(sep)
			.append(format(getJVM())).append(sep)
			.append(format(getHiTime())).append(sep)
			.append(format(getLoTime())).append(sep)
			.append(format(getCount()))
			.toString();
	}

	public static void main(String[] args)
		throws Exception
	{
		IdentifierGenerator gen = new UUIDHexGenerator("/");
		IdentifierGenerator gen2 = new UUIDHexGenerator("/");
		for (int i = 0; i < 10; i++)
		{
			String id = (String) gen.generate();
			System.out.println(id + ": " + id.length());
			String id2 = (String) gen2.generate();
			System.out.println(id2 + ": " + id2.length());
		}
	}

}

class UUIDStringGenerator
	extends UUIDGenerator
{

	private String sep;

	public UUIDStringGenerator()
	{
		super();
		sep = "";
	}

	public UUIDStringGenerator(String sep)
	{
		super();
		this.sep = sep;
	}

	public Serializable generate()
	{
		return new StringBuffer(20)
			.append(toString(getIP())).append(sep)
			.append(toString(getJVM())).append(sep)
			.append(toString(getHiTime())).append(sep)
			.append(toString(getLoTime())).append(sep)
			.append(toString(getCount()))
			.toString();
	}

	public static void main(String[] args)
		throws Exception
	{
		IdentifierGenerator gen = new UUIDStringGenerator(); //("/");
		for (int i = 0; i < 5; i++)
		{
			String id = (String) gen.generate();
			System.out.println(id + ": " + id.length());
		}
	}

	private static String toString(int value)
	{
		return new String(BytesHelper.toBytes(value));
	}

	private static String toString(short value)
	{
		return new String(BytesHelper.toBytes(value));
	}

}

final class BytesHelper
{

	public static int toInt(byte[] bytes)
	{
		int result = 0;
		for (int i = 0; i < 4; i++)
		{
			result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
		}
		return result;
	}

	public static short toShort(byte[] bytes)
	{
		return (short) ( ( ( - (short) Byte.MIN_VALUE + (short) bytes[0]) << 8) -
						(short) Byte.MIN_VALUE + (short) bytes[1]);
	}

	public static byte[] toBytes(int value)
	{
		byte[] result = new byte[4];
		for (int i = 3; i >= 0; i--)
		{
			result[i] = (byte) ( (0xFFl & value) + Byte.MIN_VALUE);
			value >>>= 8;
		}
		return result;
	}

	public static byte[] toBytes(short value)
	{
		byte[] result = new byte[2];
		for (int i = 1; i >= 0; i--)
		{
			result[i] = (byte) ( (0xFFl & value) + Byte.MIN_VALUE);
			value >>>= 8;
		}
		return result;
	}

	public static void main(String[] args)
	{
		System.out.println(0 + "==" + BytesHelper.toInt(BytesHelper.toBytes(0)));
		System.out.println(1 + "==" + BytesHelper.toInt(BytesHelper.toBytes(1)));
		System.out.println( -1 + "==" +
						   BytesHelper.toInt(BytesHelper.toBytes( -1)));
		System.out.println(Integer.MIN_VALUE + "==" +
						   BytesHelper.toInt(
			BytesHelper.toBytes(Integer.MIN_VALUE)));
		System.out.println(Integer.MAX_VALUE + "==" +
						   BytesHelper.toInt(
			BytesHelper.toBytes(Integer.MAX_VALUE)));
		System.out.println(Integer.MIN_VALUE / 2 + "==" +
						   BytesHelper.toInt(
			BytesHelper.toBytes(Integer.MIN_VALUE / 2)));
		System.out.println(Integer.MAX_VALUE / 2 + "==" +
						   BytesHelper.toInt(
			BytesHelper.toBytes(Integer.MAX_VALUE / 2)));
	}

}

abstract class UUIDGenerator
	implements IdentifierGenerator
{

	private static final int ip;
	static
	{
		int ipadd;
		try
		{
			ipadd = BytesHelper.toInt(InetAddress.getLocalHost().getAddress());
		}
		catch (Exception e)
		{
			ipadd = 0;
		}
		ip = ipadd;
	}

	private static short counter = (short) 0;
	private static final int jvm = (int) (System.currentTimeMillis() >>> 8);

	public UUIDGenerator()
	{
	}

	/**
	 * Unique across JVMs on this machine (unless they load this class
	 * in the same quater second - very unlikely)
	 */
	protected int getJVM()
	{
		return jvm;
	}

	/**
	 * Unique in a millisecond for this JVM instance (unless there
	 * are > Short.MAX_VALUE instances created in a millisecond)
	 */
	protected short getCount()
	{
		synchronized (UUIDGenerator.class)
		{
			if (counter < 0)
			{
				counter = 0;
			}
			return counter++;
		}
	}

	/**
	 * Unique in a local network
	 */
	protected int getIP()
	{
		return ip;
	}

	/**
	 * Unique down to millisecond
	 */
	protected short getHiTime()
	{
		return (short) (System.currentTimeMillis() >>> 32);
	}

	protected int getLoTime()
	{
		return (int) System.currentTimeMillis();
	}

}

interface IdentifierGenerator
{
	/**
	 * Generate a new identifier.
	 * @param session
	 * @param object the entity or toplevel collection for which the id is being generated
	 * @return Serializable a new identifier
	 * @throws SQLException
	 * @throws IdentifierGenerationException
	 */
	public Serializable generate();
}