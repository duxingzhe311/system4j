package com.dw.system.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

public class DefaultMO extends MouseOperable
{

	Point dragStartPoint = null;

	Point lastDragPoint = null;

	boolean bDragged = false;

	
	DrawPanel drawPanel = null;

	public DefaultMO(DrawPanel dp)
	{
		drawPanel = dp;
	}

	public void assemblePopupMenu(JMenu m, List<AbstractAction> aas)
	{
		if (aas == null)
			return;

		for (AbstractAction aa : aas)
		{
			assembleMenu(m, aa);
		}
	}

	private void assemblePopupMenu(JPopupMenu pm, AbstractAction aa)
	{
		if (!(aa instanceof ActionArrays))
		{
			pm.add(aa);
		}
		else
		{
			ActionArrays aas = (ActionArrays) aa;
			JMenu m = new JMenu(aas.getName());
			pm.add(m);
			AbstractAction[] aas0 = aas.getActions();
			if (aas0 != null)
			{
				for (int i = 0; i < aas0.length; i++)
				{
					assembleMenu(m, aas0[i]);
				}
			}
		}
	}

	private void assembleMenu(JMenu m, AbstractAction aa)
	{
		if (aa == NoAction.Value)
		{
			m.addSeparator();
		}

		if (aa instanceof ActionArrays)
		{
			ActionArrays aas = (ActionArrays) aa;
			JMenu m0 = new JMenu(aas.getName());
			m.add(m0);
			AbstractAction[] aas0 = aas.getActions();
			if (aas0 != null)
			{
				for (int i = 0; i < aas0.length; i++)
				{
					assembleMenu(m0, aas0[i]);
				}
			}

			return;
		}

		m.add(aa);
	}

	public static class ActionArrays extends AbstractAction
	{
		String name = null;

		AbstractAction[] aas = null;

		public ActionArrays(String name, AbstractAction[] aas)
		{
			this.name = name;
			this.aas = aas;
		}

		public void actionPerformed(ActionEvent ae)
		{
		}

		public AbstractAction[] getActions()
		{
			return aas;
		}

		public String getName()
		{
			return name;
		}
	}

	public static class NoAction extends AbstractAction
	{
		public static NoAction Value = new NoAction();

		private NoAction()
		{
		}

		public void actionPerformed(ActionEvent ae)
		{
		}
	}

	// AbstractDrawNode selectedDrawNode = null;
	public void mouseClicked(MouseEvent e)
	{
		AbstractDrawNode currentRootDrawItem = drawPanel.getRootDrawNode();
		if (currentRootDrawItem == null)
		{
			return;
		}

		switch (e.getButton())
		{
		case MouseEvent.BUTTON1:
			if (e.getClickCount() <= 1)
				return;

			AbstractDrawNode osnode = drawPanel.getCurSelectNode();
			drawPanel
					.setCurSelectNode(currentRootDrawItem
							.canSelectDescendentChildNode(drawPanel.getGraphics(), e.getX(), e
									.getY()));

			// fireOpenCurrentDrawNode() ;

			break;
		case MouseEvent.BUTTON2:
		// break ;
		case MouseEvent.BUTTON3:
		}
	} // end of mouseClicked

	void fireSelectedChanged(AbstractDrawNode root, AbstractDrawNode snode,
			AbstractDrawNode oldnode)
	{
		if (oldnode != null)
			oldnode.onNodeUnselected();
		if (snode != null)
			snode.onNodeSelected();

		// for (Enumeration en = selectedChangedListeners.elements(); en
		// .hasMoreElements();)
		// {
		// FlowUISelectedChangelistener cl = (FlowUISelectedChangelistener) en
		// .nextElement();
		// cl.selectedChanged(root, snode, oldnode);
		// }

	}

