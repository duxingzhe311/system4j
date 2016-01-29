package com.dw.mconn;

import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class MsgCmdClient
{
  static ILogger log = LoggerManager.getLogger("MsgCmdClient");

  String host = null;
  int port = 55322;

  Socket client = null;

  InputStream inputStream = null;
  OutputStream outputStream = null;

  String userName = null;
  String passwd = null;

  public MsgCmdClient(String host)
  {
    this(host, 55322);
  }

  public MsgCmdClient(String host, int p)
  {
    this.host = host;
    this.port = p;
  }

  public MsgCmdClient(String host, String usern, String psw)
  {
    this(host, 55322);

    this.userName = usern;
    this.passwd = psw;
  }

  public MsgCmdClient(String host, int p, String usern, String psw)
  {
    this(host, p);

    this.userName = usern;
    this.passwd = psw;
  }

  public String getHost()
  {
    return this.host;
  }

  public int getPort()
  {
    return this.port;
  }

  public boolean connect()
    throws Exception
  {
    if (this.client != null) {
      return true;
    }
    try
    {
      this.client = new Socket(this.host, this.port);

      this.inputStream = this.client.getInputStream();
      this.outputStream = this.client.getOutputStream();

      MsgCmd mc = MsgCmd.packCheckConnRight(this.userName, this.passwd);
      MsgCmd retmc = sendCmd(mc);
      if (!MsgCmd.unpackCheckConnRightResult(retmc)) {
        throw new IOException("check user right failed");
      }
      return true;
    }
    catch (Exception e)
    {
      if (this.client != null)
      {
        this.client.close();
        this.client = null;
      }

      throw e;
    }
  }

  public synchronized MsgCmd sendCmd(MsgCmd mc)
    throws Exception
  {
    if (this.client == null)
    {
      if (!connect()) {
        return null;
      }

    }

    try
    {
      long st = System.currentTimeMillis();
      mc.writeOut(this.outputStream);
      long et = System.currentTimeMillis();

      st = System.currentTimeMillis();
      MsgCmd retc = MsgCmd.readFrom(this.inputStream);
      et = System.currentTimeMillis();

      return retc;
    }
    catch (SocketException se)
    {
      se.printStackTrace();

      this.outputStream.close();
      this.inputStream.close();
      if (this.client != null)
      {
        this.client.close();
        this.client = null;
      }
    }
    return null;
  }

  public void close()
  {
    if (this.client != null)
    {
      try
      {
        this.client.close();
      }
      catch (Exception localException)
      {
      }
    }
  }

  public boolean isClosed()
  {
    if (this.client == null) {
      return true;
    }
    return this.client.isClosed();
  }
}
