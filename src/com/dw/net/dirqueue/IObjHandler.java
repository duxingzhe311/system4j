package com.dw.net.dirqueue;


public interface IObjHandler
{
    /**
     * ʧ�ܵĳ��Դ���
     * @return
     * @throws Exception
     */
    int processFailedRetryTimes();

    /**
     * ���³��Դ�����ӳ�-���ǵ�����ֻ��һ�����ݵ����,����ʵ
     * ��Ҫ���������һ�����ִ�����Ҫ���´���������,�����һ����ʱ���ӳ�
     * �����ӳٺͲ�ͬ��ʱ�����
     * @return
     */
    long processRetryDelay(int retrytime);

    /**
     * �����Ĵ������
     * @param o
     * @throws Exception
     */
    HandleResult processObj(Object o, int retrytime);

    /**
     * ������������Ч�ĵȴ�ʱ��,��ʱ�䲻��̫��,�����Ӱ�촦���ٶ�
     * @return
     */
    long handlerInvalidWait();
    /**
     * ����һ�������³��Բ���,����ȷ����Ҫ���������ݴ���
     * @param o
     * @throws Exception
     */
    void processObjDiscard(Object o);
}
