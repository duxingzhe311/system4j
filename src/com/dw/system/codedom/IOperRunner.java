package com.dw.system.codedom;

/**
 * ���Ը��ݲ�ͬ�Ĳ���������ͬ��ʵ�֣�Ȼ���ڱ���ʱȷ�������ʵ�ֶ���
 * ��������������ʱ�ٶȸ���
 * @author Jason Zhu
 *
 */
public interface IOperRunner
{
	public Object calculate(AbstractRunEnvironment env,RunContext context,
			IDomNode leftdn,IDomNode rightdn,
			Object leftv,Object rightv) throws Exception;
}
