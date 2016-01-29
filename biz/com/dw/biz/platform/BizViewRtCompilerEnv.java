package com.dw.biz.platform;

import com.dw.biz.BizManager;
import com.dw.biz.BizNode;
import com.dw.biz.BizPath;
import com.dw.biz.BizView;
import com.dw.biz.platform.compiler.JspAppReader;
import com.dw.biz.platform.compiler.StringJspAppReader;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class BizViewRtCompilerEnv extends CompRCEnv
{
  static String jspCompileBase = System.getProperties().getProperty("user.dir") + "/temp/biz_jc/";

  private BizManager bizMgr = null;

  private BizView bizView = null;

  public BizViewRtCompilerEnv(BizManager bm, BizView bv)
  {
    super(bv.getBizPath().getModuleName());

    this.bizMgr = bm;

    this.bizView = bv;
  }

  public BizView getBizView()
  {
    return this.bizView;
  }

  public boolean judgeNewJsp()
  {
    File tarcf = new File(getTargetClassPath());
    long tartime = tarcf.lastModified();
    if ((tartime < this.bizView.getBelongToBizNode().getLastUpdateDate()) || (tartime == 0L)) {
      return true;
    }
    return false;
  }

  private String getTargetClassPath()
  {
    String pp = this.bizView.getCanonicalPath();

    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");
    return jspCompileBase + "p_v_" + pp + ".class";
  }

  public String getTargetJavaPath()
  {
    String pp = this.bizView.getCanonicalPath();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("-", "_");
    return jspCompileBase + "p_v_" + pp + ".java";
  }

  public String getTargetClassBase()
  {
    return jspCompileBase;
  }

  public static String getClassName(BizView bv)
  {
    String pp = bv.getCanonicalPath();
    pp = pp.replaceAll("\\.", "_");
    pp = pp.replaceAll("/", ".");
    pp = pp.replaceAll("-", "_");
    return "p_v_" + pp;
  }

  public String getClassName()
  {
    return getClassName(this.bizView);
  }

  public JspAppReader getJspAppReader() throws IOException
  {
    return new StringJspAppReader(this.bizView.getStrCont());
  }

  public String getContentStr() {
    return this.bizView.getStrCont();
  }

  public String getExtClassName() {
    return "BizViewFormRunner";
  }
}
