package com.dw.biz;

import com.dw.mltag.AbstractNode;
import com.dw.mltag.NodeParser;
import com.dw.mltag.XmlNode;
import com.dw.system.gdb.datax.DataXBase;
import com.dw.system.gdb.datax.DataXClass;
import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataMember;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.xrmi.XRmi;
import java.io.StringReader;
import java.util.ArrayList;

@XRmi(reg_name="biz_form")
public class BizForm extends BizNodeObj
  implements IXmlDataable
{
  public static final String BIND_DATAX_BASE = "datax_base";
  public static final String BIND_DATAX_CLASS = "datax_class";
  public static final String BIND_DATAX_MEMBER = "datax_member";
  public static final String BIND_DATAX_STRUCT = "datax_struct";
  FormType formType = FormType.html;
  String desc = null;

  String strCont = null;

  String performer = null;

  transient ArrayList<BizAction> bizActs = new ArrayList();

  private transient XmlNode rootNode = null;

  transient DataXBase bindDataXBase = null;
  transient DataXClass bindDataXClass = null;

  transient boolean bValid = false;

  public BizForm()
  {
  }

  public BizForm(FormType ft, String desc, String strcont, String performer)
  {
    this.formType = ft;
    this.desc = desc;

    this.strCont = strcont;
    this.performer = performer;
  }

  public void initFormView() throws Exception
  {
    if (this.formType != FormType.html) {
      return;
    }
    if ((this.strCont == null) || (this.strCont.equals(""))) {
      return;
    }
    StringReader sr = new StringReader(this.strCont);
    NodeParser np = new NodeParser(sr);
    np.parse();

    this.rootNode = np.getRoot();
  }

  public void init(BizManager bizmgr)
    throws Exception
  {
    initFormView();

    initStruct(bizmgr);

    this.bValid = true;
  }

  private void initStruct(BizManager bizmgr)
    throws Exception
  {
    if (this.formType != FormType.html) {
      return;
    }
    if (this.rootNode == null) {
      throw new Exception("cannot find root node,may be it has no form content!");
    }
    XmlNode xn = this.rootNode.getFirstXmlNode();
    if (xn == null) {
      throw new Exception("cannot find first node,may be it has no form content!");
    }
    DataXManager dxmgr = bizmgr.getDataXManager();

    String datab = xn.getAttribute("datax_base");
    if ((datab == null) || (datab.equals(""))) {
      throw new Exception("no attribute with name=datax_base found in root tag");
    }
    this.bindDataXBase = dxmgr.getDataXBaseByName(datab);
    if (this.bindDataXBase == null) {
      throw new Exception("cannot find DataXBase with name=" + datab);
    }
    String datac = xn.getAttribute("datax_class");
    if ((datac == null) || (datac.equals(""))) {
      throw new Exception("no attribute with name=datax_class found in root tag");
    }
    this.bindDataXClass = this.bindDataXBase.getDataXClassByName(datac);
    if (this.bindDataXClass == null) {
      throw new Exception("cannot find DataXClass with name=" + datac + " in DataXBase=" + datab);
    }
    XmlDataStruct xds = this.bindDataXClass.getDataStruct();

    checkNode(xn, xds);
  }

  private void checkNode(XmlNode xn, XmlDataStruct curxds) throws Exception
  {
    String dxm = xn.getAttribute("datax_member");
    if (dxm != null)
    {
      XmlDataMember si = curxds.getXmlDataMember(dxm);
      if (si == null)
        throw new Exception("cannot find datax_member=" + dxm + " in datax_struct=" + curxds.getName());
    }
    else
    {
      String dxs = xn.getAttribute("datax_struct");
      if (dxs != null)
      {
        XmlDataStruct subxds = curxds.getSubStruct(dxs);
        if (subxds == null) {
          throw new Exception("cannot find datax_struct=" + dxs + " in datax_struct=" + curxds.getName());
        }
        curxds = subxds;
      }

    }

    int cc = xn.getChildCount();
    for (int i = 0; i < cc; i++)
    {
      AbstractNode tmpxn = (AbstractNode)xn.getChildAt(i);
      if ((tmpxn instanceof XmlNode))
      {
        checkNode((XmlNode)tmpxn, curxds);
      }
    }
  }

  public DataXBase getDataXBase() {
    return this.bindDataXBase;
  }

  public DataXClass getDataXClass()
  {
    return this.bindDataXClass;
  }

  public boolean isValid()
  {
    return this.bValid;
  }

  public FormType getFormType()
  {
    return this.formType;
  }

  public void setFormType(FormType ft)
  {
    this.formType = ft;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public void setDesc(String d)
  {
    this.desc = d;
  }

  public String getStrCont()
  {
    return this.strCont;
  }

  public void setStrCont(String strc)
  {
    this.strCont = strc;
  }

  public String getPerformer()
  {
    return this.performer;
  }

  public void setPerformer(String pf)
  {
    this.performer = pf;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    xd.setParamValue("type", this.formType.toString());
    if (this.desc != null)
      xd.setParamValue("desc", this.desc);
    if (this.strCont != null)
      xd.setParamValue("cont", this.strCont);
    if (this.performer != null) {
      xd.setParamValue("performer", this.performer);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    String stype = xd.getParamValueStr("type");
    if ((stype != null) && (!stype.equals("")))
      this.formType = FormType.valueOf(stype);
    else {
      this.formType = FormType.html;
    }
    this.desc = xd.getParamValueStr("desc");
    this.strCont = xd.getParamValueStr("cont");
    this.performer = xd.getParamValueStr("performer");
  }

  public static enum FormType
  {
    html, 
    jsp, 
    xml, 
    flash;
  }

  public static enum FormOption
  {
    view, 
    add, 
    update;
  }
}
