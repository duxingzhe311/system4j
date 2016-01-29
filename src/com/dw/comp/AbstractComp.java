package com.dw.comp;

import java.util.ArrayList;

import com.dw.web_ui.http.IHttpClientCmdHandler;

public abstract class AbstractComp
{
	public void doTest()
	{
		
	}
	
	public abstract String getCompName();
	
	public abstract ArrayList<CompAction> getActions() ;
	
	
	/**
	 * 如果Tomato构件中存在Http Client处理内容,则根据其名称就可以获得
	 * 
	 * 在控件客户端使用如下格式的命令 TC:CompName:hcc_name来定义
	 * @param hcc_name
	 * @return
	 */
	public IHttpClientCmdHandler getHttpClientCmdHandler(String hcc_name)
	{
		return null ;
	}
	
	/**
	 * 每个构件在被装载后，如果需要运行一些东西做测试，获得需要为容器管理
	 * 提供一些手段。应该在该方法中实现。
	 * 
	 * 例如，管理器可以通过调用该方法，来运行构件内部的一些方法等。
	 * @param args
	 */
	public abstract void doCmd(String[] args) throws Exception;
	
}
