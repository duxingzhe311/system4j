package com.dw.comp.mail.model;

import java.util.Date;
import java.util.HashMap;

public abstract interface WBMailMsg
{
  public abstract String getFullHeader();

  public abstract int getMessageNumber();

  public abstract boolean isReceived();

  public abstract boolean isSent();

  public abstract String getFrom();

  public abstract String getReplyTo();

  public abstract String getTo();

  public abstract String getCCTo();

  public abstract String getBCCTo();

  public abstract Date getDate();

  public abstract Date getReceivedDate();

  public abstract Date getSentDate();

  public abstract String getSubject();

  public abstract String getTxtBody();

  public abstract String getHtmlBody();

  public abstract HashMap<Integer, String> getAppendFileInfo();

  public abstract boolean isSinglepart();

  public abstract boolean isMultipart();
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.comp.mail.model.WBMailMsg
 * JD-Core Version:    0.6.2
 */