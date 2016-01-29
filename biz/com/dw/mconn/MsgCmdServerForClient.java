package com.dw.mconn;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

class MsgCmdServerForClient
  implements Runnable
{
  static ILogger log = LoggerManager.getLogger("MsgCmdServerForClient");

  static Object lockObj = new Object();
  static Vector<MsgCmdServerForClient> ALL_CLIENTS = new Vector();

  IMsgCmdHandler cmdHandler = null;
  Socket tcpClient = null;

  Thread thread = null;
  boolean bRun = false;

  private MsgCmdClientInfo clientInfo = null;

  static void increaseCount(MsgCmdServerForClient c)
  {
    synchronized (lockObj)
    {
      ALL_CLIENTS.add(c);
    }
  }

  static void decreaseCount(MsgCmdServerForClient c)
  {
    synchronized (lockObj)
    {
      ALL_CLIENTS.remove(c);
    }
  }

  public static int getClientConnCount()
  {
    return ALL_CLIENTS.size();
  }

  public static MsgCmdServerForClient[] getAllClients()
  {
    synchronized (lockObj)
    {
      MsgCmdServerForClient[] rets = new MsgCmdServerForClient[ALL_CLIENTS.size()];
      ALL_CLIENTS.toArray(rets);
      return rets;
    }
  }

  public MsgCmdServerForClient(IMsgCmdHandler cmdhandler, Socket tcp)
  {
    this.cmdHandler = cmdhandler;
    this.tcpClient = tcp;

    this.clientInfo = new MsgCmdClientInfo(
      tcp.getInetAddress().getHostAddress(), 
      tcp.getPort(), null);
  }

  public MsgCmdClientInfo getClientInfo()
  {
    return this.clientInfo;
  }

  public synchronized void Start()
  {
    if (this.thread != null) {
      return;
    }
    this.bRun = true;
    this.thread = new Thread(this, "MsgCmdThread");
    this.thread.start();
  }

  public synchronized void Stop()
  {
    this.bRun = false;
  }

  public void run()
  {
    try {
      try {
        increaseCount(this);

        InputStream instream = this.tcpClient.getInputStream();
        OutputStream outstream = this.tcpClient.getOutputStream();

        MsgCmd mc = MsgCmd.readFrom(instream);
        if (mc == null) {
          return;
        }
        String[] ups = MsgCmd.unpackCheckConnRight(mc);
        String usern = null;
        String psw = null;
        if ((ups != null) && (ups.length == 2))
        {
          usern = ups[0];
          psw = ups[1];
        }

        boolean bright = false;
        try
        {
          bright = this.cmdHandler.checkConnRight(usern, psw);
        }
        catch (Throwable _t)
        {
          bright = false;
        }

        if (!bright)
        {
          MsgCmd failmc = MsgCmd.packCheckConnRightResult(false);
          failmc.writeOut(outstream);
          return;
        }

        MsgCmd succmc = MsgCmd.packCheckConnRightResult(true);
        succmc.writeOut(outstream);

        this.clientInfo.loginUser = usern;

        while (this.bRun)
        {
          long st = System.currentTimeMillis();
          MsgCmd mc = MsgCmd.readFrom(instream);

          if (mc == null) {
            break;
          }
          long et = System.currentTimeMillis();

          st = System.currentTimeMillis();
          mc = this.cmdHandler.OnCmd(mc, this.clientInfo);
          et = System.currentTimeMillis();

          if (mc == null)
          {
            break;
          }
          st = System.currentTimeMillis();
          mc.writeOut(outstream);
          et = System.currentTimeMillis();
        }

      }
      catch (Exception e)
      {
        log.error("消息传输错误－断�?���? + e.getMessage());
      }
    }
    finally {
      close();

      decreaseCount(this);
    }
    close();

    decreaseCount(this);
  }

  public void close()
  {
    if (this.tcpClient != null)
    {
      try
      {
        this.tcpClient.close();
        this.tcpClient = null;
      }
      catch (Exception localException)
      {
      }
    }
  }
}
