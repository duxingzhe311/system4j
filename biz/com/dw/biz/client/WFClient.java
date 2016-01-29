package com.dw.biz.client;

import com.dw.biz.api.cmd.CmdXmlClientServerHelper;
import com.dw.net.MsgCmdClient;
import com.dw.system.xmldata.XmlData;

public class WFClient
{
  public static final int DEFAULT_PORT = 55322;
  MsgCmdClient mcc = null;

  public WFClient(String host)
    throws Exception
  {
    this.mcc = new MsgCmdClient(host);
    this.mcc.connect();
  }

  public WFClient(String host, int port)
    throws Exception
  {
    this.mcc = new MsgCmdClient(host, port);
    this.mcc.connect();
  }

  public WFClient(String host, int port, String usern, String psw)
    throws Exception
  {
    this.mcc = new MsgCmdClient(host, port, usern, psw);
    this.mcc.connect();
  }

  public XmlData sendLoginUserXmlCmd(String username, int phase_id, String cmdname, XmlData xmlcont)
    throws Exception
  {
    return CmdXmlClientServerHelper.sendXmlCmd(this.mcc, null, phase_id, username, cmdname, xmlcont);
  }

  public XmlData sendXmlCmd(String cmdname, XmlData xmlcont)
    throws Exception
  {
    return CmdXmlClientServerHelper.sendXmlCmd(this.mcc, null, -1, null, cmdname, xmlcont);
  }

  public boolean connect()
    throws Exception
  {
    return this.mcc.connect();
  }

  public boolean isClosed()
  {
    if (this.mcc == null) {
      return true;
    }
    return this.mcc.isClosed();
  }

  public void close()
  {
    if (this.mcc != null)
      this.mcc.close();
  }
}
