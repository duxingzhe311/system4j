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
     * 只提供给key为string的对象使用
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
     * 根据数据库表的列名称,获得对应的值.<br>
     * 它用来对数据库进行插入或更新操作
     * @param colname
     * @return JDBC能够接收的值
     */
    abstract public Object getValueByColumn(String colname) ;

    /**
     * 设置bean的值,当通过jdbc获得列值以后,用来对对象进行值的设置
     * @param colname 数据库列名
     * @param o jdbc值
     */
    abstract public void setValueByColumn(String colname,Object o) ;

    public String[] getAlarmInfo()
    {
        return null ;
    }
}