package com.dw.biz;

public class BizCat extends BizNodeContainer
  implements Comparable<BizCat>
{
  String name = null;

  public BizCat(BizNodeContainer pbnc, String[] parentps, String name)
    throws Exception
  {
    super(pbnc, parentps);

    this.name = name;

    this.myCatPP = calMyCatPP(parentps, name);
  }

  private static String[] calMyCatPP(String[] parentcatpp, String name)
  {
    int c = 1;
    if (parentcatpp != null)
    {
      c = parentcatpp.length + 1;
    }

    String[] rets = new String[c];
    if (parentcatpp != null)
    {
      System.arraycopy(parentcatpp, 0, rets, 0, c - 1);
    }

    rets[(c - 1)] = name;
    return rets;
  }

  public String getName()
  {
    return this.name;
  }

  public int compareTo(BizCat bc)
  {
    return this.name.compareTo(bc.name);
  }
}
