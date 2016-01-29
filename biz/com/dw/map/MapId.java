package com.dw.map;

import com.dw.system.Convert;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import java.awt.Color;
import java.awt.Graphics;
import java.util.UUID;

public abstract class MapId
  implements IXmlDataable
{
  protected String id = null;

  protected XmlData extInfo = null;

  public MapId()
  {
    this.id = UUID.randomUUID().toString().toUpperCase();
  }

  public MapId(String id)
  {
    this.id = id;
    if (Convert.isNullOrEmpty(id))
      throw new IllegalArgumentException("id cannot be null or empty");
  }

  public String getId()
  {
    return this.id;
  }

  public boolean equals(MapId o)
  {
    return this.id.equals(o.id);
  }

  public boolean equals(Object obj)
  {
    if (!(obj instanceof MapId))
      return false;
    MapId omid = (MapId)obj;

    return this.id.equals(omid.id);
  }

  public int hashCode()
  {
    return this.id.hashCode();
  }

  public XmlData getExtInfo()
  {
    return this.extInfo;
  }

  public void setExtInfo(XmlData xd)
  {
    this.extInfo = xd;
  }

  public abstract boolean canSelectedByDrawPos(Graphics paramGraphics, MapWindow paramMapWindow, int paramInt1, int paramInt2);

  public abstract void render(Graphics paramGraphics, MapWindow paramMapWindow, Color paramColor);

  public abstract MapRect getBounds();

  public abstract XmlData toXmlData();

  public abstract void fromXmlData(XmlData paramXmlData);
}
