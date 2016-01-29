package com.dw.biz.api.cmd;

import com.dw.biz.api.util.BinHexTransfer;
import com.dw.biz.api.util.XmlHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map.Entry;
import org.w3c.dom.Element;

public class XmlDataRow
{
  Hashtable<String, ValItem> pname2val = new Hashtable();

  public void setParamValue(String pn, Object v)
  {
    this.pname2val.put(pn, new ValItem(v));
  }

  public Object getParamValue(String pn)
  {
    ValItem vi = (ValItem)this.pname2val.get(pn);
    if (vi == null) {
      return null;
    }
    return vi.getObjectVal();
  }

  public int getParamValueInt32(String pn, int default_val)
  {
    Object o = getParamValue(pn);
    if (o == null) {
      return default_val;
    }
    return ((Integer)o).intValue();
  }

  public String toXmlString()
  {
    StringBuffer tmpsb = new StringBuffer();
    toXmlString("", tmpsb);
    return tmpsb.toString();
  }

  void toXmlString(String tab, StringBuffer sb)
  {
    sb.append("\r\n").append(tab).append("<row>");

    for (Map.Entry i : this.pname2val.entrySet())
    {
      sb.append("\r\n").append(tab).append("<col")
        .append(" ptype=\"").append(((ValItem)i.getValue()).getValType()).append("\"")
        .append(" pname=\"").append(XmlHelper.xmlEncoding((String)i.getKey())).append("\"")
        .append(" pval=\"").append(((ValItem)i.getValue()).getXmlEncodedStrVal()).append("\"")
        .append("/>");
    }

    sb.append("\r\n").append(tab).append("</row>");
  }

  static XmlDataRow parseFromXmlElement(Element rowxe)
  {
    if (!rowxe.getTagName().equals("row")) {
      throw new IllegalArgumentException("not row xml element!");
    }
    XmlDataRow xdr = new XmlDataRow();

    for (Element xe : XmlHelper.getSubChildElement(rowxe, "col"))
    {
      String ptype = xe.getAttribute("ptype");
      if ((ptype == null) || (ptype.equals(""))) {
        ptype = "string";
      }
      String pname = xe.getAttribute("pname");
      if ((pname == null) || (pname.equals(""))) {
        throw new IllegalArgumentException("row xml element has no pname attribute!");
      }
      String pval = xe.getAttribute("pval");

      ValItem vi = new ValItem(ptype, pval);
      xdr.pname2val.put(pname, vi);
    }

    return xdr;
  }

  static class ValItem
  {
    String xmlEncodedStrVal = null;
    String type = "string";
    Object objVal = null;

    public ValItem(String type, String xmlstrval)
    {
      if (xmlstrval == null)
        throw new IllegalArgumentException("str val cannot be null");
      if (type == null)
        throw new IllegalArgumentException("str val type cannot be null");
      this.type = type;

      this.xmlEncodedStrVal = XmlHelper.xmlEncoding(xmlstrval);
    }

    public ValItem(Object obj)
    {
      if (obj == null) {
        throw new IllegalArgumentException("obj cannot be null");
      }
      this.objVal = obj;
      if ((obj instanceof Boolean))
      {
        this.type = "bool";
      }
      else if ((obj instanceof String))
      {
        this.type = "string";
      }
      else if ((obj instanceof Byte))
      {
        this.type = "byte";
      }
      else if ((obj instanceof Short))
      {
        this.type = "int16";
      }
      else if ((obj instanceof Integer))
      {
        this.type = "int32";
      }
      else if ((obj instanceof Long))
      {
        this.type = "int64";
      }
      else if ((obj instanceof Float))
      {
        this.type = "float";
      }
      else if ((obj instanceof Double))
      {
        this.type = "double";
      }
      else if ((obj instanceof Date))
      {
        this.type = "date";
      }
      else if ((obj instanceof byte[]))
      {
        this.type = "byte_array";
      }
      else
      {
        throw new IllegalArgumentException("cannot process Object with type=" + this.objVal.getClass().getName());
      }
    }

    public Object getObjectVal()
    {
      if (this.objVal != null) {
        return this.objVal;
      }
      try
      {
        if (this.type.equals("bool"))
        {
          this.objVal = new Boolean("true".equals(this.xmlEncodedStrVal));
        }
        else if (this.type.equals("string"))
        {
          this.objVal = XmlHelper.xmlDecoding(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("byte"))
        {
          this.objVal = new Byte(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("int16"))
        {
          this.objVal = new Short(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("int32"))
        {
          this.objVal = new Integer(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("int64"))
        {
          this.objVal = new Long(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("float"))
        {
          this.objVal = new Float(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("double"))
        {
          this.objVal = new Double(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("date"))
        {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
          this.objVal = sdf.parse(this.xmlEncodedStrVal);
        }
        else if (this.type.equals("byte_array"))
        {
          this.objVal = BinHexTransfer.TransHexStrToBin(this.xmlEncodedStrVal);
        }
        return this.objVal;
      }
      catch (Exception e)
      {
        throw new RuntimeException(e.getMessage());
      }
    }

    String getXmlEncodedStrVal()
    {
      if (this.xmlEncodedStrVal != null) {
        return this.xmlEncodedStrVal;
      }
      if (this.type.equals("string"))
      {
        this.xmlEncodedStrVal = XmlHelper.xmlEncoding((String)this.objVal);
      }
      else if (this.type.equals("byte_array"))
      {
        this.xmlEncodedStrVal = BinHexTransfer.TransBinToHexStr((byte[])this.objVal);
      }
      else if (this.type.equals("bool"))
      {
        Boolean b = (Boolean)this.objVal;
        if (b.booleanValue())
          this.xmlEncodedStrVal = "true";
        else
          this.xmlEncodedStrVal = "false";
      }
      else if (this.type.equals("date"))
      {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        this.xmlEncodedStrVal = sdf.format((Date)this.objVal);
      }
      else
      {
        this.xmlEncodedStrVal = this.objVal.toString();
      }

      return this.xmlEncodedStrVal;
    }

    public String getValType()
    {
      return this.type;
    }
  }
}
