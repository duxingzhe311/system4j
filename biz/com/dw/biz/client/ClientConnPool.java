package com.dw.biz.client;

import com.dw.system.Configuration;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import java.util.Properties;
import java.util.Vector;

public class ClientConnPool
  implements Runnable
{
  static ILogger log = LoggerManager.getLogger(ClientConnPool.class
    .getCanonicalName());

  static ClientConnPool clientConnPool = null;
  private String poolName;
  private int totalNewConnections;
  private String host;
  private int port;
  private String username;
  private String password;
  private int maxConnections;
  private boolean waitIfBusy;
  private Vector availableConnections;
  private Vector busyConnections;
  private boolean connectionPending;
  private String testSql;
  private static long LIFE_TIME = 900000L;

  private static int MAX_ACCESS_COUNT = 100;

  private transient Properties connProp = null;

  public static ClientConnPool getConnPool()
  {
    if (clientConnPool != null) {
      return clientConnPool;
    }
    synchronized (log)
    {
      if (clientConnPool != null) {
        return clientConnPool;
      }

      Properties ps = 
        Configuration.getSubPropertyByPrefix("workflow_server");
      if ((ps == null) || (ps.size() <= 0)) {
        throw new RuntimeException(
          "no workflow_server.xxx prop set in configuration");
      }
      try
      {
        clientConnPool = new ClientConnPool(ps);
        return clientConnPool;
      }
      catch (Exception e)
      {
        throw new RuntimeException(
          "create conn pool failed,may be invalid workflow_server.xxx in configuration");
      }
    }
  }

  public ClientConnPool(Properties p)
    throws Exception
  {
    this.connProp = p;

    this.totalNewConnections = 0;
    this.connectionPending = false;

    this.testSql = null;
    this.host = p.getProperty("host");
    if ((this.host == null) || (this.host.equals("")))
      this.host = "localhost"; this.port = Integer.parseInt(p.getProperty("port", "55322"));

    this.username = p.getProperty("username");
    this.password = p.getProperty("password");
    this.poolName = p.getProperty("pool.name");
    int initnum;
    int maxnum;
    try { int initnum = Integer.parseInt(p.getProperty("initnumber"));
      maxnum = Integer.parseInt(p.getProperty("maxnumber"));
    }
    catch (Throwable e)
    {
      int maxnum;
      initnum = 1;
      maxnum = 2;
    }

    init(this.host, this.port, this.username, this.password, initnum, maxnum, true);
  }

  public Properties getConnProp()
  {
    return this.connProp;
  }

  protected void init(String host, int port, String username, String password, int initialConnections, int maxConnections, boolean waitIfBusy)
    throws Exception
  {
    try
    {
      String logName = this.poolName;
      if (this.poolName == null)
        logName = username;
    }
    catch (Throwable _t)
    {
      _t.printStackTrace();
    }

    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.maxConnections = maxConnections;
    this.waitIfBusy = waitIfBusy;
    if (initialConnections > maxConnections)
      initialConnections = maxConnections;
    this.availableConnections = new Vector(maxConnections);
    this.busyConnections = new Vector();
    try
    {
      for (int i = 0; i < initialConnections; i++)
        this.availableConnections.addElement(new ConnectionWarpper(
          makeNewConnection()));
    }
    catch (Throwable _t)
    {
      log.error("conn pool init error!");
    }
  }

  public int getMaxConnectionNumber()
  {
    return this.maxConnections;
  }

  public int getCurrentConnectionNumber()
  {
    return this.availableConnections.size();
  }

  public synchronized void setMaxConnectionNumber(int maxlen)
  {
    if (maxlen <= this.availableConnections.size())
    {
      return;
    }

    this.maxConnections = maxlen;
  }

  public synchronized WFClient getConnection()
    throws Exception
  {
    while (true)
    {
      while (!this.availableConnections.isEmpty())
      {
        ConnectionWarpper existingConnection = (ConnectionWarpper)this.availableConnections
          .elementAt(0);
        this.availableConnections.removeElementAt(0);
        existingConnection.accessCount += 1;
        if (existingConnection.isClosed())
        {
          existingConnection.close();
        }
        else
        {
          this.busyConnections.addElement(existingConnection);
          return existingConnection.conn;
        }
      }
      if ((totalConnections() < this.maxConnections) && (!this.connectionPending))
      {
        makeForegroundConnection();
      }
      else
      {
        if (!this.waitIfBusy)
          throw new Exception("Connection limit reached");
        try
        {
          wait();
        }
        catch (Throwable ie)
        {
          ie.printStackTrace();
        }
      }
    }
  }

  private void makeBackgroundConnection()
  {
    this.connectionPending = true;
    try
    {
      Thread connectThread = new Thread(this, "biz_client_connpool");
      connectThread.start();
    }
    catch (OutOfMemoryError oome)
    {
      oome.printStackTrace();
    }
  }

  private void makeForegroundConnection() throws Exception
  {
    WFClient conn = makeNewConnection();
    if (conn == null)
    {
      throw new Exception(
        "Can't Create new Connection. Connection is NULL.");
    }

    free(conn);
  }

  public void run()
  {
    try
    {
      WFClient connection = makeNewConnection();
      free(connection);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      try
      {
        synchronized (this)
        {
          this.connectionPending = false;
          notify();
        }
      }
      catch (Throwable _t)
      {
        _t.printStackTrace();
      }
    }
  }

  private WFClient makeNewConnection()
    throws Exception
  {
    return new WFClient(this.host, this.port, this.username, this.password);
  }

  public synchronized void free(WFClient connection)
  {
    ConnectionWarpper cw = new ConnectionWarpper(connection);
    int index = this.busyConnections.indexOf(cw);
    if (index >= 0)
    {
      cw = (ConnectionWarpper)this.busyConnections.elementAt(index);
      this.busyConnections.removeElementAt(index);
    }
    if (!this.availableConnections.contains(cw))
      this.availableConnections.addElement(cw);
    notify();
  }

  public int totalConnections()
  {
    return this.availableConnections.size() + this.busyConnections.size();
  }

  public synchronized void close()
  {
    closeConnections(this.availableConnections);
    this.availableConnections = new Vector();
    closeConnections(this.busyConnections);
    this.busyConnections = new Vector();
  }

  private void closeConnections(Vector connections)
  {
    for (int i = 0; i < connections.size(); i++)
      ((ConnectionWarpper)connections.elementAt(i)).close();
  }

  public String toString()
  {
    String info = "DefaultConnectionPool(" + this.host + "," + this.port + "," + 
      this.username + ")" + ", available=" + this.availableConnections.size() + 
      ", busy=" + this.busyConnections.size() + ", max=" + 
      this.maxConnections;
    return info;
  }

  protected void finalize() throws Throwable
  {
    try
    {
      close();
    }
    catch (Throwable _t)
    {
      _t.printStackTrace();
    }
  }

  private class ConnectionWarpper
  {
    long createTime;
    int accessCount;
    WFClient conn;
    boolean closed;

    public ConnectionWarpper(WFClient conn)
    {
      this.createTime = System.currentTimeMillis();
      this.accessCount = 0;
      this.conn = null;
      this.closed = false;
      this.conn = conn;
    }

    public boolean equals(Object obj)
    {
      return ((obj instanceof ConnectionWarpper)) && 
        (((ConnectionWarpper)obj).conn == this.conn);
    }

    public boolean isClosed()
    {
      try
      {
        this.closed = this.conn.isClosed();
      }
      catch (Throwable _t)
      {
        return true;
      }

      return (this.closed) || 
        (System.currentTimeMillis() - this.createTime > ClientConnPool.LIFE_TIME) || 
        (this.accessCount > ClientConnPool.MAX_ACCESS_COUNT);
    }

    public void close()
    {
      try
      {
        if (!this.closed)
          this.conn.close();
      }
      catch (Throwable e)
      {
        e.printStackTrace();
      }
    }
  }
}
