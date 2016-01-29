package com.dw.portal;

import com.dw.system.AppWebConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import com.dw.web_ui.WebUtil;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@XORMClass(table_name="portal_page")
public class Page
  implements Comparable<Page>
{

  @XORMProperty(name="PageId", has_col=true, is_pk=true, is_auto=true, order_num=1)
  long pageId = -1L;

  @XORMProperty(name="PageDomain", has_col=true, has_idx=true, max_len=50, order_num=5)
  String pageDomain = null;

  @XORMProperty(name="PageName", has_col=true, has_idx=true, max_len=50, order_num=5)
  String pageName = null;

  @XORMProperty(name="PageTitle", has_col=true, has_idx=true, max_len=100, order_num=7)
  String pageTitle = null;

  @XORMProperty(name="LayoutPath", has_col=true, max_len=60, order_num=10)
  String layoutPath = null;

  @XORMProperty(name="PageState", has_col=true, order_num=20)
  int pageState = 0;

  @XORMProperty(name="UsingTempId", has_col=true, default_str_val="-1", order_num=30)
  long usingTempId = -1L;

  HashMap<String, ArrayList<EditBlock>> contN2EditBK = null;

  transient HashMap<String, List<PageBlock>> cont2blocks = null;

  transient String[] _pagedoms = null;

  private transient Page[] _subpages = null;

  @XORMProperty(name="PageEditCont", has_col=true, update_as_single=true, order_num=90)
  private byte[] get_PageEditCont()
  {
    if ((this.contN2EditBK == null) || (this.contN2EditBK.size() <= 0)) {
      return null;
    }
    XmlData xd = new XmlData();
    Iterator localIterator2;
    for (Iterator localIterator1 = this.contN2EditBK.entrySet().iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      Map.Entry n2ebs = (Map.Entry)localIterator1.next();

      String n = (String)n2ebs.getKey();
      ArrayList ebs = (ArrayList)n2ebs.getValue();
      List xds = xd.getOrCreateSubDataArray(n);
      localIterator2 = ebs.iterator(); continue; EditBlock eb = (EditBlock)localIterator2.next();

      xds.add(eb.toXmlData());
    }

    return xd.toBytesWithUTF8();
  }

  private void set_PageEditCont(byte[] bs) throws Exception {
    XmlData xd = XmlData.parseFromByteArrayUTF8(bs);
    HashMap n2eb = new HashMap();
    Iterator localIterator2;
    label158: for (Iterator localIterator1 = xd.getSubDataArrayNames().iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      String sdn = (String)localIterator1.next();

      List tmpxds = xd.getSubDataArray(sdn);
      if ((tmpxds == null) || (tmpxds.size() <= 0)) {
        break label158;
      }
      localIterator2 = tmpxds.iterator(); continue; XmlData tmpxd = (XmlData)localIterator2.next();

      EditBlock eb = new EditBlock(sdn);
      eb.fromXmlData(tmpxd);

      ArrayList ebs = (ArrayList)n2eb.get(sdn);
      if (ebs == null)
      {
        ebs = new ArrayList();
        n2eb.put(sdn, ebs);
      }

      ebs.add(eb);
    }

    this.contN2EditBK = n2eb;
  }

  public Page()
  {
  }

  Page(String domain, String layout, String name, String title)
  {
    this.pageDomain = domain;
    this.layoutPath = layout;
    this.pageName = name;
    this.pageTitle = title;
    this.usingTempId = -1L;
  }

  Page(long usingtempid, String name, String title)
  {
    this.usingTempId = usingtempid;
    this.pageName = name;
    this.pageTitle = title;
  }

  public boolean isTemplate()
  {
    return this.usingTempId <= 0L;
  }

  public long getUsingTempId()
  {
    return this.usingTempId;
  }

  public void setUsingTempId(long utid)
  {
    this.usingTempId = utid;
  }

  public String getLayoutPath()
  {
    return this.layoutPath;
  }

  public void setLayoutPath(String layoutPath)
  {
    this.layoutPath = layoutPath;
  }

  public Layout getPageLayout() throws Exception
  {
    if (isTemplate()) {
      return PortalManager.getInstance().getLayoutByPath(this.layoutPath);
    }
    return PortalManager.getInstance().getLayoutByPath(getUsingTemp().layoutPath);
  }

  public long getPageId()
  {
    return this.pageId;
  }

  public String[] getPageDomainArray()
  {
    if (this._pagedoms != null) {
      return this._pagedoms;
    }
    if (this.pageDomain == null)
    {
      this._pagedoms = new String[0];
      return this._pagedoms;
    }

    StringTokenizer st = new StringTokenizer(this.pageDomain, ",|;，；");
    ArrayList ss = new ArrayList();
    while (st.hasMoreTokens())
    {
      ss.add(st.nextToken());
    }

    String[] rs = new String[ss.size()];
    ss.toArray(rs);
    this._pagedoms = rs;
    return this._pagedoms;
  }

  public String getPageDomain()
  {
    return this.pageDomain;
  }

  public void setPageDomain(String pd)
  {
    this.pageDomain = pd;
  }

  public String getPageName()
  {
    return this.pageName;
  }

  public void setPageName(String pageName)
  {
    this.pageName = pageName;
  }

  public String getPageTitle()
  {
    return this.pageTitle;
  }

  public HashMap<String, ArrayList<EditBlock>> getPageEditCont()
    throws Exception
  {
    return this.contN2EditBK;
  }

  public EditBlock getFirstPageEditCont(String container)
  {
    if (this.contN2EditBK == null) {
      return null;
    }
    ArrayList ebs = (ArrayList)this.contN2EditBK.get(container);
    if ((ebs == null) || (ebs.size() <= 0)) {
      return null;
    }
    return (EditBlock)ebs.get(0);
  }

  public void setPageEditCont(String contname, boolean bhtml, String cont)
    throws Exception
  {
    Layout lo = getPageLayout();

    Layout.Container cc = lo.getContainerByName(contname);
    if (cc == null) {
      return;
    }
    if (!cc.isEditable()) {
      return;
    }
    EditBlock eb = new EditBlock(contname);
    eb.bHtml = bhtml;
    eb.cont = cont;

    ArrayList ebs = new ArrayList();
    ebs.add(eb);

    if (this.contN2EditBK == null)
      this.contN2EditBK = new HashMap();
    this.contN2EditBK.put(contname, ebs);
  }

  private HashMap<String, List<PageBlock>> getPageBlocks() throws Exception
  {
    if (this.cont2blocks != null) {
      return this.cont2blocks;
    }
    synchronized (this)
    {
      if (this.cont2blocks != null) {
        return this.cont2blocks;
      }
      List blocks = GDB.getInstance()
        .listXORMAsObjListByColOperValue(PageBlock.class, 
        new String[] { "PageId" }, 
        new String[] { "=" }, 
        new Object[] { Long.valueOf(this.pageId) }, 
        null, 0, -1, null);

      HashMap c2ll = new HashMap();
      for (PageBlock pb : blocks)
      {
        String cn = pb.getContainerName();
        List pbs = (List)c2ll.get(cn);
        if (pbs == null)
        {
          pbs = new ArrayList();
          c2ll.put(cn, pbs);
        }

        pbs.add(pb);
      }

      for (List pbs : c2ll.values())
      {
        Convert.sort(pbs);
      }

      this.cont2blocks = c2ll;
      return this.cont2blocks;
    }
  }

  public PageBlock getPageBlockById(long bid)
    throws Exception
  {
    HashMap c2bs = getPageBlocks();
    Iterator localIterator2;
    for (Iterator localIterator1 = c2bs.values().iterator(); localIterator1.hasNext(); 
      localIterator2.hasNext())
    {
      List pbs = (List)localIterator1.next();

      localIterator2 = pbs.iterator(); continue; PageBlock pb = (PageBlock)localIterator2.next();

      if (pb.getBlockId() == bid) {
        return pb;
      }
    }

    return null;
  }

  public List<PageBlock> getPageBlockByContainerName(String cont_name)
    throws Exception
  {
    HashMap n2pbs = getPageBlocks();
    return (List)n2pbs.get(cont_name);
  }

  public void render(HttpServletRequest req, HttpServletResponse resp, UserProfile up, Writer out, XmlData inputxd) throws Exception
  {
    Layout layout = getPageLayout();

    Page usingt = null;
    HashMap temp_c2pbs = null;
    if (!isTemplate())
    {
      usingt = getUsingTemp();
      temp_c2pbs = usingt.getPageBlocks();
    }

    ArrayList contList = layout.getContList();
    HashMap c2pbs = getPageBlocks();
    for (Iterator localIterator1 = contList.iterator(); localIterator1.hasNext(); ) { Object o = localIterator1.next();

      if ((o instanceof String))
      {
        out.write((String)o);
      }
      else if ((o instanceof Layout.Container))
      {
        Layout.Container c = (Layout.Container)o;
        String containerName = c.getName();
        if (c.isEditable())
        {
          boolean bed_outed = false;
          if (this.contN2EditBK != null)
          {
            ArrayList ebs = (ArrayList)this.contN2EditBK.get(containerName);
            if (ebs != null)
            {
              for (EditBlock eb : ebs)
              {
                out.write(eb.getOutTxt());
              }
              bed_outed = true;
            }
          }

          if ((!bed_outed) && (usingt != null) && (usingt.contN2EditBK != null))
          {
            ArrayList ebs = (ArrayList)usingt.contN2EditBK.get(containerName);
            if (ebs != null)
            {
              for (EditBlock eb : ebs)
              {
                out.write(eb.getOutTxt());
              }
            }
          }

          if ((up != null) && (up.isAdministrator()))
          {
            if (WebUtil.registerPageWebReference("/system/ui/dlg.js", req, out, resp))
            {
              out.write("<script>\r\nfunction edit_editable(pid,contn){");
              out.write("dlg.open('/system/portal_mgr/page_editable_edit_dlg.jsp?pid='+pid+'&container='+contn,");
              out.write("\tfunction(ret){if(ret==1){");
              out.write("\tdocument.location.href = document.location.href;}}");
              out.write("\t,'edit_editable','修改内容�?) ;}\r\n</script>");
            }
            out.write("<a href=\"javascript:edit_editable(" + getPageId() + ",'" + containerName + "')\"><img border=0 src=/WebRes?r=/com/dw/portal/edit.gif /></a>");
          }
        }

        if ("title".equals(containerName))
        {
          out.write(getPageTitle());
        }
        else if (!Convert.isNullOrEmpty(containerName))
        {
          Border b = c.getDefaultBorder();

          List pbs = null;
          if (temp_c2pbs != null)
          {
            pbs = (List)temp_c2pbs.get(containerName);
          }

          if ((pbs == null) || (pbs.size() <= 0)) {
            pbs = (List)c2pbs.get(containerName);
          }
          if (pbs != null)
          {
            for (PageBlock pb : pbs)
            {
              if (!pb.isIgnoreBlock())
              {
                int chres = AppWebConfig.checkViewAllow(true, pb.viewPath, req);
                if (chres > 0)
                {
                  HashMap b_ps = null;

                  if (!pb.isIgnoreBorder())
                  {
                    if (b != null) {
                      b_ps = pb.getBorderProps(req, up);
                    }
                    if (b_ps != null)
                    {
                      b.renderHead(out, b_ps);
                    }
                  }

                  String editurl = pb.render(req, resp, up, out, inputxd);

                  if ((!pb.isIgnoreBorder()) && (b_ps != null))
                  {
                    b.renderTail(out, b_ps);
                    String edid = (String)b_ps.get("edit_id");
                    if ((edid != null) && (editurl != null))
                    {
                      if (editurl.lastIndexOf('?') < 0)
                      {
                        editurl = editurl + "?_portal_pid=" + this.pageId + "&" + "_portal_bid" + "=" + pb.blockId;
                      }
                      else
                      {
                        editurl = editurl + "&_portal_pid=" + this.pageId + "&" + "_portal_bid" + "=" + pb.blockId;
                      }

                      out.write("<script>document.getElementById('" + edid + "').innerHTML='<a href=" + editurl + " target=_blank><img border=0 src=/WebRes?r=/com/dw/portal/edit.gif /></a>';</script>"); }  } 
                }
              }
            }
          }
        }
      } } out.flush();
  }

  public int compareTo(Page o)
  {
    return this.pageName.compareTo(o.pageName);
  }

  public Page getUsingTemp() throws Exception
  {
    if (isTemplate()) {
      return null;
    }
    return PageManager.getInstance().getTemplateById(this.usingTempId);
  }

  public Page[] getSubPages()
    throws Exception
  {
    if (!isTemplate()) {
      return null;
    }
    Page[] ps = (Page[])PageManager.getInstance().getTempId2Pages().get(Long.valueOf(this.pageId));
    if (ps == null)
    {
      this._subpages = new Page[0];
      return this._subpages;
    }

    this._subpages = ps;
    return this._subpages;
  }

  public class EditBlock
    implements IXmlDataable
  {
    transient String containerName = null;
    int orderNum = 0;
    String cont = null;
    boolean bHtml = false;

    transient String _outTxt = null;

    public EditBlock(String container)
    {
      this.containerName = container;
    }

    public String getContainerName()
    {
      return this.containerName;
    }

    public int getOrderNum()
    {
      return this.orderNum;
    }

    public String getContent()
    {
      return this.cont;
    }

    public boolean isHtml()
    {
      return this.bHtml;
    }

    public String getOutTxt() throws Exception
    {
      if (this._outTxt != null) {
        return this._outTxt;
      }
      if (this.cont == null)
      {
        Layout lo = Page.this.getPageLayout();
        if (lo == null)
        {
          this._outTxt = "";
          return this._outTxt;
        }
        Layout.Container cc = lo.getContainerByName(this.containerName);
        if (cc == null)
        {
          this._outTxt = "";
          return this._outTxt;
        }

        this._outTxt = cc.getEditableDefaultOutTxt();
        if (this._outTxt == null)
        {
          this._outTxt = "";
        }

        return this._outTxt;
      }

      if (this.bHtml)
        this._outTxt = this.cont;
      else {
        this._outTxt = Convert.plainToHtml(this.cont);
      }
      return this._outTxt;
    }

    public XmlData toXmlData()
    {
      XmlData xd = new XmlData();
      xd.setParamValue("order", Integer.valueOf(this.orderNum));
      if (this.cont != null)
        xd.setParamValue("cont", this.cont);
      xd.setParamValue("is_html", Boolean.valueOf(this.bHtml));
      return xd;
    }

    public void fromXmlData(XmlData xd) {
      this.orderNum = xd.getParamValueInt32("order", 0);
      this.cont = xd.getParamValueStr("cont", "");
      this.bHtml = xd.getParamValueBool("is_html", false).booleanValue();
    }
  }
}
