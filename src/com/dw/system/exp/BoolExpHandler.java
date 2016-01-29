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
	 * 判断一个操作符号是否是bool操作符。
	 * 该操作符要求返回的Argument中的结果是Boolean对象
	 */
	public short getBoolType(String oper) ;
}