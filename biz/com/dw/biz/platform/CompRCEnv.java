package com.dw.biz.platform;

import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import java.util.HashMap;

public abstract class CompRCEnv
  implements IRuntimeCompilerEnv
{
  private String compName = null;

  static HashMap<String, BizClassLoader> comp2RecentCL = new HashMap();

  public CompRCEnv(String compn)
  {
    this.compName = compn;
  }

  public ClassLoader getEnvClassLoader()
  {
    AppInfo ci = CompManager.getInstance().getAppInfo(this.compName);
    if (ci == null)
      throw new RuntimeException("cannot get comp ClassLoader because cannot get comp info with name=" + this.compName + ",check web.xml in comp!");
    return ci.getRelatedClassLoader();
  }

  public String getEnvClassPath()
  {
    AppInfo ci = CompManager.getInstance().getAppInfo(this.compName);
    if (ci == null) {
      throw new RuntimeException("cannot get comp web classpath because cannot get comp info with name=" + this.compName + ",check web.xml in comp!");
    }
    return ci.getRelatedClassPath();
  }

  public BizClassLoader createNewBizClassLoader()
  {
    BizClassLoader ozCL = null;
    ClassLoader pcl = getEnvClassLoader();
    if (pcl == null)
      ozCL = new BizClassLoader(getTargetClassBase());
    else {
      ozCL = new BizClassLoader(getTargetClassBase(), pcl);
    }

    comp2RecentCL.put(this.compName, ozCL);
    return ozCL;
  }

  public BizClassLoader getRecentBizClassLoader()
  {
    BizClassLoader bcl = (BizClassLoader)comp2RecentCL.get(this.compName);
    if (bcl != null) {
      return bcl;
    }
    return createNewBizClassLoader();
  }
}
