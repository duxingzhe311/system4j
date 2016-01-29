package com.dw.portal;

import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Layout
{
  public static final String PROP_EDITABLE = "editable";
  public static final String PROP_DEFAULT = "default";
  public static final String PROP_IS_HTML = "is_html";
  AppWebConfig appWebConf = null;

  String path = null;

  File pathFile = null;

  String title = null;

  String desc = null;

  String encoding = null;

  ArrayList<Object> contList = null;

  ArrayList<Container> conts = null;

  Layout(AppWebConfig awc, String path, String title, String desc, String enc)
  {
    this.appWebConf = awc;
    this.path = catPath(awc.getModuleName(), path);
    String absp = awc.getModuleDirPath().getParentFile().getAbsolutePath() + path;
    this.pathFile = new File(absp);
    this.title = title;
    this.desc = desc;
    this.encoding = enc;
  }

  private String catPath(String appn, String p)
  {
    if (p == null) {
      return "";
    }
    if (p.startsWith("/")) {
      return p;
    }
    return "/" + appn + "/" + p;
  }

  private void init()
  {
    if (this.contList != null)
    {
      return;
    }

    synchronized (this)
    {
      if (this.contList != null)
      {
        return;
      }

      ArrayList cl = new ArrayList();

      FileInputStream fis = null;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try
      {
        fis = new FileInputStream(this.pathFile);
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = fis.read(buf)) != -1)
        {
          baos.write(buf, 0, len);
        }

        String temp_cont = null;
        if (Convert.isNotNullEmpty(this.encoding))
          temp_cont = baos.toString(this.encoding);
        else {
          temp_cont = baos.toString();
        }
        while (temp_cont != null)
        {
          int sp = temp_cont.indexOf("[#");
          if (sp < 0)
          {
            if (!temp_cont.equals(""))
              cl.add(temp_cont);
            temp_cont = null;
            break;
          }

          int ep = temp_cont.indexOf("#]", sp + 2);
          if (ep < 0)
            throw new IllegalArgumentException("invalid template, it must be split by string like [#  #]");
          String tmps = temp_cont.substring(0, sp);
          if (!tmps.equals(""))
            cl.add(tmps);
          String bn = temp_cont.substring(sp + 2, ep).trim();
          Container c = new Container(bn);
          cl.add(c);
          temp_cont = temp_cont.substring(ep + 2);
        }

        this.contList = cl;
      }
      catch (IOException localIOException)
      {
      }
      finally {
        if (fis != null)
        {
          try
          {
            fis.close();
          }
          catch (IOException localIOException1)
          {
          }
        }
      }
    }
  }

  public String getPath()
  {
    return this.path;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public ArrayList<Object> getContList()
  {
    init();

    return this.contList;
  }

  public ArrayList<Container> getContains()
  {
    if (this.conts != null) {
      return this.conts;
    }
    init();

    ArrayList cs = new ArrayList();
    for (Iterator localIterator = this.contList.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

      if ((o instanceof Container))
        cs.add((Container)o);
    }
    this.conts = cs;
    return this.conts;
  }

  public Container getContainerByName(String name)
  {
    ArrayList cs = getContains();
    if (cs == null) {
      return null;
    }
    for (Container c : cs)
    {
      if (name.equals(c.name)) {
        return c;
      }
    }
    return null;
  }

  public class Container
  {
    boolean bEditable = false;
    String name = null;
    HashMap<String, String> props = new HashMap();

    private transient Border defBorder = null;

    Container(String n)
    {
      int p = n.indexOf(' ');
      if (p > 0)
      {
        this.name = n.substring(0, p);
        String tmps = n.substring(p + 1).trim();

        p = 0;
        while (tmps != null)
        {
          int i = tmps.indexOf('=', p);
          if (i < 0)
          {
            this.props.put(tmps, "");
            return;
          }

          String pn = tmps.substring(p, i).trim();
          tmps = tmps.substring(i + 1).trim();
          char c = tmps.charAt(0);
          if ((c != '\'') && (c != '"')) {
            throw new RuntimeException("page block must like [#xx xx=\"xx\"#]");
          }
          int j = tmps.indexOf(c, 1);
          if (j <= 0) {
            throw new RuntimeException("page block must like [#xx xx=\"xx\"#]");
          }
          String pv = tmps.substring(1, j);
          this.props.put(pn, pv);
          tmps = tmps.substring(j + 1).trim();
          if (tmps.equals(""))
            break;
        }
      }
      else
      {
        this.name = n;
      }

      this.bEditable = "true".equalsIgnoreCase((String)this.props.get("editable"));
    }

    Container(HashMap<String, String> n)
    {
      this.name = n;
      this.props = p;
      if (this.props == null) {
        this.props = new HashMap();
      }
      this.bEditable = "true".equalsIgnoreCase((String)this.props.get("editable"));
    }

    public boolean isEditable()
    {
      return this.bEditable;
    }

    public String getEditableDefaultOutTxt()
    {
      if (!this.bEditable) {
        return null;
      }
      if (this.props == null) {
        return null;
      }
      boolean bhtml = "true".equalsIgnoreCase((String)this.props.get("is_html"));
      String t = (String)this.props.get("default");
      if (t == null) {
        return null;
      }
      if (bhtml) {
        return t;
      }
      return Convert.plainToHtml(t);
    }

    public String getName()
    {
      return this.name;
    }

    public HashMap<String, String> getProps()
    {
      return this.props;
    }

    public Border getDefaultBorder()
    {
      if (this.defBorder != null) {
        return this.defBorder;
      }
      String bp = (String)this.props.get("border_path");
      if (Convert.isNullOrEmpty(bp)) {
        return null;
      }
      String absp = Convert.calAbsPath(
        "/" + Layout.this.appWebConf.getModuleName() + "/", bp);
      this.defBorder = PortalManager.getInstance().getBorderByPath(absp);
      return this.defBorder;
    }

    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append("[#").append(this.name);
      for (Map.Entry kv : this.props.entrySet())
      {
        sb.append(" ").append((String)kv.getKey()).append("=\"").append((String)kv.getValue()).append("\"");
      }
      sb.append("#]");
      return sb.toString();
    }
  }
}
