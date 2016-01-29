package com.dw.system.exp ;

import java.io.* ;
import java.util.* ;
import java.text.* ;
import java.util.regex.*;

//import org.apache.oro.text.regex.*;

public class AtomItemEnum implements Enumeration
{
	//变量串的表达式定义 ^[a-z|A-Z|_][a-z|A-Z|_|0-9]*
	public final static String REGEXP_VAR = "[a-z|A-Z|_|\\u0100-][a-z|A-Z|_|0-9|\\u0100-]*" ;
	//基本操作符  >=|<=|==|!=|\|\||&&|&|\||=|>|<|\+|\-|\*
	public final static String REGEXP_BASEOPER = ">=|<=|==|!=|\\|\\||&&|&|\\||=|>|<|\\+|\\-|\\*" ;
	public final static String REGEXP_INTEGER = "[0-9]+" ;
	public final static String REGEXP_SHORT = "[0-9]+[s|S]" ;
	public final static String REGEXP_LONG = "[0-9]+[l|L]" ;
	public final static String REGEXP_BOOL = "[0|1][b|B]" ;
	// 浮点数[0-9]+.[0-9]*
	public final static String REGEXP_FLOAT = "[0-9]+\\.[0-9]*" ;
	public final static String REGEXP_DOUBLE = "[0-9]+\\.[0-9]*[d|D]" ;
	//时间 [0-9]{4}/(?:0[1-9]|1[0-2])/(?:0[1-9]|[1-2][0-9]|3[0-1]):(?:[0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]
	public final static String REGEXP_DATE_SECOND = "[0-9]{4}/(?:0[1-9]|1[0-2])/(?:0[1-9]|[1-2][0-9]|3[0-1]):(?:[0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]" ;
	public final static String REGEXP_DATE_DAY = "[0-9]{4}/(?:0[1-9]|1[0-2])/(?:0[1-9]|[1-2][0-9]|3[0-1])" ;


	//public final static String REGEXP_STRING = "\"[^\"]\"" ;
	//分隔符 [!-/|:-@|[-^|`|{-\uFFFD]
	public final static String REGEXP_SPLIT = "[!-/|:-@|[-^|`|{-\uFFFD]" ;



	//static PatternCompiler compiler = null ;

	static Pattern pattern_var = null ;
	static Pattern pattern_integer = null ;
	static Pattern pattern_short = null ;
	static Pattern pattern_long = null ;
	static Pattern pattern_bool = null ;
	static Pattern pattern_float = null ;
	static Pattern pattern_double = null ;
	static Pattern pattern_baseoper = null ;
	static Pattern pattern_date_second = null ;
	static Pattern pattern_date_day = null ;

	//static PatternMatcher matcher = new Perl5Matcher () ;

	static
	{
		try
		{
			//compiler = new Perl5Compiler () ;
			pattern_var      = Pattern.compile (REGEXP_VAR) ;
			pattern_integer  = Pattern.compile (REGEXP_INTEGER) ;
			pattern_short    = Pattern.compile (REGEXP_SHORT) ;
			pattern_long     = Pattern.compile (REGEXP_LONG) ;
			pattern_bool     = Pattern.compile (REGEXP_BOOL) ;
			pattern_float    = Pattern.compile (REGEXP_FLOAT) ;
			pattern_double   = Pattern.compile (REGEXP_DOUBLE) ;
			pattern_baseoper   = Pattern.compile (REGEXP_BASEOPER) ;
			pattern_date_second   = Pattern.compile (REGEXP_DATE_SECOND) ;
			pattern_date_day   = Pattern.compile (REGEXP_DATE_DAY) ;
		}
		catch (Exception e)
		{
			e.printStackTrace () ;
		}
	}

	int p = -1 ;
	char[] buf = null ;
	short nexttype = -1 ;
	Object nextvalue = null ;

	public AtomItemEnum (String inputstr)
	{
		buf = inputstr.toCharArray () ;
		//inputStr = inputstr ;
	}

