package com.dw.biz.api.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlCoder
{
  public static final String CLASS_ATTRN = "_xmlcoder_class_";
  public static final String CATNAME_ATTRN = "_xmlcoder_catname_";
  protected static DocumentBuilderFactory docBuildFactory = null;
  protected static DocumentBuilder docBuilder = null;

  static
  {
    try
    {
      docBuildFactory = DocumentBuilderFactory.newInstance();
      docBuilder = docBuildFactory.newDocumentBuilder();
    }
    catch (Exception eee)
    {
      eee.printStackTrace();
    }
  }

  public static Element encoderToXml(CanToXmlable ctx)
  {
    Document doc = docBuilder.newDocument();
    return encoderToXml(doc, ctx);
  }

  private static Element encoderToXml(Document doc, CanToXmlable ctx)
  {
    Element ele = ctx.toElement(doc);
    ele.setAttribute("_xmlcoder_class_", ctx.getClass().getName());

    Hashtable ht = ctx.getSubXmlables();
    if (ht != null)
    {
      CanToXmlable[] cs;
      int i;
      for (Enumeration en = ht.keys(); en.hasMoreElements(); 
        i < cs.length)
      {
        String catname = (String)en.nextElement();
        cs = (CanToXmlable[])ht.get(catname);
        i = 0; continue;

        Element cele = encoderToXml(doc, cs[i]);
        cele.setAttribute("_xmlcoder_catname_", catname);
        ele.appendChild(cele);

        i++;
      }

    }

    return ele;
  }

  public static CanToXmlable decoderFromXml(Element ele)
    throws Exception
  {
    String cn = ele.getAttribute("_xmlcoder_class_");
    if ((cn == null) && (cn.equals(""))) {
      throw new IllegalArgumentException("Illegal xml Element,it has not attribute:_xmlcoder_class_");
    }
    CanToXmlable ctx = null;

    Class c = Class.forName(cn);
    Constructor cons = c.getConstructor(
      new Class[] { Element.class });
    if (cons != null)
    {
      if (!Modifier.isPublic(cons.getModifiers()))
        cons.setAccessible(true);
      ctx = (CanToXmlable)cons.newInstance(new Object[] { ele });
    }
    else
    {
      Method m = c.getDeclaredMethod("constructFromXml", CanToXmlable.FROM_XML_PARAMSTYPE);
      if (m == null) {
        throw new RuntimeException("class has no Constructor with parameter:Element and has not static method:constructFromXml");
      }

      int modifier = m.getModifiers();
      if (!Modifier.isStatic(modifier))
        throw new RuntimeException("method:constructFromXml is not static");
      if (!Modifier.isPublic(modifier)) {
        m.setAccessible(true);
      }
      ctx = (CanToXmlable)m.invoke(null, new Object[] { ele });
    }

    List subeles = XmlHelper.getSubChildElementList(ele, "*");
    if (subeles != null)
    {
      Hashtable ht = new Hashtable();
      int size = subeles.size();
      for (int i = 0; i < size; i++)
      {
        String subcn = ((Element)subeles.get(i)).getAttribute("_xmlcoder_class_");
        if ((subcn != null) || (!subcn.equals("")))
        {
          String catname = ((Element)subeles.get(i)).getAttribute("_xmlcoder_catname_");
          if (catname == null)
            catname = "";
          Vector v = (Vector)ht.get(catname);
          if (v == null)
          {
            v = new Vector();
            ht.put(catname, v);
          }

          CanToXmlable subctx = decoderFromXml((Element)subeles.get(i));
          v.addElement(subctx);
        }
      }
      for (Enumeration en = ht.keys(); en.hasMoreElements(); )
      {
        String catname = (String)en.nextElement();
        Vector v = (Vector)ht.get(catname);
        if (v.size() > 0)
        {
          CanToXmlable[] subctxs = new CanToXmlable[v.size()];
          v.toArray(subctxs);
          ctx.setSubXmlables(catname, subctxs);
        }
      }
    }
    return ctx;
  }
}
