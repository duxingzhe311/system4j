package com.dw.system.xmldata.join;

public interface IJoinAdapterProvider
{
	public String[] getJoinAdapterNames();
	
	public String getJoinAdapterTitleByName(String n);
	
	public JoinAdapter getJoinAdapterByName(String n);
}
