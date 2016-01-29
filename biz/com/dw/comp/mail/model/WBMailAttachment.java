package com.dw.comp.mail.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

public class WBMailAttachment
{
  byte[] content = null;
  String contentType = null;
  String fileName = null;

  WBMailAttachment()
  {
  }

  public WBMailAttachment(String fn, String conttype, ByteArrayOutputStream cont)
  {
    this.fileName = fn;
    this.contentType = conttype;
    if (this.contentType == null)
      this.contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(this.fileName);
    this.content = cont.toByteArray();
  }

  public WBMailAttachment(String fn, InputStream inputs) throws IOException
  {
    this.fileName = fn;
    this.contentType = FileTypeMap.getDefaultFileTypeMap().getContentType(this.fileName);
    ByteArrayOutputStream tmpc = new ByteArrayOutputStream();
    byte[] cont = new byte[1024];
    int len;
    if ((len = inputs.read(cont)) >= 0)
    {
      tmpc.write(cont, 0, len);
    }
    this.content = tmpc.toByteArray();
  }

  public String getFileName()
  {
    return this.fileName;
  }

  public String getContentType()
  {
    return this.contentType;
  }

  public byte[] getContent()
  {
    return this.content;
  }

  public MimeBodyPart toMimeBodyPart() throws MessagingException
  {
    MimeBodyPart mbp = new MimeBodyPart();

    DataSource ds = new DS();

    mbp.setDataHandler(new DataHandler(ds));
    mbp.setFileName(this.fileName);
    return mbp;
  }

  class DS implements DataSource {
    DS() {
    }

    public String getContentType() {
      return getContentType();
    }

    public InputStream getInputStream() throws IOException
    {
      if (WBMailAttachment.this.content == null) {
        return new ByteArrayInputStream(new byte[0]);
      }
      return new ByteArrayInputStream(WBMailAttachment.this.content);
    }

    public String getName()
    {
      return WBMailAttachment.this.fileName;
    }

    public OutputStream getOutputStream() throws IOException
    {
      return null;
    }
  }
}
