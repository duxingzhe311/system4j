package com.dw.biz.platform.compiler.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class BizJavaCompiler
  implements JavaCompiler
{
  static BizJavaCompiler ozCompiler = null;
  static final int OUTPUT_BUFFER_SIZE = 1024;
  static final int BUFFER_SIZE = 512;
  String encoding;
  String classp = "";
  String compilerPath = "./jdk/bin/javac";
  String outdir;
  OutputStream out;
  boolean classDebugInfo = false;
  Properties systemProp = null;

  public static BizJavaCompiler getCompiler()
  {
    if (ozCompiler == null)
      ozCompiler = new BizJavaCompiler();
    return ozCompiler;
  }

  private BizJavaCompiler()
  {
    this.systemProp = System.getProperties();

    this.outdir = this.systemProp.getProperty("user.dir");
    File tmpf = new File(this.outdir);
    if (!tmpf.exists()) {
      tmpf.mkdirs();
    }
    this.compilerPath = (this.systemProp.getProperty("jdk.home", "./jdk") + "/bin/javac");

    this.encoding = this.systemProp.getProperty("file.encoding");
    this.out = System.out;
  }

  public BizJavaCompiler(String outdir, OutputStream output, String ext_classpath)
  {
    this.systemProp = System.getProperties();

    this.outdir = outdir;
    File tmpf = new File(outdir);
    if (!tmpf.exists()) {
      tmpf.mkdirs();
    }
    this.compilerPath = (this.systemProp.getProperty("jdk.home", "./jdk") + "/bin/javac");

    this.classp = ext_classpath;
    this.encoding = this.systemProp.getProperty("file.encoding");
    this.out = output;
  }

  public String calClassPath()
  {
    String ps = System.getProperty("path.separator");
    if ((this.classp == null) || (this.classp.equals(""))) {
      return this.systemProp.getProperty("java.class.path") + ps + this.outdir;
    }
    if (this.classp.startsWith(ps)) {
      return this.systemProp.getProperty("java.class.path") + ps + this.outdir + this.classp;
    }
    return this.systemProp.getProperty("java.class.path") + ps + this.outdir + ps + this.classp;
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
    this.classp = classpath;
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
    throws Exception
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
        "-classpath", calClassPath(), 
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
        "-classpath", calClassPath(), 
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

      if (exitValue != 0)
      {
        throw new Exception(tmpErr.toString());
      }

      tmpErr.writeTo(this.out);
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
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
      BizJavaCompiler sjc = getCompiler();
      sjc.setOutputDir(args[1]);
      if (sjc.compile(args[0]))
        System.out.println("succeed");
      else
        System.out.println("failed");
    }
    catch (Throwable e)
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
