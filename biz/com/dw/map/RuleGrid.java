package com.dw.map;

import com.dw.system.Convert;
import java.util.ArrayList;
import java.util.Comparator;

public class RuleGrid
{
  public long base_rule = 1L;

  public long gx = 0L;

  public long gy = 0L;

  public RuleGrid(long baser, long gx, long gy)
  {
    if (baser < 1L) {
      throw new IllegalArgumentException("base rule cannot < 1");
    }

    this.base_rule = baser;
    this.gx = gx;
    this.gy = gy;
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof RuleGrid))
      return false;
    RuleGrid rg = (RuleGrid)obj;

    if (this.base_rule != rg.base_rule) {
      return false;
    }
    if (this.gx != rg.gx) {
      return false;
    }
    if (this.gy != rg.gy) {
      return false;
    }
    return true;
  }

  public int hashCode()
  {
    return (this.base_rule + "_" + this.gx + "_" + this.gy).hashCode();
  }

  public boolean containsMapPoint(MapPoint mp)
  {
    long left = this.base_rule * this.gx;
    if (mp.x < left) {
      return false;
    }
    long btm = this.base_rule * this.gy;
    if (mp.y < btm) {
      return false;
    }
    long right = left + this.base_rule;
    if (mp.x >= right) {
      return false;
    }
    long top = btm + this.base_rule;
    if (mp.y >= top) {
      return false;
    }
    return true;
  }

  public static RuleGrid calBelongRuleGrid(long baserule, MapPoint mp)
  {
    return calBelongRuleGrid(baserule, mp.x, mp.y);
  }

  public static RuleGrid calBelongRuleGrid(long baserule, long px, long py)
  {
    if (baserule < 1L) {
      throw new IllegalArgumentException("基准尺不能小�?");
    }

    long x = Math.abs(px); long y = Math.abs(py);

    if (px < 0L)
    {
      x = -(x / baserule + (x % baserule != 0L ? 1 : 0));
    }
    else
    {
      x /= baserule;
    }

    if (py < 0L)
    {
      y = -(y / baserule + (y % baserule != 0L ? 1 : 0));
    }
    else
    {
      y /= baserule;
    }

    return new RuleGrid(baserule, x, y);
  }

  public static ArrayList<MapPoint> calInterPtsInSegLineByRuleGrid(long baserule, MapPoint spt, MapPoint ept)
  {
    RuleGrid startrg = calBelongRuleGrid(baserule, spt);
    RuleGrid endrg = calBelongRuleGrid(baserule, ept);

    if (startrg.equals(endrg)) {
      return null;
    }
    long sx = startrg.gx;
    long ex = endrg.gx;
    if (sx > ex)
    {
      sx = ex;
      ex = startrg.gx;
    }
    long ptnum_x = ex - sx;

    long sy = startrg.gy;
    long ey = endrg.gy;
    if (sy > ey)
    {
      sy = ey;
      ey = startrg.gy;
    }

    long ptnum_y = ey - sy;
    if (ptnum_x + ptnum_y <= 0L) {
      return null;
    }
    ArrayList mps = new ArrayList((int)(ptnum_x + ptnum_y));

    if (sx != ex)
    {
      double k = (ept.y - spt.y) / (ept.x - spt.x);
      double m = spt.y - k * spt.x;

      for (long i = sx; i < ex; i += 1L)
      {
        long x = (i + 1L) * baserule;

        long y = ()(k * x + m);

        MapPoint mp = new MapPoint(x, y);
        if (!mps.contains(mp)) {
          mps.add(mp);
        }
      }
    }
    if (sy != ey)
    {
      double k = (ept.x - spt.x) / (ept.y - spt.y);
      double m = spt.x - k * spt.y;

      for (long i = sy; i < ey; i += 1L)
      {
        long y = (i + 1L) * baserule;

        long x = ()(k * y + m);

        MapPoint mp = new MapPoint(x, y);
        if (!mps.contains(mp)) {
          mps.add(mp);
        }
      }
    }

    Convert.sort(mps, new LineSegPointCompar(spt, ept));

    return mps;
  }

  static class LineSegPointCompar
    implements Comparator<MapPoint>
  {
    MapPoint startp = null; MapPoint endp = null;

    public LineSegPointCompar(MapPoint spt, MapPoint ept)
    {
      this.startp = spt;
      this.endp = ept;
    }

    public int compare(MapPoint p1, MapPoint p2)
    {
      long detax = p2.x - p1.x;
      if (detax > 0L)
        detax = 1L;
      else if (detax < 0L) {
        detax = -1L;
      }
      long detay = p2.y - p1.y;
      if (detay > 0L)
        detay = 1L;
      else if (detay < 0L) {
        detay = -1L;
      }
      if (detax != 0L)
      {
        if (this.startp.x < this.endp.x)
        {
          return (int)detax;
        }
        if (this.startp.x > this.endp.x)
        {
          return -(int)detax;
        }
      }

      if (detay != 0L)
      {
        if (this.startp.y < this.endp.y)
        {
          return (int)detay;
        }
        if (this.startp.y > this.endp.y)
        {
          return -(int)detay;
        }
      }

      return 0;
    }
  }
}
