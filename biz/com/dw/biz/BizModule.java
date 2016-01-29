package com.dw.biz;

import com.dw.system.Convert;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlHelper;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.Hashtable;
import org.w3c.dom.Element;

@XRmi(reg_name="biz_module")
public class BizModule extends BizNodeContainer
  implements IXmlDataable, Comparable<BizModule>
{
  public static final String BIZ_MODULE_FN = "web.xml";
  private String name = null;

  String title = null;

  String desc = null;

  String ver = null;

  String verDesc = null;

  BizModuleIO bizModuleIO = null;

  static BizModule loadBizModuleByIO(BizModuleIO bmio)
    throws Exception
  {
    byte[] cont = bmio.loadFile(null, "web.xml");
    if ((cont == null) || (cont.length <= 0))
    {
      cont = bmio.loadFile(null, "module.xml");
      if ((cont == null) || (cont.length <= 0)) {
        return null;
      }
      BizModule bm = new BizModule(bmio);
      XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");
      if (xd == null) {
        return null;
      }
      bm.fromXmlData(xd);
      bm.bizModuleIO = bmio;
      return bm;
    }

    BizModule bm = new BizModule(bmio);
    Element ele = XmlHelper.byteArrayToElement(cont);
    if (ele == null) {
      return null;
    }
    bm.name = ele.getAttribute("name");
    if (Convert.isNullOrEmpty(bm.name)) {
      bm.name = bmio.getUniqueId();
    }
    bm.title = ele.getAttribute("title");
    if (bm.title == null) {
      bm.title = bm.name;
    }
    bm.desc = ele.getAttribute("desc");
    bm.ver = ele.getAttribute("ver");
    bm.verDesc = ele.getAttribute("ver_desc");
    bm.bizModuleIO = bmio;
    return bm;
  }

  private BizModule(BizModuleIO bmio)
    throws Exception
  {
    super(null, null);

    this.bizModuleIO = bmio;
  }

  BizModule(BizModuleIO bmio, String name, String title, String desc, String ver, String ver_desc)
    throws Exception
  {
    this(bmio);

    if ((name == null) || (name.equals(""))) {
      throw new IllegalArgumentException("module name cannot be null");
    }
    this.name = name;
    this.title = title;
    this.desc = desc;
    this.ver = ver;
    this.verDesc = ver_desc;
  }

  protected String[] calMyCatPP(String[] parentcatpp)
  {
    return null;
  }

  public String getName()
  {
    return this.name;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public String getVer()
  {
    return this.ver;
  }

  public String getVerDesc()
  {
    return this.verDesc;
  }

  public BizModuleIO getBizModuleIO()
  {
    return this.bizModuleIO;
  }

  public void fireFileChanged(String[] catpp, String filename)
    throws Exception
  {
    BizNode bn = getBizNode(catpp, filename);
    if (bn == null) {
      return;
    }
    long ludp = this.bizModuleIO.getSubFileUpdateDate(catpp, filename);
    bn.lastUpdate = ludp;
    bn.bizObj = null;
  }

  public BizNode fireFileAdded(String[] catpp, String filename) throws Exception
  {
    String[] fnt = BizNode.FileName2NodeNameType(filename);

    BizNodeContainer bnc = getBizNodeContainerByPath(catpp);
    if (bnc == null) {
      throw new IllegalArgumentException("cannot get Node Container by path");
    }
    BizNode newbn = new BizNode(this, this, catpp, fnt[0], fnt[1], -1L);
    bnc.name2BizNode.put(newbn.getNodeFileName(), newbn);
    return newbn;
  }

  public void fireFileDeleted(String[] catpp, String filename) throws Exception
  {
    BizNodeContainer bnc = getBizNodeContainerByPath(catpp);
    if (bnc == null) {
      throw new IllegalArgumentException("cannot get Node Container by path");
    }
    bnc.name2BizNode.remove(filename);
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    xd.setParamValue("name", this.name);
    if (this.title != null)
      xd.setParamValue("title", this.title);
    if (this.desc != null)
      xd.setParamValue("desc", this.desc);
    if (this.ver != null)
      xd.setParamValue("ver", this.ver);
    if (this.verDesc != null) {
      xd.setParamValue("ver_desc", this.verDesc);
    }
    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.name = xd.getParamValueStr("name");
    this.title = xd.getParamValueStr("title");
    this.desc = xd.getParamValueStr("desc");
    this.ver = xd.getParamValueStr("ver");
    this.verDesc = xd.getParamValueStr("ver_desc");
  }

  public int compareTo(BizModule obm)
  {
    return this.name.compareTo(obm.name);
  }
}
