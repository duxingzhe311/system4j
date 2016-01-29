package com.dw.biz;

import com.dw.system.Convert;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.task.Task;
import com.dw.system.task.TaskHandler;
import com.dw.system.task.TaskParam;
import com.dw.system.xmldata.XmlData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BizActionTaskHandler
  implements TaskHandler
{
  static final String ACT_PATH = "action_path";
  static final String ACT_INPUT = "action_input";
  public static final String MULTI_THREAD = "action_multi_thread";
  public static final String MULTI_PARAM_ACT = "action_multi_param_act";
  public static final String PN_ONE_OF_MP = "$OneOfMultiParam";
  static ArrayList<TaskParam> tps = new ArrayList();

  ILogger log = LoggerManager.getLogger(BizActionTaskHandler.class);

  String multiParamActOutName = null;

  static
  {
    tps.add(new TaskParam("action_path", "action对应的绝对路", true));
    tps.add(new TaskParam("action_input", "action对应的输入参数，它形如dx_/xx:int32=1&", false));
  }

  public String getHandlerName()
  {
    return "biz_action";
  }

  public List<TaskParam> getTaskParams()
  {
    return tps;
  }

  public boolean checkTask(Task t, StringBuilder failedreson)
  {
    HashMap tpm = t.getTaskParms();
    if (tpm == null) {
      throw new RuntimeException("no biz_action task parms found!");
    }
    String act_path = (String)tpm.get("action_path");
    if (Convert.isNullOrEmpty(act_path))
    {
      throw new RuntimeException("no param with name=action_path");
    }

    int mtn = Convert.parseToInt32((String)tpm.get("action_multi_thread"), -1);
    if (mtn <= 0) {
      return true;
    }
    String pact = (String)tpm.get("action_multi_param_act");
    if (Convert.isNullOrEmpty(pact))
    {
      failedreson.append("multi thread task must has param :action_multi_param_act like:\n\t/mail/WEB-INF/mail_recv_users.action#UserNames");
      return false;
    }

    if (act_path.indexOf("$OneOfMultiParam") <= 0)
    {
      failedreson.append("multi thread act task action_path multi like /mail/WEB-INF/mail_recv.action?dx_/UserName=$OneOfMultiParam");
      return false;
    }

    return true;
  }

  public void handleTask(Task t) throws Exception
  {
    HashMap tpm = t.getTaskParms();
    if (tpm == null) {
      throw new RuntimeException("no biz_action task parms found!");
    }
    String act_path = (String)tpm.get("action_path");
    if (Convert.isNullOrEmpty(act_path))
    {
      throw new RuntimeException("no param with name=action_path");
    }

    int mtn = Convert.parseToInt32((String)tpm.get("action_multi_thread"), -1);
    String act_input = (String)tpm.get("action_input");
    XmlData inputxd = XmlData.parseFromUrlStr(act_input);
    if (mtn <= 0)
    {
      try
      {
        t.setRunningDesc("Single Thread");

        BizManager.getInstance().RT_doBizAction(null, act_path, null, inputxd);
        return;
      }
      finally
      {
        t.setRunningDesc("");
      }
    }

    String pact = (String)tpm.get("action_multi_param_act");
    if (Convert.isNullOrEmpty(pact)) {
      throw new IllegalArgumentException("multi thread task must has param :action_multi_param_act like:\n\t/mail/WEB-INF/mail_recv_users.action#UserNames");
    }
    int p = pact.indexOf('#');
    if (p <= 0)
    {
      throw new IllegalArgumentException("multi thread task must has param :action_multi_param_act like:\n\t/mail/WEB-INF/mail_recv_users.action#UserNames");
    }

    String pout = pact.substring(p + 1);
    pact = pact.substring(0, p);

    BizActionResult bar = BizManager.getInstance().RT_doBizAction(null, pact, null, null);
    XmlData xd = bar.getResultData();
    if (xd == null) {
      return;
    }
    String[] pvss = xd.getParamValuesStr(pout);
    if ((pvss == null) || (pvss.length <= 0)) {
      return;
    }

    try
    {
      ArrayList pns = splitMulit(pvss, mtn);
      int true_thnum = pns.size();

      MultiCtrl mc = new MultiCtrl(t, true_thnum);
      for (int i = 0; i < true_thnum; i++)
      {
        ArrayList ps = (ArrayList)pns.get(i);
        MultiRunner mr = new MultiRunner(mc, act_path, inputxd, ps);

        if (this.log.isDebugEnabled()) {
          this.log.debug("Task-InnerMulti" + i);
        }
        Thread th = new Thread(t.getTaskThreadGroup(), mr, "Task-InnerMulti" + i);
        th.start();
      }

      mc.checkWaitFinish();
    }
    finally
    {
      t.setRunningDesc("");
    }
  }

  private ArrayList<ArrayList<String>> splitMulit(String[] pvss, int thnum)
  {
    if (pvss.length < thnum)
    {
      ArrayList ret = new ArrayList(pvss.length);
      for (int i = 0; i < pvss.length; i++)
      {
        ArrayList p = new ArrayList(1);
        p.add(pvss[i]);
        ret.add(p);
      }
      return ret;
    }

    ArrayList ret = new ArrayList(thnum);

    int ll = pvss.length / thnum + pvss.length % thnum > 0 ? 1 : 0;
    for (int t = 0; t < thnum; t++)
    {
      ret.add(new ArrayList(ll));
    }

    for (int i = 0; i < pvss.length; i++)
    {
      int idx = i % thnum;
      ArrayList pns = (ArrayList)ret.get(idx);
      pns.add(pvss[i]);
    }

    return ret;
  }

  public boolean isSatisfy(Task t)
  {
    return true;
  }

  public String getHandlerStatusInfo()
  {
    return "";
  }

  class MultiCtrl
  {
    Task task = null;
    int totalNum = -1;
    int thNum = -1;

    public MultiCtrl(Task t, int num)
    {
      this.task = t;
      this.totalNum = num;
      this.thNum = num;

      this.task.setRunningDesc("Multi Thread=" + this.thNum + "/" + this.totalNum);
      if (BizActionTaskHandler.this.log.isDebugEnabled())
        BizActionTaskHandler.this.log.debug("Task Multi Ctrl -" + t.toString());
    }

    public synchronized void checkWaitFinish()
    {
      try
      {
        while (this.thNum > 0)
        {
          wait();
        }
      }
      catch (Exception localException)
      {
      }
    }

    public synchronized void finishOneNotify()
    {
      this.thNum -= 1;
      this.task.setRunningDesc("Multi Thread=" + this.thNum + "/" + this.totalNum);

      if (BizActionTaskHandler.this.log.isDebugEnabled()) {
        BizActionTaskHandler.this.log.debug("Task Multi Ctrl -" + this.task.toString());
      }
      notify();
    }
  }

  class MultiRunner implements Runnable
  {
    BizActionTaskHandler.MultiCtrl multiCtrl = null;
    String actPath = null;
    XmlData inputXd = null;
    ArrayList<String> paramSet = null;

    public MultiRunner(MultiCtrl mc, String actpath, XmlData inputxd,ArrayList<String> paramSet)
    {
      this.multiCtrl = mc;
      this.actPath = actpath;
      this.inputXd = inputxd;
      this.paramSet = paramSet;
    }

    public void run()
    {
      try
      {
        int p = this.actPath.indexOf("$OneOfMultiParam");

        for (String pp : this.paramSet)
        {
          try
          {
            String act = this.actPath.substring(0, p) + pp + this.actPath.substring(p + "$OneOfMultiParam".length());
            XmlData xd = XmlData.parseFromUrlStr(act);
            if (this.inputXd != null) {
              xd.combineAppend(this.inputXd);
            }

            BizManager.getInstance().RT_doBizAction(null, act, null, xd);
          }
          catch (Exception ee)
          {
            if (BizActionTaskHandler.this.log.isErrorEnabled())
              BizActionTaskHandler.this.log.error(ee);
          }
        }
      }
      finally
      {
        this.multiCtrl.finishOneNotify();
      }
    }
  }
}
