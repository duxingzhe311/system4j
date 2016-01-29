package com.dw.biz.platform.compiler.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class JikesJavaCompiler
  implements JavaCompiler
{
  static final int OUTPUT_BUFFER_SIZE = 1024;
  static final int BUFFER_SIZE = 512;
  String encoding;
  String classpath;
  String compilerPath = "javac";
  String outdir;
  OutputStream out;
  boolean classDebugInfo = false;

  public JikesJavaCompiler()
  {
    Properties systemProp = System.getProperties();
    this.classpath = systemProp.getProperty("java.class.path");
    this.outdir = systemProp.getProperty("user.dir");
    this.encoding = systemProp.getProperty("file.encoding");
  }

  public void setCompilerPath(String compilerPath)
  {
    this.compilerPath = compilerPath;
  }

  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

  public void setClasspath(String classpath)
  {
    this.classpath = classpath;
  }

  public void setOutputDir(String outdir)
  {
    this.outdir = outdir;
  }

  public void setMsgOutput(OutputStream out)
  {
    this.out = out;
  }

  public void setClassDebugInfo(boolean classDebugInfo)
  {
    this.classDebugInfo = classDebugInfo;
  }

  public boolean compile(String source)
  {
    int exitValue = -1;
    String[] compilerCmd;
    String[] compilerCmd;
    if (this.classDebugInfo)
    {
      compilerCmd = 
        new String[] { 
        this.compilerPath, 
        "-g", 
        "-encoding", this.encoding, 
        "-classpath", this.classpath, 
        "-d", this.outdir, 
        "-nowarn", 
        source };
    }
    else
    {
      compilerCmd = 
        new String[] { 
        this.compilerPath, 
        "-encoding", this.encoding, 
        "-classpath", this.classpath, 
        "-d", this.outdir, 
        "-nowarn", 
        source };
    }

    ByteArrayOutputStream tmpErr = new ByteArrayOutputStream(1024);
    try
    {
      Process p = Runtime.getRuntime().exec(compilerCmd);

      BufferedInputStream compilerErr = 
        new BufferedInputStream(p.getErrorStream());

      StreamPumper errPumper = new StreamPumper(compilerErr, tmpErr);

      errPumper.start();

      p.waitFor();
      exitValue = p.exitValue();

      errPumper.join();
      compilerErr.close();

      p.destroy();

      tmpErr.close();
      tmpErr.writeTo(this.out);
    }
    catch (IOException ioe)
    {
      return false;
    }
    catch (InterruptedException ie)
    {
      return false;
    }
    Process p;
    boolean isOkay = exitValue == 0;

    if (tmpErr.size() > 0)
    {
      isOkay = false;
    }
    return isOkay;
  }

  public static void main(String[] args)
  {
    try
    {
      JikesJavaCompiler sjc = new JikesJavaCompiler();
      sjc.setMsgOutput(System.out);

      File f = new File(args[0]);
      byte[] buf = new byte[(int)f.length()];
      int i = args[0].indexOf('.');
      FileInputStream fis = new FileInputStream(f);
      fis.read(buf);
      fis.close();

      if (sjc.compile(new String(buf)))
        System.out.println("succeed");
      else {
        System.out.println("failed");
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  class StreamPumper extends Thread
  {
    private BufferedInputStream stream;
    private boolean endOfStream = false;
    private boolean stopSignal = false;
    private int SLEEP_TIME = 5;
    private OutputStream out;

    public StreamPumper(BufferedInputStream is, OutputStream out)
    {
      this.stream = is;
      this.out = out;
    }

    public void pumpStream()
      throws IOException
    {
      byte[] buf = new byte[512];
      if (!this.endOfStream)
      {
        int bytesRead = this.stream.read(buf, 0, 512);

        if (bytesRead > 0)
        {
          this.out.write(buf, 0, bytesRead);
        }
        else if (bytesRead == -1)
        {
          this.endOfStream = true;
        }
      }
    }

    public void run()
    {
      try
      {
        while (!this.endOfStream)
        {
          pumpStream();
          sleep(this.SLEEP_TIME);
        }
      }
      catch (InterruptedException localInterruptedException)
      {
      }
      catch (IOException localIOException)
      {
      }
    }
  }
}
