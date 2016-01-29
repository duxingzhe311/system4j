package com.dw.biz.ui;

import com.dw.biz.BizCondition;
import com.dw.biz.BizFlow;
import com.dw.biz.BizFlow.ActNode;
import com.dw.biz.BizFlow.AndXor;
import com.dw.biz.BizFlow.DataField;
import com.dw.biz.BizFlow.DataFieldContainer;
import com.dw.biz.BizFlow.DataFieldDataX;
import com.dw.biz.BizFlow.DataFieldXmlData;
import com.dw.biz.BizFlow.DataFieldXmlVal;
import com.dw.biz.BizFlow.EventType;
import com.dw.biz.BizFlow.FlowCtrlViewNode;
import com.dw.biz.BizFlow.ITitleable;
import com.dw.biz.BizFlow.IdPoint;
import com.dw.biz.BizFlow.Node;
import com.dw.biz.BizFlow.NodeAction;
import com.dw.biz.BizFlow.NodeEnd;
import com.dw.biz.BizFlow.NodePerformer;
import com.dw.biz.BizFlow.NodeRouter;
import com.dw.biz.BizFlow.NodeStart;
import com.dw.biz.BizFlow.NodeSubFlow;
import com.dw.biz.BizFlow.NodeView;
import com.dw.biz.BizFlow.Transition;
import com.dw.biz.BizParticipant;
import com.dw.biz.BizParticipant.ContType;
import com.dw.system.Convert;
import com.dw.system.graph.AbstractDrawNode;
import com.dw.system.graph.AbstractDrawNode.SelectStyle;
import com.dw.system.graph.Draggable;
import com.dw.system.graph.IDrawContainer;
import com.dw.system.graph.IconManager;
import com.dw.system.xmldata.XmlValDef;
import com.dw.user.right.RightRule;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import javax.swing.ImageIcon;

public class DrawNode
{
  static IconManager iconMgr0 = null;

  static IconManager getIconMgr()
  {
    if (iconMgr0 != null) {
      return iconMgr0;
    }
    iconMgr0 = new IconManager(DrawNode.class, "pic.prop");
    return iconMgr0;
  }

  public static ImageIcon getBizRouteNodeIcon()
  {
    return getIconMgr().getIcon("route_diagram");
  }

  public static ImageIcon getBizViewNodeIcon()
  {
    return getIconMgr().getIcon("bizViewIcon");
  }

  public static ImageIcon getBizInnerViewNodeIcon()
  {
    return getIconMgr().getIcon("bizInnerViewIcon");
  }

  public static ImageIcon getBizInnerActionNodeIcon()
  {
    return getIconMgr().getIcon("bizInnerActIcon");
  }

  public static ImageIcon getBizActionNodeIcon()
  {
    return getIconMgr().getIcon("bizActionIcon");
  }

  public static ImageIcon getBizSubFlowNodeIcon()
  {
    return getIconMgr().getIcon("bizFlowIcon");
  }

  public static ImageIcon getBizStartNodeIcon()
  {
    return getIconMgr().getIcon("bizStartNodeIcon");
  }

  public static ImageIcon getBizEndNodeIcon()
  {
    return getIconMgr().getIcon("bizEndNodeIcon");
  }

  public static ImageIcon getBizNodePerformerIcon()
  {
    return getIconMgr().getIcon("bizNodePerformerIcon");
  }

  private static Polygon getLinePolygon(Point fromp, Point top)
  {
    int h = Math.abs(fromp.y - top.y);
    int w = Math.abs(fromp.x - top.x);
    int[] xps = new int[4];
    int[] yps = new int[4];
    if (h > w)
    {
      xps[0] = (fromp.x - 2);
      yps[0] = fromp.y;
      xps[1] = (top.x - 2);
      yps[1] = top.y;
      xps[2] = (top.x + 2);
      yps[2] = top.y;
      xps[3] = (fromp.x + 2);
      yps[3] = fromp.y;
    }
    else
    {
      xps[0] = fromp.x;
      yps[0] = (fromp.y - 2);
      xps[1] = top.x;
      yps[1] = (top.y - 2);
      xps[2] = top.x;
      yps[2] = (top.y + 2);
      xps[3] = fromp.x;
      yps[3] = (fromp.y + 2);
    }
    return new Polygon(xps, yps, 4);
  }

  public static class DNBizFlow extends AbstractDrawNode
  {
    BizFlow bizFlow = null;

    public DNBizFlow(BizFlow bf)
    {
      this.bizFlow = bf;
      setSelectedStyle(AbstractDrawNode.SelectStyle.CannotSelectSelf);
    }

