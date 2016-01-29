package com.dw.biz;

import com.dw.system.xmldata.XmlData;

public class BizViewEventResult
{
  static BizViewEventResult ignoreCR = new BizViewEventResult();

  private ResultType resultType = ResultType.Ignore;

  private String refreshTarViewName = null;
  private XmlData refreshViewCtrlXd = null;
  private XmlData refreshViewInputXd = null;

  private String outputName = null;
  private XmlData outputXd = null;

  public static BizViewEventResult createIgnoreCellResult()
  {
    return ignoreCR;
  }

  public static BizViewEventResult createRefreshCellResult(String tarcellname, XmlData ctrlxd, XmlData inputxd)
  {
    BizViewEventResult cr = new BizViewEventResult();
    cr.resultType = ResultType.RefreshView;
    cr.refreshTarViewName = tarcellname;
    cr.refreshViewCtrlXd = ctrlxd;
    cr.refreshViewInputXd = inputxd;
    return cr;
  }

  public static BizViewEventResult createOutputResult(String outputname, XmlData outputxd)
  {
    BizViewEventResult cr = new BizViewEventResult();
    cr.resultType = ResultType.Output;
    cr.outputName = outputname;
    cr.outputXd = outputxd;
    return cr;
  }

  public ResultType getResultType()
  {
    return this.resultType;
  }

  public String getRefreshTarViewName()
  {
    return this.refreshTarViewName;
  }

  public XmlData getRefreshViewCtrlXmlData()
  {
    return this.refreshViewCtrlXd;
  }

  public XmlData getRefreshViewInputXmlData()
  {
    return this.refreshViewInputXd;
  }

  public String getOutputName()
  {
    return this.outputName;
  }

  public XmlData getOutputXmlData()
  {
    return this.outputXd;
  }

  public static enum ResultType
  {
    Ignore, 
    RefreshView, 
    Output;
  }
}
