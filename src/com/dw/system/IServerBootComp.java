package com.dw.system;

/**
 * 服务器引用软件启动组件接口
 * 
 * 该接口用来定义服务器要外接的组件
 * 
 * 如:把tomcat作为一个服务器组件同时启动. 则应该把Tomcat的启动实现该接口
 * 
 * 这样就可以控制tomcat的启动和停止
 * 
 * 
 */
public interface IServerBootComp {
	String getBootCompName();

	void startComp() throws Exception;

	void stopComp() throws Exception;

	boolean isRunning() throws Exception;
}
