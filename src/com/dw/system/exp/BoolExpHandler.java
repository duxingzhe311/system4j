package com.dw.system.exp ;

import java.io.*;
import java.util.*;

public interface BoolExpHandler extends ExpressionHandler
{
	public static final short BOOL_AND = 1 ;
	public static final short BOOL_OR = 2 ;
	//public static final short BOOL_NOT = 3 ;
	public static final short OTHER = 4 ;
	/**
	 * �ж�һ�����������Ƿ���bool��������
	 * �ò�����Ҫ�󷵻ص�Argument�еĽ����Boolean����
	 */
	public short getBoolType(String oper) ;
}