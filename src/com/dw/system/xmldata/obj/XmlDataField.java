/**
 * 
 */
package com.dw.system.xmldata.obj;

import java.lang.annotation.*;

import com.dw.system.xmldata.XmlDataStruct.StoreType;

/**
 * Ϊ���е�Field����XmlData�ṹ�ṩ��Annotation֧��
 * @author Jason Zhu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlDataField
{
	String name() default "";
	boolean nullable() default true;
	int max_len() default -1 ;
	StoreType store() default StoreType.Normal;
}
