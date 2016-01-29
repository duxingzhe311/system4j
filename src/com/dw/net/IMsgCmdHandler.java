package com.dw.net;

public interface IMsgCmdHandler {
	boolean checkConnRight(String username, String psw);

	MsgCmd OnCmd(MsgCmd mc, MsgCmdClientInfo ci);
}
