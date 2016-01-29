package com.dw.mltag ;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/** 
 * A Class to discribe XML #PCDATA.<br/>
 * <b>Bugs: </b><br/>
 * <i>Text have only one characeter will be ommited
 * in method {@link #escapingString(String)} -- FIXED.</i>
 */
public class XmlText extends AbstractNode
{
	static String delimiter = "<>" ;
	static String HTML_DELIMITER = "<>\'\"" ;
    static String XML_DELIMITER = "<>";
	
	/**
	 * 只被Parse调用的方法
	 * 从xml表示的字符串中解析出XmlText的内容列表
	 * @param pstr
	 * @return
	 */
	static XmlText parseToXmlText(String pstr)
	{
		if(pstr==null)
			return null ;
		
		XmlText xt = new XmlText();
		
		ArrayList<Object> lls = new ArrayList<Object>() ;
		final int ST_0 = 0 ;
		final int ST_IN_ENTITY = 1 ;
		final int ST_IN_JSP_DECL = 2 ;
		
		int st = ST_0;
		int p = 0 ;
		int len = pstr.length() ;
		StringBuffer tmp_str = new StringBuffer() ;
		
		while(p<len)
		{
			char c = pstr.charAt(p);
			switch(st)
			{
			case ST_0:
				if(c=='&')
				{
					if(tmp_str.length()>0)
					{
						lls.add(tmp_str.toString());
						tmp_str.delete(0, tmp_str.length());
					}
					
					p ++ ;
					st = ST_IN_ENTITY ;
					break ;
				}
				
				if(c=='<'&&p+2<len&&pstr.charAt(p+1)=='%'&&pstr.charAt(p+2)=='=')
				{
					if(tmp_str.length()>0)
					{
						lls.add(tmp_str.toString());
						tmp_str.delete(0, tmp_str.length());
					}
					
					p += 3 ;
					st = ST_IN_JSP_DECL ;
					break ;
				}
				
				tmp_str.append(c);
				p ++ ;
				break ;
			case ST_IN_ENTITY:
				if(c==';')
				{
					if(tmp_str.length()>0)
					{
						lls.add(new Entity(tmp_str.toString()));
						tmp_str.delete(0, tmp_str.length());
					}
					
					p ++ ;
					st = ST_0 ;
					break;
				}
				
				if(c>='0'&&c<='9' || c>='a'&&c<='z' || c>='A'&&c<='Z')
				{
					tmp_str.append(c);
				}
				else
				{//no entity
					if(tmp_str.length()>0)
					{
						lls.add("&"+tmp_str.toString());
						tmp_str.delete(0, tmp_str.length());
					}
					else
					{
						lls.add("&");
					}
					st = ST_0 ;
					break ;
				}
				p ++ ;
				break ;
			case ST_IN_JSP_DECL:
				if(c=='%'&&p+1<len&&pstr.charAt(p+1)=='>')
				{
					if(tmp_str.length()>0)
					{
						JspExpression je = new JspExpression(tmp_str.toString());
						je.parent = xt ;
						lls.add(je);
						tmp_str.delete(0, tmp_str.length());
					}
					
					p += 2 ;
					st = ST_0 ;
					break;
				}
				tmp_str.append(c);
				p ++ ;
				break ;
			default:
				throw new RuntimeException("parse xml text error!");
			}
		}
		
		if(tmp_str.length()>0)
		{
			lls.add(tmp_str.toString());
			//tmp_str.delete(0, tmp_str.length());
		}
		
		
		xt.content = lls ;
		return xt ;
	}
	
	
//	protected static XmlText parseToXmlText(String pstr,boolean ishtml,boolean enable_coding)
//    {
//        String delim = XML_DELIMITER;
//        if (ishtml)
//            delim = HTML_DELIMITER;
//        XmlText xt = new XmlText(MltagUtil.xmlDecoding(pstr, delim, enable_coding));
//        xt.bEnableCoding = enable_coding;
//        xt.delimiter = delim;
//        return xt;
//    }
	