    protected void building()
    {
      if (this.bizFlow == null)
      {
        return;
      }

      BizFlow.DataFieldContainer dfc = this.bizFlow.getDataFieldContainer();
      DrawNode.DNDataFieldContainer dndfc = new DrawNode.DNDataFieldContainer(dfc);
      appendChildNode(dndfc);

      BizFlow.NodeStart startn = this.bizFlow.getStartNode();
      if (startn != null)
      {
        DrawNode.DNBizStartNode dnbsn = new DrawNode.DNBizStartNode(this.bizFlow, 
          startn);
        appendChildNode(dnbsn);
      }

      BizFlow.NodeEnd endn = this.bizFlow.getEndNode();
      if (endn != null)
      {
        DrawNode.DNBizEndNode dnbsn = new DrawNode.DNBizEndNode(this.bizFlow, 
          endn);
        appendChildNode(dnbsn);
      }

      BizFlow.Node[] ns = this.bizFlow.getAllNodes();
      DrawNode.DNFlowCtrlViewNode dnBSF;
      if (ns != null)
      {
        for (BizFlow.Node n : ns)
        {
          if ((n instanceof BizFlow.NodeView))
          {
            DrawNode.DNBizView dnBV = new DrawNode.DNBizView(this.bizFlow, 
              (BizFlow.NodeView)n);
            appendChildNode(dnBV);
          }
          else if ((n instanceof BizFlow.NodeAction))
          {
            DrawNode.DNBizAction dnBA = new DrawNode.DNBizAction(this.bizFlow, 
              (BizFlow.NodeAction)n);
            appendChildNode(dnBA);
          }
          else if ((n instanceof BizFlow.NodeRouter))
          {
            DrawNode.DNBizRouter dnBR = new DrawNode.DNBizRouter(this.bizFlow, 
              (BizFlow.NodeRouter)n);
            appendChildNode(dnBR);
          }
          else if ((n instanceof BizFlow.NodeSubFlow))
          {
            DrawNode.DNBizSubFlow dnBSF = new DrawNode.DNBizSubFlow(this.bizFlow, 
              (BizFlow.NodeSubFlow)n);
            appendChildNode(dnBSF);
          }
          else if ((n instanceof BizFlow.NodePerformer))
          {
            DrawNode.DNBizNodePerformer dnBR = new DrawNode.DNBizNodePerformer(
              this.bizFlow, (BizFlow.NodePerformer)n);
            appendChildNode(dnBR);
          }
          else if ((n instanceof BizFlow.FlowCtrlViewNode))
          {
            dnBSF = new DrawNode.DNFlowCtrlViewNode(
              this.bizFlow, (BizFlow.FlowCtrlViewNode)n);
            appendChildNode(dnBSF);
          }
        }

      }

      BizFlow.Transition[] paths = this.bizFlow.getAllTransitions();
      if (paths != null)
      {
        for (BizFlow.Transition np : paths)
        {
          try
          {
            DrawNode.DNTransition ti = new DrawNode.DNTransition(this.bizFlow, np);
            appendChildNode(ti);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      }
    }

    public Object getUserObject()
    {
      return this.bizFlow;
    }

    public BizFlow getBizFlow()
    {
      return this.bizFlow;
    }

    public Polygon[] getDrawingPolygons(Graphics g)
    {
      return null;
    }

    public void drawingNormal(Graphics g)
    {
    }

    public void drawingSelected(Graphics g)
    {
    }
  }

  public static class DNDataFieldContainer extends AbstractDrawNode
    implements Draggable
  {
    BizFlow.DataFieldContainer dfContainer = null;

    public DNDataFieldContainer(BizFlow.DataFieldContainer dfc)
    {
      this.dfContainer = dfc;

      building();
    }

    protected void building()
    {
      BizFlow.DataField[] dfs = this.dfContainer.getAllDataFields();
      for (int i = 0; i < dfs.length; i++)
      {
        BizFlow.DataField df = dfs[i];
        if ((df instanceof BizFlow.DataFieldXmlVal))
        {
          BizFlow.DataFieldXmlVal dfxd = (BizFlow.DataFieldXmlVal)df;
          DrawNode.DNDataFieldXmlVal dndfxd = new DrawNode.DNDataFieldXmlVal(this, i, 
            dfxd);
          appendChildNode(dndfxd);
        }
        else if ((df instanceof BizFlow.DataFieldXmlData))
        {
          BizFlow.DataFieldXmlData dfxd = (BizFlow.DataFieldXmlData)df;
          DrawNode.DNDataFieldXmlData dndfxd = new DrawNode.DNDataFieldXmlData(this, i, 
            dfxd);
          appendChildNode(dndfxd);
        }
        else if ((df instanceof BizFlow.DataFieldDataX))
        {
          BizFlow.DataFieldDataX dfxd = (BizFlow.DataFieldDataX)df;
          DrawNode.DNDataFieldDataX dndfxd = new DrawNode.DNDataFieldDataX(this, i, 
            dfxd);
          appendChildNode(dndfxd);
        }
      }
    }

    Rectangle getDrawRect(Graphics g)
    {
      FontMetrics fm = g.getFontMetrics();
      int width = fm.stringWidth("Data Fields");
      int height = fm.getHeight();

      AbstractDrawNode[] cns = getAllChildren();
      for (int i = 0; i < cns.length; i++)
      {
        DrawNode.DNDataField dndf = (DrawNode.DNDataField)cns[i];
        int w = fm.stringWidth(dndf.getDrawString());
        if (w > width)
        {
          width = w;
        }
      }

      height += (height + 2) * cns.length;
      Point p = getPos();
      return new Rectangle(p.x, p.y, width + 20, height);
    }

    Rectangle getDataFieldDrawRect(Graphics g, int drawidx)
    {
      Rectangle r = getDrawRect(g);
      FontMetrics fm = g.getFontMetrics();
      int height = fm.getHeight();
      return new Rectangle(r.x + 2, r.y + height * (drawidx + 1), 
        r.width - 4, height + 1);
    }

    protected void drawingNormal(Graphics g)
    {
      Color oldc = g.getColor();
      g.setColor(Color.white);
      Rectangle r = getDrawRect(g);
      g.fillRect(r.x, r.y, r.width, r.height);

      g.setColor(Color.blue);
      FontMetrics fm = g.getFontMetrics();
      int height = fm.getHeight();

      String name = "Data Fields";

      g.drawString(name, r.x + 1, r.y + height + 1);

      g.drawRect(r.x, r.y, r.width, r.height);

      g.setColor(oldc);
    }

    protected void drawingSelected(Graphics g)
    {
      Color oldc = g.getColor();
      g.setColor(Color.white);
      Rectangle r = getDrawRect(g);
      g.fillRect(r.x, r.y, r.width, r.height);

      g.setColor(Color.blue);
      FontMetrics fm = g.getFontMetrics();
      int height = fm.getHeight();

      String name = "Data Fields";

      g.drawString(name, r.x + 1, r.y + height + 1);

      g.setColor(Color.red);
      g.drawRect(r.x, r.y, r.width, r.height);

      g.setColor(oldc);
    }

    public Polygon[] getDrawingPolygons(Graphics g)
    {
      Rectangle rect = getDrawRect(g);

      int[] xps = new int[4];
      int[] yps = new int[4];

      xps[0] = rect.x;
      yps[0] = rect.y;
      xps[1] = (rect.x + rect.width);
      yps[1] = rect.y;
      xps[2] = (rect.x + rect.width);
      yps[2] = (rect.y + rect.height);
      xps[3] = rect.x;
      yps[3] = (rect.y + rect.height);
      return new Polygon[] { new Polygon(xps, yps, 4) };
    }

    public Object getUserObject()
    {
      return this.dfContainer;
    }

    public Point getPos()
    {
      return this.dfContainer.getPos();
    }

    public void setPos(int x, int y)
    {
      this.dfContainer.setPos(x, y);
    }

    public void drawDragged(int movex, int movey, Graphics g)
    {
      Color oldc = g.getColor();
      g.setColor(Color.white);
      Rectangle r = getDrawRect(g);
      g.fillRect(r.x + movex, r.y + movey, r.width, r.height);

      g.setColor(Color.blue);
      FontMetrics fm = g.getFontMetrics();
      int height = fm.getHeight();

      String name = "Data Fields";

      g.drawString(name, r.x + 1 + movex, r.y + height + 1 + movey);

      g.drawRect(r.x + movex, r.y + movey, r.width, r.height);

      g.setColor(oldc);
    }
  }

  public static abstract class DNDataField extends AbstractDrawNode
  {
    private DrawNode.DNDataFieldContainer dnDFC = null;

    private int drawIdx = -1;

    private BizFlow.DataField dataField = null;

    public DNDataField(DrawNode.DNDataFieldContainer dfc, int drawidx, BizFlow.DataField df)
    {
      this.dnDFC = dfc;
      this.drawIdx = drawidx;
      this.dataField = df;
    }

    protected void building()
    {
    }

    public BizFlow.DataField getDataField()
    {
      return this.dataField;
    }

    public abstract String getDrawString();

    public DrawNode.DNDataFieldContainer getDNDataFieldContainer()
    {
      return this.dnDFC;
    }

    protected void drawingNormal(Graphics g)
    {
      DrawNode.DNDataFieldContainer dfc = getDNDataFieldContainer();
      Rectangle drawRect = dfc.getDataFieldDrawRect(g, this.drawIdx);
      String str = getDrawString();
      Color oldc = g.getColor();
      g.setColor(Color.white);
      g.fillRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
      g.setColor(Color.blue);
      g.drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
      g.setColor(Color.blue);

      g.drawString(str, drawRect.x + 1, drawRect.y + drawRect.height - 1);
      g.setColor(oldc);
    }

    protected void drawingSelected(Graphics g)
    {
      DrawNode.DNDataFieldContainer dfc = getDNDataFieldContainer();
      Rectangle drawRect = dfc.getDataFieldDrawRect(g, this.drawIdx);
      String str = getDrawString();
      Color oldc = g.getColor();
      g.setColor(Color.white);
      g.fillRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
      g.setColor(Color.red);
      g.drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
      g.setColor(Color.red);

      g.drawString(str, drawRect.x + 1, drawRect.y + drawRect.height - 1);
      g.setColor(oldc);
    }

    public Polygon[] getDrawingPolygons(Graphics g)
    {
      DrawNode.DNDataFieldContainer dfc = getDNDataFieldContainer();
      Rectangle rect = dfc.getDataFieldDrawRect(g, this.drawIdx);

      int[] xps = new int[4];
      int[] yps = new int[4];

      xps[0] = rect.x;
      yps[0] = rect.y;
      xps[1] = (rect.x + rect.width);
      yps[1] = rect.y;
      xps[2] = (rect.x + rect.width);
      yps[2] = (rect.y + rect.height);
      xps[3] = rect.x;
      yps[3] = (rect.y + rect.height);
      return new Polygon[] { new Polygon(xps, yps, 4) };
    }

    public Object getUserObject()
    {
      return null;
    }
  }

  public static class DNDataFieldXmlVal extends DrawNode.DNDataField
  {
    public DNDataFieldXmlVal(DrawNode.DNDataFieldContainer dfc, int drawidx, BizFlow.DataFieldXmlVal xddf)
    {
      super(drawidx, xddf);
    }

    public String getDrawString()
    {
      BizFlow.DataFieldXmlVal xv = getDataFieldXmlVal();
      if (xv.isHasIdx()) {
        return "[#]" + getDataField().getName() + ":" + 
          getDataFieldXmlVal().getXmlValDef().getValType();
      }
      return "[ ]" + getDataField().getName() + ":" + 
        getDataFieldXmlVal().getXmlValDef().getValType();
    }

    public BizFlow.DataFieldXmlVal getDataFieldXmlVal()
    {
      return (BizFlow.DataFieldXmlVal)getDataField();
    }
  }

  public static class DNDataFieldXmlData extends DrawNode.DNDataField
  {
    public DNDataFieldXmlData(DrawNode.DNDataFieldContainer dfc, int drawidx, BizFlow.DataFieldXmlData xddf)
    {
      super(drawidx, xddf);
    }

    public String getDrawString()
    {
      return getDataField().getName() + ":XmlData";
    }

    public BizFlow.DataFieldXmlData getDataFieldXmlData()
    {
      return (BizFlow.DataFieldXmlData)getDataField();
    }
  }

  public static class DNDataFieldDataX extends DrawNode.DNDataField
  {
    public DNDataFieldDataX(DrawNode.DNDataFieldContainer dfc, int drawidx, BizFlow.DataFieldDataX xddf)
    {
      super(drawidx, xddf);
    }

    public String getDrawString()
    {
      String tmps = "";
      BizFlow.DataFieldDataX dfdx = getDataFieldDataX();
      String bn = dfdx.getDataXBase();
      if (bn != null)
        tmps = tmps + bn;
      String cn = dfdx.getDataXClass();
      if (cn != null)
        tmps = tmps + "." + cn;
      return getDataField().getName() + ":DataX-" + tmps;
    }

    public BizFlow.DataFieldDataX getDataFieldDataX()
    {
      return (BizFlow.DataFieldDataX)getDataField();
    }
  }

  public static abstract class DNBizNode extends AbstractDrawNode
    implements Draggable, BizFlow.ITitleable
  {
    BizFlow bizFlow = null;

    BizFlow.Node node = null;

    public DNBizNode(BizFlow bf, BizFlow.Node n)
    {
      this.bizFlow = bf;
      this.node = n;
    }

    public BizFlow.Node getNode()
    {
      return this.node;
    }

    public String getTitle()
    {
      if (this.node == null) {
        return null;
      }
      return this.node.getTitle();
    }

    public void setTitle(String t)
    {
      if (this.node == null) {
        return;
      }
      this.node.setTitle(t);
    }

    protected void building()
    {
    }

    protected String getSelectTitleExt()
    {
      return "";
    }

    protected abstract ImageIcon getDrawImageIcon();

    protected void drawingNormal(Graphics g)
    {
      if (this.node == null)
      {
        return;
      }
      Color oc = g.getColor();
      Rectangle rect = this.node.getDrawingRect();

      g.setColor(Color.BLUE);

      ImageIcon ii = getDrawImageIcon();
      if (ii != null)
      {
        g
          .drawImage(ii.getImage(), rect.x, rect.y, rect.width, 
          rect.height, Color.lightGray, 
          getCurrentDrawComponent());
      }
      else
      {
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 2, 2);
      }

      try
      {
        String t = this.node.getTitle();
        if (Convert.isNullOrEmpty(t)) {
          t = this.node.getNodeName();
        }

        g.drawString(t, rect.x + 1, rect.y - 3);
      }
      catch (Exception e)
      {
        g.setColor(Color.RED);
        g.drawString("Error", rect.x + 1, rect.y - 1);
      }

      g.setColor(oc);
    }

    protected void drawingSelected(Graphics g)
    {
      if (this.node == null)
      {
        return;
      }
      Color oc = g.getColor();
      Rectangle rect = this.node.getDrawingRect();

      g.setColor(Color.RED);

      ImageIcon ii = getDrawImageIcon();
      if (ii != null)
      {
        g
          .drawImage(ii.getImage(), rect.x, rect.y, rect.width, 
          rect.height, Color.lightGray, 
          getCurrentDrawComponent());
      }

      g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 2, 2);
      try
      {
        g.drawString(this.node.getTitle() + getSelectTitleExt() + ":" + this.node.getNodeName(), 
          rect.x + 1, rect.y - 3);
      }
      catch (Exception e)
      {
        g.setColor(Color.RED);
        g.drawString("Error", rect.x + 1, rect.y - 1);
      }

      g.setColor(oc);
    }

