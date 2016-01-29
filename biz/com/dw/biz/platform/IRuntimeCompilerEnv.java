package com.dw.biz.platform;

import com.dw.biz.platform.compiler.JspAppReader;
import java.io.IOException;

public abstract interface IRuntimeCompilerEnv
{
  public abstract boolean judgeNewJsp();

  public abstract String getTargetJavaPath();

  public abstract ClassLoader getEnvClassLoader();

  public abstract String getEnvClassPath();

  public abstract String getTargetClassBase();

  public abstract String getClassName();

  public abstract String getExtClassName();

  public abstract String getContentStr();

  public abstract JspAppReader getJspAppReader()
    throws IOException;

  public abstract BizClassLoader createNewBizClassLoader();

  public abstract BizClassLoader getRecentBizClassLoader();
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.platform.IRuntimeCompilerEnv
 * JD-Core Version:    0.6.2
 */