	//String Entity JspExpression 组成的列表
	private ArrayList<Object> content = new ArrayList<Object>() ;
	//private StringBuffer text = new StringBuffer() ;
	transient private StringBuffer xmlStrVal = null ;
	transient private String valStr = null ;
	boolean bEnableCoding = true;
	
	public XmlText (String text , Node parent)
	{
		this ("#text" , text , parent) ;
	}
	public XmlText (String text)
	{
		this ("#text" , text , null) ;
	}
	
	public XmlText()
	{
		this(null);
	}
	
	public XmlText copyMe()
	{
		XmlText nxt = new XmlText();
		for(Object o:content)
		{
			if(o instanceof String)
			{
				nxt.content.add((String)o);
			}
			else if(o instanceof Entity)
			{
				nxt.content.add(((Entity)o).copyMe()) ;
			}
			else if(o instanceof JspExpression)
			{
				nxt.content.add(((JspExpression)o).copyMe()) ;
			}
		}
		
		return nxt ;
	}
	
	
	/**
	 * 得到输出Xml内容的Text内容
	 * @return
	 */
	public StringBuffer getTextValueForXml()
	{
		if(xmlStrVal!=null)
			return xmlStrVal;
		
		synchronized(this)
		{
			if(xmlStrVal!=null)
				return xmlStrVal;
			
			xmlStrVal = new StringBuffer();
			for(Object o:content)
			{
				if(o instanceof String)
				{
					//MltagUtil.xmlEncoding((String)o, delimiter, bEnableCoding);
					xmlStrVal.append((String)o) ;
				}
				else if(o instanceof Entity)
				{
					xmlStrVal.append(((Entity)o).toEntityStr()) ;
				}
				else if(o instanceof JspExpression)
				{
					xmlStrVal.append(((JspExpression)o).toString()) ;
				}
			}
			return xmlStrVal ;
		}
	}
	
	public String getTextValue()
	{
		if(valStr!=null)
			return valStr;
		
		synchronized(this)
		{
			if(valStr!=null)
				return valStr;
			
			StringBuffer tmpsb = new StringBuffer();
			for(Object o:content)
			{
				if(o instanceof String)
				{
					tmpsb.append((String)o) ;
				}
				else if(o instanceof Entity)
				{
					tmpsb.append(((Entity)o).getCharValue()) ;
				}
				else if(o instanceof JspExpression)
				{
					tmpsb.append(((JspExpression)o).toString()) ;
				}
			}
			valStr = tmpsb.toString() ;
			return valStr ;
		}
	}
	/**
	 * @param nodeName such as #text, #comment, #JspTag, and so on.
	 * @param text the text.
	 * @param parent the parent of this node.
	 * @since 1.5
	 */
	public XmlText (String nodeName , String text , Node parent)
	{
		super (nodeName , parent) ;
		this.content.add(text);
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return toString (tabs , tab);
	}
	
	public String toString (String tabs , String tab)
	{
		return tabs + getTextValueForXml();//MltagUtil.xmlEncoding(text.toString(),delimiter) ;
		// return tabs + escapingString (text) ;
	}
	/**
	 * Will return escaping text.
	 */
	public String toString ()
	{
		return getTextValueForXml().toString();//MltagUtil.xmlEncoding(text.toString(),delimiter) ;
		// return escapingString (text) ;
	}
	
	public ArrayList getTextMembers()
	{
		return content ;
	}
	/**
	 * 得到XmlText表示的Text值--使用时真正的内容,把实体等信息已经做了转换的内容
	 * 以便于真正想获得值的外界程序使用
	 * @return
	 */
	public String getText ()
	{
		return getTextValue();
	}
	
	public void append(String txt)
	{
		content.add(txt);
		xmlStrVal = null ;
		valStr = null ;
	}
	
	public void append(char c)
	{
		content.add(c) ;
		xmlStrVal = null ;
		valStr = null ;
	}

