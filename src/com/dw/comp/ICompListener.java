package com.dw.comp;

public interface ICompListener
{
	/**
	 * 当构件被发现时,可以知道其webapp下的目录,及context路径名称
	 * @param realpath
	 * @param comp_context_name
	 */
	public void onCompFinding(AppInfo ci);
	
	
	//public void onCompUndeploy(AppInfo ci) ;
}
