package com.dw.grid;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.dw.system.AppConfig;
import com.dw.system.xmldata.XmlDataWithFile;

/**
 * 用来收集
 * 
 * @author Jason Zhu
 */
public class GridServer {
	private static Object locker = new Object();

	private static GridServer instance = null;

	public static GridServer getInstance() {
		if (instance != null)
			return instance;

		synchronized (locker) {
			if (instance != null)
				return instance;

			instance = new GridServer();
			return instance;
		}
	}

	Thread listenTh = null;

	int defaultPort = GpsMsg.DEFAULT_PORT;

	ServerSocket serverSocket = null;

	IXmlDataWithFileRecvedHandler handler = null;

	private GridServer() {
		// my change port by config
		int gsp = AppConfig.getGridServerPort();
		if (gsp > 0)
			defaultPort = gsp;
	}

	private GridServer(int port) {
		// my change port by config
		if (port > 0)
			defaultPort = port;
	}

	public void registerGpsMsgHandler(IXmlDataWithFileRecvedHandler gpsmh)
			throws IOException {
		handler = gpsmh;
		start();// 只要有消息处理注册，则立刻启动
	}

	public synchronized void start() throws IOException {
		if (listenTh != null)
			return;

		System.out.println("****Start Grid Server**************** on port="
				+ defaultPort);
		serverSocket = new ServerSocket(defaultPort, 100);
		listenTh = new Thread(runner, "grid_server");
		listenTh.start();
	}

	Runnable runner = new Runnable() {
		public void run() {
			try {
				System.out
						.println("****Grid Server begin to accept client connection********");
				while (listenTh != null) {
					Socket ns = serverSocket.accept();
					if (handler == null) {
						try {
							ns.close();
						} catch (Exception eee) {
						}
					}
					GridServerForClient fc = new GridServerForClient(ns,
							handler);
					fc.start();
				}
			} catch (Exception eee) {

			} finally {
				listenTh = null;
			}
		}
	};

	public synchronized void stop() {
		if (listenTh == null)
			return;

		listenTh.interrupt();
		listenTh = null;
	}

	/**
	 * 获得所有的终端连接数
	 * 
	 * @return
	 */
	public int getClientCount() {
		return GridServerForClient.getClientConnCount();
	}

	/**
	 * 获得所有的终端对象
	 * 
	 * @return
	 */
	public GridClientInfo[] getAllClientInfo() {
		GridServerForClient[] fcs = GridServerForClient.getAllClients();
		GridClientInfo[] rets = new GridClientInfo[fcs.length];
		for (int i = 0; i < rets.length; i++) {
			rets[i] = fcs[i].getClientInfo();
		}
		return rets;
	}

	static class TestGpsMsgHandler implements IXmlDataWithFileRecvedHandler {
		public void onXmlDataWithFileRecved(
				XmlDataWithFile.CombinedFileHead cfh, File combined_file)
				throws Exception {
			GpsMsg gpsmsg = new GpsMsg();
			gpsmsg.fromXmlDataCombinedFileHead(cfh);
			System.out.println("recved at file="
					+ combined_file.getCanonicalPath());
			System.out.println(">>msg type=" + gpsmsg.getMsgType());
			if (gpsmsg.msgBody != null) {
				System.out
						.println(">>msg body=" + gpsmsg.msgBody.toXmlString());
			}
			int picnum = 0;
			if (gpsmsg.gpsPics != null)
				picnum = gpsmsg.gpsPics.size();

			System.out.println(">>msg pic num=" + picnum);
		}
	}

	public static void main(String[] args) throws Throwable {// test
		GridServer gs = new GridServer(-1);
		gs.registerGpsMsgHandler(new TestGpsMsgHandler());
	}
}