	public AtomItemEnum (char[] buffer)
	{
		buf = buffer ;
	}

	public boolean hasMoreElements ()
	{
		//return matcher.matchesPrefix (buf,pattern,p) ;
		if (nextvalue!=null)
			return true ;

		p ++ ;
		if (p>=buf.length)
			return false ;

		while (buf[p]==' '||buf[p]=='\r'||buf[p]=='\n'||buf[p]=='\t')
		{
			p ++ ;
			if (p>=buf.length)
				return false ;
			continue ;
		}

		if (buf[p]=='\"')
		{
			return getConstantString () ;
		}
		else if (buf[p]=='(')
		{
			nextvalue = new Character ('(') ;
			nexttype = ExpType.TYPE_L_BRACKET ;
			return true ;
		}
		else if (buf[p]==')')
		{
			nextvalue = new Character (')') ;
			nexttype = ExpType.TYPE_R_BRACKET ;
			return true ;
		}
		else if (buf[p]=='{')
		{
			return getConstantSet () ;
		}
		else if (buf[p]=='[')
		{
			//return getConstantArray () ;
		}
		else if (buf[p]=='.')
		{
			nextvalue = new Character ('.') ;
			nexttype = ExpType.TYPE_POINT ;
			return true ;
		}
		else if (buf[p]>='0'&&buf[p]<='9')
		{
			return getConstantNum () ;
		}
		else if (buf[p]>='A'&&buf[p]<='Z'||buf[p]>='a'&&buf[p]<='z'||buf[p]=='_'||buf[p]>'\u0100')
		{
			return getVar () ;
		}

		boolean boper = getBaseOper () ;
		if (boper)
			return true ;

		//throw new ExpException ("Illegal char [ "+buf[p]+" ]") ;
		return false ;
		//return matcher.contains (inputStr.substring(p),pattern) ;
	}

	public short nextType ()
	{
		if (hasMoreElements ())
		{
			//System.out.println ("nexttype====["+nexttype+"]") ;
			return nexttype ;
		}
		else
			return ExpType.TYPE_ERROR ;
	}

	public Object nextElement ()
	{
		if (nextvalue!=null)
		{
			Object tmpo = nextvalue ;
			nextvalue = null ;
			nexttype = ExpType.TYPE_ERROR ;
			//System.out.println ("nextele====["+tmpo+"]") ;
			return tmpo ;
		}
		return null ;
	}


	private boolean getConstantString ()
	{//p already point to "
		StringBuffer tmpsb = new StringBuffer (buf.length) ;
		while (p<buf.length)
		{
			p ++ ;
			if (p==buf.length)
				throw new ExpException ("Find string but it has no end [\"]!") ;
			if (buf[p]=='\\')
			{
				p ++ ;
				if (p==buf.length)
					throw new ExpException ("Find string but it has no end [\"]!") ;
				tmpsb.append (buf[p]) ;
				continue ;
			}
			else if (buf[p]!='\"')
			{
				tmpsb.append (buf[p]) ;
				continue ;
			}
			else
			{
				nextvalue = tmpsb.toString () ;
				nexttype = ExpType.TYPE_CONSTANT_STRING ;
				return true ;
			}
		}
		return false ;
	}
	/*
	public void test ()
	{
		PatternMatcher matcher = new Perl5Matcher () ;
		String tmps = "categroy.adfasd" ;
		if (matcher.matchesPrefix(tmps, pattern_var))
		{

			System.out.println ( matcher.getMatch().group(0)) ;

		}
		else
			System.out.println ( "not match!") ;
	}*/
	/*
	private boolean getVar ()
	{//p is already point var first char
		PatternMatcher matcher = new Perl5Matcher () ;
		System.out.print ("\ngetVar p="+p) ;
		//if (matcher.matchesPrefix(new String(buf,p,buf.length-p), pattern_var))
		if (matcher.matchesPrefix(buf, pattern_var,p))
		{
			nexttype = ExpType.TYPE_VAR ;
			nextvalue = matcher.getMatch().group(0) ;
			System.out.println ("   "+nextvalue+"\n") ;
			p += ((String)nextvalue).length ()-1 ;
			return true ;
		}
		throw new ExpException ("Illegal var or str oper number=[..."+new String(buf,p,buf.length-p)+"]!") ;
	}
	*/

