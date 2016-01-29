package com.dw.biz;

import java.io.File;
import java.io.IOException;
import java.util.List;

class JarBizModuleIO extends BizModuleIO
{
  File jarFile = null;

  public JarBizModuleIO(String uniqueid, File jarf)
  {
    super(uniqueid);

    this.jarFile = jarf;
  }

  public boolean canSave()
  {
    return false;
  }

  public byte[] loadFile(String[] parentp, String filename)
    throws IOException
  {
    return null;
  }

  public long saveFile(String[] parentp, String filename, byte[] cont)
    throws IOException
  {
    throw new IOException("not support");
  }

  public void addDir(String[] parentp, String catname)
    throws IOException
  {
    throw new IOException("not support");
  }

  public void delDir(String[] parentp, String catname)
    throws IOException
  {
    throw new IOException("not support");
  }

  public void changeDir(String[] parentp, String src_dirn, String[] tarp, String tar_dirn)
    throws Exception
  {
    throw new IOException("not support");
  }

  public void changeFilePath(String[] parentp, String filename, String[] tarpp, String tarfn)
    throws IOException
  {
    throw new IOException("not support");
  }

  public List<String> getSubCatName(String[] parentp)
    throws IOException
  {
    return null;
  }

  public List<String> getSubFileNames(String[] parentp, String extname)
    throws IOException
  {
    return null;
  }

  public long getSubFileUpdateDate(String[] parentp, String filename)
  {
    return this.jarFile.lastModified();
  }
}
