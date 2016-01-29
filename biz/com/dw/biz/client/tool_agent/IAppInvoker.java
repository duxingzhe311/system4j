package com.dw.biz.client.tool_agent;

import com.dw.biz.api.Parameter;
import java.util.List;

public abstract interface IAppInvoker
{
  public abstract String getAppId();

  public abstract String getAppName();

  public abstract String getAppDesc();

  public abstract List<Parameter> getInFormalParameters();

  public abstract List<Parameter> getOutFormalParameters();

  public abstract List<Parameter> getInOutFormalParameters();

  public abstract void OnInvokeApplication(String paramString1, String paramString2, List<Parameter> paramList, int paramInt)
    throws Exception;

  public abstract int OnRequestAppStatus(String paramString1, String paramString2, List<Parameter> paramList)
    throws Exception;

  public abstract void OnTerminateApplication(String paramString1, String paramString2)
    throws Exception;
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.client.tool_agent.IAppInvoker
 * JD-Core Version:    0.6.2
 */