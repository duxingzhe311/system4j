package com.dw.net.dirqueue;

public enum HandleResult
{
    Succ,//����ɹ�-�����Ӷ�����ɾ��
    Failed_Retry_Later,//������ʧ��,��Ҫ�Ժ��ٴ���,������Ҫ�����ݷŵ����к���,���ȴ�һ����ʱ���ٴ���
    Handler_Invalid,//��������������-��Ҫ����в����κθı�
}
