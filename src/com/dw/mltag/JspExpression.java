package com.dw.mltag;

public class JspExpression extends XmlStrValue
{
	public JspExpression (String comment)
	{
		super ("#jspExpression" , comment , null) ;
	}
	
	public JspExpression copyMe()
	{
		return new JspExpression(text.toString());
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return tabs + "<%=" + text + "%>" ;
	}
	
	public String toString (String tabs , String tab)
	{
		// return tabs + text + NL ;
		return tabs + "<%=" + text + "%>" ;
	}
	
	public String toString ()
	{
		return "<%=" + text + "%>" ;
	} 
}