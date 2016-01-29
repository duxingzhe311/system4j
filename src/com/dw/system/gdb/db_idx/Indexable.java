package com.dw.system.gdb.db_idx;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface Indexable
{
    /**
     * ����һ���յ���������
     * @return
     */
    public IndexItem createEmptyIndex();

    /**
     * �õ����ݿ��Ĵ������,�����԰�������������
     * @return
     */
    public String[] getDBCreationSqls();

    /**
     * �õ����ݿ������
     * @return
     */
    public String getDBTableName();

    /**
     * �õ��ؼ���,���������ֲ�ͬ��������
     * @return
     */
    public String getDBKeyColumn();
    
    /**
     * �ж�����ֵ�Ƿ��Զ�����
     * @return
     */
    public boolean isDBKeyAutoCreation();

    /**
     * �õ����е����ݿ�������,���˹ؼ���
     * @return
     */
    public String[] getDBNorColumns();
    
    

}