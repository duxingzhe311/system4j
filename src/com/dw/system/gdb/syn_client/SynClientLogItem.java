package com.dw.system.gdb.syn_client;

import com.dw.system.gdb.xorm.*;

/**
 * 终端非一致同步log
 * 
 * 服务器根据外界业务的修改，自动做相关记录的更新-如果表名称，clientid和pkid重复
 * 	则只做时间的更新
 * 终端，通过读取此log,获取修改的对于表的pkid，向后续的其他接口获得具体的记录值做本地的
 * 	增加、更新或删除。
 * 并返回成功的SynTableName，clientid和chgDt，pkid，如果服务器log存在对应的记录，则删除
 *   如果不存在，则基本上是chgDT被修改。需要终端再次做更新
 * 
 * @author Jason Zhu
 *
 */
@XORMClass(table_name="syn_client_log")
public class SynClientLogItem
{
	/**
	 * 被同步的表名词
	 */
	@XORMProperty(name="SynTableName",max_len=15,has_col=true,has_idx=true,order_num=10)
	String synTableName = null ;
	
	/**
	 * 终端id,也可能是终端的用户名
	 */
	@XORMProperty(name="ClientId",max_len=15,has_col=true,has_idx=true,order_num=20)
	String clientId = null ;
	
	/**
	 * 变化时间
	 */
	@XORMProperty(name="ChgDT",has_col=true,has_idx=true,order_num=30)
	long chgDt = -1 ;
	
	/**
	 * 业务数据表相关记录id
	 */
	@XORMProperty(name="PkId",max_len=15,has_col=true,has_idx=true,order_num=40)
	String pkId = null ;
	
	public String getSynTableName()
	{
		return synTableName ;
	}
	
	public String getClientId()
	{
		return clientId ;
	}
	
	public long getChgDT()
	{
		return chgDt;
	}
	
	public String getPkId()
	{
		return pkId ;
	}
}
