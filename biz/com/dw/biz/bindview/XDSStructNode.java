package com.dw.biz.bindview;

import com.dw.mltag.XmlNode;
import com.dw.system.xmldata.XmlDataPath;

public class XDSStructNode extends AbstractXDSNode
{
  private XmlDataPath structPath = null;

  private boolean bArray = false;

  private boolean bNullable = true;

  public XDSStructNode(XmlNode nn)
  {
    super(nn);

    String strp = getAttribute("xds_struct");
    if ((strp == null) || (strp.equals(""))) {
      throw new IllegalArgumentException("not XDS_STRUCT node");
    }
    this.structPath = new XmlDataPath(strp);
    if (!this.structPath.isStruct()) {
      throw new IllegalArgumentException("path=" + strp + " is not struct path");
    }
    this.bArray = this.structPath.isValueArray();
    this.bNullable = this.structPath.isNullable();

    String strbarray = getAttribute("xds_is_array");
    if (strbarray != null) {
      this.bArray = "true".equals(strbarray);
    }
    String strnullable = getAttribute("xds_nullable");
    if (strnullable != null)
      this.bNullable = (!"false".equals(strnullable));
  }

  public XmlDataPath getStructPath()
  {
    return this.structPath;
  }

  public boolean isXdsArray()
  {
    return this.bArray;
  }

  public boolean isNullable()
  {
    return this.bNullable;
  }
}
