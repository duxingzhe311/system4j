package com.dw.biz.platform.compiler;

import com.dw.biz.BizManager;
import com.dw.biz.BizView;
import com.dw.biz.BizViewCell;
import com.dw.biz.platform.BizViewCellRtCompilerEnv;
import com.dw.biz.platform.BizViewRtCompilerEnv;
import com.dw.mltag.AbstractNode;
import com.dw.mltag.Attr;
import com.dw.mltag.Entity;
import com.dw.mltag.JspDeclaration;
import com.dw.mltag.JspDirective;
import com.dw.mltag.JspExpression;
import com.dw.mltag.JspTag;
import com.dw.mltag.ScriptComment;
import com.dw.mltag.XmlComment;
import com.dw.mltag.XmlNode;
import com.dw.mltag.XmlStrValue;
import com.dw.mltag.XmlText;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class BizViewCompiler
{
  static Hashtable<String, String> CELLTYPE2CELL_PATH = new Hashtable();

  static int TAB_FUNC = 5;

  static int TAB_VAR = 5;

  static int TAB_CONTENT = 15;

  Writer out = null;

  static String lineSep = null;

  String packageStr = null;

  StringBuffer importSB = null;

  StringBuffer classSB = null;

  StringBuffer constructInSB = new StringBuffer();

  StringBuffer contentSB = null;

  StringBuffer declarationSB = null;

  StringBuffer viewCellSetAttrAndSubNodes = new StringBuffer();

  StringBuffer functionSB = null;

  ArrayList<String> view_cell_ids = new ArrayList();
  ArrayList<String> view_inc_ids = new ArrayList();

  StringBuffer staticContentSB = new StringBuffer();

  String viewPath = null;

  int staticVarCount = 0;

  int outWriteCount = 0;

  String extClassName = "BizRunner";

  String source = null;

  String classFullName = null;
  String className = null;

  boolean isDebug = false;

  XmlNode root = null;

  BizManager bizMgr = null;

  StringBuffer curContentSB = new StringBuffer();

  static String SS_CODE = "\r\n";

  boolean isStartContent = false;

  static
  {
    CELLTYPE2CELL_PATH.put("int32", "/system/input_int32.view_cell");
    CELLTYPE2CELL_PATH.put("int16", "/system/input_int16.view_cell");
    CELLTYPE2CELL_PATH.put("int64", "/system/input_int64.view_cell");
    CELLTYPE2CELL_PATH.put("string", "/system/input_string.view_cell");
    CELLTYPE2CELL_PATH.put("float", "/system/input_float.view_cell");
    CELLTYPE2CELL_PATH.put("double", "/system/input_double.view_cell");
    CELLTYPE2CELL_PATH.put("date", "/system/input_date.view_cell");
    CELLTYPE2CELL_PATH.put("hidden", "/system/input_hidden.view_cell");
  }

  public BizViewCompiler(String viewpath, BizManager bm, XmlNode rootn, String classname, String target, String ext_classname)
    throws IOException
  {
    this.viewPath = viewpath;

    this.bizMgr = bm;

    this.root = rootn;

    if (target.endsWith(".java"))
      setOutputFile(target);
    else {
      setOutputFile(target + ".java");
    }
    this.classFullName = classname;

    this.className = this.classFullName;
    int p = this.className.lastIndexOf('.');
    if (p >= 0)
    {
      this.className = this.className.substring(p + 1);
    }
    if (ext_classname != null) {
      this.extClassName = ext_classname;
    }
    init();
  }

  private void init()
  {
    Properties systemProp = System.getProperties();

    lineSep = systemProp.getProperty("line.separator");

    this.importSB = new StringBuffer();
    this.importSB.append(lineSep);
    this.importSB.append("import java.io.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import java.util.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.system.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.system.xmldata.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.system.xmldata.obj.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.biz.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.biz.platform.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.system.gdb.datax.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.user.*;");

    this.classSB = new StringBuffer();
    if ((this.classFullName == null) || (this.classFullName.equals(""))) {
      throw new RuntimeException("Cannot resolve class name!");
    }
    String cn = this.classFullName;
    int i = this.classFullName.lastIndexOf('.');
    if (i >= 0)
      cn = cn.substring(i + 1);
    this.classSB.append(lineSep + lineSep + "public class " + cn + " extends ")
      .append(this.extClassName);
    this.classSB.append(lineSep + "{");

    this.contentSB = new StringBuffer();

    this.declarationSB = new StringBuffer();

    this.functionSB = new StringBuffer();
    this.functionSB.append(tabSpace(TAB_FUNC) + 
      "public void render(Writer _jsp_output_jac)");
    this.functionSB.append(tabSpace(TAB_FUNC) + "     throws Exception");
    this.functionSB.append(tabSpace(TAB_FUNC) + "{");
    this.functionSB.append(tabSpace(TAB_FUNC) + "PrintWriter out = new PrintWriter(_jsp_output_jac);");
  }

  private String resolveClassName(String source)
  {
    String classname = source.replace('\\', '.');
    classname = classname.replace('/', '.');
    int i = classname.lastIndexOf('.');
    if (i > 0)
      classname = classname.substring(i + 1);
    return classname;
  }

  public void setDebug(boolean db)
  {
    this.isDebug = db;
  }

  public void setOutput(Writer output)
  {
    this.out = output;
  }

  public void setOutputFile(String target) throws IOException
  {
    File f = new File(target);
    File fp = f.getParentFile();
    fp.mkdirs();

    this.out = new OutputStreamWriter(new FileOutputStream(f));
  }

  public void setPackage(String packagestr)
  {
    this.packageStr = packagestr;
  }

  public void compile()
    throws Exception
  {
    compileChildNode(this.root);

    mergeContent();
    mergeFunc();
    mergeClass();
    StringBuffer targetSB = new StringBuffer();
    if (this.packageStr != null)
      targetSB.append(this.packageStr);
    targetSB.append(this.importSB.toString());
    targetSB.append(this.classSB.toString());

    this.out.write(targetSB.toString());
    this.out.flush();
    this.out.close();
  }

  private void compileNode(AbstractNode n)
    throws Exception
  {
    if ((n instanceof XmlNode))
    {
      if ((n instanceof JspDirective))
      {
        compileCurTextCont();

        compilePageDirective((JspDirective)n);
      }
      else
      {
        XmlNode xn = (XmlNode)n;

        if ("view_cell".equalsIgnoreCase(xn.getAttribute("runas")))
        {
          compileViewCell(xn);
        }
        else if ("view".equalsIgnoreCase(xn.getAttribute("runas")))
        {
          compileView(xn);
        }
        else
        {
          compileBeginNode(xn);
          compileChildNode(xn);
          compileEndNode(xn);
        }
      }
    }
    else if ((n instanceof XmlStrValue))
    {
      if ((n instanceof JspTag))
      {
        compileCurTextCont();

        compileJspCode(((JspTag)n).getText());
      }
      else if ((n instanceof JspDeclaration))
      {
        compileCurTextCont();

        compileJspDeclaration(((JspDeclaration)n).getText());
      }
      else if ((n instanceof JspExpression))
      {
        compileCurTextCont();

        compileJspExpression(((JspExpression)n).getText());
      }
      else if ((n instanceof XmlComment))
      {
        this.curContentSB.append(((XmlComment)n).toString());
      }
    }
    else if ((n instanceof XmlText))
    {
      XmlText xt = (XmlText)n;
      compileXmlText(xt);
    }
  }

  private void compileXmlText(XmlText xt)
  {
    ArrayList al = xt.getTextMembers();
    if (al != null)
    {
      for (Iterator localIterator = al.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

        if ((o instanceof String))
        {
          this.curContentSB.append((String)o);
        }
        else if ((o instanceof Entity))
        {
          this.curContentSB.append(((Entity)o).toEntityStr());
        }
        else if ((o instanceof JspExpression))
        {
          compileCurTextCont();

          compileJspExpression(((JspExpression)o).getText());
        }
      }
    }
  }

  private void compileBeginNode(XmlNode xn)
  {
    if ((xn instanceof ScriptComment))
    {
      this.curContentSB.append("<!--");
      return;
    }

    this.curContentSB.append("<").append(xn.getNodeName());

    for (Enumeration ans = xn.getAtributeNames(); ans.hasMoreElements(); )
    {
      String n = (String)ans.nextElement();
      Attr attr = xn.getAttr(n);
      if (n != null)
      {
        String v = xn.getAttribute(n);
        this.curContentSB.append(" ").append(n).append("=\"");

        XmlText xt = attr.getXmlTextValue();
        compileXmlText(xt);

        this.curContentSB.append("\"");
      }
    }
    if (xn.hasChildren())
      this.curContentSB.append(">");
    else
      this.curContentSB.append("/>");
  }

  private void compileChildNode(XmlNode xn) throws Exception
  {
    int cc = xn.getChildCount();
    for (int i = 0; i < cc; i++)
    {
      AbstractNode n = (AbstractNode)xn.getChildAt(i);
      compileNode(n);
    }
  }

  private void compileEndNode(XmlNode xn)
  {
    if ((xn instanceof ScriptComment))
    {
      this.curContentSB.append("-->");
      return;
    }

    if (!xn.hasChildren()) {
      return;
    }
    this.curContentSB.append("</").append(xn.getNodeName()).append(">");
  }

  public void compileViewCell(XmlNode xn)
    throws Exception
  {
    String id = xn.getAttribute("id");
    if ((id == null) || (id.equals(""))) {
      throw new RuntimeException("View Cell XmlNode must has id attr!");
    }
    String vcp = xn.getAttribute("view_cell_path");

    if ((vcp == null) || (vcp.equals("")))
    {
      String strtype = xn.getAttribute("cell_type");
      if ((strtype != null) && (!strtype.equals("")))
      {
        vcp = (String)CELLTYPE2CELL_PATH.get(strtype);
      }
    }
    if ((vcp == null) || (vcp.equals(""))) {
      throw new RuntimeException("View Cell XmlNode must has view_cell_path attr!");
    }
    if (vcp.charAt(0) != '/')
    {
      int k = this.viewPath.lastIndexOf('/');
      vcp = this.viewPath.substring(0, k + 1) + vcp;
    }

    BizViewCell bvc = this.bizMgr.getBizViewCellByPath(vcp);
    if (bvc == null)
      throw new RuntimeException("cannot find BizViewCell with path=" + vcp);
    BizViewCellRtCompilerEnv bvc_env = new BizViewCellRtCompilerEnv(bvc);
    BizViewCellRuntime bvcr = new BizViewCellRuntime(bvc_env);
    String classname = bvcr.getJspClassName();

    compileCurTextCont();
    compileJspDeclaration(tabSpace(TAB_CONTENT) + classname + " " + id + "=new " + classname + "();");
    this.view_cell_ids.add(id);
    compileJspCode(tabSpace(TAB_CONTENT) + id + ".render(_jsp_output_jac);");

    for (Enumeration tmpen = xn.getAtributeNames(); tmpen.hasMoreElements(); )
    {
      String an = (String)tmpen.nextElement();
      String av = xn.getAttribute(an);
      this.viewCellSetAttrAndSubNodes.append(tabSpace(TAB_CONTENT))
        .append(id).append(".setAttribute(\"").append(an).append("\",\"").append(av).append("\");");
    }
  }

  public void compileView(XmlNode xn)
    throws Exception
  {
    String id = xn.getAttribute("id");
    if ((id == null) || (id.equals(""))) {
      throw new RuntimeException("View XmlNode must has id attr!");
    }
    String vcp = xn.getAttribute("view_path");

    if ((vcp == null) || (vcp.equals(""))) {
      throw new RuntimeException("View XmlNode must has view_cell_path attr!");
    }
    if (vcp.charAt(0) != '/')
    {
      int k = this.viewPath.lastIndexOf('/');
      vcp = this.viewPath.substring(0, k + 1) + vcp;
    }

    BizView bv = this.bizMgr.getBizViewByPath(vcp);
    if (bv == null) {
      throw new RuntimeException("cannot find BizView with path=" + vcp);
    }

    BizViewRtCompilerEnv bvc_env = new BizViewRtCompilerEnv(this.bizMgr, bv);
    BizViewRuntime bvcr = new BizViewRuntime(this.bizMgr, bvc_env);
    String classname = bvcr.getJspClassName();

    compileCurTextCont();
    compileJspDeclaration(tabSpace(TAB_CONTENT) + classname + " " + id + "=new " + classname + "();");

    List ers = xn.getSubXmlNodeByTag("event_refresh");
    String evname;
    for (XmlNode tmpxn : ers)
    {
      evname = tmpxn.getAttribute("event_name");
      if ((evname != null) && (!evname.equals("")))
      {
        String tarvids = tmpxn.getAttribute("tar_view_ids");
        if ((tarvids != null) && (!tarvids.equals("")))
        {
          String mm = tmpxn.getAttribute("map_method");
          if ((mm != null) && (!mm.equals("")))
          {
            this.constructInSB.append(tabSpace(TAB_CONTENT) + id + ".setEventRefresh(new BizView.EventMapper(\"" + 
              evname + "\",\"" + 
              tarvids + "\",\"" + mm + "\"));");
          }
        }
      }
    }
    List eos = xn.getSubXmlNodeByTag("event_output");
    for (XmlNode tmpxn : eos)
    {
      String evname = tmpxn.getAttribute("event_name");
      if ((evname != null) && (!evname.equals("")))
      {
        String tarvids = tmpxn.getAttribute("rel_view_ids");
        if ((tarvids != null) && (!tarvids.equals("")))
        {
          String mm = tmpxn.getAttribute("map_method");
          if ((mm != null) && (!mm.equals("")))
          {
            String outputn = tmpxn.getAttribute("output_name");
            if ((outputn == null) || (outputn.equals("")))
              outputn = evname;
            this.constructInSB.append(tabSpace(TAB_CONTENT) + id + ".setEventOutput(new BizView.EventMapper(\"" + 
              evname + "\",\"" + 
              tarvids + "\",\"" + outputn + "\",\"" + mm + "\"));");
          }
        }
      }
    }
    if ("false".equals(xn.getAttribute("ajax")))
    {
      compileJspCode(tabSpace(TAB_CONTENT) + id + ".runIt();");
    }
    else
    {
      compileJspCode(tabSpace(TAB_CONTENT) + id + ".runItAjax();");
    }

    this.view_inc_ids.add(id);
  }

  private void mergeContent()
  {
    compileCurTextCont();

    this.contentSB.append(tabSpace(TAB_CONTENT) + "return ;");
  }

  private void mergeFunc()
  {
    this.functionSB.append(this.contentSB.toString());

    this.functionSB.append(tabSpace(TAB_FUNC) + "}");
  }

  private void mergeClass()
  {
    this.classSB.append(this.declarationSB.toString());

    this.classSB.append("\r\npublic " + this.className + "()\r\n");
    this.classSB.append("{\r\n");
    if (this.view_cell_ids != null)
    {
      for (String vci : this.view_cell_ids)
      {
        this.classSB.append("     ").append(vci).append(".setCellName(\"").append(vci).append("\");\r\n");
      }
    }

    if (this.view_inc_ids != null)
    {
      for (String vii : this.view_inc_ids)
      {
        this.classSB.append("     ").append(vii).append(".setViewId(\"").append(vii).append("\");\r\n");
      }
    }

    this.classSB.append(this.viewCellSetAttrAndSubNodes);

    this.classSB.append(this.constructInSB);
    this.classSB.append("}\r\n");

    this.classSB.append("\r\npublic int getWriteLineNumber ()\r\n");
    this.classSB.append("{\r\n");
    this.classSB.append("     return " + this.outWriteCount + " ;\r\n");
    this.classSB.append("}\r\n");

    this.classSB.append("\r\npublic String getViewPath ()\r\n");
    this.classSB.append("{\r\n");
    this.classSB.append("     return \"" + this.viewPath + "\" ;\r\n");
    this.classSB.append("}\r\n");

    StringBuffer tmpsb = new StringBuffer();
    if ((this.view_cell_ids != null) && (this.view_cell_ids.size() > 0))
    {
      int idnum = this.view_cell_ids.size();
      tmpsb.append((String)this.view_cell_ids.get(0));
      for (int i = 1; i < idnum; i++)
      {
        tmpsb.append(',').append((String)this.view_cell_ids.get(i));
      }
    }
    this.classSB.append("\r\nprotected BizViewCellRunner[] getViewCells()\r\n");
    this.classSB.append("{\r\n");
    this.classSB.append("     return new BizViewCellRunner[]{" + tmpsb.toString() + "} ;\r\n");
    this.classSB.append("}\r\n");

    tmpsb = new StringBuffer();
    if ((this.view_inc_ids != null) && (this.view_inc_ids.size() > 0))
    {
      int idnum = this.view_inc_ids.size();
      tmpsb.append((String)this.view_inc_ids.get(0));
      for (int i = 1; i < idnum; i++)
      {
        tmpsb.append(',').append((String)this.view_inc_ids.get(i));
      }

    }

    this.classSB.append("\r\nprotected BizViewRunner[] getIncludeViews()\r\n");
    this.classSB.append("{\r\n");
    this.classSB.append("     return new BizViewRunner[]{" + tmpsb.toString() + "} ;\r\n");
    this.classSB.append("}\r\n");

    this.classSB.append(this.functionSB.toString());
    this.classSB.append(this.staticContentSB.toString());
    this.classSB.append(lineSep + "}");
  }

  private void compileCurTextCont()
  {
    if ((this.curContentSB == null) || (this.curContentSB.length() <= 0)) {
      return;
    }
    compileTextCont(this.curContentSB);
    this.curContentSB.delete(0, this.curContentSB.length());
  }

  private void compileTextCont(StringBuffer str)
  {
    if ((str == null) || (str.equals(""))) {
      return;
    }

    int len = str.length();
    StringBuffer tmpsb = new StringBuffer(len + 50);

    for (int i = 0; i < len; i++)
    {
      char ch = str.charAt(i);

      switch (ch)
      {
      case '\\':
        tmpsb.append("\\\\");
        break;
      case '\'':
        tmpsb.append("\\'");
        break;
      case '"':
        tmpsb.append("\\\"");
        break;
      case '\r':
        tmpsb.append("\\r");
        break;
      case '\n':
        tmpsb.append("\\n");
        break;
      default:
        tmpsb.append(ch);
      }

    }

    this.staticVarCount += 1;
    this.staticContentSB.append(tabSpace(TAB_CONTENT) + 
      "private static String _jsp_string_jac_" + this.staticVarCount + 
      " = \"" + tmpsb.toString() + "\" ;");
    this.contentSB.append(tabSpace(TAB_CONTENT) + 
      "_jsp_output_jac.write (_jsp_string_jac_" + this.staticVarCount + 
      ") ;");
    this.outWriteCount += 1;
  }

  private void compileJspCode(String codestr)
  {
    this.contentSB.append(tabSpace(TAB_CONTENT) + codestr);
  }

  private void compileComment(String commstr)
  {
    int len = commstr.length();
    if ((commstr.charAt(0) != '-') || (commstr.charAt(len - 1) != '-') || 
      (commstr.charAt(len - 2) != '-'))
      throw new RuntimeException("wrong comment info=<%-" + commstr + 
        "%>");
  }

  private void compileJspDeclaration(String varstr)
  {
    this.declarationSB.append(tabSpace(TAB_VAR) + varstr);
  }

  private void compileJspExpression(String exp_str)
  {
    exp_str = exp_str.trim();
    if (exp_str.charAt(exp_str.length() - 1) == ';')
      exp_str = exp_str.substring(0, exp_str.length() - 1);
    this.contentSB.append(tabSpace(TAB_CONTENT) + 
      "_jsp_output_jac.write ((\"\"+" + exp_str + ")) ;");
    this.outWriteCount += 1;
  }

  private void compilePageDirective(JspDirective jpt)
  {
    String dt = jpt.getJspDirectiveType();
    if ("page".equals(dt))
    {
      String impstr = jpt.getAttribute("import");
      if ((impstr != null) && (!(impstr = impstr.trim()).equals("")))
      {
        StringTokenizer tmpst = new StringTokenizer(impstr, ",");
        while (tmpst.hasMoreTokens())
        {
          String tmps = tmpst.nextToken().trim();
          if ((tmps != null) && (!tmps.equals("")))
          {
            if (tmps.charAt(tmps.length() - 1) != ';')
              tmps = tmps + ';';
            this.importSB.append(lineSep).append("import ").append(tmps);
          }
        }
      }
    }
  }

  private void log(String str)
  {
    System.out.println(str);
  }

  private static String tabSpace(int num)
  {
    byte[] buf = new byte[num];
    for (int i = 0; i < num; i++)
      buf[i] = 32;
    return lineSep + new String(buf);
  }

  public static void main(String[] args)
  {
    try
    {
      JspAppCompiler jac = new JspAppCompiler(args[0], null);
      jac.compile();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
