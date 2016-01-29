/*
 * Created on 2004-7-28
 *
 * Copyright (c) Jason Zhu
 */
package com.dw.system.graph;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * 抽象绘画节点
 * 
 * @author Jason Zhu
 * 
 * Desc:
 */
public abstract class AbstractDrawNode
{
	static Graphics currentGraphics = null;

	static Component currentComponent = null;

	static public void setCurrentGraphics(Graphics g)
	{
		currentGraphics = g;
	}

	static public Graphics getCurrentGraphics()
	{
		return currentGraphics;
	}

	static public void setCurrentDrawComponent(Component cp)
	{
		currentComponent = cp;
		if (currentComponent == null)
		{
			currentGraphics = null;
		}
		else
		{
			currentGraphics = currentComponent.getGraphics();
		}
	}

	static public Component getCurrentDrawComponent()
	{
		return currentComponent;
	}
	
	public static boolean isDrawCtrlState(short v)
	{
		switch(v)
		{
		case DrawController.THIS_NORMAL:
		case DrawController.THIS_SELECTED:
		case DrawController.THIS_HIDDEN:
		case DrawController.THIS_BLINK:
		case DrawController.THIS_OFFSPRING_NORMAL:
		case DrawController.THIS_OFFSPRING_SELECTED:
		case DrawController.THIS_OFFSPRING_HIDDEN:
		case DrawController.THIS_OFFSPRING_BLINK:
			return true;
		default:
			return false;
		}
	}

	public static final short SWITCHER_NULL = -1; // 不起作用

	public static final short SWITCHER_NORMAL = 0; // normal

	public static final short SWITCHER_SELECT = 1;

	/**
	 * 节点在鼠标操作时如何被选择
	 * 
	 * @author Jason Zhu
	 */
	public static enum SelectStyle
	{
		Normal, // 普通情况
		CannotSelectSelf, // 当鼠标点击到它时,不能被选中,只能判断它的父节点是否可以被选中
		AfterParentSelect, // 只有其直接父节点已经在被选中的状态下,再次点击才可以被选中
		AfterParentSelect_CannotSelectSelf,//不能直接被选中,但可以在父节点被选择之后被选中
	}
	
	IDrawContainer drawContainer = null ;

	Vector childs = new Vector();

	/**
	 * 被选中时,可以独立的被选择,如果不能被独立选中,则应该选择其父节点
	 */
	SelectStyle selectStyle = SelectStyle.Normal;

	AbstractDrawNode parent = null;

	DrawController drawController = null; // DrawController.THIS_OFFSPRING_NORMAL;

	Point dragPoint = null;
	
	short selfDrawState = -1 ;

	/**
	 * 控制开关，SWITCHER_NULL表示不起作用，它在controller之后使用
	 */
	public short switcher = SWITCHER_NULL;

	public AbstractDrawNode()
	{
	}

	public SelectStyle getSelectStyle()
	{
		return selectStyle;
	}

	public void setSelectedStyle(SelectStyle ss)
	{
		selectStyle = ss;
	}

	public Point getDragPoint()
	{
		return dragPoint;
	}

	/**
	 * 得到本节点在绘画时有效的控制器.包含继承德控制器
	 * 
	 * @return 能够控制绘画的控制器
	 */
	private DrawController getValidateDrawController()
	{
		if (drawController != null)
		{
			return drawController;
		}

		AbstractDrawNode po = this;
		AbstractDrawNode pn = null;
		DrawController tmpdc = null;
		// 寻找继承控制器
		while ((pn = po.getParentNode()) != null)
		{
			tmpdc = pn.getController();
			if (tmpdc != null && tmpdc.isInherited())
			{
				return tmpdc;
			}
			po = pn;
		}
		return null;
	}

	public short getDrawState()
	{
		//如果自己定义了绘画控制信息,则使用自己的
		if(isDrawCtrlState(selfDrawState))
			return selfDrawState;
		
		DrawController dc = getValidateDrawController();
		if (dc == null)
		{
			return DrawController.THIS_NORMAL;
		}
		return dc.getDrawingState(this);
	}
	
	/**
	 * 得到节点自己控制定义好的绘画状态
	 * @return
	 */
	public short getSelfDrawState()
	{
		return selfDrawState;
	}
	
	public void setSelfDrawState(short v)
	{
		selfDrawState = v ;
	}
	
	public void unsetSelfDrawState()
	{
		selfDrawState = -1 ;
	}

	public DrawController getController()
	{
		return drawController;
	}
	
	

	public void setController(DrawController dc)
	{
		drawController = dc;
	}

	public void setControllerDescendent(DrawController dc)
	{
		drawController = dc;
		AbstractDrawNode[] cs = getAllChildren();
		for (int i = 0; i < cs.length; i++)
		{
			cs[i].setControllerDescendent(dc);
		}
	}

