package com.dw.system;

/**
 * ���������������������ӿ�
 * 
 * �ýӿ��������������Ҫ��ӵ����
 * 
 * ��:��tomcat��Ϊһ�����������ͬʱ����. ��Ӧ�ð�Tomcat������ʵ�ָýӿ�
 * 
 * �����Ϳ��Կ���tomcat��������ֹͣ
 * 
 * 
 */
public interface IServerBootComp {
	String getBootCompName();

	void startComp() throws Exception;

	void stopComp() throws Exception;

	boolean isRunning() throws Exception;
}
