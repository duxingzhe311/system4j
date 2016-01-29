package com.dw.biz.bus;

public class BizBusDataSender
{
  String name = null;

  String busLine = null;

  String dataType = null;

  public BizBusDataSender()
  {
  }

  public BizBusDataSender(String n, String busline, String datatype) {
    this.name = n;
    this.busLine = busline;
    this.dataType = datatype;
  }

  public String getName()
  {
    return this.name;
  }

  public String getBusLine()
  {
    return this.busLine;
  }

  public String getDataType()
  {
    return this.dataType;
  }
}
