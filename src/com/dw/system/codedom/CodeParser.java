package com.dw.system.codedom;

import java.io.InputStream;
import java.io.StringReader;
import java.util.*;

import com.dw.system.codedom.parser.*;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

/**
 * 代码解析对象模型
 * 
 * 用户系统查询语言 .orgunit[0].user select $us=users where name like 'a*' and
 * ext.propname='' or role_set.contains select $u=user where
 * $u.belongto_orgunit=$u1.belongto_orgunit and has_role('dept_mgr') select role
 * select orgunit
 * 
 * @author Jason Zhu
 */
public class CodeParser
{
	static String[] test_exp = new String[] {
			"true",
			"1==1",
			"\"abc\"+5",
			"to_date(\"2006-2-5\",\"yyyy-MM-dd\").getTime()",
			"to_date(\"2006-2-5\",\"yyyy-MM-dd\").getTime()==to_date(\"2006-2-5\",\"yyyy-MM-dd\").time",
			"user=\'\\u0055\'",
			"true or ((user=3) and (role=aa(@mm=1))) or @vv=@abc",
			"true",
			"hasRole(\"dept_\\\"mgr哈哈\",11)",
			"@u.belongto_orgunit",
			"ff(1e137)=0",
			"user.id=3 or role.id=8 and to_int(@v)+8=9",
			"(user.id=3 or role.id=.5f) and to_int(@v)+8=9 or user.name.charAt(0)='a' or pp='\\r'",
			"user.containsRole(\"true=dept_mgr\") and user.belongto_orgunit=@app_user.belongto_orgunit",
			"0b0101L+0o777*3e8f-55s",
			"aa.bb.ccc>1 && @performer=\"小王\"",
			};

	static String[] test_code = new String[] {
			"{" + "@v=33;0b0101L+0o777*3e8f-55s;}",
			"{if(@v==33)" + "{var @mm,@kk=to_str(34234);" + "	@v=4; abc=5;"
					+ "}else @v=5;}", };
	
	static String[] test_exp_run = new String[] {
//		"to_date(\"2006-2-5\",\"yyyy-MM-dd\").time",
//		"to_date(\"2006-2-5\",\"yyyy-MM-dd\").getTime()",
//		"to_date(\"2006-2-5\",\"yyyy-MM-dd\").getTime()==to_date(\"2006-2-5\",\"yyyy-MM-dd\").time",
		"gc2.gc3.xxx",
		"\"abc\"+5",
		"6l+5",
		"@kk=5;@mm=0b0101L+0o777*3e8f-55s;"
		};
	
	static String[] test_code_run = new String[] {
		"var @s=true;if(@s)@kk=5;else @mm=0b0101L+0o777*3e8f-55s;",
		
		"{var @zzy;@zzy=\"1234\";\r\n//阿说的发\r\n" +
		"if(@zzy==\"123\")\r\n" +
		" @kk=5;" +
		"else\r\n" +
		"{\r\n" +
		"   @ss=123d;@zzj=true;\r\n @title=\"案件:\"+@ss+@datafield.Title;" +
		"}\r\n ;}"+
		"@datafield.Title=@title;"
		};

	public static void main(String args[])
	{
		System.out.println("Reading from standard input..." + 011);

		try
		{
			IDomNode n = null;
			for (String s : test_exp)
			{
				System.out.println("\nparse-exp->" + s);
				n = AbstractRunEnvironment.parseExpToTree(s, null);
				n.dump();
			}

			for (String s : test_code)
			{
				System.out.println("\nparse-code->" + s);
				n = AbstractRunEnvironment.parseCodeBlockToTree(s, null);
				n.dump();
			}

			TestRunEnv runEnv = new TestRunEnv();
			runEnv.init();

			Hashtable in_params = new Hashtable();
			for (String s : test_exp_run)
			{
				try
				{
					n = AbstractRunEnvironment.parseExpToTree(s, null);
					n.dump();
					
					System.out.println("\nrun-exp->" + s);
					RunContext cxt = new RunContext() ;
					Object v = runEnv.runDomExp(cxt,n, null, in_params);
					System.out.println(" result==" + v+ "  class="+(v==null?"null":v.getClass().getCanonicalName()));
					//System.out.println(" cxt =="+cxt.toString()) ;
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
			
			for (String s : test_code_run)
			{
				try
				{
					n = AbstractRunEnvironment.parseCodeBlockToTree("{"+s+"}", null);
					
					
					System.out.println("\nrun-code->" + s);
					TestRunCxt cxt = new TestRunCxt() ;
					Object v = runEnv.runDomExp(cxt,n, null, in_params);
					System.out.println(" result==" + v+ "  class="+(v==null?"null":v.getClass().getCanonicalName()));
					System.out.println(" cxt =="+cxt.toString()) ;
					n.dump();
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
			
			
		}
		catch (Exception e)
		{
			System.out.println("Oops.");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	static class TestRunEnv extends AbstractRunEnvironment
	{

		@Override
		protected Object[] globalSupportedObj()
		{
			
			return new Object[]{new TestGC1()};
		}

		@Override
		public IOperRunner getEnvOperRunner(String oper_str)
		{
			return null;
		}

		
	}
	
	static class TestRunCxt extends RunContext
	{
		ValWrapper datafield = new ValWrapper() ;
		XmlData xd = new XmlData() ;
		
		public TestRunCxt()
		{
			
			xd.setParamValue("Title", "呵呵TTTT") ;
			datafield = new ValWrapper(xd) ;
		}
		
		public ValWrapper getValueWrapper(String var_name)
			throws Exception
		{
			if("@datafield".equalsIgnoreCase(var_name))
			{
				return datafield ;
			}
			
			return super.getValueWrapper(var_name);
		}
		
		public void setValValue(String var_name,Object v)
		{
			ValWrapper vw = globalVal.get(var_name);
			if(vw!=null)
			{
				vw.val = v;
				return ;
			}
			else
			{
				vw = new  ValWrapper(v) ;
				globalVal.put(var_name, vw) ;
			}
		}

		public String toString()
		{
			return super.toString()+"\n DataField"+xd.toXmlString() ;
		}
	}
	
	
	
	static class TestGC1
	{
		public TestGC2 gc2 = new TestGC2() ;
	}
	
	static class TestGC2
	{
		public TestGC3 gc3 = new TestGC3();
	}
	
	static class TestGC3
	{
		public String xxx = "hehe哈" ;
	}
}
