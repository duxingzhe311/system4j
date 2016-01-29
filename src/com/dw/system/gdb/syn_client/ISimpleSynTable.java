package com.dw.system.gdb.syn_client;

import java.util.*;

import com.dw.system.xmldata.*;

/**
 * 支持终端简单表同步的实现接口
 * 
 * 终端所有的存放表结构都相同 pkid , up_dt, xd_cont
 * 
 * 服务器端通过up_dt来和终端进行同步操作
 * 
 * @author Jason Zhu
 *
 */
public interface ISimpleSynTable
{
	/**
	 * 获得数据表名称
	 * @return
	 */
	public String SST_getTableName() ;
	
	/**
	 * 根据上一次更新时间，获得之后的更新id和时间
	 * <b>
	 * 该方法实现时，仅仅考虑时间变化的记录，不考虑状态，不进行过滤，其原因是有可能终端对应的
	 * 记录需要做删除——配合SST_readItemsDetail不返回内容即可
	 * </b>
	 * @param clientid
	 * @param last_dt 
	 * @return
	 */
	public List<SynItem> SST_readIdAndDTAfter(String clientid,long last_dt)
		throws Exception;
	
	/**
	 * 当终端初始化时，直接根据终端id获得此终端id最终有效的id和更新时间
	 * <b>该方法实现时，要考虑不需要输出到终端的状态，进行过滤</b>
	 * @param clientid
	 * @return
	 * @throws Exception
	 */
	public List<SynItem> SST_readIdAndDTValidAll(String clientid)
		throws Exception;
	/**
	 * 根据主键id获取,相关的同步项内容
	 * 要求终端估计每个记录的大小，控制每次获取的数量
	 * 
	 * 如果某一个pkid对应的SynItem不存在，则终端做删除
	 * @param pkid
	 * @return
	 */
	public List<SynItem> SST_readItemsDetail(String clientid,String[] pkid)
		throws Exception;
	
	/**
	 * 支持终端新增的方法
	 * @param add_t
	 * @param xd
	 * @return
	 */
	public SynItem SST_onClientAdd(String clientid,String add_t,XmlData xd)
		throws Exception;
	
	/**
	 * 支持终端更新的方法
	 * @param update_t 更新类型
	 * @param pkid
	 * @param xd
	 * @return 返回的SynItem,需要根据具体情况填写updateRes成员
	 * 	用来控制服务器对终端的应答
	 */
	public SynItem SST_onClientUpdate(String clientid,String update_t,String pkid,XmlData xd)
		throws Exception;
	
	/**
	 * 支持终端删除的方法
	 * @param pkid
	 */
	public boolean SST_onClientDelete(String clientid,String pkid)
		throws Exception;
}
