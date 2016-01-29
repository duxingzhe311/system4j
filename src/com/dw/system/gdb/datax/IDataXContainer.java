package com.dw.system.gdb.datax;

/**
 * Ϊ֧��DataX�еĶ�����Ϣ�ܹ���Զ�̵�Ӧ��ʹ��,�����ܹ�ͳһ�ӿ�
 * �ض����˸ýӿ�
 * 
 * �ýӿ������˻�ȡDataX�е������Ϣ�ķ���
 * 
 * ���һ��Զ��Ӧ�����ȡIDataXContainer�е���Ϣ,���ڲ��ͱ������
 * һ��IDataXContainer�ľ���ʵ�ֵ�ʵ��. �����Ϳ���ͳһʹ��DataX�е���Ϣ
 * 
 * @author Jason Zhu
 */
public interface IDataXContainer
{
	public DataXBase[] getAllDataXBase();
	
	public DataXBase getDataXBaseByName(String name);
	
	public DataXClass[] getAllDataXClassesInBase(String basen) ;
	
	public DataXClass getDataXClass(String basen,String classn);
}
