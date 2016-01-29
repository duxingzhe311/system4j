package com.dw.mltag ;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

/**
 * package level inclosing classes.
 * for Enumeration using.
 */
 
class ArrayEnumerator implements Enumeration
{
	Object storage [] ;
			
	int index = 0 ;

	/**
	 * Using an Array as its argument, create an enumeration
	 * from this storage.
	 */ 
	ArrayEnumerator (Object [] list)
	{
		storage = list ;
	}
	
	public boolean hasMoreElements ()
	{
		if (storage == null)
			return false ;
		return (index + 1 <= storage.length) ;			
	}
	
	
	public Object nextElement ()
	{
		
		if (storage == null || storage.length <= index)
			throw new NoSuchElementException (
				"ArrayEnumerator has no more elements") ;
		return storage [index ++] ;
	}
	
}
 

class PairsEnumerator implements Enumeration
{
	Hashtable storage ;
	
	Enumeration keys ;
	/**
	 * Using an Hashtable as its argument, create an enumeration
	 * from this storage.
	 */ 
	PairsEnumerator (Hashtable list)
	{
		storage = list ;
		
		keys = storage.keys () ;
	}
	
	public boolean hasMoreElements ()
	{
		return keys.hasMoreElements () ;
	}

	public Object nextElement ()
	{
		String key = (String) keys.nextElement () ;
		
		return new Attr (key , (String) storage.get (key)) ;
	}
	
}