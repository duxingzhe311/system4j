package com.dw.biz.platform;

import com.dw.biz.BizNode;
import com.dw.biz.BizPath;
import com.dw.biz.BizViewCell;
import com.dw.biz.platform.compiler.JspAppReader;
import com.dw.biz.platform.compiler.StringJspAppReader;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class BizViewCellRtCompilerEnv extends CompRCEnv
{
  static String jspCompileBase = System.getProperties().getProperty("user.dir") + "/temp/biz_jc/";

  private BizViewCell bizViewCell = null;

  public BizViewCellRtCompilerEnv(BizViewCell bv)
  {
    super(bv.getBizPath().getModuleName());

    this.bizViewCell = bv;
  }

  public boolean judgeNewJsp()
  {
    File tarcf = new File(getTargetClassPath());
    long tartime = tarcf.lastModified();
    if ((tartime < this.bizViewCell.getBelongToBizNode().getLastUpdateDate()) || (tartime == 0L)) {
      return true;
    }
    return false;
  }

  private String getTargetClassPath()
  {
    String pp = this.bizViewCell.getBizPathStr();

    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");
    return jspCompileBase + "p_vc_" + pp + ".class";
  }

  public String getTargetJavaPath()
  {
    String pp = this.bizViewCell.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");
    return jspCompileBase + "p_vc_" + pp + ".java";
  }

  public String getTargetClassBase()
  {
    return jspCompileBase;
  }

  public static String getClassName(BizViewCell bvc)
  {
    String pp = bvc.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("/", ".");
    pp = pp.replaceAll("-", "_");
    return "p_vc_" + pp;
  }

  public String getClassName()
  {
    return getClassName(this.bizViewCell);
  }

  public JspAppReader getJspAppReader() throws IOException
  {
    return new StringJspAppReader(this.bizViewCell.getStrCont());
  }

  public String getContentStr() {
    return this.bizViewCell.getStrCont();
  }

  public String getExtClassName() {
    return "BizViewCellRunner";
  }
}
