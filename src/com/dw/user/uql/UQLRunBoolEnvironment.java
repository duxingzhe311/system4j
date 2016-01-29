package com.dw.user.uql;

import java.util.Hashtable;

import com.dw.system.codedom.RunContext;
import com.dw.system.codedom.AbstractRunEnvironment;
import com.dw.system.codedom.IOperRunner;
import com.dw.user.*;

/**
 * ��Ҫ�ṩ�û�����ɫ������֯�����Ƿ�����һ���ı��ʽ
 * @author Jason Zhu
 *
 */
public class UQLRunBoolEnvironment extends AbstractRunEnvironment
{
	@Override
	protected Object[] globalSupportedObj()
	{
		return null;
	}

	@Override
	public IOperRunner getEnvOperRunner(String oper_str)
	{
		return null;
	}

	/**
	 * ����û��Ƿ�������ʽ
	 * @param exp_str
	 * @param up
	 * @return
	 */
	public boolean checkUserByExp(String exp_str,UserProfile up,Hashtable in_params)
		throws Exception
	{
		Boolean b = (Boolean)this.runExp(exp_str,up, in_params);
		return b.booleanValue();
	}
}
