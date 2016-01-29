package com.dw.comp.webmail.inner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class JamesHeadBodyInputStream extends InputStream
{
  byte[] heads = null;
  FileInputStream bodyf = null;

  int curp = 0;

  public JamesHeadBodyInputStream(byte[] heads, FileInputStream bodyf)
  {
    this.heads = heads;
    this.bodyf = bodyf;
  }

  public int read()
    throws IOException
  {
    if (this.curp < this.heads.length)
    {
      int r = this.heads[this.curp];
      this.curp += 1;
      return r;
    }

    if (this.bodyf == null) {
      return -1;
    }
    return this.bodyf.read();
  }
}
