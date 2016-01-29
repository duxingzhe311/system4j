package com.dw.system.graph;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

public abstract class DraggableDrawNode extends AbstractDrawNode
	implements Draggable
{
	@Override
	protected final void drawingNormal(Graphics g)
	{
		Point curpos = getPos();
		drawingNormalInPos(g,curpos.x,curpos.y);
	}

	@Override
	protected final void drawingSelected(Graphics g)
	{
		Point curpos = getPos();
		drawingSelectedInPos(g,curpos.x,curpos.y);
	}

	/**
	 * 在指定的位置进行绘画
	 * @param g
	 * @param x
	 * @param y
	 */
	protected abstract void drawingNormalInPos(Graphics g,int x,int y);
	
	/**
	 * 在指定的位置进行绘画
	 * @param g
	 * @param x
	 * @param y
	 */
	protected abstract void drawingSelectedInPos(Graphics g,int x,int y);
	
	public final void drawDragged(int movex,int movey,Graphics g)
	{
		Point curpos = getPos();
		drawingSelectedInPos(g,curpos.x + movex,curpos.y+movey);
	}
}
