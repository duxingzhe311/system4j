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
	 * �õ�ʵ�ʵ�λ������
	 * @return λ����Ϣ
	 */
	public Point getPos();

	/**
	 * ����ʵ�ʵ�λ������
	 * @param x ������
	 * @param y ������
	 */
	public void setPos(int x, int y);

	/**
		 * ���Ʊ��ϵ�ָ��λ�õĻ滭
		 * @param x �ƶ���λ�ú�����
		 * @param y �ƶ���λ��������
		 * @param g �滭����
		 */
	public void drawDragged(int movex,int movey,Graphics g) ;
}