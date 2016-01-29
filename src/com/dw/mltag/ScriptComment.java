package com.dw.mltag;

public class ScriptComment extends XmlNode
{
	private XmlText txtComment = null ;
	
	public ScriptComment(String txt)
	{
		super("#comment");
		
		if(txt!=null)
		{
			txtComment = XmlText.parseToXmlText(txt);
			this.addChild(txtComment);
		}
	}
	
	@Override
	public String toString (String tabs , String tab,AttrFilter af)
	{
		if(txtComment!=null)
			return tabs + "<!--" + txtComment.toString() + "-->" ;
		else
			return tabs + "<!---->" ;
	}
	
	public String toString (String tabs , String tab)
	{
		if(txtComment!=null)
			return tabs + "<!--" + txtComment.toString() + "-->" ;
		else
			return tabs + "<!---->" ;
	}
	
	public String toString (boolean b)
	{
		if(txtComment!=null)
			return "<!--" + txtComment.toString() + "-->" ;
		else
			return "<!---->" ;
	}
	
	public String toString ()
	{
		if(txtComment!=null)
			return "<!--" + txtComment.toString() + "-->" ;
		else
			return "<!---->" ;
	} 
}
