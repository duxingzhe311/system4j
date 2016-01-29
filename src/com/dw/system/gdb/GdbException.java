package com.dw.system.gdb;

import java.io.*;
import java.sql.*;

public class GdbException extends Exception
{
	Exception oex = null;

	SQLException sqlE = null;

	public GdbException()
	{
	}

	public GdbException(Exception oe)
	{
		oex = oe;
		if (oe instanceof SQLException)
		{
			sqlE = (SQLException) oe;
		}
	}

	public GdbException(String msg)
	{
		super(msg);
	}

	public GdbException(String msg, Exception oe)
	{
		super(msg);
		oex = oe;
		if (oe instanceof SQLException)
		{
			sqlE = (SQLException) oe;
		}
	}

	// / <summary>
	// / 获得原始的SqlException
	// / </summary>
	public SQLException getSqlException()
	{
		return sqlE;
	}

	public String getStackTraceStr()
	{
		StringWriter sw = new StringWriter();
		PrintWriter pt = new PrintWriter(sw);
		this.printStackTrace(pt);
		return sw.toString();
	}

	public String getMessage()
	{
		if (oex == null)
			return super.getMessage();
		StringBuilder sb = new StringBuilder();
		sb.append("\r\n--------DataAccessException Message-------\r\n");
		sb.append(super.getMessage());
		sb.append("\r\n--------原始错误信息[").append(
				oex.getClass().getCanonicalName()).append("]---------\r\n");
		sb.append(oex.getMessage());
		if (oex instanceof SQLException)
		{
			SQLException sqle = (SQLException) oex;
			sb.append("\r\nSqlException.Number=").append(sqle.getErrorCode());
			StringWriter sw = new StringWriter();
			PrintWriter pt = new PrintWriter(sw);
			oex.printStackTrace(pt);
			sb.append("\r\n").append(sw.toString());
		}

		sb.append("\r\n******************************************\r\n");
		return sb.toString();
	}
}
