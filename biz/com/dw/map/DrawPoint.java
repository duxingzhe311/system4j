package com.dw.map;

public class DrawPoint
{
  public int x;
  public int y;

  public DrawPoint(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof DrawPoint))
    {
      return false;
    }
    DrawPoint odp = (DrawPoint)obj;

    return (this.x == odp.x) && (this.y == odp.y);
  }

  public String toString()
  {
    return "(" + this.x + "," + this.y + ")";
  }
}
