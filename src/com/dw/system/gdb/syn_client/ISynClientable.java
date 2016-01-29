package com.dw.system.gdb.syn_client;

import java.util.*;
import com.dw.system.xmldata.*;

/**
 * XORM定义的类，如果实现此接口，则表明对应的表支持非一致终端协同应用
 * 
 * GDB在建表过程中，会自动的建立一个相关的log表，用来记录对应表的变化
 * 
 * 实现该接口，是为了向终端提供非一致协同的
 * @author Jason Zhu
 */
public interface ISynClientable
{
	/**
	 * 获得同步终端区分id的列名称
	 * 之所以有多个，是因为一个数据库表有可能要同时支持多种不用意义的终端
	 * 比如：指令接收表，就需要有sender和recver
	 * 	sender的记录添加，要支持recver的log
	 *  而recver的回复处理，要同时支持sender的log
	 * 如果终端id对应的列有多个，则每次记录的更新，就会根据此记录中的对应clientid列值，自动
	 * 	记录多个log
	 * @return
	 */
	public String[] getSynClientClientIdColNames() ;
	
	/**
	 * 获得终端非一致同步的表名词
	 * @param client_col 根据终端列名词，获得对应的终端表名词
	 * @return
	 */
	public String getSynClientTableName(String client_col) ;
	
	/**
	 * 根据数据库表名称或的对应的终端列名称
	 * @param tablen
	 * @return
	 */
	public String getSynClientColByTableName(String tablen);
	
	/**
	 * 根据输入的终端id列名称，获得本对象对应的终端id值
	 * 获得当前对象，所对应记录的终端id具体值
	 * @return
	 */
	public String getSynClientIdByColName(String client_col) ;
	
	/**
	 * 获得当前对象，所对应记录的主键id
	 * @return
	 */
	public String getSynClientPkId() ;
	
	/**
	 * 获得终端非一致同步的结构
	 * 此结构用来定义终端存储的表结构信息，大部分情况下，此结构与服务器的数据结构不同
	 * 如：发送的指令，实现此接口的类并不包含指令的内容，但有接收id。但终端里面必须有
	 * 	此内容，所以输出此内容，必须通过实现{@see getSynClientData}完成
	 * @param client_col 对应的终端列，这样不同的列，可以为不同类型的终端提供不同的
	 * 	表结构
	 * @return
	 */
	public XmlDataStruct getSynClientStruct(String client_col) ;
	
	/**
	 * 根据终端输入id，获得本表的对应终端记录总数
	 */
	public int getSynClientTableRecordNum(String client_col,String clientid)
		throws Exception;
	
//	/**
//	 * 根据输入的终端列和终端id值，从变化日志中，按照顺序
//	 * @param clientid
//	 * @return
//	 */
//	public List<String> readSynClientIdInChgLog(String client_col,String clientid) ;
	
	/**
	 * 根据输入的终端主键id，获得对应的终端同步数据列表
	 * 该方法，和log对应的id配合使用
	 * @param client_pkids
	 * @return 如果返回null或出错，表示失败。否则，返回数据列表，如果列表中
	 * 	不存在某一个client_pk对应的值，表示没有这条记录，终端应该删除对应的pkid记录
	 * 	才能算同步
	 */
	public List<XmlData> getSynClientData(String client_col,String clientid,List<String> client_pkids) throws Exception ;
	
	
	/**
	 * 根据输入的上一次主键id，按顺序获得大于此主键id的，后续记录
	 * 同时，记录数不能操作最大数量
	 * 
	 * @param client_col 终端列
	 * @param clientid
	 * @param last_pkid
	 * @param max_num
	 * @return
	 */
	public List<XmlData> getSynClientData(String client_col,String clientid,String last_pkid,int max_num) throws Exception ;
	
	/**
	 * 当终端增加记录时，向服务器发请求，最终会调用此方法
	 * 思路1：由终端生成id，以避免出现服务添加成功，终端不知道，而需要重复发送 - X
	 * 思路2：由服务器生成id，并通知终端，如果出现服务器成功，终端失败，可以通过服务器
	 * 		同步，让终端自动添加。此思路还允许，通过服务器提供的web界面发送信息，并且能够
	 * 		自动同步到对应的终端。--OKOK
	 * @param client_pkid 终端生成的唯一id,之所以
	 * @param xd 
	 * @return 如果成功，则返回对应的XmlData记录，终端应该根据此记录添加本地记录
	 * 	服务器端有可能通过非一致计算，调整一些内容给终端。
	 */
	public XmlData onSynClientAdd(String client_col,String clientid,XmlData xd)
	 throws Exception;
	
	/**
	 * 当终端修改记录时，向服务器发送更新请求
	 * @param client_pkid
	 * @param xd
	 * @return
	 */
	public XmlData onSynClientUpdate(String client_col,String clientid,String client_pkid,String[] update_cols,XmlData xd)
	 throws Exception;
	
	/**
	 * 当终端删除记录时，向服务器发送删除请求
	 * @param clientid
	 * @param client_pkid
	 */
	public void onSynClientDelete(String client_col,String clientid,String[] client_pkid)
	 throws Exception;
}
