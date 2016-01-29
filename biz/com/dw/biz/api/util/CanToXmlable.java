package com.dw.biz.api.util;

import java.util.Hashtable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract interface CanToXmlable
{
  public static final String FROM_XML_METHOD = "constructFromXml";
  public static final Class[] FROM_XML_PARAMSTYPE = { Element.class };

  public abstract String getTagName();

  public abstract Element toElement(Document paramDocument);

  public abstract Hashtable getSubXmlables();

  public abstract void setSubXmlables(String paramString, CanToXmlable[] paramArrayOfCanToXmlable)
    throws Exception;
}
