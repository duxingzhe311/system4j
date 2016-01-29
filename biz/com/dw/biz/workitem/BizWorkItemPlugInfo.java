package com.dw.biz.workitem;

import com.dw.mltag.util.XmlUtil;
import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BizWorkItemPlugInfo
{
  String appName = null;

  ArrayList<InfoItem> infoItems = null;

  public static BizWorkItemPlugInfo getBizWorkItemPlugInfoByFile(String appname, File f)
    throws Exception
  {
    DocumentBuilderFactory docBuilderFactory = null;
    DocumentBuilder docBuilder = null;

    docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setValidating(false);
    docBuilder = docBuilderFactory.newDocumentBuilder();

    Document doc = docBuilder.parse(f);

    Element ele = doc.getDocumentElement();

    Element[] eles = XmlUtil.getSubChildElement(ele, "workitem_plug");

    ArrayList iis = new ArrayList();
    if (eles != null)
    {
      for (Element tmpe : eles)
      {
        InfoItem ii = new InfoItem(appname, tmpe);
        iis.add(ii);
      }
    }

    return new BizWorkItemPlugInfo(appname, iis);
  }

  public BizWorkItemPlugInfo(String appn, ArrayList<InfoItem> iis)
  {
    this.appName = appn;
    this.infoItems = iis;
  }

  public String getAppName()
  {
    return this.appName;
  }

  public ArrayList<InfoItem> getInfoItems()
  {
    return this.infoItems;
  }

  public static class InfoItem
  {
    String plugName = null;

    String title = null;

    String createView = null;

    String finishView = null;

    String finishAction = null;

    public InfoItem()
    {
    }

    public InfoItem(String appn, Element ele) {
      this.plugName = ele.getAttribute("name");
      this.title = ele.getAttribute("title");
      this.createView = catPath(appn, ele.getAttribute("create_view"));
      this.finishView = catPath(appn, ele.getAttribute("finish_view"));
      this.finishAction = catPath(appn, ele.getAttribute("finish_action"));
    }

    private String catPath(String appn, String p)
    {
      if (p == null) {
        return "";
      }
      if (p.startsWith("/")) {
        return p;
      }
      return "/" + appn + "/" + p;
    }

    public InfoItem(String plugn, String title, String createv, String finishv, String finisha)
    {
      this.plugName = plugn;
      this.title = title;
      this.createView = createv;
      this.finishView = finishv;
      this.finishAction = finisha;
    }

    public String getPlugName()
    {
      return this.plugName;
    }

    public String getTitle()
    {
      return this.title;
    }

    public String getCreateView()
    {
      return this.createView;
    }

    public String getFinishView()
    {
      return this.finishView;
    }

    public String getFinishAction()
    {
      return this.finishAction;
    }
  }
}
