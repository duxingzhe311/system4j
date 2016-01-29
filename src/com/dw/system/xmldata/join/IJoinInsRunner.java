package com.dw.system.xmldata.join;

import com.dw.system.xmldata.XmlData;

/**
 * 支持运行时实现
 * 
 * @author Jason Zhu
 */
public interface IJoinInsRunner
{

	/**
	 * 如果接口类型支持输入,in,inout,则调用该方法
	 * @param ju 针对Unit
	 * @param ji 针对的接口
	 * @param in_data 必须符合对应接口的数据
	 */
	public void onPulseInData(JoinUnit ju,JoinInterface ji,XmlData in_data)
	throws Exception;

	/**
	 * 
	 * @param ju
	 * @param ji
	 * @return 必须符合对应接口的数据
	 */
	public XmlData onPulseOutData(JoinUnit ju,JoinInterface ji)
	throws Exception;

//	protected abstract XmlData onPulseInOutData(JoinInterface ji,
//			XmlData in_data);
}