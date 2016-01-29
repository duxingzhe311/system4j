package com.dw.map;

public class MapWindow
{
  MapMain mapMain = null;

  long baseRule = 1L;

  MapRect mapRectInWin = null;
  DrawRect drawRect = null;

  MapPoint mapCenter = null;

  DrawPoint drawCenter = null;

  IMapModelListener modelLis = null;

  static long MAX_BASE_RULE = 4611686018427387903L;

  public MapWindow(IMapModelListener mlis, MapMain mm, DrawRect draw_rect)
  {
    this.modelLis = mlis;

    this.mapMain = mm;

    reset(draw_rect);
  }

  public MapMain getMapMain()
  {
    return this.mapMain;
  }

  private static long calFitBaseRule(MapRect map_rect, DrawRect draw_rect)
  {
    if (map_rect == null) {
      return 1L;
    }
    if ((map_rect.width == 0L) || (map_rect.height == 0L)) {
      return 1L;
    }
    long v1 = map_rect.width / draw_rect.width + 1L;
    long v2 = map_rect.height / draw_rect.height + 1L;
    if (v1 > v2) {
      return v1;
    }
    return v2;
  }

  private MapRect recalMapRectInDrawWin()
  {
    MapPoint mp0 = calMapPoint(0, 0);
    MapPoint mp1 = calMapPoint(this.drawRect.width, this.drawRect.height);

    this.mapRectInWin = new MapRect(mp0.x, mp1.y, mp1.x - mp0.x, mp0.y - mp1.y);
    return this.mapRectInWin;
  }

  public void reset(DrawRect draw_rect)
  {
    MapRect mr = this.mapMain.calWholeMapRect();

    if (mr != null)
      this.mapCenter = mr.getCenter();
    else {
      this.mapCenter = new MapPoint(0L, 0L);
    }
    this.drawRect = draw_rect;
    this.drawCenter = draw_rect.getCenter();
    this.baseRule = calFitBaseRule(mr, this.drawRect);

    recalMapRectInDrawWin();
  }

  public void reset()
  {
    MapRect mr = this.mapMain.calWholeMapRect();
    if (mr != null)
    {
      this.mapCenter = mr.getCenter();

      this.drawCenter = this.drawRect.getCenter();
      this.baseRule = calFitBaseRule(mr, this.drawRect);
    }

    recalMapRectInDrawWin();
  }

  public long getMaxBaseRule()
  {
    return MAX_BASE_RULE;
  }

  public long getBaseRule()
  {
    return this.baseRule;
  }

  public void setBaseRule(long br)
  {
    if (br <= 0L)
      br = 1L;
    else if (br > MAX_BASE_RULE) {
      br = MAX_BASE_RULE;
    }
    this.baseRule = br;

    recalMapRectInDrawWin();
  }

  public void zoomDown(DrawPoint new_draw_center)
  {
    if (this.baseRule >= MAX_BASE_RULE) {
      return;
    }
    if (new_draw_center != null)
    {
      setMapCenterByDrawXY(new_draw_center);
    }

    this.baseRule *= 2L;

    recalMapRectInDrawWin();
  }

  public void zoomUp(DrawPoint new_draw_center)
  {
    if (this.baseRule <= 1L)
    {
      return;
    }

    if (new_draw_center != null)
    {
      setMapCenterByDrawXY(new_draw_center);
    }

    this.baseRule /= 2L;

    recalMapRectInDrawWin();
  }

  public MapPoint getMapCenter()
  {
    return this.mapCenter;
  }

  public void setMapCenter(MapPoint cp)
  {
    this.mapCenter = cp;

    recalMapRectInDrawWin();
  }

  public void setMapCenterByDrawXYMove(int x_chg, int y_chg)
  {
    int draw_x = this.drawCenter.x + x_chg;
    int draw_y = this.drawCenter.y + y_chg;

    this.mapCenter = calMapPoint(new DrawPoint(draw_x, draw_y));

    recalMapRectInDrawWin();
  }

  public void setMapCenterByDrawXY(DrawPoint new_center_dp)
  {
    this.mapCenter = calMapPoint(new_center_dp);

    recalMapRectInDrawWin();
  }

  public MapRect getMapRectInDrawWin()
  {
    return this.mapRectInWin;
  }

  public DrawRect getDrawRect()
  {
    return this.drawRect;
  }

  public void setDrawRect(DrawRect dr)
  {
    this.drawRect = dr;
    this.drawCenter = this.drawRect.getCenter();

    recalMapRectInDrawWin();
  }

  public DrawPoint getDrawCenter()
  {
    return this.drawCenter;
  }

  public MapPoint calMapPoint(DrawPoint dp)
  {
    return calMapPoint(dp.x, dp.y);
  }

  public MapPoint calMapPoint(int draw_x, int draw_y)
  {
    long x_chg = (draw_x - this.drawCenter.x) * this.baseRule;
    long y_chg = (this.drawCenter.y - draw_y) * this.baseRule;

    long mapx = x_chg + this.mapCenter.x;
    long mapy = y_chg + this.mapCenter.y;

    return new MapPoint(mapx, mapy);
  }

  public MapRect calMapRect(DrawRect dr)
  {
    MapPoint mp0 = calMapPoint(dr.left, dr.top + dr.height);
    MapPoint mp1 = calMapPoint(dr.left + dr.width, dr.top);

    return new MapRect(mp0.x, mp0.y, mp1.x - mp0.x, mp1.y - mp0.y);
  }

  public DrawPoint calDrawPoint(long map_x, long map_y)
  {
    long x_chg = (map_x - this.mapCenter.x) / this.baseRule;
    long y_chg = (map_y - this.mapCenter.y) / this.baseRule;

    int draw_x = (int)(x_chg + this.drawCenter.x);
    int draw_y = (int)(this.drawCenter.y - y_chg);

    return new DrawPoint(draw_x, draw_y);
  }

  public DrawPoint calDrawPoint(MapPoint mp)
  {
    return calDrawPoint(mp.x, mp.y);
  }
}
