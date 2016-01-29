package com.dw.comp;

import java.util.ArrayList;

import com.dw.web_ui.http.IHttpClientCmdHandler;

public abstract class AbstractComp
{
	public void doTest()
	{
		
	}
	
	public abstract String getCompName();
	
	public abstract ArrayList<CompAction> getActions() ;
	
	
	/**
	 * ���Tomato�����д���Http Client��������,����������ƾͿ��Ի��
	 * 
	 * �ڿؼ��ͻ���ʹ�����¸�ʽ������ TC:CompName:hcc_name������
	 * @param hcc_name
	 * @return
	 */
	public IHttpClientCmdHandler getHttpClientCmdHandler(String hcc_name)
	{
		return null ;
	}
	
	/**
	 * ÿ�������ڱ�װ�غ������Ҫ����һЩ���������ԣ������ҪΪ��������
	 * �ṩһЩ�ֶΡ�Ӧ���ڸ÷�����ʵ�֡�
	 * 
	 * ���磬����������ͨ�����ø÷����������й����ڲ���һЩ�����ȡ�
	 * @param args
	 */
	public abstract void doCmd(String[] args) throws Exception;
	
}
