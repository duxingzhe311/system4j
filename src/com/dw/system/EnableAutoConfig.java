package com.dw.system ;

/**
 * ���������ö���Ľ�ڡ�
 */
public interface EnableAutoConfig
{
	/**
	 * ����ö��������Configuration.register (this), �÷�������Configuration.refresh () ���á�
	 * @see	Configuration
	 * @see	Configuration#refresh(String)
	 * @see	Configuration#register(EnableAutoConfig)
	 */
	public void autoConfig () ;
}