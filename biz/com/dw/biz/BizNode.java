package com.dw.biz;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

public class BizNode
  implements IXmlDataable, Comparable<BizNode>
{
  String moduleName = null;

  String[] catPaths = null;

  private String name = null;

  private String nodeType = null;

  long lastUpdate = -1L;

  transient BizModule belongToModule = null;

  transient BizNodeContainer belongToNodeContainer = null;

  transient BizNodeObj bizObj = null;

  public static String NodeNameType2FileName(String noden, String nodet)
  {
    return noden + "." + nodet;
  }

  public static String[] FileName2NameExt(String filename)
  {
    int p = filename.lastIndexOf('.');
    if (p < 0) {
      return new String[] { filename };
    }
    String nn = filename.substring(0, p);
    String nt = filename.substring(p + 1);
    return new String[] { nn, nt };
  }

  public static String[] FileName2NodeNameType(String filename)
  {
    int p = filename.lastIndexOf('.');
    if (p <= 0) {
      throw new IllegalArgumentException("invalid file name with no ext name");
    }
    String nt = filename.substring(p + 1);
    if (BizManager.getBizNodeTypeClass(nt) == null)
      throw new IllegalArgumentException("unknow node type=" + nt);
    String nn = filename.substring(0, p);
    return new String[] { nn, nt };
  }

  public BizNode()
  {
  }

  public BizNode(BizModule bm, BizNodeContainer bnc, String[] catps, String name, String type, long lastupd)
  {
    if (bm == null) {
      throw new IllegalArgumentException("BizModule cannot be null");
    }
    if ((name == null) || (name.equals(""))) {
      throw new IllegalArgumentException("name cannot be null or empty!");
    }
    Class c = BizManager.getBizNodeTypeClass(type);
    if (c == null) {
      throw new IllegalArgumentException("unknow biz node type=" + type);
    }
    this.belongToModule = bm;
    this.belongToNodeContainer = bnc;
    this.moduleName = bm.getName();
    this.catPaths = catps;
    this.name = name;
    this.nodeType = type;
    this.lastUpdate = lastupd;
  }

  public BizPath getBizPath()
  {
    return new BizPath(this.moduleName, this.catPaths, this.name, this.nodeType);
  }

  public String getNodeName()
  {
    return this.name;
  }

  public String getNodeType()
  {
    return this.nodeType;
  }

  public long getLastUpdateDate()
  {
    return this.belongToModule.getBizModuleIO().getSubFileUpdateDate(this.catPaths, getNodeFileName());
  }

  public String getNodeFileName()
  {
    return NodeNameType2FileName(this.name, this.nodeType);
  }

  public BizModule getBelongToBizModule()
  {
    return this.belongToModule;
  }

  public BizNodeContainer getBelongToNodeContainer()
  {
    return this.belongToNodeContainer;
  }

  public long updateBizObj(BizNodeObj bizobj)
    throws Exception
  {
    if (bizobj == null) {
      throw new IllegalArgumentException("node cont cannot be null");
    }
    if ((bizobj instanceof IXmlDataable))
    {
      byte[] cont = ((IXmlDataable)bizobj).toXmlData().toXmlString().getBytes("UTF-8");
      long retid = this.belongToModule.getBizModuleIO().saveFile(this.catPaths, getNodeFileName(), 
        cont);

      bizobj.setBelongToBizNode(this);
      this.bizObj = bizobj;
      this.lastUpdate = retid;

      return retid;
    }

    throw new IllegalArgumentException("only IXmlDataable BizNodeObj can be updated!");
  }

  public void changeName(String newname) throws Exception
  {
    BizManager.checkBizName(newname);
    if (newname.equals(this.name)) {
      return;
    }
    getBelongToBizModule().changeNode(this.catPaths, this.name, this.nodeType, this.catPaths, newname);
  }

  public void delBizNode()
    throws Exception
  {
    getBelongToBizModule().delNode(this.catPaths, getNodeName(), getNodeType());
  }

  public BizNodeObj createEmptyBizNodeObj() throws Exception
  {
    Class c = BizManager.getBizNodeTypeClass(this.nodeType);
    return (BizNodeObj)c.newInstance();
  }

  public BizNodeObj getBizObj()
    throws Exception
  {
    long lud = getLastUpdateDate();
    if (this.bizObj != null)
    {
      if (this.lastUpdate == lud) {
        return this.bizObj;
      }
      this.bizObj = null;
    }

    synchronized (this)
    {
      if (this.bizObj != null) {
        return this.bizObj;
      }
      byte[] cont = this.belongToModule.getBizModuleIO().loadFile(this.catPaths, 
        this.name + "." + this.nodeType);
      if (cont == null) {
        return null;
      }
      Class c = BizManager.getBizNodeTypeClass(this.nodeType);
      this.bizObj = ((BizNodeObj)c.newInstance());

      if ((this.bizObj instanceof IXmlDataable))
      {
        XmlData xd = null;
        if (cont.length != 0)
        {
          xd = XmlData.parseFromByteArray(cont, "UTF-8");
        }

        if (xd != null)
          ((IXmlDataable)this.bizObj).fromXmlData(xd);
      }
      else if ((this.bizObj instanceof IBizFile))
      {
        ((IBizFile)this.bizObj).fromFileContent(cont);
      }
      else
      {
        return null;
      }

      this.bizObj.setBelongToBizNode(this);

      this.lastUpdate = lud;
      return this.bizObj;
    }
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();
    xd.setParamValue("module_name", this.moduleName);
    if ((this.catPaths != null) && (this.catPaths.length > 0)) {
      xd.setParamValues("cat_paths", this.catPaths);
    }
    xd.setParamValue("node_name", this.name);
    xd.setParamValue("node_type", this.nodeType);
    xd.setParamValue("last_upd", Long.valueOf(this.lastUpdate));

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.moduleName = xd.getParamValueStr("module_name");
    this.catPaths = xd.getParamValuesStr("cat_paths");

    this.name = xd.getParamValueStr("node_name");
    this.nodeType = xd.getParamValueStr("node_type");
    this.lastUpdate = xd.getParamValueInt64("last_upd", -1L);
  }

  public int compareTo(BizNode bn)
  {
    int v = this.nodeType.compareTo(bn.nodeType);
    if (v != 0) {
      return v;
    }
    return this.name.compareTo(bn.name);
  }
}
