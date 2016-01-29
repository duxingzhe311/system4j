/*
 * Created on 2004-7-28
 *
 * Copyright (c) Jason Zhu
 */
package com.dw.system.graph;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;
import java.awt.event.*;

//import com.dw.biz.ui.FlowDrawable;

/**
 * 
 * @author Jason Zhu
 *
 * Desc:
 */
public class DraggedArrowItem
	extends AbstractDrawNode
{
	//private static final double ROTATED = 0.3490658503988659D ;
	private static final double ROTATED = 0.1790658503988659D;
	private static final double ARRAWLEN = 20D;

	Point fromp = null;
	Point top = null;
	public DraggedArrowItem()
	{

	}

	public void setFromToPoint(Point fp, Point tp)
	{
		fromp = fp;
		top = tp;
	}

	protected void building()
	{
	}
	
	public Object getUserObject()
	{
		return null ;
	}

	private void drawLink(Graphics g, double x1, double y1, double x2,
						  double y2, Color linec, Color fillc)
	{
		if (linec == null)
		{
			throw new IllegalArgumentException("Line Color cannot be null!");
		}
		Color oldc = g.getColor();
		try
		{
			double d4 = ARRAWLEN;
			double s = Math.sqrt( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
			double ad[] =
				{
				x2 - (d4 / s) * (x2 - x1), y2 - (d4 / s) * (y2 - y1)
			};
			g.setColor(linec);
			//g.drawLine( (int) x1, (int) y1, (int) x2, (int) y2);
			g.drawLine( (int) x1, (int) y1, (int) ad[0], (int) ad[1]);
			if (s < 1.0D)
			{
				return;
			}
			else
			{
				double ay1[] = new double[4];
				AffineTransform affinetransform = AffineTransform.
					getRotateInstance(
					ROTATED, x2, y2);
				affinetransform.transform(ad, 0, ay1, 0, 1);
				affinetransform = AffineTransform.getRotateInstance( -
					ROTATED, x2, y2);
				affinetransform.transform(ad, 0, ay1, 2, 1);
				int ai[] =
					{
					(int) x2, (int) ay1[0], (int) ay1[2]
				};
				int ai1[] =
					{
					(int) y2, (int) ay1[1], (int) ay1[3]
				};

				if (fillc != null)
				{
					g.setColor(fillc);
					g.fillPolygon(ai, ai1, 3);
				}
				g.setColor(linec);
				g.drawPolygon(ai, ai1, 3);
				return;
			}
		}
		finally
		{
			g.setColor(oldc);
		}
	}

	private Polygon calLinkPolygon(double x1, double y1, double x2,
								   double y2)
	{
		double d4 = ARRAWLEN;
		double s = Math.sqrt( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		double ad[] =
			{
			x2 - (d4 / s) * (x2 - x1), y2 - (d4 / s) * (y2 - y1)
		};

//		int fx = (int) x1;
//		int fy = (int) y1;
//		int tox = (int) ad[0];
//		int toy = (int) ad[1];

		if (s < 1.0D)
		{
			return null;
		}

		double ay1[] = new double[4];
		AffineTransform affinetransform = AffineTransform.
			getRotateInstance(
			ROTATED, x2, y2);
		affinetransform.transform(ad, 0, ay1, 0, 1);
		affinetransform = AffineTransform.getRotateInstance( -
			ROTATED, x2, y2);
		affinetransform.transform(ad, 0, ay1, 2, 1);

//		int h = Math.abs(fy - toy);
//		int w = Math.abs(fx - tox);
//		int[] xps = new int[7];
//		int[] yps = new int[7];
//		if (h > w)
//		{ //左右位移计算
//			xps[0] = fx - 1;
//			yps[0] = fy;
//			xps[1] = tox - 1;
//			yps[1] = toy;
//			xps[2] = tox + 1;
//			yps[2] = toy;
//			xps[3] = fx + 1;
//			yps[3] = fy;
//		}
//		else
//		{ //上下位移计算
//			xps[0] = fx;
//			yps[0] = fy - 1;
//			xps[1] = tox;
//			yps[1] = toy - 1;
//			xps[2] = tox;
//			yps[2] = toy + 1;
//			xps[3] = fx;
//			yps[3] = fy + 1;
//		}
//
//		xps[4] = (int) x2;
//		xps[5] = (int) ay1[0];
//		xps[6] = (int) ay1[2];
//
//		yps[4] = (int) y2;
//		yps[5] = (int) ay1[1];
//		yps[6] = (int) ay1[3];
//
//		return new Polygon(xps, yps, 7);

		int[] xps = new int[4];
		int[] yps = new int[4];
		xps[0] = (int) x1;
		yps[0] = (int) y1;
		xps[1] = (int) ay1[0];
		yps[1] = (int) ay1[1];
		xps[2] = (int) x2;
		yps[2] = (int) y2;
		xps[3] = (int) ay1[2];
		yps[3] = (int) ay1[3];

		return new Polygon(xps, yps, 4);
	}

	public static Point[] truncateLine(double x0, double y0, double r0,
									   double x1, double y1, double r1)
	{
		double s = Math.sqrt( (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
		if (s < r0 || s < r1)
		{
			return null;
		}

		double d = (x1 - x0) / s;

		double xa = d * r0 + x0;
		double xb = d * (s - r1) + x0;

		d = (y1 - y0) / s;
		double ya = d * r0 + y0;
		double yb = d * (s - r1) + y0;

		Point pa = new Point( (int) xa, (int) ya);
		Point pb = new Point( (int) xb, (int) yb);

		return new Point[]
			{
			pa, pb};
	}

	public void drawingNormal(Graphics g)
	{
		if (fromp == null || top == null)
		{
			return;
		}

		Color oldc = g.getColor();
		//g.setColor(Color.blue);
		//g.drawLine(fromp.x, fromp.y, top.x, top.y);
		drawLink(g, fromp.x, fromp.y, top.x, top.y, Color.red, null);
		g.setColor(oldc);
	}

	public void drawingSelected(Graphics g)
	{

	}

	public Polygon[] getDrawingPolygons(Graphics g)
	{
		return null;
	}
}