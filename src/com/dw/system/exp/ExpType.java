package com.dw.system.exp ;

import java.io.*;
import java.util.*;
/**
 * 表达式的变量类型定义
 */
public interface ExpType
{

	public final static short TYPE_ERROR = -1 ;
	public final static short TYPE_CONSTANT_STRING = 10 ;
	public final static short TYPE_CONSTANT_INTEGER = 11 ;
	public final static short TYPE_CONSTANT_SHORT = 12 ;
	public final static short TYPE_CONSTANT_LONG = 13 ;
	public final static short TYPE_CONSTANT_FLOAT = 14 ;
	public final static short TYPE_CONSTANT_DOUBLE = 15 ;
	public final static short TYPE_CONSTANT_BOOL = 16 ;
	public final static short TYPE_CONSTANT_DATE = 17 ;

	public final static short TYPE_CONSTANT_SET = 20 ;

	public final static short TYPE_CONSTANT_ARRAY_S = 21 ; //
	public final static short TYPE_CONSTANT_ARRAY_I = 22 ;
	public final static short TYPE_CONSTANT_ARRAY_H = 23 ;
	public final static short TYPE_CONSTANT_ARRAY_L = 24 ;
	public final static short TYPE_CONSTANT_ARRAY_F = 25 ;
	public final static short TYPE_CONSTANT_ARRAY_D = 26 ;
	public final static short TYPE_CONSTANT_ARRAY_B = 27 ;
	public final static short TYPE_CONSTANT_ARRAY_DATE = 28 ;

	public final static short TYPE_CONSTANT_OBJ = 29 ;
	/*
	public final static short TYPE_CONSTANT_SET_STRING = 5 ;
	public final static short TYPE_CONSTANT_SET_SHORT = 6 ;
	public final static short TYPE_CONSTANT_SET_LONG = 7 ;
	public final static short TYPE_CONSTANT_SET_FLOAT = 8 ;
	public final static short TYPE_CONSTANT_SET_DOUBLE = 9 ;
	*/
	public final static short TYPE_VAR = 30 ;
	public final static short TYPE_POINT = 31 ;
	public final static short TYPE_BASEOPER = 32 ;
	public final static short TYPE_L_BRACKET = 33 ;
	public final static short TYPE_R_BRACKET = 34 ;
}