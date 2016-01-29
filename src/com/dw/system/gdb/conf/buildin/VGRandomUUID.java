package com.dw.system.gdb.conf.buildin;

import java.util.UUID;

import com.dw.system.gdb.conf.BuildInValGenerator;

public class VGRandomUUID extends BuildInValGenerator
{

	@Override
	public String getName()
	{
		return "RANDOM_UUID";
	}

	@Override
	public Object getVal(String[] parms)
	{
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	@Override
	public String getDesc()
	{
		return "一个随机产生的uuid字符串";
	}

}
