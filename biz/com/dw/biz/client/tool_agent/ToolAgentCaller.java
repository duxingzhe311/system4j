package com.dw.biz.client.tool_agent;

import com.dw.biz.api.Parameter;
import com.dw.biz.api.cmd.CmdXmlMsg;
import com.dw.net.MsgCmd;
import com.dw.net.MsgCmdClient;
import com.dw.system.xmldata.XmlData;
import java.util.ArrayList;
import java.util.List;

public class ToolAgentCaller
{
  MsgCmdClient cmdClient = null;

  public ToolAgentCaller(String host)
    throws Exception
  {
    this(host, 13005);
  }

  public ToolAgentCaller(String host, int port)
    throws Exception
  {
    this.cmdClient = new MsgCmdClient(host, port);
    connect();
  }

  public boolean connect()
    throws Exception
  {
    return this.cmdClient.connect();
  }

  CmdXmlMsg sendXmlCmd(String cmdname, String xmlcont)
    throws Exception
  {
    MsgCmd mc = CmdXmlMsg.packClientSendMsg(cmdname, xmlcont);
    MsgCmd rmc = this.cmdClient.sendCmd(mc);
    if (rmc == null) {
      throw new Exception("send cmd error,may be network connect broken!");
    }
    return CmdXmlMsg.unpackClientRecvMsg(rmc);
  }

  public List<AppInvokerInfo> listAppInvokerInfos()
    throws Exception
  {
    CmdXmlMsg cxm = sendXmlCmd("list_invokerinfo", "");
    if (cxm.getType() != 1)
    {
      throw new Exception(cxm.getErrorInfo());
    }

    List rets = new ArrayList();

    XmlData rxd = XmlData.parseFromXmlStr(cxm.getXmlContent());
    List ps = rxd.getSubDataArray("invoker_info");
    if ((ps != null) && (ps.size() > 0))
    {
      for (XmlData pxd : ps)
      {
        AppInvokerInfo aii = new AppInvokerInfo();
        aii.loadXmlData(pxd);

        rets.add(aii);
      }
    }
    return rets;
  }

  public void WMTAInvokeApplication(String application_id, String proc_inst_id, String work_item_id, List<Parameter> params, int app_mode)
    throws Exception
  {
    XmlData xd = new XmlData();

    xd.setParamValue("app_id", application_id);
    xd.setParamValue("proc_inst_id", proc_inst_id);
    xd.setParamValue("work_item_id", work_item_id);
    xd.setParamValue("app_mode", Integer.valueOf(app_mode));

    if ((params != null) && (params.size() > 0))
    {
      List xds = xd.getOrCreateSubDataArray("params");
      for (Parameter p : params)
      {
        xds.add(p.toXmlData());
      }
    }

    CmdXmlMsg cxm = sendXmlCmd("invoke_app", xd.toXmlString());
    if (cxm.getType() != 1)
    {
      throw new Exception(cxm.getErrorInfo());
    }
  }

  public int WMTARequestAppStatus(String application_id, String proc_inst_id, String work_item_id, List<Parameter> out_params)
    throws Exception
  {
    XmlData xd = new XmlData();

    xd.setParamValue("app_id", application_id);
    xd.setParamValue("proc_inst_id", proc_inst_id);
    xd.setParamValue("work_item_id", work_item_id);

    CmdXmlMsg cxm = sendXmlCmd("request_app_status", xd.toXmlString());
    if (cxm.getType() != 1)
    {
      throw new Exception(cxm.getErrorInfo());
    }

    XmlData rxd = XmlData.parseFromXmlStr(cxm.getXmlContent());
    int appst = rxd.getParamValueInt32("app_status", 0);
    if (appst <= 0)
    {
      throw new Exception("unknow appstatus");
    }
    List ps = rxd.getSubDataArray("out_params");
    if ((ps != null) && (ps.size() > 0))
    {
      for (XmlData pxd : ps)
      {
        Parameter p = new Parameter();
        p.loadXmlData(pxd);
        out_params.add(p);
      }
    }
    return appst;
  }

  public void WMTATerminateApp(String app_id, String proc_inst_id, String work_item_id)
    throws Exception
  {
    XmlData xd = new XmlData();

    xd.setParamValue("app_id", app_id);
    xd.setParamValue("proc_inst_id", proc_inst_id);
    xd.setParamValue("work_item_id", work_item_id);

    CmdXmlMsg cxm = sendXmlCmd("terminate_app", xd.toXmlString());
    if (cxm.getType() != 1)
    {
      throw new Exception(cxm.getErrorInfo());
    }
  }

  public void close()
  {
    if (this.cmdClient != null)
      this.cmdClient.close();
  }
}
