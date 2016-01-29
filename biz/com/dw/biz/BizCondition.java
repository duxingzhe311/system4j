package com.dw.biz;

import com.dw.system.Convert;
import com.dw.system.codedom.BoolExp;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

public class BizCondition
  implements IXmlDataable
{
  private CondType type = CondType.Normal;

  private String condTitle = null;
  private String condContent = null;

  private transient BoolExp condExp = null;

  public BizCondition()
  {
  }

  public BizCondition(CondType ct, String t, String cc)
  {
    this.type = ct;
    this.condTitle = t;
    this.condContent = cc;
  }

  public BizCondition(BizCondition bc)
  {
    if (bc == null) {
      return;
    }

    this.type = bc.type;
    this.condTitle = bc.condTitle;
    this.condContent = bc.condContent;
  }

  public CondType getCondType()
  {
    return this.type;
  }

  public void setCondType(CondType ct)
  {
    this.type = ct;
  }

  public String getConditionTitle()
  {
    return this.condTitle;
  }

  public void setConditionTitle(String t)
  {
    this.condTitle = t;
  }

  public String getConditionContent()
  {
    return this.condContent;
  }

  public void setConditionContent(String cc)
  {
    this.condContent = cc;
    if (this.condContent != null)
      this.condContent = this.condContent.trim();
  }

  public BoolExp getConditionBoolExp()
    throws Exception
  {
    if (this.condExp != null) {
      return this.condExp;
    }
    if (Convert.isNullOrTrimEmpty(this.condContent)) {
      return null;
    }
    this.condExp = new BoolExp(this.condContent);
    return this.condExp;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    xd.setParamValue("cond_type", this.type.toString());
    if (this.condTitle != null)
      xd.setParamValue("title", this.condTitle);
    if (this.condContent != null)
      xd.setParamValue("content", this.condContent);
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    String strt = xd.getParamValueStr("cond_type");
    if ((strt != null) && (!strt.equals("")))
      this.type = CondType.valueOf(strt);
    this.condTitle = xd.getParamValueStr("title");

    this.condContent = xd.getParamValueStr("content");
  }

  public static enum CondType
  {
    Normal, 
    NormalDefault, 
    Exception, 
    ExceptionDefault;
  }
}
