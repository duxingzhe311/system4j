package com.dw.comp.webmail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;

class ByteArrayDataSource
  implements DataSource
{
  String fileName = null;
  byte[] cont = null;

  public ByteArrayDataSource(String filename, WebMailAttachment cont) {
    this.fileName = filename;
    this.cont = cont.getContent();
  }

  public String getContentType()
  {
    return FileTypeMap.getDefaultFileTypeMap().getContentType(this.fileName);
  }

  public InputStream getInputStream() throws IOException
  {
    return new ByteArrayInputStream(this.cont);
  }

  public String getName()
  {
    return this.fileName;
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    return null;
  }
}
