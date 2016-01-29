package com.dw.biz.platform.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Stack;

public class FileIncludeReader
  implements JspAppReader
{
  Stack readerStack = new Stack();

  File parentFile = null;
  char fileSep;

  public FileIncludeReader()
  {
  }

  public FileIncludeReader(String fn)
    throws IOException
  {
    String tmps = fn.replace('\\', File.separatorChar);
    tmps = tmps.replace('/', File.separatorChar);
    putFile(tmps);
  }

  public void putFile(String fn)
    throws IOException
  {
    if ((fn == null) || (fn.equals("")))
      return;
    Object[] tmpo = (Object[])null;
    if (!this.readerStack.empty())
    {
      tmpo = (Object[])this.readerStack.peek();
      this.parentFile = ((File)tmpo[0]);
    }

    File f = new File(this.parentFile, fn);
    tmpo = new Object[2];
    tmpo[0] = f.getParentFile();

    FileInputStream fis = new FileInputStream(f);
    InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
    tmpo[1] = isr;
    this.readerStack.push(tmpo);
  }

  public boolean isEnd()
  {
    return this.readerStack.empty();
  }

  public int read()
    throws IOException
  {
    if (this.readerStack.empty())
    {
      return -1;
    }

    Object[] tmpo = (Object[])this.readerStack.peek();
    Reader currentReader = (Reader)tmpo[1];

    int c = currentReader.read();

    if (c == -1)
    {
      currentReader.close();
      this.readerStack.pop();
      return read();
    }

    return c;
  }

  public static void main(String[] args)
  {
    try
    {
      FileIncludeReader fir = new FileIncludeReader(args[0]);
      int c;
      while ((c = fir.read()) != -1)
      {
        int c;
        System.out.print((char)c);
      }System.out.print("#end\n");
    }
    catch (Exception localException)
    {
    }
  }
}
