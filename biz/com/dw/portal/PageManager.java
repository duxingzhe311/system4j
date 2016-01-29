package com.dw.portal;

import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.GDBMulti;
import com.dw.system.xmldata.XmlData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class PageManager
{
  private static PageManager pMgr = null;

  private static Object locker = new Object();

  private List<Page> allTempPages = null;
  private List<Page> allTemps = null;
  private List<Page> allPages = null;

  private HashMap<Long, Page> id2temp = null;
  private HashMap<Long, Page> id2page = null;
  private HashMap<Long, Page[]> tempid2ps = null;

  private HashMap<String, HashMap<String, Page>> domain2n_p = null;
  private HashMap<String, HashMap<String, Page>> domain2n_t = null;
  private HashSet<String> domainSet = null;

  public static PageManager getInstance()
  {
    if (pMgr != null) {
      return pMgr;
    }
    synchronized (locker)
    {
      if (pMgr != null)
        return pMgr;
      pMgr = new PageManager();
      return pMgr;
    }
  }

  private void clearCache()
  {
    this.allTempPages = null;
    this.allTemps = null;
    this.allPages = null;
    this.id2temp = null;
    this.id2page = null;

    this.tempid2ps = null;
    this.domain2n_p = null;
    this.domainSet = null;
  }

  public List<Page> getAllTempPages()
    throws Exception
  {
    if (this.allTempPages != null) {
      return this.allTempPages;
    }

    this.allTempPages = GDB.getInstance().listXORMAsObjList(Page.class, "", "", 0, -1);

    return this.allTempPages;
  }

  public List<Page> getAllTemplates() throws Exception
  {
    if (this.allTemps != null) {
      return this.allTemps;
    }
    ArrayList temps = new ArrayList();
    for (Page p : getAllTempPages())
    {
      if (p.isTemplate()) {
        temps.add(p);
      }
    }
    this.allTemps = temps;
    return this.allTemps;
  }

  List<Page> getAllPages() throws Exception
  {
    if (this.allPages != null) {
      return this.allPages;
    }
    ArrayList pps = new ArrayList();
    for (Page p : getAllTempPages())
    {
      if (!p.isTemplate()) {
        pps.add(p);
      }
    }
    this.allPages = pps;
    return this.allPages;
  }

  private HashMap<Long, Page> getId2Temps() throws Exception
  {
    if (this.id2temp != null) {
      return this.id2temp;
    }
    HashMap mm = new HashMap();
    for (Page p : getAllTemplates())
    {
      mm.put(Long.valueOf(p.getPageId()), p);
    }

    this.id2temp = mm;
    return this.id2temp;
  }

  private HashMap<Long, Page> getId2Pages() throws Exception
  {
    if (this.id2page != null) {
      return this.id2page;
    }
    HashMap mm = new HashMap();
    for (Page p : getAllPages())
    {
      mm.put(Long.valueOf(p.getPageId()), p);
    }

    this.id2page = mm;
    return this.id2page;
  }

  HashMap<Long, Page[]> getTempId2Pages() throws Exception
  {
    if (this.tempid2ps != null) {
      return this.tempid2ps;
    }
    HashMap mm = new HashMap();
    long utid;
    for (Page p : getAllPages())
    {
      utid = p.getUsingTempId();
      Page tp = getTemplateById(utid);
      if (tp != null)
      {
        ArrayList ps = (ArrayList)mm.get(Long.valueOf(utid));
        if (ps == null)
        {
          ps = new ArrayList();
          mm.put(Long.valueOf(utid), ps);
        }

        ps.add(p);
      }
    }
    HashMap rr = new HashMap();
    for (Map.Entry id2ls : mm.entrySet())
    {
      ArrayList ps = (ArrayList)id2ls.getValue();
      Page[] ss = new Page[ps.size()];
      ps.toArray(ss);
      Arrays.sort(ss);
      rr.put((Long)id2ls.getKey(), ss);
    }
    this.tempid2ps = rr;
    return this.tempid2ps;
  }

  HashMap<String, HashMap<String, Page>> getDomain2PageMap()
    throws Exception
  {
    if (this.domain2n_p != null) {
      return this.domain2n_p;
    }
    HashMap mm = new HashMap();
    for (Page p : getAllPages())
    {
      Page tp = getTemplateById(p.getUsingTempId());
      if (tp != null)
      {
        String[] dns = tp.getPageDomainArray();
        if ((dns != null) && (dns.length != 0))
        {
          for (String dn : dns)
          {
            String ldn = dn.toLowerCase();
            HashMap dn2p = (HashMap)mm.get(ldn);
            if (dn2p == null)
            {
              dn2p = new HashMap();
              mm.put(ldn, dn2p);
            }

            dn2p.put(p.getPageName(), p);
          }
        }
      }
    }
    this.domain2n_p = mm;
    return this.domain2n_p;
  }

  HashMap<String, HashMap<String, Page>> getDomain2TempMap()
    throws Exception
  {
    if (this.domain2n_t != null) {
      return this.domain2n_t;
    }
    HashMap mm = new HashMap();
    int i;
    int j;
    label147: for (Iterator localIterator = getAllTemplates().iterator(); localIterator.hasNext(); 
      i < j)
    {
      Page tp = (Page)localIterator.next();

      String[] dns = tp.getPageDomainArray();
      if ((dns == null) || (dns.length <= 0)) {
        break label147;
      }
      String[] arrayOfString1 = dns; i = 0; j = arrayOfString1.length; continue; String dn = arrayOfString1[i];

      String ldn = dn.toLowerCase();
      HashMap dn2p = (HashMap)mm.get(ldn);
      if (dn2p == null)
      {
        dn2p = new HashMap();
        mm.put(ldn, dn2p);
      }

      dn2p.put(tp.getPageName(), tp);

      i++;
    }

    this.domain2n_t = mm;
    return this.domain2n_t;
  }

  private HashSet<String> getDomainSet()
  {
    if (this.domainSet != null) {
      return this.domainSet;
    }
    try
    {
      HashSet ds = new HashSet();
      List ps = getAllTemplates();
      int i;
      int j;
      label106: for (Iterator localIterator = ps.iterator(); localIterator.hasNext(); 
        i < j)
      {
        Page p = (Page)localIterator.next();

        String[] dns = p.getPageDomainArray();
        if ((dns == null) || (dns.length <= 0)) {
          break label106;
        }
        String[] arrayOfString1 = dns; i = 0; j = arrayOfString1.length; continue; String dn = arrayOfString1[i];
        ds.add(dn);

        i++;
      }

      this.domainSet = ds;
      return ds;
    }
    catch (Exception ee) {
    }
    return null;
  }

  public Page getTemplateById(long tid)
    throws Exception
  {
    HashMap id2p = getId2Temps();
    return (Page)id2p.get(Long.valueOf(tid));
  }

  public Page getSubPageById(long pid)
    throws Exception
  {
    return (Page)getId2Pages().get(Long.valueOf(pid));
  }

  public Page getTempOrPageById(long id) throws Exception
  {
    Page p = getTemplateById(id);
    if (p != null) {
      return p;
    }
    return getSubPageById(id);
  }

  public boolean isPortalDomain(String domain)
  {
    HashSet ds = getDomainSet();
    if (ds == null)
      return false;
    return ds.contains(domain);
  }

  public Page getTempByDomainName(String domain, String name)
    throws Exception
  {
    if ((name == null) || (name.equals(""))) {
      return null;
    }
    String dom = domain.toLowerCase();
    HashMap n2p = (HashMap)getDomain2TempMap().get(dom);
    if (n2p == null) {
      return null;
    }
    return (Page)n2p.get(name);
  }

  public Page getPageByDomainName(String domain, String name) throws Exception
  {
    if ((name == null) || (name.equals(""))) {
      return null;
    }
    String dom = domain.toLowerCase();
    HashMap n2p = (HashMap)getDomain2PageMap().get(dom);
    if (n2p == null) {
      return null;
    }
    return (Page)n2p.get(name);
  }

  public void addTemplate(String domain, String layout, String name, String title)
    throws Exception
  {
    Page p = new Page(domain, layout, name, title);
    addPage0(p);
  }

  public void updateTemplate(long tempid, Page tempp)
    throws Exception
  {
    if (Convert.isNullOrEmpty(tempp.getPageName()))
    {
      throw new Exception("page must has a name!");
    }

    Convert.checkVarName(tempp.getPageName());

    GDB.getInstance().updateXORMObjToDBWithHasColNames(Long.valueOf(tempid), tempp, new String[] { "PageDomain", "PageName", "PageTitle", "LayoutPath" });

    clearCache();
  }

  public void addPage(long usingtempid, String name, String title)
    throws Exception
  {
    Page p = new Page(usingtempid, name, title);
    addPage0(p);
  }

  private void addPage0(Page p) throws Exception
  {
    if (Convert.isNullOrEmpty(p.getPageName()))
    {
      throw new Exception("page must has a name!");
    }

    Convert.checkVarName(p.getPageName());

    GDB.getInstance().addXORMObjWithNewId(p);

    clearCache();
  }

  public void updatePage(long pid, Page p) throws Exception
  {
    if (Convert.isNullOrEmpty(p.getPageName()))
    {
      throw new Exception("page must has a name!");
    }

    Convert.checkVarName(p.getPageName());

    GDB.getInstance().updateXORMObjToDBWithHasColNames(Long.valueOf(pid), p, new String[] { "PageName", "PageTitle" });

    clearCache();
  }

  public void updatePageBlockBase(long bid, String title, String editurl, String editright, int seq)
    throws Exception
  {
    GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(bid), PageBlock.class, 
      new String[] { "Title", "EditUrl", "EditRight", "Sequence" }, new Object[] { title, editurl, editright, Integer.valueOf(seq) });

    clearCache();
  }

  public void updatePageBlockBase(long bid, PageBlock pb)
    throws Exception
  {
    GDB.getInstance().updateXORMObjToDBWithHasColNames(Long.valueOf(bid), pb, 
      new String[] { "Title", "EditUrl", "EditRight", "Sequence", "IgnoreBorder", "IgnoreBlock" });

    clearCache();
  }

  public void updatePageBlockInput(long bid, XmlData inputxd)
    throws Exception
  {
    byte[] vv = (byte[])null;
    if (inputxd != null)
      vv = inputxd.toBytesWithUTF8();
    GDB.getInstance().saveXORMSingleUpdateCol(bid, PageBlock.class, 
      "InputParam", vv);

    clearCache();
  }

  public void updateTempOrPageEditCont(long pid, String container, boolean bhtml, String cont)
    throws Exception
  {
    Page p = getTempOrPageById(pid);
    if (p == null) {
      return;
    }
    p.setPageEditCont(container, bhtml, cont);
    GDB.getInstance().updateXORMObjToDBWithHasColNames(Long.valueOf(pid), p, new String[] { "PageEditCont" });
    clearCache();
  }

  public void delPage(long pid) throws Exception
  {
    GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(pid), Page.class);

    clearCache();
  }

  public void addPageBlock(long pageid, String containername, String viewp)
    throws Exception
  {
    PageBlock pb = new PageBlock();

    pb.setContainerName(containername);
    pb.setPageId(pageid);
    pb.setViewPath(viewp);
    pb.setSequence(100);

    GDB.getInstance().addXORMObjWithNewId(pb);

    clearCache();
  }

  public void delPageBlock(long bid)
    throws Exception
  {
    GDB.getInstance().deleteXORMObjFromDB(Long.valueOf(bid), PageBlock.class);
    clearCache();
  }

  public void setPageBlockToPage(long pageid, HashMap<String, ArrayList<String>> cont2paths)
    throws Exception
  {
    try
    {
      GDBMulti gdbm = new GDBMulti();
      gdbm.addDeleteXORMObjByCondWithNoFile(PageBlock.class, "PageId=" + pageid);

      if (cont2paths == null)
        return;
      Iterator localIterator2;
      for (Iterator localIterator1 = cont2paths.keySet().iterator(); localIterator1.hasNext(); 
        localIterator2.hasNext())
      {
        String cn = (String)localIterator1.next();

        ArrayList ps = (ArrayList)cont2paths.get(cn);
        int i = 1;
        localIterator2 = ps.iterator(); continue; String tmps = (String)localIterator2.next();

        PageBlock pb = new PageBlock();
        pb.setContainerName(cn);
        pb.setPageId(pageid);
        pb.setViewPath(tmps);
        pb.setSequence(i);

        gdbm.addAddXORMObj(pb);
        i++;
      }

      GDB.getInstance().accessDBMulti(gdbm);
    }
    finally
    {
      clearCache(); } clearCache();
  }

  public boolean changePageBlockContainer(long blockid, String contn)
    throws Exception
  {
    if (Convert.isNullOrEmpty(contn)) {
      return false;
    }
    PageBlock pb = (PageBlock)GDB.getInstance().getXORMObjByPkId(PageBlock.class, Long.valueOf(blockid));
    if (pb == null) {
      return false;
    }
    if (contn.equals(pb.getContainerName())) {
      return true;
    }
    boolean ret = GDB.getInstance().updateXORMObjToDBWithHasColNameValues(Long.valueOf(blockid), PageBlock.class, 
      new String[] { "ContainerName" }, new Object[] { contn });

    if (ret) {
      clearCache();
    }
    return ret;
  }
}