    public Polygon[] getDrawingPolygons(Graphics g)
    {
      if (this.node == null)
      {
        return null;
      }
      Rectangle rect = this.node.getDrawingRect();

      int[] xps = new int[4];
      int[] yps = new int[4];

      xps[0] = rect.x;
      yps[0] = rect.y;
      xps[1] = (rect.x + rect.width);
      yps[1] = rect.y;
      xps[2] = (rect.x + rect.width);
      yps[2] = (rect.y + rect.height);
      xps[3] = rect.x;
      yps[3] = (rect.y + rect.height);
      return new Polygon[] { new Polygon(xps, yps, 4) };
    }

    public Object getUserObject()
    {
      return this.node;
    }

    public Point getPos()
    {
      return new Point(this.node.getX(), this.node.getY());
    }

    public void setPos(int x, int y)
    {
      this.node.setX(x);
      this.node.setY(y);
    }

    public void drawDragged(int movex, int movey, Graphics g)
    {
      if (this.node == null)
      {
        return;
      }
      Color oc = g.getColor();
      Rectangle rect = this.node.getDrawingRect();

      g.setColor(Color.BLUE);

      g.drawRoundRect(rect.x + movex, rect.y + movey, rect.width, 
        rect.height, 2, 2);
      try
      {
        g.drawString(this.node.getTitle() + ":" + this.node.getNodeName(), rect.x + 
          movex + 1, rect.y + movey - 1);
      }
      catch (Exception e)
      {
        g.setColor(Color.RED);
        g.drawString("Error", rect.x + 1, rect.y - 1);
      }

      g.setColor(oc);
    }
  }

