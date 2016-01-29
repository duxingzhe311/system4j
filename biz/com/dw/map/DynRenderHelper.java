package com.dw.map;

import com.dw.map.dyn_render.BlinkPoint;
import com.dw.map.dyn_render.CircleWavePoint;
import java.util.HashMap;

class DynRenderHelper
{
  static HashMap<String, IMapDynItemRender> name2dynr = new HashMap();

  static
  {
    setRender(new BlinkPoint());
    setRender(new CircleWavePoint());
  }

  private static void setRender(IMapDynItemRender dr)
  {
    name2dynr.put(dr.getDynName(), dr);
  }

  static IMapDynItemRender getRender(String dynn)
  {
    return (IMapDynItemRender)name2dynr.get(dynn);
  }
}
