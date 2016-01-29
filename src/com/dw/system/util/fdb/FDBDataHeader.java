package com.dw.system.util.fdb;

/**
 * ���ݿ��ͷ��Ϣ
 * @author Jason Zhu
 *
 */
public class FDBDataHeader
{
	/**
	 * ��ʼλ��
	 */
	long startPos = -1 ;
	
	/**
	 * ���ݿ���λ
	 */
	byte dataTag = -1 ;
	/**
	 * �������ݳ���
	 */
	long dataLen = -1 ;
	
	//1cfbdbe0-5 0c8-4f9e-9 d86-bc40b5 d740f1
	/**
	 * 1cfbdbe0-5 0c8-4f9e-9 d86-bc40b5 d740f1
	 * 1000000001
	 * 
	 * ����key��ʹ��UTF8����󣬳��Ȳ��ܳ���40
	 */
	String idxKey = null ;
	
	/**
	 * ����key��С��������֧��˫������ǰ��
	 */
	FDBDataHeader prevHeader = null ;
	
	FDBDataHeader nextHeader = null ;
}
