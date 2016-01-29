package com.dw.biz.flow_ins;

import java.util.ArrayList;
import java.util.List;

public class BizFlowInsList
{
  int pageIdx = -1;
  int pageSize = -1;
  int totalCount = -1;
  private List<BizFlowInsContext> listCxts = null;

  public BizFlowInsList(int pageidx, int pagesize, int total, List<BizFlowInsContext> ls)
  {
    this.pageIdx = pageidx;
    this.pageSize = pagesize;
    this.totalCount = total;
    this.listCxts = ls;
    if (this.listCxts == null)
      this.listCxts = new ArrayList(0);
  }

  public int getPageIdx()
  {
    return this.pageIdx;
  }

  public int getPageSize()
  {
    return this.pageSize;
  }

  public int getTotalCount()
  {
    return this.totalCount;
  }

  public List<BizFlowInsContext> getListCxts()
  {
    return this.listCxts;
  }
}
