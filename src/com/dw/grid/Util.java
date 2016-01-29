package com.dw.grid;

import java.io.InputStream;

public class Util
{
	public static String readStreamLine(InputStream inputs, int maxlen)
			throws Exception
	{
		StringBuilder firstline = new StringBuilder();
		int c;
		while ((c = inputs.read()) >= 0)
		{
			if (c == '\n')
			{
				return firstline.toString();
			}

			firstline.append((char) c);
			if (firstline.length() > maxlen)
			{
				throw new Exception("invalid line,big than max len!");
			}
		}

		throw new Exception("invalid line,big than max len!");
	}
}
