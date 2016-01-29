package com.dw.mltag ;

/**
 * A Jsp Page Tag. 
 * Supportted form: <b>&lt;%@ page name="value" %&gt;</b>
 * @since 1.5.1
 */
public class JspDeclaration extends XmlStrValue
{
	public JspDeclaration (String comment)
	{
		super ("#jspDeclaration" , comment , null) ;
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return tabs + "<%!" + text + "%>" ;
	}
	
	public String toString (String tabs , String tab)
	{
		// return tabs + text + NL ;
		return tabs + "<%!" + text + "%>" ;
	}
	
	public String toString ()
	{
		return "<%!" + text + "%>" ;
	} 
}