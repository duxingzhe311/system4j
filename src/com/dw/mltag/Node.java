package com.dw.mltag ;

import java.util.Enumeration ;
import javax.swing.tree.MutableTreeNode ;

/**
 * We need a simple, stable easy for used XML toolkits, so we have one.<br/> 
 * We use TreeNode instand of W3C DOM standard. We can easily bulid a tree 
 * for a XML document. <br/>
 * To see it's method, Only two node type we needed, Normal Node & TextNode.
 * All of the other be omitted.
 * @see XmlNode
 * @see XmlComment
 * @see XmlText
 * @see JspTag 
 * @see JspComment
 */

public interface Node extends MutableTreeNode
{
	// For nodeName 
	/**
	 * The name of Node, <br/>
	 * if node is a standard <b>tag</b>, will return tag name;
	 * if node is a Text, will return "#text";
	 * if node is a Html Comment, will return "#comment";
	 * if node is a XML document declaration, will return "#decl";
	 * if node is a Jsp Tag, will return "#jspTag";
	 * if node ia s Jsp Comment, will return "#jspComment".
	 */
	public String getNodeName () ;
	
	/** 
	 * Set the Node Name.
	 */
	public void setNodeName (String name) ;
	
	// For Attributes 
	/**
	 * Gets Attribute Value by name.
	 */
	public String getAttribute (String name) ;
	/**
	 * Sets Attribute value.
	 */
	public String setAttribute (String name , String value) ;
	
	/**
	 * Uodate attribute object.
	 */
	public String setAttribute (Attr attr) ;
	
	public Enumeration getAtributeNames () ;
	
	public void setAttributes (Attr attrs []) ;
	
	// For Child
	public Node setChild (Node child , int i) ;
	
	public void addChild (Node child) ;
	
	public void removeAllChildrenByName (String name) ;
	
	public Enumeration getAllChildrenByName (String name) ;
	
	public boolean hasChildren () ;
	// ---------------------------------------------------
	// from javax.swing.tree.TreeNode
	// ---------------------------------------------------
	/*
	public Enumeration children () ;
	public boolean getAllowsChildren () ;
	public TreeNode getChildAt (int index) ; 
	public int getIndex (TreeNode node) ;
	public int getChildCount () ;
	public TreeNode getParent () ;
	public boolean isLeaf () ;
	*/
	// -----------------------------------------------------
	// From javax.swing.tree.MutableTreeNode
	// -----------------------------------------------------
	/*
	public void insert (MutableTreeNode child , int index) ;
	public void remove (int index) ;
	
	public void remove (MutableTreeNode node) ;
	public void removeFromParent () ;
	public void setParent (MutableTreeNode newParent) ;
	
	public void setUserObject (Object object) ;
	*/
	//public String toString (String indents , String indent,AttrFilter af) ;
	public String toString (String indents , String indent) ;
	public String toString (boolean indented) ;
}
	