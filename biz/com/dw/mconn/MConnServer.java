package com.dw.mconn;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MConnServer
  implements Runnable
{
  static ILogger log = LoggerManager.getLogger("MsgCmdServer");

  int port = 55322;

  ServerSocket server = null;
  Thread serverThread = null;
  boolean bRun = false;

  IMsgCmdHandler handler = null;

  public MConnServer(IMsgCmdHandler h)
  {
    this(55322, h);
  }

  public MConnServer(int port, IMsgCmdHandler h)
  {
    this.port = port;
    this.handler = h;
  }

  public int getClientConnCount()
  {
    return MsgCmdServerForClient.getClientConnCount();
  }

  public MsgCmdClientInfo[] getClientConnInfos()
  {
    MsgCmdServerForClient[] sfc = MsgCmdServerForClient.getAllClients();
    if (sfc == null) {
      return null;
    }
    MsgCmdClientInfo[] rets = new MsgCmdClientInfo[sfc.length];
    for (int i = 0; i < sfc.length; i++)
    {
      rets[i] = sfc[i].getClientInfo();
    }

    return rets;
  }

  public synchronized void Start()
  {
    if (this.serverThread != null) {
      return;
    }
    this.bRun = true;
    this.serverThread = new Thread(this, "msg_cmd_server");
    this.serverThread.start();
  }

  public synchronized void Stop()
  {
    this.bRun = false;
  }

  public void run()
  {
    try
    {
      this.server = new ServerSocket(this.port, 100);

      System.out.println("MsgCmd Server started..<<<<<.,ready to recv client connection on port=" + this.port);
      while (this.bRun)
      {
        Socket client = this.server.accept();

        MsgCmdServerForClient sfc = new MsgCmdServerForClient(this.handler, client);
        sfc.Start();
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();

      log.error(e);
    }
    finally
    {
      close();

      this.serverThread = null;
    }
  }

  public void close()
  {
    if (this.server != null)
    {
      try
      {
        this.server.close();
      }
      catch (Exception localException)
      {
      }
    }
  }
}
