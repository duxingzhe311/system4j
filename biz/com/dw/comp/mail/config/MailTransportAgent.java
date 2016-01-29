package com.dw.comp.mail.config;

import java.io.Serializable;

public class MailTransportAgent
  implements Serializable
{
  private String m_Name = "Default";

  private String m_Address = "localhost";

  private int m_Port = -1;

  private boolean m_Secure = false;

  private String m_Protocol = "smtp";

  private boolean m_Authenticated = false;

  private int m_TransportLimit = 2048;

  private String m_systemUser = "";

  private String m_systemPsw = "";

  public String getName()
  {
    return this.m_Name;
  }

  public void setName(String name)
  {
    this.m_Name = name;
  }

  public String getProtocol()
  {
    return this.m_Protocol;
  }

  public void setProtocol(String protocol)
  {
    this.m_Protocol = protocol;
  }

  public String getAddress()
  {
    return this.m_Address;
  }

  public void setAddress(String address)
  {
    this.m_Address = address;
  }

  public int getPort()
  {
    return this.m_Port;
  }

  public void setPort(int port)
  {
    this.m_Port = port;
  }

  public boolean isSecure()
  {
    return this.m_Secure;
  }

  public void setSecure(boolean secure)
  {
    this.m_Secure = secure;
  }

  public boolean isAuthenticated()
  {
    return this.m_Authenticated;
  }

  public void setAuthenticated(boolean auth)
  {
    this.m_Authenticated = auth;
  }

  public int getTransportLimit()
  {
    return this.m_TransportLimit;
  }

  public void setTransportLimit(int size)
  {
    this.m_TransportLimit = size;
  }

  public String getSystemAccount()
  {
    return this.m_systemUser;
  }

  public void setSystemAccount(String sa)
  {
    this.m_systemUser = sa;
  }

  public String getSystemPsw()
  {
    return this.m_systemPsw;
  }

  public void setSystemPsw(String sa)
  {
    this.m_systemPsw = sa;
  }
}
