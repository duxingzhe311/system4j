package com.dw.mltag;

public class Entity
{
	String entStr = null ;
	
	public Entity(String str_ent)
	{
		if(str_ent==null)
			throw new IllegalArgumentException("entity str cannot be null");
		
		entStr = str_ent ;
		char c = getCharValue() ;
		if(c<0)
			throw new IllegalArgumentException("unknow entity string:"+str_ent);
	}
	
	public Entity(char c)
	{
		entStr = MltagUtil.getEncodeStr(c) ;
	}
	
	public Entity copyMe()
	{
		return new Entity(entStr);
	}
	
	public String toEntityStr()
	{
		return "&"+entStr + ";";
	}
	
	public String toString()
	{
		return toEntityStr();
	}
	
	public char getCharValue()
	{
		return MltagUtil.getDecodeChar(entStr);
	}
	
}
