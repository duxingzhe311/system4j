package com.dw.biz.client.tool_agent;

import com.dw.biz.api.Parameter;
import com.dw.biz.api.cmd.CmdXmlMsg;
import com.dw.net.IMsgCmdHandler;
import com.dw.net.MsgCmd;
import com.dw.net.MsgCmdClientInfo;
import com.dw.net.MsgCmdServer;
import com.dw.system.xmldata.XmlData;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ToolAgentService
  implements IMsgCmdHandler
{
  public static final int SERVICE_DEFAULT_PORT = 13005;
  private int port = 13005;

  MsgCmdServer server = null;

  public ToolAgentService()
  {
    this(13005);
  }

  public ToolAgentService(int port)
  {
    this.port = port;
    this.server = new MsgCmdServer(port, this);

    init();

    start();
  }

  private void init()
  {
  }

  public void start()
  {
    this.server.Start();
  }

  public void stop()
  {
    this.server.Stop();
  }

  public boolean checkConnRight(String username, String psw)
  {
    return true;
  }

  public MsgCmd OnCmd(MsgCmd mc, MsgCmdClientInfo ci)
  {
    String cmdname = "";
    try
    {
      CmdXmlMsg cxm = CmdXmlMsg.unpackServerRecvMsg(mc);
      cmdname = cxm.getCmdName();

      if (cmdname.equals("invoke_app"))
      {
        XmlData xd = XmlData.parseFromXmlStr(cxm.getXmlContent());
        OnInvokeApp(xd);

        return CmdXmlMsg.packServerReplyOkMsg(cmdname, "");
      }
      if (cmdname.equals("request_app_status"))
      {
        XmlData xd = XmlData.parseFromXmlStr(cxm.getXmlContent());
        xd = OnRequestAppStatus(xd);
        return CmdXmlMsg.packServerReplyOkMsg(cmdname, xd.toXmlString());
      }
      if (cmdname.equals("terminate_app"))
      {
        XmlData xd = XmlData.parseFromXmlStr(cxm.getXmlContent());
        OnTerminateApp(xd);

        return CmdXmlMsg.packServerReplyOkMsg(cmdname, "");
      }
      if (cmdname.equals("list_invokerinfo"))
      {
        XmlData xd = OnListInvokerInfo();
        return CmdXmlMsg.packServerReplyOkMsg(cmdname, xd.toXmlString());
      }

      return CmdXmlMsg.packServerReplyErrorMsg(cmdname, "unknow cmd=" + cmdname);
    }
    catch (Exception e)
    {
      try
      {
        return CmdXmlMsg.packServerReplyErrorMsg(cmdname, e.getMessage());
      } catch (Exception ee) {
      }
    }
    return null;
  }

  private void OnInvokeApp(XmlData xd)
    throws Exception
  {
    String application_id = xd.getParamValueStr("app_id");

    IAppInvoker ai = AppInvokerManager.getInstance().getAppInvokerById(application_id);
    if (ai == null) {
      throw new RuntimeException("cannot find app with id=" + application_id);
    }
    String proc_inst_id = xd.getParamValueStr("proc_inst_id");
    String work_item_id = xd.getParamValueStr("work_item_id");
    int app_mode = xd.getParamValueInt32("app_mode", 0);

    List xds = xd.getSubDataArray("params");
    List ps = new ArrayList();
    if ((xds != null) && (xds.size() > 0))
    {
      for (XmlData tmpxd : xds)
      {
        Parameter p = new Parameter();
        p.loadXmlData(tmpxd);
        ps.add(p);
      }
    }

    ai.OnInvokeApplication(proc_inst_id, work_item_id, ps, app_mode);
  }

  private XmlData OnRequestAppStatus(XmlData xd)
    throws Exception
  {
    String application_id = xd.getParamValueStr("app_id");

    IAppInvoker ai = AppInvokerManager.getInstance().getAppInvokerById(application_id);
    if (ai == null) {
      throw new RuntimeException("cannot find app with id=" + application_id);
    }
    String proc_inst_id = xd.getParamValueStr("proc_inst_id");
    String work_item_id = xd.getParamValueStr("work_item_id");
    List outparams = new ArrayList();
    int st = ai.OnRequestAppStatus(proc_inst_id, work_item_id, outparams);

    XmlData rxd = new XmlData();
    rxd.setParamValue("app_status", Integer.valueOf(st));

    List ps = rxd.getOrCreateSubDataArray("out_params");
    if ((outparams != null) && (outparams.size() > 0))
    {
      for (Parameter p : outparams)
      {
        ps.add(p.toXmlData());
      }
    }
    return rxd;
  }

  private XmlData OnListInvokerInfo()
    throws Exception
  {
    IAppInvoker[] ais = AppInvokerManager.getInstance().getAllAppInvokers();
    XmlData rxd = new XmlData();

    List ps = rxd.getOrCreateSubDataArray("invoker_info");
    if (ais != null)
    {
      for (IAppInvoker ai : ais)
      {
        AppInvokerInfo aii = new AppInvokerInfo(ai);
        ps.add(aii.toXmlData());
      }
    }
    return rxd;
  }

  private void OnTerminateApp(XmlData xd)
    throws Exception
  {
    String application_id = xd.getParamValueStr("app_id");

    IAppInvoker ai = AppInvokerManager.getInstance().getAppInvokerById(application_id);
    if (ai == null) {
      throw new RuntimeException("cannot find app with id=" + application_id);
    }
    String proc_inst_id = xd.getParamValueStr("proc_inst_id");
    String work_item_id = xd.getParamValueStr("work_item_id");

    ai.OnTerminateApplication(proc_inst_id, work_item_id);
  }

  public static void main(String[] args)
    throws Exception
  {
    BufferedReader in = new BufferedReader(
      new InputStreamReader(
      System.in));

    ToolAgentService taService = new ToolAgentService();
    String inputLine;
    while ((inputLine = in.readLine()) != null)
      try
      {
        String inputLine;
        StringTokenizer st = new StringTokenizer(inputLine, " ", false);
        String[] cmds = new String[st.countTokens()];
        for (int i = 0; i < cmds.length; i++)
        {
          cmds[i] = st.nextToken();
        }

        if (!"test".equals(cmds[0]))
        {
          if ("list_app".equals(cmds[0]))
          {
            IAppInvoker[] ais = AppInvokerManager.getInstance().getAllAppInvokers();
            for (IAppInvoker ai : ais)
            {
              System.out.println(ai.getAppId());
            }
          }
          else if ("exit".equals(cmds[0]))
          {
            System.exit(0);
          }
        }
      }
      catch (Exception _e) {
        _e.printStackTrace();
      }
  }
}
