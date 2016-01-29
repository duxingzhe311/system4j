package com.dw.net.msgqueue;

public interface IMsgQueueIO
{
	public byte[] getMsgFromServer(String quename)
		throws Exception;//, out byte[] cont, out string failedreson);

	public void addMsgToServer(String quename, byte[] cont)
		throws Exception;//, out String failedreson);
}
