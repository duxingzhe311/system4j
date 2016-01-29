package com.dw.biz.workitem;

import java.util.List;

public class BizWorkItemList
{
  List<BizWorkItem> workItems = null;

  int pageIdx = -1;

  int pageSize = -1;

  int total = -1;

  public BizWorkItemList(List<BizWorkItem> bwis, int pageidx, int pagesize, int total)
  {
    this.workItems = bwis;
    this.pageIdx = pageidx;
    this.pageSize = pagesize;
    this.total = total;
  }

  public List<BizWorkItem> getWorkItems()
  {
    return this.workItems;
  }

  public int getPageIdx()
  {
    return this.pageIdx;
  }

  public int getPageSize()
  {
    return this.pageSize;
  }

  public int getTotal()
  {
    return this.total;
  }
}
