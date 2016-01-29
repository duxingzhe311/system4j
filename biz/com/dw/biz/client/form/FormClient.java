package com.dw.biz.client.form;

import com.dw.biz.api.cmd.CmdXmlMsg;
import com.dw.net.MsgCmd;
import com.dw.net.MsgCmdClient;
import com.dw.system.xmldata.XmlData;

public class FormClient
{
  MsgCmdClient mcc = null;

  public FormClient(String host)
    throws Exception
  {
    this.mcc = new MsgCmdClient(host);
    this.mcc.connect();
  }

  public FormClient(String host, int port)
    throws Exception
  {
    this.mcc = new MsgCmdClient(host, port);
    this.mcc.connect();
  }

  private XmlData sendSessionXmlCmd(String session_id, String cmdname, XmlData xmlcont)
    throws Exception
  {
    return sendXmlCmd(cmdname + '@' + session_id, xmlcont);
  }

  private XmlData sendXmlCmd(String cmdname, XmlData xmlcont)
    throws Exception
  {
    MsgCmd mc = null;
    if (xmlcont != null)
      mc = CmdXmlMsg.packClientSendMsg(cmdname, xmlcont.toXmlString());
    else {
      mc = CmdXmlMsg.packClientSendMsg(cmdname, "");
    }
    MsgCmd rmc = this.mcc.sendCmd(mc);
    if (rmc == null) {
      throw new Exception("send cmd error,may be network connect broken!");
    }
    CmdXmlMsg retcxm = CmdXmlMsg.unpackClientRecvMsg(rmc);

    if (retcxm.getType() != 1)
    {
      throw new Exception(retcxm.getErrorInfo());
    }

    String retxml = retcxm.getXmlContent();
    if ((retxml == null) || (retxml.equals(""))) {
      return null;
    }
    return XmlData.parseFromXmlStr(retxml);
  }

  public boolean connect()
    throws Exception
  {
    return this.mcc.connect();
  }

  public void close()
  {
    if (this.mcc != null)
      this.mcc.close();
  }
}
