package com.dw.biz;

import com.dw.mltag.JspDirective;
import com.dw.mltag.NodeParser;
import com.dw.mltag.XmlNode;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XRmi(reg_name="biz_action")
public class BizAction extends BizNodeObj
  implements IBizFile
{
  String title = null;

  String desc = null;

  String type = "java_jsp";

  String strCont = null;

  BizAttr attr = BizAttr.logic_only;

  XmlDataStruct inputDataStruct = new XmlDataStruct();

  ArrayList<BizOutput> outputs = new ArrayList();

  String[] results = null;

  private transient XmlNode contTreeRoot = null;

  transient boolean bValid = false;

  transient XmlDataStruct inputXD = null; transient XmlDataStruct outputXD = null;

  public BizAction()
  {
  }

  public BizAction(String title, String desc, String type, String strcont, BizAttr attr, XmlDataStruct inputxds, List<BizOutput> outputs)
  {
    this.title = title;
    this.desc = desc;
    if ((type == null) || (type.equals("")))
      this.type = "java_jsp";
    else
      this.type = type;
    this.strCont = strcont;

    this.attr = attr;

    this.inputDataStruct = inputxds;

    if (outputs != null) {
      this.outputs.addAll(outputs);
    }
    for (BizOutput op : this.outputs)
    {
      op.belongTo = this;
    }
  }

  public void initView() throws Exception
  {
    if ((this.strCont == null) || (this.strCont.equals("")));
  }

  public void init(BizManager bizmgr)
    throws Exception
  {
    initView();

    this.bValid = true;
  }

  public XmlNode getContTreeNodeRoot()
    throws IOException
  {
    if (this.contTreeRoot != null) {
      return this.contTreeRoot;
    }
    synchronized (this)
    {
      if (this.contTreeRoot != null) {
        return this.contTreeRoot;
      }
      if (this.strCont == null) {
        return null;
      }
      StringReader sr = new StringReader(this.strCont);
      NodeParser parser = new NodeParser(sr);

      parser.setIgnoreCase(true);

      parser.parse();

      this.contTreeRoot = parser.getRoot();
      return this.contTreeRoot;
    }
  }

  public boolean isValid()
  {
    return this.bValid;
  }

  public String getType()
  {
    return this.type;
  }

  public void setType(String t)
  {
    this.type = t;
  }

  public String getTitle()
  {
    if (this.title != null) {
      return this.title;
    }
    try
    {
      XmlNode xn = getContTreeNodeRoot();
      List ll = xn.getSubXmlNodeByType(JspDirective.class);
      if (ll != null)
      {
        for (Iterator localIterator = ll.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

          JspDirective jd = (JspDirective)o;
          if ("action".equalsIgnoreCase(jd.getJspDirectiveType())) {
            this.title = jd.getAttribute("title");
          }
        }
      }
    }
    catch (IOException localIOException)
    {
    }
    if (this.title == null)
      this.title = "";
    return this.title;
  }

  public void setTitle(String t)
  {
    this.title = t;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public void setDesc(String d)
  {
    this.desc = d;
  }

  public BizAttr getAttr()
  {
    return this.attr;
  }

  public void setAttr(BizAttr ba)
  {
    this.attr = ba;
  }

  public String getStrCont()
  {
    return this.strCont;
  }

  public void setStrCont(String strc)
  {
    this.strCont = strc;
  }

  public XmlDataStruct getInputDataStruct()
  {
    if (this.inputXD != null) {
      return this.inputXD;
    }
    try
    {
      this.inputXD = getBizContainer().getBizActionInputDataStruct(getBizPathStr());
      return this.inputXD;
    }
    catch (Exception ee) {
    }
    return null;
  }

  public XmlDataStruct getOutputDataStruct()
  {
    if (this.outputXD != null) {
      return this.outputXD;
    }
    try
    {
      this.outputXD = getBizContainer().getBizActionOutputDataStruct(getBizPathStr());
      return this.outputXD;
    }
    catch (Exception ee) {
    }
    return null;
  }

  public void setInputDataStruct(XmlDataStruct xds)
  {
    if (xds != null)
      this.inputDataStruct = xds;
    else
      this.inputDataStruct = new XmlDataStruct();
  }

  public BizOutput[] getOutputs()
  {
    BizOutput[] rets = new BizOutput[this.outputs.size()];
    this.outputs.toArray(rets);
    return rets;
  }

  public BizOutput getOneOutput()
  {
    if (this.outputs.size() <= 0) {
      return null;
    }
    return (BizOutput)this.outputs.get(0);
  }

  public void setOutputs(List<BizOutput> outs)
  {
    if (this.outputs == null) {
      this.outputs = new ArrayList();
    }
    this.outputs.clear();

    if (outs != null)
      this.outputs.addAll(outs);
  }

  public BizOutput addOutput(String output_name, XmlDataStruct outputxds)
    throws Exception
  {
    if ((output_name == null) || (output_name.equals(""))) {
      throw new Exception("Oper Name Cannot be null or empty!");
    }
    if (outputxds == null) {
      outputxds = new XmlDataStruct();
    }
    BizOutput op = getOutput(output_name);
    if (op != null)
    {
      throw new Exception("Oper with name=" + output_name + 
        " is already existed!");
    }

    return setOutput(output_name, outputxds);
  }

  public BizOutput setOutput(String output_name, XmlDataStruct outputxds)
  {
    BizOutput op = getOutput(output_name);

    if (outputxds == null)
    {
      if (op != null)
        this.outputs.remove(op);
      return null;
    }

    if (op != null)
    {
      op.setOutputDataStruct(outputxds);
      return op;
    }

    op = new BizOutput(output_name, outputxds);
    op.belongTo = this;

    this.outputs.add(op);

    return op;
  }

  public BizOutput getOutput(String output_name)
  {
    for (BizOutput op : this.outputs)
    {
      if (op.getName().equals(output_name))
        return op;
    }
    return null;
  }

  public String[] getResults()
  {
    return this.results;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    if (this.title != null) {
      xd.setParamValue("title", this.title);
    }
    if (this.type != null) {
      xd.setParamValue("type", this.type);
    }
    if (this.desc != null)
      xd.setParamValue("desc", this.desc);
    if (this.strCont != null) {
      xd.setParamValue("cont", this.strCont);
    }
    xd.setParamValue("attr", Integer.valueOf(this.attr.getValue()));
    if (this.results != null)
    {
      xd.setParamValues("result", this.results);
    }

    if (this.inputDataStruct != null)
    {
      xd.setSubDataSingle("input_data_struct", 
        this.inputDataStruct.toXmlData());
    }

    if (this.outputs != null)
    {
      List xds = xd.getOrCreateSubDataArray("outputs");
      for (BizOutput op : this.outputs)
      {
        xds.add(op.toXmlData());
      }

    }

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.title = xd.getParamValueStr("title");

    this.type = xd.getParamValueStr("type");
    if ((this.type == null) || (this.type.equals(""))) {
      this.type = "java_jsp";
    }
    this.desc = xd.getParamValueStr("desc");
    this.strCont = xd.getParamValueStr("cont");

    this.attr = BizAttr.valueOf(xd.getParamValueInt32("attr", 1));

    this.results = xd.getParamValuesStr("result");

    XmlData tmpxd = xd.getSubDataSingle("input_data_struct");
    if (tmpxd != null)
    {
      this.inputDataStruct = new XmlDataStruct();
      this.inputDataStruct.fromXmlData(tmpxd);
    }

    List<XmlData> operxds = xd.getSubDataArray("outputs");
    if (operxds != null)
    {
      for (XmlData xd0 : operxds)
      {
        BizOutput tmpop = new BizOutput();
        tmpop.fromXmlData(xd0);
        tmpop.belongTo = this;

        this.outputs.add(tmpop);
      }
    }
  }

  public void fromFileContent(byte[] cont)
  {
    try
    {
      this.strCont = new String(cont, "UTF-8");
    }
    catch (Exception localException)
    {
    }
  }
}
