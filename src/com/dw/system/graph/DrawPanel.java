package com.dw.system.graph;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JPanel;
import javax.swing.JViewport;

/**
 * 绘画面板，该类应该由scrollPane包含
 * 
 * @author Jason Zhu
 * 
 */
public class DrawPanel extends JPanel implements IDrawContainer// ,Printable,Pageable
{
	AbstractDrawNode rootDrawNode = null;

	AbstractDrawNode selectedDrawNode = null;
	
	MouseOperable mouseOp = null ;

	public DrawPanel()
	{
		// addMouseListener(new MyMouseAdapter());

		addMouseMotionListener(mouseMADP);
		addMouseListener(mouseAdp);
		
		this.setBackground(Color.GRAY) ;
	}
	
	/**
	 * 继承类想要有自己的动作，可以重载本方法
	 * @return
	 */
	protected MouseOperable getCurMouseOperable()
	{
		if(mouseOp!=null)
			return mouseOp ;
		
		mouseOp = new DefaultMO(this) ;
		return mouseOp ;
	}

	MouseAdapter mouseAdp = new MouseAdapter()
	{
		public void mouseClicked(MouseEvent e)
		{
			getCurMouseOperable().mouseClicked(e) ;
		}

		public void mousePressed(MouseEvent e)
		{
			getCurMouseOperable().mousePressed(e) ;
		}

		public void mouseReleased(MouseEvent e)
		{
			getCurMouseOperable().mouseReleased(e) ;
		}
	};

	MouseMotionAdapter mouseMADP = new MouseMotionAdapter()
	{

		public void mouseMoved(MouseEvent e)
		{
			getCurMouseOperable().mouseMoved(e) ;
		}

		public void mouseDragged(MouseEvent e)
		{
			getCurMouseOperable().mouseDragged(e) ;
		}

	};

	public void setRootDrawNode(AbstractDrawNode adn)
	{
		rootDrawNode = adn;
	}

	public AbstractDrawNode getRootDrawNode()
	{
		return rootDrawNode;
	}

	public AbstractDrawNode getCurSelectNode()
	{
		return selectedDrawNode;
	}
	
	public void setCurSelectNode(AbstractDrawNode dn)
	{
		selectedDrawNode = dn ;
	}

	public void paint(Graphics g)
	{
		super.paint(g);

		if (rootDrawNode != null)
		{
			Container cont = getParent();
			if (cont instanceof JViewport)
			{
				Rectangle r = ((JViewport) cont).getViewRect();
				rootDrawNode.drawNodeControll(g, r);
			}
			else
			{
				rootDrawNode.drawNodeControll(g);
			}
		}// end of if

		// draw arrow
		// if (draggedArrowItem != null)
		// {
		// draggedArrowItem.drawingNormal(g);
		// }
	}
	
	public Dimension getSize()
	{
		return getPreferredSize() ;
	}
	
	
	private transient long lastup = 0 ;
	private transient Dimension lastSize = null ;
	
	public Dimension getPreferredSize()
	{
		if(System.currentTimeMillis()-lastup<50)
		{
			return lastSize ;
		}
		
		if (rootDrawNode == null)
		{
			return super.getPreferredSize();
		}
		
		Dimension pd = this.getParent().getSize() ;
		lastSize = rootDrawNode.getSize(getGraphics());
		if(lastSize.width<pd.width)
			lastSize.width = pd.width ;
		if(lastSize.height<pd.height)
			lastSize.height = pd.height ;
		// System.out.println("dddd w="+d.width+" h="+d.height);
		lastup=System.currentTimeMillis() ;
		return lastSize;
	}
	
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	public void flushDraw()
	{
		if (rootDrawNode != null)
		{
			// 做清扫
			//bizFlow.cleanFlow();
			// System.out.println("rebuild diagram----");
			rootDrawNode.rebuild(this);
			// this.selectedDrawNode = null;
			invalidate();
			repaint();
		}
	}
}
