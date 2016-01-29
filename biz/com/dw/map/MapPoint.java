package com.dw.map;

import com.dw.grid.GpsPos;

public class MapPoint
{
  public long x;
  public long y;
  public boolean real = true;

  public static double calDist(MapPoint p1, MapPoint p2)
  {
    long detax = p1.x - p2.x;
    long detay = p1.y - p2.y;
    return Math.sqrt(detax * detax + detay * detay);
  }

  public static long calDistNotSqrt(MapPoint p1, MapPoint p2)
  {
    long detax = p1.x - p2.x;
    long detay = p1.y - p2.y;
    return detax * detax + detay * detay;
  }

  public MapPoint(long x, long y)
  {
    this.x = x;
    this.y = y;
  }

  public MapPoint(GpsPos gp)
  {
    this.x = gp.getLongitudeAsTenthSecond();
    this.y = gp.getLatitudeAsTenthSecond();
  }

  public MapPoint(double gps_lng, double gps_lat)
  {
    GpsPos gp = new GpsPos(gps_lng, gps_lat);
    this.x = gp.getLongitudeAsTenthSecond();
    this.y = gp.getLatitudeAsTenthSecond();
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof MapPoint))
    {
      return false;
    }

    MapPoint mp = (MapPoint)obj;

    if (this.x != mp.x) {
      return false;
    }
    if (this.y != mp.y) {
      return false;
    }
    return true;
  }

  public GpsPos calGpsPos()
  {
    return new GpsPos(this.x, this.y);
  }

  public int hashCode()
  {
    return (this.x + "_" + this.y).hashCode();
  }

  public String toString()
  {
    return "(" + this.x + "," + this.y + ")";
  }
}
