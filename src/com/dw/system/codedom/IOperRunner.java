package com.dw.system.codedom;

/**
 * 可以根据不同的操作符做不同的实现，然后在编译时确定其具体实现对象
 * 这样可以在运行时速度更快
 * @author Jason Zhu
 *
 */
public interface IOperRunner
{
	public Object calculate(AbstractRunEnvironment env,RunContext context,
			IDomNode leftdn,IDomNode rightdn,
			Object leftv,Object rightv) throws Exception;
}
