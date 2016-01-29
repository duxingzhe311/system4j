/*
 * Created on 2004-7-28
 *
 * Copyright (c) Jason Zhu
 */
package com.dw.system.graph;



/**
 * 
 * @author Jason Zhu
 *
 * Desc:
 */
public interface DrawController
{
	public static final short THIS_NORMAL = 0;
	public static final short THIS_SELECTED = 1;
	public static final short THIS_HIDDEN = 2;
	public static final short THIS_BLINK = 3;

	public static final short THIS_OFFSPRING_NORMAL = 4;
	public static final short THIS_OFFSPRING_SELECTED = 5;
	public static final short THIS_OFFSPRING_HIDDEN = 6;
	public static final short THIS_OFFSPRING_BLINK = 7;

	
	
	/**
	 * �õ�����״̬
	 * @param adn ��ؽڵ�
	 * @return ״̬��Ϣ
	 */
	public short getDrawingState(AbstractDrawNode adn);

	/**
	 * �жϸÿ������Ƿ�Ҫ���̳�
	 * @return true��ʾ�ӽڵ�Ҳ����ʹ�øÿ�����
	 */
	public boolean isInherited();
}