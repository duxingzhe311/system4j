package com.dw.system.gdb.xorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ������XORMClassָ��<br/>
 * <br/>
 * ��gdb_XXX.xml�ļ���Ҫָ�����µ�����,����<br/>
 * &lt;XORM name="#CN11" class="com.dw.comp.app_case_mgr.CXxItem"/&gt;<br/>
 * <br/>
 * XORM����������֮��,�������������±���,���Ա���xml�ļ��е�Sql��䶨��ʱʹ��<br/>
    <b>#CN11.all_cols</b> //���е���(����������xorm_ext��)<br/>
    <b>#CN11.pk_col</b> //������<br/>
    <b>#CN11.nor_cols</b> //��ͨ��(������������xorm_ext�е�has_col=true��)<br/>
    <b>#CN11.pk_nor_cols</b> //�����к���ͨ��<br/>
    <b>#CN11.table_name</b> //������<br/>
    <br/>
    ����:<br/>
    select #CN11.pk_nor_cols from #CN11.table_name where c1=? and ...<br/>
    �����������ǰ�ᱻ�Զ�ת��Ϊ<br/>
    select id,c1,c2 from t1 where c1=? and ...<br/>
    <br/>
    ������Щ����֮��,���Ҫ�������ӵ�sql,�����֪��XORM�����Ķ���<br/>
     ����²��ֵ���ͨ�е�.<br/>
     
 * @author Jason Zhu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XORMClass {
	
	/**
	 * ���ݿ������
	 * @return
	 */
	String table_name();
	
	/**
	 * ���=true,�������Ӧ����ı��̳и����е�XORM��������XORMProperty�ᱻ�̳й���ʹ��
	 * 
	 * @return
	 */
	boolean inherit_parent() default false;
	
	/**
	 * ����
	 * @return
	 */
	String title() default "" ;
	/**
	 * ָ������
	 * ���һ���ඨ����ָ��������,��ô������Դ���һ����������fk_base_class=true,
	 * �����ʹ�������е�������Ϊ�������
	 * --������Ϊ��ļ̳��ṩ���Զ���Ӧ������������Ķ���
	 * @return
	 */
	Class base_class() default Object.class;
	
	/**
	 * ���=0 ��ʾ��֧�ֲַ�ʽ
	 * 
	 * ���=1����������Ӧ�ı�֧�ֲַ�ʽ���У�����������ģʽ1��
	 * 	1���ڷֲ�ʽ�����ľֲ��ˣ�Proxy�ˣ������ݿ��Ľ�������ͨ�����ƣ��������������������ϵ
	 * 	2���ڷֲ�ʽ�����ķ������ˣ�Server�ˣ������ݿ��Ľ������Զ���һ��_ProxyId�У������ձ��
	 * 		�Զ��б��������ͨ��
	 * 	3���ֲ�ʽ���й����У�����ֻ����Proxy�˽��и��£�ÿ��Proxy�˼�¼�������еĸ���log�������������
	 * 		���͸�����־���������Զ�����_ProxyId����ͬ���������������˻�ͨ��ͬ����¼ȫ��Proxy�˵����ݡ�
	 * 	4����Proxy��ʼ�׶Σ�Proxy�ڽ�����Ӧ�ı�֮�󣬻���������˷��͡����ء��������󣬿���ʹ�÷�������
	 * 		��Ӧ��_ProxyId���е��������ص�Proxy����
	 * 
	 * ���=2,��������Ӧ�ı�֧�ֲַ�ʽ���У�����������ģʽ2��
	 * 	1���ڷֲ�ʽ�����£��������˺�Proxy�˵ı�ṹ��ͬ��ÿһ�ж�����һ��Ψһ�����к͸���ʱ����С�
	 * 	2�����ݿ��ֻ���ڷ������˸��¸���
	 * 	3��ÿ��Proxy�˻ᶨʱ��������˽�������ͬ����Proxy�������ط������˶�Ӧ�������id�͸���ʱ��
	 * 		�б����ݱ��������������µ�����Ϣ��ɾ���Ѿ������ڵ�id--��Ҫ�����ݱ���ò�Ҫ̫��
	 * 
	 * 
	 * @return
	 */
	int distributed_mode() default 0;
	
	/**
	 * ����֧���ն�ͬ��
	 * ��Ҫ�Դ˱�����ն�ͬ��������־��¼
	 * GDB�ڽ�����Ĺ����У����Զ�������xxx_syn_client_log��
	 * ���ڴ˱��ʹ�ù����У��Զ���¼�仯log
	 * �˱������һ������ֻ��һ��Ψһ����
	 * @return
	 */
	boolean support_syn_client() default false;
	
	/**
	 * ָ��������ڵ��ն�id��
	 * @return
	 */
	String syn_client_client_id_col() default "" ;
	
	/**
	 * ָ�������Ӧ���ն�Ψһid��
	 * @return
	 */
	String syn_client_pk_col() default "" ;
	
	
}
