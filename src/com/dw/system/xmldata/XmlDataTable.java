package com.dw.system.xmldata;

import java.util.*;
/**
 * 在很多情况下,输出的数据是个表结构,其中包含表头,表的行列信息.
 * 
 * 如果数据是翻页的一部分,则还有数据页面信息
 * 
 * 表的每一行的结构都是相同的,可以对应XORM对象
 * 
 * 本类由DataTable替代
 * @author Jason Zhu
 */
public class XmlDataTable
{
	String tableName = null ;
	
	ArrayList<XmlData> rowDatas = new ArrayList<XmlData>() ;
	
	int pageIdx = 0 ;
	
	int pageSize = 20 ;
	
	//String pkColName
}
