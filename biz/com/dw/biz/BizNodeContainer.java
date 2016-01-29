package com.dw.biz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public abstract class BizNodeContainer
{
  protected String[] parentCatPP = null;

  protected String[] myCatPP = null;

  Hashtable<String, BizNode> name2BizNode = new Hashtable();
  Hashtable<String, BizCat> name2BizCat = new Hashtable();

  private transient boolean bInit = false;

  private transient BizNodeContainer parentNC = null;

  public BizNodeContainer(BizNodeContainer pbnc, String[] parentcatpp)
    throws Exception
  {
    this.parentNC = pbnc;

    this.parentCatPP = parentcatpp;
  }

  public abstract String getName();

  public String[] getParentCatNames()
  {
    return this.parentCatPP;
  }

  public BizNodeContainer getParentBizNodeContainer()
  {
    return this.parentNC;
  }

  public void init()
    throws Exception
  {
    if (this.bInit) {
      return;
    }
    synchronized (this)
    {
      if (this.bInit) {
        return;
      }
      init0();

      this.bInit = true;
    }
  }

  private void init0() throws Exception
  {
    this.name2BizNode.clear();
    this.name2BizCat.clear();

    List fns = getBizModuleIO().getSubFileNames(this.myCatPP, null);
    if (fns != null)
    {
      for (String fn : fns)
      {
        int p = fn.lastIndexOf('.');
        if (p > 0)
        {
          String name = fn.substring(0, p);
          String extn = fn.substring(p + 1);

          Class c = BizManager.getBizNodeTypeClass(extn);
          if (c != null)
          {
            long ludp = getBizModuleIO().getSubFileUpdateDate(this.myCatPP, fn);
            BizNode bn = new BizNode(getBizModule(), this, this.myCatPP, name, extn, ludp);
            this.name2BizNode.put(bn.getNodeName() + "." + bn.getNodeType(), bn);
          }
        }
      }
    }
    fns = getBizModuleIO().getSubCatName(this.myCatPP);
    if (fns != null)
    {
      for (String fn : fns)
      {
        BizCat bc = new BizCat(getBizModule(), this.myCatPP, fn);

        this.name2BizCat.put(bc.getName(), bc);
      }
    }
  }

  public BizModule getBizModule()
  {
    BizNodeContainer bnc = this;
    while (bnc.getParentBizNodeContainer() != null) {
      bnc = bnc.getParentBizNodeContainer();
    }

    if ((bnc instanceof BizModule)) {
      return (BizModule)bnc;
    }
    return null;
  }

  public BizModuleIO getBizModuleIO()
  {
    BizModule bm = getBizModule();
    if (bm == null)
      return null;
    return bm.getBizModuleIO();
  }

  public String[] getMyCatPath()
  {
    return this.myCatPP;
  }

  public BizNode getNode(String n, String type) throws Exception
  {
    if (!this.bInit)
      init();
    return (BizNode)this.name2BizNode.get(n + "." + type);
  }

  public List<BizNode> getSubNodesByPrefixType(String prefix, String type) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    ArrayList rets = new ArrayList();
    for (BizNode bn : this.name2BizNode.values())
    {
      if ((bn.getNodeName().startsWith(prefix)) && (bn.getNodeType().equals(type)))
        rets.add(bn);
    }
    return rets;
  }

  public BizNode getBizNode(String[] catpp, String n, String type) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizNodeContainer bnc = this;
    if (catpp != null)
    {
      for (String s : catpp)
      {
        bnc = bnc.getCat(s);
        if (bnc == null) {
          return null;
        }
      }
    }
    return bnc.getNode(n, type);
  }

  public BizNode getBizNode(String[] catpp, String filename) throws Exception
  {
    String[] fnt = BizNode.FileName2NodeNameType(filename);
    return getBizNode(catpp, fnt[0], fnt[1]);
  }

  public BizNode[] getAllNodes() throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizNode[] rets = new BizNode[this.name2BizNode.size()];
    this.name2BizNode.values().toArray(rets);
    Arrays.sort(rets);
    return rets;
  }

  public List<BizNode> getBizNodesByNodeType(String nodetype) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    ArrayList bns = new ArrayList();
    for (BizNode bn : this.name2BizNode.values())
    {
      if (nodetype.equals(bn.getNodeType()))
      {
        bns.add(bn);
      }
    }

    return bns;
  }

  public BizCat getCat(String n) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    return (BizCat)this.name2BizCat.get(n);
  }

  public BizNodeContainer getBizNodeContainerByPath(String[] catpp) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    if ((catpp == null) || (catpp.length == 0)) {
      return this;
    }
    BizNodeContainer bnc = this;
    for (String s : catpp)
    {
      bnc = bnc.getCat(s);
      if (bnc == null) {
        return null;
      }
    }
    return bnc;
  }

  public BizCat[] getAllCats()
    throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizCat[] rets = new BizCat[this.name2BizCat.size()];
    this.name2BizCat.values().toArray(rets);
    Arrays.sort(rets);
    return rets;
  }

  public BizCat addCat(String name)
    throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizManager.checkBizName(name);

    BizCat bc = getCat(name);
    if (bc != null) {
      throw new IllegalArgumentException("BizCat with name=" + name + " is existed!");
    }
    if (!getBizModuleIO().canSave()) {
      throw new IOException("biz module io is cannot save!");
    }
    getBizModuleIO().addDir(this.myCatPP, name);

    BizCat newbc = new BizCat(getBizModule(), this.myCatPP, name);
    this.name2BizCat.put(name, newbc);
    return newbc;
  }

  public void addCat(String[] catpp, String catn) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizNodeContainer bnc = getBizNodeContainerByPath(catpp);
    if (bnc == null) {
      throw new Exception("cannot get parent cat Node Container");
    }
    bnc.addCat(catn);
  }

  public void delCat(String name)
    throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizCat bc = getCat(name);
    if (bc == null) {
      return;
    }
    if (!getBizModuleIO().canSave()) {
      throw new IOException("biz module io is cannot save!");
    }
    BizCat[] bcs = bc.getAllCats();
    if ((bcs != null) && (bcs.length > 0)) {
      throw new IOException("Biz Cat has sub cats");
    }
    BizNode[] bns = bc.getAllNodes();
    if ((bns != null) && (bns.length > 0)) {
      throw new IOException("Biz Cat has sub nodes");
    }
    getBizModuleIO().delDir(this.myCatPP, name);

    this.name2BizCat.remove(name);
  }

  public void delCat(String[] catpp, String catn) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizNodeContainer bnc = getBizNodeContainerByPath(catpp);
    if (bnc == null) {
      throw new Exception("cannot get parent cat Node Container");
    }
    bnc.delCat(catn);
  }

  public BizNode addNode(String name, String type) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizManager.checkBizName(name);

    Class c = BizManager.getBizNodeTypeClass(type);
    if (c == null) {
      throw new IllegalArgumentException("unknown node type=" + type);
    }
    BizNode bn = getNode(name, type);
    if (bn != null) {
      throw new IllegalArgumentException("node with " + name + "." + type + " is already existed!");
    }
    BizNode newbn = new BizNode(getBizModule(), this, this.myCatPP, name, type, -1L);
    String nfn = newbn.getNodeFileName();
    getBizModuleIO().saveFile(this.myCatPP, nfn, new byte[0]);
    this.name2BizNode.put(nfn, newbn);
    return newbn;
  }

  public BizNode addNode(String[] catpp, String name, String type) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizNodeContainer bnc = getBizNodeContainerByPath(catpp);
    if (bnc == null) {
      throw new Exception("cannot get parent cat Node Container");
    }
    return bnc.addNode(name, type);
  }

  public void delNode(String name, String type) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizNode bn = getNode(name, type);
    if (bn == null) {
      return;
    }
    String nfn = name + "." + type;
    getBizModuleIO().saveFile(this.myCatPP, nfn, null);
    this.name2BizNode.remove(nfn);
  }

  public void delNode(String[] catpp, String name, String type) throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizNodeContainer bnc = getBizNodeContainerByPath(catpp);
    if (bnc == null) {
      throw new Exception("cannot get parent cat Node Container");
    }
    bnc.delNode(name, type);
  }

  public void changeNode(String[] catpp, String noden, String nodet, String[] tarcatpp, String tar_noden)
    throws Exception
  {
    if (!this.bInit) {
      init();
    }
    BizManager.checkBizName(tar_noden);

    BizNode bn = getBizNode(catpp, noden, nodet);
    if (bn == null) {
      throw new Exception("cannot find source BizNode to be changed!");
    }
    BizNode tarbn = getBizNode(tarcatpp, tar_noden, nodet);
    if (tarbn != null) {
      throw new Exception("tar BizNode is already existed!");
    }
    BizNodeContainer tarbnc = getBizNodeContainerByPath(tarcatpp);
    if (tarbnc == null) {
      throw new Exception("tar BizNode Container is not existed");
    }
    String sorfn = BizNode.NodeNameType2FileName(noden, nodet);
    String tarfn = BizNode.NodeNameType2FileName(tar_noden, nodet);
    getBizModuleIO().changeFilePath(catpp, bn.getNodeFileName(), tarcatpp, tarfn);

    getBizModule().fireFileDeleted(catpp, sorfn);
    getBizModule().fireFileAdded(tarcatpp, tarfn);
  }

  public void changeNodeContainerName(String[] catpp, String sorname, String[] tarpp, String tarname)
    throws Exception
  {
    if (!this.bInit) {
      init();
    }
    if ((sorname == null) || (sorname.equals("")))
      throw new IllegalArgumentException("source container name cannot be null or empty!");
    if ((tarname == null) || (tarname.equals(""))) {
      throw new IllegalArgumentException("tar container name cannot be null or empty!");
    }
    BizNodeContainer psorbnc = getBizNodeContainerByPath(catpp);
    BizNodeContainer sorbnc = null;
    if (psorbnc != null)
      sorbnc = psorbnc.getBizNodeContainerByPath(new String[] { sorname });
    if (sorbnc == null) {
      throw new Exception("cannot find source Node Container!");
    }
    BizNodeContainer ptarbnc = getBizNodeContainerByPath(tarpp);
    BizNodeContainer tarbnc = null;
    if (ptarbnc != null)
      tarbnc = ptarbnc.getBizNodeContainerByPath(new String[] { tarname });
    if (tarbnc != null) {
      throw new Exception("target Node Container is already existed!");
    }
    getBizModuleIO().changeDir(catpp, sorname, tarpp, tarname);

    if (psorbnc != ptarbnc)
    {
      psorbnc.refresh();
      ptarbnc.refresh();
    }
    else
    {
      psorbnc.refresh();
    }
  }

  public void refresh()
    throws Exception
  {
    init0();
  }
}
