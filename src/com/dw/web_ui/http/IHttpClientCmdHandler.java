package com.dw.web_ui.http;

import java.util.HashMap;

import com.dw.user.LoginSession;

public interface IHttpClientCmdHandler
{
	/**
	 * ÃüÁîÃû³Æ
	 * @return
	 */
	public String getCmdName();
	
	public boolean doCmd(LoginSession ls,HashMap<String, String> parms,
			HashMap<String, byte[]> files,
			HashMap<String, String> out_ret_parms,
			HashMap<String, byte[]> out_ret_files,
			StringBuilder succ_message,StringBuilder failedreson)
		throws Exception;
}
