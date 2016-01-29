package com.dw.biz.bindview;

import com.dw.biz.BizOutput;
import com.dw.mltag.AbstractNode;
import com.dw.mltag.Attr;
import com.dw.mltag.MltagUtil;
import com.dw.mltag.NodeParser;
import com.dw.mltag.XmlNode;
import com.dw.mltag.XmlText;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlDataStruct.StoreType;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class HtmlBindTemp
{
  public static final String XDS_MEMBER = "xds_member";
  public static final String XDS_STRUCT = "xds_struct";
  public static final String XDS_TYPE = "xds_type";
  public static final String XDS_IS_ARRAY = "xds_is_array";
  public static final String XDS_NULLABLE = "xds_nullable";
  public static final String XDS_MAXLEN = "xds_maxlen";
  public static final String XDS_IN_NULLABLE = "xds_in_nullable";
  public static final String XDS_OUT_NULLABLE = "xds_out_nullable";
  public static final String XDS_OUTPUT_NAME = "xds_output_name";
  public static final String XDS_OUTPUT_VAL = "xds_output_val";
  XmlDataStruct inputXDS = new XmlDataStruct();

  XmlDataStruct outputXDS = new XmlDataStruct();

  ArrayList<BizOutput> outputs = new ArrayList();

  String strCont = null;

  String codeBehind = null;

  private transient XmlNode rootNode = null;

  public HtmlBindTemp(String htmlstr)
  {
    this.strCont = htmlstr;
  }

  public void init() throws Exception
  {
    if ((this.strCont == null) || (this.strCont.equals(""))) {
      return;
    }
    StringReader sr = new StringReader(this.strCont);
    NodeParser np = new NodeParser(sr);
    np.parse();

    this.rootNode = np.getRoot();

    initXDS();

    this.outputs.add(0, new BizOutput("submit", this.outputXDS));
  }

  private void setOtherOutputStruct(String outputn, XmlDataStruct xds)
  {
    int c = this.outputs.size();
    for (int i = 0; i < c; i++)
    {
      BizOutput bo = (BizOutput)this.outputs.get(i);
      if (bo.getName().equals(outputn))
      {
        bo.setOutputDataStruct(xds);
        return;
      }
    }

    this.outputs.add(new BizOutput(outputn, xds));
  }

  private void initXDS()
  {
    if (this.rootNode == null) {
      return;
    }
    initXDS(this.rootNode, this.inputXDS, this.outputXDS);
  }

  private void initXDS(AbstractNode curn, XmlDataStruct cur_input_xds, XmlDataStruct cur_output_xds)
  {
    if ((curn instanceof XmlNode))
    {
      XmlNode xn = (XmlNode)curn;

      initXDSForXmlNode(xn, cur_input_xds, cur_output_xds);

      return;
    }

    if ((curn instanceof XmlText))
    {
      XmlText xt = (XmlText)curn;
      if (XDSStr.checkHasXDSStr(xt.getText()))
      {
        XDSText xdstxt = new XDSText(xt);

        List inputps = xdstxt.getXDSTextValue().getXmlDataPath();
        if (inputps != null)
        {
          for (XmlDataPath xdp : inputps)
          {
            XmlDataStruct tmpxds = this.inputXDS;
            if (xdp.isRelative())
            {
              tmpxds = cur_input_xds;
            }

            tmpxds.setByPath(xdp, xdp.getXmlValType(), xdp.isNullable(), 2147483647, XmlDataStruct.StoreType.Normal);
          }
        }
      }

      return;
    }
  }

  private void initXDSForXmlNode(XmlNode xn, XmlDataStruct cur_input_xds, XmlDataStruct cur_output_xds)
  {
    String struct = xn.getAttribute("xds_struct");
    String member = xn.getAttribute("xds_member");
    String output = xn.getAttribute("xds_output_name");
    if ((struct != null) && (!(struct = struct.trim()).equals("")))
    {
      XDSStructNode xdssn = new XDSStructNode(xn);

      XmlDataPath xdp = xdssn.getStructPath();
      XmlDataStruct inxds = this.inputXDS;
      XmlDataStruct outxds = this.outputXDS;
      if (xdp.isRelative())
      {
        inxds = cur_input_xds;
        outxds = cur_output_xds;
      }

      cur_input_xds = inxds.setSubStructByPath(xdp, xdssn.isNullable());
      cur_output_xds = outxds.setSubStructByPath(xdp, xdssn.isNullable());

      xn = xdssn;

      initXDSForXmlNodeAttrs(xn, cur_input_xds);
    }
    else
    {
      XmlDataStruct outxds;
      if ((member != null) && (!(member = member.trim()).equals("")))
      {
        XDSMemberNode xdsmn = XDSMemberNode.createNode(cur_output_xds, xn);

        XmlDataPath xdp = xdsmn.getMemberPath();
        XmlDataStruct inxds = this.inputXDS;
        outxds = this.outputXDS;
        if (xdp.isRelative())
        {
          inxds = cur_input_xds;
          outxds = cur_output_xds;
        }

        XdsType xdst = xdsmn.getXdsType();
        if ((xdst == XdsType.in) || (xdst == XdsType.inout)) {
          inxds.setByPath(xdp, xdsmn.getMemberXmlValType(), xdsmn.isXdsInNullable(), xdsmn.getMaxLen(), XmlDataStruct.StoreType.Normal);
        }
        if ((xdst == XdsType.out) || (xdst == XdsType.inout)) {
          outxds.setByPath(xdp, xdsmn.getMemberXmlValType(), xdsmn.isXdsOutNullable(), xdsmn.getMaxLen(), XmlDataStruct.StoreType.Normal);
        }
        xn = xdsmn;

        initXDSForXmlNodeAttrs(xn, cur_input_xds);
      }
      else if ((output != null) && (!(output = output.trim()).equals("")))
      {
        XDSOutputNode xdson = new XDSOutputNode(xn);

        setOtherOutputStruct(xdson.getOutputName(), xdson.getOutputDataStruct());

        List inputps = xdson.getInputPaths();
        for (XmlDataPath xdp : inputps)
        {
          XmlDataStruct inxds = this.inputXDS;

          if (xdp.isRelative())
          {
            inxds = cur_input_xds;
          }

          inxds.setByPath(xdp, xdp.getXmlValType(), xdp.isNullable(), 2147483647, XmlDataStruct.StoreType.Normal);
        }

        xn = xdson;
      }

    }

    int cc = xn.getChildCount();
    for (int i = 0; i < cc; i++)
    {
      AbstractNode cn = (AbstractNode)xn.getChildAt(i);
      initXDS(cn, cur_input_xds, cur_output_xds);
    }
  }

  private void initXDSForXmlNodeAttrs(XmlNode xn, XmlDataStruct cur_input_xds)
  {
    ArrayList maybe_xds = new ArrayList();
    String an;
    for (Enumeration en = xn.getAtributeNames(); en.hasMoreElements(); )
    {
      an = (String)en.nextElement();
      Attr a = xn.getAttr(an);
      if (XDSStr.checkHasXDSStr(a.getValue()))
      {
        maybe_xds.add(a);
      }
    }
    for (Attr a : maybe_xds)
    {
      XDSAttr xdsa = new XDSAttr(a.getName(), a.getValue());
      xn.setAttr(xdsa);

      List inputps = xdsa.getXDSStrValue().getXmlDataPath();
      if (inputps != null)
      {
        for (XmlDataPath xdp : inputps)
        {
          XmlDataStruct tmpxds = this.inputXDS;
          if (xdp.isRelative())
          {
            tmpxds = cur_input_xds;
          }

          tmpxds.setByPath(xdp, xdp.getXmlValType(), xdp.isNullable(), 2147483647, XmlDataStruct.StoreType.Normal);
        }
      }
    }
  }

  public String getStrCont()
  {
    return this.strCont;
  }

  public XmlDataStruct getInputDataStruct()
  {
    return this.inputXDS;
  }

  public List<BizOutput> getOutputDataStruct()
  {
    return this.outputs;
  }

  public void writeInputToView(XmlData inputxd, Writer w)
    throws IOException
  {
    writeInputToView(this.rootNode, inputxd, inputxd, new XmlDataPath(), w);
  }

  private static void writeInputToView(AbstractNode curn, XmlData inputxd, XmlData cur_parent, XmlDataPath cur_xd_path, Writer w)
    throws IOException
  {
    if ((curn instanceof XDSMemberNode))
    {
      ((XDSMemberNode)curn).writeMember(inputxd, cur_parent, cur_xd_path, w);
    }
    else if ((curn instanceof XDSStructNode))
    {
      writeStruct((XDSStructNode)curn, inputxd, cur_parent, cur_xd_path, w);
    }
    else if ((curn instanceof XDSOutputNode))
    {
      writeOutput((XDSOutputNode)curn, inputxd, cur_parent, cur_xd_path, w);
    }
    else
    {
      writeNormal(curn, inputxd, cur_parent, cur_xd_path, w);
    }
  }

  private static void writeStruct(XDSStructNode structn, XmlData inputxd, XmlData cur_parent, XmlDataPath cur_xd_path, Writer w)
    throws IOException
  {
    if (structn.isXdsArray())
    {
      XmlDataPath bp = structn.getStructPath();

      XmlDataPath next_p = null;

      List xds = null;
      if (bp.isRelative())
      {
        if (cur_parent != null)
          xds = cur_parent.getSubDataArrayByPath(bp);
        next_p = cur_xd_path.appendRelativePath(bp);
      }
      else
      {
        if (inputxd != null)
          xds = inputxd.getSubDataArrayByPath(bp);
        next_p = bp;
      }

      if ((xds == null) || (xds.size() <= 0))
      {
        return;
      }

      int n = xds.size();
      for (int i = 0; i < n; i++)
      {
        XmlData xd = (XmlData)xds.get(i);
        writeNormal(structn, inputxd, xd, next_p.getArrayIdxPath(i), w);
      }
    }
    else
    {
      XmlDataPath bp = structn.getStructPath();
      XmlDataPath next_p = null;
      XmlData tmp_p = null;
      if (bp.isRelative())
      {
        if (cur_parent != null)
          tmp_p = cur_parent.getSubDataByPath(bp);
        next_p = cur_xd_path.appendRelativePath(bp);
      }
      else
      {
        if (inputxd != null)
          tmp_p = inputxd.getSubDataByPath(bp);
        next_p = bp;
      }

      writeNormal(structn, inputxd, tmp_p, next_p, w);
    }
  }

  private static void writeOutput(XDSOutputNode outputn, XmlData inputxd, XmlData cur_parent, XmlDataPath cur_xd_path, Writer w)
    throws IOException
  {
    w.write(60);
    w.write(outputn.getNodeName());

    for (Enumeration en = outputn.getAtributeNames(); en.hasMoreElements(); )
    {
      String attrn = (String)en.nextElement();
      Attr attr = outputn.getAttr(attrn);

      writeAttr(inputxd, cur_parent, w, outputn, attr);
    }

    Attr att = new Attr();
    att.setName("onclick");
    att.setValue(outputn
      .calOutputAttrValue(inputxd, 
      cur_parent, cur_xd_path));

    writeAttr(inputxd, cur_parent, w, outputn, att);

    int cc = outputn.getChildCount();
    if (cc <= 0)
    {
      w.write("/>");
    }
    else
    {
      w.write(62);
    }

    writeXmlNodeChild(inputxd, cur_parent, cur_xd_path, w, outputn);
    writeXmlNodeEnd(w, outputn);
  }

  protected static void writeNormal(AbstractNode n, XmlData inputxd, XmlData cur_parent, XmlDataPath cur_xd_path, Writer w)
    throws IOException
  {
    if ((n instanceof XmlNode))
    {
      XmlNode xn = (XmlNode)n;
      writeXmlNode(inputxd, cur_parent, cur_xd_path, w, xn);
      return;
    }

    if ((n instanceof XmlText))
    {
      XmlText xt = (XmlText)n;
      writeXmlText(inputxd, cur_parent, w, xt);
      return;
    }

    w.write(n.toString());
  }

  protected static void writeXmlNode(XmlData inputxd, XmlData cur_parent, XmlDataPath cur_xd_path, Writer w, XmlNode xn)
    throws IOException
  {
    writeXmlNodeBegin(inputxd, cur_parent, w, xn);

    writeXmlNodeChild(inputxd, cur_parent, cur_xd_path, w, xn);

    writeXmlNodeEnd(w, xn);
  }

  static void writeXmlNodeBegin(XmlData inputxd, XmlData cur_parent, Writer w, XmlNode xn) throws IOException
  {
    w.write(60);
    w.write(xn.getNodeName());

    for (Enumeration en = xn.getAtributeNames(); en.hasMoreElements(); )
    {
      String attrn = (String)en.nextElement();
      Attr attr = xn.getAttr(attrn);

      writeAttr(inputxd, cur_parent, w, xn, attr);
    }

    int cc = xn.getChildCount();
    if (cc <= 0)
    {
      w.write("/>");
    }
    else
    {
      w.write(62);
    }
  }

  static void writeXmlNodeChild(XmlData inputxd, XmlData cur_parent, XmlDataPath cur_xd_path, Writer w, XmlNode xn) throws IOException
  {
    int c = xn.getChildCount();
    for (int i = 0; i < c; i++)
    {
      AbstractNode tmpn = (AbstractNode)xn.getChildAt(i);
      writeInputToView(tmpn, inputxd, cur_parent, cur_xd_path, w);
    }
  }

  static void writeXmlNodeEnd(Writer w, XmlNode xn) throws IOException
  {
    if (xn.getChildCount() > 0)
    {
      w.write("</");
      w.write(xn.getNodeName());
      w.write(">");
    }
  }

  static void writeAttr(XmlData inputxd, XmlData cur_parent, Writer w, XmlNode xn, Attr attr) throws IOException
  {
    w.write(" ");
    w.write(attr.getName());
    w.write("=\"");

    if ((attr instanceof XDSAttr))
    {
      XDSAttr xdsa = (XDSAttr)attr;
      XDSStr xdsstr = xdsa.getXDSStrValue();
      xdsstr.writeOut(inputxd, cur_parent, w);
    }
    else
    {
      String attrv = MltagUtil.xmlEncoding(xn.getAttribute(attr.getName()), 
        "><&'\"\r\n");

      w.write(attrv);
    }

    w.write("\"");
  }

  protected static void writeXmlText(XmlData inputxd, XmlData cur_parent, Writer w, XmlText xt)
    throws IOException
  {
    if (!(xt instanceof XDSText))
    {
      w.write(xt.toString());
      return;
    }

    XDSText xdst = (XDSText)xt;
    XDSStr xds = xdst.getXDSTextValue();
    xds.writeOut(inputxd, cur_parent, w);
  }

  public void readOutputFromSubmit(Properties submit_prop)
  {
  }

  public static enum XdsType
  {
    in, 
    out, 
    inout;
  }
}
