package com.dw.biz;

import com.dw.system.Convert;
import com.dw.system.gdb.datax.IDataXContainer;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public abstract class BizContainer
{
  private Hashtable<String, BizModuleIO> uniqueId2bmIO = new Hashtable();

  private Hashtable<String, BizModule> name2module = new Hashtable();

  protected abstract List<BizModuleIO> loadExistedBizModuleIO()
    throws Exception;

  protected abstract BizModuleIO createNewBizModuleIO(String paramString)
    throws Exception;

  public abstract IDataXContainer getDataXContainer();

  public void initContainer()
    throws Exception
  {
    this.name2module.clear();

    for (BizModuleIO bmio : loadExistedBizModuleIO())
    {
      bmio.setBizContainer(this);

      BizModule bm = BizModule.loadBizModuleByIO(bmio);
      if (bm != null)
      {
        bmio.setBizModule(bm);

        this.uniqueId2bmIO.put(bmio.getUniqueId(), bmio);
        this.name2module.put(bm.getName(), bm);
      }
    }
  }

  public String[] getBizModuleIOUniqueIds()
  {
    String[] rets = new String[this.uniqueId2bmIO.size()];
    this.uniqueId2bmIO.keySet().toArray(rets);
    return rets;
  }

  public BizModuleIO getBizModuleIOByUniqueId(String uniqueid)
  {
    return (BizModuleIO)this.uniqueId2bmIO.get(uniqueid);
  }

  public BizModule[] getAllBizModule()
  {
    BizModule[] rets = new BizModule[this.name2module.size()];
    this.name2module.values().toArray(rets);
    Arrays.sort(rets);
    return rets;
  }

  public BizModule getBizModuleByName(String n)
  {
    return (BizModule)this.name2module.get(n);
  }

  public String createNewBizModuleIOReturnUniqueId(String modulen) throws Exception
  {
    BizModule bm = getBizModuleByName(modulen);
    if (bm != null) {
      throw new Exception("BizModule with name=" + modulen + " is already existed!");
    }
    BizModuleIO bmio = createNewBizModuleIO(modulen);
    return bmio.getUniqueId();
  }

  public BizModule addBizModule(String name, String title, String desc)
    throws Exception
  {
    BizManager.checkBizName(name);

    BizModule bm = getBizModuleByName(name);
    if (bm != null) {
      throw new Exception("Biz Module with name=" + name + 
        " is already existed!");
    }
    BizModuleIO bmio = createNewBizModuleIO(name);
    bm = new BizModule(bmio, name, title, desc, null, null);

    byte[] cont = bm.toXmlData().toXmlString().getBytes("UTF-8");
    bmio.saveFile(null, "web.xml", cont);

    bmio.setBizModule(bm);
    this.uniqueId2bmIO.put(bmio.getUniqueId(), bmio);
    this.name2module.put(name, bm);
    return bm;
  }

  public BizModule updateBizModule(String name, String title, String desc, String ver, String ver_desc)
    throws Exception
  {
    BizModule bm = getBizModuleByName(name);
    if (bm == null) {
      throw new IllegalArgumentException("BizModule with name=" + name + 
        " cannot be found!");
    }
    bm.title = title;
    bm.desc = desc;
    bm.ver = ver;
    bm.verDesc = ver_desc;

    byte[] cont = bm.toXmlData().toXmlString().getBytes("UTF-8");
    bm.getBizModuleIO().saveFile(null, "web.xml", cont);

    return bm;
  }

  public void delBizModule(String name) throws Exception
  {
    BizModule bm = getBizModuleByName(name);
    if (bm == null) {
      throw new IllegalArgumentException("BizModule with name=" + name + 
        " cannot be found!");
    }
    BizCat[] bcs = bm.getAllCats();
    if ((bcs != null) && (bcs.length > 0)) {
      throw new Exception("Cannot delete biz module for it has BizCat");
    }
    BizNode[] bns = bm.getAllNodes();
    if ((bcs != null) && (bcs.length > 0)) {
      throw new Exception("Cannot delete biz module for it has BizNode");
    }

    BizModuleIO bmio = bm.getBizModuleIO();
    bmio.delBizModule();
    this.uniqueId2bmIO.remove(bmio.getUniqueId());
    this.name2module.remove(name);
  }

  public BizNode getBizNodeByPath(String path) throws Exception
  {
    if (Convert.isNullOrEmpty(path)) {
      return null;
    }
    BizPath bp = new BizPath(path);
    return getBizNode(bp);
  }

  public BizNode getBizNode(BizPath bp) throws Exception
  {
    if (!bp.isNode()) {
      throw new IllegalArgumentException("biz path=" + bp.toString() + 
        " is not node path!");
    }
    BizModule bm = (BizModule)this.name2module.get(bp.getModuleName());
    if (bm == null) {
      return null;
    }
    return bm.getBizNode(bp.getCatPaths(), bp.getNodeName(), bp.getNodeType());
  }

  public abstract XmlDataStruct getBizViewDataStruct(String paramString)
    throws Exception;

  public abstract XmlDataStruct getBizActionInputDataStruct(String paramString)
    throws Exception;

  public abstract XmlDataStruct getBizActionOutputDataStruct(String paramString)
    throws Exception;

  public BizNode addBizNode(String modulen, String[] catpp, String noden, String nodetype)
    throws Exception
  {
    BizModule bm = getBizModuleByName(modulen);
    if (bm == null) {
      throw new Exception("cannot find BizModule=" + modulen);
    }
    BizNodeContainer bnc = bm.getBizNodeContainerByPath(catpp);
    if (bnc == null) {
      throw new Exception("cannot find Node Container in path");
    }
    return bnc.addNode(noden, nodetype);
  }

  public List<BizNode> getAllBizNodeByType(String nodetype)
    throws Exception
  {
    Class c = BizManager.getBizNodeTypeClass(nodetype);
    if (c == null) {
      throw new IllegalArgumentException("unknow BizNode Type=" + nodetype);
    }
    ArrayList bns = new ArrayList();

    for (BizModule bm : getAllBizModule())
    {
      getAllBizNodeByType(bns, bm, nodetype);
    }

    return bns;
  }

  private void getAllBizNodeByType(ArrayList<BizNode> bns, BizNodeContainer bnc, String nodetype)
    throws Exception
  {
    List tmpbns = bnc.getBizNodesByNodeType(nodetype);
    if (tmpbns != null) {
      bns.addAll(tmpbns);
    }
    for (BizCat bc : bnc.getAllCats())
    {
      getAllBizNodeByType(bns, bc, nodetype);
    }
  }
}
