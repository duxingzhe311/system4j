package com.dw.system.exp;

import java.io.*;
import java.util.*;

public class Expression
{
	ExpressionHandler expHandler = null;
	ThreeItem rootFA = null;
	String strExp = null;

	public Expression(String strexp, ThreeItem rootfa)
	{
		strExp = strexp;
		rootFA = rootfa;
	}

	public String getStr()
	{
		return strExp;
	}

	public Object clone()
	{
		return new Expression(strExp, rootFA);
	}

	public void setHandler(ExpressionHandler handler)
	{
		expHandler = handler;
	}

	public Object operate()
	{
		return operate(rootFA).arg;
	}

	ThreeItem getRootThreeItem()
	{
		return rootFA;
	}

	Argument operate(ThreeItem ti)
	{
		if (rootFA == null || expHandler == null)
		{
			return null;
		}
		Object arg1 = ti.arg1.arg;
		Object arg2 = ti.arg2.arg;
		String oper = ti.oper;

		return expHandler.onExpression(
			( (arg1 instanceof ThreeItem) ? (operate( (ThreeItem) arg1)) :
			 ti.arg1),
			( (arg2 instanceof ThreeItem) ? (operate( (ThreeItem) arg2)) :
			 ti.arg2),
			oper);
	}

	public String toString()
	{
		return rootFA.toString("", 4);
	}

	public Expression[] filterBoolExpSatisify(Expression[] exps,
											  BoolExpHandler beh)
	{
		if (exps == null)
		{
			return null;
		}

		Expression[] retexps = new Expression[exps.length];
		int c = 0;
		Hashtable ht = new Hashtable();
		for (int i = 0; i < exps.length; i++)
		{
			if (exps[i] == null)
			{
				continue;
			}

			Boolean b = (Boolean) boolOperate(exps[i].getRootThreeItem(), beh,
											  ht).arg;
			if (!b.booleanValue())
			{
				continue;
			}
			retexps[c] = exps[i];
			c++;
		}

		Expression[] rexps = new Expression[c];
		System.arraycopy(retexps, 0, rexps, 0, c);
		return rexps;
	}

	Argument boolOperate(ThreeItem ti, BoolExpHandler beh, Hashtable ht)
	{
		Argument retArg = (Argument) ht.get(ti);
		if (retArg != null)
		{
			return retArg;
		}

		try
		{
			Object arg1 = ti.arg1.arg;
			Object arg2 = ti.arg2.arg;
			String oper = ti.oper;

			boolean b1, b2;
			//judge if gotten
			boolean bg1 = false, bg2 = false;
			Argument tmparg;

			switch (beh.getBoolType(oper))
			{
				case BoolExpHandler.BOOL_AND:
					if (arg1 instanceof Boolean)
					{
						b1 = ( (Boolean) arg1).booleanValue();
						if (!b1)
						{
							retArg = new Argument();
							retArg.type = ExpType.TYPE_CONSTANT_BOOL;
							retArg.arg = new Boolean(false);
							return retArg;
						}
						bg1 = true;
					}

					if (arg2 instanceof Boolean)
					{
						b2 = ( (Boolean) arg2).booleanValue();
						if (!b2)
						{
							retArg = new Argument();
							retArg.type = ExpType.TYPE_CONSTANT_BOOL;
							retArg.arg = new Boolean(false);
							return retArg;
						}
						bg2 = true;
					}

					if (bg1 && bg2)
					{
						retArg = new Argument();
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(true);
						return retArg;
					}

					if (!bg1)
					{
						tmparg = boolOperate( (ThreeItem) arg1, beh, ht);
						b1 = ( (Boolean) tmparg.arg).booleanValue();
						if (!b1)
						{
							retArg = new Argument();
							retArg.type = ExpType.TYPE_CONSTANT_BOOL;
							retArg.arg = new Boolean(false);
							return retArg;
						}
						bg1 = true;
					}

					tmparg = boolOperate( (ThreeItem) arg2, beh, ht);
					b2 = ( (Boolean) tmparg.arg).booleanValue();

					retArg = new Argument();
					retArg.type = ExpType.TYPE_CONSTANT_BOOL;
					retArg.arg = new Boolean(b2);
					return retArg;
				case BoolExpHandler.BOOL_OR:

					if (arg1 instanceof Boolean)
					{
						b1 = ( (Boolean) arg1).booleanValue();
						if (b1)
						{
							retArg = new Argument();
							retArg.type = ExpType.TYPE_CONSTANT_BOOL;
							retArg.arg = new Boolean(true);
							return retArg;
						}
						bg1 = true;
					}

					if (arg2 instanceof Boolean)
					{
						b2 = ( (Boolean) arg2).booleanValue();
						if (b2)
						{
							retArg = new Argument();
							retArg.type = ExpType.TYPE_CONSTANT_BOOL;
							retArg.arg = new Boolean(true);
							return retArg;
						}
						bg2 = true;
					}

					if (bg1 && bg2)
					{
						retArg = new Argument();
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(false);
						return retArg;
					}

					if (!bg1)
					{
						tmparg = boolOperate( (ThreeItem) arg1, beh, ht);
						b1 = ( (Boolean) tmparg.arg).booleanValue();
						if (b1)
						{
							retArg = new Argument();
							retArg.type = ExpType.TYPE_CONSTANT_BOOL;
							retArg.arg = new Boolean(true);
							return retArg;
						}
						bg1 = true;
					}

					tmparg = boolOperate( (ThreeItem) arg2, beh, ht);
					b2 = ( (Boolean) tmparg.arg).booleanValue();

					retArg = new Argument();
					retArg.type = ExpType.TYPE_CONSTANT_BOOL;
					retArg.arg = new Boolean(b2);
					return retArg;
				case BoolExpHandler.OTHER:
					return retArg = beh.onExpression(
						( (arg1 instanceof ThreeItem) ?
						 (boolOperate( (ThreeItem) arg1, beh, ht)) : ti.arg1),
						( (arg2 instanceof ThreeItem) ?
						 (boolOperate( (ThreeItem) arg2, beh, ht)) : ti.arg2),
						oper);
				default:
					throw new RuntimeException(
						"Expression.boolOperate Error:Unknown oper type["
						+ beh.getBoolType(oper) + "]for [" + oper + "]");
			}

		}
		finally
		{
			if (retArg != null)
			{
				ht.put(ti, retArg);
			}
		}
	}

