package com.dw.system.task;

/**
 * �������̹�������
 * 
 * 
 * �ܶ��ֳ���,��Ҫ����������������������ͬ���һЩ����
 * ����,������һ����7x24Сʱ����,����Щ��������Ҳ�����. ͬʱ,������Ӧ�ÿ�����ȫ
 * ���Ƹ������̵������͹ر�--Ҫ��������killʱ,Ҳ�ܹ�ʹ���������Զ�ֹͣ
 * 
 * ��������ҪΪ�������ṩ��һ��ͳһ���Ƹ������̵ķ���
 * �ؼ�Ҫ����:<b>Ҫ��������killʱ,Ҳ�ܹ�ʹ���������Զ�ֹͣ</b>
 * 
 * ��Server��,ÿ��webappӦ�ö�Ӧһ������,��������ж����������--ÿ�������Լ������Լ�����Ƶ��.
 * 
 * @author Jason Zhu
 */
public class ProManager
{
	private static Object locker = new Object() ;
	
	private static ProManager proMgr = null;
	
	public static ProManager getInstance()
	{
		if(proMgr!=null)
			return proMgr ;
		
		synchronized(locker)
		{
			if(proMgr!=null)
				return proMgr ;
			
			proMgr = new ProManager() ;
			return proMgr ;
		}
	}
	
	private ProManager()
	{}
	
	//public 
}
