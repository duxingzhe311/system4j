package com.dw.mconn;

public abstract interface IMsgCmdHandler
{
  public abstract boolean checkConnRight(String paramString1, String paramString2);

  public abstract MsgCmd OnCmd(MsgCmd paramMsgCmd, MsgCmdClientInfo paramMsgCmdClientInfo);
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.mconn.IMsgCmdHandler
 * JD-Core Version:    0.6.2
 */