	public boolean boolOperate(Expression exp, BoolExpHandler beh)
	{
		Boolean b = (Boolean) boolOperate(exp.getRootThreeItem(), beh).arg;
		return b.booleanValue();
	}

	Argument boolOperate(ThreeItem ti, BoolExpHandler beh)
	{
		Object arg1 = ti.arg1.arg;
		Object arg2 = ti.arg2.arg;
		String oper = ti.oper;

		Argument retArg = new Argument();
		boolean b1, b2;
		//judge if gotten
		boolean bg1 = false, bg2 = false;
		Argument tmparg;

		switch (beh.getBoolType(oper))
		{
			case BoolExpHandler.BOOL_AND:

				if (arg1 instanceof Boolean)
				{
					b1 = ( (Boolean) arg1).booleanValue();
					if (!b1)
					{
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(false);
						return retArg;
					}
					bg1 = true;
				}

				if (arg2 instanceof Boolean)
				{
					b2 = ( (Boolean) arg2).booleanValue();
					if (!b2)
					{
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(false);
						return retArg;
					}
					bg2 = true;
				}

				if (bg1 && bg2)
				{
					retArg.type = ExpType.TYPE_CONSTANT_BOOL;
					retArg.arg = new Boolean(true);
					return retArg;
				}

				if (!bg1)
				{
					tmparg = boolOperate( (ThreeItem) arg1, beh);
					b1 = ( (Boolean) tmparg.arg).booleanValue();
					if (!b1)
					{
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(false);
						return retArg;
					}
					bg1 = true;
				}

				tmparg = boolOperate( (ThreeItem) arg2, beh);
				b2 = ( (Boolean) tmparg.arg).booleanValue();

				retArg.type = ExpType.TYPE_CONSTANT_BOOL;
				retArg.arg = new Boolean(b2);
				return retArg;
			case BoolExpHandler.BOOL_OR:

				if (arg1 instanceof Boolean)
				{
					b1 = ( (Boolean) arg1).booleanValue();
					if (b1)
					{
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(true);
						return retArg;
					}
					bg1 = true;
				}

				if (arg2 instanceof Boolean)
				{
					b2 = ( (Boolean) arg2).booleanValue();
					if (b2)
					{
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(true);
						return retArg;
					}
					bg2 = true;
				}

				if (bg1 && bg2)
				{
					retArg.type = ExpType.TYPE_CONSTANT_BOOL;
					retArg.arg = new Boolean(false);
					return retArg;
				}

				if (!bg1)
				{
					tmparg = boolOperate( (ThreeItem) arg1, beh);
					b1 = ( (Boolean) tmparg.arg).booleanValue();
					if (b1)
					{
						retArg.type = ExpType.TYPE_CONSTANT_BOOL;
						retArg.arg = new Boolean(true);
						return retArg;
					}
					bg1 = true;
				}

				tmparg = boolOperate( (ThreeItem) arg2, beh);
				b2 = ( (Boolean) tmparg.arg).booleanValue();

				retArg.type = ExpType.TYPE_CONSTANT_BOOL;
				retArg.arg = new Boolean(b2);
				return retArg;
			case BoolExpHandler.OTHER:
				return beh.onExpression(
					( (arg1 instanceof ThreeItem) ?
					 (boolOperate( (ThreeItem) arg1, beh)) : ti.arg1),
					( (arg2 instanceof ThreeItem) ?
					 (boolOperate( (ThreeItem) arg2, beh)) : ti.arg2),
					oper);
			default:
				throw new RuntimeException(
					"Expression.boolOperate Error:Unknown oper type["
					+ beh.getBoolType(oper) + "]for [" + oper + "]");
		}
	}
}