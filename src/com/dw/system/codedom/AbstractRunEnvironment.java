package com.dw.system.codedom;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.*;

import com.dw.system.IExpPropProvider;
import com.dw.system.codedom.parser.*;

/**
 * �������е����廷��,���������繫���������������Ե�����
 * 
 * @author Jason Zhu
 */
public abstract class AbstractRunEnvironment
{
	static class ObjMethodWrapper
	{
		public Object obj = null;

		public List<Method> methods = new ArrayList<Method>();
	}

	static class ObjFieldWrapper
	{
		public Object field_val = null;
	}

	/**
	 * �����ṩ��ȫ�ַ���֧��
	 */
	private Hashtable<String, ObjMethodWrapper> globalMethodName2Method = new Hashtable<String, ObjMethodWrapper>();

	/**
	 * �����ṩ��ȫ������֧��
	 */
	private Hashtable<String, ObjFieldWrapper> globalPropName2Field = new Hashtable<String, ObjFieldWrapper>();

	/**
	 * �����ṩȫ������֧��
	 */
	private Hashtable<String, ObjMethodWrapper> globalPropName2Method = new Hashtable<String, ObjMethodWrapper>();

	public Object runFunc()
	{
		return null;
	}

	public void init() throws Exception
	{
		initGlobalObj(new DefaultGlobal());

		Object[] objs = globalSupportedObj();
		if (objs != null)
		{
			for (Object o : objs)
				initGlobalObj(o);
		}
	}

	private void initGlobalObj(Object o) throws Exception
	{
		Class c = o.getClass();
		Field[] fs = c.getDeclaredFields();
		if (fs != null)
		{
			for (Field f : fs)
			{
				if ((f.getModifiers() & Modifier.PUBLIC) > 0)
				{
					ObjFieldWrapper ofw = new ObjFieldWrapper();
					ofw.field_val = f.get(o);
					globalPropName2Field.put(f.getName(), ofw);
				}
			}
		}

		Method[] ms = c.getDeclaredMethods();
		if (ms != null)
		{
			for (Method m : ms)
			{
				if ((m.getModifiers() & Modifier.PUBLIC) == 0)
					continue;

				String mn = m.getName();
				ObjMethodWrapper omw = globalMethodName2Method.get(mn);
				if (omw == null)
				{
					omw = new ObjMethodWrapper();
					omw.obj = o;
					globalMethodName2Method.put(mn, omw);
				}

				omw.methods.add(m);

				if (mn.startsWith("get"))
				{
					mn = mn.substring(3);
					String tmpfn = mn.substring(0, 1).toLowerCase()
							+ mn.substring(1);
					ObjMethodWrapper tmpomw = globalPropName2Method.get(tmpfn);
					if (tmpomw == null)
					{
						tmpomw = new ObjMethodWrapper();
						tmpomw.obj = o;
						globalPropName2Method.put(tmpfn, tmpomw);
					}
					tmpomw.methods.add(m);
				}
			}
		}
	}

	public RunContext createNewRunContext()
	{
		return new RunContext();
	}

	protected abstract Object[] globalSupportedObj();

	/**
	 * ����ȫ�ַ����������ؽ��
	 * 
	 * @param methodname
	 * @param args
	 * @return
	 */
	public Object callGlobalMethod(String methodname, Object[] args)
			throws Exception
	{
		ObjMethodWrapper mw = globalMethodName2Method.get(methodname);
		if (mw == null)
			throw new Exception("cannot get global method with name="
					+ methodname);

		int pnum = 0;
		if (args != null)
			pnum = args.length;

		for (Method m : mw.methods)
		{
			int ptnum = 0;
			Class[] pts = m.getParameterTypes();
			if (pts != null)
				ptnum = pts.length;

			if (pnum == ptnum)
			{
				return m.invoke(mw.obj, args);
			}
		}

		throw new Exception("cannot get global method with name=" + methodname);
	}

