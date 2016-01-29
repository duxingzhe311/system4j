package com.dw.biz.flow_ins;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;

@XORMClass(table_name="biz_act_ins")
public class BizActInsContext
{

  @XORMProperty(name="ActInsId", has_col=true, is_pk=true, is_auto=true)
  private long actInsId = -1L;

  @XORMProperty(name="FlowInsId", has_col=true, has_idx=true, has_fk=true, fk_table="biz_flow_ins", fk_column="FlowInsId")
  private long flowInsId = -1L;

  @XORMProperty(name="FlowNodeId", has_col=true, max_len=50)
  private String flowNodeId = null;

  BizActInsState state = BizActInsState.open_notRunning;

  @XORMProperty(name="State", has_col=true)
  private int get_State()
  {
    return this.state.getValue();
  }

  private void set_State(int st) {
    this.state = BizActInsState.valueOf(st);
  }
}
