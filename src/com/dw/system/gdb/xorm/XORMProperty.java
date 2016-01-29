package com.dw.system.gdb.xorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在类成员,或getXXX setXXX之上的标记
 * 其中,同一个标记对应getXXX或setXXX只需要标记其中一个即可
 * 一般来说,标记在Field成员就满足要求. 但在一些情况下,
 * 需要做数据转换的操作,建议使用标记getXXX setXXX方法.
 * 比如: 某一个成员类型是枚举,而数据库中使用整数,则需要专门做getXXX setXXX方法
 * 
 * 
 * 
 * 
 * @author Jason Zhu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
public @interface XORMProperty
{
	/**
	 * Xml变量和数据库名称
	 * @return
	 */
	String name();//符合xmldata变量名的名称
	
	/**
	 * 标题
	 * @return
	 */
	String title() default "" ;
	/**
	 * 如果该属性=true,则表明该Field或Method不参与数据库结构的定义和存储,也不参与生成XmlData和结构
	 * 它只在类说对应的对象中使用
	 * 
	 * 也就是该Field或Method只能在对象中使用,也就是在构造对象时内的值时,可以把XmlData或数据库列中的值
	 * 写入到对象中. 但反过来不允许
	 * @return
	 */
	boolean is_transient() default false;
	
	/**
	 * 是否需要单独的数据库列
	 * 如果=false,则表示不需要,其数据统一保存在xorm_ext列中
	 * @return
	 */
	boolean has_col() default false; // 如果没有指定,表明内容保存在XmlData结构中

	/**
	 * 缺省值的字符串表示
	 * @return
	 */
	String default_str_val() default "";//缺省字符串值
	
	/**
	 * 是否是主键--一般也就是一个表对应一个
	 * @return
	 */
	boolean is_pk() default false;// 是否是主键
	
	/**
	 * 是否是自增长列,当前只针对is_pk=true的情况下有效
	 * @return
	 */
	boolean is_auto() default false;//是否自动值 则该属性有效,判断是否组件列自增长

	/**
	 * 如果is_auto=true,则用此value表示自动增长的其实值
	 * @return
	 */
	long auto_value_start() default -1;
	
	/**
	 * 是否有索引 前提has_col-true存在,表示有独立的列
	 * @return
	 */
	boolean has_idx() default false;// 是否有索引 前提has_col-true存在,表示有独立的列
	
	/**
	 * 是否唯一 前提has_col-true,并且has_idx=true存在,表示有独立的列
	 * @return
	 */
	boolean is_unique_idx() default false;//

	/**
	 * 是否可为空 前提has_col-true存在,表示有独立的列
	 * @return
	 */
	boolean nullable() default true ;//
	//boolean is_auto() default 
	/**
	 * 是否有外键引用 前提has_col-true存在,表示有独立的列
	 */
	boolean has_fk() default false;// 

	
	
	/**
	 * 外键引用的表 前提has_col-true存在,表示有独立的列
	 * @return
	 */
	String fk_table() default "";// 

	/**
	 * 外键引用的列 前提has_col=true存在,表示有独立的列
	 * @return
	 */
	String fk_column() default "";// 

	/**
	 * 如果该属性为true,则该属性根据本类中指定的base class的主键作为外键关联
	 * 该属性用在如下场合:
	 * 有两个类一个是主类M1 主键域 F1,另一个是非主类M2 引用外键域 F2 -- 该域fk_base_class=true
	 * 在使用过程中,需要对这两个类都进行继承--继承类C1-&gt;M1 和 C2-&gt;M2中分别定义了主类的表名和非主类的表名
	 * C2 指定base_class=C1
	 * 则C2中的F2就会自动的引用 C1定义的表名
	 * @return
	 */
	boolean fk_base_class() default false;
	
	/**
	 * 最大长度String 类型必须指定,数据库对应类型为varchar(max_len*2)
	 * @return
	 */
	int max_len() default -1; // . 前提is_seperate = true
	
	/**
	 * 当内容达到最大值时自动截断内容
	 */
	boolean auto_truncate() default false;
	
	/**
	 * 顺序号,前提has_col=true存在,表示建立数据库表列的顺序
	 * @return
	 */
	int order_num() default 1000; //顺序号
	
	/**
	 * 如果对应的成员是byte[]类型,并且本定义不为空
	 * 则该成员的值作为文件,并通过主键id进行自动保存.GDB会自动的创建目录
	 * 如果定义了该属性,则has_col不允许出现
	 * 
	 * 该属性在整个类定义中,只允许出现一次
	 * @return
	 */
	boolean store_as_file() default false;
	
	/**
	 * 如果store_as_file=true(以后可能更多)
	 * 并且该属性=true,则表明对应的内容在普通查询中不读入,而需要使用单独的方法获取
	 * 如读取流等
	 * 
	 * 建议配合 GDB.loadXORMFileContToOutputStream() 使用
	 * 减少内存的占用
	 * @return
	 */
	boolean read_on_demand() default false;
	
	/**
	 * 对于一些列,如byte[]大数据类型的值,一般不和普通列进行同时更新(有可能同时添加)
	 * 也就是普通的XORM对象update不会影响本列
	 * 本列的更新必须使用特殊方法
	 * 
	 * 
	 * @return
	 */
	boolean update_as_single() default false;
	
	/**
	 * 是否可以被自动化方式处理——如自动生成编辑页面，自动更新等。
	 * 该属性仅仅是个标记，外界应该可以使用本值进行自动化处理参考
	 * 如，自动表单生成的时候，可以根据此标记决定是否要生成对应的输入框。
	 * 	并作存储的时候决定是否要忽略此内容
	 * @return
	 */
	boolean support_auto() default false ;
	
	/**
	 * 本属性对应的bean名称 get set方法后面后续名词（并且第一个字母小写）
	 * @return
	 */
	String bean_get_set_name() default "" ;
	
	/**
	 * 对应值的可选范围，如字典，enum等
	 * 如：  dd:dd_class_name1
	 * 	    enum:com.xx.xx.XXType 等
	 * 		provider:com.xx.xx.XXClass
	 * 本属性为编辑界面自动生成提供依据
	 * @return
	 */
	String value_options() default "" ;
	
	/**
	 * 判断是否可以被编辑界面进行编辑输入
	 * 在一些情况下，某一个数据库列值是依赖其他字段自动生成时，没有必要在界面上对此
	 * 进行输入，可以设置此内容false
	 * 
	 * 该属性一般用来支持代码自动生成-编辑表单
	 * @return
	 */
	boolean is_view_editable() default true ;
	
	
	/**
	 * 判断是否可以被列举界面进行列举
	 * 
	 * 在一些情况下，某一个数据库列值，是没有必要进行列举的，可以设置false
	 * 
	 * 该属性一般用来支持代码自动生成-数据表的列举
	 * @return
	 */
	boolean is_view_listable() default true ;
	
	
	/**
	 * 如果对于的成员是long类型
	 * 设置了此属性=true之后，对应记录只要被更新，此列都会被
	 * 自动设置成更新时刻的时间毫秒数
	 * @return
	 */
	boolean is_auto_update_dt() default false;
}
