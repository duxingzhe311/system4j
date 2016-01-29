package com.dw.biz.platform.compiler;

import java.io.IOException;

public class StringJspAppReader
  implements JspAppReader
{
  String strCont = null;
  int strLen = -1;
  int p = 0;

  public StringJspAppReader(String strcont)
  {
    if (strcont == null) {
      throw new IllegalArgumentException("str cont cannot be null");
    }
    this.strCont = strcont;
    this.strLen = this.strCont.length();
  }

  public int read() throws IOException
  {
    if (this.p >= this.strLen) {
      return -1;
    }
    char c = this.strCont.charAt(this.p);
    this.p += 1;
    return c;
  }

  public boolean isEnd()
  {
    return this.p >= this.strLen;
  }
}
