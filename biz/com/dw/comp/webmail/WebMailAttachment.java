package com.dw.comp.webmail;

import java.io.InputStream;

public class WebMailAttachment
{
  String fileName = null;
  String contentId = null;
  byte[] cont = null;
  InputStream streamCont = null;

  boolean bInner = false;

  public WebMailAttachment(String filename, String contid, byte[] cont, boolean binner)
  {
    this.fileName = filename;
    this.contentId = contid;
    this.cont = cont;
    this.bInner = binner;
  }

  public WebMailAttachment(String filename, String contid, byte[] cont)
  {
    this.fileName = filename;
    this.contentId = contid;
    this.cont = cont;
    this.bInner = false;
  }

  public WebMailAttachment(String filename, byte[] cont)
  {
    this.fileName = filename;
    this.cont = cont;
  }

  public WebMailAttachment(String filename, InputStream cont)
  {
    this.fileName = filename;
    this.streamCont = cont;
  }

  public boolean isInner()
  {
    return this.bInner;
  }

  public String getFileName()
  {
    return this.fileName;
  }

  public String getContentId()
  {
    return this.contentId;
  }

  public byte[] getContent()
  {
    return this.cont;
  }

  public InputStream getStreamContent()
  {
    return this.streamCont;
  }
}