	private boolean isVarHeadChar (char c)
	{
		//[a-z|A-Z|_|\\u0100-]
		if ( (c>='a'&&c<='z')||
			 (c>='A'&&c<='Z')||
			 (c=='_')||
			 (c>='\u0100'&&c<='\uffff'))
			 return true ;
		else
			return false ;
	}

	private boolean isVarContChar (char c)
	{//[a-z|A-Z|_|0-9|\\u0100-]*
		if ( (c>='a'&&c<='z')||
			 (c>='A'&&c<='Z')||
			 (c>='0'&&c<='9')||
			 (c=='_')||
			 (c>='\u0100'&&c<='\uffff'))
			 return true ;
		else
			return false ;
	}

	private boolean getVar ()
	{//p is already point var first char

		//System.out.print ("\ngetVar p="+p) ;
		int tmpp = p ;
		//if (matcher.matchesPrefix(new String(buf,p,buf.length-p), pattern_var))
		if (!isVarHeadChar(buf[tmpp]))
			throw new ExpException ("Illegal var or str oper number=[..."+new String(buf,p,buf.length-p)+"]!") ;

		while (true)
		{
			tmpp ++ ;
			if (tmpp>=buf.length)
				break ;
			if (!isVarContChar (buf[tmpp]))
				break ;
		}
		nexttype = ExpType.TYPE_VAR ;
		nextvalue = new String (buf,p,tmpp-p) ;
		p += ((String)nextvalue).length ()-1 ;
		return true ;
	}

