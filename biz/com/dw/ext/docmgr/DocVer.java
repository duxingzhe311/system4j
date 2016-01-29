package com.dw.ext.docmgr;

import com.dw.system.Convert;
import java.util.StringTokenizer;

public class DocVer
  implements Comparable<DocVer>
{
  int userSeg = 0;
  int[] autoSeg = null;

  public static DocVer createFirstAutoVer()
  {
    DocVer dv = new DocVer();
    dv.autoSeg = new int[1];
    dv.autoSeg[0] = 1;
    return dv;
  }

  public static String transToShowStr(String ss)
  {
    if (Convert.isNullOrEmpty(ss)) {
      return "";
    }
    if (ss.startsWith("000"))
      return ss.substring(3);
    if (ss.startsWith("00"))
      return ss.substring(2);
    if (ss.startsWith("0")) {
      return ss.substring(1);
    }
    return ss;
  }

  private DocVer()
  {
  }

  public DocVer(String strver)
  {
    if (Convert.isNullOrEmpty(strver)) {
      return;
    }
    StringTokenizer st = new StringTokenizer(strver, ".");
    this.autoSeg = new int[st.countTokens() - 1];
    if (st.hasMoreTokens())
    {
      String s = st.nextToken();
      this.userSeg = Integer.parseInt(s);
    }

    int i = 0;
    while (st.hasMoreTokens())
    {
      this.autoSeg[i] = Integer.parseInt(st.nextToken());
      i++;
    }
  }

  public DocVer(DocVer dv)
  {
    this.userSeg = dv.userSeg;
    if (dv.autoSeg != null)
    {
      this.autoSeg = new int[dv.autoSeg.length];
      System.arraycopy(dv.autoSeg, 0, this.autoSeg, 0, this.autoSeg.length);
    }
  }

  public DocVer autoIncrease()
  {
    DocVer newdv = new DocVer(this);

    if ((newdv.autoSeg == null) || (newdv.autoSeg.length == 0))
    {
      newdv.autoSeg = new int[1];
      newdv.autoSeg[0] = 1;
      return newdv;
    }

    int c = newdv.autoSeg.length - 1;

    if (newdv.autoSeg[c] < 999)
    {
      newdv.autoSeg[c] += 1;
      return newdv;
    }

    int[] tmpis = newdv.autoSeg;
    newdv.autoSeg = new int[newdv.autoSeg.length + 1];
    System.arraycopy(tmpis, 0, newdv.autoSeg, 0, tmpis.length);
    newdv.autoSeg[c] = 1;

    return newdv;
  }

  public int getUserVer()
  {
    return this.userSeg;
  }

  public DocVer userIncrease(int num)
  {
    if (num <= 0) {
      throw new IllegalArgumentException("increase num < = 0 ");
    }
    if (this.userSeg + num > 9999) {
      throw new IllegalArgumentException("version cannot > 9999");
    }
    DocVer newdv = new DocVer(this);
    newdv.userSeg += num;
    return newdv;
  }

  public DocVer(DocVer basedv, int user_ver)
  {
    if (user_ver <= basedv.userSeg) {
      throw new IllegalArgumentException("user version must > base ver");
    }
    if (user_ver > 9999) {
      throw new IllegalArgumentException("user version cannot > 9999");
    }
    this.userSeg = user_ver;
    if (basedv.autoSeg != null)
    {
      this.autoSeg = new int[basedv.autoSeg.length];
      System.arraycopy(basedv.autoSeg, 0, this.autoSeg, 0, this.autoSeg.length);
    }
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    String strus = this.userSeg;
    int c = strus.length();
    if (c == 1)
    {
      strus = "000" + strus;
    }
    else if (c == 2)
    {
      strus = "00" + strus;
    }
    else if (c == 3)
    {
      strus = "0" + strus;
    }
    sb.append(strus);

    if ((this.autoSeg != null) && (this.autoSeg.length > 0))
    {
      for (int i : this.autoSeg)
      {
        String tmps = i;
        c = tmps.length();
        if (c == 1)
        {
          tmps = "00" + tmps;
        }
        else if (c == 2)
        {
          tmps = "0" + tmps;
        }

        sb.append('.').append(tmps);
      }
    }

    return sb.toString();
  }

  public int compareTo(DocVer ov)
  {
    int i = this.userSeg - ov.userSeg;
    if (i != 0) {
      return i;
    }
    int aslen = 0;
    if (this.autoSeg != null)
      aslen = this.autoSeg.length;
    int oaslen = 0;
    if (ov.autoSeg != null) {
      oaslen = ov.autoSeg.length;
    }
    int c = Math.max(aslen, oaslen);
    for (i = 0; i < c; i++)
    {
      if (aslen < i + 1) {
        return -1;
      }
      if (oaslen < i + 1) {
        return 1;
      }
      int tmpi = this.autoSeg[i] - ov.autoSeg[i];
      if (tmpi != 0) {
        return tmpi;
      }
    }
    return 0;
  }
}
