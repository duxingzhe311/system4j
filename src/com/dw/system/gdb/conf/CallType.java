package com.dw.system.gdb.conf;

public enum CallType
{
    /// <remarks/>
    sql,
    
    /// <remarks/>
    pro,
    
    //  ����gdb�������ݿ�ʱ��ͬ���ݿ������ͬ��Ϊ��ʹxml����
    //�ļ��ܹ������ڲ�ͬ���ݿ��У���Ҫ��һЩ������Բ�ͬ�����ݿ������ͬ�����
    //�룬insertһ����¼����Ҫ����µ�id�����sqlserver - select @@IDENDITY
    //��derbyʹ�� values IDENTITY_VAL_LOCAL()
    //���ԣ�exe_type=auto_fit����£���Ҫָ��һ��gdb�ڲ��Դ���sql�����Ϣ
    //�磺����Ĳ�������Ϊ select_identity
    auto_fit,
}
