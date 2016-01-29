/*
 * Created on 2004-7-28
 *
 * Copyright (c) Jason Zhu
 */
package com.dw.system.graph;

import java.awt.*;

/**
 * 
 * @author Jason Zhu
 *
 * Desc:
 */
public interface Draggable
{
	/**
	 * 得到实际的位置坐标
	 * @return 位置信息
	 */
	public Point getPos();

	/**
	 * 设置实际的位置坐标
	 * @param x 横坐标
	 * @param y 纵坐标
	 */
	public void setPos(int x, int y);

	/**
		 * 绘制被拖到指定位置的绘画
		 * @param x 移动的位置横坐标
		 * @param y 移动的位置纵坐标
		 * @param g 绘画对象
		 */
	public void drawDragged(int movex,int movey,Graphics g) ;
}