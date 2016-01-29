package com.dw.system.queue;

public interface IObjHandler
{
	/**
	 * ʧ�ܵĳ��Դ���
	 * @return
	 * @throws Exception
	 */
	public int processFailedRetryTimes() ;
	
	/**
	 * ���³��Դ�����ӳ�-���ǵ�����ֻ��һ�����ݵ����,����ʵ
	 * ��Ҫ���������һ�����ִ�����Ҫ���´���������,�����һ����ʱ���ӳ�
	 * �����ӳٺͲ�ͬ��ʱ�����
	 * @return
	 */
	public long processRetryDelay(int retrytime) ;
	
	/**
	 * �����Ĵ������
	 * @param o
	 * @throws Exception
	 */
	public HandleResult processObj(Object o,int retrytime) throws Exception;
	
	/**
	 * ������������Ч�ĵȴ�ʱ��,��ʱ�䲻��̫��,�����Ӱ�촦���ٶ�
	 * @return
	 */
	public long handlerInvalidWait() ;
	/**
	 * ����һ�������³��Բ���,����ȷ����Ҫ���������ݴ���
	 * @param o
	 * @throws Exception
	 */
	public void processObjDiscard(Object o) throws Exception ;
}
