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
	 * �������ɵ��¼�
	 * @param event XEvent�¼�����
	 */
	public void onEvent(XEvent event);

	/**
	 * ��������
	 * @return ������Ϣ
	 */
	public String getListenerDesc();
}