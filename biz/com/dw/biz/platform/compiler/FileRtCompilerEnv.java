package com.dw.biz.platform.compiler;

import com.dw.biz.platform.BizClassLoader;
import com.dw.biz.platform.IRuntimeCompilerEnv;
import java.io.File;
import java.io.IOException;

public class FileRtCompilerEnv
  implements IRuntimeCompilerEnv
{
  String dirBase = null;
  String source = null;
  String extClassName = null;

  public FileRtCompilerEnv(String dirbase, String source)
  {
    this.dirBase = dirbase;
    this.source = source;
  }

  public String getContentStr()
  {
    return null;
  }

  public boolean judgeNewJsp() {
    if (!this.source.endsWith(".jsp")) {
      throw new RuntimeException("invalid source file to be compiled without extension [.jsp]!");
    }
    int i = this.source.lastIndexOf(".jsp");
    String source0 = this.source.substring(0, i);
    String absSor = this.dirBase + this.source;
    String absTar = this.dirBase + source0;

    File sorf = new File(absSor);
    long sortime = sorf.lastModified();
    if (sortime == 0L)
      throw new RuntimeException("the soruce is not existed,file=" + absSor);
    File tarcf = new File(absTar + ".class");
    long tartime = tarcf.lastModified();
    if ((tartime < sortime) || (tartime == 0L)) {
      return true;
    }
    return false;
  }

  public String getTargetClassBase()
  {
    return this.dirBase;
  }

  public String getTargetJavaPath()
  {
    int i = this.source.lastIndexOf(".jsp");
    String source0 = this.source.substring(0, i);
    String absSor = this.dirBase + this.source;
    String absTar = this.dirBase + source0;
    return absTar + ".java";
  }

  public String getTargetClassPath()
  {
    int i = this.source.lastIndexOf(".jsp");
    String source0 = this.source.substring(0, i);
    String absSor = this.dirBase + this.source;
    String absTar = this.dirBase + source0;
    return absTar + ".class";
  }

  public String getClassName()
  {
    if (!this.source.endsWith(".jsp"))
      throw new RuntimeException("invalid source file to be compiled without extension [.jsp]!");
    int i = this.source.lastIndexOf(".jsp");
    String source0 = this.source.substring(0, i);

    String classname = null;
    classname = source0.replace('\\', '.');
    classname = classname.replace('/', '.');
    return classname;
  }

  public JspAppReader getJspAppReader() throws IOException
  {
    return new FileIncludeReader(this.source);
  }

  public String getExtClassName()
  {
    return this.extClassName;
  }

  public ClassLoader getEnvClassLoader()
  {
    return null;
  }

  public String getEnvClassPath()
  {
    return null;
  }

  public BizClassLoader createNewBizClassLoader()
  {
    return null;
  }

  public BizClassLoader getRecentBizClassLoader()
  {
    return null;
  }
}
