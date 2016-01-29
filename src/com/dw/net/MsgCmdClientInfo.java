package com.dw.net;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;

@XRmi(reg_name = "msgcmd_clientinfo")
public class MsgCmdClientInfo implements IXmlDataable {
	private String clientIP = null;
	private int port = -1;

	String loginUser = null;

	public MsgCmdClientInfo() {
	}

	public MsgCmdClientInfo(String addrip, int p, String luser) {
		clientIP = addrip;
		port = p;
		loginUser = luser;
	}

	public String getClientIPAddr() {
		return clientIP;
	}

	public int getClientPort() {
		return port;
	}

	public String getConnUserName() {
		if (loginUser == null)
			return "";

		return loginUser;
	}

	public XmlData toXmlData() {
		XmlData xd = new XmlData();

		xd.setParamValue("client_ip", clientIP);
		xd.setParamValue("client_port", port);
		if (loginUser != null)
			xd.setParamValue("conn_user", loginUser);

		return xd;
	}

	public void fromXmlData(XmlData xd) {
		clientIP = xd.getParamValueStr("client_ip");
		port = xd.getParamValueInt32("client_port", -1);
		loginUser = xd.getParamValueStr("conn_user");
	}
}
