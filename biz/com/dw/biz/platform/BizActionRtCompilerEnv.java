package com.dw.biz.platform;

import com.dw.biz.BizAction;
import com.dw.biz.BizNode;
import com.dw.biz.BizPath;
import com.dw.biz.platform.compiler.JspAppReader;
import com.dw.biz.platform.compiler.StringJspAppReader;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class BizActionRtCompilerEnv extends CompRCEnv
  implements IRuntimeCompilerEnv
{
  static String jspCompileBase = System.getProperties().getProperty("user.dir") + "/temp/biz_jc/";

  private BizAction bizAction = null;

  public BizActionRtCompilerEnv(BizAction ba)
  {
    super(ba.getBizPath().getModuleName());

    this.bizAction = ba;
  }

  public BizAction getBizAction()
  {
    return this.bizAction;
  }

  public String getContentStr() {
    return null;
  }

  public boolean judgeNewJsp() {
    File tarcf = new File(getTargetClassPath());
    long tartime = tarcf.lastModified();
    if ((tartime < this.bizAction.getBelongToBizNode().getLastUpdateDate()) || (tartime == 0L)) {
      return true;
    }
    return false;
  }

  private String getTargetClassPath()
  {
    String pp = this.bizAction.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");

    return jspCompileBase + "p_a_" + pp + ".class";
  }

  public String getTargetJavaPath()
  {
    String pp = this.bizAction.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");

    return jspCompileBase + "p_a_" + pp + ".java";
  }

  public String getTargetClassBase()
  {
    return jspCompileBase;
  }

  public String getClassName()
  {
    String pp = this.bizAction.getBizPathStr();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");
    pp = pp.replaceAll("/", ".");

    return "p_a_" + pp;
  }

  public JspAppReader getJspAppReader() throws IOException
  {
    return new StringJspAppReader(this.bizAction.getStrCont());
  }

  public String getExtClassName()
  {
    return "BizActionRunner";
  }
}
