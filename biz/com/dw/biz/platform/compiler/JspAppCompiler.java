package com.dw.biz.platform.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Properties;
import java.util.StringTokenizer;

public class JspAppCompiler
{
  static int TAB_FUNC = 5;
  static int TAB_VAR = 5;
  static int TAB_CONTENT = 15;

  JspAppReader fileReader = null;
  Writer out = null;

  static String lineSep = null;

  String packageStr = null;
  StringBuffer importSB = null;
  StringBuffer classSB = null;
  StringBuffer contentSB = null;
  StringBuffer variableSB = null;
  StringBuffer functionSB = null;
  StringBuffer staticContentSB = new StringBuffer();
  int staticVarCount = 0;
  int outWriteCount = 0;

  String extClassName = "BizRunner";
  String source = null;
  String className = null;

  boolean isDebug = false;

  static String SS_CODE = "\r\n";
  String tmpLine = "";
  boolean isStartContent = false;

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
    this.importSB.append("import com.dw.system.xmldata.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.biz.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.biz.platform.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.system.gdb.datax.*;");
    this.importSB.append(lineSep);
    this.importSB.append("import com.dw.user.*;");

    this.classSB = new StringBuffer();
    if ((this.className == null) || (this.className.equals(""))) {
      throw new RuntimeException("Cannot resolve class name!");
    }
    String cn = this.className;
    int i = this.className.lastIndexOf('.');
    if (i >= 0)
      cn = cn.substring(i + 1);
    this.classSB.append(lineSep + lineSep + "public class " + cn + " extends ").append(this.extClassName);
    this.classSB.append(lineSep + "{");

    this.contentSB = new StringBuffer();

    this.variableSB = new StringBuffer();

    this.functionSB = new StringBuffer();
    this.functionSB.append(tabSpace(TAB_FUNC) + "public void render(Writer _jsp_output_jac)");
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

  public JspAppCompiler(String source, String ext_classname)
    throws IOException
  {
    int i = source.lastIndexOf(".jsp");
    if (i < 0)
      throw new RuntimeException("invalid file extension");
    this.source = source.substring(0, i);
    setOutputFile(this.source + ".java");

    this.fileReader = new FileIncludeReader(source);
    this.className = resolveClassName(source);
    if (ext_classname != null) {
      this.extClassName = ext_classname;
    }
    init();
  }

  public JspAppCompiler(String source, String target, String ext_classname)
    throws IOException
  {
    int i = source.lastIndexOf(".jsp");
    if (i < 0)
      throw new RuntimeException("invalid file extension");
    this.source = source.substring(0, i);

    if (target.endsWith(".java"))
      setOutputFile(target);
    else {
      setOutputFile(target + ".java");
    }
    this.className = resolveClassName(source);
    this.fileReader = new FileIncludeReader(source);

    if (ext_classname != null) {
      this.extClassName = ext_classname;
    }
    init();
  }

  public JspAppCompiler(JspAppReader jar, String classname, String target, String ext_classname)
    throws IOException
  {
    if (target.endsWith(".java"))
      setOutputFile(target);
    else {
      setOutputFile(target + ".java");
    }
    this.fileReader = jar;
    this.className = classname;
    if (ext_classname != null) {
      this.extClassName = ext_classname;
    }
    init();
  }

  public void setDebug(boolean db)
  {
    this.isDebug = db;
  }

  public void setOutput(Writer output)
  {
    this.out = output;
  }

