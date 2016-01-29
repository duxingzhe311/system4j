package com.dw.biz.flow_ins;

public enum BizFlowInsState
{
  open_notStarted(0), 
  open_running(1), 
  open_suspend(2), 

  closed_completed(3), 
  closed_aborted(4);

  private final int val;
  static BizFlowInsState[] OPEN_STS = { open_notStarted, open_running, open_suspend };

  private BizFlowInsState(int v)
  {
    this.val = v;
  }

  public int getValue()
  {
    return this.val;
  }

  public static BizFlowInsState[] getOpenStates()
  {
    return OPEN_STS;
  }

  public static BizFlowInsState valueOf(int v)
  {
    switch (v)
    {
    case 0:
      return open_notStarted;
    case 1:
      return open_running;
    case 2:
      return open_suspend;
    case 3:
      return closed_completed;
    case 4:
      return closed_aborted;
    }
    throw new RuntimeException("invald flow ins state!");
  }

  public boolean isClose()
  {
    switch (this.val)
    {
    case 3:
      return true;
    case 4:
      return true;
    }
    return false;
  }
}
