package com.dw.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;

public class MsgCmdClient {

	static ILogger log = LoggerManager.getLogger(MsgCmdClient.class.getCanonicalName());

	String host = null;
	int port = MsgCmd.DEFAULT_PORT;

	Socket client = null;

	InputStream inputStream = null;
	OutputStream outputStream = null;

	String userName = null;
	String passwd = null;

	public MsgCmdClient(String host) {
		this(host, MsgCmd.DEFAULT_PORT);
	}

	public MsgCmdClient(String host, int p) {
		this.host = host;
		port = p;
	}

	public MsgCmdClient(String host, String usern, String psw) {
		this(host, MsgCmd.DEFAULT_PORT);

		userName = usern;
		passwd = psw;
	}

	public MsgCmdClient(String host, int p, String usern, String psw) {
		this(host, p);

		userName = usern;
		passwd = psw;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean connect() throws Exception {
		if (client != null)
			return true;

		try {
			client = new Socket(host, port);

			// Get a stream object for reading and writing
			inputStream = client.getInputStream();
			outputStream = client.getOutputStream();

			MsgCmd mc = MsgCmd.packCheckConnRight(userName, passwd);
			MsgCmd retmc = sendCmd(mc);
			if (!MsgCmd.unpackCheckConnRightResult(retmc))
				throw new IOException("check user right failed");

			return true;
		} catch (Exception e) {
			if (client != null) {
				client.close();
				client = null;
			}
			// 出错的情况下，首先要确保连接的关闭，以避免占用资源
			// 同时，必须抛出错误，以使连接池不会出现误判连接成功
			throw e;
		}
	}

	synchronized public MsgCmd sendCmd(MsgCmd mc) throws Exception {
		if (client == null) {
			if (!connect())
				return null;
		}

		try {
			long st = System.currentTimeMillis();
			mc.writeOut(outputStream);
			long et = System.currentTimeMillis();

			st = System.currentTimeMillis();
			MsgCmd retc = MsgCmd.readFrom(inputStream);
			et = System.currentTimeMillis();

			return retc;
		} catch (SocketException se) {
			se.printStackTrace();

			outputStream.close();
			inputStream.close();
			if (client != null) {
				client.close();
				client = null;
			}
			return null;
		}
	}

	public void close() {
		if (client != null) {
			try {
				client.close();
			} catch (Exception e) {

			}
		}
	}

	public boolean isClosed() {
		if (client == null)
			return true;

		return client.isClosed();
	}
}
