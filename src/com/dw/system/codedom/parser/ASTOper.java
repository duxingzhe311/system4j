/* Generated By:JJTree: Do not edit this line. ASTOper.java */

package com.dw.system.codedom.parser;

import java.io.*;
import java.util.*;

import com.dw.system.codedom.RunContext;
import com.dw.system.codedom.AbstractRunEnvironment;
import com.dw.system.codedom.IDomNode;
import com.dw.system.codedom.IOperRunner;
import com.dw.system.codedom.parser.ASTConst.ValType;

public class ASTOper extends SimpleNode
{
	static Hashtable<String,Integer> OPERSTR2PRIORITY = new Hashtable<String,Integer>();
	
	static
	{
		OPERSTR2PRIORITY.put("or", 1);
		OPERSTR2PRIORITY.put("||", 1);
		OPERSTR2PRIORITY.put("|", 1);
		
		OPERSTR2PRIORITY.put("and", 2);
		OPERSTR2PRIORITY.put("&", 2);
		OPERSTR2PRIORITY.put("&&", 2);
		
		OPERSTR2PRIORITY.put(">", 3);
		OPERSTR2PRIORITY.put("<", 3);
		OPERSTR2PRIORITY.put("=", 3);
		OPERSTR2PRIORITY.put("==", 3);
		OPERSTR2PRIORITY.put("!=", 3);
		OPERSTR2PRIORITY.put(">=", 3);
		OPERSTR2PRIORITY.put("<=", 3);
		
		OPERSTR2PRIORITY.put("+", 4);
		OPERSTR2PRIORITY.put("-", 4);
		
		OPERSTR2PRIORITY.put("*", 5);
		OPERSTR2PRIORITY.put("/", 5);
		OPERSTR2PRIORITY.put("%", 5);
		
		OPERSTR2PRIORITY.put(".", 100);
	}
	
	
	
	private String operStr = null;
	
	transient IOperRunner operRunner = null ;

	public ASTOper(int id)
	{
		super(id);
	}

	public ASTOper(UQLTreeParser p, int id)
	{
		super(p, id);
	}
	
	/**
	 * 得到该操作符优先级，值越大，优先级越高
	 * @return
	 */
	public int getOperPriority()
	{
		return OPERSTR2PRIORITY.get(operStr);
	}

	public void setOperStr(String oper_str)
	{
		operStr = oper_str;
	}

	public String getOperStr()
	{
		return operStr;
	}
	
	
	@Override
	public void compileNode(AbstractRunEnvironment env)
	{
		if(env!=null)
		{//环境提供的运行器优先级最高
			operRunner = env.getEnvOperRunner(operStr);
			if(operRunner!=null)
				return ;
		}
		
		if("or".equals(operStr)||"||".equals(operStr)||"|".equals(operStr))
		{
			operRunner = new OperRunnerOr();
		}
		else if("and".equals(operStr)||"&".equals(operStr)||"&&".equals(operStr))
		{
			operRunner = new OperRunnerAnd();
		}
		else if(">".equals(operStr))
		{
			operRunner = new OperRunnerGt();
		}
		else if("<".equals(operStr))
		{
			operRunner = new OperRunnerLt();
		}
		else if("==".equals(operStr))
		{
			operRunner = new OperRunnerEq();
		}
		else if("=".equals(operStr))
		{
			operRunner = new OperRunnerSetVar();
		}
		else if("!=".equals(operStr))
		{
			operRunner = new OperRunnerNe();
		}
		else if(">=".equals(operStr))
		{
			operRunner = new OperRunnerGe();
		}
		else if("<=".equals(operStr))
		{
			operRunner = new OperRunnerLe();
		}
		else if("+".equals(operStr))
		{
			operRunner = new OperRunnerPlus();
		}
		else if("-".equals(operStr))
		{
			operRunner = new OperRunnerMinus();
		}
		else if("*".equals(operStr))
		{
			operRunner = new OperRunnerStar();
		}
		else if("/".equals(operStr))
		{
			operRunner = new OperRunnerSlash();
		}
		else if("%".equals(operStr))
		{
			operRunner = new OperRunnerRem();
		}
		else if(".".equals(operStr))
		{
			//do nothing, . 会被后续的Prop或Func替代
		}
		else
		{
			throw new RuntimeException("unknow oper ="+operStr);
		}
	}
	
