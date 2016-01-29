package com.dw.portal;

import com.dw.system.AppConfig;
import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Border
{
  AppWebConfig appWebConf = null;

  String name = null;

  String title = null;

  String desc = null;

  String pathFile = null;

  String encoding = null;

  transient ArrayList<Object> contList = null;

  ArrayList<Object> headLs = null;
  ArrayList<Object> tailLs = null;

  public Border(AppWebConfig awc, String name, String title, String desc, String path, String enc)
  {
    this.appWebConf = awc;
    this.name = name;
    this.title = title;
    this.desc = desc;
    this.pathFile = path;
    this.encoding = enc;
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
        fis = new FileInputStream(AppConfig.getTomatoWebappBase() + this.pathFile);
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
      catch (IOException ioe)
      {
        ioe.printStackTrace();
      }
      finally
      {
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

  public AppWebConfig getAppWebConfig()
  {
    return this.appWebConf;
  }

  public String getUniqueName()
  {
    return this.appWebConf.getModuleName() + "." + this.name;
  }

  public String getLocalName()
  {
    return this.name;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public String getPathFile()
  {
    return this.pathFile;
  }

  private ArrayList<Object> getHeadLs()
  {
    if (this.headLs != null) {
      return this.headLs;
    }
    synchronized (this)
    {
      if (this.headLs != null) {
        return this.headLs;
      }
      init();

      ArrayList ss = new ArrayList();
      for (Iterator localIterator = this.contList.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

        if ((o instanceof String))
        {
          ss.add(o);
        }
        else if ((o instanceof Container))
        {
          Container c = (Container)o;
          if ("main".equalsIgnoreCase(c.getName()))
          {
            break;
          }

          ss.add(c);
        }
      }

      this.headLs = ss;
      return this.headLs;
    }
  }

  private ArrayList<Object> getTailLs()
  {
    if (this.tailLs != null) {
      return this.tailLs;
    }
    synchronized (this)
    {
      if (this.tailLs != null) {
        return this.tailLs;
      }
      init();

      boolean bp = false;
      ArrayList ss = new ArrayList();
      for (Iterator localIterator = this.contList.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

        if (bp)
        {
          ss.add(o);
        }
        else if ((o instanceof Container))
        {
          Container c = (Container)o;
          if ("main".equalsIgnoreCase(c.getName()))
          {
            bp = true;
          }
        }
      }

      this.tailLs = ss;
      return this.tailLs;
    }
  }

  public void renderHead(Writer out, HashMap<String, String> cn2val)
    throws Exception
  {
    ArrayList ss = getHeadLs();
    for (Iterator localIterator = ss.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

      if ((o instanceof String))
      {
        out.write((String)o);
      }
      else if ((cn2val != null) && ((o instanceof Container)))
      {
        Container c = (Container)o;
        String n = c.getName();
        String v = (String)cn2val.get(n);
        if (v != null)
          out.write(v);
      }
    }
  }

  public void renderTail(Writer out, HashMap<String, String> cn2val)
    throws Exception
  {
    ArrayList ss = getTailLs();
    for (Iterator localIterator = ss.iterator(); localIterator.hasNext(); ) { Object o = localIterator.next();

      if ((o instanceof String))
      {
        out.write((String)o);
      }
      else if ((cn2val != null) && ((o instanceof Container)))
      {
        Container c = (Container)o;
        String n = c.getName();
        String v = (String)cn2val.get(n);
        if (v != null)
          out.write(v);
      }
    }
  }

  public static class Container
  {
    String name = null;
    HashMap<String, String> props = new HashMap();

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
    }

    Container(String n, HashMap<String, String> p)
    {
      this.name = n;
      this.props = p;
      if (this.props == null)
        this.props = new HashMap();
    }

    public String getName()
    {
      return this.name;
    }

    public HashMap<String, String> getProps()
    {
      return this.props;
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
