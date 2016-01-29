package com.dw.biz.bus;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import java.util.Date;

public class BizBusData
  implements IXmlDataable
{
  private String dataType = null;
  private XmlData dataCont = null;

  private String sentAdapter = null;
  private String senderName = null;

  private Date sentDate = null;

  public BizBusData()
  {
  }

  public BizBusData(String datatype, XmlData data, String sent_adp, String sender) {
    this.dataType = datatype;
    this.dataCont = data;

    this.sentAdapter = sent_adp;
    this.senderName = sender;
    this.sentDate = new Date();
  }

  public String getDataType()
  {
    return this.dataType;
  }

  public XmlData getDataContent()
  {
    return this.dataCont;
  }

  public String getSentAdapter()
  {
    return this.sentAdapter;
  }

  public String getSenderName()
  {
    return this.senderName;
  }

  public Date getSentDate()
  {
    return this.sentDate;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("data_type", this.dataType);
    if (this.dataCont != null) {
      xd.setSubDataSingle("cont", this.dataCont);
    }
    if (this.sentAdapter != null) {
      xd.setParamValue("sent_adp", this.sentAdapter);
    }
    if (this.senderName != null) {
      xd.setParamValue("sender", this.senderName);
    }
    if (this.sentDate != null) {
      xd.setParamValue("sent_date", this.sentDate);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.dataType = xd.getParamValueStr("data_type");
    this.dataCont = xd.getSubDataSingle("cont");

    this.sentAdapter = xd.getParamValueStr("sent_adp");
    this.senderName = xd.getParamValueStr("sender");

    this.sentDate = xd.getParamValueDate("sent_date", null);
  }
}
