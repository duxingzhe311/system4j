package com.dw.mltag ;

/**
 * The Comment in XML(HTML). (&lt;!-- --&gt;)
 */
public class XmlComment extends XmlStrValue
{
	public XmlComment (String comment)
	{
		super ("#comment" , comment , null) ;
	}
	
	public String toString (String tabs , String tab,AttrFilter af)
	{
		return tabs + "<!--" + text + "-->" ;
	}
	
	public String toString (String tabs , String tab)
	{
		// return tabs + text + NL ;
		return tabs + "<!--" + text + "-->" ;
	}
	
	public String toString ()
	{
		return "<!--" + text + "-->" ;
	} 
}