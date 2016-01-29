package com.dw.mltag ;

/**
 * Attribute, a pair in "name-value" mode.
 */
public class Attr implements Cloneable
{
	public final static String delimiter = "><&\'\"\r\n";
	
	static void parseAttrValue(Attr a,String val)
	{
		a.value = XmlText.parseToXmlText(val);
		//a.setValue(MltagUtil.xmlDecoding(val,delimiter));
	}
	
	String name ;
	XmlText value ;
	
	public Attr (String name , String value)
	{
		this.name = name ;
		this.value = XmlText.parseToXmlText(value);//convertAttrValue (value) ;
	}
	
	private Attr(String name,XmlText val)
	{
		this.name = name ;
		value = val ;
	}
	
	public Attr(String name)
	{
		this(name,"");
	}
	
	public Attr ()
	{
		this.name = "" ;
		this.value = new XmlText("") ;
	}
	
	public Object clone()
    	throws CloneNotSupportedException
	{
		return copyMe();
	}
	
	public Attr copyMe()
	{
		XmlText val = null ;
		if(value!=null)
			val = value.copyMe();
		return new Attr(name,val);
	}
	
	public String getName ()
	{
		return name ;
	}
	
	public String getValue ()
	{
		if(value==null)
			return null ;
		return value.getText() ;
	}
	
	public void setName (String name)
	{
		this.name = name ;
	}
	
	public void setValue (String value)
	{
		this.value.setText(value);
		//this.value = convertAttrValue (value) ;
	}
	
	public XmlText getXmlTextValue()
	{
		return value ;
	}
	
	public static String convertAttrValue (String value)
	{
		if (value == null)
			return null ;
		char [] buffer = value.toCharArray () ;
		
		for (int i = 0 ; i < buffer.length ; i ++)
		{
			if (buffer [i] == '\"')
				buffer [i] = '\'' ;
		}
		
		return new String (buffer) ;
	}

}
	