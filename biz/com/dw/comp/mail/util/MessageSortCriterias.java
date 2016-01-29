package com.dw.comp.mail.util;

public abstract interface MessageSortCriterias
{
  public static final int RECEIVE_CHRONOLOGICAL = 0;
  public static final int RECEIVE_REVERSE_CHRONOLOGICAL = 1;
  public static final int SEND_CHRONOLOGICAL = 2;
  public static final int SEND_REVERSE_CHRONOLOGICAL = 3;
  public static final int RECEIVER_LEXOGRAPHICAL = 4;
  public static final int RECEIVER_REVERSE_LEXOGRAPHICAL = 5;
  public static final int SENDER_LEXOGRAPHICAL = 6;
  public static final int SENDER_REVERSE_LEXOGRAPHICAL = 7;
  public static final int NUMBER_NUMERICAL = 8;
  public static final int NUMBER_REVERSE_NUMERICAL = 9;
  public static final int DATE_CHRONOLOGICAL = 10;
  public static final int DATE_REVERSE_CHRONOLOGICAL = 11;
  public static final int WHO_LEXOGRAPHICAL = 12;
  public static final int WHO_REVERSE_LEXOGRAPHICAL = 13;
  public static final int[] EXUI_CRITERIAS = { 
    10, 
    11, 
    8, 
    9, 
    12, 
    13 };

  public static final String[] EXUI_CRITERIAS_STR = { 
    "messages.sort.oldestfirst", 
    "messages.sort.recentfirst", 
    "messages.sort.numberincreasing", 
    "messages.sort.numberdecreasing", 
    "messages.sort.wholexographical", 
    "messages.sort.whoreverselexographical" };
}
