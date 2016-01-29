package com.dw.system.gdb.conf.buildin;

import java.sql.Timestamp;

import com.dw.system.gdb.conf.BuildInValGenerator;

public class VGCurTimestamp extends BuildInValGenerator
{
	public String getName()
	{
		return "CURRENT_TIMESTAMP";
	}

	public Object getVal(String[] parms)
	{
		return new Timestamp(System.currentTimeMillis());
	}

	public String getDesc()
	{
		return "当前时间戳";
	}
}
