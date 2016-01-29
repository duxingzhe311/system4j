package com.dw.biz;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DirBizModuleIO extends BizModuleIO
{
  File moduleDir = null;

  String moduleDirStr = null;

  FileFilter catNameFilter = new FileFilter()
  {
    public boolean accept(File f)
    {
      return f.isDirectory();
    }
  };

  public DirBizModuleIO(String uniqueid, File moduledir)
    throws IOException
  {
    super(uniqueid);

    this.moduleDir = moduledir;
    this.moduleDirStr = this.moduleDir.getCanonicalPath();

    if (!this.moduleDirStr.endsWith("/"))
      this.moduleDirStr += "/";
  }

  public boolean canSave()
  {
    return true;
  }

  private File calDir(String[] catpp)
  {
    if (catpp == null) {
      return this.moduleDir;
    }
    StringBuffer tmpsb = new StringBuffer();
    tmpsb.append(this.moduleDirStr);
    for (String p : catpp)
    {
      tmpsb.append(p).append('/');
    }
    return new File(tmpsb.toString());
  }

  private File calDir(String[] catpp, String catn)
  {
    if (catpp == null) {
      return new File(this.moduleDir, catn + "/");
    }
    StringBuffer tmpsb = new StringBuffer();
    tmpsb.append(this.moduleDirStr);
    for (String p : catpp)
    {
      tmpsb.append(p).append('/');
    }
    return new File(tmpsb.toString());
  }

  private File calFile(String[] catpp, String filename)
  {
    if (catpp == null) {
      return new File(this.moduleDirStr + filename);
    }
    StringBuffer tmpsb = new StringBuffer();
    tmpsb.append(this.moduleDirStr);
    for (String p : catpp)
    {
      tmpsb.append(p).append('/');
    }
    tmpsb.append(filename);
    return new File(tmpsb.toString());
  }

  private void saveFile(File f, byte[] cont)
    throws FileNotFoundException, IOException
  {
    if (cont == null)
    {
      if (f.exists())
        f.delete();
      return;
    }
    FileOutputStream fos = null;
    try
    {
      File pf = f.getParentFile();
      if ((pf != null) && (!pf.exists())) {
        pf.mkdirs();
      }
      fos = new FileOutputStream(f);

      fos.write(cont);

      fos.flush();
    }
    finally
    {
      if (fos != null)
        fos.close();
    }
  }

  private byte[] loadFile(File f) throws FileNotFoundException, IOException
  {
    FileInputStream fis = null;
    try
    {
      byte[] arrayOfByte1;
      if (!f.exists()) {
        return null;
      }
      int flen = (int)f.length();
      if (flen == 0) {
        return new byte[0];
      }
      byte[] buf = new byte[flen];
      fis = new FileInputStream(f);

      fis.read(buf);
      return buf;
    }
    finally
    {
      if (fis != null)
        fis.close();
    }
  }

  public byte[] loadFile(String[] parentp, String filename)
    throws IOException
  {
    File f = calFile(parentp, filename);
    if (!f.exists()) {
      return null;
    }
    return loadFile(f);
  }

  public long saveFile(String[] parentp, String filename, byte[] cont)
    throws IOException
  {
    File f = calFile(parentp, filename);
    saveFile(f, cont);
    if (cont == null) {
      return -1L;
    }
    return f.lastModified();
  }

  public void addDir(String[] parentp, String catname)
    throws IOException
  {
    File fp = calDir(parentp, catname);
    if (!fp.exists())
      fp.mkdirs();
  }

  public void delDir(String[] parentp, String catname)
    throws IOException
  {
    File fp = calDir(parentp, catname);
    if (!fp.exists()) {
      return;
    }
    if (!fp.delete())
      throw new IOException("delete io error!");
  }

  public void changeDir(String[] parentp, String src_dirn, String[] tarp, String tar_dirn)
    throws Exception
  {
    File sorfp = calDir(parentp, src_dirn);
    if (!sorfp.isDirectory())
      throw new Exception("source is not dir!");
    if (!sorfp.exists()) {
      throw new Exception("sor dir is not existed!");
    }
    File tarfp = calDir(tarp, tar_dirn);
    if (tarfp.exists()) {
      throw new Exception("tar dir is already existed!");
    }
    sorfp.renameTo(tarfp);
  }

  public void changeFilePath(String[] parentp, String filename, String[] tarpp, String tarfn)
    throws IOException
  {
    File srcf = calFile(parentp, filename);
    if (!srcf.exists())
      throw new IOException("source file does not existed!");
    File tarf = calFile(tarpp, tarfn);

    srcf.renameTo(tarf);
  }

  public List<String> getSubCatName(String[] parentp)
    throws IOException
  {
    File dirf = calDir(parentp);
    if (!dirf.exists()) {
      return null;
    }
    File[] ffs = dirf.listFiles(this.catNameFilter);
    if (ffs == null) {
      return null;
    }
    ArrayList rets = new ArrayList(ffs.length);
    for (File tmpf : ffs)
    {
      rets.add(tmpf.getName());
    }
    return rets;
  }

  public List<String> getSubFileNames(String[] parentp, String extname)
    throws IOException
  {
    File pdir = calDir(parentp);
    if (!pdir.exists()) {
      return null;
    }
    SubFileF sff = new SubFileF(extname);
    File[] ffs = pdir.listFiles(sff);
    if (ffs == null) {
      return null;
    }
    ArrayList rets = new ArrayList(ffs.length);
    for (File f : ffs)
    {
      rets.add(f.getName());
    }
    return rets;
  }

  public long getSubFileUpdateDate(String[] parentp, String filename)
  {
    File f = calFile(parentp, filename);
    if (!f.exists()) {
      throw new RuntimeException("cannot find file for get update date");
    }
    return f.lastModified();
  }

  class SubFileF
    implements FileFilter
  {
    private String extn = null;

    public SubFileF(String extname) {
      if ((extname == null) || (extname.equals("")) || ("*".equals(extname)))
        this.extn = null;
      else
        this.extn = extname;
    }

    public boolean accept(File f)
    {
      if (!f.isFile()) {
        return false;
      }
      if (this.extn == null) {
        return true;
      }
      return f.getName().toLowerCase().endsWith(this.extn);
    }
  }
}
