package com.dw.biz;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.user.right.RightManager;
import com.dw.user.right.RightRule;
import java.util.HashSet;

public class BizParticipant
  implements IXmlDataable
{
  String cont = null;

  ContType contType = ContType.RightRule;
  Type type = Type.none;
  AssignmentStyle assignStyle = AssignmentStyle.auto;

  boolean bCanAbortFlow = false;

  boolean bCanSuspendFlow = false;

  private transient RightRule _rr = null;

  public BizParticipant()
  {
  }

  public BizParticipant(Type t)
  {
    this.type = t;
  }

  public BizParticipant(BizParticipant bp)
  {
    if (bp == null) {
      return;
    }
    this.cont = bp.cont;
    this.contType = bp.contType;
    this.assignStyle = bp.assignStyle;
  }

  public BizParticipant(String rr, AssignmentStyle asss)
  {
    this.type = Type.single;

    this.cont = rr;

    this.assignStyle = asss;
  }

  public BizParticipant(ContType ct, String cont, AssignmentStyle asss)
  {
    this.type = Type.single;
    this.contType = ct;
    this.cont = cont;

    this.assignStyle = asss;
  }

  public Type getType()
  {
    return this.type;
  }

  public String getCont()
  {
    return this.cont;
  }

  public ContType getContType()
  {
    return this.contType;
  }

  public void setCont(ContType ct, String rr)
  {
    this.contType = ct;
    this.cont = rr;
  }

  public RightRule getContAsRightRule()
  {
    if (this.contType != ContType.RightRule) {
      return null;
    }
    if (this._rr != null) {
      return this._rr;
    }
    if ((this.cont == null) || (this.cont.equals(""))) {
      return null;
    }
    this._rr = RightRule.Parse(this.cont);
    return this._rr;
  }

  public AssignmentStyle getAssignmentStyle()
  {
    return this.assignStyle;
  }

  public void setAssignmentStyle(AssignmentStyle ass)
  {
    this.assignStyle = ass;
  }

  public boolean canAbortFlow()
  {
    return this.bCanAbortFlow;
  }

  public void setCanAbortFlow(boolean b)
  {
    this.bCanAbortFlow = b;
  }

  public boolean canSuspendFlow()
  {
    return this.bCanSuspendFlow;
  }

  public void setCanSuspendFlow(boolean b)
  {
    this.bCanSuspendFlow = b;
  }

  public String[] getRelatedUserNames(RightManager rm)
    throws Exception
  {
    HashSet userns = rm
      .getUserNamesByRightRule(this.cont);
    if (userns == null)
    {
      return new String[0];
    }

    String[] relatedUserNames = new String[userns.size()];
    userns.toArray(relatedUserNames);
    return relatedUserNames;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    if (this.cont != null)
      xd.setParamValue("cont", this.cont);
    xd.setParamValue("cont_type", this.contType.toString());

    if (this.type == null)
      xd.setParamValue("type", this.type.toString());
    if (this.assignStyle != null) {
      xd.setParamValue("assign_style", this.assignStyle.toString());
    }
    if (this.bCanAbortFlow) {
      xd.setParamValue("can_abort_flow", Boolean.valueOf(this.bCanAbortFlow));
    }
    if (this.bCanSuspendFlow) {
      xd.setParamValue("can_suspend_flow", Boolean.valueOf(this.bCanSuspendFlow));
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.cont = xd.getParamValueStr("cont");
    String tmpct = xd.getParamValueStr("cont_type");
    if ((tmpct != null) && (!tmpct.equals(""))) {
      this.contType = ContType.valueOf(tmpct);
    }
    String tmps = xd.getParamValueStr("type");
    if ((tmps != null) && (!tmps.equals("")))
      this.type = Type.valueOf(tmps);
    tmps = xd.getParamValueStr("assign_style");
    if ((tmps != null) && (!tmps.equals(""))) {
      this.assignStyle = AssignmentStyle.valueOf(tmps);
    }
    this.bCanAbortFlow = xd.getParamValueBool("can_abort_flow", false).booleanValue();

    this.bCanSuspendFlow = xd.getParamValueBool("can_suspend_flow", false).booleanValue();
  }

  public static enum Type
  {
    none, 
    single, 
    datafield, 
    all;
  }

  public static enum AssignmentStyle
  {
    auto(0), 
    first_accept(1), 
    manual(2);

    private final int val;

    private AssignmentStyle(int v) {
      this.val = v;
    }

    public int getValue()
    {
      return this.val;
    }

    public static AssignmentStyle valueOf(int v)
    {
      switch (v)
      {
      case 0:
        return auto;
      case 1:
        return first_accept;
      case 2:
        return manual;
      }
      throw new IllegalArgumentException("unknown assignment style value=" + v);
    }
  }

  public static enum ContType
  {
    RightRule, 
    DataField;
  }
}
