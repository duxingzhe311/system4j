package com.dw.map;

public class MapRect
{
  public long left;
  public long bottom;
  public long width;
  public long height;

  public MapRect(long left, long bottom, long w, long h)
  {
    this.left = left;
    this.bottom = bottom;

    this.width = w;
    this.height = h;
  }

  public MapRect(MapRect mr)
  {
    this.left = mr.left;
    this.bottom = mr.bottom;

    this.width = mr.width;
    this.height = mr.height;
  }

  public boolean containsPoint(MapPoint mp)
  {
    if (mp.x < this.left) {
      return false;
    }
    if (this.left + this.width < mp.x) {
      return false;
    }
    if (mp.y < this.bottom) {
      return false;
    }
    if (this.bottom + this.height < mp.y) {
      return false;
    }
    return true;
  }

  public boolean containsPoint(long x, long y)
  {
    if (x < this.left) {
      return false;
    }
    if (this.left + this.width < x) {
      return false;
    }
    if (y < this.bottom) {
      return false;
    }
    if (this.bottom + this.height < y) {
      return false;
    }
    return true;
  }

  public boolean containsPoint(double x, double y)
  {
    if (x < this.left) {
      return false;
    }
    if (this.left + this.width < x) {
      return false;
    }
    if (y < this.bottom) {
      return false;
    }
    if (this.bottom + this.height < y) {
      return false;
    }
    return true;
  }

  public MapPoint getCenter()
  {
    return new MapPoint(this.left + this.width / 2L, this.bottom + this.height / 2L);
  }

  public long getRight()
  {
    return this.left + this.width;
  }

  public boolean containsRect(MapRect othermr)
  {
    if (!containsPoint(othermr.left, othermr.bottom)) {
      return false;
    }
    if (othermr.left + othermr.width > this.left + this.width) {
      return false;
    }
    if (othermr.bottom + othermr.height > this.bottom + this.height) {
      return false;
    }
    return true;
  }

  public long getTop()
  {
    return this.bottom + this.height;
  }

  public void expandByRect(MapRect mr)
  {
    if (mr.left < this.left)
    {
      this.width += this.left - mr.left;
      this.left = mr.left;
    }

    if (mr.bottom < this.bottom)
    {
      this.height += this.bottom - mr.bottom;
      this.bottom = mr.bottom;
    }

    long mrr = mr.getRight();
    if (mrr > getRight()) {
      this.width = (mrr - this.left);
    }
    long mrb = mr.getTop();
    if (mrb > getTop())
      this.height = (mrb - this.bottom);
  }

  public void expandByPoint(MapPoint mp)
  {
    if (mp.x < this.left)
    {
      this.width += this.left - mp.x;
      this.left = mp.x;
    }

    if (mp.y < this.bottom)
    {
      this.height += this.bottom - mp.y;
      this.bottom = mp.y;
    }

    if (mp.x > getRight()) {
      this.width = (mp.x - this.left);
    }
    if (mp.y > getTop())
      this.height = (mp.y - this.bottom);
  }

  public void translate(long dx, long dy)
  {
    long oldv = this.left;
    long newv = oldv + dx;
    if (dx < 0L)
    {
      if (newv > oldv)
      {
        if (this.width >= 0L)
        {
          this.width += newv - -9223372036854775808L;
        }

        newv = -9223372036854775808L;
      }

    }
    else if (newv < oldv)
    {
      if (this.width >= 0L)
      {
        this.width += newv - 9223372036854775807L;

        if (this.width < 0L) this.width = 9223372036854775807L;
      }
      newv = 9223372036854775807L;
    }

    this.left = newv;

    oldv = this.bottom;
    newv = oldv + dy;
    if (dy < 0L)
    {
      if (newv > oldv)
      {
        if (this.height >= 0L)
        {
          this.height += newv - -9223372036854775808L;
        }

        newv = -9223372036854775808L;
      }

    }
    else if (newv < oldv)
    {
      if (this.height >= 0L)
      {
        this.height += newv - 9223372036854775807L;
        if (this.height < 0L) this.height = 9223372036854775807L;
      }
      newv = 9223372036854775807L;
    }

    this.bottom = newv;
  }

  public String toString()
  {
    return "L" + this.left + " B" + this.bottom + " W" + this.width + " H" + this.height;
  }
}
