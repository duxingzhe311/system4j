package com.dw.biz;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class BizPath
{
  String moduleName = null;
  String[] catPaths = null;
  String nodeName = null;
  String nodeType = null;

  boolean bInner = false;
  String innerId = null;

  public BizPath(String strpath)
  {
    if (strpath.charAt(0) != '/') {
      throw new IllegalArgumentException("Invalid path=" + strpath + " BizPath must start with module likc /mmm/..");
    }
    int ip = strpath.lastIndexOf('$');
    if (ip > 0)
    {
      this.innerId = strpath.substring(ip + 1);
      this.bInner = true;
      strpath = strpath.substring(0, ip);
    }

    if (strpath.endsWith("/"))
    {
      StringTokenizer tmpst = new StringTokenizer(strpath, "/");
      int c = tmpst.countTokens();
      if (c <= 0) {
        throw new IllegalArgumentException("Invalid path=" + strpath + " BizPath must has moudle name");
      }
      this.moduleName = tmpst.nextToken();
      if ((this.moduleName == null) || (this.moduleName.equals(""))) {
        throw new IllegalArgumentException("Invalid path=" + strpath + " module name is empty");
      }
      this.catPaths = new String[c - 1];
      for (int i = 1; i < c; i++)
        this.catPaths[(i - 1)] = tmpst.nextToken();
    }
    else
    {
      StringTokenizer tmpst = new StringTokenizer(strpath, "/");
      int c = tmpst.countTokens();
      if (c <= 1) {
        throw new IllegalArgumentException("Invalid path=" + strpath + " BizPath must has moudle name like /mmm/xxx ...");
      }
      this.moduleName = tmpst.nextToken();
      if ((this.moduleName == null) || (this.moduleName.equals(""))) {
        throw new IllegalArgumentException("Invalid path=" + strpath + " module name is empty");
      }
      this.catPaths = new String[c - 2];
      for (int i = 1; i < c - 1; i++) {
        this.catPaths[(i - 1)] = tmpst.nextToken();
      }
      this.nodeName = tmpst.nextToken();
      int p = this.nodeName.lastIndexOf('.');
      if (p <= 0) {
        throw new IllegalArgumentException("invalid str node path=" + strpath + " ,for it must end like ../aa.view");
      }
      this.nodeType = this.nodeName.substring(p + 1);
      Class cc = BizManager.getBizNodeTypeClass(this.nodeType);
      if (cc == null) {
        throw new IllegalArgumentException("Invalid path=" + strpath + "  ,unknown node type=" + this.nodeType);
      }
      this.nodeName = this.nodeName.substring(0, p);
    }
  }

  public BizPath(String modulen, String[] catpaths)
  {
    if ((modulen == null) || (modulen.equals(""))) {
      throw new IllegalArgumentException("module name cannot be null");
    }
    this.moduleName = modulen;
    this.catPaths = catpaths;
  }

  public BizPath(String modulen, String[] catpaths, String nodename, String nodetype)
  {
    this(modulen, catpaths);

    if ((nodename == null) || (nodename.equals(""))) {
      throw new IllegalArgumentException("node path must has node name!");
    }
    Class c = BizManager.getBizNodeTypeClass(nodetype);
    if (c == null)
      throw new IllegalArgumentException("unknown node type!");
    this.nodeName = nodename;
    this.nodeType = nodetype;
  }

  public BizPath(String modulen, String[] catpaths, String filename)
  {
    if ((modulen == null) || (modulen.equals(""))) {
      throw new IllegalArgumentException("module name cannot be null");
    }
    this.moduleName = modulen;
    this.catPaths = catpaths;

    int p = filename.lastIndexOf('.');
    if (p <= 0) {
      throw new IllegalArgumentException("invalid file name : no ext found");
    }
    this.nodeName = filename.substring(0, p);
    this.nodeType = filename.substring(p + 1);
    Class c = BizManager.getBizNodeTypeClass(this.nodeType);
    if (c == null)
      throw new IllegalArgumentException("unknown node type=" + this.nodeType);
  }

  public BizPath calRelatedPath(String relatedp)
  {
    ArrayList catps = new ArrayList();
    if (this.catPaths != null)
    {
      for (String cp : catps)
      {
        catps.add(cp);
      }
    }

    StringTokenizer st = new StringTokenizer(relatedp, "/\\");
    int c = st.countTokens();
    for (int i = 0; i < c - 1; i++)
    {
      catps.add(st.nextToken());
    }

    String fn = st.nextToken();

    String[] tmps = new String[catps.size()];
    catps.toArray(tmps);
    return new BizPath(this.moduleName, tmps, fn);
  }

  public boolean isCat()
  {
    return this.nodeName == null;
  }

  public boolean isNode()
  {
    return this.nodeName != null;
  }

  public String getModuleName()
  {
    return this.moduleName;
  }

  public String[] getCatPaths()
  {
    return this.catPaths;
  }

  public String getNodeName()
  {
    return this.nodeName;
  }

  public String getNodeType()
  {
    return this.nodeType;
  }

  public boolean isInner()
  {
    return this.bInner;
  }

  public String getInnerId()
  {
    return this.innerId;
  }

  public String toString()
  {
    StringBuffer tmpsb = new StringBuffer();
    tmpsb.append('/').append(this.moduleName).append('/');

    if (this.catPaths != null)
    {
      for (String s : this.catPaths)
      {
        tmpsb.append(s).append('/');
      }
    }

    if (this.nodeName != null) {
      tmpsb.append(this.nodeName).append('.').append(this.nodeType);
    }
    if (this.bInner) {
      tmpsb.append('$').append(this.innerId);
    }
    return tmpsb.toString();
  }
}