	public IDrawContainer getDrawContainer()
	{
		if(this.drawContainer!=null)
			return drawContainer;
		
		AbstractDrawNode dn = this.getParentNode() ;
		if(dn==null)
			return null ;
		
		return dn.getDrawContainer() ;
	}
	
	public void setDragPoint(Point p)
	{
		dragPoint = p;
	}

	public void setDragPoint(int x, int y)
	{
		dragPoint = new Point(x, y);
	}

	public void removeFromParent()
	{
		if (parent == null)
		{
			return;
		}
		parent.removeChild(this);
	}

	/**
	 * 增加子节点，并且返回自己
	 */
	public AbstractDrawNode appendChildNode(AbstractDrawNode adn)
	{
		if (childs.contains(adn))
		{
			return this;
		}
		childs.addElement(adn);
		adn.parent = this;
		return this;
	}

	public int indexOf(AbstractDrawNode childdn)
	{
		return childs.indexOf(childdn);
	}

	public AbstractDrawNode getChildAt(int i)
	{
		return (AbstractDrawNode) childs.elementAt(i);
	}

	public AbstractDrawNode findChildByUserObj(Object userobj)
	{
		int cc = childs.size();
		for (int i = 0; i < cc; i++)
		{
			AbstractDrawNode dn = getChildAt(i);
			if (userobj.equals(dn.getUserObject()))
				return dn;
		}
		return null;
	}

	public int getChildNodeCount()
	{
		return childs.size();
	}

	public AbstractDrawNode getParentNode()
	{
		return parent;
	}

	public AbstractDrawNode removeChild(AbstractDrawNode oldChild)
	{
		if (!childs.contains(oldChild))
		{
			return null;
		}

		childs.remove(oldChild);
		oldChild.parent = null;
		return oldChild;
	}

	public void removeAllChild()
	{
		childs.clear();
	}

	/**
	 * 判断是否是根节点
	 * 
	 * @return true表示没有父节点,但有子节点的节点.孤立节点不能称作根结点
	 */
	public boolean isRootNode()
	{
		if (getParentNode() != null)
		{
			return false;
		}
		if (childs.size() <= 0)
		{
			return false;
		}
		return true;
	}

	/**
	 * 判断是否是叶子节点
	 * 
	 * @return true表示没有子节点,但有父节点.孤立节点不能称作叶结点
	 */
	public boolean isLeafNode()
	{
		if (childs.size() > 0)
		{
			return false;
		}
		if (getParentNode() == null)
		{
			return false;
		}
		return true;
	}

	/**
	 * 判断是否是孤立节点,既没有父节点,又没有子节点
	 * 
	 * @return true/false
	 */
	public boolean isAloneNode()
	{
		if (getParentNode() != null)
		{
			return false;
		}
		if (childs.size() > 0)
		{
			return false;
		}
		return true;
	}

	public AbstractDrawNode[] getAllChildren()
	{
		int s = childs.size();
		AbstractDrawNode[] rets = new AbstractDrawNode[s];
		for (int i = 0; i < s; i++)
		{
			rets[i] = (AbstractDrawNode) childs.elementAt(i);
		}
		return rets;
	}

	public boolean hasChild(AbstractDrawNode n)
	{
		return childs.contains(n);
	}

	public void drawNodeNormal(Graphics g)
	{
		drawNodeNormal(g, null);
	}

	public void drawNodeNormal(Graphics g, Rectangle within)
	{
		drawingNormal(g, within);
		AbstractDrawNode[] cs = getAllChildren();
		for (int i = 0; i < cs.length; i++)
		{
			cs[i].drawNodeNormal(g, within);
		}
	}

	public void drawNodeSelected(Graphics g)
	{
		drawNodeSelected(g, null);
	}

	public void drawNodeSelected(Graphics g, Rectangle within)
	{
		drawingSelected(g, within);
		AbstractDrawNode[] cs = getAllChildren();
		for (int i = 0; i < cs.length; i++)
		{
			cs[i].drawingSelected(g, within);
		}
	}

	/**
	 * 在节点被设置选中时会被调用
	 * 
	 */
	public void onNodeSelected()
	{
	}

	/**
	 * 当节点被设置成非选中状态时
	 * 
	 */
	public void onNodeUnselected()
	{
	}

	public void setSwitcher(short sw)
	{
		switch (sw)
		{
		case SWITCHER_NULL:
		case SWITCHER_NORMAL:
		case SWITCHER_SELECT:
			switcher = sw;
			break;
		default:
			switcher = SWITCHER_NULL;
		}
	}

	public short getSwitch()
	{
		return switcher;
	}

	public void drawNodeControll(Graphics g) // , DrawController contr)
	{
		drawNodeControll(g, null);
	}

