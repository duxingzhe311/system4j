package com.dw.system.gdb.xorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来对XORMClass指定<br/>
 * <br/>
 * 在gdb_XXX.xml文件需要指定如下的内容,例如<br/>
 * &lt;XORM name="#CN11" class="com.dw.comp.app_case_mgr.CXxItem"/&gt;<br/>
 * <br/>
 * XORM定义了名称之后,里面隐藏了如下变量,可以被本xml文件中的Sql语句定义时使用<br/>
    <b>#CN11.all_cols</b> //所有的列(包含主键和xorm_ext列)<br/>
    <b>#CN11.pk_col</b> //主键列<br/>
    <b>#CN11.nor_cols</b> //普通列(不包含主键和xorm_ext列的has_col=true列)<br/>
    <b>#CN11.pk_nor_cols</b> //主键列和普通列<br/>
    <b>#CN11.table_name</b> //表名称<br/>
    <br/>
    例如:<br/>
    select #CN11.pk_nor_cols from #CN11.table_name where c1=? and ...<br/>
    该语句在运行前会被自动转换为<br/>
    select id,c1,c2 from t1 where c1=? and ...<br/>
    <br/>
    除了这些变量之外,如果要做更复杂的sql,则必须知道XORM相关类的定义<br/>
     如更新部分的普通列等.<br/>
     
 * @author Jason Zhu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XORMClass {
	
	/**
	 * 数据库表名称
	 * @return
	 */
	String table_name();
	
	/**
	 * 如果=true,则表明对应的类的被继承父类中的XORM定义内容XORMProperty会被继承过来使用
	 * 
	 * @return
	 */
	boolean inherit_parent() default false;
	
	/**
	 * 标题
	 * @return
	 */
	String title() default "" ;
	/**
	 * 指定主类
	 * 如果一个类定义中指定了主类,那么里面可以存在一个域有属性fk_base_class=true,
	 * 该域的使用主类中的主键作为外键关联
	 * --该属性为类的继承提供了自动适应主键外键关联的定义
	 * @return
	 */
	Class base_class() default Object.class;
	
	/**
	 * 如果=0 表示不支持分布式
	 * 
	 * 如果=1，这表明类对应的表支持分布式运行，并且运行在模式1下
	 * 	1，在分布式环境的局部端（Proxy端），数据库表的建立和普通表类似，但不允许有外键关联关系
	 * 	2，在分布式环境的服务器端（Server端），数据库表的建立会自动多一个_ProxyId列，并且普遍的
	 * 		自动列被处理成普通列
	 * 	3，分布式运行过程中，本表只能在Proxy端进行更新，每个Proxy端记录本地运行的更新log，并向服务器端
	 * 		发送更新日志，服务器自动根据_ProxyId进行同步。这样服务器端会通过同步记录全部Proxy端的数据。
	 * 	4，在Proxy初始阶段，Proxy在建立对应的表之后，会向服务器端发送“下载”数据请求，可以使得服务器中
	 * 		对应的_ProxyId所有的数据下载到Proxy本地
	 * 
	 * 如果=2,这表明类对应的表支持分布式运行，并且运行在模式2下
	 * 	1，在分布式环境下，服务器端和Proxy端的表结构相同，每一行都会有一个唯一主键列和更新时间戳列。
	 * 	2，数据库表只能在服务器端更新更新
	 * 	3，每个Proxy端会定时与服务器端进行数据同步，Proxy首先下载服务器端对应表的主键id和更新时间
	 * 		列表，根据本地情况，请求更新的行信息，删除已经不存在的id--这要求数据表最好不要太大
	 * 
	 * 
	 * @return
	 */
	int distributed_mode() default 0;
	
	/**
	 * 本表支持终端同步
	 * 需要对此表进行终端同步更新日志记录
	 * GDB在建立表的过程中，会自动的增加xxx_syn_client_log表
	 * 并在此表的使用过程中，自动记录变化log
	 * 此表必须有一个而且只有一个唯一主键
	 * @return
	 */
	boolean support_syn_client() default false;
	
	/**
	 * 指定本表对于的终端id列
	 * @return
	 */
	String syn_client_client_id_col() default "" ;
	
	/**
	 * 指定本表对应的终端唯一id列
	 * @return
	 */
	String syn_client_pk_col() default "" ;
	
	
}
