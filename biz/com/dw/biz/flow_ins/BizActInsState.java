package com.dw.biz.flow_ins;

public enum BizActInsState
{
  open_notRunning(0), 
  open_running(1), 

  open_run_failed(2), 
  closed_completed(3), 
  closed_aborted(4);

  private final int val;

  private BizActInsState(int v) {
    this.val = v;
  }

  public int getValue()
  {
    return this.val;
  }

  public static BizActInsState valueOf(int v)
  {
    switch (v)
    {
    case 0:
      return open_notRunning;
    case 1:
      return open_running;
    case 2:
      return closed_completed;
    case 3:
      return closed_aborted;
    }
    throw new RuntimeException("invald flow act state!");
  }
}