	public void drawNodeControll(Graphics g, Rectangle within) // ,
																// DrawController
																// contr)
	{
		switch (getDrawState())
		{
		case DrawController.THIS_NORMAL:
			drawingNormal(g, within);
			break;
		case DrawController.THIS_SELECTED:
			drawingSelected(g, within);
			break;
		case DrawController.THIS_HIDDEN:
			break;
		case DrawController.THIS_BLINK:
			throw new RuntimeException("Blink is not support!");
		case DrawController.THIS_OFFSPRING_NORMAL:
			drawNodeNormal(g, within);
			return;
		case DrawController.THIS_OFFSPRING_SELECTED:
			drawNodeSelected(g, within);
			return;
		case DrawController.THIS_OFFSPRING_HIDDEN:
			return;
		case DrawController.THIS_OFFSPRING_BLINK:
			throw new RuntimeException("Blink is not support!");
		}

		AbstractDrawNode[] cs = getAllChildren();
		for (int i = 0; i < cs.length; i++)
		{
			cs[i].drawNodeControll(g, within);
		}

		switch (switcher)
		{
		case SWITCHER_NULL:
			break;
		case SWITCHER_NORMAL:
			drawingNormal(g, within);
			break;
		case SWITCHER_SELECT:
			drawingSelected(g, within);
		}
	}
	
	public Rectangle getBounds(Graphics g)
	{
		return getBounds(this.getDrawingPolygons(g)) ;
	}

	public static Rectangle getBounds(Polygon[] pgs)
	{
		if (pgs == null || pgs.length <= 0)
		{
			return null;
		}

		int xt = Integer.MAX_VALUE;
		int yt = Integer.MAX_VALUE;
		int xb = Integer.MIN_VALUE;
		int yb = Integer.MIN_VALUE;

		for (int i = 0; i < pgs.length; i++)
		{
			Rectangle r0 = pgs[i].getBounds();
			if (r0.x < xt)
			{
				xt = r0.x;
			}
			if (r0.y < yt)
			{
				yt = r0.y;
			}
			if (r0.x + r0.width > xb)
			{
				xb = r0.x + r0.width;
			}
			if (r0.y + r0.height > yb)
			{
				yb = r0.y + r0.height;
			}
		}
		return new Rectangle(xt, yt, xb - xt, yb - yt);
	}

	Point getRightBottom(Graphics g)
	{
		Polygon[] pgs = getDrawingPolygons(g);

		Rectangle r = getBounds(pgs);
		if (r == null)
		{
			r = new Rectangle(0, 0, 0, 0);
		}
		Point p = new Point(r.x + r.width, r.y + r.height);
		AbstractDrawNode[] cns = this.getAllChildren();
		for (int i = 0; i < cns.length; i++)
		{
			Point p0 = cns[i].getRightBottom(g);
			if (p0.x > p.x)
			{
				p.x = p0.x;
			}
			if (p0.y > p.y)
			{
				p.y = p0.y;
			}
		}
		return p;
	}

	public Dimension getSize(Graphics g)
	{
		Point p = getRightBottom(g);
		Dimension d = new Dimension(400, 400);
		if (d.width < p.x)
		{
			d.width = p.x;
		}
		if (d.height < p.y)
		{
			d.height = p.y;
		}
		d.width += 50;
		d.height += 50;
		// System.out.println("w="+d.width+" h="+d.height);
		return d;
	}

	/**
	 * 循环递归判断显示的位置是否有可被选择的元素
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean canSelectedPos(Graphics g, int x, int y)
	{
		if (getDrawState() == DrawController.THIS_OFFSPRING_HIDDEN)
		{
			return false;
		}

		Polygon[] ps = getDrawingPolygons(g);
		if (ps != null && ps.length > 0
				&& getDrawState() != DrawController.THIS_HIDDEN)
		{
			for (int i = 0; i < ps.length; i++)
			{
				if (ps[i].contains(x, y))
				{
					return true;
				}
			}
		}

		AbstractDrawNode[] ns = this.getAllChildren();
		for (int i = 0; i < ns.length; i++)
		{
			if (ns[i].canSelectedPos(g, x, y))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断该节点和该节点的相关子节点所在的绘画区域是否包含某一坐标位置
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean canNodeSelectedPos(Graphics g, int x, int y)
	{
		return canSelectedPos(g, x, y);
	}

	/**
	 * 当前节点下的可被选择的直接子节点
	 */
	public AbstractDrawNode canSelectChildNode(Graphics g, int x, int y)
	{
		int s = childs.size();

		for (int i = 0; i < s; i++)
		{
			AbstractDrawNode tmpd = (AbstractDrawNode) childs.elementAt(i);
			if (tmpd.canSelectedPos(g, x, y))
			{
				return tmpd;
			}
		}
		return null;
	}

