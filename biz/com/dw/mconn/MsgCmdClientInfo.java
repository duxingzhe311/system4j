package com.dw.mconn;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;

@XRmi(reg_name="msgcmd_clientinfo")
public class MsgCmdClientInfo
  implements IXmlDataable
{
  private String clientIP = null;
  private int port = -1;

  String loginUser = null;

  public MsgCmdClientInfo()
  {
  }

  public MsgCmdClientInfo(String addrip, int p, String luser) {
    this.clientIP = addrip;
    this.port = p;
    this.loginUser = luser;
  }

  public String getClientIPAddr()
  {
    return this.clientIP;
  }

  public int getClientPort()
  {
    return this.port;
  }

  public String getConnUserName()
  {
    if (this.loginUser == null) {
      return "";
    }
    return this.loginUser;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    xd.setParamValue("client_ip", this.clientIP);
    xd.setParamValue("client_port", Integer.valueOf(this.port));
    if (this.loginUser != null) {
      xd.setParamValue("conn_user", this.loginUser);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.clientIP = xd.getParamValueStr("client_ip");
    this.port = xd.getParamValueInt32("client_port", -1);
    this.loginUser = xd.getParamValueStr("conn_user");
  }
}