	/**
	 * 
	 * @param propname
	 * @return
	 */
	public Object callGlobalProp(String propname) throws Exception
	{
		ObjFieldWrapper fw = globalPropName2Field.get(propname);
		if (fw != null)
		{
			return fw.field_val;
		}

		ObjMethodWrapper mw = globalPropName2Method.get(propname);
		if (mw != null)
		{
			return mw.methods.get(0).invoke(mw.obj, (Object[])null);
		}

		return null ;
		//throw new Exception("cannot get global field with name=" + propname);
	}

	/**
	 * ���ö��󷽷������У������еķ�������ʱ�������ܵĸ�������������ж�λ ���磬������ֵ�������ͣ����Կ����ܹ�int����֧��shortֵ
	 * 
	 * @param obj
	 * @param methodname
	 * @param args
	 * @return
	 */
	public Object callObjectMethod(Object obj, String methodname, Object[] args)
			throws Exception
	{
		Method[] ms = obj.getClass().getDeclaredMethods();
		if (ms == null || ms.length <= 0)
			throw new Exception("no method found in obj with class name="
					+ obj.getClass().getCanonicalName());

		if (ms.length == 1)
		{
			return ms[0].invoke(obj, args);
		}

		int pnum = 0;
		if (args != null)
			pnum = args.length;

		for (Method m : ms)
		{
			if(!m.getName().equals(methodname))
				continue ;
			
			int tmppcs = 0;
			Class[] pcs = m.getParameterTypes();
			if (pcs != null)
				tmppcs = pcs.length;

			if (pnum != tmppcs)
				continue;

			// ��������һ��
			return m.invoke(obj, args);
		}

		throw new Exception("no method found in obj with class name="
				+ obj.getClass().getCanonicalName());
	}

	/**
	 * ���ö����е����ԣ����п����й����ľ�̬���Ǿ�̬��Ա��getPropname()����
	 * 
	 * @param obj
	 * @param propname
	 * @return
	 * @throws Exception
	 */
	public Object callObjectProp(Object obj, String propname) throws Exception
	{
		if(obj instanceof IExpPropProvider)
		{//�������ʵ��IExpPropProvider�ӿ�,��ֱ��ͨ���ӿڵ���
			IExpPropProvider epp = (IExpPropProvider)obj ;
			return epp.getPropValue(propname);
		}
		
		Class c = obj.getClass();
		Field f = null ;
		Field[] fs = c.getFields() ;
		for(Field tmpf:fs)
		{
			if(tmpf.getName().equalsIgnoreCase(propname))
			{
				f = tmpf ;
				break ;
			}
		}
		
		if (f != null)
		{
			return f.get(obj);
		}

		String pmn = "get" + propname.substring(0, 1).toUpperCase()
				+ propname.substring(1);
		Method m = c.getDeclaredMethod(pmn, (Class[])null);
		if (m != null)
		{
			return m.invoke(obj, (Object[])null);
		}
		throw new Exception("no property found in obj with class name="
				+ obj.getClass().getCanonicalName());
	}

	public Object runCode(IDomNode dn) throws Exception
	{
		if (!(dn instanceof ASTExp))
			throw new RuntimeException("Code Dom root must be Exp");

		RunContext context = createNewRunContext();
		return dn.runGetValue(this, context);
	}
	
	
	public void runCode(IDomNode dn,RunContext rc) throws Exception
	{
		//if (!(dn instanceof ASTExp))
		//	throw new RuntimeException("Code Dom root must be Exp");

		//RunContext context = createNewRunContext();
		dn.runGetValue(this, rc);
	}

	/**
	 * һ����˵��ȱʡʵ�����еĲ����������Լ���ȱʡ���з�ʽ����+ - * / & | ��
	 * ���ܶ�����£���Ҫ���ʹ�����Լ�����ĳһ���������ľ���������̣��� && || �ĳɼ�������,=�ĳɱȽ��������
	 * ͨ�����ظ÷��������л������Ը����Լ�����Ҫ���Զ�����������������
	 * 
	 * �������ʱ�Ѹ�Env���󴫵ݸ�����������ô��DomCode���Ϳ���ʹ�û���ʵ���Զ�����������
	 * 
	 * @param oper_str
	 * @return
	 */
	public abstract IOperRunner getEnvOperRunner(String oper_str);

	// //////////////////////////
	// ��������ʱ֧�ֵķ���
	// ////////////////////////

