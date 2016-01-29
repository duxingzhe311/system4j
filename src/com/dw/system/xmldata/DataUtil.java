package com.dw.system.xmldata;

import java.io.IOException;
import java.io.InputStream;

public class DataUtil
{
	public final static byte[] intToBytes(int i)
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

	public final static short bytesToShort(byte[] bytes)
	{
		if (bytes == null || bytes.length != 2)
			throw new IllegalArgumentException("byte array size must be 2!");

		short i = 0;
		i = (short) (bytes[0] & 0xFF);
		i = (short) ((i << 8) | (bytes[1] & 0xFF));

		return i;
	}

	public final static byte[] shortToBytes(short i)
	{
		// int is 32bits, 4Bytes
		byte[] bytes = new byte[2];

		bytes[1] = (byte) (i & 0xFF);

		i = (short) (i >>> 8);
		bytes[0] = (byte) (i & 0xFF);

		return bytes;
	}

	public final static int bytesToInt(byte[] bytes)
	{
		if (bytes == null || bytes.length != 4)
			throw new IllegalArgumentException("byte array size must be 4!");

		int i = 0;
		i = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
		i = (i << 8) | (bytes[2] & 0xFF);
		i = (i << 8) | (bytes[3] & 0xFF);

		return i;
	}

	public final static byte[] longToBytes(long i)
	{
		// long is 64bits, 8Bytes
		byte[] bytes = new byte[8];

		bytes[7] = (byte) (i & 0xFF);

		i = i >>> 8;
		bytes[6] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[5] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[4] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[3] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[2] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[1] = (byte) (i & 0xFF);
		i = i >>> 8;
		bytes[0] = (byte) (i & 0xFF);

		return bytes;
	}

	public final static long bytesToLong(byte[] bytes)
	{
		if (bytes == null || bytes.length != 8)
			throw new IllegalArgumentException("byte array size must be 8!");

		long i = 0;

		i = ((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF);
		i = (i << 8) | (bytes[2] & 0xFF);
		i = (i << 8) | (bytes[3] & 0xFF);
		i = (i << 8) | (bytes[4] & 0xFF);
		i = (i << 8) | (bytes[5] & 0xFF);
		i = (i << 8) | (bytes[6] & 0xFF);
		i = (i << 8) | (bytes[7] & 0xFF);
		// i = (i << 8) | (bytes [3] & 0xFF) ;
		return i;
	}

	public final static byte[] floatToBytes(float f)
	{
		return intToBytes(Float.floatToIntBits(f));
	}

	public final static float bytesToFloat(byte[] bytes)
	{
		return Float.intBitsToFloat(bytesToInt(bytes));
	}

	public final static byte[] doubleToBytes(double f)
	{
		return longToBytes(Double.doubleToLongBits(f));
	}

	public final static double bytesToDouble(byte[] bytes)
	{
		return Double.longBitsToDouble(bytesToLong(bytes));
	}

	public final static byte booleanToByte(boolean b)
	{
		if (b)
			return (byte) 1;
		else
			return (byte) 0;
	}

	public final static boolean byteToBoolean(byte b)
	{
		return b != 0;
	}

	public final static byte[] readBytes(InputStream in, int size)
			throws IOException
	{
		if (size <= 0)
			return new byte[0];

		byte[] buffer = new byte[size];

		int count = 0;
		int ret = 0;
		while (true)
		{
			ret = in.read(buffer, count, size - count);
			if (ret == -1)
				throw new IOException("No more bytes! [" + count + " < " + size
						+ "]");
			count += ret;
			if (count == size)
				break;

		}
		if (count != size)
			throw new IOException("Must be " + size + " bytes! [" + count + "]");

		return buffer;
	}

	/**
	 * read boolean value from inputStream.
	 */
	public static boolean readBoolean(InputStream in) throws IOException
	{
		return byteToBoolean(readByte(in));
	}

	/**
	 * read long value from inputStream.
	 */
	public static long readLong(InputStream in) throws IOException
	{

		return bytesToLong(readBytes(in, 8));
	}

	/**
	 * read byte value from inputStream.
	 */
	public static byte readByte(InputStream in) throws IOException
	{
		int ret = in.read();
		if (ret < 0)
			throw new IOException("Must be 1bytes! [" + ret + "]");

		return (byte) ret;
	}

	/**
	 * read short value from inputStream.
	 */
	public static short readShort(InputStream in) throws IOException
	{
		return bytesToShort(readBytes(in, 2));
	}

	/**
	 * read integer value from inputStream.
	 */
	public static int readInt(InputStream in) throws IOException
	{

		return bytesToInt(readBytes(in, 4));
	}

	/**
	 * read float value from inputStream.
	 */
	public static float readFloat(InputStream in) throws IOException
	{
		return bytesToFloat(readBytes(in, 4));
	}

	/**
	 * read double value from inputStream.
	 */
	public static double readDouble(InputStream in) throws IOException
	{

		return bytesToDouble(readBytes(in, 8));
	}

	public static byte[] readByteArray(InputStream in, int size)
			throws IOException
	{

		return readBytes(in, size);
	}

	public static short[] readShortArray(InputStream in, int size)
			throws IOException
	{
		short[] shorts = new short[size];

		for (int i = 0; i < size; i++)
		{
			shorts[i] = readShort(in);
		}

		return shorts;
	}

	public static int[] readIntArray(InputStream in, int size)
			throws IOException
	{
		int[] ints = new int[size];

		for (int i = 0; i < size; i++)
		{
			ints[i] = readInt(in);
		}

		return ints;
	}

	public static long[] readLongArray(InputStream in, int size)
			throws IOException
	{
		long[] longs = new long[size];

		for (int i = 0; i < size; i++)
		{
			longs[i] = readLong(in);
		}

		return longs;
	}

	public static float[] readFloatArray(InputStream in, int size)
			throws IOException
	{
		float[] floats = new float[size];

		for (int i = 0; i < size; i++)
		{
			floats[i] = readFloat(in);
		}

		return floats;
	}

	public static double[] readDoubleArray(InputStream in, int size)
			throws IOException
	{
		double[] doubles = new double[size];

		for (int i = 0; i < size; i++)
		{
			doubles[i] = readDouble(in);
		}

		return doubles;
	}
}
