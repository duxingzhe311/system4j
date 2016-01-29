package com.dw.mltag ;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

public class VectorEnumerator implements Enumeration
{
	Vector storage ;
			
	int index = 0 ;

	/**
	 * Using an Vector as its argument, create an enumeration
	 * from this storage.
	 */ 
	public VectorEnumerator (Vector list)
	{
		storage = list ;
	}
	
	public VectorEnumerator (ArrayList list)
	{
		storage = new Vector() ;
		storage.addAll(list);
	}
	
	public boolean hasMoreElements ()
	{
		if (storage == null)
			return false ;
		return (index + 1 <= storage.size ()) ;			
	}
	
	
	public Object nextElement ()
	{
		
		if (storage == null || storage.size () <= index)
			throw new NoSuchElementException (
				"VectorEnumerator has no more elements") ;
		return storage.elementAt (index ++) ;
	}
	
}
