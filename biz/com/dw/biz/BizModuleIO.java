package com.dw.biz;

import java.util.List;

public abstract class BizModuleIO
{
  private String uniqueId = null;

  protected BizContainer bizContainer = null;

  protected BizModule bizModule = null;

  public BizModuleIO(String unique_id)
  {
    this.uniqueId = unique_id;
  }

  public String getUniqueId()
  {
    return this.uniqueId;
  }

  void setBizContainer(BizContainer bizc)
  {
    this.bizContainer = bizc;
  }

  void setBizModule(BizModule bm)
  {
    this.bizModule = bm;
  }

  public final BizContainer getBizContainer()
  {
    return this.bizContainer;
  }

  public final BizModule getBizModule()
  {
    return this.bizModule;
  }

  public abstract boolean canSave();

  public abstract byte[] loadFile(String[] paramArrayOfString, String paramString)
    throws Exception;

  public abstract long saveFile(String[] paramArrayOfString, String paramString, byte[] paramArrayOfByte)
    throws Exception;

  public abstract void addDir(String[] paramArrayOfString, String paramString)
    throws Exception;

  public abstract void delDir(String[] paramArrayOfString, String paramString)
    throws Exception;

  public abstract void changeDir(String[] paramArrayOfString1, String paramString1, String[] paramArrayOfString2, String paramString2)
    throws Exception;

  public abstract void changeFilePath(String[] paramArrayOfString1, String paramString1, String[] paramArrayOfString2, String paramString2)
    throws Exception;

  public abstract List<String> getSubCatName(String[] paramArrayOfString)
    throws Exception;

  public abstract List<String> getSubFileNames(String[] paramArrayOfString, String paramString)
    throws Exception;

  public abstract long getSubFileUpdateDate(String[] paramArrayOfString, String paramString);

  public void delBizModule()
    throws Exception
  {
  }
}
