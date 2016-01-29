package com.dw.system.exp;

import java.io.*;
import java.util.*;

import com.dw.system.cache.*;

public class Parser
{
	static Cacher cache = Cacher.getCacher("_COM.DW.SYSTEM.EXP");
	static
	{
		cache.setCacheLength(3500);
	}

	Stack itemStack = new Stack();

	Stack operStack = new Stack();

	public Expression parse(String rule)
	{// System.out.println ("---"+rule) ;
		Expression rete = (Expression) cache.get(rule);
		if (rete != null)
			return (Expression) rete.clone();

		itemStack.clear();
		operStack.clear();
		ThreeItem root = parseX(rule.toCharArray());
		rete = new Expression(rule, root);
		cache.cache(rule, rete);
		return (Expression) rete.clone();
	}

	/*
	 * private ThreeItem parse (char[] input) { AtomItemEnum atomEnum = new
	 * AtomItemEnum (input) ; ThreeItem root = null ;
	 * 
	 * Stack tistack = new Stack () ;
	 * 
	 * Object arg = null ; String oper = null ; String tmps = null ; ThreeItem
	 * tmpti = null ; while (atomEnum.hasMoreElements()) { switch
	 * (atomEnum.nextType ()) { case ExpType.TYPE_CONSTANT_STRING: case
	 * ExpType.TYPE_CONSTANT_INTEGER: case ExpType.TYPE_CONSTANT_SHORT: case
	 * ExpType.TYPE_CONSTANT_LONG: case ExpType.TYPE_CONSTANT_FLOAT: case
	 * ExpType.TYPE_CONSTANT_DOUBLE: case ExpType.TYPE_CONSTANT_SET: case
	 * ExpType.TYPE_CONSTANT_DATE: case ExpType.TYPE_CONSTANT_BOOL:
	 * 
	 * short tmptype = atomEnum.nextType () ;
	 * 
	 * arg = atomEnum.nextElement () ; if (tistack.empty()) { tmpti = new
	 * ThreeItem () ; if (root==null) root = tmpti ; tmpti.arg1.arg = arg ;
	 * tmpti.arg1.type = tmptype ; tistack.push (tmpti) ; } else { tmpti =
	 * (ThreeItem)tistack.peek () ; if (tmpti.arg1.arg==null) { tmpti.arg1.arg =
	 * arg ; tmpti.arg1.type = tmptype ; } else if (tmpti.oper==null) throw new
	 * ExpException ("Illegal operation with ["+arg+"](1)!") ; else if
	 * (tmpti.arg2.arg==null) { tmpti.arg2.arg = arg ; tmpti.arg2.type = tmptype ;
	 * if (!tmpti.bracket) tistack.pop () ; } else throw new ExpException
	 * ("Illegal expression with ["+arg+"]may be less \')\' before it!") ; }
	 * break ; case ExpType.TYPE_VAR: tmps = (String)atomEnum.nextElement () ;
	 * while
	 * (atomEnum.hasMoreElements()&&atomEnum.nextType()==ExpType.TYPE_POINT) {
	 * atomEnum.nextElement() ; if
	 * (!atomEnum.hasMoreElements()||atomEnum.nextType()!=ExpType.TYPE_VAR)
	 * throw new ExpException ("Illegal variable ["+tmps+".]!") ; tmps +=
	 * ("."+(String)atomEnum.nextElement()) ; }
	 * 
	 * arg = tmps ; if (tistack.empty()) { tmpti = new ThreeItem () ; if
	 * (root==null) { root = tmpti ; tmpti.arg1.arg = arg ; tmpti.arg1.type =
	 * ExpType.TYPE_VAR ; } else { tmpti.arg1.arg = root ; tmpti.oper = tmps ;
	 * root = tmpti ; } tistack.push (tmpti) ; } else { tmpti =
	 * (ThreeItem)tistack.peek () ; if (tmpti.arg1.arg==null) { tmpti.arg1.arg =
	 * arg ; tmpti.arg1.type = ExpType.TYPE_VAR ; } else if (tmpti.oper==null)
	 * tmpti.oper = tmps ; else if (tmpti.arg2.arg==null) { tmpti.arg2.arg = arg ;
	 * tmpti.arg2.type = ExpType.TYPE_VAR ; if (!tmpti.bracket) tistack.pop () ; }
	 * else throw new ExpException ("Illegal expression with ["+arg+"]may be
	 * less ')' before it!") ; } break ; //case ExpType.TYPE_POINT: // break ;
	 * case ExpType.TYPE_BASEOPER: tmps = (String)atomEnum.nextElement () ; arg =
	 * tmps ; if (tistack.empty()) { if (root==null) throw new ExpException
	 * ("Illegal expression with ["+tmps+"] may be less var or const!") ; else {
	 * tmpti = new ThreeItem () ; tmpti.arg1.arg = root ; tmpti.oper = tmps ;
	 * root = tmpti ; tistack.push (tmpti) ; }
	 *  } else { tmpti = (ThreeItem)tistack.peek () ; if (tmpti.arg1.arg==null)
	 * throw new ExpException ("Illegal expression with ["+tmps+"] may be less
	 * var or const!") ; else if (tmpti.oper==null) tmpti.oper = tmps ; else
	 * throw new ExpException ("Illegal expression with ["+tmps+"] may be too
	 * many oper!") ; } break ; case ExpType.TYPE_L_BRACKET:
	 * atomEnum.nextElement () ; ThreeItem tmptitn = new ThreeItem () ;
	 * 
	 * tmptitn.bracket = true ; if (tistack.empty()) { if (root==null) root =
	 * tmptitn ; else throw new ExpException ("Illegal expression with [(] may
	 * be less oper before it!") ; tistack.push (tmptitn) ; } else { tmpti =
	 * (ThreeItem)tistack.peek () ; if (tmpti.arg1.arg==null) tmpti.arg1.arg =
	 * tmptitn ; else if (tmpti.oper==null) throw new ExpException ("Illegal
	 * operation with [(]!") ; else if (tmpti.arg2.arg==null) { tmpti.arg2.arg =
	 * tmptitn ; if (!tmpti.bracket) tistack.pop () ; } tistack.push (tmptitn) ; }
	 * break ; case ExpType.TYPE_R_BRACKET: atomEnum.nextElement () ;
	 * 
	 * if (tistack.empty()) { throw new ExpException ("Illegal expression with
	 * [)] may be too many \')\'!") ; } else { tmpti = (ThreeItem)tistack.peek () ;
	 * if (!tmpti.bracket) throw new ExpException ("Illegal expression with [)]
	 * may be too many \')\'!") ; if (tmpti.arg1.arg==null) throw new
	 * ExpException ("Illegal expression with [()] may be no oper and args !") ;
	 * else if (tmpti.oper==null) throw new ExpException ("Illegal expression
	 * with [(arg)] may be no oper and args !") ; else if (tmpti.arg2.arg==null)
	 * throw new ExpException ("Illegal expression with [(arg oper )] may be
	 * less last arg!") ; tistack.pop () ; } break ; default: throw new
	 * ExpException ("Illegal expression with ["+atomEnum.nextElement()+"](0)!") ; } }
	 * 
	 * return root ; }
	 */

