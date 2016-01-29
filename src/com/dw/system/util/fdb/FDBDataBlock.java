package com.dw.system.util.fdb;

/**
 * 数据块
 * 数据格式如下
 * 
 * 1字节标签-8字节数据长度-41字节索引key-由8字节数据长度的数据
 * 可以通过扫描建立索引
 * 其中标签字节0-表示数据块为空内容，以支持删除
 * @author Jason Zhu
 */
public class FDBDataBlock
{
	FDBDataHeader header = null ;
	
	FDBDataBlock()
	{
		
	}
	
	/**
	 * 存储的数据
	 * @param dh
	 * @param cont
	 */
	public FDBDataBlock(FDBDataHeader dh,byte[] cont)
	{
		
	}
}
