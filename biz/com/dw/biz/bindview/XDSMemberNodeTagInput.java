package com.dw.biz.bindview;

import com.dw.mltag.MltagUtil;
import com.dw.mltag.XmlNode;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;
import java.io.IOException;
import java.io.Writer;

public class XDSMemberNodeTagInput extends XDSMemberNode
{
  boolean bPsw = false;

  public XDSMemberNodeTagInput(XmlDataStruct curxds, XmlNode nn)
  {
    super(curxds, nn);

    String input_type = getAttribute("type");
    this.bPsw = "password".equalsIgnoreCase(input_type);

    if (this.memberPath.isStruct()) {
      throw new IllegalArgumentException("member path=" + this.memberPath.toString() + " is struct which cannot be used in <input>");
    }

    removeAttribute("name");
    removeAttribute("type");
  }

  private String getInputName(XmlDataPath cur_struct_path)
  {
    if (this.memberPath.isRelative())
    {
      return cur_struct_path.toString() + this.memberPath.toString() + ":" + getMemberXmlValType();
    }

    return this.memberPath.toString() + ":" + getMemberXmlValType();
  }

  public void writeMember(XmlData inputxd, XmlData cur_parent, XmlDataPath cur_parent_path, Writer w)
    throws IOException
  {
    w.write("<input type=\"");
    if (this.bPsw)
      w.write("password\"");
    else {
      w.write("text\"");
    }
    if (this.xdsType == HtmlBindTemp.XdsType.in)
    {
      w.write(" name=0");
    }
    else
    {
      w.write(" name=\"");
      w.write(getInputName(cur_parent_path));
      w.write("\"");
    }

    XmlData vxd = inputxd;
    if (this.memberPath.isRelative())
    {
      vxd = cur_parent;
    }

    String tmpv = "";

    if (vxd != null)
    {
      Object ov = vxd.getParamValueByPath(this.memberPath);
      if (ov != null) {
        tmpv = ov.toString();
      }
    }
    w.write(" value=\"");
    w.write(MltagUtil.xmlEncoding(tmpv));
    w.write("\"");

    w.write("/>");
  }
}