	public IDomNode parseCodeBlockToTree(String code_str) throws Exception
	{
		return parseCodeBlockToTree(code_str, this);
	}

	public static IDomNode parseCodeBlockToTree(String code_str,
			AbstractRunEnvironment env) throws Exception
	{
		StringReader sr = new StringReader(code_str);
		IDomNode dn = UQLTreeParser.parseCodeBlockToTree(sr);
		compileNode(dn, env);
		return dn;
	}

	public IDomNode parseExpToTree(String exp_str) throws Exception
	{
		return parseExpToTree(exp_str, this);
	}

	public static IDomNode parseExpToTree(String exp_str,
			AbstractRunEnvironment env) throws Exception
	{
		StringReader sr = new StringReader(exp_str);
		return parseExpToTree(sr, env);
	}

	public static IDomNode parseExpToTree(StringReader sr,
			AbstractRunEnvironment env) throws Exception
	{
		IDomNode dn = UQLTreeParser.parseExpToTree(sr);
		compileNode(dn, env);
		return dn;
	}

	public static IDomNode parseExpToTree(InputStream inps,
			AbstractRunEnvironment env) throws Exception
	{
		IDomNode dn = UQLTreeParser.parseExpToTree(inps);
		compileNode(dn, env);
		return dn;
	}

	private static void compileNode(IDomNode dn, AbstractRunEnvironment env)
	{
		// �ȶ��ӽڵ���б��봦��
		int cc = dn.jjtGetNumChildren();
		for (int i = 0; i < cc; i++)
		{
			compileNode(dn.jjtGetChild(i), env);
		}

		// ��������д���
		dn.compileNode(env);
	}

	// //////////////////////////
	// ����ʱ֧�ֵķ���
	// ////////////////////////
	private Hashtable<String,IDomNode> regExpName2DomTree = new Hashtable<String,IDomNode>();
	
	public void registerExp(String reg_exp_name,String exp_str)
		throws Exception
	{
		IDomNode dn = parseExpToTree(exp_str);
		regExpName2DomTree.put(reg_exp_name, dn);
	}
	
	/**
	 * �������Ѿ�ע��ı��ʽʱ�������˱��ʽ�Ľ���������̣���ֱ������DomTree
	 * ���ܱȽϺ�
	 * @param exp_name
	 * @param defaultobj
	 * @param in_params
	 * @return
	 * @throws Exception
	 */
	public Object runRegisteredExp(String exp_name,Object defaultobj, Hashtable in_params)
		throws Exception
	{
		IDomNode dn = regExpName2DomTree.get(exp_name);
		if(dn==null)
			throw new Exception("cannot find registered exp name="+exp_name);
		
		return runDomExp(dn, defaultobj, in_params);
	}

	public Object runExp(String exp_str, Object defaultobj, Hashtable in_params)
			throws Exception
	{
		IDomNode dn = parseExpToTree(exp_str);
		return runDomExp(dn, defaultobj, in_params);
	}
	
	public Object runDomExp(IDomNode dn, Object defaultobj, Hashtable in_params)
	throws Exception
	{
		RunContext rc = createNewRunContext();
		return runDomExp(rc,dn, defaultobj, in_params);
	}
	
	public Object runExp(String exp_str,RunContext rc)
		throws Exception
	{
		IDomNode dn = parseExpToTree(exp_str);
		return runDomExp(rc,dn, null, null);
	}

	public Object runDomExp(RunContext rc,IDomNode dn, Object defaultobj, Hashtable in_params)
			throws Exception
	{
		//RunContext rc = createNewRunContext();
		rc.setDefaultContextObj(defaultobj);
		if (in_params != null)
		{
			for (Enumeration en = in_params.keys(); en.hasMoreElements();)
			{
				String kn = (String) en.nextElement();
				Object v = in_params.get(kn);
				rc.addVar(kn, v);
			}
		}

		return dn.runGetValue(this, rc);
	}
	
	
	
}

class DefaultGlobal
{

	public static Date to_date(String strv, String pattern) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.parse(strv);
	}

	public static Date to_date(String strv) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.parse(strv);
	}
}