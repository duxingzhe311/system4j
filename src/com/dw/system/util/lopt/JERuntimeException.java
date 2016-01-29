package com.dw.system.util.lopt ;

public class JERuntimeException extends RuntimeException 
{
	Throwable target ;
	public JERuntimeException (String msg)
	{
		this (null , msg) ;
	}
	public JERuntimeException (Throwable _t , String msg)
	{
		super (msg) ;
		target = _t ;
	}
	public Throwable getTargetException ()
	{
		return target ;
	}
}