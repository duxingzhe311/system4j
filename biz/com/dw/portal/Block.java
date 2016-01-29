package com.dw.portal;

import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import com.dw.user.UserProfile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.w3c.dom.Element;

public class Block
{
  AppWebConfig appWebConf = null;

  String viewPath = null;
  String title = null;
  String desc = null;

  ArrayList<Oper> opers = null;

  static Oper createOper(String modulen, Element e0)
  {
    String n0 = e0.getAttribute("name");
    if (Convert.isNullOrEmpty(n0)) {
      return null;
    }
    String t0 = e0.getAttribute("title");
    if (Convert.isNullOrEmpty(t0)) {
      t0 = n0;
    }
    String u0 = e0.getAttribute("url");
    if (Convert.isNullOrEmpty(u0)) {
      return null;
    }
    if (!u0.startsWith("/"))
    {
      u0 = "/" + modulen + "/" + u0;
    }

    String rolestr = e0.getAttribute("roles");
    HashSet rs = null;
    if (Convert.isNotNullEmpty(rolestr))
    {
      rs = new HashSet();
      StringTokenizer st = new StringTokenizer(rolestr, "|,");
      while (st.hasMoreTokens()) {
        rs.add(st.nextToken());
      }
    }
    return new Oper(n0, t0, u0, rs, null);
  }

  public Block(AppWebConfig awc, String viewpath, String t, String d, ArrayList<Oper> opers)
  {
    this.appWebConf = awc;
    this.viewPath = viewpath;
    this.title = t;
    this.desc = d;
    this.opers = opers;
  }

  public String getViewPath()
  {
    return this.viewPath;
  }

  public String getTitle()
  {
    if (this.title == null) {
      return "";
    }
    return this.title;
  }

  public String getDesc()
  {
    return this.desc;
  }

  public ArrayList<Oper> getOpers()
  {
    return this.opers;
  }

  public static class Oper
  {
    String name = null;
    String title = null;

    String url = null;

    HashSet<String> roles = null;

    private Oper(String n, String t, String u, HashSet<String> rs)
    {
      this.name = n;
      this.title = t;
      this.url = u;
      this.roles = rs;
    }

    public String getName()
    {
      return this.name;
    }

    public String getTitle()
    {
      return this.title;
    }

    public String getUrl()
    {
      return this.url;
    }

    public boolean checkRight(UserProfile up)
    {
      if (this.roles == null) {
        return true;
      }
      return up.checkMatchOneRoleName(this.roles);
    }
  }
}
