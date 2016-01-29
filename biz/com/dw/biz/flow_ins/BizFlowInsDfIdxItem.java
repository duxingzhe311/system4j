package com.dw.biz.flow_ins;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;

@XORMClass(table_name="biz_flow_ins_df_idx")
public class BizFlowInsDfIdxItem
{

  @XORMProperty(name="AutoId", has_col=true, is_pk=true, is_auto=true)
  long autoId = -1L;

  @XORMProperty(name="FlowPath", has_col=true, has_idx=true, max_len=50, order_num=5)
  String flowPath = null;

  @XORMProperty(name="InsId", has_col=true, has_idx=true, has_fk=true, fk_table="biz_flow_ins", fk_column="FlowInsId", order_num=10)
  long insId = -1L;

  @XORMProperty(name="DfName", has_col=true, has_idx=true, max_len=50, order_num=20)
  String dfName = null;

  @XORMProperty(name="DfStrVal", has_col=true, max_len=100, auto_truncate=true, order_num=30)
  String dfStrVal = null;

  public BizFlowInsDfIdxItem()
  {
  }

  public BizFlowInsDfIdxItem(String flowpath, long insid, String dfname, String dfstrval)
  {
    this.flowPath = flowpath;
    this.insId = insid;
    this.dfName = dfname;
    this.dfStrVal = dfstrval;
  }

  public long getAutoId()
  {
    return this.autoId;
  }

  public String getFlowPath()
  {
    return this.flowPath;
  }

  public long getFlowInsId()
  {
    return this.insId;
  }

  public String getDfName()
  {
    return this.dfName;
  }

  public String getDfStrVal()
  {
    return this.dfStrVal;
  }

  public void setDfStrVal(String dfstrv)
  {
    this.dfStrVal = dfstrv;
  }
}
