package com.dw.biz.api.cmd;

import com.dw.net.MsgCmd;
import com.dw.net.MsgCmdClient;
import com.dw.system.xmldata.XmlData;

public class CmdXmlClientServerHelper
{
  public static XmlData sendXmlCmd(MsgCmdClient mcc, String session_id, int phase_id, String user_name, String cmdname, XmlData xmlcont)
    throws Exception
  {
    if (session_id == null) {
      session_id = "";
    }
    if (user_name == null) {
      user_name = "";
    }
    String strcmd = cmdname + '@' + session_id + '$' + phase_id + '#' + user_name;
    return sendXmlCmd(mcc, strcmd, xmlcont);
  }

  private static XmlData sendXmlCmd(MsgCmdClient mcc, String cmdname, XmlData xmlcont)
    throws Exception
  {
    MsgCmd mc = null;
    if (xmlcont != null)
      mc = CmdXmlMsg.packClientSendMsg(cmdname, xmlcont.toXmlString());
    else {
      mc = CmdXmlMsg.packClientSendMsg(cmdname, "");
    }
    long st = System.currentTimeMillis();
    MsgCmd rmc = mcc.sendCmd(mc);
    long et = System.currentTimeMillis();

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

  public static ClientXmlCmd recvClientCmd(MsgCmd mc)
    throws Exception
  {
    long st = System.currentTimeMillis();

    CmdXmlMsg cmp = CmdXmlMsg.unpackServerRecvMsg(mc);

    long st1 = System.currentTimeMillis();

    if (cmp == null)
    {
      throw new Exception("Cannot unpack server recv msg!");
    }

    ClientXmlCmd ret = new ClientXmlCmd();

    String tmpcmd = cmp.getCmdName();

    int ps = tmpcmd.indexOf('@');
    ret.cmdName = tmpcmd.substring(0, ps);
    int pp = tmpcmd.indexOf('$', ps);
    ret.sessionId = tmpcmd.substring(ps + 1, pp);
    int pu = tmpcmd.indexOf('#', pp);
    String strph = tmpcmd.substring(pp + 1, pu);
    if ((strph != null) && (!strph.equals("")))
      ret.phaseId = Integer.parseInt(strph);
    ret.userName = tmpcmd.substring(pu + 1);

    String inputxml = cmp.getXmlContent();

    if ((inputxml != null) && (!inputxml.equals(""))) {
      ret.xmlData = XmlData.parseFromXmlStr(inputxml);
    }
    long st2 = System.currentTimeMillis();

    return ret;
  }

  public static MsgCmd packServerOkReplyCmd(ClientXmlCmd cxc, XmlData rxd)
    throws Exception
  {
    long st5 = System.currentTimeMillis();
    MsgCmd retmc = null;
    if (rxd == null)
      retmc = CmdXmlMsg.packServerReplyOkMsg(cxc.cmdName, "");
    else
      retmc = CmdXmlMsg.packServerReplyOkMsg(cxc.cmdName, 
        rxd.toXmlString());
    long st6 = System.currentTimeMillis();

    return retmc;
  }

  public static MsgCmd packServerErrorReplyCmd(ClientXmlCmd cxc, String errorinfo)
    throws Exception
  {
    return CmdXmlMsg.packServerReplyErrorMsg(cxc.cmdName, errorinfo);
  }

  public static class ClientXmlCmd
  {
    public String sessionId = null;
    public int phaseId = -1;
    public String userName = null;
    public String cmdName = null;
    public XmlData xmlData = null;

    public String toSimpleStr()
    {
      return this.cmdName + " phaseid=" + this.phaseId + " session=" + this.sessionId;
    }
  }
}
