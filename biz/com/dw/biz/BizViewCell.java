package com.dw.biz;

import com.dw.mltag.JspDirective;
import com.dw.mltag.NodeParser;
import com.dw.mltag.XmlNode;
import com.dw.system.xmldata.IXmlDataDef;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlValDef;
import com.dw.system.xmldata.xrmi.XRmi;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XRmi(reg_name="biz_view_cell")
public class BizViewCell extends BizNodeObj
  implements IBizFile
{
  String title = null;

  String desc = null;

  String strCont = null;

  IXmlDataDef cellXmlDataDef = null;

  ArrayList<BizEvent> events = new ArrayList();

  transient boolean bValid = false;

  private transient XmlNode contTreeRoot = null;

  public BizViewCell()
  {
  }

  public BizViewCell(String title, String desc, String strcont, IXmlDataDef xmldata_def, List<BizEvent> events)
  {
    this.title = title;

    this.desc = desc;

    this.strCont = strcont;

    this.cellXmlDataDef = xmldata_def;

    if (events != null)
      this.events.addAll(events);
  }

  public void init(BizManager bizmgr)
    throws Exception
  {
    this.bValid = true;
  }

  public boolean isValid()
  {
    return this.bValid;
  }

  public String getTitle()
  {
    if (this.title != null) {
      return this.title;
    }
    try
    {
      XmlNode xn = getContTreeNodeRoot();
      List ll = xn.getSubXmlNodeByType(JspDirective.class);
      if (ll != null)
      {
        for (Iterator localIterator = ll.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

          JspDirective jd = (JspDirective)o;
          if (("viewcell".equalsIgnoreCase(jd.getJspDirectiveType())) || 
            ("view_cell".equalsIgnoreCase(jd.getJspDirectiveType())))
          {
            this.title = jd.getAttribute("title");
          }
        }
      }
    }
    catch (Exception localException)
    {
    }
    if (this.title == null)
      this.title = "";
    return this.title;
  }

  public void setTitle(String t)
  {
    this.title = t;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public void setDesc(String d)
  {
    this.desc = d;
  }

  public String getStrCont()
  {
    return this.strCont;
  }

  public void setStrCont(String sc)
  {
    this.strCont = sc;
  }

  public XmlNode getContTreeNodeRoot()
    throws Exception
  {
    if (this.contTreeRoot != null) {
      return this.contTreeRoot;
    }
    synchronized (this)
    {
      if (this.contTreeRoot != null) {
        return this.contTreeRoot;
      }
      if (this.strCont == null) {
        return null;
      }
      StringReader sr = new StringReader(this.strCont);
      NodeParser parser = new NodeParser(sr);

      parser.setIgnoreCase(true);

      parser.parse();

      this.contTreeRoot = parser.getRoot();

      return this.contTreeRoot;
    }
  }

  public IXmlDataDef getCellXmlDataDef()
  {
    return this.cellXmlDataDef;
  }

  public void setCellXmlDataDef(IXmlDataDef xdd)
  {
    this.cellXmlDataDef = xdd;
  }

  public BizEvent[] getEvents()
  {
    BizEvent[] rets = new BizEvent[this.events.size()];
    this.events.toArray(rets);
    return rets;
  }

  public void setEvents(List<BizEvent> evs)
  {
    if (this.events == null) {
      this.events = new ArrayList();
    }
    this.events.clear();

    if (evs != null)
      this.events.addAll(evs);
  }

  public BizEvent addEvent(String name, XmlDataStruct evxds)
    throws Exception
  {
    if ((name == null) || (name.equals(""))) {
      throw new Exception("Event Name Cannot be null or empty!");
    }
    if (evxds == null) {
      evxds = new XmlDataStruct();
    }
    BizEvent op = getEvent(name);
    if (op != null)
    {
      throw new Exception("Event with name=" + name + " is already existed!");
    }

    return setEvent(name, evxds);
  }

  public BizEvent setEvent(String name, XmlDataStruct evxds)
  {
    BizEvent op = getEvent(name);
    if (op != null)
    {
      op.setEventDataStruct(evxds);
      return op;
    }

    op = new BizEvent(name, evxds);

    this.events.add(op);

    return op;
  }

  public BizEvent getEvent(String ename)
  {
    for (BizEvent op : this.events)
    {
      if (op.getName().equals(ename))
        return op;
    }
    return null;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    if (this.title != null) {
      xd.setParamValue("title", this.title);
    }
    if (this.desc != null) {
      xd.setParamValue("desc", this.desc);
    }
    if (this.strCont != null) {
      xd.setParamValue("cont", this.strCont);
    }

    if (this.cellXmlDataDef != null)
    {
      XmlData tmpxd = this.cellXmlDataDef.toXmlData();

      if ((this.cellXmlDataDef instanceof XmlDataStruct))
      {
        tmpxd.setParamValue("def_type", "struct");
      }
      else if ((this.cellXmlDataDef instanceof XmlValDef))
      {
        tmpxd.setParamValue("def_type", "val");
      }

      xd.setSubDataSingle("xmldate_def", tmpxd);
    }

    if (this.events != null)
    {
      List xds = xd.getOrCreateSubDataArray("events");
      for (BizEvent op : this.events)
      {
        xds.add(op.toXmlData());
      }
    }

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.title = xd.getParamValueStr("title");

    this.desc = xd.getParamValueStr("desc");
    this.strCont = xd.getParamValueStr("cont");

    XmlData tmpxd = xd.getSubDataSingle("xmldate_def");
    if (tmpxd != null)
    {
      String deft = tmpxd.getParamValueStr("def_type");
      if ("struct".equals(deft))
      {
        this.cellXmlDataDef = new XmlDataStruct();
        this.cellXmlDataDef.fromXmlData(tmpxd);
      }
      else if ("val".equals(deft))
      {
        this.cellXmlDataDef = new XmlValDef();
        this.cellXmlDataDef.fromXmlData(tmpxd);
      }

    }

    List operxds = xd.getSubDataArray("events");
    if (operxds != null)
    {
      for (XmlData xd0 : operxds)
      {
        BizEvent tmpop = new BizEvent();
        tmpop.fromXmlData(xd0);
        this.events.add(tmpop);
      }
    }
  }

  public void fromFileContent(byte[] cont)
  {
    try
    {
      XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");
      fromXmlData(xd);
      return;
    }
    catch (Exception localException)
    {
      try
      {
        this.strCont = new String(cont, "UTF-8");
      }
      catch (Exception localException1)
      {
      }
    }
  }

  public static enum CtrlType
  {
    ignore, 
    hidden, 
    read, 
    write_no, 
    write, 
    write_need;
  }
}
