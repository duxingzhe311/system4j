package com.dw.comp.mail.util;

import com.dw.comp.mail.model.WBMailMsgInfo;
import java.util.Comparator;
import java.util.Date;

public class MessageSortingUtil
{
  public static final Comparator DATE_CHRONOLOGICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg1.getDate().compareTo(
        msg2.getDate());
    }
  };

  public static final Comparator DATE_REVERSE_CHRONOLOGICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg2.getDate().compareTo(
        msg1.getDate());
    }
  };

  public static final Comparator RECEIVE_CHRONOLOGICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg1.getReceivedDate().compareTo(
        msg2.getReceivedDate());
    }
  };

  public static final Comparator RECEIVE_REVERSE_CHRONOLOGICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg2.getReceivedDate().compareTo(
        msg1.getReceivedDate());
    }
  };

  public static final Comparator SEND_CHRONOLOGICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg1.getSentDate().compareTo(
        msg2.getSentDate());
    }
  };

  public static final Comparator SEND_REVERSE_CHRONOLOGICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg2.getSentDate().compareTo(
        msg1.getSentDate());
    }
  };

  public static final Comparator WHO_LEXOGRAPHICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg1.getWho().compareTo(
        msg2.getWho());
    }
  };

  public static final Comparator WHO_REVERSE_LEXOGRAPHICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg2.getWho().compareTo(
        msg1.getWho());
    }
  };

  public static final Comparator SENDER_LEXOGRAPHICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg1.getFrom().compareTo(
        msg2.getFrom());
    }
  };

  public static final Comparator SENDER_REVERSE_LEXOGRAPHICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg2.getFrom().compareTo(
        msg1.getFrom());
    }
  };

  public static final Comparator RECEIVER_LEXOGRAPHICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg1.getTo().compareTo(
        msg2.getTo());
    }
  };

  public static final Comparator RECEIVER_REVERSE_LEXOGRAPHICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg2.getTo().compareTo(
        msg1.getTo());
    }
  };

  public static final Comparator NUMBER_NUMERICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg1.getNumberForSort().compareTo(
        msg2.getNumberForSort());
    }
  };

  public static final Comparator NUMBER_REVERSE_NUMERICAL = new Comparator()
  {
    public int compare(Object o1, Object o2) {
      WBMailMsgInfo msg1 = (WBMailMsgInfo)o1;
      WBMailMsgInfo msg2 = (WBMailMsgInfo)o2;
      return msg2.getNumberForSort().compareTo(
        msg1.getNumberForSort());
    }
  };

  public static final Comparator[] CRITERIA_COMPARATOR = { 
    RECEIVE_CHRONOLOGICAL, 
    RECEIVE_REVERSE_CHRONOLOGICAL, 
    SEND_CHRONOLOGICAL, 
    SEND_REVERSE_CHRONOLOGICAL, 
    RECEIVER_LEXOGRAPHICAL, 
    RECEIVER_REVERSE_LEXOGRAPHICAL, 
    SENDER_LEXOGRAPHICAL, 
    SENDER_REVERSE_LEXOGRAPHICAL, 
    NUMBER_NUMERICAL, 
    NUMBER_REVERSE_NUMERICAL, 
    DATE_CHRONOLOGICAL, 
    DATE_REVERSE_CHRONOLOGICAL, 
    WHO_LEXOGRAPHICAL, 
    WHO_REVERSE_LEXOGRAPHICAL };
}
