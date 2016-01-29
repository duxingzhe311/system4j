package com.dw.biz.api.util;

public class BinHexTransfer
{
  private static final int CharsChunkSize = 128;
  private static final String s_hexDigits = "0123456789ABCDEF";

  public static void TransBinToHexStr(byte[] buffer, int index, int count, StringBuffer writer)
  {
    if (buffer == null)
    {
      throw new IllegalArgumentException("buffer");
    }
    if (index < 0)
    {
      throw new IllegalArgumentException("index");
    }
    if (count < 0)
    {
      throw new IllegalArgumentException("count");
    }
    if (count > buffer.length - index)
    {
      throw new IllegalArgumentException("count");
    }
    char[] chArray1 = new char[count * 2 < 128 ? count * 2 : ''];
    int num1 = index + count;
    while (index < num1)
    {
      int num2 = count < 64 ? count : 64;
      int num3 = Encode(buffer, index, num2, chArray1);
      writer.append(chArray1, 0, num3);
      index += num2;
      count -= num2;
    }
  }

  public static String TransBinToHexStr(byte[] inArray)
  {
    return TransBinToHexStr(inArray, 0, inArray.length);
  }

  public static String TransBinToHexStr(byte[] inArray, int offsetIn, int count)
  {
    if (inArray == null)
    {
      throw new IllegalArgumentException("inArray");
    }
    if (offsetIn < 0)
    {
      throw new IllegalArgumentException("offsetIn");
    }
    if (count < 0)
    {
      throw new IllegalArgumentException("count");
    }
    if (count > inArray.length - offsetIn)
    {
      throw new IllegalArgumentException("count");
    }
    char[] chArray1 = new char[2 * count];
    int num1 = Encode(inArray, offsetIn, count, chArray1);
    return new String(chArray1, 0, num1);
  }

  public static byte[] TransHexStrToBin(String hexstr)
  {
    int c = hexstr.length();
    byte[] rets = new byte[c / 2];
    for (int i = 0; i < c; i += 2)
    {
      byte tmph = 0;
      char ch = hexstr.charAt(i);
      char cl = hexstr.charAt(i + 1);
      if ((ch >= '0') && (ch <= '9'))
        tmph = (byte)(ch - '0');
      else if ((ch >= 'A') && (ch <= 'F'))
        tmph = (byte)(ch - 'A' + 10);
      else {
        throw new RuntimeException("Illegal hex str unknow char=" + ch);
      }
      byte tmpl = 0;
      if ((cl >= '0') && (cl <= '9'))
        tmpl = (byte)(cl - '0');
      else if ((cl >= 'A') && (cl <= 'F'))
        tmpl = (byte)(cl - 'A' + 10);
      else {
        throw new RuntimeException("Illegal hex str unknow char=" + cl);
      }
      rets[(i / 2)] = ((byte)((tmph << 4) + tmpl));
    }
    return rets;
  }

  private static int Encode(byte[] inArray, int offsetIn, int count, char[] outArray)
  {
    int outi = 0;
    int j = 0;
    int outlen = outArray.length;
    for (int i = 0; i < count; i++)
    {
      byte tmpi = inArray[(offsetIn++)];
      outArray[(outi++)] = "0123456789ABCDEF".charAt(tmpi >> 4 & 0xF);
      if (outi == outlen)
      {
        break;
      }
      outArray[(outi++)] = "0123456789ABCDEF".charAt(tmpi & 0xF);
      if (outi == outlen)
      {
        break;
      }
    }
    return outi - j;
  }
}
