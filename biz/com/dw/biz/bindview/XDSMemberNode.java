package com.dw.biz.bindview;

import com.dw.mltag.XmlNode;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlVal;
import java.io.IOException;
import java.io.Writer;

public abstract class XDSMemberNode extends AbstractXDSNode
{
  XmlDataStruct parentXDS = null;

  protected XmlDataPath memberPath = null;

  protected String memberType = "string";

  protected HtmlBindTemp.XdsType xdsType = HtmlBindTemp.XdsType.inout;

  protected boolean bArray = false;

  protected int maxLen = -1;

  protected boolean bInNullable = true;

  protected boolean bOutNullable = true;

  public static XDSMemberNode createNode(XmlDataStruct curxds, XmlNode xn)
  {
    String noden = xn.getNodeName();

    if ("input".equalsIgnoreCase(noden)) {
      return new XDSMemberNodeTagInput(curxds, xn);
    }
    return new XDSMemberNodeDefault(curxds, xn);
  }

  public XDSMemberNode(XmlDataStruct curxds, XmlNode nn)
  {
    super(nn);

    this.parentXDS = curxds;

    String xdsmem = getAttribute("xds_member");
    if ((xdsmem == null) || (xdsmem.equals(""))) {
      throw new IllegalArgumentException("not XDS_MEMBER node");
    }
    parseXdsMember(xdsmem);

    String xdstype = getAttribute("xds_type");
    if ((xdstype != null) && (!xdstype.equals(""))) {
      this.xdsType = HtmlBindTemp.XdsType.valueOf(xdstype);
    }
    String str_barray = getAttribute("xds_is_array");
    if (str_barray != null) {
      this.bArray = "true".equals(str_barray);
    }
    if (this.memberType.equals("string"))
    {
      String strmaxlen = getAttribute("xds_maxlen");
      if ((strmaxlen == null) || (strmaxlen.equals(""))) {
        throw new IllegalArgumentException(
          "string member type must has xds_maxlen attribute");
      }
      this.maxLen = Integer.parseInt(strmaxlen);
    }

    String str_bin_null = getAttribute("xds_in_nullable");
    String str_bout_null = getAttribute("xds_out_nullable");
    if (str_bin_null != null)
    {
      this.bInNullable = (!"false".equals(str_bin_null));
    }
    if (str_bout_null != null)
    {
      this.bOutNullable = (!"false".equals(str_bout_null));
    }

    if ((str_bin_null == null) && (str_bout_null == null))
    {
      String str_bnull = getAttribute("xds_nullable");
      if (str_bnull != null)
        this.bInNullable = (this.bOutNullable = "false".equals(str_bnull) ? 0 : 1);
    }
  }

  private void parseXdsMember(String mb)
  {
    this.memberPath = new XmlDataPath(mb);

    String tmpxvt = this.memberPath.getXmlValType();
    if ((tmpxvt != null) && (!tmpxvt.equals(""))) {
      this.memberType = tmpxvt;
    }
    if (!XmlVal.isXmlValType(this.memberType)) {
      throw new IllegalArgumentException("String :" + this.memberType + 
        " is not XmlVal Type!");
    }
    if (this.memberPath.isStruct()) {
      throw new IllegalArgumentException("path=" + this.memberPath + 
        " is not member path");
    }
    this.bArray = this.memberPath.isValueArray();
    this.bInNullable = (this.bOutNullable = this.memberPath.isNullable());
  }

  public XmlDataStruct getParentXmlDataStruct()
  {
    return this.parentXDS;
  }

  public XmlDataPath getMemberPath()
  {
    return this.memberPath;
  }

  public String getMemberXmlValType()
  {
    return this.memberType;
  }

  public HtmlBindTemp.XdsType getXdsType()
  {
    return this.xdsType;
  }

  public boolean isArray()
  {
    return this.bArray;
  }

  public boolean isXdsInNullable()
  {
    return this.bInNullable;
  }

  public boolean isXdsOutNullable()
  {
    return this.bOutNullable;
  }

  public int getMaxLen()
  {
    return this.maxLen;
  }

  public abstract void writeMember(XmlData paramXmlData1, XmlData paramXmlData2, XmlDataPath paramXmlDataPath, Writer paramWriter)
    throws IOException;
}
