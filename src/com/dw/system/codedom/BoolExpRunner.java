package com.dw.system.codedom;

import com.dw.system.IExpPropProvider;
import com.dw.system.codedom.parser.*;

/**
 * 用来运行bool结果的表达式的运行对象
 * 
 * 比如:流程实例对象,可以内部实现该抽象类,用来支持bool表达式的运行. 如流程实例中,可以用来支持Transition的判定
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
	 * 根据属性名称获得对应的值
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
	// * 根据变量名称获得对应的变量值
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
