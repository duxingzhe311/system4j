/*
 * Created on 2004-8-20
 *
 * Copyright (c) Jason Zhu
 */
package com.dw.mltag;

public interface AttrFilter
{
	boolean ignoreAttribute(AbstractNode n,String attrn,String value) ;
}
