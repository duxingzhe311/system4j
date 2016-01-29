package com.dw.biz.platform.compiler;

import com.dw.biz.BizManager;
import com.dw.biz.BizView;
import com.dw.biz.platform.BizClassLoader;
import com.dw.biz.platform.BizViewRtCompilerEnv;
import com.dw.biz.platform.compiler.util.BizJavaCompiler;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

public class BizViewRuntime
{
  public static final int DEBUG_IGNORE = 0;
  public static final int DEBUG_FALSE = 1;
  public static final int DEBUG_TRUE = 2;
  public static final int DEBUG_ALL = 3;
  public static int isDebug = 1;

  BizManager bizMgr = null;

  BizViewRtCompilerEnv compEnv = null;

  private BizViewRuntime()
  {
    String tmps = System.getProperties().getProperty("wf.debug").trim();
    if ("ignore".equals(tmps))
      isDebug = 0;
    else if ("true".equals(tmps))
      isDebug = 2;
    else if ("all".equals(tmps))
      isDebug = 3;
    else
      isDebug = 1;
  }

  public BizViewRuntime(BizManager bm, BizViewRtCompilerEnv env)
  {
    this.bizMgr = bm;
    this.compEnv = env;
  }

  public BizViewRuntime(OutputStream output)
  {
    String tmps = System.getProperties().getProperty("wf.debug").trim();
    if ("ignore".equals(tmps))
      isDebug = 0;
    else if ("true".equals(tmps))
      isDebug = 2;
    else if ("all".equals(tmps))
      isDebug = 3;
    else
      isDebug = 1;
  }

  public String getJspClassName()
    throws Exception
  {
    checkNewCompile();
    return this.compEnv.getClassName();
  }

  public void checkNewCompile() throws Exception
  {
    if (this.compEnv.judgeNewJsp())
    {
      compile();
    }
  }

  public Object getJspInstance()
    throws Exception
  {
    Object ob = null;
    switch (isDebug)
    {
    case 0:
      ob = createNoCompile();

      return ob;
    case 2:
      if (this.compEnv.judgeNewJsp())
      {
        ob = compileAndCreate();

        return ob;
      }

      ob = createNoCompile();

      return ob;
    case 1:
      if (this.compEnv.judgeNewJsp())
        ob = compileAndCreate();
      else {
        ob = createNoCompile();
      }

      return ob;
    case 3:
      return compileAndCreate();
    }

    if (this.compEnv.judgeNewJsp())
      ob = compileAndCreate();
    else {
      ob = createNoCompile();
    }

    return ob;
  }

  private Object compileAndCreate()
    throws Exception
  {
    String classname = compile();

    BizClassLoader ozCL = this.compEnv.createNewBizClassLoader();

    Class c = ozCL.loadClass(classname);
    return c.newInstance();
  }

  private String compile() throws IOException, Exception
  {
    String classname = this.compEnv.getClassName();

    BizViewCompiler jspc = new BizViewCompiler(
      this.compEnv.getBizView().getBizPathStr(), this.bizMgr, this.compEnv.getBizView().reloadContTreeNodeRoot(), 
      this.compEnv.getClassName(), this.compEnv.getTargetJavaPath(), 
      this.compEnv.getExtClassName());

    if ((isDebug == 2) || (isDebug == 3))
      jspc.setDebug(true);
    int i = classname.lastIndexOf('.');

    if (i > 0) {
      jspc.setPackage("package " + classname.substring(0, i) + ";");
    }
    jspc.compile();

    BizJavaCompiler ozJC = new BizJavaCompiler(
      this.compEnv.getTargetClassBase(), System.out, this.compEnv.getEnvClassPath());

    ozJC.compile(this.compEnv.getTargetJavaPath());
    return classname;
  }

  private Object createNoCompile()
    throws IOException
  {
    String classname = this.compEnv.getClassName();
    try
    {
      Class c = Class.forName(classname, true, this.compEnv.getRecentBizClassLoader());
      return c.newInstance();
    }
    catch (Exception e)
    {
      throw new RuntimeException("create instance error [" + classname + "]=" + e.toString());
    }
  }

  private void log(String str)
  {
    System.out.println(str);
  }

  public static void main(String[] args)
  {
  }
}
