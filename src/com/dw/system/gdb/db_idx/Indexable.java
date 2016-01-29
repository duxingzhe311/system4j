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
     * 创建一个空的索引对象
     * @return
     */
    public IndexItem createEmptyIndex();

    /**
     * 得到数据库表的创建语句,它可以包含索引等内容
     * @return
     */
    public String[] getDBCreationSqls();

    /**
     * 得到数据库表名称
     * @return
     */
    public String getDBTableName();

    /**
     * 得到关键列,它用来区分不同的索引项
     * @return
     */
    public String getDBKeyColumn();
    
    /**
     * 判断主键值是否自动生成
     * @return
     */
    public boolean isDBKeyAutoCreation();

    /**
     * 得到所有的数据库列名称,除了关键列
     * @return
     */
    public String[] getDBNorColumns();
    
    

}