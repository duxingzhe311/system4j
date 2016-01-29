package com.dw.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;

public class MsgCmdServer implements Runnable {
	static ILogger log = LoggerManager.getLogger("MsgCmdServer");

	int port = MsgCmd.DEFAULT_PORT;

	ServerSocket server = null;
	Thread serverThread = null;
	boolean bRun = false;

	IMsgCmdHandler handler = null;

	public MsgCmdServer(IMsgCmdHandler h) {
		this(MsgCmd.DEFAULT_PORT, h);
	}

	public MsgCmdServer(int port, IMsgCmdHandler h) {
		this.port = port;
		handler = h;
	}

	/**
	 * 得到当前客户端连接数
	 * 
	 * @return
	 */
	public int getClientConnCount() {
		return MsgCmdServerForClient.getClientConnCount();
	}

	public MsgCmdClientInfo[] getClientConnInfos() {
		MsgCmdServerForClient[] sfc = MsgCmdServerForClient.getAllClients();
		if (sfc == null)
			return null;

		MsgCmdClientInfo[] rets = new MsgCmdClientInfo[sfc.length];
		for (int i = 0; i < sfc.length; i++) {
			rets[i] = sfc[i].getClientInfo();
		}

		return rets;
	}

	synchronized public void Start() {
		if (serverThread != null)
			return;

		bRun = true;
		serverThread = new Thread(this, "msg_cmd_server");
		serverThread.start();
	}

	synchronized public void Stop() {
		bRun = false;
	}

	public void run() {
		try {
			server = new ServerSocket(port, 100);
			System.out
					.println("MsgCmd Server started..<<<<<.,ready to recv client connection on port="
							+ port);
			while (bRun) {
				Socket client = server.accept();

				MsgCmdServerForClient sfc = new MsgCmdServerForClient(handler,
						client);
				sfc.Start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		} finally {
			close();

			serverThread = null;
		}
	}

	public void close() {
		if (server != null) {
			try {
				server.close();
			} catch (Exception e) {
			}
		}
	}
}

class MsgCmdServerForClient implements Runnable {
	static ILogger log = LoggerManager.getLogger("MsgCmdServerForClient");

	static Object lockObj = new Object();
	static Vector<MsgCmdServerForClient> ALL_CLIENTS = new Vector<MsgCmdServerForClient>();

	static void increaseCount(MsgCmdServerForClient c) {
		synchronized (lockObj) {
			ALL_CLIENTS.add(c);
		}
	}

	static void decreaseCount(MsgCmdServerForClient c) {
		synchronized (lockObj) {
			ALL_CLIENTS.remove(c);
		}
	}

	public static int getClientConnCount() {
		return ALL_CLIENTS.size();
	}

	public static MsgCmdServerForClient[] getAllClients() {
		synchronized (lockObj) {
			MsgCmdServerForClient[] rets = new MsgCmdServerForClient[ALL_CLIENTS
					.size()];
			ALL_CLIENTS.toArray(rets);
			return rets;
		}
	}

	IMsgCmdHandler cmdHandler = null;
	Socket tcpClient = null;

	Thread thread = null;
	boolean bRun = false;

	private MsgCmdClientInfo clientInfo = null;

	public MsgCmdServerForClient(IMsgCmdHandler cmdhandler, Socket tcp) {
		cmdHandler = cmdhandler;
		tcpClient = tcp;

		clientInfo = new MsgCmdClientInfo(
				tcp.getInetAddress().getHostAddress(), tcp.getPort(), null);
	}

	public MsgCmdClientInfo getClientInfo() {
		return clientInfo;
	}

	synchronized public void Start() {
		if (thread != null)
			return;

		bRun = true;
		thread = new Thread(this, "MsgCmdThread");
		thread.start();
	}

	synchronized public void Stop() {
		bRun = false;
	}

	public void run() {
		try {
			increaseCount(this);
			// Get a stream object for reading and writing
			InputStream instream = tcpClient.getInputStream();
			OutputStream outstream = tcpClient.getOutputStream();

			{// check user right
				MsgCmd mc = MsgCmd.readFrom(instream);
				if (mc == null)
					return;

				String[] ups = MsgCmd.unpackCheckConnRight(mc);
				String usern = null;
				String psw = null;
				if (ups != null && ups.length == 2) {
					usern = ups[0];
					psw = ups[1];
				}

				boolean bright = false;
				try {
					bright = cmdHandler.checkConnRight(usern, psw);
				} catch (Throwable _t) {
					bright = false;
				}

				if (!bright) {
					MsgCmd failmc = MsgCmd.packCheckConnRightResult(false);
					failmc.writeOut(outstream);
					return;
				}

				MsgCmd succmc = MsgCmd.packCheckConnRightResult(true);
				succmc.writeOut(outstream);

				clientInfo.loginUser = usern;
			}

			while (bRun) {
				// get cmd from client
//				long st = System.currentTimeMillis();
				MsgCmd mc = MsgCmd.readFrom(instream);

				if (mc == null)
					break;// connect break

//				long et = System.currentTimeMillis();

				// server doing
//				st = System.currentTimeMillis();
				mc = cmdHandler.OnCmd(mc, clientInfo);// server.DoCmd(mc);
//				et = System.currentTimeMillis();

				if (mc == null)
					break;

				// reply
//				st = System.currentTimeMillis();
				mc.writeOut(outstream);
//				et = System.currentTimeMillis();
			}
		} catch (Exception e) {
			log.error("消息传输错误－断开连接" + e.getMessage());
		} finally {
			close();

			decreaseCount(this);
		}
	}

	public void close() {
		if (tcpClient != null) {
			try {
				tcpClient.close();
				tcpClient = null;
			} catch (Exception e) {
			}
		}
	}
}
