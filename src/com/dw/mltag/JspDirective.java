package com.dw.mltag ;

import java.util.Enumeration;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
/**
 * 形如 <%@ 的标签%>
 * Supportted form: <b>&lt;%@ page name="value" %&gt;</b>
 * <b>&lt;%@ include name="value" %&gt;</b>
 * <b>&lt;%@ taglib name="value" %&gt;</b>
 * @since 1.5.1
 */
public class JspDirective extends XmlNode
{
	String jspDirType = "page";
	
	public JspDirective (String jspdir_type)
	{
		super ("#JspDirective_"+jspdir_type) ;
		jspDirType = jspdir_type ;
	}
	
	public String getJspDirectiveType()
	{
		return jspDirType;
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return toString(tabs,tab);
	}
	
	public String toString (String tabs , String tab)
	{
		StringBuffer buf = new StringBuffer (100) ;
		
		buf.append (tabs + "<%@").append(jspDirType) ;
		Enumeration e ;
		for (e = getAtributeNames () ; e.hasMoreElements () ;)
		{
			String attrName = (String) e.nextElement () ;
			
			if (getAttribute (attrName) == null)
				buf.append (" " + attrName) ;
			else
				buf.append (" " + attrName + "=\"" + getAttribute (attrName) + "\"") ;
		}
		buf.append ("%>") ;
			
		return buf.toString () ; 
	}
	
	
	
	// Ignored Methods
	/**
	 * Do Nothing. Jsp Page Tag name can't be changed.
	 */
	public void setNodeName (String name)
	{
		// nodeName = name ;
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
}