package com.dw.biz.flow_ins;

import com.dw.biz.BizFlow;
import com.dw.biz.BizFlow.ActNode;
import com.dw.system.xmldata.XmlData;

public class BizActIns
{
  long insId = -1L;

  long bizFlowInsId = -1L;

  String nodePath = null;

  private XmlData inputParam = null;

  private XmlData outputParam = null;

  BizActInsState state = BizActInsState.open_notRunning;

  transient BizFlow belongToFlow = null;
  transient BizFlow.ActNode belongToActNode = null;

  transient BizFlowIns belongToFlowIns = null;

  public long getInsId()
  {
    return this.insId;
  }

  public long getFlowInsId()
  {
    return this.bizFlowInsId;
  }

  public boolean isInsStateOpenRunning()
  {
    return this.state.toString().startsWith("open_running");
  }

  public boolean isInsStateClosed()
  {
    return this.state.toString().startsWith("closed_");
  }

  public BizActInsState getState()
  {
    return this.state;
  }

  public void setState(BizActInsState st)
  {
    this.state = st;
  }

  public XmlData getInputParam()
  {
    return this.inputParam;
  }

  public XmlData getOutputParam()
  {
    return this.outputParam;
  }

  public void runActIns()
  {
    if (this.state != BizActInsState.open_notRunning) {
      throw new RuntimeException("only not running act can be run!");
    }

    if (this.nodePath.endsWith(".view"));
  }
}
