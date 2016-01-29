package com.dw.system.gdb.xorm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ��ע�����Ա,��getXXX setXXX֮�ϵı��
 * ����,ͬһ����Ƕ�ӦgetXXX��setXXXֻ��Ҫ�������һ������
 * һ����˵,�����Field��Ա������Ҫ��. ����һЩ�����,
 * ��Ҫ������ת���Ĳ���,����ʹ�ñ��getXXX setXXX����.
 * ����: ĳһ����Ա������ö��,�����ݿ���ʹ������,����Ҫר����getXXX setXXX����
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
	 * Xml���������ݿ�����
	 * @return
	 */
	String name();//����xmldata������������
	
	/**
	 * ����
	 * @return
	 */
	String title() default "" ;
	/**
	 * ���������=true,�������Field��Method���������ݿ�ṹ�Ķ���ʹ洢,Ҳ����������XmlData�ͽṹ
	 * ��ֻ����˵��Ӧ�Ķ�����ʹ��
	 * 
	 * Ҳ���Ǹ�Field��Methodֻ���ڶ�����ʹ��,Ҳ�����ڹ������ʱ�ڵ�ֵʱ,���԰�XmlData�����ݿ����е�ֵ
	 * д�뵽������. ��������������
	 * @return
	 */
	boolean is_transient() default false;
	
	/**
	 * �Ƿ���Ҫ���������ݿ���
	 * ���=false,���ʾ����Ҫ,������ͳһ������xorm_ext����
	 * @return
	 */
	boolean has_col() default false; // ���û��ָ��,�������ݱ�����XmlData�ṹ��

	/**
	 * ȱʡֵ���ַ�����ʾ
	 * @return
	 */
	String default_str_val() default "";//ȱʡ�ַ���ֵ
	
	/**
	 * �Ƿ�������--һ��Ҳ����һ�����Ӧһ��
	 * @return
	 */
	boolean is_pk() default false;// �Ƿ�������
	
	/**
	 * �Ƿ�����������,��ǰֻ���is_pk=true���������Ч
	 * @return
	 */
	boolean is_auto() default false;//�Ƿ��Զ�ֵ ���������Ч,�ж��Ƿ������������

	/**
	 * ���is_auto=true,���ô�value��ʾ�Զ���������ʵֵ
	 * @return
	 */
	long auto_value_start() default -1;
	
	/**
	 * �Ƿ������� ǰ��has_col-true����,��ʾ�ж�������
	 * @return
	 */
	boolean has_idx() default false;// �Ƿ������� ǰ��has_col-true����,��ʾ�ж�������
	
	/**
	 * �Ƿ�Ψһ ǰ��has_col-true,����has_idx=true����,��ʾ�ж�������
	 * @return
	 */
	boolean is_unique_idx() default false;//

	/**
	 * �Ƿ��Ϊ�� ǰ��has_col-true����,��ʾ�ж�������
	 * @return
	 */
	boolean nullable() default true ;//
	//boolean is_auto() default 
	/**
	 * �Ƿ���������� ǰ��has_col-true����,��ʾ�ж�������
	 */
	boolean has_fk() default false;// 

	
	
	/**
	 * ������õı� ǰ��has_col-true����,��ʾ�ж�������
	 * @return
	 */
	String fk_table() default "";// 

	/**
	 * ������õ��� ǰ��has_col=true����,��ʾ�ж�������
	 * @return
	 */
	String fk_column() default "";// 

	/**
	 * ���������Ϊtrue,������Ը��ݱ�����ָ����base class��������Ϊ�������
	 * �������������³���:
	 * ��������һ��������M1 ������ F1,��һ���Ƿ�����M2 ��������� F2 -- ����fk_base_class=true
	 * ��ʹ�ù�����,��Ҫ���������඼���м̳�--�̳���C1-&gt;M1 �� C2-&gt;M2�зֱ���������ı����ͷ�����ı���
	 * C2 ָ��base_class=C1
	 * ��C2�е�F2�ͻ��Զ������� C1����ı���
	 * @return
	 */
	boolean fk_base_class() default false;
	
	/**
	 * ��󳤶�String ���ͱ���ָ��,���ݿ��Ӧ����Ϊvarchar(max_len*2)
	 * @return
	 */
	int max_len() default -1; // . ǰ��is_seperate = true
	
	/**
	 * �����ݴﵽ���ֵʱ�Զ��ض�����
	 */
	boolean auto_truncate() default false;
	
	/**
	 * ˳���,ǰ��has_col=true����,��ʾ�������ݿ���е�˳��
	 * @return
	 */
	int order_num() default 1000; //˳���
	
	/**
	 * �����Ӧ�ĳ�Ա��byte[]����,���ұ����岻Ϊ��
	 * ��ó�Ա��ֵ��Ϊ�ļ�,��ͨ������id�����Զ�����.GDB���Զ��Ĵ���Ŀ¼
	 * ��������˸�����,��has_col���������
	 * 
	 * �������������ඨ����,ֻ�������һ��
	 * @return
	 */
	boolean store_as_file() default false;
	
	/**
	 * ���store_as_file=true(�Ժ���ܸ���)
	 * ���Ҹ�����=true,�������Ӧ����������ͨ��ѯ�в�����,����Ҫʹ�õ����ķ�����ȡ
	 * ���ȡ����
	 * 
	 * ������� GDB.loadXORMFileContToOutputStream() ʹ��
	 * �����ڴ��ռ��
	 * @return
	 */
	boolean read_on_demand() default false;
	
	/**
	 * ����һЩ��,��byte[]���������͵�ֵ,һ�㲻����ͨ�н���ͬʱ����(�п���ͬʱ���)
	 * Ҳ������ͨ��XORM����update����Ӱ�챾��
	 * ���еĸ��±���ʹ�����ⷽ��
	 * 
	 * 
	 * @return
	 */
	boolean update_as_single() default false;
	
	/**
	 * �Ƿ���Ա��Զ�����ʽ���������Զ����ɱ༭ҳ�棬�Զ����µȡ�
	 * �����Խ����Ǹ���ǣ����Ӧ�ÿ���ʹ�ñ�ֵ�����Զ�������ο�
	 * �磬�Զ������ɵ�ʱ�򣬿��Ը��ݴ˱�Ǿ����Ƿ�Ҫ���ɶ�Ӧ�������
	 * 	�����洢��ʱ������Ƿ�Ҫ���Դ�����
	 * @return
	 */
	boolean support_auto() default false ;
	
	/**
	 * �����Զ�Ӧ��bean���� get set��������������ʣ����ҵ�һ����ĸСд��
	 * @return
	 */
	String bean_get_set_name() default "" ;
	
	/**
	 * ��Ӧֵ�Ŀ�ѡ��Χ�����ֵ䣬enum��
	 * �磺  dd:dd_class_name1
	 * 	    enum:com.xx.xx.XXType ��
	 * 		provider:com.xx.xx.XXClass
	 * ������Ϊ�༭�����Զ������ṩ����
	 * @return
	 */
	String value_options() default "" ;
	
	/**
	 * �ж��Ƿ���Ա��༭������б༭����
	 * ��һЩ����£�ĳһ�����ݿ���ֵ�����������ֶ��Զ�����ʱ��û�б�Ҫ�ڽ����϶Դ�
	 * �������룬�������ô�����false
	 * 
	 * ������һ������֧�ִ����Զ�����-�༭��
	 * @return
	 */
	boolean is_view_editable() default true ;
	
	
	/**
	 * �ж��Ƿ���Ա��оٽ�������о�
	 * 
	 * ��һЩ����£�ĳһ�����ݿ���ֵ����û�б�Ҫ�����оٵģ���������false
	 * 
	 * ������һ������֧�ִ����Զ�����-���ݱ���о�
	 * @return
	 */
	boolean is_view_listable() default true ;
	
	
	/**
	 * ������ڵĳ�Ա��long����
	 * �����˴�����=true֮�󣬶�Ӧ��¼ֻҪ�����£����ж��ᱻ
	 * �Զ����óɸ���ʱ�̵�ʱ�������
	 * @return
	 */
	boolean is_auto_update_dt() default false;
}
