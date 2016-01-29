package com.dw.system.graph;

import java.awt.Color;
import java.awt.Graphics;

public class Bspline
{
//	Image buffImage;
	Graphics	buffGraphics;
	int			n	= 1, k = 2, n1, nt, Tmin, Tmax,  w2;
	//int h0, h10,w0,; 
	double[]	Px, Py, ti;
	double		N[][];
	Color		col[];

	public Bspline(Graphics g, double[] pxs, double[] pys)
	{
		buffGraphics = g;
		init1(pxs, pys);
	}

	public void init1(double[] pxs, double[] pys)
	{
		//w = 150;
		//h = 400;
		//h1 = h - 1;
		w2 = 100;
		n = 3;
		k = 3;
		n1 = n + 1; //4
		
		nt = n + k + 1; //3+3+1
		//Px = new double[n1];
		//Py = new double[n1];

		Px = pxs;
		Py = pys;

		ti = new double[nt + k];
		col = new Color[w2];
		N = new double[nt + 1][w2];

		ti[0] = 0;
		ti[1] = 1;
		ti[2] = 2;
		ti[3] = 3;
		ti[4] = 4;
		ti[5] = 5;
		ti[6] = 6;
	}

	public void drawFun()
	{
		double step = (ti[nt - 1] - ti[0]) / (w2 - .9);
		double t = ti[0];
		
		Color[] iColor = {Color.red, new Color(0f, .7f, 0f), Color.blue,
				Color.magenta, new Color(0f, .8f, .8f),
				new Color(.9f, .9f, 0f), Color.gray};
		//buffGraphics.clearRect(0, 0, w, h);
		Tmin = (int) ((ti[k - 1] - ti[0]) / step) + 1;
		Tmax = (int) ((ti[n1] - ti[0]) / step);
		System.out.println("Tmin="+Tmin);
		System.out.println("Tmax="+Tmax);
		System.out.println("Step="+step);
		int i1 = 0;
		for (int l = 0; l < w2; l++)
		{
			while (t >= ti[i1])
				i1++;
			int i = i1 - 1;
			col[l] = iColor[(i + 8 - k) % 7];
			for (int j = 0; j < nt; j++)
				N[j][l] = 0;
			N[i][l] = 1;

			for (int m = 2; m <= k; m++)
			{ //  basis functions calculation
				int jb = i - m + 1;
				if(jb < 0)
					jb = 0;
				for (int j = jb; j <= i; j++)
				{
					N[j][l] = N[j][l] * (t - ti[j]) / (ti[j + m - 1] - ti[j])
							+ N[j + 1][l] * (ti[j + m] - t)
							/ (ti[j + m] - ti[j + 1]);
				}
			}

			t += step;
		}
		for (int j = 0; j < n1; j++)
		{
			buffGraphics.setColor(iColor[j % 7]);
			t = ti[0];
			int to = (int) t;
			for (int l = 1; l < w2; l++)
			{
				t += step;
				int t1 = (int) t;
				//buffGraphics.drawLine(to, h1-(int)(h1*N[j][l-1]),t1,
				// h1-(int)(h1*N[j][l]) );
				to = t1;
			}
		}
		for (int l = k; l <= n1; l++)
		{
			//buffGraphics.setColor(iColor[(l - k) % 7]);
			// buffGraphics.drawLine((int)ti[l-1], 1, (int)ti[l], 1);
		}
		buffGraphics.setColor(Color.black);
		for (int l = 0; l < nt; l++)
		{
			//buffGraphics.drawRect((int) ti[l] - 1, 0, 3, 3);
		}
	}

	public void drawSpline()
	{
		int X, Y;
		
		double sX = 0, sY = 0;
		for (int j = 0; j < n1; j++)
		{
			sX += Px[j] * N[j][Tmin];
			sY += Py[j] * N[j][Tmin];
		}
		int Xold = (int) sX, Yold = (int) sY;
		for (int k = Tmin + 1; k <= Tmax; k++)
		{
			sX = 0;
			sY = 0;
			for (int j = 0; j < n1; j++)
			{
				sX += Px[j] * N[j][k];
				sY += Py[j] * N[j][k];
			}
			X = (int) sX;
			Y = (int) sY;
			buffGraphics.setColor(col[k]);
			if((X < w2) && (Xold < w2))
				buffGraphics.drawLine(Xold, Yold, X, Y);
			Xold = X;
			Yold = Y;
		}
	}

	public void drawMe()
	{
		drawFun();
		drawSpline();
	}
}
