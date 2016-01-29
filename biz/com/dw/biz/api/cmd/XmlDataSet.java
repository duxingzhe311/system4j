package com.dw.biz.api.cmd;

import com.dw.biz.api.util.XmlHelper;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class XmlDataSet
{
  public static final String VAL_TYPE_BOOL = "bool";
  public static final String VAL_TYPE_STR = "string";
  public static final String VAL_TYPE_BYTE = "byte";
  public static final String VAL_TYPE_INT16 = "int16";
  public static final String VAL_TYPE_INT32 = "int32";
  public static final String VAL_TYPE_INT64 = "int64";
  public static final String VAL_TYPE_FLOAT = "float";
  public static final String VAL_TYPE_DOUBLE = "double";
  public static final String VAL_TYPE_DATE = "date";
  public static final String VAL_TYPE_BYTEARRAY = "byte_array";
  public static final String VAL_TYPE_XML = "byte_array";
  static DocumentBuilderFactory xmlDocBuilderFactory = null;

  List<XmlDataTable> tables = new ArrayList();

  static
  {
    xmlDocBuilderFactory = DocumentBuilderFactory.newInstance();

    xmlDocBuilderFactory.setNamespaceAware(false);
    xmlDocBuilderFactory.setValidating(false);
  }

  public XmlDataTable getTable(int idx)
  {
    return (XmlDataTable)this.tables.get(idx);
  }

  public XmlDataTable getTable(String n)
  {
    for (XmlDataTable dt : this.tables)
    {
      if (dt.getTableName().equals(n))
        return dt;
    }
    return null;
  }

  public XmlDataTable getOrCreateTable(String n)
  {
    for (XmlDataTable dt : this.tables)
    {
      if (dt.getTableName().equals(n)) {
        return dt;
      }
    }
    XmlDataTable ndt = new XmlDataTable(n);
    this.tables.add(ndt);
    return ndt;
  }

  public boolean removeTable(XmlDataTable dt)
  {
    return this.tables.remove(dt);
  }

  public XmlDataTable removeTable(int idx)
  {
    return (XmlDataTable)this.tables.remove(idx);
  }

  public String toXmlString()
  {
    StringBuffer tmpsb = new StringBuffer();

    tmpsb.append("<data_set>");
    for (XmlDataTable xdt : this.tables)
    {
      xdt.toXmlString("\t", tmpsb);
    }
    tmpsb.append("\r\n</data_set>");

    return tmpsb.toString();
  }

  public static XmlDataSet parseFromXmlElement(Element datasetxe)
  {
    if (!datasetxe.getTagName().equals("data_set")) {
      throw new IllegalArgumentException("not data_set xml element!");
    }
    XmlDataSet xds = new XmlDataSet();

    for (Element xe : XmlHelper.getSubChildElement(datasetxe, "table"))
    {
      XmlDataTable xdt = XmlDataTable.parseFromXmlElement(xe);
      xds.tables.add(xdt);
    }

    return xds;
  }

  public static XmlDataSet parseFromXmlStr(String xmlstr)
    throws Exception
  {
    InputSource is = new InputSource(new StringReader(xmlstr));
    DocumentBuilder db = xmlDocBuilderFactory.newDocumentBuilder();

    Document doc = db.parse(is);
    return parseFromXmlElement(doc.getDocumentElement());
  }
}
