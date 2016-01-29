package com.dw.mltag ;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public abstract class AbstractNode implements Node
{
	public static String NEED_CHILD_ELEMENTS = "|a|" ;
	public static String META_HTML_ELEMENT = "|script|";
	public static String NO_CHILD_ELEMENTS = "|br|area|meta|link|img|param|hr|input|col|base|" ; //li
	/**
	 * 关键的Html元素，一个html文档中必须有严格的架构，它可以用来控制分析整体结构的完整
	 */
    public static String KEY_HTML_ELEMENT = "|head|body|table|" ;
    
	protected static String NL = System.getProperty ("line.separator") ;
	protected static final Enumeration EMPTY_ENUMERATION = new Enumeration ()
	{
		public boolean hasMoreElements ()
		{
			return false ;
		}

		public Object nextElement ()
		{
			throw new NoSuchElementException ("Enumeration Empty!") ;
		}
	} ;

	public static final int NODE_STYLE_NO_CHILD = 1 ;//不能有子节点，不管有没有/，如<input name="" >
    public static final int NODE_STYLE_NEED_CHILD = 2 ;//必须有子节点  ，如<a href=""/>sdfsa<a>
    public static final int NODE_STYLE_META = 3 ; //元元素，元素包含的内容必须原封不动的分析成txt，如<script> a>3; </script>
    public static final int NODE_STYLE_KEY = 4 ;//关键元素
    
	protected String nodeName ;

	protected Node parent ;

	// private String NO_USED_ELEMENTS = "" ; // "|style|span|div|link|meta|head|b|i|em|big|small|font|a|anchor|" ;
	// private String USED_ATTRIBUTES = "" ; // "|align|valign|halign|width|border|src|cellspacing|cellpadding|height|colspan|rowspan|" ;
	

	// Hashtable attrs ;
	public AbstractNode (String name , Node parent)
	{
		nodeName = name ;

		this.parent = parent ;
	}

	public AbstractNode (String name)
	{
		this (name , null) ;
	}

	public String getNodeName ()
	{
		return nodeName ;
	}

	public void setNodeName (String name)
	{
		nodeName = name ;
	}

	public final String getAttribute (String name)
	{
		Attr a = getAttr(name);
		if(a==null)
			return null ;
		
		return a.getValue();
	}

	public final String setAttribute (String name , String value)
	{
		return setAttribute(new Attr(name,value));
	}

	public abstract void removeAttribute (String name) ;

	public abstract String setAttribute (Attr attr) ;
	
	public abstract Attr getAttr(String name);
//	{
//		if (attr == null)
//			return null ;
//		return setAttribute (attr.getName () , attr.getValue ()) ;
//	}

	public abstract Enumeration getAtributeNames () ;

	public void setAttributes (Attr[] attrs)
	{
		if(attrs==null)
			return ;
		
		for (int i = 0 ; i < attrs.length ; i ++)
			setAttribute (attrs [i]) ;
	}

	public abstract TreeNode getChildAt (int index) ;

	public abstract Node setChild (Node child , int i) ;

	public abstract void insert (MutableTreeNode child , int i) ;


	public void addChild (Node child)
	{
		insert (child , getChildCount ()) ;
	}

	public abstract void remove (int i) ;

	public abstract void remove (MutableTreeNode child) ;

	public void removeAllChildrenByName (String name)
	{
		for (int i = 0 ; i < getChildCount () ;)
			if (((Node) getChildAt (i)).getNodeName ().equals (name))
				remove (i) ;
			else
				i ++ ;
	}
	
	public void removeAllChildren()
	{
		while(getChildCount()>0)
				remove (0) ;
	}

	public Enumeration getAllChildrenByName (String name)
	{
		Vector v = new Vector () ;

		for (int i = 0 ; i < getChildCount () ; i ++)
			if (((Node) getChildAt (i)).getNodeName ().equals (name))
				v.addElement (getChildAt (i)) ;

		return new VectorEnumerator (v) ;
	}

	public abstract Enumeration children () ;

	public abstract int getChildCount () ;

	public boolean hasChildren ()
	{
		return getChildCount () > 0 ;
	}

	public TreeNode getParent ()
	{
		return parent ;
	}

	public void setParent (MutableTreeNode parent)
	{
		/*
		Node oldParent = this.parent ;
		if (this.parent != null)
			this.parent.remove (this) ;

		parent.insert (this , parent.getChildCount ()) ;
		*/
		this.parent = (Node) parent ;
	}

	public void removeFromParent ()
	{
		parent.remove (this) ;
	}

	public String toString (String tabs , String tab,AttrFilter af)
	{
		StringBuffer buf = new StringBuffer (100) ;

		buf.append (tabs + "<" + nodeName) ;
		Enumeration e ;
		for (e = getAtributeNames () ; e.hasMoreElements () ;)
		{
			String attrName = (String) e.nextElement () ;
			String attrv = MltagUtil.xmlEncoding(getAttribute(attrName),Attr.delimiter) ;
			
			if(af!=null&&af.ignoreAttribute(this,attrName, attrv))
				continue ;
				
			if (attrv == null)
				buf.append (" " + attrName) ;
			else
				buf.append (" " + attrName + "=\"" + attrv + "\"") ;
		}
		

		if (hasChildren ())
		{
			buf.append (">") ;
			for (e = children () ; e.hasMoreElements () ;)
			{
				Node node = (Node) e.nextElement () ;
				if (node == null)
					break ;
				
				buf.append (((AbstractNode)node).toString (tabs + tab , tab,af)) ;
			}

			buf.append (tabs + "</" + nodeName + ">") ;
		}
		else
		{
			if(NodeParser.IS_FOR_HTML)
			{// may be has <input >
				if(NodeParser.isHtmlNoChildElement(nodeName))
					buf.append(">") ;
				else
					buf.append("/>");
			}
			else
			{
				buf.append ("/>") ;
			}
		}

		return buf.toString () ;
	}
	
	public String toString (String tabs , String tab)
	{
		return toString(tabs,tab,null);
	}

	public String toString ()
	{
		return toString("","") ;
	}

	public String toString (boolean tabbed)
	{
		if (tabbed)
			return toString ("" , "\t") ;
		else
			return toString ("" , "") ;

	}

	public abstract boolean getAllowsChildren () ;

	public void setUserObject (Object obj)
	{
	}
}