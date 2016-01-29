package com.dw.biz.bindview;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XDSStr
{
  ArrayList strs_paths = new ArrayList();

  ArrayList<XmlDataPath> paths = new ArrayList();

  public static boolean checkHasXDSStr(String txt)
  {
    if (txt == null) {
      return false;
    }
    return txt.indexOf("{@") >= 0;
  }

  public XDSStr(String txt)
  {
    int p = 0; int sp = 0; int ep = 0;

    while ((p = txt.indexOf("{@", sp)) >= 0)
    {
      ep = txt.indexOf('}', p + 2);
      if (ep <= 0) {
        break;
      }
      if (p > sp)
      {
        this.strs_paths.add(txt.substring(sp, p));
      }

      String strp = txt.substring(p + 2, ep).trim();
      XmlDataPath xdp = new XmlDataPath(strp);
      if (xdp.isStruct()) {
        throw new IllegalArgumentException("{@" + strp + "} in text must be member,but it is a struct!");
      }
      if (xdp.isValueArray()) {
        throw new IllegalArgumentException("{@" + strp + "} in text must be single value path!");
      }
      this.strs_paths.add(xdp);
      this.paths.add(xdp);

      sp = ep + 1;
    }

    this.strs_paths.add(txt.substring(sp));
  }

  public List<XmlDataPath> getXmlDataPath()
  {
    return this.paths;
  }

  public void writeOut(XmlData inputxd, XmlData cur_xd, Writer w)
    throws IOException
  {
    for (Iterator localIterator = this.strs_paths.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

      if ((o instanceof String))
      {
        w.write((String)o);
      }
      else if ((o instanceof XmlDataPath))
      {
        XmlDataPath xdp = (XmlDataPath)o;
        XmlData vxd = inputxd;
        if (xdp.isRelative())
        {
          vxd = cur_xd;
        }

        if (vxd != null)
        {
          Object ov = vxd.getParamValueByPath(xdp);
          if (ov != null)
            w.write(ov.toString());
        }
      }
    }
  }
}
