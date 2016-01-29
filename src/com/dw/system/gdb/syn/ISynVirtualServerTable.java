package com.dw.system.gdb.syn;

import com.dw.system.gdb.DataTable;

/**
 * ���ڷ��������µı��п���Server��ʹ�õ���ԭ�е�һЩ���ݡ�
 * ������Ҫ����һЩģ�飬ģ�����ص��������Ϣ�����ṩ��Proxy��Ӧ�ı���и���
 * 
 * @author Jason Zhu
 */
public interface ISynVirtualServerTable
{
	/**
	 * ����ʱ���
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
