package com.dw.biz.bindview;

import com.dw.mltag.XmlNode;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlDataStruct.StoreType;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class XDSOutputNode extends AbstractXDSNode
{
  String outputName = null;

  Hashtable<XmlDataPath, Object> outputPath2Val = new Hashtable();

  XmlDataStruct outputXDS = new XmlDataStruct();

  ArrayList<XmlDataPath> inputPaths = new ArrayList();

  public XDSOutputNode(XmlNode nn)
  {
    super(nn);

    this.outputName = getAttribute("xds_output_name");
    if ((this.outputName == null) || (this.outputName.equals(""))) {
      throw new IllegalArgumentException("Output Node must has attribute=xds_output_name");
    }

    String stroutval = getAttribute("xds_output_val");
    if ((stroutval != null) && (!(stroutval = stroutval.trim()).equals("")))
    {
      StringTokenizer tmpst = new StringTokenizer(stroutval, ",|");
      while (tmpst.hasMoreTokens())
      {
        String tmps = tmpst.nextToken();
        int p = tmps.indexOf('=');
        if (p >= 0)
        {
          String outp = tmps.substring(0, p).trim();
          String inv = tmps.substring(p + 1).trim();
          if ((outp.startsWith("{")) && (outp.endsWith("}")))
          {
            outp = outp.substring(1, outp.length() - 1);
            XmlDataPath outxdp = new XmlDataPath(outp);
            if (outxdp.isStruct())
              throw new IllegalArgumentException("output path must be member in output node:" + stroutval);
            if (outxdp.isValueArray())
              throw new IllegalArgumentException("output path must be single val in output node:" + stroutval);
            if (outxdp.isRelative()) {
              throw new IllegalArgumentException("output path must be absolute path in output node:" + stroutval);
            }
            Object inobj = null;
            boolean bnullable = true;
            if (inv.startsWith("{@"))
            {
              if (!inv.endsWith("}")) {
                throw new IllegalArgumentException("input path must be like {@/xxx/xxx} or constant string in output node:" + stroutval);
              }
              XmlDataPath inxdp = new XmlDataPath(inv.substring(2, inv.length() - 1));
              if (outxdp.isStruct())
                throw new IllegalArgumentException("input path must be member in output node:" + stroutval);
              if (outxdp.isValueArray()) {
                throw new IllegalArgumentException("input path must be single val in output node:" + stroutval);
              }
              inobj = inxdp;
              this.inputPaths.add(inxdp);
              bnullable = inxdp.isNullable();
            }
            else
            {
              inobj = inv;
              bnullable = false;
            }

            this.outputXDS.setByPath(outxdp, outxdp.getXmlValType(), bnullable, 1, XmlDataStruct.StoreType.Normal);

            this.outputPath2Val.put(outxdp, inobj);
          }
        }
      }
    }
    removeAttribute("xds_output_name");
    removeAttribute("xds_output_val");
  }

  public String getOutputName()
  {
    return this.outputName;
  }

  public XmlDataStruct getOutputDataStruct()
  {
    return this.outputXDS;
  }

  public List<XmlDataPath> getInputPaths()
  {
    return this.inputPaths;
  }

  public String calOutputAttrValue(XmlData inputxd, XmlData cur_parent, XmlDataPath cur_xd_path)
  {
    StringBuffer tmpsb = new StringBuffer();
    tmpsb.append("xds_submit_output('")
      .append(getOutputName()).append("','");

    for (Map.Entry pv : this.outputPath2Val.entrySet())
    {
      XmlDataPath p = (XmlDataPath)pv.getKey();
      Object v = pv.getValue();
      if ((v instanceof String))
      {
        tmpsb.append(p.toFullString()).append('=').append((String)v).append(',');
      }
      else if ((v instanceof XmlDataPath))
      {
        XmlDataPath mxdp = (XmlDataPath)v;
        XmlData vxd = inputxd;
        if (mxdp.isRelative()) {
          vxd = cur_parent;
        }
        String sv = "";
        if (vxd != null)
          sv = vxd.getParamXmlValStrByPath(mxdp);
        tmpsb.append(p.toFullString()).append('=').append(sv).append(',');
      }
    }

    tmpsb.deleteCharAt(tmpsb.length() - 1);
    tmpsb.append("')");

    return tmpsb.toString();
  }
}
