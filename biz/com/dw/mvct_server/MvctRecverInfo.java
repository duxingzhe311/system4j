package com.dw.mvct_server;

import java.util.ArrayList;
import java.util.List;

public class MvctRecverInfo
{
  String recverId = null;

  String recverHost = null;

  int recverPort = -1;

  ArrayList<String> clientIds = new ArrayList();

  long pulseTimeMS = System.currentTimeMillis();

  public MvctRecverInfo()
  {
  }

  public MvctRecverInfo(String recverid, String host, int port, List<String> clientids) {
    this.recverId = recverid;
    this.recverHost = host;
    this.recverPort = port;
    this.clientIds.addAll(clientids);
  }

  public boolean isTimeout()
  {
    return System.currentTimeMillis() - this.pulseTimeMS > 30000L;
  }

  public String getRecverId()
  {
    return this.recverId;
  }

  public String getRecverHost()
  {
    return this.recverHost;
  }

  public int getRecverPort()
  {
    return this.recverPort;
  }

  public List<String> getClientIds()
  {
    return this.clientIds;
  }

  public String getClientIdsStr()
  {
    if ((this.clientIds == null) || (this.clientIds.size() <= 0)) {
      return "";
    }
    int s = this.clientIds.size();
    StringBuilder sb = new StringBuilder();
    sb.append((String)this.clientIds.get(0));
    for (int i = 1; i < s; i++) {
      sb.append(',').append((String)this.clientIds.get(i));
    }
    return sb.toString();
  }
}
