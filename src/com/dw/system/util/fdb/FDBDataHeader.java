package com.dw.system.util.fdb;

/**
 * 数据块的头信息
 * @author Jason Zhu
 *
 */
public class FDBDataHeader
{
	/**
	 * 起始位置
	 */
	long startPos = -1 ;
	
	/**
	 * 数据块标记位
	 */
	byte dataTag = -1 ;
	/**
	 * 正文数据长度
	 */
	long dataLen = -1 ;
	
	//1cfbdbe0-5 0c8-4f9e-9 d86-bc40b5 d740f1
	/**
	 * 1cfbdbe0-5 0c8-4f9e-9 d86-bc40b5 d740f1
	 * 1000000001
	 * 
	 * 索引key，使用UTF8编码后，长度不能超过40
	 */
	String idxKey = null ;
	
	/**
	 * 根据key从小到大排序支持双向链接前置
	 */
	FDBDataHeader prevHeader = null ;
	
	FDBDataHeader nextHeader = null ;
}
