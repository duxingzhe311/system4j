package com.dw.comp;

public interface ICompListener
{
	/**
	 * ������������ʱ,����֪����webapp�µ�Ŀ¼,��context·������
	 * @param realpath
	 * @param comp_context_name
	 */
	public void onCompFinding(AppInfo ci);
	
	
	//public void onCompUndeploy(AppInfo ci) ;
}
