package com.dw.mltag;

import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/** 
 * 对于节点内容仅仅是字符串,并且不需要做任何编码/实体转换的内容使用的节点
 * <b>Bugs: </b><br/>
 * <i>Text have only one characeter will be ommited
 * in method {@link #escapingString(String)} -- FIXED.</i>
 */
public class XmlStrValue extends AbstractNode
{
	static String delimiter = "<>" ;
	
	
	protected StringBuffer text = new StringBuffer() ;
	
	
	public XmlStrValue (String text , Node parent)
	{
		this ("#str" , text , parent) ;
	}
	public XmlStrValue (String text)
	{
		this ("#str" , text , null) ;
	}
	
	public XmlStrValue()
	{
		this(null);
	}
	
	
//	/**
//	 * 得到输出Xml内容的Text内容
//	 * @return
//	 */
//	public StringBuffer getTextValueForXml()
//	{
//		return text ;
//	}
	/**
	 * @param nodeName such as #text, #comment, #JspTag, and so on.
	 * @param text the text.
	 * @param parent the parent of this node.
	 * @since 1.5
	 */
	public XmlStrValue (String nodeName , String text , Node parent)
	{
		super (nodeName , parent) ;
		this.text.append(text);
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return toString (tabs , tab);
	}
	
	public String toString (String tabs , String tab)
	{
		return tabs + text.toString() ;
		// return tabs + escapingString (text) ;
	}
	/**
	 * Will return escaping text.
	 */
	public String toString ()
	{
		return text.toString() ;
		// return escapingString (text) ;
	}
	
	/**
	 * 得到XmlText表示的Text值--使用时真正的内容,把实体等信息已经做了转换的内容
	 * 以便于真正想获得值的外界程序使用
	 * @return
	 */
	public String getText ()
	{
		return text.toString() ;
	}
	
	public void append(String txt)
	{
		text.append(txt);
	}
	
	public void append(char c)
	{
		text.append(c) ;
	}

	public void append(char[] cs)
	{
		text.append(cs) ;
	}
	/**
	 * Replace text.
	 */
	public void setText (String newText)
	{
		text.delete(0,text.length());
		text.append(newText);
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
		return null ;
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

	/**
	 * Return a empty enumeration.
	 */
	public Enumeration children ()
	{
		return EMPTY_ENUMERATION ;
	}
	/**
	 * Allways return 0.
	 */
	public int getChildCount ()
	{
		return 0 ;
	}
	/**
	 * Allways return true.
	 */
	public boolean isLeaf ()
	{
		return true ;
	}
	/**
	 * Allways return false.
	 */
	public boolean getAllowsChildren ()
	{
		return false ;
	}
	/**
	 * Allways return -1.
	 */
	public int getIndex (TreeNode node)
	{
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
