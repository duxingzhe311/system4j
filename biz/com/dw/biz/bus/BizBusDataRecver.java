package com.dw.biz.bus;

public abstract class BizBusDataRecver
{
  String name = null;

  String busLine = null;

  String dataType = null;

  public BizBusDataRecver()
  {
  }

  public BizBusDataRecver(String n, String busline, String datatype) {
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

  public abstract void OnBusDataRecved(BizBusData paramBizBusData);
}
