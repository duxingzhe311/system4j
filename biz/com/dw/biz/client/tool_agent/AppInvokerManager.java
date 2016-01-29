package com.dw.biz.client.tool_agent;

import java.util.Collection;
import java.util.Hashtable;

public class AppInvokerManager
{
  static AppInvokerManager invokerMgr = null;

  Hashtable<String, IAppInvoker> id2invoker = null;

  public static AppInvokerManager getInstance()
  {
    if (invokerMgr != null) {
      return invokerMgr;
    }
    invokerMgr = new AppInvokerManager();
    return invokerMgr;
  }

  private AppInvokerManager()
  {
    this.id2invoker = new Hashtable();

    String appinvokers = System.getProperty("wf_app_invoker");
    if ((appinvokers != null) && (appinvokers != ""))
    {
      String[] cs = appinvokers.split(",");
      for (String s : cs)
      {
        try
        {
          Class c = Class.forName(s);
          IAppInvoker ai = (IAppInvoker)c.newInstance();
          this.id2invoker.put(ai.getAppId(), ai);
        }
        catch (Exception ee)
        {
          ee.printStackTrace();
        }
      }
    }
  }

  public IAppInvoker getAppInvokerById(String appid)
  {
    return (IAppInvoker)this.id2invoker.get(appid);
  }

  public IAppInvoker[] getAllAppInvokers()
  {
    IAppInvoker[] rets = new IAppInvoker[this.id2invoker.size()];
    this.id2invoker.values().toArray(rets);
    return rets;
  }
}
