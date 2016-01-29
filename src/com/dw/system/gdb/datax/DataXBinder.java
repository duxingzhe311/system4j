package com.dw.system.gdb.datax;

import java.util.*;

import com.dw.system.xmldata.IXmlDataable;

/**
 * DataX�����֧��
 * 
 * ��DataXΪ����,Ϊ����ʹ��DataX�����ṩ���ٿ���Ӧ�õ�֧��.
 * 
 * �������Ӧ��Ҫʹ��DataX,��ֻ��Ҫʵ�ֵ���صĽӿںͷ��Ϲ涨��ģʽ
 * 1,DataX�����ݱ���
 * 2,DataX��Ӧ��Seperate��Ϣ����ͨ�����������ݻ��
 * 
 * @author Jason Zhu
 */
public class DataXBinder
{
	public static interface IBinderStub extends IXmlDataable
	{
		public long getDataXId();
		
		public void setDataXId(long xdid);
	}
	
	/**
	 * һ�������ʵ�ָýӿ�,��˵�����Ĺ�����Ҫ��DataX�л�ȡ����������
	 * 
	 * @author Jason Zhu
	 *
	 */
	public static interface IBinderFull extends IXmlDataable
	{
		public long getDataXId();
		
		public void setDataXId(long xdid);
	}
	
	public List<IBinderStub> listBinderStubs()
	{
		return null ;
	}
	
	public IBinderFull getBinderFull(long xdid)
	{
		return null ;
	}
}
