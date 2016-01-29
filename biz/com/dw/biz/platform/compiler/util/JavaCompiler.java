package com.dw.biz.platform.compiler.util;

import java.io.OutputStream;

public abstract interface JavaCompiler
{
  public abstract void setCompilerPath(String paramString);

  public abstract void setEncoding(String paramString);

  public abstract void setClasspath(String paramString);

  public abstract void setOutputDir(String paramString);

  public abstract void setMsgOutput(OutputStream paramOutputStream);

  public abstract void setClassDebugInfo(boolean paramBoolean);

  public abstract boolean compile(String paramString)
    throws Throwable;
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.platform.compiler.util.JavaCompiler
 * JD-Core Version:    0.6.2
 */