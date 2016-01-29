package com.dw.biz.bus;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.ArrayList;
import java.util.List;

public abstract class BizBusAdapter
{
  private boolean bCanSend = true;
  private boolean bCanRecv = true;

  private ArrayList<BizBusDataSender> senders = new ArrayList();

  private ArrayList<BizBusDataRecver> recvers = new ArrayList();

  public BizBusAdapter()
  {
    initDefault();
  }

  private void initDefault()
  {
    this.bCanSend = canSendDefault();
    this.bCanRecv = canRecvDefault();
  }

  public abstract String getName();

  public abstract String getDesc();

  public List<BizBusDataSender> getBusSenders()
  {
    return null;
  }

  public List<BizBusDataSender> getBusRecvers()
  {
    return null;
  }

  public final boolean canSend()
  {
    return this.bCanSend;
  }

  public final boolean canRecv()
  {
    return this.bCanRecv;
  }

  protected boolean canSendDefault()
  {
    return true;
  }

  protected boolean canRecvDefault()
  {
    return true;
  }

  public void setConfigInfo(BizBusAdpConfig config)
  {
    if (config == null)
    {
      initDefault();
      return;
    }

    this.bCanSend = config.canSend();
    this.bCanRecv = config.canRecv();
  }

  public class DataSender
  {
    public DataSender()
    {
    }

    public void sendData(String line_name, String datatype, XmlData data)
    {
    }
  }

  @XRmi(reg_name="bizbus_adp_config")
  public static class BizBusAdpConfig
    implements IXmlDataable
  {
    private boolean canSend = true;
    private boolean canRecv = true;

    public boolean canSend()
    {
      return this.canSend;
    }

    public boolean canRecv()
    {
      return this.canRecv;
    }

    public void setCanSend(boolean bcan_send)
    {
      this.canSend = bcan_send;
    }

    public void setCanRecv(boolean bcan_recv)
    {
      this.canRecv = bcan_recv;
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("can_send", Boolean.valueOf(this.canSend));
      xd.setParamValue("can_send", Boolean.valueOf(this.canRecv));
      return xd;
    }

    public void fromXmlData(XmlData xd)
    {
      this.canSend = xd.getParamValueBool("can_send", this.canSend).booleanValue();
      this.canRecv = xd.getParamValueBool("can_recv", this.canRecv).booleanValue();
    }
  }
}