  public static abstract class DNBizActNode extends DrawNode.DNBizNode
  {
    public DNBizActNode(BizFlow bf, BizFlow.ActNode n)
    {
      super(n);
    }

    public BizFlow.ActNode getActNode()
    {
      return (BizFlow.ActNode)super.getNode();
    }

    protected void building()
    {
    }

    public Point getInPoint()
    {
      return getActNode().getInPoint();
    }

    public Point getOutPoint()
    {
      return getActNode().getOutPoint();
    }

    protected void drawingNormal(Graphics g)
    {
      super.drawingNormal(g);
      Color oc = g.getColor();
      g.setColor(Color.BLUE);

      if (getActNode().getJoin() == BizFlow.AndXor.AND)
      {
        Point inp = getInPoint();
        g.drawString("&&", inp.x - 10, inp.y - 5);
      }

      if (getActNode().getSplit() == BizFlow.AndXor.AND)
      {
        Point outp = getOutPoint();
        g.drawString("&&", outp.x, outp.y - 5);
      }

      g.setColor(oc);
    }

    protected void drawingSelected(Graphics g)
    {
      super.drawingSelected(g);

      Color oc = g.getColor();
      g.setColor(Color.RED);

      if (getActNode().getJoin() == BizFlow.AndXor.AND)
      {
        Point inp = getInPoint();
        g.drawString("&&", inp.x - 10, inp.y - 5);
      }

      if (getActNode().getSplit() == BizFlow.AndXor.AND)
      {
        Point outp = getOutPoint();
        g.drawString("&&", outp.x, outp.y - 5);
      }

      g.setColor(oc);
    }
  }

