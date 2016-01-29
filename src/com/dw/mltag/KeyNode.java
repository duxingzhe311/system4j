package com.dw.mltag;

import java.util.ArrayList;
import java.util.Enumeration;

public class KeyNode extends XmlNode
{
	KeyNode keyNodeParent = null ;

	ArrayList keyNodeChildren = new ArrayList() ;

	public KeyNode(String name)
	{
		super(name);
	}

	public KeyNode (String name , AbstractNode parent)
	{
		super(name,parent);
	}

	protected AbstractNode copyMe()
	{
		KeyNode tmpxn = new KeyNode(this.nodeName) ;
		for(Object at : attrs)
		{
			Attr att = (Attr)at ;
			tmpxn.attrs.add(att.copyMe()) ;
		}
		return tmpxn ;
	}

	public void addKeyNodeChild(KeyNode kn)
	{
		if(kn==this)
			throw new IllegalArgumentException() ;

		if(this.keyNodeChildren.contains(kn))
			return ;

		keyNodeChildren.add(kn) ;
		kn.keyNodeParent = this ;
	}

	public int getKeyNodeChildCount()
	{
		return keyNodeChildren.size() ;
	}

	public KeyNode getKeyNodeIdx(int idx)
	{
		if(idx<0||idx>=getKeyNodeChildCount())
			return null ;

		return (KeyNode)this.keyNodeChildren.get(idx);
	}

	public void RemoveKeyNodeChild(KeyNode kn)
	{
		if(!keyNodeChildren.contains(kn))
			return ;

		this.keyNodeChildren.remove(kn) ;
		kn.keyNodeParent = null ;
	}

	public KeyNode getParentKeyNode()
	{
		return this.keyNodeParent ;
	}

	public void removeFromParent()
	{
		if(this.keyNodeParent==null)
			return ;

		keyNodeParent.RemoveKeyNodeChild(this) ;
	}

	public String toKeyNodeString (String tabs , String tab,AttrFilter af)
	{
		StringBuilder buf = new StringBuilder (100) ;

		buf.append (tabs + "<" + nodeName) ;
		for (Enumeration e = this.getAtributeNames() ; e.hasMoreElements() ;)
		{
			String attrName = (String) e.nextElement();
			String attrv = MltagUtil.xmlEncoding(getAttribute(attrName),Attr.delimiter) ;
		
			if(af!=null&&af.ignoreAttribute(this,attrName, attrv))
				continue ;
			
			if (attrv == null)
				buf.append (" " + attrName) ;
			else
				buf.append (" " + attrName + "=\"" + attrv + "\"") ;
		}
	

		int cnum = this.getKeyNodeChildCount() ;
		if (cnum>0)
		{
			buf.append (">") ;
			for (int i = 0 ; i < cnum ; i ++)
			{
				KeyNode node = getKeyNodeIdx(i) ;
				buf.append (node.toKeyNodeString (tabs + tab , tab,af)) ;
			}

			buf.append (tabs + "</" + nodeName + ">") ;
		}
		else
		{
//			if(IS_FOR_HTML)
//			{// may be has <input >
//				if(isHtmlNoChildElement(nodeName))
//					buf.Append(">") ;
//				else
//					buf.Append("/>");
//			}
//			else
//			{
//				buf.Append ("/>") ;
//			}
			buf.append ("/>") ;
		}

		return buf.toString () ;
	}
}
