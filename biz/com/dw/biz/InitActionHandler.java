package com.dw.biz;

import com.dw.system.AppConfigFilter.InitActionHander;

public class InitActionHandler
  implements AppConfigFilter.InitActionHander
{
  public void handleAction(String actp)
    throws Exception
  {
    BizManager.getInstance().RT_doBizAction(null, actp, null);
  }
}