	/**
	 *  ‰»ÎArgument ThreeItem
	 */
	private void inputItem(Object arg)
	{// System.out.println ("InputItem -----"+arg) ;
		if (operStack.empty() || "(".equals(operStack.peek().toString()))
		{
			itemStack.push(arg);
		}
		else
		{
			ThreeItem tmpti = new ThreeItem();
			Object obj = itemStack.pop();
			if (obj instanceof Argument)
			{
				tmpti.arg1 = (Argument) obj;
			}
			else if (obj instanceof ThreeItem)
			{
				tmpti.arg1.arg = obj;
			}
			else
				throw new ExpException(
						"Illegal input item,it must Argument or ThreeItem!");

			if (arg instanceof Argument)
			{
				tmpti.arg2 = (Argument) arg;
			}
			else if (arg instanceof ThreeItem)
			{
				tmpti.arg2.arg = arg;
			}
			else
				throw new ExpException(
						"Illegal input item,it must Argument or ThreeItem!");

			tmpti.oper = (String) operStack.pop();
			inputItem(tmpti);
		}
	}

	/**
	 *  ‰»Î≤Ÿ◊˜∑˚
	 */
	private void inputOper(String oper)
	{// System.out.println ("-oper-----"+oper) ;
		if (")".equals(oper))
		{
			if (!"(".equals(operStack.peek().toString()))
			{
				throw new ExpException("Error,less ( to match!");
			}
			operStack.pop();
			if (operStack.empty() || "(".equals(operStack.peek())
					|| itemStack.size() <= 1)
				return;

			ThreeItem tmpti = new ThreeItem();
			Object obj = itemStack.pop();
			if (obj instanceof Argument)
			{
				tmpti.arg1 = (Argument) obj;
			}
			else if (obj instanceof ThreeItem)
			{
				tmpti.arg1.arg = obj;
			}
			else
				throw new ExpException(
						"Illegal input item,it must Argument or ThreeItem!");
			obj = itemStack.pop();
			if (obj instanceof Argument)
			{
				tmpti.arg2 = (Argument) obj;
			}
			else if (obj instanceof ThreeItem)
			{
				tmpti.arg2.arg = obj;
			}
			else
				throw new ExpException(
						"Illegal input item,it must Argument or ThreeItem!");

			tmpti.oper = (String) operStack.pop();

			inputItem(tmpti);
			return;
		}

		operStack.push(oper);
	}