	public IOperRunner getOperRunner()
	{
		return operRunner;
	}
	
//	public Object calculate(Object leftv,Object rightv)
//	{
//		return operRunner.calculate(leftv, rightv) ;
//	}
	
	@Override
	public String toString()
	{
		return super.toString()+":" + operStr;
	}
	
	public static class OperRunnerPlus implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			if(leftv instanceof String)
			{
				return ((String)leftv) + rightv ;
			}
			
			if(rightv instanceof String)
			{
				return leftv + ((String)rightv) ;
			}
			
			ValType vt = ASTConst.getNumberCalReturnValueType(leftv, rightv);
			Number leftn = (Number)leftv;
			Number rightn = (Number)rightv;
			if(vt== ValType.BYTE)
				return leftn.byteValue() + rightn.byteValue();
			else if(vt== ValType.INT16)
				return leftn.shortValue() + rightn.shortValue();
			else if(vt== ValType.INT32)
				return leftn.intValue()+rightn.intValue();
			else if(vt==ValType.INT64)
				return leftn.longValue()+rightn.longValue();
			else if(vt== ValType.FLOAT)
				return leftn.floatValue()+rightn.floatValue();
			else if(vt== ValType.DOUBLE)
				return leftn.doubleValue()+rightn.doubleValue();
			else
				throw new IllegalArgumentException("object is not number Val type");
		}
	}

	public static class OperRunnerOr implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftn,IDomNode rightn,
				Object leftv,Object rightv)
		{
			
			Boolean lb = (Boolean)leftv;
			Boolean rb = (Boolean)rightv;
			return lb.booleanValue() || rb.booleanValue();
		}
	}

	public static class OperRunnerAnd implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftn,IDomNode rightn,
				Object leftv,Object rightv)
		{
			Boolean lb = (Boolean)leftv;
			Boolean rb = (Boolean)rightv;
			return lb.booleanValue() && rb.booleanValue();
		}
	}

	public static class OperRunnerMinus implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftn,IDomNode rightn,
				Object leftv,Object rightv)
		{
			ValType vt = ASTConst.getNumberCalReturnValueType(leftv, rightv);
			Number leftnv = (Number)leftv;
			Number rightnv = (Number)rightv;
			if(vt== ValType.BYTE)
				return leftnv.byteValue() - rightnv.byteValue();
			else if(vt== ValType.INT16)
				return leftnv.shortValue() - rightnv.shortValue();
			else if(vt== ValType.INT32)
				return leftnv.intValue()-rightnv.intValue();
			else if(vt==ValType.INT64)
				return leftnv.longValue()-rightnv.longValue();
			else if(vt== ValType.FLOAT)
				return leftnv.floatValue()-rightnv.floatValue();
			else if(vt== ValType.DOUBLE)
				return leftnv.doubleValue()-rightnv.doubleValue();
			else
				throw new IllegalArgumentException("object is not number Val type");
		}
	}

	public static class OperRunnerStar implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			ValType vt = ASTConst.getNumberCalReturnValueType(leftv, rightv);
			Number leftn = (Number)leftv;
			Number rightn = (Number)rightv;
			if(vt== ValType.BYTE)
				return leftn.byteValue() * rightn.byteValue();
			else if(vt== ValType.INT16)
				return leftn.shortValue() * rightn.shortValue();
			else if(vt== ValType.INT32)
				return leftn.intValue()-rightn.intValue();
			else if(vt==ValType.INT64)
				return leftn.longValue()*rightn.longValue();
			else if(vt== ValType.FLOAT)
				return leftn.floatValue()*rightn.floatValue();
			else if(vt== ValType.DOUBLE)
				return leftn.doubleValue()*rightn.doubleValue();
			else
				throw new IllegalArgumentException("object is not number Val type");
		}
	}

	public static class OperRunnerSlash implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			ValType vt = ASTConst.getNumberCalReturnValueType(leftv, rightv);
			Number leftn = (Number)leftv;
			Number rightn = (Number)rightv;
			if(vt== ValType.BYTE)
				return leftn.byteValue() / rightn.byteValue();
			else if(vt== ValType.INT16)
				return leftn.shortValue() / rightn.shortValue();
			else if(vt== ValType.INT32)
				return leftn.intValue()/rightn.intValue();
			else if(vt==ValType.INT64)
				return leftn.longValue()/rightn.longValue();
			else if(vt== ValType.FLOAT)
				return leftn.floatValue()/rightn.floatValue();
			else if(vt== ValType.DOUBLE)
				return leftn.doubleValue()/rightn.doubleValue();
			else
				throw new IllegalArgumentException("object is not number Val type");
		}
	}

	public static class OperRunnerRem implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			ValType vt = ASTConst.getNumberCalReturnValueType(leftv, rightv);
			Number leftn = (Number)leftv;
			Number rightn = (Number)rightv;
			if(vt== ValType.BYTE)
				return leftn.byteValue() % rightn.byteValue();
			else if(vt== ValType.INT16)
				return leftn.shortValue() % rightn.shortValue();
			else if(vt== ValType.INT32)
				return leftn.intValue()%rightn.intValue();
			else if(vt==ValType.INT64)
				return leftn.longValue()%rightn.longValue();
			else if(vt== ValType.FLOAT)
				return leftn.floatValue()%rightn.floatValue();
			else if(vt== ValType.DOUBLE)
				return leftn.doubleValue()%rightn.doubleValue();
			else
				throw new IllegalArgumentException("object is not number Val type");
		}
	}

	public static class OperRunnerLt implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			if(leftv instanceof Number && rightv instanceof Number)
			{
				double v = ((Number)leftv).doubleValue()-((Number)rightv).doubleValue();
				return v<0 ;
			}
			
			if(leftv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)<0 ;
			
			if(rightv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)>0 ;
			
			throw new IllegalArgumentException("Object must implements Comparable!");
		}
	}

	public static class OperRunnerLe implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			if(leftv instanceof Number && rightv instanceof Number)
			{
				double v = ((Number)leftv).doubleValue()-((Number)rightv).doubleValue();
				return v<=0 ;
			}
			
			if(leftv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)<=0 ;
			
			if(rightv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)>=0 ;
			
			throw new IllegalArgumentException("Object must implements Comparable!");
		}
	}

	public static class OperRunnerGt implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			if(leftv instanceof Number && rightv instanceof Number)
			{
				double v = ((Number)leftv).doubleValue()-((Number)rightv).doubleValue();
				return v>0 ;
			}
			
			if(leftv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)>0 ;
			
			if(rightv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)<0 ;
			
			throw new IllegalArgumentException("Object must implements Comparable!");
		}
	}

	public static class OperRunnerGe implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			if(leftv instanceof Number && rightv instanceof Number)
			{
				double v = ((Number)leftv).doubleValue()-((Number)rightv).doubleValue();
				return v>=0 ;
			}
			
			if(leftv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)>=0 ;
			
			if(rightv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)<=0 ;
			
			throw new IllegalArgumentException("Object must implements Comparable!");
		}
	}

	public static class OperRunnerNe implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			if(leftv==null)
				return (rightv!=null);
			
			if(rightv==null)
				return (leftv!=null) ;			
			
			if(leftv==rightv)
				return false ;
			
			if(leftv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)!=0 ;
			
			if(rightv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)!=0 ;
			
			throw new IllegalArgumentException("Object must implements Comparable!");
		}
	}

	public static class OperRunnerEq implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv)
		{
			if(leftv==null)
				return (rightv==null);
			
			if(rightv==null)
				return (leftv==null) ;			
			
			if(leftv==rightv)
				return true ;
			
			if(leftv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)==0 ;
			
			if(rightv instanceof Comparable)
				return ((Comparable)leftv).compareTo(rightv)==0 ;
			
			throw new IllegalArgumentException("Object must implements Comparable!");
		}
	}

	public static class OperRunnerSetVar implements IOperRunner
	{
		public Object calculate(AbstractRunEnvironment env,RunContext context,
				IDomNode leftdn,IDomNode rightdn,
				Object leftv,Object rightv) throws Exception
		{
			if(leftdn instanceof ASTVar)
			{
				ASTVar v = (ASTVar)leftdn;
				String n = v.getName() ;
				context.setValValue(n, rightv) ;
				return rightv ;
			}
			else if(leftdn instanceof ASTExpProp)
			{
				((ASTExpProp)leftdn).runSetValue(env,
						context,rightv);
			}
			
			return null ;
		}
	}

}


