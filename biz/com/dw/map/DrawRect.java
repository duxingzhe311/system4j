package com.dw.map;

public class DrawRect
{
  public int left;
  public int top;
  public int width;
  public int height;

  public DrawRect(int left, int top, int w, int h)
  {
    this.left = left;
    this.top = top;

    this.width = w;
    this.height = h;
  }

  public DrawRect(DrawPoint dp1, DrawPoint dp2)
  {
    this.left = dp1.x;
    this.width = (dp2.x - dp1.x);
    if (this.left > dp2.x)
    {
      this.left = dp2.x;
      this.width = (-this.width);
    }

    this.top = dp1.y;
    this.height = (dp2.y - dp1.y);
    if (this.top > dp2.y)
    {
      this.top = dp2.y;
      this.height = (-this.height);
    }
  }

  public boolean containsPoint(DrawPoint mp)
  {
    if (mp.x < this.left)
      return false;
    if (mp.y > this.top)
      return false;
    if (this.left + this.width < mp.x)
      return false;
    if (this.top + this.height < mp.y) {
      return false;
    }
    return true;
  }

  public boolean containsPoint(int x, int y)
  {
    if (x < this.left) {
      return false;
    }
    if (this.left + this.width < x) {
      return false;
    }
    if (y < this.top) {
      return false;
    }
    if (this.top + this.height < y) {
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
    if (y < this.top) {
      return false;
    }
    if (this.top + this.height < y) {
      return false;
    }
    return true;
  }

  public DrawPoint getCenter()
  {
    return new DrawPoint(this.left + this.width / 2, this.top + this.height / 2);
  }

  public int getRight()
  {
    return this.left + this.width;
  }

  public int getBottom()
  {
    return this.top + this.height;
  }

  public void expandByRect(DrawRect mr)
  {
    if (mr.left < this.left)
    {
      this.width += this.left - mr.left;
      this.left = mr.left;
    }

    if (mr.top < this.top)
    {
      this.height += this.top - mr.top;
      this.top = mr.top;
    }

    int mrr = mr.getRight();
    if (mrr > getRight()) {
      this.width = (mrr - this.left);
    }
    int mrb = mr.getBottom();
    if (mrb > getBottom())
      this.height = (mrb - this.top);
  }

  public void translate(int dx, int dy)
  {
    int oldv = this.left;
    int newv = oldv + dx;
    if (dx < 0)
    {
      if (newv > oldv)
      {
        if (this.width >= 0)
        {
          this.width += newv - -2147483648;
        }

        newv = -2147483648;
      }

    }
    else if (newv < oldv)
    {
      if (this.width >= 0)
      {
        this.width += newv - 2147483647;

        if (this.width < 0) this.width = 2147483647;
      }
      newv = 2147483647;
    }

    this.left = newv;

    oldv = this.top;
    newv = oldv + dy;
    if (dy < 0)
    {
      if (newv > oldv)
      {
        if (this.height >= 0)
        {
          this.height += newv - -2147483648;
        }

        newv = -2147483648;
      }

    }
    else if (newv < oldv)
    {
      if (this.height >= 0)
      {
        this.height += newv - 2147483647;
        if (this.height < 0) this.height = 2147483647;
      }
      newv = 2147483647;
    }

    this.top = newv;
  }

  public String toString()
  {
    return "L" + this.left + " T" + this.top + " W" + this.width + " H" + this.height;
  }
}