	private ThreeItem parseX(char[] input)
	{
		AtomItemEnum atomEnum = new AtomItemEnum(input);
		ThreeItem root = null;

		// Stack itemStack = new Stack () ;
		// Stack operStack = new Stack () ;

		// ThreeItem curTI = null ;
		Object arg = null;
		short type = -1;
		String tmps = null;
		Argument tmpa = null;
		boolean bnextoper = false;
		while (atomEnum.hasMoreElements())
		{
			switch (atomEnum.nextType())
			{
			case ExpType.TYPE_CONSTANT_STRING:
			case ExpType.TYPE_CONSTANT_INTEGER:
			case ExpType.TYPE_CONSTANT_SHORT:
			case ExpType.TYPE_CONSTANT_LONG:
			case ExpType.TYPE_CONSTANT_FLOAT:
			case ExpType.TYPE_CONSTANT_DOUBLE:
			case ExpType.TYPE_CONSTANT_SET:
			case ExpType.TYPE_CONSTANT_DATE:
			case ExpType.TYPE_CONSTANT_BOOL:
				bnextoper = true;
				type = atomEnum.nextType();
				arg = atomEnum.nextElement();
				tmpa = new Argument();
				tmpa.type = type;
				tmpa.arg = arg;
				inputItem(tmpa);
				break;
			case ExpType.TYPE_VAR:
				type = atomEnum.nextType();
				tmps = (String) atomEnum.nextElement();
				while (atomEnum.hasMoreElements()
						&& atomEnum.nextType() == ExpType.TYPE_POINT)
				{
					atomEnum.nextElement();
					if (!atomEnum.hasMoreElements()
							|| atomEnum.nextType() != ExpType.TYPE_VAR)
						throw new ExpException("Illegal variable [" + tmps
								+ ".]!");
					tmps += ("." + (String) atomEnum.nextElement());
				}

				arg = tmps;
				if (bnextoper)
				{
					inputOper(tmps);
					bnextoper = false;
					break;
				}
				bnextoper = true;
				tmpa = new Argument();
				tmpa.type = type;
				tmpa.arg = arg;
				inputItem(tmpa);
				break;
			// case ExpType.TYPE_POINT:
			// break ;
			case ExpType.TYPE_BASEOPER:
				tmps = (String) atomEnum.nextElement();
				bnextoper = false;
				inputOper(tmps);
				break;
			case ExpType.TYPE_L_BRACKET:
				atomEnum.nextElement();
				bnextoper = false;
				inputOper("(");
				break;
			case ExpType.TYPE_R_BRACKET:
				atomEnum.nextElement();
				bnextoper = true;
				inputOper(")");
				break;
			default:
				throw new ExpException("Illegal expression with ["
						+ atomEnum.nextElement() + "](0)!");
			}
		}

		if (!operStack.empty())
			throw new ExpException(
					"Illegal expression with too many operation!");
		if (itemStack.size() == 0)
			return null;
		if (itemStack.size() > 1)
			throw new ExpException("Illegal expression with too many item!");

		return (ThreeItem) itemStack.pop();
	}

	public static void main(String[] args)
	{
		Parser p = new Parser();

		try
		{
			String inputLine;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			while ((inputLine = in.readLine()) != null)
			{
				try
				{
					StringTokenizer st = new StringTokenizer(inputLine);
					int c = st.countTokens();
					String cmd[] = new String[c];
					for (int i = 0; i < c; i++)
					{
						cmd[i] = st.nextToken();
					}
					if (cmd.length == 0)
					{
						continue;
					}

					if (cmd[0].equals("input"))
					{ //
						System.out.println("input=" + cmd[1] + "\n---------");
						Expression exp = p.parse(cmd[1]);
						System.out.println(exp);
					}
					else if (cmd[0].equals("ls"))
					{

					}
					else if (cmd[0].equals("exit"))
					{
						System.exit(0);
					}
				}
				catch (Exception _t)
				{
					_t.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
