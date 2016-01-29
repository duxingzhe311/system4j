package com.dw.system;

/**
 * 为内部服务组件（如 mail，mconn等）
 * 提供监听应用构件被发现装载成功之后的一个通知接口。
 * 
 * 实现此接口的类必须实现IServerBootComp接口
 * @author Jason Zhu
 *
 */
public interface IAppWebConfigLoadedListener
{
	public void onAppWebConfigLoaded(AppWebConfig awc,ClassLoader comp_cl) ;
}
