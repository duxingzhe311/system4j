package com.dw.mltag ;

/**
 * A Class to handle Jsp Comment in jsp File.
 * @since 1.5
 */
public class JspComment extends XmlStrValue
{
	public JspComment (String comment)
	{
		super ("#jspComment" , comment , null) ;
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return toString(tabs,tab);
	}
	
	public String toString (String tabs , String tab)
	{
		// return tabs + text + NL ;
		return tabs + "<%--" + text + "--%>" ;
	}
	
	public String toString ()
	{
		return "<%--" + text + "--%>" ;
	} 
}