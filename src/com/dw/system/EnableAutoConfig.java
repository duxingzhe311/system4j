package com.dw.system ;

/**
 * 定义自配置对象的借口。
 */
public interface EnableAutoConfig
{
	/**
	 * 如果该对象调用了Configuration.register (this), 该方法将被Configuration.refresh () 调用。
	 * @see	Configuration
	 * @see	Configuration#refresh(String)
	 * @see	Configuration#register(EnableAutoConfig)
	 */
	public void autoConfig () ;
}