	/**
	 * 当前节点下的可被选择的最小子/孙节点
	 */
	public AbstractDrawNode canSelectDescendentChildNode(Graphics g, int x,
			int y)
	{
		AbstractDrawNode tmpn = canSelectChildNode(g, x, y);
		if (tmpn == null)
		{
			return null;
		}
		else
		{
			AbstractDrawNode tmpn0 = tmpn.canSelectChildNode(g, x, y);
			if (tmpn0 == null)
			{
				return tmpn;
			}
			else
			{// 往下递归寻找点中的最小节点
				return tmpn.canSelectDescendentChildNode(g, x, y);
			}
		}
	}

	/**
	 * 根据节点的选择风格,定位被选择的节点
	 * 
	 * @param cur_selnode
	 * @param g
	 * @param x
	 * @param y
	 * @return
	 */
	public AbstractDrawNode selectNodeByStyle(AbstractDrawNode cur_selnode,
			Graphics g, int x, int y)
	{
		AbstractDrawNode tmpn = canSelectDescendentChildNode(g, x, y);
		if (tmpn == null)
			return null;
		
		if(cur_selnode==tmpn)
			return tmpn;//如果已经选择了同样最小的节点,则不做变化
		
		return selectNodeByStyle(tmpn,cur_selnode,g,x,y);
	}
	
	private AbstractDrawNode selectNodeByStyle(AbstractDrawNode curjn,AbstractDrawNode cur_selnode,
			Graphics g, int x, int y)
	{
		if (curjn.selectStyle == SelectStyle.Normal)
			return curjn;

		if (curjn.selectStyle == SelectStyle.CannotSelectSelf)
		{
			AbstractDrawNode pn = curjn.getParentNode();
			if(pn==null)
				return null ;
			
			//直接判断父节点是否可以被选中
			return selectNodeByStyle(pn,cur_selnode, g, x, y);
		}

		if (curjn.selectStyle == SelectStyle.AfterParentSelect)
		{
			if (getParentNode() == cur_selnode)
				return curjn;
			else
				return null;
		}
		
		if(curjn.selectStyle==SelectStyle.AfterParentSelect_CannotSelectSelf)
		{
			AbstractDrawNode pn = curjn.getParentNode();
			if(pn==null)
				return null ;
			
			if (pn == cur_selnode)
				return curjn;
			
//			直接判断父节点是否可以被选中
			return selectNodeByStyle(pn,cur_selnode, g, x, y);
		}

		return null;
	}

	/*
	 * 根据当前节点所组成的树状结构进行重新构建绘画内容。
	 */
	public void rebuild(IDrawContainer dc)
	{
		// 
		removeAllChild();
		building();
		this.drawContainer = dc ;
		int s = childs.size();
		 for (int i = 0; i < s; i++)
		 {
		 AbstractDrawNode tmpd = (AbstractDrawNode) childs.elementAt(i);
		 tmpd.rebuild(dc);
		 }
	}
	
	
//	public void rebuildAll(IDrawContainer dc)
//	{
//		// 
//		removeAllChild();
//		building();
//		this.drawContainer = dc ;
//		int s = childs.size();
//		 for (int i = 0; i < s; i++)
//		 {
//		 AbstractDrawNode tmpd = (AbstractDrawNode) childs.elementAt(i);
//		 tmpd.rebuildAll(dc);
//		 }
//	}

	private boolean isCover(Rectangle area, Polygon[] units)
	{
		if (units == null || units.length <= 0)
		{
			return false;
		}
		Rectangle runit = getBounds(units);
		if (!isProjectOverlap(area.x, area.x + area.width, runit.x, runit.x
				+ runit.width))
		{
			return false;
		}
		if (!isProjectOverlap(area.y, area.y + area.height, runit.y, runit.y
				+ runit.height))
		{
			return false;
		}
		return true;
	}

	private boolean isProjectOverlap(int sx0, int ex0, int sx1, int ex1)
	{
		if (sx0 > ex1)
		{
			return false;
		}
		if (sx1 > ex0)
		{
			return false;
		}
		return true;
	}

	private void drawingNormal(Graphics g, Rectangle within)
	{
		if (within == null)
		{
			drawingNormal(g);
			return;
		}
		Polygon[] myr = getDrawingPolygons(g);
		if (isCover(within, myr))
		{
			drawingNormal(g);
		}
	}

	private void drawingSelected(Graphics g, Rectangle within)
	{
		if (within == null)
		{
			drawingSelected(g);
			return;
		}
		Polygon[] myr = getDrawingPolygons(g);
		if (isCover(within, myr))
		{
			drawingSelected(g);
		}
	}

	abstract protected void building();

	abstract protected void drawingNormal(Graphics g);

	abstract protected void drawingSelected(Graphics g);

	abstract public Polygon[] getDrawingPolygons(Graphics g);

	abstract public Object getUserObject();
}