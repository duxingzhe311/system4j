package com.dw.biz.platform;

import com.dw.biz.BizForm;
import com.dw.biz.BizNode;
import com.dw.biz.BizPath;
import com.dw.biz.platform.compiler.JspAppReader;
import com.dw.biz.platform.compiler.StringJspAppReader;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class BizFormRtCompilerEnv extends CompRCEnv
  implements IRuntimeCompilerEnv
{
  static String jspCompileBase = System.getProperties().getProperty("user.dir") + "/temp/biz_jc/";

  private BizForm bizForm = null;

  public BizFormRtCompilerEnv(BizForm bv)
  {
    super(bv.getBizPath().getModuleName());
    this.bizForm = bv;
  }

  public String getContentStr() {
    return null;
  }

  public boolean judgeNewJsp() {
    File tarcf = new File(getTargetClassPath());
    long tartime = tarcf.lastModified();
    if ((tartime < this.bizForm.getBelongToBizNode().getLastUpdateDate()) || (tartime == 0L)) {
      return true;
    }
    return false;
  }

  private String getTargetClassPath()
  {
    String pp = this.bizForm.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");
    return jspCompileBase + "p_f_" + pp + ".class";
  }

  public String getTargetJavaPath()
  {
    String pp = this.bizForm.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");
    return jspCompileBase + "p_f_" + pp + ".java";
  }

  public String getTargetClassBase()
  {
    return jspCompileBase;
  }

  public String getClassName()
  {
    String pp = this.bizForm.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("/", ".");
    pp = pp.replaceAll("-", "_");
    return "p_f_" + pp;
  }

  public JspAppReader getJspAppReader() throws IOException
  {
    return new StringJspAppReader(this.bizForm.getStrCont());
  }

  public String getExtClassName()
  {
    return null;
  }
}
