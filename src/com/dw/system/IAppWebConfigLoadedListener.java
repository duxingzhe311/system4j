package com.dw.system;

/**
 * Ϊ�ڲ������������ mail��mconn�ȣ�
 * �ṩ����Ӧ�ù���������װ�سɹ�֮���һ��֪ͨ�ӿڡ�
 * 
 * ʵ�ִ˽ӿڵ������ʵ��IServerBootComp�ӿ�
 * @author Jason Zhu
 *
 */
public interface IAppWebConfigLoadedListener
{
	public void onAppWebConfigLoaded(AppWebConfig awc,ClassLoader comp_cl) ;
}