  public static class DNBizRouter extends DrawNode.DNBizActNode
  {
    public DNBizRouter(BizFlow bf, BizFlow.NodeRouter nv)
    {
      super(nv);
    }

    public BizFlow.NodeRouter getNodeView()
    {
      return (BizFlow.NodeRouter)getNode();
    }

    protected ImageIcon getDrawImageIcon()
    {
      return DrawNode.getBizRouteNodeIcon();
    }
  }

  public static class DNBizNodePerformer extends DrawNode.DNBizNode
  {
    public DNBizNodePerformer(BizFlow bf, BizFlow.NodePerformer nv)
    {
      super(nv);
    }

    public BizFlow.NodePerformer getNodePerformer()
    {
      return (BizFlow.NodePerformer)getNode();
    }

    protected ImageIcon getDrawImageIcon()
    {
      return DrawNode.getBizNodePerformerIcon();
    }

    public void drawingNormal(Graphics g)
    {
      super.drawingNormal(g);

      IDrawContainer dc = getDrawContainer();
      AbstractDrawNode cdn = null;
      if (dc != null)
        cdn = dc.getCurSelectNode();
      if ((cdn != null) && ((cdn instanceof DrawNode.DNBizNode)))
      {
        BizFlow.NodePerformer np = getNodePerformer();
        BizFlow.Node n = ((DrawNode.DNBizNode)cdn).getNode();
        Point thisp = np.getCenterPoint();
        if (np.hasRelation(n))
        {
          Point tarp = n.getCenterPoint();
          g.drawLine(thisp.x, thisp.y, tarp.x, tarp.y);
        }

        if (np.hasManualAssign(n))
        {
          Rectangle r = getBounds(g);
          if (r != null)
          {
            Color oc = g.getColor();
            g.setColor(Color.red);
            g.drawOval(r.x - 5, r.y - 5, r.width + 5, r.height + 5);
            g.drawString("Manual Assignment", r.x + r.width + 2, r.y + r.height);
            g.setColor(oc);
          }
        }
      }
    }

    public void drawingSelected(Graphics g)
    {
      super.drawingSelected(g);

      BizFlow.NodePerformer np = getNodePerformer();

      IDrawContainer dc = getDrawContainer();
      AbstractDrawNode cdn = null;
      if (dc != null)
        cdn = dc.getCurSelectNode();
      if (cdn == this)
      {
        AbstractDrawNode[] cns = getParentNode().getAllChildren();
        if (cns != null)
        {
          Point thisp = np.getCenterPoint();
          for (AbstractDrawNode tmpdn : cns)
          {
            if ((tmpdn instanceof DrawNode.DNBizNode))
            {
              DrawNode.DNBizNode dnbn = (DrawNode.DNBizNode)tmpdn;
              BizFlow.Node n = dnbn.getNode();
              if (np.hasRelation(n))
              {
                Point tarp = n.getCenterPoint();
                g.drawLine(thisp.x, thisp.y, tarp.x, tarp.y);
              }
            }
          }

        }

      }

      String desc_str = null;
      BizParticipant tmpbp = np.getParticipant();
      if ((tmpbp != null) && (tmpbp.getContType() == BizParticipant.ContType.RightRule))
      {
        try
        {
          desc_str = tmpbp.getCont();
          if ((desc_str != null) && (!desc_str.equals("")))
          {
            RightRule rr = RightRule.parse(desc_str);
            desc_str = rr.ToDescString("en");
          }
        }
        catch (Exception localException)
        {
        }
      }

      if (desc_str != null)
      {
        Rectangle r = getBounds(g);

        FontMetrics fm = g.getFontMetrics();

        int height = fm.getHeight();
        try
        {
          BufferedReader sr = new BufferedReader(
            new StringReader(desc_str));
          String line = null;
          int x = r.x;
          int y = r.y + r.height;
          while ((line = sr.readLine()) != null)
          {
            y += height;
            g.drawString(line, x, y);
          }
        }
        catch (Exception localException2)
        {
        }
      }
    }
  }

  public static class DNBizStartNode extends DrawNode.DNBizActNode
  {
    public DNBizStartNode(BizFlow bf, BizFlow.NodeStart nv)
    {
      super(nv);
    }

    public BizFlow.NodeStart getNodeStart()
    {
      return (BizFlow.NodeStart)getNode();
    }

    protected ImageIcon getDrawImageIcon()
    {
      return DrawNode.getBizStartNodeIcon();
    }
  }

  public static class DNBizEndNode extends DrawNode.DNBizActNode
  {
    public DNBizEndNode(BizFlow bf, BizFlow.NodeEnd nv)
    {
      super(nv);
    }

    public BizFlow.NodeEnd getNodeEnd()
    {
      return (BizFlow.NodeEnd)getNode();
    }

    protected ImageIcon getDrawImageIcon()
    {
      return DrawNode.getBizEndNodeIcon();
    }
  }

  public static class DNBizView extends DrawNode.DNBizActNode
  {
    public DNBizView(BizFlow bf, BizFlow.NodeView nv)
    {
      super(nv);
    }

    public BizFlow.NodeView getNodeView()
    {
      return (BizFlow.NodeView)getNode();
    }

