package com.dw.biz.bindview;

import com.dw.mltag.XmlNode;
import com.dw.mltag.XmlText;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

class XDSMemberNodeDefault extends XDSMemberNode
{
  public XDSMemberNodeDefault(XmlDataStruct curxds, XmlNode nn)
  {
    super(curxds, nn);

    this.xdsType = HtmlBindTemp.XdsType.in;

    removeAllChildren();
    XmlText xt = new XmlText(" ");
    addChild(xt);
  }

  public void writeMember(XmlData inputxd, XmlData cur_parent, XmlDataPath cur_parent_path, Writer w)
    throws IOException
  {
    XmlData vxd = inputxd;
    if (this.memberPath.isRelative()) {
      vxd = cur_parent;
    }
    if (this.memberPath.isValueArray())
    {
      List vs = vxd.getParamValuesByPath(this.memberPath);
      if ((vs == null) || (vs.size() <= 0)) {
        return;
      }
      for (Iterator localIterator = vs.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

        HtmlBindTemp.writeXmlNodeBegin(inputxd, cur_parent, w, this);
        w.write(o.toString());
        HtmlBindTemp.writeXmlNodeEnd(w, this);
      }
    }
    else
    {
      Object o = vxd.getParamValueByPath(this.memberPath);
      if (o != null)
      {
        HtmlBindTemp.writeXmlNodeBegin(inputxd, cur_parent, w, this);
        w.write(o.toString());
        HtmlBindTemp.writeXmlNodeEnd(w, this);
      }
    }
  }
}