  public void setOutputFile(String target)
    throws IOException
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
    throws IOException
  {
    int c;
    while ((c = this.fileReader.read()) != -1)
    {
      int c;
      char ch = (char)c;
      if (ch == '<')
        maybeElementDecl();
      else {
        addContent(c);
      }
      if (this.fileReader.isEnd())
      {
        flushContentLine();
        break;
      }

    }

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

  private void mergeContent()
  {
    this.staticVarCount += 1;
    this.staticContentSB.append(tabSpace(TAB_CONTENT) + "private static String _jsp_string_jac_" + this.staticVarCount + " = \"" + this.tmpLine.trim() + "\" ;");
    this.contentSB.append(tabSpace(TAB_CONTENT) + "_jsp_output_jac.write (_jsp_string_jac_" + this.staticVarCount + ") ;");
    this.outWriteCount += 1;

    this.contentSB.append(tabSpace(TAB_CONTENT) + "return ;");

    this.tmpLine = "";
  }

  private void mergeFunc()
  {
    this.functionSB.append(this.contentSB.toString());

    this.functionSB.append(tabSpace(TAB_FUNC) + "}");
  }

  private void mergeClass()
  {
    this.classSB.append(this.variableSB.toString());

    this.classSB.append("\r\npublic int getWriteLineNumber ()\r\n");
    this.classSB.append("{\r\n");
    this.classSB.append("     return " + this.outWriteCount + " ;\r\n");
    this.classSB.append("}\r\n");

    this.classSB.append(this.functionSB.toString());
    this.classSB.append(this.staticContentSB.toString());
    this.classSB.append(lineSep + "}");
  }

  private void addContent(int c)
  {
    if (c == -1) {
      return;
    }
    char ch = (char)c;

    switch (ch)
    {
    case '\\':
      this.tmpLine += "\\\\";
      break;
    case '\'':
      this.tmpLine += "\\'";
      break;
    case '"':
      this.tmpLine += "\\\"";
      break;
    case '\r':
      this.tmpLine += "\\r";
      break;
    case '\n':
      this.tmpLine += "\\n";
      break;
    default:
      this.tmpLine += ch;
    }
  }

  private void flushContentLine()
  {
    if ((this.tmpLine == null) || (this.tmpLine.equals(""))) {
      return;
    }
    this.staticVarCount += 1;
    this.staticContentSB.append(tabSpace(TAB_CONTENT) + "private static String _jsp_string_jac_" + this.staticVarCount + " = \"" + this.tmpLine + "\" ;");
    this.contentSB.append(tabSpace(TAB_CONTENT) + "_jsp_output_jac.write (_jsp_string_jac_" + this.staticVarCount + ") ;");
    this.outWriteCount += 1;

    this.tmpLine = "";
  }

  private void maybeElementDecl()
    throws IOException
  {
    int c = this.fileReader.read();
    char ch = (char)c;
    if (ch != '%')
    {
      addContent(60);
      if (ch == '<') {
        maybeElementDecl();
      }
      else if (c != -1) {
        addContent(c);
      }
      return;
    }

    flushContentLine();
    c = this.fileReader.read();
    if (c == -1)
      throw new RuntimeException("<% has no matched %>");
    ch = (char)c;
    switch (ch)
    {
    case '@':
      maybeAttr(readElementDecl());
      break;
    case '=':
      maybeEqual(readElementDecl());
      break;
    case '!':
      maybeVar(readElementDecl());
      break;
    case '-':
      maybeComment(readElementDecl());
      break;
    default:
      maybeCode(ch, readElementDecl());
    }
  }

  private String readElementDecl()
    throws IOException
  {
    StringBuffer sb = new StringBuffer();
    while (true)
    {
      int c = this.fileReader.read();
      char ch = (char)c;
      if (c == -1) {
        throw new RuntimeException("find element has no %>");
      }
      if (ch != '%') {
        sb.append(ch);
      }
      else {
        ch = (char)this.fileReader.read();
        if (ch == '>')
        {
          break;
        }
        sb.append('%');
        sb.append(ch);
      }

    }

    return sb.toString().trim();
  }

  private void maybeCode(char ch, String codestr)
  {
    this.contentSB.append(tabSpace(TAB_CONTENT) + ch + codestr);
  }

  private void maybeComment(String commstr)
  {
    int len = commstr.length();
    if ((commstr.charAt(0) != '-') || 
      (commstr.charAt(len - 1) != '-') || 
      (commstr.charAt(len - 2) != '-'))
      throw new RuntimeException("wrong comment info=<%-" + commstr + "%>");
  }

  private void maybeVar(String varstr)
  {
    this.variableSB.append(tabSpace(TAB_VAR) + varstr);
  }

  private void maybeEqual(String equalstr)
  {
    equalstr = equalstr.trim();
    if (equalstr.charAt(equalstr.length() - 1) == ';')
      equalstr = equalstr.substring(0, equalstr.length() - 1);
    this.contentSB.append(tabSpace(TAB_CONTENT) + "_jsp_output_jac.write ((\"\"+" + equalstr + ")) ;");
    this.outWriteCount += 1;
  }

  private void maybeAttr(String attstr)
    throws IOException
  {
    StringTokenizer st = new StringTokenizer(attstr, " ", false);
    if (st.hasMoreTokens())
    {
      String attrn = st.nextToken();
      if (attrn.equals("page"))
      {
        if (!st.hasMoreTokens())
          throw new RuntimeException("<%page ... has no import");
        String ss = st.nextToken("= ");
        if (!ss.equals("import"))
          throw new RuntimeException("<%page ... has no import");
        if (!st.hasMoreTokens())
          throw new RuntimeException("<%page import=... has no info");
        while (st.hasMoreTokens())
        {
          ss = st.nextToken("= \"|").trim();
          if (ss.charAt(ss.length() - 1) != ';')
            ss = ss + ';';
          this.importSB.append(lineSep + "import " + ss);
        }
      }
      else if ((attrn.equals("include")) && ((this.fileReader instanceof FileIncludeReader)))
      {
        if (!st.hasMoreTokens())
          throw new RuntimeException("<%include ... has no file");
        String ss = st.nextToken("= ");
        if (!ss.equals("file"))
          throw new RuntimeException("<%include ... has no file");
        if (!st.hasMoreTokens())
          throw new RuntimeException("<%include file=... has no filename");
        ss = st.nextToken("= \"");
        ((FileIncludeReader)this.fileReader).putFile(ss);
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