    protected String getSelectTitleExt()
    {
      BizFlow.NodeView nv = getNodeView();
      long ldms = nv.getLimitDurMS();
      if (ldms > 0L)
        return "(In" + ldms / 60000L + "min)";
      return "";
    }

    protected ImageIcon getDrawImageIcon()
    {
      BizFlow.NodeView nv = getNodeView();
      if (nv.isInnerView()) {
        return DrawNode.getBizInnerViewNodeIcon();
      }
      return DrawNode.getBizViewNodeIcon();
    }
  }

  public static class DNFlowCtrlViewNode extends DrawNode.DNBizNode
  {
    public DNFlowCtrlViewNode(BizFlow bf, BizFlow.FlowCtrlViewNode fcvn)
    {
      super(fcvn);
    }

    public BizFlow.FlowCtrlViewNode getFlowCtrlViewNode()
    {
      return (BizFlow.FlowCtrlViewNode)getNode();
    }

    protected ImageIcon getDrawImageIcon()
    {
      return DrawNode.getBizViewNodeIcon();
    }

    protected void drawingNormal(Graphics g)
    {
      super.drawingNormal(g);
      Color oc = g.getColor();
      Rectangle rect = this.node.getDrawingRect();
      g.setColor(Color.BLUE);

      g.drawOval(rect.x - 5, rect.y - 5, rect.width + 10, 
        rect.height + 10);

      g.setColor(oc);
    }

    protected void drawingSelected(Graphics g)
    {
      super.drawingSelected(g);
      Color oc = g.getColor();
      Rectangle rect = this.node.getDrawingRect();
      g.setColor(Color.RED);

      g.drawOval(rect.x - 5, rect.y - 5, rect.width + 10, 
        rect.height + 10);

      g.setColor(oc);
    }

    public void drawDragged(int movex, int movey, Graphics g)
    {
      super.drawDragged(movex, movey, g);
      Color oc = g.getColor();
      Rectangle rect = this.node.getDrawingRect();
      g.setColor(Color.BLUE);

      g.drawOval(rect.x - 5 + movex, rect.y - 5 + movey, rect.width + 10, 
        rect.height + 10);

      g.setColor(oc);
    }
  }

  public static class DNBizAction extends DrawNode.DNBizActNode
  {
    public DNBizAction(BizFlow bf, BizFlow.NodeAction na)
    {
      super(na);
    }

    public BizFlow.NodeAction getNodeAction()
    {
      return (BizFlow.NodeAction)getNode();
    }

    protected ImageIcon getDrawImageIcon()
    {
      return DrawNode.getBizActionNodeIcon();
    }

    protected void drawingNormal(Graphics g)
    {
      super.drawingNormal(g);

      BizFlow.NodeAction na = getNodeAction();
      BizFlow.EventType et = na.getOnEvent();
      if (et != null)
      {
        Color oc = g.getColor();
        g.setColor(Color.GREEN);
        String t = et.getTitleEn() + "->";
        Point inp = getInPoint();
        int strlen = g.getFontMetrics().stringWidth(t);
        g.drawString(t, inp.x - strlen - 5, inp.y);
        g.setColor(oc);
      }
    }

    protected void drawingSelected(Graphics g)
    {
      super.drawingSelected(g);

      BizFlow.NodeAction na = getNodeAction();
      BizFlow.EventType et = na.getOnEvent();
      if (et != null)
      {
        Color oc = g.getColor();
        g.setColor(Color.RED);
        String t = et.getTitleEn() + "->";
        Point inp = getInPoint();
        int strlen = g.getFontMetrics().stringWidth(t);
        g.drawString(t, inp.x - strlen - 5, inp.y);
        g.setColor(oc);
      }
    }
  }

  public static class DNBizSubFlow extends DrawNode.DNBizActNode
  {
    public DNBizSubFlow(BizFlow bf, BizFlow.NodeSubFlow na)
    {
      super(na);
    }

    public BizFlow.NodeSubFlow getNodeSubFlown()
    {
      return (BizFlow.NodeSubFlow)getNode();
    }

    protected ImageIcon getDrawImageIcon()
    {
      return DrawNode.getBizSubFlowNodeIcon();
    }
  }

  public static class DNTransition extends AbstractDrawNode
    implements Draggable, BizFlow.ITitleable
  {
    public static final int MAX_EXTLEN = 32;
    BizFlow BizFlow = null;

    BizFlow.Transition nodePath = null;

    public DNTransition(BizFlow bf, BizFlow.Transition np)
    {
      this.BizFlow = bf;
      this.nodePath = np;

      building();
    }

    public String getTitle()
    {
      if (this.nodePath == null) {
        return null;
      }
      return this.nodePath.getTitle();
    }

    public void setTitle(String t)
    {
      if (this.nodePath == null) {
        return;
      }
      this.nodePath.setTitle(t);
    }

    public BizFlow.Transition getTransition()
    {
      return this.nodePath;
    }

    protected void building()
    {
      ArrayList allps = this.nodePath.getAllPoints();
      int c = allps.size();
      DrawNode.DNTransitionSeg dnts;
      for (int i = 1; i < c; i++)
      {
        dnts = new DrawNode.DNTransitionSeg(this, 
          (BizFlow.IdPoint)allps.get(i - 1), (BizFlow.IdPoint)allps.get(i));
        appendChildNode(dnts);
      }

      for (BizFlow.IdPoint idp : this.nodePath.getAllPoints())
      {
        DrawNode.DNTransitionPoint dntp = new DrawNode.DNTransitionPoint(this, idp);
        appendChildNode(dntp);
      }
    }

    public Object getUserObject()
    {
      return this.nodePath;
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof DNTransition))
      {
        return false;
      }

