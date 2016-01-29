package com.dw.biz;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

public class BizCtrl
  implements IXmlDataable
{
  private Hashtable<String, String> ctrlItemName2Option = new Hashtable<String, String>();

  public void setCtrlItemOption(String ctrln, String option)
  {
    if (option == null)
    {
      this.ctrlItemName2Option.remove(ctrln);
      return;
    }

    this.ctrlItemName2Option.put(ctrln, option);
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    for (Map.Entry<String, String> n2o : this.ctrlItemName2Option.entrySet())
    {
      xd.setParamValue((String)n2o.getKey(), n2o.getValue());
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    for (String n : xd.getParamNames())
    {
      this.ctrlItemName2Option.put(n, xd.getParamValueStr(n));
    }
  }

  public static abstract class BizCtrlItem
  {
    String name = null;

    public BizCtrlItem(String n)
    {
      this.name = n;
    }

    public String getName()
    {
      return this.name;
    }

    public abstract String[] getCtrlOptions();

    public abstract String[] getCtrlDisOptions();

    public abstract String getDefaultCtrlOption();
  }

  public static abstract interface IBizCtrlable
  {
    public abstract List<BizCtrl.BizCtrlItem> getAllCtrlItems();

    public abstract BizCtrl.BizCtrlItem getCtrlItem(String paramString);

    public abstract void setBizCtrl(BizCtrl paramBizCtrl);
  }
}
