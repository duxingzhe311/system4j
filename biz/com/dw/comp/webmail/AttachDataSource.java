package com.dw.comp.webmail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;

class AttachDataSource
  implements DataSource
{
  String fileName = null;
  WebMailAttachment cont = null;

  public AttachDataSource(String filename, WebMailAttachment cont) {
    this.fileName = filename;
    this.cont = cont;
  }

  public String getContentType()
  {
    return FileTypeMap.getDefaultFileTypeMap().getContentType(this.fileName);
  }

  public InputStream getInputStream() throws IOException
  {
    if (this.cont.getContent() != null)
      return new ByteArrayInputStream(this.cont.getContent());
    if (this.cont.getStreamContent() != null) {
      return this.cont.getStreamContent();
    }
    return null;
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
