package com.dw.system.xmldata.join;

import java.util.ArrayList;

import com.dw.system.xmldata.XmlData;

/**
 * XmlData�ӿڵĶ�������,�ñ�оƬ�ķ�װ���� �������ӿڵĶ���,�����ݴ洢ʵ���Ķ���
 * 
 * ʵ�ָýӿڵ����ܹ�ָ�����ܹ�֧�ֵ����ݽӿ�
 * 
 * ��������join�ӿڵĶ���ʵ����Ҫ������Ҫ��Ը��� 1,Ϊ֧�ֶ��Ƽ̳и���ʵ�ֲ�ͬ�ĵ�Ԫ,Ϊ�����ṩ�ӿ�����
 * 2,������ʱ,���ض���ʵ����(�о�������),ͨ���̳��ڲ�InsXXX�� ʵ�ֲ�ͬ�ӿ�(ֻ���ݽӿ�����)�ľ������
 * 
 * @author Jason Zhu
 */
public interface JoinUnit
{
	/**
	 * Unit����
	 * 
	 * @return
	 */
	public String getJoinUnitName();

	/**
	 * ������е�XmlDataInterface
	 * 
	 * @return
	 */
	public ArrayList<JoinInterface> getAllJoinInterfaces();

	/**
	 * ���ݽӿ����ƻ�ö�Ӧ�Ľӿڶ���
	 * 
	 * @param name
	 * @return
	 */
	public JoinInterface getJoinInterface(String name);
}
