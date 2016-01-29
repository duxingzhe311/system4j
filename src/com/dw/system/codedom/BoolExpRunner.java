package com.dw.system.codedom;

import com.dw.system.IExpPropProvider;
import com.dw.system.codedom.parser.*;

/**
 * ��������bool����ı��ʽ�����ж���
 * 
 * ����:����ʵ������,�����ڲ�ʵ�ָó�����,����֧��bool���ʽ������. ������ʵ����,��������֧��Transition���ж�
 * 
 * @author Jason Zhu
 * 
 */
public abstract class BoolExpRunner extends RunContext implements
		IExpPropProvider
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

	public ValWrapper getValueWrapper(String name)
		throws Exception
	{
		ValWrapper vw = getRunnerPropValue(name);
		if(vw!=null)
			return vw ;
		
		vw = getRunnerVarValue(name);
		if(vw!=null)
			return vw ;
		
		return super.getValueWrapper(name);
	}
	
	protected abstract ValWrapper getRunnerVarValue(String var_name);
	/**
	 * �����������ƻ�ö�Ӧ��ֵ
	 */
	protected abstract ValWrapper getRunnerPropValue(String propname);

	public Object getPropValue(String propname)
	{
		ValWrapper vw = getRunnerPropValue(propname);
		if(vw==null)
			return null ;
		
		return vw.val;
	}
	
	public void setPropValue(String propname,Object o)
	{
		//do nothing
	}
	// /**
	// * ���ݱ������ƻ�ö�Ӧ�ı���ֵ
	// *
	// * @param varname
	// * @return
	// */
	// public abstract Object getVarValue(String varname);

	public boolean runExp(String strexp) throws Exception
	{
		if(strexp==null||strexp.equals(""))
			return false;
		Boolean b = (Boolean) getEnv().runExp(strexp, this);
		return b.booleanValue();
	}
}
