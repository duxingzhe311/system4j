package com.dw.biz.api.cmd;

import com.dw.biz.api.util.XmlHelper;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

public class XmlDataTable
{
  String tableName = null;

  List<XmlDataRow> rows = new ArrayList();

  public XmlDataTable(String n)
  {
    if ((n == null) || (n == "")) {
      throw new IllegalArgumentException("table name cannot be null!");
    }
    this.tableName = n;
  }

  public String getTableName()
  {
    return this.tableName;
  }

  public int getRowCount()
  {
    return this.rows.size();
  }

  public XmlDataRow getFirstRow()
  {
    if (this.rows.size() <= 0) {
      return null;
    }
    return (XmlDataRow)this.rows.get(0);
  }

  public XmlDataRow getRow(int idx)
  {
    return (XmlDataRow)this.rows.get(idx);
  }

  public XmlDataRow addRow(XmlDataRow xdr)
  {
    this.rows.add(xdr);
    return xdr;
  }

  public XmlDataRow removeRow(int idx)
  {
    return (XmlDataRow)this.rows.remove(idx);
  }

  public boolean removeRow(XmlDataRow xdr)
  {
    return this.rows.remove(xdr);
  }

  public String toXmlString()
  {
    StringBuffer tmpsb = new StringBuffer();
    toXmlString("", tmpsb);
    return tmpsb.toString();
  }

  void toXmlString(String tab, StringBuffer sb)
  {
    sb.append("\r\n").append(tab)
      .append("<table name=\"").append(this.tableName).append("\">");

    for (XmlDataRow xdr : this.rows)
    {
      xdr.toXmlString(tab + "\t", sb);
    }

    sb.append("\r\n").append(tab).append("</table>");
  }

  static XmlDataTable parseFromXmlElement(Element tablexe)
  {
    if (!tablexe.getTagName().equals("table")) {
      throw new IllegalArgumentException("not table xml element!");
    }
    String tn = tablexe.getAttribute("name");
    XmlDataTable xdt = new XmlDataTable(tn);

    for (Element xe : XmlHelper.getSubChildElement(tablexe, "row"))
    {
      XmlDataRow xdr = XmlDataRow.parseFromXmlElement(xe);
      xdt.addRow(xdr);
    }

    return xdt;
  }
}
