package com.dw.biz.api.cmd;

import com.dw.net.MsgCmd;

public class CmdXmlMsg
{
  public static final int TYPE_REQ = 0;
  public static final int TYPE_RESP_OK = 1;
  public static final int TYPE_RESP_ERROR = 2;
  int cmdType = 0;
  String cmdName = null;

  String xmlCont = null;
  String errorInfo = null;

  public static MsgCmd packClientSendMsg(String cmdname, String xml_cont)
    throws Exception
  {
    byte[] scont = (byte[])null;
    if (xml_cont != null)
      scont = xml_cont.getBytes("UTF-8");
    else {
      scont = new byte[0];
    }
    return new MsgCmd("wf_cmd " + cmdname, scont);
  }

  public static CmdXmlMsg unpackClientRecvMsg(MsgCmd mc)
    throws Exception
  {
    if (mc == null) {
      return null;
    }
    String replycmd = mc.getCmd();

    if (replycmd.startsWith("wf_cmd_ok "))
    {
      String cmdn = replycmd.substring("wf_cmd_ok ".length());
      CmdXmlMsg cmp = new CmdXmlMsg();
      cmp.cmdName = cmdn;
      cmp.cmdType = 1;

      byte[] rcont = mc.getContent();
      if ((rcont != null) || (rcont.length > 0)) {
        cmp.xmlCont = new String(rcont, "UTF-8");
      }
      return cmp;
    }

    CmdXmlMsg cmp = new CmdXmlMsg();
    if (replycmd.startsWith("wf_cmd_err ")) {
      cmp.cmdName = replycmd.substring("wf_cmd_err ".length());
    }
    cmp.cmdType = 2;

    byte[] rcont = mc.getContent();
    if (rcont != null)
    {
      cmp.errorInfo = new String(rcont, "UTF-8");
    }

    return cmp;
  }

  public static CmdXmlMsg unpackServerRecvMsg(MsgCmd mc)
    throws Exception
  {
    String replycmd = mc.getCmd();

    if (!replycmd.startsWith("wf_cmd ")) {
      return null;
    }
    String cmdn = replycmd.substring("wf_cmd ".length());
    CmdXmlMsg cmp = new CmdXmlMsg();
    cmp.cmdName = cmdn;

    byte[] rcont = mc.getContent();
    if ((rcont != null) || (rcont.length > 0)) {
      cmp.xmlCont = new String(rcont, "UTF-8");
    }
    return cmp;
  }

  public static MsgCmd packServerReplyOkMsg(String cmdname, String reply_xml_cont)
    throws Exception
  {
    byte[] scont = (byte[])null;
    if (reply_xml_cont != null)
      scont = reply_xml_cont.getBytes("UTF-8");
    else {
      scont = new byte[0];
    }
    return new MsgCmd("wf_cmd_ok " + cmdname, scont);
  }

  public static MsgCmd packServerReplyErrorMsg(String cmdname, String errorinfo)
    throws Exception
  {
    byte[] scont = (byte[])null;
    if (errorinfo != null)
      scont = errorinfo.getBytes("UTF-8");
    else {
      scont = new byte[0];
    }
    if (cmdname == null) {
      cmdname = "";
    }
    return new MsgCmd("wf_cmd_err " + cmdname, scont);
  }

  public int getType()
  {
    return this.cmdType;
  }

  public String getCmdName()
  {
    return this.cmdName;
  }

  public String getXmlContent()
  {
    return this.xmlCont;
  }

  public String getErrorInfo()
  {
    return this.errorInfo;
  }
}
