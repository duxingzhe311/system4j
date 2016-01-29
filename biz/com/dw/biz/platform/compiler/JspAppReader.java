package com.dw.biz.platform.compiler;

import java.io.IOException;

public abstract interface JspAppReader
{
  public abstract int read()
    throws IOException;

  public abstract boolean isEnd();
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.platform.compiler.JspAppReader
 * JD-Core Version:    0.6.2
 */