package com.dw.system.codedom;

import java.util.Hashtable;

import com.dw.system.codedom.BoolExpRunner.REnv;
import com.dw.system.codedom.parser.ASTOper;

/**
 * ��һЩ����,���ʽ��Ϊ������Ϣ�ǹ̶���,���������Dom����,��û�б�Ҫ����ͬ�ı������ͬ���Ľ���
 * 
 * ����Ķ�����Ǵ���һ�����ʽdom. �����ݲ�ͬ�����������bool�ж�,����֧�ֶ��̰߳�ȫ
 * @author Jason Zhu
 */
public class BoolExp
{
	static class REnv extends AbstractRunEnvironment
	{
		@Override
		protected Object[] globalSupportedObj()
		{
			return null;
		}

		@Override
		public IOperRunner getEnvOperRunner(String oper_str)
		{
			if ("=".equals(oper_str))
			{
				return new ASTOper.OperRunnerEq();
			}
			return null;
		}
	}

	private static Object locker = new Object();

	private static AbstractRunEnvironment ENV = null;

	private static AbstractRunEnvironment getEnv() throws Exception
	{
		if (ENV != null)
			return ENV;

		synchronized (locker)
		{
			if (ENV != null)
				return ENV;

			ENV = new REnv();
			ENV.init();
			return ENV;
		}
	}
	
	private IDomNode domRoot = null ;
	
	public BoolExp(String exp_str) throws Exception
	{
		if(exp_str==null||exp_str.equals(""))
			throw new IllegalArgumentException("exp cannot null or empty!");
		
		domRoot = getEnv().parseExpToTree(exp_str);
	}
	
	public boolean checkWithParam(Hashtable input_parms) throws Exception
	{
		RunContext rc = new RunContext(input_parms);
		Boolean b = (Boolean) getEnv().runDomExp(rc,domRoot, null, null);
		return b.booleanValue();
	}
	
	public boolean checkWithContext(RunContext rc) throws Exception
	{
		Boolean b = (Boolean) getEnv().runDomExp(rc,domRoot, null, null);
		return b.booleanValue();
	}
}
