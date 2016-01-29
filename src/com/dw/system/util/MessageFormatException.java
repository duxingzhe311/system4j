package com.dw.system.util ;

import java.io.IOException ;

/**
 * Exception class, will be thrown during extract or pack message. 
 */
public class MessageFormatException extends IOException
{
	/**
	 * Construct a Exception with a string.
	 */
	public MessageFormatException (String msg)
	{
		super (msg) ;
	}
	
}