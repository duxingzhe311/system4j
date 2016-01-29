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
	 * 得到控制状态
	 * @param adn 相关节点
	 * @return 状态信息
	 */
	public short getDrawingState(AbstractDrawNode adn);

	/**
	 * 判断该控制器是否要被继承
	 * @return true表示子节点也可以使用该控制器
	 */
	public boolean isInherited();
}