	private boolean getBaseOper ()
	{
		//PatternMatcher matcher = new Perl5Matcher () ;
		//if (matcher.matchesPrefix(new String(buf,p,buf.length-p), pattern_baseoper))
		
		Matcher m = pattern_baseoper.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_BASEOPER ;
			nextvalue = m.group(0);//matcher.getMatch().group(0) ;
			p += ((String)nextvalue).length ()-1 ;
			return true ;
		}
		throw new ExpException ("Illegal base oper number=[..."+new String(buf,p,buf.length-p)+"]!") ;
	}

	private boolean getConstantNum ()
	{//p is already point num first char
		//PatternMatcher matcher = new Perl5Matcher () ;

		//if (matcher.matchesPrefix(buf, pattern_double,p))
		Matcher m = pattern_double.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_DOUBLE ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			nextvalue = new Double(tmps.substring(0,tmps.length()-1)) ;
			p += tmps.length ()-1 ;
			return true ;
		}

		//if (matcher.matchesPrefix(buf, pattern_bool,p))
		m = pattern_bool.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_BOOL ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			if (tmps.charAt(0)=='0')
				nextvalue = new Boolean (false) ;
			else
				nextvalue = new Boolean (true) ;
			p += tmps.length ()-1 ;
			return true ;
		}

		//if (matcher.matchesPrefix(buf, pattern_date_second,p))
		m = pattern_date_second.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_DATE ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd:HH:mm:ss");

			// Parse the previous string back into a Date.
			ParsePosition pos = new ParsePosition(0);
			nextvalue = formatter.parse(tmps, pos);

			p += tmps.length ()-1 ;
			return true ;
		}
		
		//if (matcher.matchesPrefix(buf, pattern_date_day,p))
		m = pattern_date_day.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_DATE ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd");

			// Parse the previous string back into a Date.
			ParsePosition pos = new ParsePosition(0);
			nextvalue = formatter.parse(tmps, pos);

			p += tmps.length ()-1 ;
			return true ;
		}
		
		//if (matcher.matchesPrefix(buf, pattern_float,p))
		m = pattern_float.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_FLOAT ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			nextvalue = new Float(tmps.substring(0,tmps.length())) ;
			p += tmps.length ()-1 ;
			return true ;
		}
		
		//if (matcher.matchesPrefix(buf, pattern_short,p))
		m = pattern_short.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_SHORT ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			nextvalue = new Short(tmps.substring(0,tmps.length()-1)) ;
			p += tmps.length ()-1 ;
			return true ;
		}
		
		//if (matcher.matchesPrefix(buf, pattern_long,p))
		m = pattern_long.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_LONG ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			nextvalue = new Long(tmps.substring(0,tmps.length()-1)) ;
			p += tmps.length ()-1 ;
			return true ;
		}


		//if (matcher.matchesPrefix(buf, pattern_integer,p))
		m = pattern_integer.matcher(new String(buf,p,buf.length-p));
		if (m.find())
		{
			nexttype = ExpType.TYPE_CONSTANT_INTEGER ;
			String tmps = m.group(0);//matcher.getMatch().group (0) ;
			nextvalue = new Integer(tmps.substring(0,tmps.length())) ;
			p += tmps.length ()-1 ;
			return true ;
		}
		throw new ExpException ("Illegal constant number=[..."+new String(buf,p,buf.length-p)+"]!") ;
	}

	private boolean getConstantSet ()
	{//p is already point to {
		HashSet hset = new HashSet () ;

		while (p<buf.length&&buf[p]!='}')
		{
			p ++ ;
			if (p==buf.length)
				throw new ExpException ("Find set but it has no end [}]!") ;
			if (buf[p]==' '||buf[p]==',')
			{
				//p ++ ;
				continue ;
			}

			if (buf[p]=='\"')
			{
				boolean bstr = getConstantString () ;
				hset.add (nextvalue) ;
				continue ;
			}
			else if (buf[p]>='0'&&buf[p]<='9')
			{
				boolean bcn = getConstantNum () ;
				hset.add (nextvalue) ;
				continue ;
			}
			else if (buf[p]=='}')
				break ;
			else
				throw new ExpException ("Illegal constant set =[..."+new String(buf,p,buf.length-p)+"]!") ;
		}

		if (buf[p]=='}')
		{
			nextvalue = hset ;
			nexttype = ExpType.TYPE_CONSTANT_SET ;
			return true ;
		}
		throw new ExpException ("Find set but it has no end [}]!") ;
	}

	/*
	private boolean getConstantArray ()
	{//p is already point to {
		HashSet hset = new HashSet () ;

		while (p<buf.length&&buf[p]!=']')
		{
			p ++ ;
			if (p==buf.length)
				throw new ExpException ("Find array but it has no end []]!") ;
			if (buf[p]==' '||buf[p]==',')
			{
				//p ++ ;
				continue ;
			}

			if (buf[p]=='\"')
			{
				boolean bstr = getConstantString () ;
				hset.add (nextvalue) ;
				continue ;
			}
			else if (buf[p]>='0'&&buf[p]<='9')
			{
				boolean bcn = getConstantNum () ;
				hset.add (nextvalue) ;
				continue ;
			}
			else if (buf[p]=='}')
				break ;
			else
				throw new ExpException ("Illegal constant set =[..."+new String(buf,p,buf.length-p)+"]!") ;
		}

		if (buf[p]=='}')
		{
			nextvalue = hset ;
			nexttype = ExpType.TYPE_CONSTANT_SET ;
			return true ;
		}
		throw new ExpException ("Find set but it has no end [}]!") ;
	}*/
	public static void main (String[] args)
	{
		AtomItemEnum en = new AtomItemEnum (args[0]) ;
		System.out.println ("INput="+args[0]);

		while (en.hasMoreElements())
		{
			System.out.println ("type="+en.nextType()+" val="+en.nextElement()) ;
		}
		//enum.test () ;
	}

}