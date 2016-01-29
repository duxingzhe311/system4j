package com.dw.biz.api.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class XmlHelper
{
  static Hashtable s2charMap = new Hashtable();

  static String entitystr = "><&'\"\r\n\t";

  static
  {
    s2charMap.put("lt", new char[] { '<' });
    s2charMap.put("gt", new char[] { '>' });
    s2charMap.put("apos", new char[] { '\'' });
    s2charMap.put("amp", new char[] { '&' });
    s2charMap.put("quot", new char[] { '"' });
  }

  private static char getDecodeChar(String s)
  {
    char[] cs = (char[])s2charMap.get(s);
    if (cs != null) {
      return cs[0];
    }
    if (!s.startsWith("#")) {
      return 65535;
    }
    try
    {
      return (char)Integer.parseInt(s.substring(1));
    }
    catch (Exception ee) {
    }
    return 65535;
  }

  private static String getEncodeStr(char c)
  {
    switch (c)
    {
    case '<':
      return "lt";
    case '>':
      return "gt";
    case '&':
      return "amp";
    case '\'':
      return "apos";
    case '"':
      return "quot";
    case '\n':
      return "#10;";
    case '\r':
      return "#13;";
    }
    return "#" + c;
  }

  public static String xmlEncoding(String input)
  {
    return xmlEncoding(input, entitystr);
  }

  public static String xmlEncoding(String input, String delimiter)
  {
    if ((input == null) || (input.equals(""))) {
      return input;
    }
    delimiter = delimiter + '&';

    StringTokenizer tmpst = new StringTokenizer(input, delimiter, true);
    StringBuffer tmpsb = new StringBuffer(input.length() + 100);
    String tmps = null;
    while (tmpst.hasMoreTokens())
    {
      tmps = tmpst.nextToken();
      if ((tmps.length() == 1) && (delimiter.indexOf(tmps) >= 0));
      switch (tmps.charAt(0))
      {
      case '<':
        tmpsb.append("&lt;");
        break;
      case '>':
        tmpsb.append("&gt;");
        break;
      case '&':
        tmpsb.append("&amp;");
        break;
      case '\'':
        tmpsb.append("&apos;");
        break;
      case '"':
        tmpsb.append("&quot;");
        break;
      case '\n':
        tmpsb.append("&#10;");
        break;
      case '\r':
        tmpsb.append("&#13;");
        break;
      case '\t':
        tmpsb.append("&#9;");
      default:
        continue;

        tmpsb.append(tmps);
      }
    }

    return tmpsb.toString();
  }

  public static String xmlDecoding(String input)
  {
    return xmlDecoding(input, entitystr);
  }

  public static String xmlDecoding(String input, String delimiter)
  {
    if ((input == null) || (input.equals(""))) {
      return input;
    }
    delimiter = delimiter + '&';

    StringBuffer sb = new StringBuffer(input.length());
    int p = 0;
    while (true)
    {
      int a = input.indexOf('&', p);
      if (a < 0)
      {
        sb.append(input.substring(p));
        break;
      }

      sb.append(input.substring(p, a));
      p = a;
      int b = input.indexOf(';', p);
      if (b < 0)
      {
        sb.append(input.substring(p));
        break;
      }

      String s = input.substring(p + 1, b);
      char cc = getDecodeChar(s);

      if (delimiter.indexOf(cc) < 0)
      {
        sb.append('&').append(s).append(';');
        p = b + 1;
      }
      else
      {
        sb.append(cc);
        p = b + 1;
      }

    }

    return sb.toString();
  }

  public static String getElementFirstText(Element ele)
  {
    Node n = ele.getFirstChild();
    if ((n instanceof Text))
    {
      return ((Text)n).getNodeValue();
    }

    return null;
  }

  public static List<Element> getSubChildElementList(Element ele, String tagname)
  {
    if (ele == null)
    {
      return null;
    }

    List v = new ArrayList();

    boolean isall = false;
    if (tagname.equals("*"))
    {
      isall = true;
    }

    int p0 = tagname.indexOf(':');
    if (p0 >= 0) {
      tagname = tagname.substring(p0 + 1);
    }
    NodeList tmpnl = ele.getChildNodes();

    Node tmpn = null;

    for (int k = 0; k < tmpnl.getLength(); k++)
    {
      tmpn = tmpnl.item(k);

      if (tmpn.getNodeType() == 1)
      {
        Element eee = (Element)tmpn;
        String noden = eee.getNodeName();
        int p = noden.indexOf(':');
        if (p >= 0) {
          noden = noden.substring(p + 1);
        }
        if ((isall) || (tagname.equals(noden)))
        {
          v.add(eee);
        }
      }
    }
    return v;
  }

  public static Element[] getSubChildElement(Element ele, String tagname)
  {
    if (ele == null)
    {
      return null;
    }

    List v = new ArrayList();

    boolean isall = false;
    if (tagname.equals("*"))
    {
      isall = true;
    }

    int p0 = tagname.indexOf(':');
    if (p0 >= 0) {
      tagname = tagname.substring(p0 + 1);
    }
    NodeList tmpnl = ele.getChildNodes();

    Node tmpn = null;

    for (int k = 0; k < tmpnl.getLength(); k++)
    {
      tmpn = tmpnl.item(k);

      if (tmpn.getNodeType() == 1)
      {
        Element eee = (Element)tmpn;
        String noden = eee.getNodeName();
        int p = noden.indexOf(':');
        if (p >= 0) {
          noden = noden.substring(p + 1);
        }
        if ((isall) || (tagname.equals(noden)))
        {
          v.add(eee);
        }
      }
    }
    Element[] rets = new Element[v.size()];
    v.toArray(rets);
    return rets;
  }

  public static Properties getElementAttributes(Element ele)
  {
    Properties ht = new Properties();
    NamedNodeMap nnm = ele.getAttributes();
    int len = nnm.getLength();
    Node tmpn = null;
    for (int k = 0; k < len; k++)
    {
      tmpn = nnm.item(k);
      String tmps = tmpn.getNodeValue();
      if (tmps == null)
      {
        tmps = "";
      }
      ht.put(tmpn.getNodeName(), tmps);
    }
    return ht;
  }

  public static String arrayOfLongToStr(long[] ls)
  {
    if ((ls == null) || (ls.length == 0))
    {
      return "";
    }

    StringBuffer tmpsb = new StringBuffer();

    tmpsb.append('|');
    for (int i = 0; i < ls.length; i++)
    {
      tmpsb.append(ls[i])
        .append('|');
    }

    return tmpsb.toString();
  }

  public static long[] strToArrayOfLong(String str)
  {
    StringTokenizer tmpst = new StringTokenizer(str, "|", false);
    int len = tmpst.countTokens();
    long[] retl = new long[len];

    for (int i = 0; i < len; i++)
    {
      retl[i] = Long.parseLong(tmpst.nextToken());
    }

    return retl;
  }

  public static String[] strToArrayOfString(String str)
  {
    if (str == null)
    {
      return new String[0];
    }
    StringTokenizer tmpst = new StringTokenizer(str, "|", false);
    int len = tmpst.countTokens();
    String[] retl = new String[len];

    for (int i = 0; i < len; i++)
    {
      retl[i] = tmpst.nextToken();
    }

    return retl;
  }

  public static String arrayOfStringToStr(String[] ls)
  {
    if ((ls == null) || (ls.length == 0))
    {
      return "";
    }

    StringBuffer tmpsb = new StringBuffer();

    tmpsb.append('|');
    for (int i = 0; i < ls.length; i++)
    {
      tmpsb.append(ls[i])
        .append('|');
    }

    return tmpsb.toString();
  }

  public static String elementToString(Element ele)
  {
    try
    {
      StringWriter sw = new StringWriter();

      TransformerFactory tFactory = 
        TransformerFactory.newInstance();
      Transformer transformer = tFactory.newTransformer();

      DOMSource source = new DOMSource(ele);
      StreamResult result = new StreamResult(sw);
      transformer.setOutputProperty("indent", "yes");
      transformer.transform(source, result);

      return sw.toString();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  public static Element stringToElement(String str)
  {
    StringReader sr = new StringReader(str);
    try
    {
      DocumentBuilderFactory docBuilderFactory = 
        DocumentBuilderFactory.newInstance();
      docBuilderFactory.setNamespaceAware(false);
      docBuilderFactory.setValidating(false);
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

      InputSource is = new InputSource(sr);

      Document doc = docBuilder.parse(is);
      return doc.getDocumentElement();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }
}
