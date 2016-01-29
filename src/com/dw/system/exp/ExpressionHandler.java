package com.dw.system.exp ;

import java.io.*;
import java.util.*;

public interface ExpressionHandler
{
	public Argument onExpression (Argument arg1,Argument arg2,String oper) ;
}