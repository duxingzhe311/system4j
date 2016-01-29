package com.dw.grid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.Vector;

import com.dw.system.Convert;
import com.dw.system.encrypt.DES;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlDataWithFile;

class GridServerForClient implements Runnable {
	static ILogger log = LoggerManager.getLogger(GridServerForClient.class);

	static Object lockObj = new Object();

	static Vector<GridServerForClient> ALL_CLIENTS = new Vector<GridServerForClient>();

	static void increaseCount(GridServerForClient c) {
		synchronized (lockObj) {
			ALL_CLIENTS.add(c);
		}
	}

	static void decreaseCount(GridServerForClient c) {
		synchronized (lockObj) {
			ALL_CLIENTS.remove(c);
		}
	}

	public static int getClientConnCount() {
		return ALL_CLIENTS.size();
	}

	public static GridServerForClient[] getAllClients() {
		synchronized (lockObj) {
			GridServerForClient[] rets = new GridServerForClient[ALL_CLIENTS
					.size()];
			ALL_CLIENTS.toArray(rets);
			return rets;
		}
	}

	// MsgCmdServer server = null;
	// IMsgCmdHandler cmdHandler = null ;
	Socket tcpClient = null;

	Thread thread = null;

	// boolean bRun = false;

	private GridClientInfo clientInfo = null;

	private IXmlDataWithFileRecvedHandler handler = null;

	public GridServerForClient(Socket tcp, IXmlDataWithFileRecvedHandler h) {
		// cmdHandler = cmdhandler ;
		tcpClient = tcp;
		handler = h;

		clientInfo = new GridClientInfo(tcp.getInetAddress().getHostAddress(),
				tcp.getPort());
	}

	public GridClientInfo getClientInfo() {
		return clientInfo;
	}

	synchronized public void start() {
		if (thread != null)
			return;

		thread = new Thread(this, "GridClientThread");
		thread.start();
	}

	synchronized public void stop() {
		if (thread == null)
			return;

		close();
	}

	public void run() {
		try {
			increaseCount(this);

			System.out.println("accept By Grid Server！！！！");
			// Get a stream object for reading and writing
			InputStream instream = tcpClient.getInputStream();
			OutputStream outstream = tcpClient.getOutputStream();

			// send uuid 向客户端发送随机串
			String uuid = UUID.randomUUID().toString();
			outstream.write(uuid.getBytes());
			outstream.write((byte) '\n');
			outstream.flush();

			// 客户端根据自己的id和密钥串把随机串加密，回答
			// id=服务器的随机串加密串
			// 验证阻塞不能超过5s
			this.tcpClient.setSoTimeout(5000);//
			String idsec = null;
			try {
				idsec = Util.readStreamLine(instream, 1000);
			} catch (SocketTimeoutException stoe) {
				throw new Exception("auth time out!");
			}
			int p = idsec.indexOf('=');
			if (p <= 0)
				throw new Exception("invalid id security str");

			long id = Long.parseLong(idsec.substring(0, p));
			String sec_str = idsec.substring(p + 1);
			String seckey = null;
			if (id == 0) {// test
				seckey = "12345678";
			} else {
				GridClientItem cci = GridClientManager.getInstance()
						.getClientById(id);
				if (cci == null)
					throw new Exception("no client found!");
				seckey = cci.getSecKey();
			}

			if (Convert.isNullOrEmpty(seckey))
				throw new Exception("no key!");

			// 解密串和原随机串对比，相同表示ok
			String dec_str = DES.decode(sec_str, seckey);
			if (!uuid.equals(dec_str))
				throw new Exception("check security key failed!");

			outstream.write("ok\n".getBytes());
			outstream.flush();

			// 收取数据阻塞不能超过10s
			this.tcpClient.setSoTimeout(10000);//
			// ok begin trans data ;
			FileOutputStream fos = null;
			File recvf = null;
			XmlDataWithFile xdwf = new XmlDataWithFile();
			try {
				String name = UUID.randomUUID().toString();
				// if(id==0)
				// {
				// recvf = new File("./test_recv_xdwf/"+name+".qi") ;
				// if(!recvf.getParentFile().exists())
				// {
				// recvf.getParentFile().mkdirs() ;
				// }
				// }
				// else
				{
					recvf = GridManager.getInstance().getTmpRecvFile(name);
					fos = new FileOutputStream(recvf);
				}

				XmlDataWithFile.StreamRecvCB recv = new XmlDataWithFile.StreamRecvCB(
						fos);
				xdwf.readFromStream(instream, recv);
				XmlDataWithFile.CombinedFileHead cfh = recv
						.getCombinedFileHead();

				handler.onXmlDataWithFileRecved(cfh, recvf);

				outstream.flush();
				// tell client succ
				outstream.write("succ\n".getBytes());
				outstream.flush();
			} catch (Exception recve) {
				recve.printStackTrace();
				if (fos != null) {
					fos.close();
					fos = null;
				}

				if (recvf != null)
					recvf.delete();
			} finally {
				if (fos != null)
					fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Console.WriteLine(e.GetType().FullName + "\n" + e.Message + "\n"
			// + e.StackTrace);
			if (log.isErrorEnabled())
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

		Thread t = this.thread;
		if (t != null) {
			try {
				t.interrupt();
			} catch (Exception eee) {
			}

			thread = null;
		}
	}
}