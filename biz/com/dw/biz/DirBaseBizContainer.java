package com.dw.biz;

import com.dw.biz.platform.BizActionRunner;
import com.dw.biz.platform.BizViewFormRunner;
import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.gdb.datax.IDataXContainer;
import com.dw.system.xmldata.XmlDataStruct;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class DirBaseBizContainer extends BizContainer
{
  BizManager bizMgr = null;
  DataXManager dataxMgr = null;
  File dirBase = null;
  String dirBasePath = null;

  transient FileFilter moduleFF = new FileFilter()
  {
    public boolean accept(File f)
    {
      if (f.isDirectory()) {
        return true;
      }
      return f.getName().toLowerCase().endsWith(".jar");
    }
  };

  public DirBaseBizContainer(String dirbase, BizManager bm)
  {
    this.dirBase = new File(dirbase);
    this.dirBase.mkdirs();
    this.bizMgr = bm;
    this.dataxMgr = DataXManager.getInstance();
  }

  protected List<BizModuleIO> loadExistedBizModuleIO()
    throws IOException
  {
    ArrayList lls = new ArrayList();

    File[] dffs = this.dirBase.listFiles(this.moduleFF);
    if (dffs == null)
    {
      return lls;
    }
    for (File df : dffs)
    {
      BizModuleIO bmio = null;
      if (df.isDirectory())
      {
        bmio = new DirBizModuleIO(df.getName(), df);
      }
      else
      {
        bmio = null;
      }

      if (bmio != null)
      {
        lls.add(bmio);
      }
    }
    return lls;
  }

  protected BizModuleIO createNewBizModuleIO(String modulen) throws IOException
  {
    String fn = modulen + "_" + UUID.randomUUID().toString();
    File nbmf = new File(this.dirBase, fn + "/");
    nbmf.mkdirs();
    return new DirBizModuleIO(fn, nbmf);
  }

  public IDataXContainer getDataXContainer()
  {
    return this.dataxMgr;
  }

  public XmlDataStruct getBizViewDataStruct(String viewpath) throws Exception
  {
    BizView bv = this.bizMgr.getBizViewByPath(viewpath);
    if (bv == null)
      return null;
    BizViewFormRunner bvr = (BizViewFormRunner)this.bizMgr.createBizViewRunnerIns(bv);
    if (bvr == null) {
      return null;
    }
    return bvr.getFormDataStruct();
  }

  public XmlDataStruct getBizActionInputDataStruct(String actpath) throws Exception
  {
    BizAction ba = this.bizMgr.getBizActionByPath(actpath);
    if (ba == null) {
      return null;
    }
    BizActionRunner bar = this.bizMgr.getBizActionRunnerObj(ba);
    return bar.getRunnerInputXmlDataStruct();
  }

  public XmlDataStruct getBizActionOutputDataStruct(String actpath) throws Exception
  {
    BizAction ba = this.bizMgr.getBizActionByPath(actpath);
    if (ba == null) {
      return null;
    }
    BizActionRunner bar = this.bizMgr.getBizActionRunnerObj(ba);
    return bar.getRunnerOutputXmlDataStruct();
  }
}
