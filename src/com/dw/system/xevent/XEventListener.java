package com.dw.system.xevent;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: CssWeb</p>
 * @author Jason Zhu
 * @version 1.0
 */

public interface XEventListener
{
	/**
	 * 处理被分派的事件
	 * @param event XEvent事件对象
	 */
	public void onEvent(XEvent event);

	/**
	 * 处理描述
	 * @return 描述信息
	 */
	public String getListenerDesc();
}