      DNTransition ot = (DNTransition)o;
      return this.nodePath.equals(ot.nodePath);
    }

    public Polygon[] getDrawingPolygons(Graphics g)
    {
      Polygon p = new Polygon();

      Point p0 = this.nodePath.getPathCenterPoint();
      p.addPoint(p0.x, p0.y);
      return new Polygon[] { p };
    }

    public Point getPos()
    {
      return this.nodePath.getTransitionPos();
    }

    public void setPos(int x, int y)
    {
      this.nodePath.setTransitionPos(new Point(x, y));
    }

    protected void drawingNormal(Graphics g)
    {
      String t = this.nodePath.getTitle();

      if (t == null)
        return;
      Point p = this.nodePath.getPathCenterPoint();
      Color oldc = g.getColor();
      g.setColor(Color.blue);
      g.drawString(t, p.x, p.y);
      g.setColor(oldc);
    }

    protected void drawingSelected(Graphics g)
    {
      String t = this.nodePath.getTitle();
      BizCondition bc = this.nodePath.getCondition();
      if (bc != null)
      {
        String s = bc.getConditionContent();
        if (s != null) {
          t = t + s;
        }

      }

      if (t == null)
        return;
      Point p = this.nodePath.getPathCenterPoint();
      Color oldc = g.getColor();
      g.setColor(Color.red);
      g.drawString(t, p.x, p.y);
      g.setColor(oldc);
    }

    public void drawDragged(int movex, int movey, Graphics g)
    {
      AbstractDrawNode[] cdns = getAllChildren();
      if (cdns == null) {
        return;
      }
      for (AbstractDrawNode tmpdn : cdns)
      {
        if ((tmpdn instanceof DrawNode.DNTransitionPoint))
        {
          DrawNode.DNTransitionPoint dntp = (DrawNode.DNTransitionPoint)tmpdn;
          dntp.drawDragged(movex, movey, g);
        }
        else if ((tmpdn instanceof DrawNode.DNTransitionSeg))
        {
          DrawNode.DNTransitionSeg dnts = (DrawNode.DNTransitionSeg)tmpdn;
          dnts.drawDragged(movex, movey, g);
        }
      }
    }
  }

  public static class DNTransitionPoint extends AbstractDrawNode
    implements Draggable
  {
    public static final int R = 3;
    DrawNode.DNTransition dnTrans = null;

    BizFlow.IdPoint idp = null;

    public DNTransitionPoint(DrawNode.DNTransition dnt, BizFlow.IdPoint p)
    {
      this.dnTrans = dnt;
      this.idp = p;

      setSelectedStyle(AbstractDrawNode.SelectStyle.Normal);
    }

    public BizFlow.IdPoint getIdPoint()
    {
      return this.idp;
    }

    public DrawNode.DNTransition getDNTransition()
    {
      return this.dnTrans;
    }

    public boolean canUnset()
    {
      return this.dnTrans.getTransition().getMidPointIdx(this.idp) >= 0;
    }

    protected void building()
    {
    }

    protected void drawingNormal(Graphics g)
    {
    }

    public Rectangle getDrawingRect()
    {
      int w = 6;
      int x = this.idp.getX();
      int y = this.idp.getY();

      return new Rectangle(x - 3, y - 3, w, w);
    }

    protected void drawingSelected(Graphics g)
    {
      Color oc = g.getColor();
      Rectangle rect = getDrawingRect();

      g.setColor(Color.RED);

      g.drawRect(rect.x, rect.y, rect.width, rect.height);

      g.setColor(oc);
    }

    public Polygon[] getDrawingPolygons(Graphics g)
    {
      Rectangle rect = getDrawingRect();

      int[] xps = new int[4];
      int[] yps = new int[4];

      xps[0] = rect.x;
      yps[0] = rect.y;
      xps[1] = (rect.x + rect.width);
      yps[1] = rect.y;
      xps[2] = (rect.x + rect.width);
      yps[2] = (rect.y + rect.height);
      xps[3] = rect.x;
      yps[3] = (rect.y + rect.height);
      return new Polygon[] { new Polygon(xps, yps, 4) };
    }

    public Object getUserObject()
    {
      return null;
    }

    public Point getPos()
    {
      return this.idp.getLocation();
    }

    public void setPos(int x, int y)
    {
      this.idp.setLocation(x, y);
    }

    public void drawDragged(int movex, int movey, Graphics g)
    {
      Color oc = g.getColor();
      Rectangle rect = getDrawingRect();

      g.setColor(Color.RED);

      g.drawRect(rect.x + movex, rect.y + movey, rect.width, rect.height);

      g.setColor(oc);
    }
  }

  public static class DNTransitionSeg extends AbstractDrawNode
    implements BizFlow.ITitleable
  {
    DrawNode.DNTransition dnTrans = null;

    BizFlow.IdPoint fIdPt = null;

    BizFlow.IdPoint tIdPt = null;
    private static final double ROTATED = 0.1790658503988659D;
    private static final double ARRAWLEN = 20.0D;

    public DNTransitionSeg(DrawNode.DNTransition dnt, BizFlow.IdPoint from, BizFlow.IdPoint to)
    {
      this.dnTrans = dnt;
      this.fIdPt = from;
      this.tIdPt = to;

      setSelectedStyle(AbstractDrawNode.SelectStyle.CannotSelectSelf);
    }

    public DrawNode.DNTransition getDNTransition()
    {
      return this.dnTrans;
    }

    public String getTitle()
    {
      return this.dnTrans.getTitle();
    }

    public void setTitle(String t)
    {
      this.dnTrans.setTitle(t);
    }

    public BizFlow.IdPoint getFromIdPoint()
    {
      return this.fIdPt;
    }

    public BizFlow.IdPoint getToIdPoint()
    {
      return this.tIdPt;
    }

    public boolean isEndSeg()
    {
      return this.dnTrans.getTransition().getEndPoint().equals(this.tIdPt);
    }

    public boolean isStartSeg()
    {
      return this.dnTrans.getTransition().getStartPoint().equals(this.fIdPt);
    }

    protected void building()
    {
    }

    protected void drawingNormal(Graphics g)
    {
      Color oldc = g.getColor();
      g.setColor(Color.blue);
      if (isStartSeg())
      {
        g.drawOval(this.fIdPt.getX() - 4, this.fIdPt.getY() - 4, 8, 8);
      }

      if (isEndSeg())
      {
        drawArrawLink(g, this.fIdPt.getX(), this.fIdPt.getY(), this.tIdPt.getX(), 
          this.tIdPt.getY(), Color.blue, null);
      }
      else
      {
        g.drawLine(this.fIdPt.getX(), this.fIdPt.getY(), this.tIdPt.getX(), 
          this.tIdPt.getY());
      }
      g.setColor(oldc);
    }

    protected void drawingSelected(Graphics g)
    {
      Color oldc = g.getColor();
      g.setColor(Color.red);

      if (isStartSeg())
      {
        g.drawOval(this.fIdPt.getX() - 4, this.fIdPt.getY() - 4, 8, 8);
        g.drawLine(this.fIdPt.getX(), this.fIdPt.getY(), this.tIdPt.getX(), 
          this.tIdPt.getY());
      }

      if (isEndSeg())
      {
        drawArrawLink(g, this.fIdPt.getX(), this.fIdPt.getY(), this.tIdPt.getX(), 
          this.tIdPt.getY(), Color.red, null);
      }
      else
      {
        g.drawLine(this.fIdPt.getX(), this.fIdPt.getY(), this.tIdPt.getX(), 
          this.tIdPt.getY());
      }

      g.setColor(oldc);
    }

    public void drawDragged(int movex, int movey, Graphics g)
    {
      Color oldc = g.getColor();
      g.setColor(Color.red);

      if (isStartSeg())
      {
        g.fillOval(this.fIdPt.getX() - 4 + movex, this.fIdPt.getY() + movey - 4, 
          8, 8);
        g.drawLine(this.fIdPt.getX() + movex, this.fIdPt.getY() + movey, 
          this.tIdPt.getX() + 
          movex, this.tIdPt.getY() + movey);
      }
      else if (isEndSeg())
      {
        drawArrawLink(g, this.fIdPt.getX() + movex, this.fIdPt.getY() + movey, 
          this.tIdPt.getX() + movex, this.tIdPt.getY() + movey, Color.red, 
          null);
      }
      else
      {
        g.drawLine(this.fIdPt.getX() + movex, this.fIdPt.getY() + movey, 
          this.tIdPt.getX() + 
          movex, this.tIdPt.getY() + movey);
      }

      g.setColor(oldc);
    }

    public Polygon[] getDrawingPolygons(Graphics g)
    {
      Polygon[] retps = new Polygon[1];

      Point fpt = this.fIdPt.getLocation();
      Point tpt = this.tIdPt.getLocation();

      int cx = tpt.x - fpt.x;
      int cy = tpt.y - fpt.y;
      int acx = Math.abs(cx);
      int acy = Math.abs(cy);

      int r = 8;

      if ((acx <= r) && (acy <= r)) {
        return null;
      }
      if (acx >= acy)
      {
        if (cx > 0)
        {
          fpt.x += r;
          tpt.x -= r;
        }
        else
        {
          fpt.x -= r;
          tpt.x += r;
        }

        int y0 = r * acy / acx;
        if (cy > 0)
        {
          fpt.y += y0;
          tpt.y -= y0;
        }
        else
        {
          fpt.y -= y0;
          tpt.y += y0;
        }

      }
      else
      {
        if (cy > 0)
        {
          fpt.y += r;
          tpt.y -= r;
        }
        else
        {
          fpt.y -= r;
          tpt.y += r;
        }

        int x0 = r * acx / acy;
        if (cx > 0)
        {
          fpt.x += x0;
          tpt.x -= x0;
        }
        else
        {
          fpt.x -= x0;
          tpt.x += x0;
        }
      }

      retps[0] = DrawNode.getLinePolygon(fpt, tpt);

      return retps;
    }

    public Object getUserObject()
    {
      return null;
    }

    private static void drawArrawLink(Graphics g, double x1, double y1, double x2, double y2, Color linec, Color fillc)
    {
      if (linec == null)
      {
        throw new IllegalArgumentException("Line Color cannot be null!");
      }
      Color oldc = g.getColor();
      try
      {
        double d4 = 20.0D;
        double s = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (
          y1 - y2));
        double[] ad = { x2 - d4 / s * (x2 - x1), 
          y2 - d4 / s * (y2 - y1) };
        g.setColor(linec);

        g.drawLine((int)x1, (int)y1, (int)ad[0], (int)ad[1]);
        if (s < 1.0D)
        {
          return;
        }

        double[] ay1 = new double[4];
        AffineTransform affinetransform = 
          AffineTransform.getRotateInstance(0.1790658503988659D, x2, y2);
        affinetransform.transform(ad, 0, ay1, 0, 1);
        affinetransform = AffineTransform.getRotateInstance(
          -0.1790658503988659D, x2, y2);
        affinetransform.transform(ad, 0, ay1, 2, 1);
        int[] ai = { (int)x2, (int)ay1[0], (int)ay1[2] };
        int[] ai1 = { (int)y2, (int)ay1[1], (int)ay1[3] };

        if (fillc != null)
        {
          g.setColor(fillc);
          g.fillPolygon(ai, ai1, 3);
        }
        g.setColor(linec);
        g.drawPolygon(ai, ai1, 3);
        return;
      }
      finally {
        g.setColor(oldc);
      }
    }
  }
}
