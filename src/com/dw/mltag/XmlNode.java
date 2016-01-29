package com.dw.mltag ;
                    
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class XmlNode extends AbstractNode
{
	protected boolean bHasEndTag = false ;
	Vector children ;
	//Hashtable attrs ;
	protected Vector attrs = new Vector() ;
	
	/**
	 * 在一些使用场合,节点虽然符合最基本的标签格式,但从语义上可能不符合正确使用的
	 * 需要,如某种标签必须有某种属性,否则就认为有问题.
	 */
	transient Throwable exception = null ;
	
	public XmlNode (String name)
	{
		this (name , (Node)null) ;
	}
	
	public XmlNode (String name,Throwable exce)
	{
		this (name , null,exce) ;
	}
	
	public XmlNode (String name , Node parent)
	{
		this(name,parent,null);
	}
	
	public XmlNode (String name , Node parent,Throwable exception)
	{
		super (name , parent) ;
		
		children = new Vector () ;
		
		this.exception = exception ;
	}
	
	public Attr[] copyAttrs()
	{
		Attr[] rets = new Attr[attrs.size()];
		for(int i = 0 ; i < rets.length ; i ++)
		{
			Attr tmpat = (Attr)attrs.elementAt(i) ;
			rets[i] = tmpat.copyMe();
		}
		return rets ;
	}
	
	
	public String setAttribute (Attr attr)
	{
		String n = attr.getName() ;
		if(n==null)
			return null ;
		
		if(attr.getValue()==null)
		{
			String v = this.getAttribute(n);
			this.removeAttribute(n);
			return v ;
		}
		else
		{
			return setAttr (attr) ;
		}
	}
	
	public String setAttr (Attr attr)
	{
		return setAttr(attr,true);
	}
	
	public String setAttr (Attr attr,boolean ignorenamecase)
    {
		if(ignorenamecase)
			attr.setName(attr.getName().toLowerCase());
		
		int c = attrs.size() ;
		for(int i = 0 ; i < c ; i ++)
		{
			Attr att = (Attr)attrs.get(i) ;
			if(ignorenamecase)
			{
				if(att.getName().equalsIgnoreCase(attr.getName()))
				{
					String tmps = att.getValue();
					//att.setValue(attr.getValue()) ;
					attrs.setElementAt(attr, i);
					return tmps;
				}
			}
			else
			{
				if(att.getName().equals(attr.getName()))
				{
					String tmps = att.getValue();
					//att.setValue(attr.getValue()) ;
					attrs.setElementAt(attr, i);
					return tmps;
				}
			}
		}
        //attrs[attr.Name]=attr ;
		attrs.add(attr) ;
		return null ;
    }
	
	public Attr getAttr(String name)
	{
		return getAttr(name,true);
	}

    public Attr getAttr(String name,boolean ignorenamecase)
    {
		if(ignorenamecase)
			name = name.toLowerCase() ;

		for(Iterator ir  = attrs.iterator() ; ir.hasNext();)
		{
			Attr att = (Attr)ir.next();
			if(ignorenamecase)
			{
				if(name.equalsIgnoreCase(att.getName()))
					return att ;
			}
			else
			{
				if(name.equals(att.getName()))
					return att ;
			}
		}

		return null ;
    }

	public void removeAttribute (String name)
	{
		attrs.remove (name);
	}
	
	public Enumeration getAtributeNames ()
	{
		Vector tmpv = new Vector();
		int c = attrs.size() ;
		String[] ss = new String[c];
		for(int i = 0 ; i < c ; i ++)
		{
			tmpv.addElement(((Attr)attrs.get(i)).getName()) ;
		}

		return tmpv.elements() ;
	}

	public TreeNode getChildAt (int index)
	{
		return (Node) children.elementAt (index) ;
	}
	
	public AbstractNode getChildByIdx(int idx)
	{
		return (AbstractNode) children.elementAt (idx) ;
	}
	
	public XmlNode getFirstXmlNode()
	{
		int cc = this.getChildCount() ;
		if(cc<=0)
			return null ;
		
		for(int i = 0 ; i < cc ; i ++)
		{
			TreeNode tn = this.getChildAt(i);
			if(tn instanceof XmlNode)
			{
				return (XmlNode)tn ;
			}
		}
	
		return null ;
	}
	
	public Node setChild (Node child , int i)
	{
		Node oldNode = (Node) children.elementAt (i) ;
		
		children.setElementAt (child , i) ;
		
		return oldNode ;
	}
	
	public Node setChild (Collection childs , int i)
	{
		Node oldNode = (Node) children.remove(i);
		
		
		children.addAll(i, childs);
		
		return oldNode ;
	}
	
	public void insert (MutableTreeNode child , int i)
	{
		children.insertElementAt (child , i) ;
		child.setParent (this) ;
	}
	
	
	
	
	
	public void remove (int i)
	{
		AbstractNode node = (AbstractNode) children.elementAt (i) ;
		children.removeElementAt (i) ;
		
		node.parent = null ;
	}
	
	public void remove (MutableTreeNode child)
	{
		children.removeElement (child) ;
		
		((AbstractNode) child).parent = null ;
	}
	
	public Enumeration children ()
	{
		return new VectorEnumerator (children) ;
	}
	
	public int getChildCount ()
	{
		return children.size () ;
	}
	
	public Collection getChildren()
	{
		return children ;
	}
	
	public boolean isLeaf ()
	{
		return children.isEmpty () ;
	}
	
	public List<XmlNode> getSubXmlNodeByTag(String tagn)
	{
		ArrayList<XmlNode> rets = new ArrayList<XmlNode>() ;
		for(Object o:children)
		{
			if(!(o instanceof XmlNode))
				continue ;
			
			XmlNode tmpn = (XmlNode)o;
			if(tmpn.getNodeName().equals(tagn))
				rets.add(tmpn);
		}
		
		return rets ;
	}
	
	public List getSubXmlNodeByType(Class t)
	{
		ArrayList rets = new ArrayList() ;
		for(Object o:children)
		{
			if(t.isInstance(o))
				rets.add(o);
		}
		
		return rets ;
	}
	
	public boolean getAllowsChildren ()
	{
		return true ;
	}
	
	public int getIndex (TreeNode node)
	{
		return children.indexOf (node) ;
	}
	
	public boolean isHasEndTag()
	{
		return this.bHasEndTag;
	}
}

	