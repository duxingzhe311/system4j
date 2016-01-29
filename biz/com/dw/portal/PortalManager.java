package com.dw.portal;

import com.dw.mltag.util.XmlUtil;
import com.dw.system.AppWebConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.Element;

public class PortalManager
{
  private static PortalManager lMgr = null;
  private static Object locker = new Object();

  private HashMap<String, Block> path2Blocks = null;
  private HashMap<String, ArrayList<Block>> module2blocks = null;

  private HashMap<String, Layout> path2Layout = null;

  private HashMap<String, Border> uniquename2bd = new HashMap();

  private HashMap<String, Border> path2bd = new HashMap();

  public static PortalManager getInstance()
  {
    if (lMgr != null) {
      return lMgr;
    }
    synchronized (locker)
    {
      if (lMgr != null) {
        return lMgr;
      }
      lMgr = new PortalManager();
      return lMgr;
    }
  }

  private void init()
  {
    if (this.path2Layout != null) {
      return;
    }

    reloadLayout();
    reloadBlock();
  }

  public HashMap<String, Block> getPath2Blocks()
  {
    if (this.path2Blocks != null) {
      return this.path2Blocks;
    }

    reloadBlock();
    return this.path2Blocks;
  }

  public Block getBlockByPath(String p)
  {
    return (Block)getPath2Blocks().get(p);
  }

  public ArrayList<Block> getAllBlocks()
  {
    HashMap bks = getPath2Blocks();
    ArrayList rets = new ArrayList(bks.size());
    rets.addAll(bks.values());
    return rets;
  }

  public ArrayList<Block> getBlocksByModule(String modulen)
  {
    if (this.module2blocks != null) {
      return (ArrayList)this.module2blocks.get(modulen);
    }
    reloadBlock();
    return (ArrayList)this.module2blocks.get(modulen);
  }

  public void reloadBlock()
  {
    HashMap p2l = new HashMap();
    HashMap m2bs = new HashMap();

    for (AppWebConfig awc0 : AppWebConfig.getModuleWebConfigAll())
    {
      Element cnele = awc0.getConfElement("portal");
      if (cnele != null)
      {
        Element[] eles = XmlUtil.getSubChildElement(cnele, "blocks");
        if ((eles != null) && (eles.length > 0))
        {
          ArrayList mbs = new ArrayList();
          for (Element tmpe : eles)
          {
            Element[] leles = XmlUtil.getSubChildElement(tmpe, "view");
            if ((leles != null) && (leles.length > 0))
            {
              for (Element lele : leles)
              {
                String p = lele.getAttribute("path");
                if (!p.startsWith("/"))
                {
                  p = "/" + awc0.getModuleName() + "/" + p;
                }
                String t = lele.getAttribute("title");
                String d = lele.getAttribute("desc");

                Element[] tmpeles = XmlUtil.getSubChildElement(lele, "oper");
                ArrayList opers = null;
                if (tmpeles != null)
                {
                  opers = new ArrayList(tmpeles.length);
                  for (Element e0 : tmpeles)
                  {
                    Block.Oper o0 = Block.createOper(awc0.getModuleName(), e0);
                    if (o0 != null)
                    {
                      opers.add(o0);
                    }
                  }
                }
                Block lout = new Block(awc0, p, t, d, opers);

                p2l.put(p, lout);

                mbs.add(lout);
              }
            }
          }
          m2bs.put(awc0.getModuleName(), mbs);
        }
      }
    }
    this.path2Blocks = p2l;

    this.module2blocks = m2bs;
  }

  public void reloadLayout()
  {
    HashMap p2l = new HashMap();
    HashMap n2b = new HashMap();
    HashMap p2b = new HashMap();

    for (AppWebConfig awc0 : AppWebConfig.getModuleWebConfigAll())
    {
      Element cnele = awc0.getConfElement("portal");
      if (cnele != null)
      {
        Element[] eles = XmlUtil.getSubChildElement(cnele, "layouts");
        if ((eles != null) && (eles.length > 0))
        {
          for (Element tmpe : eles)
          {
            Element[] leles = XmlUtil.getSubChildElement(tmpe, "layout");
            if ((leles != null) && (leles.length > 0))
            {
              for (Element lele : leles)
              {
                String p = lele.getAttribute("path");
                if (!p.startsWith("/"))
                {
                  p = "/" + awc0.getModuleName() + "/" + p;
                }
                String t = lele.getAttribute("title");
                String d = lele.getAttribute("desc");
                String enc = lele.getAttribute("encoding");

                Layout lout = new Layout(awc0, p, t, d, enc);

                p2l.put(p, lout);
              }
            }
          }

          eles = XmlUtil.getSubChildElement(cnele, "borders");
          if ((eles != null) && (eles.length > 0))
          {
            for (Element tmpe : eles)
            {
              Element[] leles = XmlUtil.getSubChildElement(tmpe, "border");
              if ((leles != null) && (leles.length > 0))
              {
                for (Element lele : leles)
                {
                  String p = lele.getAttribute("path");
                  if (!p.startsWith("/"))
                  {
                    p = "/" + awc0.getModuleName() + "/" + p;
                  }
                  String n = lele.getAttribute("name");
                  String t = lele.getAttribute("title");
                  String d = lele.getAttribute("desc");
                  String enc = lele.getAttribute("encoding");

                  Border br = new Border(awc0, n, t, d, p, enc);

                  n2b.put(br.getUniqueName(), br);
                  p2b.put(p, br);
                }
              }
            }
          }
        }
      }
    }
    this.path2Layout = p2l;
    this.uniquename2bd = n2b;
    this.path2bd = p2b;
  }

  public Layout getLayoutByPath(String path)
  {
    init();

    return (Layout)this.path2Layout.get(path);
  }

  public List<Layout> getAllLayouts()
  {
    init();

    ArrayList rets = new ArrayList();

    rets.addAll(this.path2Layout.values());

    return rets;
  }

  public Border getBorderByUniqueName(String uniquen)
  {
    return (Border)this.uniquename2bd.get(uniquen);
  }

  public Border getBorderByPath(String abs_path)
  {
    return (Border)this.path2bd.get(abs_path);
  }

  public List<Border> getAllBorders()
  {
    init();

    ArrayList rets = new ArrayList();

    rets.addAll(this.uniquename2bd.values());

    return rets;
  }
}
