package com.dw.system.gdb.syn;

import com.dw.system.gdb.DataTable;

/**
 * 对于服务器更新的表，有可能Server端使用的是原有的一些数据。
 * 可能需要根据一些模块，模拟出相关的虚拟表信息，以提供给Proxy对应的表进行更新
 * 
 * @author Jason Zhu
 */
public interface ISynVirtualServerTable
{
	/**
	 * 更新时间戳
	 * @param proxyid
	 * @return
	 */
	public long getSynUpdateTimestamp(String proxyid) ;
	
	/**
	 * 
	 * @param proxyid
	 * @param lastmax_pk
	 * @param read_maxnum
	 * @return
	 */
	public DataTable readSynUpdateDT(String proxyid,Object lastmax_pk,int read_maxnum) ;  
}
