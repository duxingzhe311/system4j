package com.dw.net.util;


public interface ITestConnListener
{
	public void onNetLineStrRecved(TestConnForClient conn,byte[] rdata,int dlen) ;
}
