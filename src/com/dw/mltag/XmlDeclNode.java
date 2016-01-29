package com.dw.mltag ;

public class XmlDeclNode extends XmlStrValue
{
	public XmlDeclNode (String name)
	{
		super (name) ;
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return tabs + "<!" + text + ">" ;
	}
	
	public String toString (String tabs , String tab)
	{
		// return tabs + text + NL ;
		return tabs + "<!" + text + ">" ;
	}
	
	public String toString ()
	{
		return "<!" + text + ">" ;
	} 
}