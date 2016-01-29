package com.dw.mltag ;

/**
 * A Class to handle Jsp Tag in jsp File.
 * @since 1.5
 */
public class JspTag extends XmlStrValue
{
	public JspTag (String comment)
	{
		super ("#jspTag" , comment , null) ;
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return tabs + "<%" + text + "%>" ;
	}
	
	public String toString (String tabs , String tab)
	{
		// return tabs + text + NL ;
		return tabs + "<%" + text + "%>" ;
	}
	
	public String toString ()
	{
		return "<%" + text + "%>" ;
	} 
}