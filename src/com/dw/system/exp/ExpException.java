package com.dw.system.exp ;

import java.io.*;
import java.util.*;

public class ExpException extends RuntimeException
{
	public ExpException (String str)
	{
		super (str) ;
	}

	public String toString()
	{
		return super.toString () ;
	}
}