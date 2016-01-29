package com.dw.biz.platform;

import com.dw.biz.BizEvent;
import com.dw.biz.BizOutput;
import com.dw.system.xmldata.IXmlDataDef;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlValDef;

public abstract class BizViewFormRunner extends BizViewRunner
{
  private String viewFormSubmitTitle = "Submit";

  public final String getViewFormSubmitTitle()
  {
    return this.viewFormSubmitTitle;
  }

  public final void setViewFormSubmitTitle(String t)
  {
    this.viewFormSubmitTitle = t;
  }

  public final String calViewCellsPostUrl()
  {
    return calculateFormViewSubmitUrl();
  }

  public final boolean canHasFormViewSubmit()
  {
    return true;
  }

  public final String calculateFormViewSubmitUrl()
  {
    if (!canHasFormViewSubmit()) {
      return "javascript:void";
    }

    return "javascript:biz_view_form_submit('" + getViewId() + "')";
  }

  public XmlDataStruct getFormDataStruct()
  {
    XmlDataStruct xds = new XmlDataStruct();
    BizViewCellRunner[] bvcrs = getViewCells();
    if (bvcrs == null) {
      return xds;
    }
    for (BizViewCellRunner bvcr : bvcrs)
    {
      IXmlDataDef xdd = bvcr.getCellXmlDataDef();
      if (xdd != null)
      {
        String n = bvcr.getCellName();
        if ((n != null) && (!n.equals("")))
        {
          if (!bvcr.isIgnore())
          {
            if ((xdd instanceof XmlValDef))
            {
              xds.setXmlDataMember(n, (XmlValDef)xdd);
            }
            else if ((xdd instanceof XmlDataStruct))
            {
              XmlDataStruct tmpxds = (XmlDataStruct)xdd;
              tmpxds.setName(n);
              xds.setSubStruct(tmpxds);
            }
          }
        }
      }
    }
    return xds;
  }

  public XmlDataStruct getRunnerInputXmlDataStruct()
  {
    XmlDataStruct xds = super.getRunnerInputXmlDataStruct();
    XmlDataStruct tmpxds = getFormDataStruct();
    xds.combineAppend(tmpxds);
    return xds;
  }

  public XmlDataStruct getFormSubmitXmlDataStruct()
  {
    return getFormDataStruct();
  }

  public BizEvent[] getRunnerEvents()
  {
    BizEvent[] rets = super.getRunnerEvents();
    if (rets == null)
    {
      rets = new BizEvent[1];
    }
    else
    {
      BizEvent[] tmpbes = new BizEvent[rets.length + 1];
      System.arraycopy(rets, 0, tmpbes, 1, rets.length);
    }

    rets[0] = new BizEvent("submit", getFormDataStruct(), true);
    return rets;
  }

  public BizOutput[] getRunnerOutputs()
  {
    BizOutput[] rets = super.getRunnerOutputs();
    if (rets == null)
    {
      rets = new BizOutput[1];
    }
    else
    {
      BizOutput[] tmpbes = new BizOutput[rets.length + 1];
      System.arraycopy(rets, 0, tmpbes, 1, rets.length);
    }

    rets[0] = new BizOutput("submit", getFormDataStruct());
    return rets;
  }

  public XmlData toViewStateXmlData() throws Exception
  {
    XmlData xd = super.toViewStateXmlData();

    BizViewCellRunner[] bvcrs = getViewCells();
    if (bvcrs == null) {
      return xd;
    }
    for (BizViewCellRunner bvcr : bvcrs)
    {
      bvcr.getValueToXmlData(bvcr.getCellName(), xd);
    }
    return xd;
  }

  public void fromViewStateXmlData(XmlData xd)
    throws Exception
  {
    if (xd == null) {
      return;
    }
    super.fromViewStateXmlData(xd);

    BizViewCellRunner[] bvcrs = getViewCells();
    if (bvcrs == null) {
      return;
    }
    for (BizViewCellRunner bvcr : bvcrs)
    {
      bvcr.setValueByXmlData(bvcr.getCellName(), xd);
    }
  }
}
