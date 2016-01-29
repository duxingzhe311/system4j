package com.dw.system.gdb.syn;

/**
 * 同一个表，不仅Proxy端可以更新，服务器端也可以更新
 * 并且所有Proxy端，通过Server端，进行着全局的同步
 * 
 * 1，每个Proxy端做的本地更新，都记录日志，并自动发送到服务器端（与mode1类似）
 * 2，Server端接收到日志，不仅做本地的更新，而且还做日志
 * 3，每个Proxy端定时向Server查看日志更新时间戳，并根据其值进行同步
 * 
 * 注：由于同步时间的滞后性，更新删除操作应该尽可能的在Proxy进行，并且Proxy端更新，尽可能的限定在
 * 自己的范围内
 * 
 * @author Jason Zhu
 */
public class PSCSynMode3
{
	
}