	public void mousePressed(MouseEvent e)
	{
		lastDragPoint = dragStartPoint = e.getPoint();
		bDragged = false;

		AbstractDrawNode currentRootDrawItem = drawPanel.getRootDrawNode();
		if (currentRootDrawItem == null)
		{
			return;
		}

		switch (e.getButton())
		{
		case MouseEvent.BUTTON1:
			AbstractDrawNode osnode = drawPanel.getCurSelectNode();
			AbstractDrawNode selnode = currentRootDrawItem.selectNodeByStyle(
					osnode, drawPanel.getGraphics(), e.getX(), e.getY());
			drawPanel.setCurSelectNode(selnode);
			if (osnode != selnode)
			{ // fire event
				fireSelectedChanged(currentRootDrawItem, selnode, osnode);
			}

			// System.out.println(this.selectedDrawNode);
			if (selnode != null)
			{
				selnode.setDragPoint(e.getPoint());
			}
			drawPanel.repaint();
			break;
		case MouseEvent.BUTTON2:
		// break ;
		case MouseEvent.BUTTON3:
			AbstractDrawNode tmpdn = currentRootDrawItem
					.canSelectDescendentChildNode(drawPanel.getGraphics(), e.getX(), e.getY());
			Object obj = null;
			if (tmpdn == null)
			{
				// obj = wfProcess ;
				return;
			}
			else
			{
				obj = tmpdn.getUserObject();
			}

			JMenu pmenu = new JMenu();

			ArrayList<AbstractAction> aas = null;// ActionManager.getInstance().getCanDoActions(mcModel,obj);
			aas = new ArrayList<AbstractAction>(2);

			assemblePopupMenu(pmenu, aas);

			pmenu.getPopupMenu().show(drawPanel, e.getX(), e.getY());
		}
	} // end of mousePrecessed

	public void mouseDragged(MouseEvent e)
	{
		try
		{
			AbstractDrawNode rootDrawItem = drawPanel.getRootDrawNode();
			if (rootDrawItem == null || dragStartPoint == null)
			{
				return;
			}

			AbstractDrawNode selnode = drawPanel.getCurSelectNode();
			if (selnode == null)
			{
				return;
			}

			if (selnode instanceof Draggable)
			{
				Draggable nbi = (Draggable) selnode;
				Point op = nbi.getPos();
				Point dp = selnode.getDragPoint();

				int tmplx = lastDragPoint.x - dp.x;
				int tmply = lastDragPoint.y - dp.y;
				int tmpnx = e.getX() - dp.x;
				int tmpny = e.getY() - dp.y;

				if (tmpnx == 0 && tmpny == 0)
					return;

				bDragged = true;
				// selectedDrawNode.setDragPoint(e.getPoint());
				Graphics g = drawPanel.getGraphics();
				g.setXORMode(Color.pink);
				nbi.drawDragged(tmplx, tmply, g);
				nbi.drawDragged(tmpnx, tmpny, g);
				g.setPaintMode();
				return;
			}

		}
		finally
		{
			lastDragPoint = e.getPoint();
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		try
		{
			// for draw arraw
			dragStartPoint = null;
			lastDragPoint = null;
			AbstractDrawNode rootDrawItem = drawPanel.getRootDrawNode();
			if (rootDrawItem == null)
			{
				return;
			}

			// rootDrawNode.setController(defaultController);

			AbstractDrawNode selnode = drawPanel.getCurSelectNode();
			switch (e.getButton())
			{
			case MouseEvent.BUTTON1:
				if (selnode == null)
				{
					return;
				}

				if ((selnode instanceof Draggable) && bDragged)
				{
					try
					{
						Draggable nbi = (Draggable) selnode;
						Point op = nbi.getPos();
						Point dp = selnode.getDragPoint();

						nbi.setPos(op.x + (e.getX() - dp.x), op.y
								+ (e.getY() - dp.y));

						Object obj = selnode.getUserObject();

						drawPanel.flushDraw();
						// drawPanel.fireChanged();
					}
					catch (Exception ee)
					{
						ee.printStackTrace();
						JOptionPane.showMessageDialog(drawPanel, ee
								.getMessage());
					}
				}
				else
				{
					{
						// stateStub.setCurrentState(STAT_NORMAL);
					}
				}

				drawPanel.repaint();
			case MouseEvent.BUTTON2:
			case MouseEvent.BUTTON3:
			}
		}
		finally
		{
			bDragged = false;
		}
	}

}
