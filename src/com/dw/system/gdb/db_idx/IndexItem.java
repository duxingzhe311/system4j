package com.dw.system.gdb.db_idx;

import java.io.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public abstract class IndexItem
{
    protected Object m_key = null ;

//    public void setPrimaryKey(IndexItem oii)
//    {
//        m_key = oii.m_key ;
//    }

    public Object getIndexKey()
    {
        return m_key ;
    }
    
    public void setIndexKeyVal(Object v)
    {
    	m_key = v ;
    }

    /**
     * ֻ�ṩ��keyΪstring�Ķ���ʹ��
     * @return
     */
    public String getPrimaryKey()
    {
        return (String)m_key ;
    }

    public boolean isValid()
    {
        if(m_key==null)
            return false ;

        return true ;
    }


    /**
     * �������ݿ���������,��ö�Ӧ��ֵ.<br>
     * �����������ݿ���в������²���
     * @param colname
     * @return JDBC�ܹ����յ�ֵ
     */
    abstract public Object getValueByColumn(String colname) ;

    /**
     * ����bean��ֵ,��ͨ��jdbc�����ֵ�Ժ�,�����Զ������ֵ������
     * @param colname ���ݿ�����
     * @param o jdbcֵ
     */
    abstract public void setValueByColumn(String colname,Object o) ;

    public String[] getAlarmInfo()
    {
        return null ;
    }
}