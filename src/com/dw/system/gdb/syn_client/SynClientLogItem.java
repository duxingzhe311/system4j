package com.dw.system.gdb.syn_client;

import com.dw.system.gdb.xorm.*;

/**
 * �ն˷�һ��ͬ��log
 * 
 * �������������ҵ����޸ģ��Զ�����ؼ�¼�ĸ���-��������ƣ�clientid��pkid�ظ�
 * 	��ֻ��ʱ��ĸ���
 * �նˣ�ͨ����ȡ��log,��ȡ�޸ĵĶ��ڱ��pkid��������������ӿڻ�þ���ļ�¼ֵ�����ص�
 * 	���ӡ����»�ɾ����
 * �����سɹ���SynTableName��clientid��chgDt��pkid�����������log���ڶ�Ӧ�ļ�¼����ɾ��
 *   ��������ڣ����������chgDT���޸ġ���Ҫ�ն��ٴ�������
 * 
 * @author Jason Zhu
 *
 */
@XORMClass(table_name="syn_client_log")
public class SynClientLogItem
{
	/**
	 * ��ͬ���ı�����
	 */
	@XORMProperty(name="SynTableName",max_len=15,has_col=true,has_idx=true,order_num=10)
	String synTableName = null ;
	
	/**
	 * �ն�id,Ҳ�������ն˵��û���
	 */
	@XORMProperty(name="ClientId",max_len=15,has_col=true,has_idx=true,order_num=20)
	String clientId = null ;
	
	/**
	 * �仯ʱ��
	 */
	@XORMProperty(name="ChgDT",has_col=true,has_idx=true,order_num=30)
	long chgDt = -1 ;
	
	/**
	 * ҵ�����ݱ���ؼ�¼id
	 */
	@XORMProperty(name="PkId",max_len=15,has_col=true,has_idx=true,order_num=40)
	String pkId = null ;
	
	public String getSynTableName()
	{
		return synTableName ;
	}
	
	public String getClientId()
	{
		return clientId ;
	}
	
	public long getChgDT()
	{
		return chgDt;
	}
	
	public String getPkId()
	{
		return pkId ;
	}
}
