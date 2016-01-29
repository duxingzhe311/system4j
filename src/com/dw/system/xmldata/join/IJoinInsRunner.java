package com.dw.system.xmldata.join;

import com.dw.system.xmldata.XmlData;

/**
 * ֧������ʱʵ��
 * 
 * @author Jason Zhu
 */
public interface IJoinInsRunner
{

	/**
	 * ����ӿ�����֧������,in,inout,����ø÷���
	 * @param ju ���Unit
	 * @param ji ��ԵĽӿ�
	 * @param in_data ������϶�Ӧ�ӿڵ�����
	 */
	public void onPulseInData(JoinUnit ju,JoinInterface ji,XmlData in_data)
	throws Exception;

	/**
	 * 
	 * @param ju
	 * @param ji
	 * @return ������϶�Ӧ�ӿڵ�����
	 */
	public XmlData onPulseOutData(JoinUnit ju,JoinInterface ji)
	throws Exception;

//	protected abstract XmlData onPulseInOutData(JoinInterface ji,
//			XmlData in_data);
}