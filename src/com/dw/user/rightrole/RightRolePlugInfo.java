package com.dw.user.rightrole;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RightRolePlugInfo
{
	public static final String PLUG_NAME = "name";

	public static final String DATA_PROVIDER_VIEW = "rightroleplug.data.provider.view";
	
	public static final String DATA_PROVIDER_ACTION = "rightroleplug.data.provider.action";

	public static final String DATA_HANDLER_ACTION = "rightroleplug.data.handler.action";
	
	private String appName = "";

	private Map<String, String> appPlugMap = new HashMap<String, String>();

	public RightRolePlugInfo(String appName, HashMap<String, String> hm)
	{
		this.appName = appName;
		if (null != hm && !hm.isEmpty())
			this.appPlugMap.putAll(hm);
	}

	public void setAppPlugMap(Map<String, String> appPlugMap)
	{
		this.appPlugMap = appPlugMap;
	}

	public String getAppName()
	{
		return appName;
	}

	public String getPlugName()
	{
		return appPlugMap.get(PLUG_NAME);
	}

	public Map<String, String> getAppPlugMap()
	{
		return appPlugMap;
	}

	public String getDataProviderView()
	{
		return appPlugMap.get(DATA_PROVIDER_VIEW);
	}

	public String getDataProviderAction()
	{
		return appPlugMap.get(DATA_PROVIDER_ACTION);
	}

	public String getDataHandlerAction()
	{
		return appPlugMap.get(DATA_HANDLER_ACTION);
	}

}