	public void append(char[] cs)
	{
		content.add(cs) ;
		xmlStrVal = null ;
		valStr = null ;
	}
	/**
	 * Replace text.
	 */
	public void setText (String newText)
	{
		content.clear();
		content.add(newText);
		xmlStrVal = new StringBuffer(newText) ;
		valStr = newText ;
//		text.delete(0,text.length());
//		text.append(newText);
		//text = 
	}
	/**
	 * Do Nothing.
	 */
	public void setNodeName (String name)
	{
		// nodeName = name ;
	}
	
	public Attr getAttr(String name)
	{
		return null ;
	}
	
	/**
	 * Do Nothing.
	 */
	public String setAttribute (Attr attr)
	{
		return null ;
	}
	/**
	 * Do Nothing.
	 */
	public void removeAttribute (String name)
	{
	}
	/**
	 * Return a empty enumeration.
	 */
	public Enumeration getAtributeNames ()
	{
		return EMPTY_ENUMERATION ;
	}
	/**
	 * Allways return null.
	 */
	public TreeNode getChildAt (int index)
	{
		
		if(index<0)
			return null ;
		
		ArrayList<JspExpression> jes = getAllJspExp() ;
		if(index>=jes.size())
			return null ;
		return jes.get(index) ;
	}
	
	/**
	 * Do Nothing, return null.
	 */
	public Node setChild (Node child , int i)
	{
		return null ;
	}
	/**
	 * Do Nothing.
	 */
	public void insert (MutableTreeNode child , int i)
	{
	}
	
	/**
	 * Do Nothing.
	 */
	public void remove (int i)
	{
	}
	/**
	 * Do Nothing.
	 */
	public void remove (MutableTreeNode child)
	{
	}

	ArrayList<JspExpression> getAllJspExp()
	{
		ArrayList<JspExpression> rets = new ArrayList<JspExpression>() ;
		for(Object c:content)
		{
			if(c instanceof JspExpression)
			{
				rets.add((JspExpression)c) ;
			}
		}
		return rets ;
	}
	/**
	 * Return a empty enumeration.
	 */
	public Enumeration children ()
	{
		//return EMPTY_ENUMERATION ;
		return new VectorEnumerator (getAllJspExp()) ;
	}
	/**
	 * Allways return 0.
	 */
	public int getChildCount ()
	{
		return getAllJspExp().size() ;
	}
	/**
	 * Allways return true.
	 */
	public boolean isLeaf ()
	{
		return getChildCount ()<=0 ;
	}
	/**
	 * Allways return false.
	 */
	public boolean getAllowsChildren ()
	{
		return true ;
	}
	/**
	 * Allways return -1.
	 */
	public int getIndex (TreeNode node)
	{
		ArrayList<JspExpression> jes = getAllJspExp() ;
		int i = 0 ;
		for(i=0;i<jes.size();i++)
		{
			JspExpression je = jes.get(i) ;
			if(je==node)
				return i ;
		}
		return -1 ;
	}
	
	/**
	 * Escaping char to entity, such as &lt; &amp; &gt;.
	 */
	public static String escapingString (String str)
	{
		StringTokenizer st = new StringTokenizer (str , "<>" , true) ;
		StringBuffer buf = new StringBuffer (str.length ()) ;
		
		while (st.hasMoreTokens ())
		{
			String tk = st.nextToken () ;
			
			if (tk.length () > 1)
				buf.append (tk) ;
			else
			{
				switch (tk.charAt (0))
				{
				// case '\"' :
				//	buf.append ("&quot;") ;
				//	break ;
				// case '&' :
				//	buf.append ("&amp;") ;
				//	break ;
				case '<' :
					buf.append ("&lt;") ;
					break ;
				case '>' :
					buf.append ("&gt;") ;
					break ;
				default :
					buf.append (tk) ;
				}
			}
		}
		
		return buf.toString () ;
	}
}
